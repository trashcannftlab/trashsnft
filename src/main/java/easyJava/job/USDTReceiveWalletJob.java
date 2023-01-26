package easyJava.job;

import com.alibaba.fastjson.JSON;
import easyJava.dao.master.BaseDao;
import easyJava.entity.BaseModel;
import easyJava.utils.DESUtils;
import easyJava.utils.DateUtils;
import easyJava.utils.GenerateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.tron.core.exception.CipherException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://blog.csdn.net/weixin_41602901/article/details/121759042
 * https://github.com/lim960/Eth/blob/master/ERC20_USDT%E6%99%BA%E8%83%BD%E5%90%88%E7%BA%A6/solidity.js
 * https://github.com/lim960/Eth/blob/master/ERC20_USDT%E6%99%BA%E8%83%BD%E5%90%88%E7%BA%A6/solidity.js
 * https://www.cnblogs.com/bizzan/p/11339063.html?ivk_sa=1024320u
 */
@EnableScheduling
@Service
public class USDTReceiveWalletJob {
    public static final Logger logger = LoggerFactory.getLogger(USDTReceiveWalletJob.class);
    @Autowired
    BaseDao baseDao;
    public static final String USDT_RECEIVE_ACCOUNT_TABLE = "usdt_receive_account";

    /**
     *
     *
     * @return
     */
    public Map getOneIdleAccount(String chainType) throws CipherException {
        Map query = new HashMap<>();
        query.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
        query.put("status", 1);
        query.put("chain_type", chainType);
        BaseModel baseModel = new BaseModel();
        baseModel.setPageSize(1);
        baseModel.setPageNo(1);
        baseModel.setOrderColumn("update_time");
        baseModel.setOrderAsc("asc");
        List<Map> oneAccount = baseDao.selectBaseListOrder(query, baseModel);
        Map ret = new HashMap<>();
        if (oneAccount == null || oneAccount.size() == 0) {
            ret = generateWallet(chainType);
            lockAccount(ret);
        } else {
            int success = lockAccount(ret);
            if (success == 1) {
                ret = parseWallet(oneAccount.get(0));
            } else {
                logger.warn("lockAccount fail:" + JSON.toJSONString(ret));
                ret = generateWallet(chainType);
                lockAccount(ret);
            }
        }
        return ret;
    }

    /**
     *
     */
    //@Scheduled(cron = "0 * * * * ?")
    public void releaseAccountsAndSetOrderFail() {
        logger.info("-----------releaseAccountsAndSetOrderFail begin-----------");
        var busyAccounts = getInUseAccounts(null);
        busyAccounts.forEach(account -> {
            var update = Date.from(((LocalDateTime) account.get("update_time")).atZone(ZoneId.of("+8")).toInstant());
            //
            if (update.before(DateUtils.getDateTimePastMin(new Date(), -3 * 60))) {
                Map<String, Object> queryOrderMap = new HashMap();
                queryOrderMap.put("to_address", account.get("address").toString());
                queryOrderMap.put("status", 1);
                BaseModel baseModel = new BaseModel();
                baseModel.setPageSize(1);
                baseModel.setPageNo(1);
                List<Map> list = baseDao.selectBaseList(queryOrderMap, baseModel);
                if (list != null && list.size() != 0) {
                    list.forEach(order -> {
                        order.put("status", 4);
                        baseDao.updateBaseByPrimaryKey(order);
                        logger.info("-----------releaseAccountsAndSetOrderFail fail order-----------:" + JSON.toJSONString(order));
                    });
                }
                account.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
                account.put("status", 1);
                account.put("update_time", LocalDateTime.now(ZoneId.of("+8")));
                baseDao.updateBaseByPrimaryKey(account);
                logger.info("-----------releaseAccountsAndSetOrderFail release account-----------:" + JSON.toJSONString(account));
            }
        });
        logger.info("-----------releaseAccountsAndSetOrderFail end-----------");
    }

    /**
     *
     */
    public List<Map> getInUseAccounts(String chainType) {
        Map query = new HashMap<>();
        query.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
        query.put("status", 2);
        if (chainType != null)
            query.put("chain_type", chainType);
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(1);
        baseModel.setPageSize(100);
        List<Map> inUseAccounts = baseDao.selectBaseList(query, baseModel);
        return inUseAccounts;
    }

    /**
     *
     *
     * @param query
     */
    public int lockAccount(Map query) {
        query.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
        query.put("status", 2);
        query.put("update_time", LocalDateTime.now(ZoneId.of("+8")));
        return baseDao.updateBaseByPrimaryKey(query);
    }

    /**
     *
     * @param query
     */
    public void lockReleaseAccount(Map query) {
        query.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
        query.put("status", 1);
        query.put("update_time", LocalDateTime.now(ZoneId.of("+8")));
        baseDao.updateBaseByPrimaryKey(query);
    }

    /**
     *
     * @return
     */
    public Map generateWallet(String chainType) throws CipherException {
        String privateKey = "";
        String address = "";
        String tron_address = "";
        Map walletMap = new HashMap<>();
        walletMap.put("tableName", USDT_RECEIVE_ACCOUNT_TABLE);
        walletMap.put("address", address);
        walletMap.put("tron_address", tron_address);
        int encrypt_key = GenerateUtils.getRandomOneToMax(400000);
        walletMap.put("encrypt_key", encrypt_key);
        walletMap.put("encrypted_private", DESUtils.encrypt(privateKey, encrypt_key));
        //1 idle，2 inuse
        walletMap.put("status", 1);
        walletMap.put("update_time", LocalDateTime.now(ZoneId.of("+8")));
        //
        walletMap.put("balance", 0);
        //
        walletMap.put("use_count", 0);
        walletMap.put("chain_type", chainType);
        baseDao.insertBase(walletMap);
        return walletMap;
    }

    /**
     *
     *
     * @param useWallet
     * @return
     */
    public Map parseWallet(Map useWallet) {
        String encrypt_key = useWallet.get("encrypt_key").toString();
        String encrypted_private = useWallet.get("encrypted_private").toString();
        String address = useWallet.get("address").toString();
        String walletPrivate = DESUtils.encrypt(encrypted_private, Integer.parseInt(encrypt_key));
        Map ret = new HashMap();
        ret.put("walletPrivate", walletPrivate);
        ret.put("address", address);
        logger.info("parseWallet:" + address + ",private:" + walletPrivate);
        return ret;
    }
}
