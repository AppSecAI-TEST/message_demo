package person.terry.message.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.charset.Charset;

/**
 * Created by terry on 2017/3/19.
 */
public class FrameEncoder implements ProtocolEncoder {

    public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
        if (o instanceof String) {
            String message = (String) o;
            byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));
            int totalSize = messageBytes.length + 4;
            IoBuffer buffer = IoBuffer.allocate(totalSize);
            buffer.putInt(totalSize);
            buffer.put(messageBytes);
            buffer.flip();
            protocolEncoderOutput.write(buffer);
        }
    }

    public void dispose(IoSession ioSession) throws Exception {

    }

}
