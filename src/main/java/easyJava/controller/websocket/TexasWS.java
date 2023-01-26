package easyJava.controller.websocket;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author lxr
 */
@ServerEndpoint("/ws/texas")
@Component
public class TexasWS {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TexasWS.class);
    static final int maxSize = 256;// 1 * 1024;// 1K

    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static CopyOnWriteArrayList<Session> sessionList = new CopyOnWriteArrayList<>();

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("onMessage:" + message);
        onMessageDo(message, session);
    }

    public void onMessageDo(String message, Session session) {
    }


    @OnOpen
    public void onOpen(Session session) {
        logger.info("TexasWS onOpen");
        session.setMaxBinaryMessageBufferSize(maxSize);
        session.setMaxTextMessageBufferSize(maxSize);
        sessionList.add(session);

    }

    @OnClose
    public void onClose(Session session) {
        onConnectLost(session);
        logger.info("TexasWS connection closed ");
    }

    @OnError
    public void onError(Session session, Throwable e) {
        onConnectLost(session);
        logger.error("TexasWS connection error: ", e);
    }

    public void onConnectLost(Session session) {
        sessionList.remove(session);
    }

    public static void sendToAllText(String text) {
//        logger.info("sendToAllText size:" + sessionList.size() + ",txt:" + text);
        sessionList.forEach(session -> {
            if (session == null) {
                return;
            }
            synchronized (session) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(text);
                    } catch (IOException e) {
                        logger.error("sendToAllText error:", e);
                    }
                }
            }
        });
        for (int i = 0; i < sessionList.size(); i++) {
            if (sessionList.get(i) == null || !sessionList.get(i).isOpen()) {
                logger.error("session remove :" + i);
                sessionList.remove(i);
                break;
            }
        }
    }

    /**
     *
     * @param session
     * @param text
     */
    public static void sendText(Session session, String text) {
        if (session == null) {
            return;
        }
        synchronized (session) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(text);
                    // logger.info(text);
                } catch (IOException e) {
                    logger.error("sendText error:", e);
                }
            }
        }
    }

}
