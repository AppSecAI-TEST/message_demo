package person.terry.message.mina.demo;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by terry on 2017/3/26.
 */
public class MinaSocketClient {

    private static Logger logger = LoggerFactory.getLogger(MinaSocketClient.class);

    public static void main(String[] args) {
        NioSocketConnector connector = new NioSocketConnector();
        DefaultIoFilterChainBuilder chain = connector.getFilterChain();
        chain.addLast("CodeFilter", new ProtocolCodecFilter(new TextLineCodecFactory()));
        connector.setHandler(new MinaClientHandler());
        connector.setConnectTimeoutMillis(30 * 1000);
        ConnectFuture cf = connector.connect(new InetSocketAddress("localhost", 9988));
        cf.awaitUninterruptibly();
        cf.getSession().getCloseFuture().awaitUninterruptibly();
        connector.dispose();
    }


}
