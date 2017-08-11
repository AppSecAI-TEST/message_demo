package person.terry.message.basic_nio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by terry on 2017/8/10.
 */
public class MultiThreadReactor implements Runnable {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public MultiThreadReactor(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor());
    }

    /*
        Alternatively, use explicit SPI provider:
        SelectorProvider p = SelectorProvider.provider();
        selector = p.openSelector();
        serverSocket = p.openServerSocketChannel();
    */
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                while (it.hasNext())
                    dispatch((SelectionKey) it.next());
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) k.attachment();
        if (r != null)
            r.run();
    }

    class Acceptor implements Runnable {

        public void run() {
            try {
                SocketChannel c = serverSocketChannel.accept();
                if (c != null)
                    new Handler(selector, c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    final class Handler implements Runnable {

        private static final int MAXIN = 1024;
        private static final int MAXOUT = 1024;
        final SocketChannel socket;
        final SelectionKey sk;
        ByteBuffer input = ByteBuffer.allocate(MAXIN);
        ByteBuffer output = ByteBuffer.allocate(MAXOUT);
        static final int READING = 0, SENDING = 1, PROCESSING = 3;
        int state = READING;

        private final Executor pool = Executors.newCachedThreadPool();

        public Handler(Selector selector, SocketChannel c) throws IOException {
            socket = c;
            c.configureBlocking(false);
            sk = socket.register(selector, 0);
            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        boolean inputIsComplete() {
            //todo
            return false;
        }

        boolean outputIsComplete() {
            //todo
            return false;
        }

        void process() {

        }

        public void run() {
            try {
                if (state == READING) {
                    read();
                } else if (state == SENDING) {
                    pool.execute(new Sender());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        synchronized void read() throws IOException {
            socket.read(input);
            if (inputIsComplete()) {
                state = PROCESSING;
                pool.execute(new Processor());  // Offload non-IO processing to other threads
            }
        }

        synchronized void processAndHandOff() {
            process();
            state = SENDING;
            sk.interestOps(SelectionKey.OP_WRITE);
            sk.selector().wakeup();
        }

        class Processor implements Runnable {
            public void run() {
                processAndHandOff();
            }
        }

        class Sender implements Runnable {
            public void run() {
                try {
                    socket.write(output);
                    if (outputIsComplete())
                        sk.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }


}
