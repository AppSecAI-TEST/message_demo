package person.terry.message.basic_nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Random;

/**
 * Created by terry on 2017/8/9.
 *
 * pipe传递没有线程间传递数据效率高 但是编程模型简单
 *
 */
public class PipeTest {

    public static void main(String[] args) throws Exception {
        // Wrap a channel around stdout
        WritableByteChannel out = Channels.newChannel(System.out);
        // Start worker and get read end of channel
        ReadableByteChannel workerChannel = startWorker(10);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        while (workerChannel.read(buffer) >= 0) {
            buffer.flip();
            out.write(buffer);
            buffer.clear();
        }
    }

    private static ReadableByteChannel startWorker(int reps) throws Exception {
        Pipe pipe = Pipe.open();
        Worker worker = new Worker(pipe.sink(), reps);
        worker.start();
        return pipe.source();
    }

    /**
     * A worker thread object which writes data down a channel.
     * Note: this object knows nothing about Pipe, uses only a
     * generic WritableByteChannel.
     */
    private static final class Worker extends Thread {

        WritableByteChannel channel;
        private int reps;

        public Worker(WritableByteChannel channel, int reps) {
            this.channel = channel;
            this.reps = reps;
        }

        @Override
        public void run() {

            ByteBuffer buffer = ByteBuffer.allocate(100);
            for (int i = 0; i < this.reps; i++) {
                doSomeWork(buffer);
                // channel may not take it all at once
                try {
                    while (channel.write(buffer) > 0) {

                    }
                } catch (IOException e) {
                    e.printStackTrace(); // easy way out; this is demo code
                }
            }

        }

        private String[] products = {
                "No good deed goes unpunished",
                "To be, or what?",
                "No matter where you go, there you are",
                "Just say \"Yo\"",
                "My karma ran over my dogma"
        };

        private Random rand = new Random();

        private void doSomeWork(ByteBuffer buffer) {
            int product = rand.nextInt(products.length);
            buffer.clear();
            buffer.put(products[product].getBytes());
            buffer.put("\r\n".getBytes());
            buffer.flip();
        }


    }


}
