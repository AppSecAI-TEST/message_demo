package person.terry.message.basic_nio.reactor.finish.impl;

import person.terry.message.basic_nio.reactor.finish.Acceptor;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by terry on 2017/8/11.
 */
public class EnterAcceptor extends Acceptor {

    public EnterAcceptor(Selector selector, ServerSocketChannel serverSocketChannel, boolean useMultipleReactors) {
        super(selector, serverSocketChannel, useMultipleReactors);
    }

    public void handle(Selector selector, SocketChannel clientChannel) {
        new EnterHandler(clientChannel, selector).run();
    }

}
