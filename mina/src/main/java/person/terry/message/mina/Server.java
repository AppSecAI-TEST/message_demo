package person.terry.message.mina;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by terry on 2017/3/19.
 */
public class Server {

    private static final int PORT = 9123;
    /**
     * 30秒后超时
     */
    private static final int IDELTIMEOUT = 30;
    /**
     * 15秒发送一次心跳包
     */
    private static final int HEARTBEATRATE = 15;
    /**
     * 心跳包内容
     */
    private static final String HEARTBEATREQUEST = "0x11";
    private static final String HEARTBEATRESPONSE = "0x12";
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getSessionConfig().setReadBufferSize(1024);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
                IDELTIMEOUT);

        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast(
                "codec", new ProtocolCodecFilter(new TextLineCodecFactory()));

        KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
        //下面注释掉的是自定义Handler方式
//      KeepAliveRequestTimeoutHandler heartBeatHandler = new
//                              KeepAliveRequestTimeoutHandlerImpl();
//      KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory,
//              IdleStatus.BOTH_IDLE, heartBeatHandler);

        KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory,
                IdleStatus.BOTH_IDLE);

        //设置是否forward到下一个filter
        heartBeat.setForwardEvent(true);
        //设置心跳频率
        heartBeat.setRequestInterval(HEARTBEATRATE);

        acceptor.getFilterChain().addLast("heartbeat", heartBeat);

        acceptor.setHandler(new MyIoHandler());
        acceptor.bind(new InetSocketAddress(PORT));
        System.out.println("Server started on port： " + PORT);
    }


    /**
     * @author cruise
     * @ClassName KeepAliveMessageFactoryImpl
     * @Description 内部类，实现KeepAliveMessageFactory（心跳工厂）
     */
    private static class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {

        public boolean isRequest(IoSession session, Object message) {
            LOG.info("请求心跳包信息: " + message);
            if (message.equals(HEARTBEATREQUEST))
                return true;
            return false;
        }

        public boolean isResponse(IoSession session, Object message) {
//          LOG.info("响应心跳包信息: " + message);
//          if(message.equals(HEARTBEATRESPONSE))
//              return true;
            return false;
        }

        public Object getRequest(IoSession session) {
            LOG.info("请求预设信息: " + HEARTBEATREQUEST);
            /** 返回预设语句 */
            return HEARTBEATREQUEST;
        }

        public Object getResponse(IoSession session, Object request) {
            LOG.info("响应预设信息: " + HEARTBEATRESPONSE);
            /** 返回预设语句 */
            return HEARTBEATRESPONSE;
//          return null;
        }

    }


    public static class MyIoHandler extends IoHandlerAdapter {
        private final static Logger log = LoggerFactory
                .getLogger(MyIoHandler.class);

        @Override
        public void sessionOpened(IoSession session) throws Exception {

        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {

        }

        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {
            String ip = session.getRemoteAddress().toString();
            log.info("===> Message From " + ip + " : " + message);
        }
    }

}