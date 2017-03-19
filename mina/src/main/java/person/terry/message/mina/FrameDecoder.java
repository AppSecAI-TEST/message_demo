package person.terry.message.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;

/**
 * Created by terry on 2017/3/19.
 */
public class FrameDecoder extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        int totalLength = ioBuffer.getInt();
        int messageLength = totalLength - 4;
        if (ioBuffer.remaining() >= messageLength) {
            String message = ioBuffer.getString(messageLength, Charset.forName("UTF-8").newDecoder());
            protocolDecoderOutput.write(message);
            return true;
        }
        return false;
    }

}
