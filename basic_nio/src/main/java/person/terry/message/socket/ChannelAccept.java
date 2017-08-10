package person.terry.message.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by terry on 2017/8/9.
 * <p>
 * accept demo
 */
public class ChannelAccept {

    public static final String GREETING = "Hello I must be going.\r\n";

    public static void main(String[] args) throws Exception {
        int port = 1234;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ByteBuffer buffer = ByteBuffer.wrap(GREETING.getBytes());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port));
        ssc.configureBlocking(false);
        while (true) {
            System.out.println("Waiting for connections");
            SocketChannel sc = ssc.accept();
            if (sc == null) {
                Thread.sleep(2000);
            } else {
                System.out.println ("Incoming connection from: " + sc.socket().getRemoteSocketAddress( ));
                buffer.rewind();   // 这里可以使用rewind代替flip 因为buffer固定长度就那么点。。
                sc.write(buffer);
                sc.close();
            }
        }
    }

}
