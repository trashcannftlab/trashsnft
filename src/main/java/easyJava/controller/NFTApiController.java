package easyJava.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import easyJava.Configs;
import easyJava.dao.master.BaseDao;
import easyJava.entity.BaseModel;
import easyJava.entity.ResponseEntity;
import easyJava.entity.moralis.NftEventsRet;
import easyJava.job.NFTEventsScanJob;
import easyJava.utils.HttpsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class NFTApiController {
    private static final Logger logger = LogManager.getLogger(NFTApiController.class);
    public static String NFT_API_URL_BASE = "https://deep-index.moralis.io/api/v2/";
    public static String ASSETS_API_TAIL = "/nft";
    public static String ERC20_TAIL = "/erc20";
    public static String NFT_TRANSFER_API_TAIL = "/nft/transfers";
    public static String EVENTS_API_TAIL = "/events";
    public static String LOGS_API_TAIL = "/logs";
    public static String BLOCK_API_TAIL = "dateToBlock";
    public static String API_KEY = "xxxx";
    @Autowired
    BaseDao baseDao;

    @RequestMapping("/nft/myRecycleNFT")
    public ResponseEntity myRecycleNFT(@RequestParam Map<String, Object> map) {
        if (map.get("from_address") == null || map.get("from_address").toString().length() == 0) {
            return new ResponseEntity(400, "from_address not null！");
        }
        if (map.get("pageSize") == null || map.get("pageSize").toString().length() == 0) {
            return new ResponseEntity(400, "pageSize not null！");
        }
        if (map.get("pageNo") == null || map.get("pageNo").toString().length() == 0) {
            return new ResponseEntity(400, "pageNo not null！");
        }
        map.put("tableName", NFTEventsScanJob.NFT_EVENTS_TABLE);
        map.put("from_address", map.get("from_address").toString().toLowerCase());
        map.put("to_address", Configs.getSystemAddress().toLowerCase());
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(map.get("pageNo").toString());
        baseModel.setPageSize(map.get("pageSize").toString());
        var ret = baseDao.selectBaseList(map, baseModel);
        return new ResponseEntity(ret);
    }

    @RequestMapping("/nft/network")
    public String network() {
        return Configs.getEthNetwork();
    }

    @RequestMapping("/erc20/balanceOf")
    public Object balanceOf(@RequestParam Map<String, Object> map) {
        return getTrashCoinBalanceOf(map.get("address").toString());
    }

    @RequestMapping("/nft/balanceOf")
    public Object balanceOfNFT(@RequestParam Map<String, Object> map) {
        return getTrashNFTBalanceOf(map.get("address").toString());
    }

    public JSONObject getTrashNFTBalanceOf(String address) {
        if (address.startsWith("0x00000000000")) {
            return new JSONObject();
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        params.put("token_addresses", Configs.getTrashNftContractAddr());
        String ret = HttpsUtils.Get(NFT_API_URL_BASE + address + ASSETS_API_TAIL, headers, params);
        var retJson = JSON.parseObject(ret);
        return retJson;
    }

    public JSONArray getTrashCoinBalanceOf(String address) {
        if (address.startsWith("0x00000000000")) {
            return new JSONArray();
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", Configs.getEthNetwork());
        params.put("token_addresses", Configs.getTrashContractAddr());
        String ret = HttpsUtils.Get(NFT_API_URL_BASE + address + ERC20_TAIL, headers, params);
        if (ret.startsWith("{")) {
            logger.error("getTrashCoinBalanceOf ret:" + ret + ",address:" + address);
            return new JSONArray();
        }
        var retJson = JSON.parseArray(ret);
        return retJson;
    }

    @RequestMapping("/nft/assets")
    public JSONObject getNFTAssets(@RequestParam Map<String, Object> map) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        if (map.get("chain") == null) {
            params.put("chain", Configs.getEthNetwork());
        } else {
            params.put("chain", map.get("chain").toString());
        }
        String ret = HttpsUtils.Get(NFT_API_URL_BASE + map.get("wallet_address").toString() + ASSETS_API_TAIL, headers, params);
        var retJson = JSON.parseObject(ret);
        return retJson;
    }

    @RequestMapping("/nft/events")
    public JSONObject getNFTEvents(@RequestParam Map<String, Object> map) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        if (map.get("chain") == null) {
            params.put("chain", Configs.getEthNetwork());
        } else {
            params.put("chain", map.get("chain").toString());
        }
        String ret = HttpsUtils.Get(NFT_API_URL_BASE + map.get("wallet_address").toString() + EVENTS_API_TAIL, headers, params);
        var retJson = JSON.parseObject(ret);
        return retJson;
    }

    /**
     *
     * @param map
     * @return
     */
    @RequestMapping("/nft/address/valid")
    public ResponseEntity addressValid(@RequestParam Map<String, Object> map) {
        var walletAddress = map.get("wallet_address").toString();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        if (map.get("chain") == null) {
            params.put("chain", Configs.getEthNetwork());
        } else {
            params.put("chain", map.get("chain").toString());
        }
        params.put("from_block", "0");
        long timeNowSecond = (new Date()).getTime();
        long timestamp = timeNowSecond - 3 * 30 * 24 * 3600 * 1000;
        String toBlock = getBlock(timestamp, params.get("chain")).toString();
        params.put("to_block", toBlock);
        String retStr = HttpsUtils.Get(NFT_API_URL_BASE + walletAddress + EVENTS_API_TAIL, headers, params);

        NftEventsRet nftEventsRet = JSON.parseObject(retStr, NftEventsRet.class);
        if (nftEventsRet != null && nftEventsRet.getResult() != null && nftEventsRet.getResult().size() != 0) {
            return new ResponseEntity<>(200, "valid");
        }
        return new ResponseEntity<>(400, "notValid," + timestamp + ",to_block:" + toBlock);
    }

    public static Integer getBlock(long timestamp, String chain) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", API_KEY);
        Map<String, String> params = new HashMap<String, String>();
        params.put("chain", chain);
        //https://deep-index.moralis.io/api/v2/dateToBlock?chain=eth&date=1667231162186
        String retStr = HttpsUtils.Get(NFT_API_URL_BASE + BLOCK_API_TAIL, headers, params);
        JSONObject retJson = JSON.parseObject(retStr);
        return (Integer) retJson.get("block");
    }

    public static void main(String[] args) {
        System.out.println(getBlock(new Date().getTime(), "eth"));
    }
}
