package person.terry.message.mina.demo;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by terry on 2017/3/26.
 */
public class MinaSocketServer {

    private static Logger logger = LoggerFactory.getLogger(MinaServerHandler.class);

    private static final int REQUEST_INTERVAL = 15;

    private static final int REQUEST_TIMEOUT = 5;

    public static void main(String[] args) throws IOException {
        
        SocketAcceptor acceptor = new NioSocketAcceptor();
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("protocolCodeFilter", new ProtocolCodecFilter(new TextLineCodecFactory())); // 行读取\r\n

        KeepAliveMessageFactoryImpl serverKeepAliveMessageFactoryImpl = new KeepAliveMessageFactoryImpl();
        serverKeepAliveMessageFactoryImpl.setHeartBeatRequestSent("ping");
        serverKeepAliveMessageFactoryImpl.setHeartBeatRequestReceived("ping");
        serverKeepAliveMessageFactoryImpl.setHeartBeatResponseSent("pong");
        serverKeepAliveMessageFactoryImpl.setHeartBeatResponseReceived("pong");
        KeepAliveFilter keepAliveFilter = new KeepAliveFilter(serverKeepAliveMessageFactoryImpl, IdleStatus.BOTH_IDLE);

        keepAliveFilter.setRequestInterval(REQUEST_INTERVAL);
        keepAliveFilter.setRequestTimeout(REQUEST_TIMEOUT);
        keepAliveFilter.setForwardEvent(true);

        chain.addLast("heartBeat", keepAliveFilter);

        acceptor.setHandler(new MinaServerHandler());
        int bindPort = 9988;
        acceptor.bind(new InetSocketAddress(bindPort));
        logger.info("Mina server is Listing on:= " + bindPort);

    }


}
