package easyJava.job;

import com.alibaba.fastjson.JSON;
import easyJava.Configs;
import easyJava.controller.NFTApiController;
import easyJava.controller.TrashCoinController;
import easyJava.controller.Web3jController;
import easyJava.dao.master.BaseDao;
import easyJava.dao.master.EthScanDao;
import easyJava.dao.master.NftEventDao;
import easyJava.dao.master.OrderScanDao;
import easyJava.entity.BaseModel;
import easyJava.etherScan.ScanService;
import easyJava.utils.DateUtils;
import easyJava.utils.HttpsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.websocket.WebSocketService;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Service
public class NFTEventsScanJob {
    public static final Logger logger = LoggerFactory.getLogger(NFTEventsScanJob.class);

    @Autowired
    BaseDao baseDao;
    @Autowired
    NftEventDao nftEventDao;
    @Autowired
    EthScanDao ethScanDao;
    @Autowired
    OrderScanDao orderScanDao;
    @Autowired
    ScanService scanService;
    @Autowired
    USDTReceiveWalletJob uSDTReceiveWallet;
    @Autowired
    NFTApiController nFTApiController;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private Web3jController Web3jController;

    public static final String NFT_EVENTS_TABLE = "system_nft_events";
    public static final String TRASH_ORDER_TABLE = "system_trash_order";
    public static volatile long canClaimTime = (new Date().getTime() / 6 / 3600 / 1000 + 1) * 6 * 3600 * 1000;
    public static final String ZERO_ADDR = "0x000000000000000000000000000000000000dEaD";
    WebSocketService ws = new WebSocketService(Configs.getEthHost(), false);
    int corePoolSize = 4;
    int maximumPoolSize = 8;
    long keepAliveTime = 20;
    TimeUnit unit = TimeUnit.SECONDS;
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(5000);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
            workQueue);

    public NFTEventsScanJob() {
        try {
            ws.connect();
        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "*/20 * * * * ?")
    public void scanETHLogJob() {
        scanNFTEvents();
    }

    public void scanNFTEvents() {
        logger.info("------scanNFTEvents start------");
        if (TrashCoinController.getAMP(new Date().getTime()) == 0) {
            logger.error("--------------scanNFTEvents end amp==0 time reach  --------------");
            return;
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", NFTApiController.API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        long timeNowSecond = (new Date()).getTime();
        params.put("limit", "100");
        String retStr = HttpsUtils.Get(NFTApiController.NFT_API_URL_BASE
                + ZERO_ADDR
                + NFTApiController.NFT_TRANSFER_API_TAIL, headers, params);
        easyJava.entity.moralis.NftEventsRet nftEventsRet = JSON.parseObject(retStr, easyJava.entity.moralis.NftEventsRet.class);
        if (nftEventsRet != null && nftEventsRet.getResult() != null && nftEventsRet.getResult().size() != 0) {
            logger.info("------scanNFTEvents nftEventsRet.getData() ------size:" + nftEventsRet.getResult().size());
        } else {
            logger.warn("------scanNFTEvents nftEventsRet.getData() size:0------" + retStr);
            return;
        }
        nftEventsRet.getResult().forEach(map -> {
            if (map.get("from_address").toString().toLowerCase().startsWith("0x000000000")) {
                return;
            }
            if (!nftBeforeActivity(map.get("token_address").toString(), map.get("token_id").toString())) {
                return;
            }
            map.put("tableName", NFT_EVENTS_TABLE);
            map.put("chain_type", Configs.getEthNetwork());
            baseDao.insertIgnoreBase(map);
            Map trashOrder = new HashMap<>();
            trashOrder.put("tableName", TRASH_ORDER_TABLE);
            trashOrder.put("to_account", map.get("from_address").toString().toLowerCase());
            int hasAddr = baseDao.selectBaseCount(trashOrder);
            if (hasAddr == 0) {
                trashOrder.put("status", 1);
                trashOrder.put("chain_type", Configs.getEthNetwork());
                trashOrder.put("timestamp", (new Date()).getTime());
                trashOrder.put("trash_coin_claim", 0);
                trashOrder.put("trash_nft_claim", 0);
                trashOrder.put("merkle_root_nft", 0);
                trashOrder.put("merkle_root_coin", 0);
                trashOrder.put("amp", TrashCoinController.getAMP(new Date().getTime()));
                baseDao.insertIgnoreBase(trashOrder);
            }
        });
    }

    public void scanClaimCoinEvents() {
        logger.info("------scanClaimCoinEvents start------");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", NFTApiController.API_KEY);
        String url = NFTApiController.NFT_API_URL_BASE + Configs.getTrashContractAddr()
                + NFTApiController.LOGS_API_TAIL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        params.put("limit", "100");
        params.put("topic0", "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");
        params.put("topic1", "0x0000000000000000000000000000000000000000000000000000000000000000");
        String retStr = HttpsUtils.Get(url, headers, params);
        easyJava.entity.moralis.NftEventsRet logsRet = JSON.parseObject(retStr, easyJava.entity.moralis.NftEventsRet.class);
        if (logsRet != null && logsRet.getResult() != null && logsRet.getResult().size() != 0) {
            logger.info("------scanClaimCoinEvents logsRet.getData() ------size:" + logsRet.getResult().size());
        } else {
            logger.warn("------scanClaimCoinEvents logsRet.getData() size:0------" + retStr);
            return;
        }
        logsRet.getResult().forEach(map -> {
            String claimAddress = "0x" + map.get("topic2").toString().substring(26);
            long time = 0;
            try {
                time = DateUtils.parseTZDate(map.get("block_timestamp").toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> query = new HashMap<String, Object>();
            query.put("tableName", TRASH_ORDER_TABLE);
            query.put("status", "2");
            query.put("to_account", claimAddress.toLowerCase());
            BaseModel baseModel = new BaseModel();
            baseModel.setPageSize(10000);
            baseModel.setPageNo(1);
            baseModel.setOrderAsc("desc");
            baseModel.setOrderColumn("id");
            var list = nftEventDao.selectTrashOrder(query, baseModel);
            if (list != null && list.size() != 0) {
                long finalTime = time;
                list.forEach(updateTrashOrder -> {
                    updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
                    updateTrashOrder.put("trash_coin_claim", finalTime);
                    baseDao.updateBaseByPrimaryKey(updateTrashOrder);
                    logger.info("claim trashCoin success address:" + claimAddress);
                });
            }
        });
    }

    public void scanClaimNFTEvents() {
        logger.info("------scanClaimNFTEvents start------");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", NFTApiController.API_KEY);
        String url = NFTApiController.NFT_API_URL_BASE + Configs.getTrashNftContractAddr()
                + NFTApiController.LOGS_API_TAIL;
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        params.put("limit", "100");
        params.put("topic0", "topic0_address");
        params.put("topic1", "0x0000000000000000000000000000000000000000000000000000000000000000");
        String retStr = HttpsUtils.Get(url, headers, params);
        easyJava.entity.moralis.NftEventsRet logsRet = JSON.parseObject(retStr, easyJava.entity.moralis.NftEventsRet.class);
        if (logsRet != null && logsRet.getResult() != null && logsRet.getResult().size() != 0) {
            logger.info("------scanClaimNFTEvents logsRet.getData() ------size:" + logsRet.getResult().size());
        } else {
            logger.warn("------scanClaimNFTEvents logsRet.getData() size:0------" + retStr);
            return;
        }
        logsRet.getResult().forEach(map -> {
            String claimAddress = "0x" + map.get("topic2").toString().substring(26);
            long time = 0;
            try {
                time = DateUtils.parseTZDate(map.get("block_timestamp").toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> query = new HashMap<String, Object>();
            query.put("tableName", TRASH_ORDER_TABLE);
            query.put("status", "2");
            query.put("to_account", claimAddress.toLowerCase());
            BaseModel baseModel = new BaseModel();
            baseModel.setPageSize(10000);
            baseModel.setPageNo(1);
            baseModel.setOrderAsc("desc");
            baseModel.setOrderColumn("id");
            var list = nftEventDao.selectTrashOrder(query, baseModel);
            if (list != null && list.size() != 0) {
                long finalTime = time;
                list.forEach(updateTrashOrder -> {
                    updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
                    updateTrashOrder.put("trash_nft_claim", finalTime);
                    baseDao.updateBaseByPrimaryKey(updateTrashOrder);
                    logger.info(" claim nft success address:" + claimAddress);

                });
            }
        });
    }

    @Scheduled(cron = "0 * * * * ?")
    public void scanTrashOrderJob() {
        scanTrashOrder();
    }

    @Scheduled(cron = "0 * * * * ?")
    public void scanTrashOrderClaimJob() {
        try {
            scanClaimNFTEvents();
        } catch (Exception e) {
            logger.error("scanClaimNFTEvents", e);
        }
        try {
            scanClaimCoinEvents();
        } catch (Exception e) {
            logger.error("scanClaimCoinEvents", e);
        }

        updateOrderStatus();
    }

    public void updateOrderStatus() {
        logger.info("scanTrashOrderClaim start");
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("tableName", TRASH_ORDER_TABLE);
        query.put("status", "2");
        BaseModel baseModel = new BaseModel();
        baseModel.setPageSize(10000);
        baseModel.setPageNo(1);
        baseModel.setOrderAsc("desc");
        baseModel.setOrderColumn("id");
        var list = nftEventDao.selectTrashOrder(query, baseModel);
        for (int i = 0; i < list.size(); i++) {
            String trash_nft_claim = list.get(i).get("trash_nft_claim").toString();
            String trash_coin_claim = list.get(i).get("trash_coin_claim").toString();
            String address = list.get(i).get("to_account").toString();
            Map updateTrashOrder = new HashMap();
            if (!trash_nft_claim.equals("0") && !trash_coin_claim.equals("0")) {
                updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
                updateTrashOrder.put("id", list.get(i).get("id"));
                updateTrashOrder.put("status", 3);
                baseDao.updateBaseByPrimaryKey(updateTrashOrder);
                logger.info(" claim success address:" + address);
            }
        }
    }

    public void scanTrashOrder() {
        logger.info("scanTrashOrder start");
        if (TrashCoinController.getAMP(new Date().getTime()) == 0) {
            logger.error("--------------scanNFTEvents end amp==0 time reach  --------------");
            return;
        }
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("tableName", TRASH_ORDER_TABLE);
        query.put("status", "1");
        query.put("timestamp", (new Date()).getTime() - 5 * 60 * 1000);
        BaseModel baseModel = new BaseModel();
        baseModel.setPageSize(100000);
        baseModel.setPageNo(1);
        baseModel.setOrderAsc("desc");
        baseModel.setOrderColumn("id");
        var list = nftEventDao.selectTrashOrder(query, baseModel);
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tableName", NFT_EVENTS_TABLE);
            map.put("from_address", list.get(i).get("to_account"));
            baseModel.setPageSize(100000);
            baseModel.setPageNo(1);
            var retNftList = baseDao.selectBaseList(map, baseModel);
            int s = retNftList.size();
            if (s == 0) {
                logger.error("，addr:" + list.get(i).get("to_account"));
                continue;
            }
            int rank = Integer.parseInt(list.get(i).get("id").toString());
            int amp = Integer.parseInt(list.get(i).get("amp").toString());
            long coin = TrashCoinController.getTrashCoin(s, amp, rank);
            if (coin == 0) {
                logger.error("activity not start or end");
                continue;
            }
            Map updateTrashOrder = new HashMap();
            updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
            updateTrashOrder.put("id", list.get(i).get("id"));
            updateTrashOrder.put("nft_info", "nft_info");
            updateTrashOrder.put("nft_count", s);
            updateTrashOrder.put("trash_coin", coin);
            int nft = getNFTAward(list.get(i).get("id").toString());
            updateTrashOrder.put("reward_nft_count", nft);
            updateTrashOrder.put("trash_coin_send", 0);
            updateTrashOrder.put("trash_nft_send", 0);
            updateTrashOrder.put("contract_address", Configs.getTrashNftContractAddr());
            updateTrashOrder.put("chain_type", Configs.getEthNetwork());
            updateTrashOrder.put("status", 2);
            updateTrashOrder.put("can_claim_time", canClaimTime);
            if (nft != 0) {
                updateTrashOrder.put("can_claim_time_nft", canClaimTime);
            } else {
                updateTrashOrder.put("can_claim_time_nft", 0);
            }
            baseDao.updateBaseByPrimaryKey(updateTrashOrder);
        }
    }

    /***
     * 1。 Rules for obtaining nft

     *A. The first 999 of the sequence can be divided by 3 to obtain nft

     *B. Start with 1000, including 1000, and divide by 10 to get nft
     *
     * @param idStr
     * @return
     */
    public static int getNFTAward(String idStr) {
        int id = Integer.parseInt(idStr);
        if (id <= 999) {
            if (id % 3 == 0) {
                return 1;
            }
        } else if (id <= 96670) {
            if (id % 10 == 0) {
                return 1;
            }
        }
        return 0;
    }

    @Scheduled(cron = "0 0 */1 * * ?")
    public void sendTrashCoinMerkleRootJob() {
        sendTrashCoinMerkleRoot();
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendTrashNFTMerkleRoot();
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    public void saveReportJob() {
        saveReport();
    }

    public void saveReport() {
        var end = new Date().getTime();
        var begin = end - 6 * 3600 * 1000;
        Map param = new HashMap();
        param.put("start_time", begin);
        param.put("end_time", end);
        var ret = orderScanDao.getReport(param);
        var recycle_nft_sum = (Double) ret.get("recycle_nft_sum");
        ret.put("recycle_nft_sum", recycle_nft_sum.longValue());
        var trash_coin_sum = (Double) ret.get("trash_coin_sum");
        ret.put("trash_coin_sum", trash_coin_sum.longValue());
        ret.put("tableName", "order_report");
        ret.put("start_time", begin);
        ret.put("end_time", end);

        //
        param.put("trash_coin_claim", 1);
        param.put("trash_nft_claim", 1);
        var claimed = orderScanDao.getReport(param);
        var claimed_trash_coin = (Double) claimed.get("trash_coin_sum");
        long claimed_trash_coin_str = 0;
        if (claimed_trash_coin != null) {
            claimed_trash_coin_str = claimed_trash_coin.longValue();
        }
        ret.put("claimed_trash_coin", claimed_trash_coin_str);
        ret.put("claimed_trash_nft", claimed.get("trash_nft_sum"));
        baseDao.insertBase(ret);
    }

    public List<List> getOrdersCoinList(List<Map> list) {
        List<String> toAddressArr = new ArrayList<>();
        List<Double> trashCoinArr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).get("trash_coin_claim").toString().equals("0")) {
                logger.debug("trashCoin already claim：" + list.get(i).get("to_account").toString());
                continue;
            }
            String toAddress = list.get(i).get("to_account").toString();
            String trashCoin = list.get(i).get("trash_coin").toString();
            toAddressArr.add(toAddress);
            trashCoinArr.add(Double.parseDouble(trashCoin));
        }
        List<List> orders = new ArrayList<>();
        for (int i = 0; i < toAddressArr.size(); i++) {
            List<String> order = new ArrayList();
            BigDecimal balanceDecimal = new BigDecimal(trashCoinArr.get(i)).multiply(new BigDecimal(10).pow(18));
            order.add(toAddressArr.get(i));
            order.add(balanceDecimal.toBigInteger().toString());
            orders.add(order);
        }
        return orders;
    }

    public List<List> getOrdersNFTList(List<Map> list) {
        List<String> toAddressArr = new ArrayList<>();
        List<Double> trashCoinArr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).get("trash_nft_claim").toString().equals("0")) {
                logger.debug("trashNFT already claim：" + list.get(i).get("to_account").toString());
                continue;
            }
            if (list.get(i).get("reward_nft_count").toString().equals("0")) {
                continue;
            }
            String toAddress = list.get(i).get("to_account").toString();
            String trashCoin = list.get(i).get("trash_coin").toString();
            toAddressArr.add(toAddress);
            trashCoinArr.add(Double.parseDouble(trashCoin));
        }
        List<List> orders = new ArrayList<>();
        for (int i = 0; i < toAddressArr.size(); i++) {
            List<String> order = new ArrayList();
            BigDecimal balanceDecimal = new BigDecimal(1);
            order.add(toAddressArr.get(i));
            order.add(balanceDecimal.toBigInteger().toString());
            orders.add(order);
        }
        return orders;
    }

    public List<Map> getOrdersMap() {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("tableName", TRASH_ORDER_TABLE);
        query.put("status", "2");
        BaseModel baseModel = new BaseModel();
        baseModel.setPageSize(100000);
        baseModel.setPageNo(1);
        baseModel.setOrderAsc("desc");
        baseModel.setOrderColumn("id");
        var list = nftEventDao.selectTrashOrder(query, baseModel);
        return list;
    }

    public List<Map> sendTrashCoinMerkleRoot() {
        logger.info("sendTrashCoinMerkleRoot start");
        try {
            ws.connect();
        } catch (ConnectException e) {
            logger.error("sendTrashCoinMerkleRoot ws.connect ", e);
        }
        var list = getOrdersMap();
        Map transactionMap = Web3jController.setMerkleRoot(ws, "system_nft", getOrdersCoinList(list), Configs.getTrashContractAddr());
        if (transactionMap == null) {
            return list;
        }
        logger.info("sendTrashCoinMerkleRoot list:" + JSON.toJSONString(list) + ",transactionMap:" + JSON.toJSONString(transactionMap));
        for (int i = 0; i < list.size(); i++) {
            Map updateTrashOrder = new HashMap();
            updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
            updateTrashOrder.put("id", list.get(i).get("id"));
            //send trashCoin hash
            updateTrashOrder.put("trash_coin_send", transactionMap.get("transactionHash"));
            updateTrashOrder.put("merkle_root_coin", transactionMap.get("merkleRoot"));
            updateTrashOrder.put("can_claim_time", new Date().getTime());
            baseDao.updateBaseByPrimaryKey(updateTrashOrder);
        }
        return list;
    }

    public List<Map> sendTrashNFTMerkleRoot() {
        logger.info("sendTrashNFTMerkleRoot start");
        try {
            ws.connect();
        } catch (ConnectException e) {
            logger.error("sendTrashNFTMerkleRoot ws.connect ", e);
        }
        var list = getOrdersMap();
        Map transactionMap = Web3jController.setMerkleRoot(ws, "system_nft", getOrdersNFTList(list), Configs.getTrashNftContractAddr());
        if (transactionMap == null) {
            return list;
        }
        logger.info("sendTrashNFTMerkleRoot list:" + JSON.toJSONString(list) + ",transactionMap:" + JSON.toJSONString(transactionMap));
        for (int i = 0; i < list.size(); i++) {
            Map updateTrashOrder = new HashMap();
            updateTrashOrder.put("tableName", TRASH_ORDER_TABLE);
            updateTrashOrder.put("id", list.get(i).get("id"));
            //send trashNFT hash
            updateTrashOrder.put("trash_nft_send", transactionMap.get("transactionHash"));
            updateTrashOrder.put("merkle_root_nft", transactionMap.get("merkleRoot"));
            if (!list.get(i).get("reward_nft_count").equals("0")) {
                updateTrashOrder.put("can_claim_time_nft", new Date().getTime());
            }
            baseDao.updateBaseByPrimaryKey(updateTrashOrder);
        }
        return list;
    }

    public static void main(String[] args) {
//        scanClaimNFTEvents();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", NFTApiController.API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        long timeNowSecond = (new Date()).getTime();
//        long timestamp = timeNowSecond - 7 * 24 * 3600 * 1000;
//        String fromBlock = NFTApiController.getBlock(timestamp, params.get("chain")).toString();
        params.put("limit", "10");
        //https://deep-index.moralis.io/api/v2/nft/{address}/{token_id}/transfers
        String url = NFTApiController.NFT_API_URL_BASE
                + "nft/" + "0xaf3ef32af09c9ddf679ba5e37db0536ae2ae56f5"
                + "/100/transfers";
        String retStr = HttpsUtils.Get(url, headers, params);
        System.out.println("-----" + url);
        System.out.println("-----" + Configs.getEthNetwork());
        System.out.println("-----" + retStr);
    }

    /**
     *
     * @param contract
     * @param tokenId
     * @return
     */
    public static boolean nftBeforeActivity(String contract, String tokenId) {
        var list = getNftHistory(contract, tokenId);
        if (list != null && list.size() != 0) {
            Map earliestTransfer = list.get(list.size() - 1);
            var firstTime = earliestTransfer.get("block_timestamp").toString();
            try {
                var firstTimeLong = DateUtils.parseTZDate(firstTime);
                logger.info("nftBeforeActivity firstTimeLong:" + firstTimeLong + ",begin:" + Configs.getBeginTime());
                if (firstTimeLong < Long.parseLong(Configs.getBeginTime())) {
                    return true;
                } else {
                    logger.error("nftBeforeActivity false:" + JSON.toJSONString(list.get(0)));
                }
            } catch (ParseException e) {
                logger.error("nftBeforeActivity", e);
            }
        }
        return false;
    }

    public static List<Map> getNftHistory(String contract, String tokenId) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", NFTApiController.API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        params.put("limit", "50");
        //https://deep-index.moralis.io/api/v2/nft/{address}/{token_id}/transfers
        String url = NFTApiController.NFT_API_URL_BASE
                + "nft/" + contract
                + "/" + tokenId + "/transfers";
        String retStr = HttpsUtils.Get(url, headers, params);
        easyJava.entity.moralis.NftEventsRet nftEventsRet = JSON.parseObject(retStr, easyJava.entity.moralis.NftEventsRet.class);
        if (nftEventsRet != null && nftEventsRet.getResult() != null && nftEventsRet.getResult().size() != 0) {
            logger.info("------scanNFTEvents nftEventsRet.getData() ------size:" + nftEventsRet.getResult().size());
            return nftEventsRet.getResult();
        } else {
            logger.warn("------scanNFTEvents nftEventsRet.getData() size:0------" + retStr);
            return null;
        }
    }
}
