package person.terry.message.basic_nio.reactor.finish;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by terry on 2017/8/10.
 */
public abstract class Reactor extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(Reactor.class);

    protected final int port;
    protected final ServerSocketChannel serverSocketChannel;
    protected final boolean isMainReactor;
    protected final boolean userMultipleReactors;
    protected final long timeout;
    protected Selector selector;

    public Reactor(int port, ServerSocketChannel serverSocketChannel, boolean isMainReactor, boolean userMultipleReactors, long timeout) {
        this.port = port;
        this.serverSocketChannel = serverSocketChannel;
        this.isMainReactor = isMainReactor;
        this.userMultipleReactors = userMultipleReactors;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            init();
            while (!Thread.interrupted()) {
                if (selector.select(timeout) > 0) {
                    logger.info(selector + " isMainReactor=" + isMainReactor + " select...");
                    Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();
                    while (keyIt.hasNext()) {
                        SelectionKey key = keyIt.next();
                        dispatch(key);
                        keyIt.remove();
                    }
                }
            }
            logger.info(getClass().getSimpleName() + " end on " + port + " ..." + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();
        logger.info(selector + " isMainReactor=" + isMainReactor);
        if (isMainReactor) {
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            key.attach(newAcceptor(selector));
            logger.info(getClass().getSimpleName() + "start on " + port + " ..." + "\n");
        }

        //如果使用阻塞的select方式，且开启下面的代码的话，相当于开启了多个reactor池，而不是mainReactor和subReactor的关系了
        //SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        //key.attach(newAcceptor(selector, serverChannel));

    }

    public abstract Acceptor newAcceptor(Selector selector);

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) key.attachment();
        if (r != null)
            r.run();
    }

}
