package person.terry.message.mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * Created by terry on 2017/3/19.
 */
public class Client {

    private static final String HOST_NAME = "127.0.0.1";
    private static final int PORT = 8043;

    public static void main(String[] args) {

        SocketConnector mSocketConnector = new NioSocketConnector();
        // 设置协议解析处理
        mSocketConnector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new FrameCodecFactory()));
        // 设置心跳包
        KeepAliveFilter heartFilter = new KeepAliveFilter(new HeartbeatMessageFactory());
        heartFilter.setRequestInterval(5 * 60);
        heartFilter.setRequestTimeout(10);
        mSocketConnector.getFilterChain().addLast("heartbeat", heartFilter);
        // 设置handler 业务处理
        mSocketConnector.setHandler(null);
        InetSocketAddress mSocketAddress = new InetSocketAddress(HOST_NAME, PORT);
        ConnectFuture mFuture = mSocketConnector.connect(mSocketAddress);
        mFuture.awaitUninterruptibly();
        IoSession mSession = mFuture.getSession();
        // TODO session write and session read

//        mFuture.cancel();
//        mSession.closeNow();
//        mSession.getCloseFuture().setClosed();
//        mSession.getCloseFuture().awaitUninterruptibly();
//        //如果完全不连接了
//        mSocketConnector.dispose();
        
    }

}
