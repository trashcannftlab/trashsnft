package easyJava.etherScan;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ScanServiceImpl implements ScanService {
    private static final Logger logger = LoggerFactory.getLogger(ScanServiceImpl.class);

    private static String usdtContractAddress = "usdtContractAddress";
    private static String myAddress;
    private static String apiKey = "apikay";
    /**
     */
    private static String url = "https://api.etherscan.io/api";
    public static String url_ropsten = "https://api-ropsten.etherscan.io/api";
    public static String url_rinkeby = "https://api-rinkeby.etherscan.io/api";
    public static String usdt_erc20_address;
    public static String usdt_erc20_ropsten = "usdt_erc20_ropsten";
    private static String chain = "ropsten";

    private static volatile Long currentBlock = 0L;

    static {
        if (chain.equals("ropsten")) {
            url = url_ropsten;
            usdtContractAddress = usdt_erc20_ropsten;
        }
    }


    private EventList scanEventOnePage(Long startBlock, Long endBlock) {

        Map<String, String> map = new HashMap<String, String>();

        map.put("module", "logs");
        map.put("action", "getLogs");
        map.put("address", usdtContractAddress);
        map.put("fromBlock", String.valueOf(startBlock));
        map.put("toBlock", String.valueOf(endBlock));
        map.put("apikey", apiKey);
        map.put("topic0", "topic0_address");
        map.put("topic1", myAddress);

        String responseStr = HttpClientUtil.httpGet(url, map, null);
        EventList response = JSON.parseObject(responseStr, EventList.class);

        return response;
    }

    private EventList scanAddressContractTransactions(String sysAddress, String action, String contractaddress, Long startblock) {

        Map<String, String> map = new HashMap<String, String>();

        map.put("module", "account");
        map.put("action", action);
        if (sysAddress == null || sysAddress.length() == 0) {
            sysAddress = myAddress;
        }
        map.put("address", sysAddress);
        map.put("contractaddress", contractaddress);
        map.put("startblock", startblock + "");
        map.put("page", "1");
        map.put("sort", "desc");
        map.put("toBlock", "latest");
        map.put("apikey", apiKey);

        return null;

    }


    private Map updateInfo(Map<String, Object> item) {

        Long blockNum = EthUtil.hexToBigInt(item.get("blockNumber").toString()).longValue();
        long time = EthUtil.hexToBigInt(item.get("timeStamp").toString()).longValue() * 1000;

        Map map = new HashMap();
        map.put("block_num", blockNum);
        map.put("timestamp", time);
        map.put("value", item.get("value"));
        map.put("from", item.get("from"));
        map.put("to", item.get("to"));
        if (item.get("input") != null) {
            String input = item.get("input").toString();
            if (input.length() > 100) {
                input = input.substring(0, 100);
                map.put("input", input);
            } else {
                map.put("input", item.get("input"));
            }
        }
        if (item.get("tokenSymbol") != null) {
            map.put("token", item.get("tokenSymbol"));
        }
        if (item.get("contractAddress") != null) {
            map.put("contract", item.get("contractAddress"));
        }

        map.put("hash", item.get("hash"));
        return map;
    }

    private Map updateEventInfo(Map<String, Object> item) {

        List<String> topics = (List<String>) item.get("topics");
        String fromAddress = topics.get(1).replace("0x000000000000000000000000", "0x");
        String toAddress = topics.get(2).replace("0x000000000000000000000000", "0x");

        Long blockNum = EthUtil.hexToBigInt(item.get("blockNumber").toString()).longValue();
        String transactionHash = item.get("transactionHash").toString().toLowerCase();
        long time = EthUtil.hexToBigInt(item.get("timeStamp").toString()).longValue() * 1000;

        Map map = new HashMap();
        map.put("block_num", blockNum);
        map.put("from", fromAddress);
        map.put("to", toAddress);
        map.put("timestamp", time);
        map.put("hash", transactionHash);
        return map;
    }

    public List<Map> doScanEvent() {
        List<Map> ret = new ArrayList<>();
        EventList eventList = null;

        System.out.println(String.format("doScan startBlock:%d", currentBlock));

        eventList = scanEventOnePage(currentBlock, 99999999L);

        if (eventList != null && eventList.getResult() != null && eventList.getResult().size() > 0) {

            for (Map<String, Object> item : eventList.getResult()) {

                logger.debug(JSON.toJSONString(item));
                ret.add(updateEventInfo(item));

            }

            Map<String, Object> map = eventList.getResult().get(eventList.getResult().size() - 1);

            BigInteger blockNum = new BigInteger(map.get("blockNumber").toString().substring(2), 16);

            if (blockNum.longValue() > currentBlock) {
                currentBlock = blockNum.longValue();
            }

        }
        return ret;
    }

    public List<Map> doScanETH() {
        return doScan("", "txlist", "");
    }

    public List<Map> doScanEthToken(String sysAddress) {
        return doScan(sysAddress, "tokentx", usdtContractAddress);
    }

    private List<Map> doScan(String sysAddress, String action, String contractaddress) {
        List<Map> ret = new ArrayList<>();
        EventList eventList = null;

        eventList = scanAddressContractTransactions(sysAddress, action, contractaddress, currentBlock);

        if (eventList != null && eventList.getResult() != null && eventList.getResult().size() > 0) {

            for (Map<String, Object> item : eventList.getResult()) {

                logger.debug(JSON.toJSONString(item));
                ret.add(updateInfo(item));

            }

            Map<String, Object> map = eventList.getResult().get(eventList.getResult().size() - 1);

            BigInteger blockNum = new BigInteger(map.get("blockNumber").toString().substring(2), 16);

            if (blockNum.longValue() > currentBlock) {
                currentBlock = blockNum.longValue();
            }

        }
        return ret;
    }
}
