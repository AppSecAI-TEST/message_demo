package person.terry.message.basic_nio.reactor.finish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by terry on 2017/8/10.
 *
 * io thread assign subReactor
 *
 * <p>
 * Reactor中的init方法里的isMainReactor字段即是用来判断是否该Reactor是否为mainReactor的，如果是mainReactor的话，则注册感兴趣的为ACCEPT事件，并且添加Acceptor附件
 * 然后run方法里面的while循环即是EventLoop轮询了，需要注意的是这里有坑：
 * 别使用阻塞的select方法，因为该方法会导致accept后subReactor的selector在register的时候会一直阻塞；也别使用非阻塞的selecNow方法，因为selectNow在无限循环下即使没有IO事件，也会使CPU飙到100%；所以最终选择使用带有超时的select(timeout)方法
 */
public abstract class Acceptor extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

    protected final Selector selector;
    protected final ServerSocketChannel serverSocketChannel;
    protected final boolean useMultipleReactors;

    public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel, boolean useMultipleReactors) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.useMultipleReactors = useMultipleReactors;
    }

    @Override
    public void run() {
        logger.info(selector + " accept...");
        try {
            SocketChannel clientChannel = serverSocketChannel.accept();
            if (clientChannel != null) {
                logger.info(selector + " clientChannel not null...");
                //如果使用阻塞的select方式，且目的是开启了多个reactor池，而不是mainReactor和subReactor的关系的话，
                //则下面就不是nextSubSelector().selector，而是改为传递当前实例的selector对象即可
                handle(useMultipleReactors ? ServerContext.nextSubReactor().selector : selector, clientChannel);
            } else {
                logger.info(selector + " clientChannel is null...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 在每个具体的Handler下调用run方法是为了令其从connecting状态变为reading状态，
     * 和原pdf版本下的做法是一样的，只不过原pdf版本是在构造函数直接修改设置了感兴趣为read事件
     */
    public abstract void handle(Selector selector, SocketChannel clientChannel);

}
