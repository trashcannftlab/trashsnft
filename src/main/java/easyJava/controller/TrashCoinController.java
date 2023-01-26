package easyJava.controller;

import com.alibaba.fastjson.JSON;
import easyJava.Configs;
import easyJava.dao.master.BaseDao;
import easyJava.dao.master.NftEventDao;
import easyJava.dao.master.OrderScanDao;
import easyJava.entity.BaseModel;
import easyJava.entity.ResponseEntity;
import easyJava.job.NFTEventsScanJob;
import easyJava.utils.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TrashCoinController {
    private static final Logger logger = LogManager.getLogger(TrashCoinController.class);
    @Autowired
    NftEventDao nftEventDao;
    public static final String pwd = "123456";
    @Autowired
    BaseDao baseDao;
    @Autowired
    NFTEventsScanJob NFTEventsScanJob;
    @Autowired
    OrderScanDao orderScanDao;

    public static final String ABI = "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Approval\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"previousOwner\",\"type\":\"address\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"indexed\":true,\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"uint256\",\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"Transfer\",\"type\":\"event\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"owner\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"}],\"name\":\"allowance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"account\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"bytes32[]\",\"name\":\"proof\",\"type\":\"bytes32[]\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"claim\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"internalType\":\"uint8\",\"name\":\"\",\"type\":\"uint8\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"subtractedValue\",\"type\":\"uint256\"}],\"name\":\"decreaseAllowance\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"spender\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"addedValue\",\"type\":\"uint256\"}],\"name\":\"increaseAllowance\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"name\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"renounceOwnership\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"rootNew\",\"type\":\"bytes32\"}],\"name\":\"setRoot\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"totalSupply\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"from\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"to\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]\n";

    @RequestMapping("/trash/getReportAll")
    public ResponseEntity<?> getReportAll() {
        Map param = new HashMap();
//        param.put("start_time", TrashCoinController.beginTime);
//        param.put("end_time", new Date().getTime());
        var ret = orderScanDao.getReport(param);
        var recycle_nft_sum = (Double) ret.get("recycle_nft_sum");
        ret.put("recycle_nft_sum", recycle_nft_sum.longValue());
        var trash_coin_sum = (Double) ret.get("trash_coin_sum");
        ret.put("trash_coin_sum", trash_coin_sum.longValue());
        //check is chaim
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
        return new ResponseEntity<>(ret);
    }

    @RequestMapping("/trash/getReportList")
    public ResponseEntity<?> getReportList(@RequestParam Map<String, Object> map) {
        if (map.get("pageSize") == null || map.get("pageSize").toString().length() == 0) {
            return new ResponseEntity(400, "pageSize not null！");
        }
        if (map.get("pageNo") == null || map.get("pageNo").toString().length() == 0) {
            return new ResponseEntity(400, "pageNo not null！");
        }
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(map.get("pageNo").toString());
        baseModel.setPageSize(map.get("pageSize").toString());
        baseModel.setOrderColumn("id");
        baseModel.setOrderAsc("desc");
        Map param = new HashMap();
        param.put("tableName", "order_report");
        var ret = orderScanDao.getReportList(param, baseModel);
        var count = orderScanDao.getReportListCount(param);
        return new ResponseEntity<>(ret, count,
                map.get("pageNo").toString(), map.get("pageSize").toString());
    }

    @RequestMapping("/trash/getABI")
    public ResponseEntity<?> getABI() {
        return new ResponseEntity<>(ABI);
    }

    @RequestMapping("/trash/getProof")
    public ResponseEntity<?> getProof(@RequestParam Map<String, Object> map) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("array", JSON.toJSONString(NFTEventsScanJob.getOrdersCoinList(NFTEventsScanJob.getOrdersMap())));
        params.put("address", map.get("address").toString().toLowerCase());
        String proof = HttpUtil.get("http://localhost:8080/xxxx/getProof", params);
        Map mapRet = JSON.parseObject(proof);
        return new ResponseEntity<>(mapRet);
    }

    /**
     *
     * @param map
     * @return
     */
    @RequestMapping("/trash/estimate")
    public ResponseEntity<?> getNFTAssets(@RequestParam Map<String, Object> map) {
        Map<String, Object> ret = new HashMap<String, Object>();
        //nft count
        int nftCount = Integer.parseInt(map.get("count").toString());
        int rank = nftEventDao.selectTrashOrderCount();
        //trashCoin count
        var now = new Date().getTime();
        ret.put("trashCoin", getTrashCoin(nftCount, getAMP(now), rank));
        ret.put("rank", rank);
        ret.put("now", now);
        var beginTime = Long.parseLong(Configs.getBeginTime());
        ret.put("beginTime", beginTime);
        return new ResponseEntity<>(ret);
    }

    @RequestMapping("/trash/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> ret = new HashMap<String, Object>();
        int rank = nftEventDao.selectTrashOrderCount();
        ret.put("destructionNFT", 683306);
        ret.put("trashNFTSupply", 10000);
        ret.put("trashNFTDoneMint", 1000);
        ret.put("trashCoinSupply", 1750000000);
        ret.put("trashCoinCurrent", 17500000);
        var beginTime = Long.parseLong(Configs.getBeginTime());
        ret.put("beginTime", beginTime);
        ret.put("rank", rank);
        Map q = new HashMap();
        q.put("tableName", NFTEventsScanJob.TRASH_ORDER_TABLE);
        int mintPlayer = baseDao.selectBaseCount(q);
        ret.put("mintPlayer", mintPlayer);
        ret.put("amp", getAMP(new Date().getTime()));
        return new ResponseEntity<>(ret);
    }

    @RequestMapping("/trash/orders")
    public ResponseEntity<?> orders(@RequestParam Map<String, Object> map) {
        if (map.get("to_account") == null || map.get("to_account").toString().length() == 0) {
            return new ResponseEntity(400, "to_account is empty！");
        }
        if (map.get("pageSize") == null || map.get("pageSize").toString().length() == 0) {
            return new ResponseEntity(400, "pageSize is empty！");
        }
        if (map.get("pageNo") == null || map.get("pageNo").toString().length() == 0) {
            return new ResponseEntity(400, "pageNo is empty！");
        }
        map.put("tableName", NFTEventsScanJob.TRASH_ORDER_TABLE);
        map.put("to_account", map.get("to_account").toString().toLowerCase());
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(map.get("pageNo").toString());
        baseModel.setPageSize(map.get("pageSize").toString());
        var ret = baseDao.selectBaseList(map, baseModel);

        return new ResponseEntity<>(ret);
    }

    @RequestMapping("/test/trash/ordersDelete")
    public ResponseEntity<?> ordersDelete(@RequestParam Map<String, Object> map) {
        if (map.get("to_account") == null || map.get("to_account").toString().length() == 0) {
            return new ResponseEntity(400, "to_account is empty！");
        }
        map.put("tableName", NFTEventsScanJob.TRASH_ORDER_TABLE);
        map.put("to_account", map.get("to_account").toString().toLowerCase());
        var ret = baseDao.delete(map);
        return new ResponseEntity<>(ret);
    }

    /**
     */
    public static final long top = 100000000;

    public static long getTrashCoin(int n, long amp, int rank) {
        var beginTime = Long.parseLong(Configs.getBeginTime());
        logger.info("getTrashCoin n:" + n + ",amp:" + amp + ",rank:" + rank + ",begin:" + beginTime);
        if (amp <= 0) {
            return 0l;
        }
        if (n == 0) {
            return 0;
        }
        var ampAndRank = new BigDecimal(0.99999).pow(rank).multiply(new BigDecimal(amp));
        if (n > 10) {
            n = 10;
        }
        var log2 = 1 + log(n * n, 2) / 10;
        BigDecimal result = ampAndRank.multiply(new BigDecimal(log2)).multiply(new BigDecimal(30000));
        long ret = result.longValue();
//        if (ret > top) {
//            ret = top;
//        }
        logger.info("getTrashCoin n:" + n + ",amp:" + amp + ",rank:" + rank + ",begin:" + beginTime + ",ret:" + ret);
        return ret;
    }

    public static double log(double value, double base) {
        return Math.log(value) / Math.log(base);
    }

    /**
     *
     * @param time
     * @return
     */
    public static long getAMP(long time) {
        var beginTime = Long.parseLong(Configs.getBeginTime());
        if (time < beginTime) {
            logger.error("activity not start");
            return 0;
        }
        long ret = 3000 - (time - beginTime) / 1000 / 3600;
        if (ret < 0) {
            logger.error("activity has end");
            return 0;
        }
        return ret;
    }

    public void transferTrashCoin() {
        try {
            Web3j web3 = Web3j.build(new HttpService(""));  // defaults to http://localhost:8545/
            Web3ClientVersion web3ClientVersion = null;
            web3ClientVersion = web3.web3ClientVersion().send();
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        } catch (Exception e) {
            logger.error("transferTrashCoin", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(getTrashCoin(2, new Date().getTime(), 12));
        System.out.println(getAMP(new Date().getTime()));
        System.out.println(1 + log(2, 2) / 10);

        var bd = new BigDecimal(0.99999).pow(12).multiply(new BigDecimal(getAMP(new Date().getTime())));

        System.out.println(bd.toPlainString());
    }

}
