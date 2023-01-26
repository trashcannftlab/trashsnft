package easyJava.klay;

import com.alibaba.fastjson.JSON;
import easyJava.controller.websocket.TexasWS;
import easyJava.entity.BaseEntity;
import easyJava.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClearOrdersRedisThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClearOrdersRedisThread.class);

    public ClearOrdersRedisThread() {
    }

    /**
     * @param methodName
     * @param secondIntervalStr
     * @param pageSize
     * @param order
     */
    public void sendNotification(String methodName, String secondIntervalStr, int pageSize, int order) {
    }

    @Override
    public void run() {
        sendNotification("getHistoryOrders", "1", 15, 2);
        sendNotification("getKline", "21600", 100, 1);
        sendNotification("getBuyOrders", "1", 10, 1);
        sendNotification("getSaleOrders", "1", 10, 1);
    }


}
