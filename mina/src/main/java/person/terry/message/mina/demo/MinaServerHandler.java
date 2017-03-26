package person.terry.message.mina.demo;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by terry on 2017/3/26.
 */
public class MinaServerHandler extends IoHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MinaServerHandler.class);
    private int count = 0;

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.debug("connected:" + session.getRemoteAddress());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.debug("disconnected:" + session.getRemoteAddress());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        logger.debug("*********************" + session.isConnected());
        logger.debug("*********************" + session.isClosing());
        logger.debug("*********************" + session.isBothIdle());
        logger.debug("*********************" + session.isWriterIdle());
        logger.debug("*********************" + session.isReaderIdle());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.debug("connection error:" + session.getRemoteAddress());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String s = (String) message;
        logger.debug(s);
        if ("a".equals(s))
            Thread.sleep(60 * 1000);
        session.write(s + count);
        count++;
    }
}
