package person.terry.message.basic_nio.reactor.finish;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by terry on 2017/8/10.
 */
public class ServerContext {

    private static final int subReactorSize = 3;
    public static final long selectTimeOut = TimeUnit.MILLISECONDS.toMillis(10);
    private static final AtomicLong nextIndex = new AtomicLong();

    private static ServerSocketChannel serverSocketChannel;
    private static Reactor mainReactor;
    private static Reactor[] subReactors;

    public static final boolean userThreadPool = true;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static <T extends Reactor> void startSingleReactor(int port, Class<T> clazz) {
        start(port, clazz, false, subReactorSize);
    }

    public static <T extends Reactor> void startMultipleReactor(int port, Class<T> clazz) {
        start(port, clazz, true, subReactorSize);
    }

    public static <T extends Reactor> void startMultipleReactor(int port, Class<T> clazz, int subReactorSize) {
        start(port, clazz, true, subReactorSize);
    }

    private static <T extends Reactor> void start(int port, Class<T> clazz, boolean useMultipleReactors, int subReactorSize) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Constructor<T> constructor = clazz.getConstructor(int.class, ServerSocketChannel.class, boolean.class, boolean.class, long.class);
            mainReactor = constructor.newInstance(port, serverSocketChannel, true, useMultipleReactors, selectTimeOut);
            if (useMultipleReactors) {
                subReactors = new Reactor[subReactorSize];
                for (int i = 0; i < subReactors.length; i++) {
                    subReactors[i] = constructor.newInstance(port, serverSocketChannel, false, useMultipleReactors, selectTimeOut);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainReactor.start();
        if (useMultipleReactors) {
            for (Reactor subReactor : subReactors) {
                subReactor.start();
            }
        }
    }

    public static Reactor nextSubReactor() {
        long nextIndexValue = nextIndex.getAndDecrement();
        if (nextIndexValue < 0) {
            nextIndex.set(0);
            nextIndexValue = 0;
        }
        return subReactors[(int) nextIndexValue % subReactors.length];
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public static <T> void execute(Runnable runnable) {
        executor.execute(runnable);
    }


}
