package easyJava.job;

import com.alibaba.fastjson.JSON;
import easyJava.dao.master.BaseDao;
import easyJava.entity.HuobiKlineEntity;
import easyJava.utils.HttpsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://huobiapi.github.io/docs/spot/v1/cn/#c1ae0a8486
 * Connect with the currency price query interface, change the chr price according to the klay price in real time, and fix the chr at 0.1usdt
 * @author lxr
 */
@Component
@EnableScheduling
public class HuobiDataSync {

    ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private static final Logger logger = LogManager.getLogger(HuobiDataSync.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    BaseDao baseDao;

    public static final String HUOBI_API_URL_PRE = "https://api-aws.huobi.pro";
    public static final String HUOBI_API_URL_PRE_BACKUP = "https://api.huobi.pro";
    public static final String MARKET_KLINE = "/market/history/kline";
    public static final String HUOBI_KLINE = "huobi_kline";
    public static final String KLAY_HUOBI_USDT_PRICE = "klayusdt";
    public static final String KLAY_HUOBI_CHR_PRICE = "klaychr";
    public static final String KLAY_HUOBI_CHR_BACK_PRICE = "chrklay";
    public static final String MARKET_SYMBOLS = "/v2/settings/common/symbols";

    //@Scheduled(cron = "0 0/5 * * * ?")
//    //@Scheduled(cron = "0 * * * * ?")
    public void priceToChrContractSync() {
        BigInteger price = new BigInteger("3000");
        var klayToChr = redisTemplate.opsForHash().get(HUOBI_KLINE, KLAY_HUOBI_CHR_PRICE);
        if (klayToChr == null) {
            logger.error(" priceToChrContractSync price:null");
            return;
        }
        price = new BigDecimal(Double.parseDouble(klayToChr.toString())).setScale(3, RoundingMode.DOWN).multiply(new BigDecimal(1000)).toBigInteger();
        logger.info(" priceToChrContractSync price:" + price);
    }

    //@Scheduled(cron = "0 * * * * ?")
    public void priceSync() {
        Set<String> symbolSet = new HashSet<String>();
        symbolSet.add("klayusdt");
//        symbolSet.add("btcusdt");
//		symbolSet.add("ethusdt");
//		symbolSet.add("dashusdt");
//		symbolSet.add("ltcusdt");
//1min, 5min, 15min, 30min, 60min, 4hour, 1day, 1mon, 1week, 1year
        Set<String> periodSet = new HashSet<String>();
//		periodSet.add("1min");
//		periodSet.add("5min");
//		periodSet.add("15min");
//		periodSet.add("30min");
//		periodSet.add("60min");
//		periodSet.add("4hour");
        periodSet.add("1day");
//		periodSet.add("1mon");
//		periodSet.add("1week");
//		periodSet.add("1year");
        for (String symbol : symbolSet) {
            for (String period : periodSet) {
                Map<String, String> headers = new HashMap<String, String>();
                Map<String, String> params = new HashMap<String, String>();
                params.put("symbol", symbol);
                params.put("period", period);
                params.put("size", "1");
                String result = HttpsUtils.Get(HUOBI_API_URL_PRE + MARKET_KLINE, headers, params);
                if (result != null) {
                    HuobiKlineEntity entity = JSON.parseObject(result, HuobiKlineEntity.class);
                    var price = entity.getData().get(0).getClose();
                    logger.info("klay usdt price:" + price);
                    redisTemplate.opsForHash().put(HUOBI_KLINE, KLAY_HUOBI_USDT_PRICE, price);
                    Map map = new HashMap();
                    map.put("from", "klay");
                    map.put("to", "usdt");
                    map.put("rate", price);
                    baseDao.insertUpdateBase(map);

                    var klayToChr = new BigDecimal(Double.parseDouble(price))
                            .setScale(3, RoundingMode.DOWN);
                    map.clear();
                    map.put("from", "klay");
                    map.put("to", "chr");
                    map.put("rate", klayToChr.toPlainString());
                    baseDao.insertUpdateBase(map);
                    redisTemplate.opsForHash().put(HUOBI_KLINE, KLAY_HUOBI_CHR_PRICE, klayToChr.toPlainString());

                    logger.info(" klayToChr price:" + klayToChr.toPlainString());
                    var chrToKlay = new BigDecimal(1).setScale(3, RoundingMode.CEILING)
                            .divide(klayToChr, RoundingMode.CEILING).setScale(3, RoundingMode.CEILING);
                    map.clear();
                    map.put("from", "chr");
                    map.put("to", "klay");
                    map.put("rate", chrToKlay.toPlainString());
                    baseDao.insertUpdateBase(map);
                    redisTemplate.opsForHash().put(HUOBI_KLINE, KLAY_HUOBI_CHR_BACK_PRICE, chrToKlay.toPlainString());
                    logger.info(" chrToKlay price:" + chrToKlay.toPlainString());
                }
            }
        }
    }

    public static void main(String[] args) {

    }

}
