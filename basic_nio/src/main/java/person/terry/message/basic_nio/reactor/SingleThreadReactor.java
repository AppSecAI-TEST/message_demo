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

/**
 * Created by terry on 2017/8/10.
 */
public class SingleThreadReactor implements Runnable {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public SingleThreadReactor(int port) throws IOException {
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
        static final int READING = 0, SENDING = 1;
        int state = READING;

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
                socket.read(input);   // initial state read
                if (inputIsComplete()) {
                    process();
                    sk.attach(new Sender());
                    sk.interestOps(SelectionKey.OP_WRITE);
                    sk.selector().wakeup();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public void run() {
//            try {
//                if (state == READING) read();
//                else if (state == SENDING) send();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }


        void read() throws IOException {
            socket.read(input);
            if (inputIsComplete()) {
                process();
                state = SENDING;
                // Normally also do first write now
                sk.interestOps(SelectionKey.OP_WRITE);
            }
        }

//        void send() throws IOException {
//            socket.write(output);
//            if (outputIsComplete()) sk.cancel();
//        }

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
