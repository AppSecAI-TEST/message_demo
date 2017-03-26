package person.terry.message.mina.demo;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by terry on 2017/3/26.
 */
public class MinaClientHandler extends IoHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(MinaClientHandler.class);

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        
        logger.debug("connected:" + session.getRemoteAddress());

        // 连续发送两条消息，服务端一次收到
        WriteFuture wf1 = session.write("client says：我来啦1........");
        logger.debug("client says：我来啦1........");
        wf1.addListener(new IoFutureListener<IoFuture>() {
            public void operationComplete(IoFuture future) {
                logger.debug("future1 -- write completed!");
            }
        });

        Thread.sleep(10 * 1000);
        WriteFuture wf2 = session.write("client says：我来啦2........");
        logger.debug("client says：我来啦2........");
        wf2.addListener(new IoFutureListener<WriteFuture>() {
            public void operationComplete(WriteFuture future) {
                if (future.isWritten()) {
                    logger.info("Message send:");
                } else {
                    logger.error("发送失败：" + "，原因：" + future.getException());
                }
            }
        });
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.debug("disconnect:" + session.getRemoteAddress());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.debug("connect error:" + session.getRemoteAddress());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String s = (String) message;
        logger.debug(s);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        logger.info("messageSent");
    }
}
