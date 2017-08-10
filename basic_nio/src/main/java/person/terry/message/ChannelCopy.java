package person.terry.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by terry on 2017/8/7.
 * <p>
 * a class demonstrates how to transfer bytes with file channel
 */
public class ChannelCopy {

    public static void main(String[] args) throws IOException {
        ReadableByteChannel source = Channels.newChannel (System.in);
        WritableByteChannel dest = Channels.newChannel (System.out);
        channelCopy1 (source, dest);
        // alternatively, call channelCopy2 (source, dest);
        source.close( );
        dest.close( );
    }

    /**
     * Channel copy method 1. This method copies data from the src
     * channel and writes it to the dest channel until EOF on src.
     * This implementation makes use of compact( ) on the temp buffer
     * to pack down the data if the buffer wasn't fully drained. This
     * may result in data copying, but minimizes system calls. It also
     * requires a cleanup loop to make sure all the data gets sent.
     */
    private static void channelCopy1(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        while (src.read(buffer) != -1) {  // -1 = read at EOF
            buffer.flip();
            dest.write(buffer);  // 这里可能进行了部分写入 =。=
            buffer.compact(); // If partial transfer, shift remainder down . If buffer is empty, same as doing clear( )
        }
        buffer.flip(); // EOF will leave buffer in fill state
        while (buffer.hasRemaining()) { // Make sure that the buffer is fully drained
            dest.write(buffer);
        }
    }

    /**
     * Channel copy method 2. This method performs the same copy, but
     * assures the temp buffer is empty before reading more data. This
     * never requires data copying but may result in more systems calls.
     * No post-loop cleanup is needed because the buffer will be empty
     * when the loop is exited.
     */
    private static void channelCopy2(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {  // 这个循环进行了多次system call
                dest.write(buffer);
            }
            buffer.clear();
        }
    }


}
