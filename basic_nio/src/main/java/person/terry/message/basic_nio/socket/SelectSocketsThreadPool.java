package person.terry.message.basic_nio.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Specialization of the SelectSockets class which uses a thread pool to service
 * channels. The thread pool is an ad-hoc implementation quicky lashed togther
 * in a few hours for demonstration purposes. It's definitely not production
 * quality.
 * <p>
 * Created by terry on 2017/8/10.
 */
public class SelectSocketsThreadPool extends SelectSockets {

    private static final int MAX_THREADS = 5;
    private ThreadPool pool = new ThreadPool(MAX_THREADS);

    public static void main(String[] args) throws Exception {
        new SelectSocketsThreadPool().go(args);
    }

    /**
     * Sample data handler method for a channel with data ready to read. This
     * method is invoked from the go( ) method in the parent class. This handler
     * delegates to a worker thread in a thread pool to service the channel,
     * then returns immediately.
     *
     * @param key A SelectionKey object representing a channel determined by the * selector to be ready for reading. If the channel returns an
     *            EOF condition, it is closed here, which automatically
     *            invalidates the associated key. The selector will then
     *            de-register the channel on the next select call.
     */
    @Override
    protected void readDataFromSocket(SelectionKey key) throws Exception {
        WorkerThread worker = pool.getWorker();
        if (worker == null) {
            // No threads available. Do nothing. The selection
            // loop will keep calling this method until a
            // thread becomes available. This design could
            // be improved.
            return;
        }
        worker.serviceChannel(key);
    }

    private class ThreadPool {

        List idle = new LinkedList();

        ThreadPool(int poolSize) {
            for (int i = 0; i < poolSize; i++) {
                WorkerThread thread = new WorkerThread(this);
                thread.setName("worker" + i);
                thread.start();
                idle.add(thread);
            }
        }

        /**
         * Find an idle worker thread, if any. Could return null.
         */
        WorkerThread getWorker() {
            WorkerThread worker = null;
            synchronized (idle) {
                if (idle.size() > 0) {
                    worker = (WorkerThread) idle.remove(0);
                }
            }
            return worker;
        }

        /**
         * Called by the worker thread to return itself to the idle pool.
         */
        void returnWorker(WorkerThread worker) {
            synchronized (idle) {
                idle.add(worker);
            }
        }

    }


    /**
     * A worker thread class which can drain channels and echo-back the input.
     * Each instance is constructed with a reference to the owning thread pool
     * object. When started, the thread loops forever waiting to be awakened to
     * service the channel associated with a SelectionKey object. The worker is
     * tasked by calling its serviceChannel( ) method with a SelectionKey
     * object. The serviceChannel( ) method stores the key reference in the
     * thread object then calls notify( ) to wake it up. When the channel has
     * been drained, the worker thread returns itself to its parent pool.
     */
    private class WorkerThread extends Thread {

        private ByteBuffer buffer = ByteBuffer.allocate(1024);
        private ThreadPool pool;
        private SelectionKey key;

        WorkerThread(ThreadPool pool) {
            this.pool = pool;
        }

        @Override
        public synchronized void run() {

            System.out.println(this.getName() + "is ready");
            while (true) {
                try {
                    this.wait();  // started and wait for 'work'
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.isInterrupted();  // clear interrupted status , may be a 'interrupt wakeup'
                }
                if (key == null) {  // if there is not a context or a binding
                    continue;
                }
                System.out.println(this.getName() + "has been awakened");  // interrupt
                try {
                    drainChannel(key); // read and write
                } catch (Exception e) {
                    System.out.println("Caught '" + e + "' closing channel");
                    try {
                        key.channel().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    key.selector().wakeup(); // wakeup selector
                }
                key = null;
                this.pool.returnWorker(this); // return worker
            }

        }

        /**
         *
         * service read event
         *
         * @param key
         */
        synchronized void serviceChannel(SelectionKey key) {
            this.key = key;
            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));  // OP_READ listen bit clearup
            this.notify();
        }


        /**
         * do drain
         *
         * @param key
         * @throws Exception
         */
        void drainChannel(SelectionKey key) throws Exception {
            SocketChannel channel = (SocketChannel) key.channel();
            int count;
            buffer.clear();
            while ((count = channel.read(buffer)) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) { // Send the data; may not go all at once
                    channel.write(buffer);
                }
                buffer.clear();
            }
            if (count < 0) {
                channel.close();
                return;
            }
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();
        }


    }


}
