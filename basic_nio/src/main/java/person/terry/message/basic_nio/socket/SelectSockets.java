package person.terry.message.basic_nio.socket;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by terry on 2017/8/10.
 */
public class SelectSockets {

    public static int PORT_NUMBER = 1234;

    public static void main(String[] args) throws Exception {
        new SelectSockets().go(args);
    }

    public void go(String[] argv) throws Exception {
        int port = PORT_NUMBER;
        if (argv.length > 0) { // Override default listen port
            port = Integer.parseInt(argv[0]);
        }
        System.out.println("Listening on port " + port);
        ServerSocketChannel serverChannel = ServerSocketChannel.open();    // Allocate an unbound server socket channel
        ServerSocket serverSocket = serverChannel.socket(); // Get the associated ServerSocket to bind it with
        serverSocket.bind(new InetSocketAddress(port));  // Set the port the server channel will listen to
        Selector selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int n = selector.select();
            if (n == 0)
                continue;
            Iterator it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                if (key.isAcceptable()) { // Is a new connection coming in
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    registerChannel(selector, channel, SelectionKey.OP_READ);  // 注册读事件
                    sayHello(channel);  // 发送连接建立 反馈信息 比如"OK"
                }
                if (key.isReadable()) { // Is there data to read on this channel
                    readDataFromSocket(key);
                }
                it.remove();
            }
        }
    }


    /**
     * Register the given channel with the given selector for the given
     * operations of interest
     */
    protected void registerChannel(Selector selector, SelectableChannel channel, int ops) throws Exception {
        if (channel == null) {
            return; // could happen
        }
        // Set the new channel nonblocking
        channel.configureBlocking(false);
        // Register it with the selector
        channel.register(selector, ops);
    }

    // Use the same byte buffer for all channels. A single thread is servicing all the channels, so no danger of concurrent acccess.
    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);


    /**
     * Sample data handler method for a channel with data ready to read.
     *
     * @param key A SelectionKey object associated with a channel determined by * the selector to be ready for reading. If the channel returns
     *            an EOF condition, it is closed here, which automatically
     *            invalidates the associated key. The selector will then
     *            de-register the channel on the next select call.
     */
    protected void readDataFromSocket(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int count;
        buffer.clear(); // Empty buffer
        // loop while data is available ;channel is nonblouucking
        while ((count = socketChannel.read(buffer)) > 0) {
            buffer.flip(); //make buffer readable
            while (buffer.hasRemaining()) {
                // WARNING: the loop is evil. Because
                // it's writing back to the same nonblocking
                // channel it read the data from, this code can
                // potentially spin in a busy loop. In real life
                // you'd do something more useful than this.
                socketChannel.write(buffer);
            }
            buffer.clear();
        }
        if (count < 0) {
            socketChannel.close(); // Close channel on EOF, invalidates the key
        }
    }

    /**
     * Spew a greeting to the incoming client connection.
     *
     * @param channel The newly connected SocketChannel to say hello to.
     */
    private void sayHello(SocketChannel channel) throws Exception {
        buffer.clear();
        buffer.put("Hi there!\r\n".getBytes());
        buffer.flip();
        channel.write(buffer);
    }


}
