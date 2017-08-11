package person.terry.message.basic_nio.reactor.finish.impl;

import person.terry.message.basic_nio.reactor.finish.Handler;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by terry on 2017/8/11.
 */
public class EnterHandler extends Handler {

    private static final String ENTER = "\r\n";
    private static final String QUIT = "quit";

    public EnterHandler(SocketChannel clientChannel, Selector selector) {
        super(clientChannel, selector);
    }

    public int byteBufferSize() {
        return 1;
    }

    public boolean readIsComplete() {
        return readData.lastIndexOf(ENTER) != -1;
    }

    public boolean writeIsComplete() {
        return !writeBuf.hasRemaining();
    }

}
