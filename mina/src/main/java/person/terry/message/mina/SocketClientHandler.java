package person.terry.message.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by terry on 2017/3/19.
 */
public class SocketClientHandler extends IoHandlerAdapter {


    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        //TODO 处理业务，分发数据，可以使用广播等方式
    }

}
