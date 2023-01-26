package easyJava.job;

import easyJava.klay.ClearOrdersRedisThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Service
public class GameCoinAndItemJob {
    private static final Logger logger = LoggerFactory.getLogger(GameCoinAndItemJob.class);
    int corePoolSize = 20;
    int maximumPoolSize = 40;
    long keepAliveTime = 20;
    TimeUnit unit = TimeUnit.SECONDS;
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(5000);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
            workQueue);
    @Autowired
    ClearOrdersRedisThread cache;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void matchOrders() {
        executor.execute(new MatchOrders());
    }

    public void QueryItems() {
        executor.execute(new QueryItems());
    }

    class MatchOrders extends Thread {
        @Override
        public void run() {
            cache.run();

        }
    }

    class QueryItems extends Thread {
        @Override
        public void run() {
            long begin = new Date().getTime();

        }
    }
}
