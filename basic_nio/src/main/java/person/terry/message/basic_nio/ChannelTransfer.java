package person.terry.message.basic_nio;

import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by terry on 2017/8/9.
 * <p>
 * <p>
 * Test channel transfer. This is a very simplistic concatenation
 * program. It takes a list of file names as arguments, opens each
 * in turn and transfers (copies) their content to the given
 * WritableByteChannel (in this case, stdout).
 */
public class ChannelTransfer {

    public static void main(String[] argv) throws Exception {
        if (argv.length == 0) {
            System.err.println("Usage: filename ...");
            return;
        }
        catFiles(Channels.newChannel(System.out), argv);
    }

    private static void catFiles(WritableByteChannel target, String[] files) throws Exception {
        for (int i = 0; i < files.length; i++) {
            FileInputStream fis = new FileInputStream(files[i]);
            FileChannel channel = fis.getChannel();
            channel.transferTo(0, channel.size(), target);
            channel.close();
            fis.close();
        }
    }

}
