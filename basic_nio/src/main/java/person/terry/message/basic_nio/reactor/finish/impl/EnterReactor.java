package person.terry.message.basic_nio.reactor.finish.impl;

import person.terry.message.basic_nio.reactor.finish.Acceptor;
import person.terry.message.basic_nio.reactor.finish.Reactor;
import person.terry.message.basic_nio.reactor.finish.ServerContext;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by terry on 2017/8/11.
 */
public class EnterReactor extends Reactor {

    public EnterReactor(int port, ServerSocketChannel serverSocketChannel, boolean isMainReactor, boolean userMultipleReactors, long timeout) {
        super(port, serverSocketChannel, isMainReactor, userMultipleReactors, timeout);
    }

    public Acceptor newAcceptor(Selector selector) {
        return new EnterAcceptor(selector, serverSocketChannel, userMultipleReactors);
    }

    public static void main(String[] args) throws IOException {
        //new EnterReactor(9003, ServerSocketChannel.open(), true, false, TimeUnit.MILLISECONDS.toMillis(10)).start();
        ServerContext.startMultipleReactor(9003, EnterReactor.class);
        //ServerContext.startSingleReactor(9003, EnterReactor.class);
    }

}
