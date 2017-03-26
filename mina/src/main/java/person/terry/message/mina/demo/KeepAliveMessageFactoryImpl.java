package person.terry.message.mina.demo;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by terry on 2017/3/26.
 */
public class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveMessageFactoryImpl.class);

    private Object heartBeatRequestSent;
    private Object heartBeatRequestReceived;
    private Object heartBeatResponseReceived;
    private Object heartBeatResponseSent;

    public boolean isRequest(IoSession ioSession, Object message) {
        logger.info("接收心跳请求消息: " + message);
        if (message.equals(heartBeatRequestReceived))
            return true;
        return false;
    }

    /**
     * 判断收到的是否是心跳响应，否则会执行TimeoutHandler（
     * 除非超时异常handler是KeepAliveRequestTimeoutHandler.DEAF_SPEAKER）
     */
    public boolean isResponse(IoSession ioSession, Object message) {
        logger.info("接收心跳响应消息: " + message);
        if (message.equals(heartBeatResponseReceived))
            return true;
        return false;
    }

    /**
     * 需要获取发送的心跳包<br/>
     * null则表明不需要发送请求心跳<br/>
     * 否则发送，配合requestTimeoutHandler（决定是否需要心跳响应或请求超时处理）和isResponse()
     *
     * @param ioSession
     * @return
     */
    public Object getRequest(IoSession ioSession) {
        logger.info("发送心跳请求消息: " + heartBeatRequestSent);
        return heartBeatRequestSent;
    }

    /**
     * 获取需要响应的心跳包，null则表明不需要发送心跳响应
     *
     * @param ioSession
     * @param o
     * @return
     */
    public Object getResponse(IoSession ioSession, Object o) {
        logger.info("发送心跳响应消息: " + heartBeatResponseSent);
        return heartBeatResponseSent;
    }

    public Object getHeartBeatRequestSent() {
        return heartBeatRequestSent;
    }

    public void setHeartBeatRequestSent(Object heartBeatRequestSent) {
        this.heartBeatRequestSent = heartBeatRequestSent;
    }

    public Object getHeartBeatRequestReceived() {
        return heartBeatRequestReceived;
    }

    public void setHeartBeatRequestReceived(Object heartBeatRequestReceived) {
        this.heartBeatRequestReceived = heartBeatRequestReceived;
    }

    public Object getHeartBeatResponseReceived() {
        return heartBeatResponseReceived;
    }

    public void setHeartBeatResponseReceived(Object heartBeatResponseReceived) {
        this.heartBeatResponseReceived = heartBeatResponseReceived;
    }

    public Object getHeartBeatResponseSent() {
        return heartBeatResponseSent;
    }

    public void setHeartBeatResponseSent(Object heartBeatResponseSent) {
        this.heartBeatResponseSent = heartBeatResponseSent;
    }
}
