package person.terry.message.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Created by terry on 2017/3/19.
 */
public class FrameCodecFactory implements ProtocolCodecFactory {

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return new FrameEncoder();
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new FrameDecoder();
    }

}
