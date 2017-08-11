package person.terry.message.basic_nio.reactor.finish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by terry on 2017/8/10.
 * <p>
 * handle read and write
 */
public abstract class Handler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    private enum State {

        CONNECTING(0),
        READING(SelectionKey.OP_READ),
        PROCESSING(2),
        WRITING(SelectionKey.OP_WRITE);
        private final int opBit;

        State(int operateBit) {
            opBit = operateBit;
        }

    }

    private State state;
    protected final SocketChannel clientChannel;
    protected final SelectionKey key;

    protected final ByteBuffer readBuf;
    protected final StringBuilder readData = new StringBuilder();
    protected ByteBuffer writeBuf;


    public Handler(SocketChannel clientChannel, Selector selector) {
        this.state = State.CONNECTING;
        SelectionKey key = null;
        try {
            clientChannel.configureBlocking(false);
            key = clientChannel.register(selector, this.state.opBit);
            key.attach(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.clientChannel = clientChannel;
        this.key = key;
        this.readBuf = ByteBuffer.allocate(byteBufferSize());
        logger.info(selector + " connect success...");
    }

    @Override
    public void run() {
        switch (state) {
            case CONNECTING:
                connect();
                break;
            case READING:
                readAndProcess();
                break;
            case WRITING:
                write();
                break;
            default:
                logger.error("\nUnsupported State: " + state + " ! overlap processing with IO...");
        }
    }


    private synchronized void readAndProcess() {
        doRead();
        doProcess();
    }


    private void doRead() {
        int readSize;
        try {
            while ((readSize = clientChannel.read(readBuf)) > 0) {
                readData.append(new String(Arrays.copyOfRange(readBuf.array(), 0, readSize)));
                readBuf.clear();
            }
            if (readSize == -1) {
                disconnect();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
        logger.info("readed from client:" + readData + ", " + readData.length());
    }


    private void doProcess() {
        if (readIsComplete()) {
            state = State.PROCESSING;
            processAndInterestWrite();
        }
    }


    private void connect() {
        interestOps(State.READING);
    }


    /**
     * 处理过程可能是比较耗时的，所以可考虑将其交由线程池处理，处理完毕后才注册感兴趣的write事件<p>
     * 然而正是由于交由线程池处理所以可能造成重叠IO的多线程处理的状态问题，最好能一次性全部读入buffer，否则考虑同步状态处理问题
     */
    private void processAndInterestWrite() {
        Processor processor = new Processor();
        if (ServerContext.userThreadPool) {
            ServerContext.execute(processor);
        } else {
            processor.run();
        }
    }

    private final class Processor implements Runnable {
        public void run() {
            processAndHandOff();
        }
    }

    private synchronized void processAndHandOff() {
        if (process()) {
            interestOps(State.WRITING);
        }
    }

    public boolean process() {
        logger.info("process readData=" + readData.toString());
        if (isQuit()) {
            disconnect();
            return false;
        }

        writeBuf = ByteBuffer.wrap(readData.toString().getBytes());
        readData.delete(0, readData.length());
        return true;
    }

    private void write() {
        try {
            do {
                clientChannel.write(writeBuf);
            } while (!writeIsComplete());
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }

        String writeData = new String(Arrays.copyOf(writeBuf.array(), writeBuf.array().length));
        logger.info("writed to client:" + writeData + ", " + writeData.length());
        interestOps(State.READING);
    }


    private void interestOps(State state) {
        this.state = state;
        key.interestOps(state.opBit);
    }

    private boolean isQuit() {
        return false;
    }

    private void disconnect() {
        try {
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("\nclient Address=【" + clientAddress(clientChannel) + "】 had already closed!!! ");
    }

    private static SocketAddress clientAddress(SocketChannel clientChannel) {
        return clientChannel.socket().getRemoteSocketAddress();
    }

    public abstract int byteBufferSize();

    public abstract boolean readIsComplete();

    public abstract boolean writeIsComplete();

}


