package easyJava.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import easyJava.Configs;
import easyJava.dao.master.BaseDao;
import easyJava.entity.BaseModel;
import easyJava.entity.ResponseEntity;
import easyJava.entity.TransactionMy;
import easyJava.utils.HttpUtil;
import easyJava.utils.HttpsUtils;
import easyJava.utils.MapBeanUtil;
import io.reactivex.functions.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

@RestController
public class Web3jController {
    private static final Logger logger = LogManager.getLogger(Web3jController.class);
    @Autowired
    BaseDao baseDao;
    public static final String TRANSACTION_RECEIPT_TABLE_NAME = "accounts_coin_transaction_receipts";
    public static final String TRANSACTION_TABLE_NAME = "accounts_coin_transactions";
    public static final String ACCOUNT_TABLE_NAME = "accounts_coin_balance";
    public static final String pwd = "382697973307508894";
    WebSocketService ws = new WebSocketService(Configs.getEthHost(), false);
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(42000);
    public static final BigInteger ETH_WEI = BigInteger.valueOf(1000000000000000000L);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private void initWsToEthNode() {
        try {
            ws.close();
        } catch (Exception e) {
        }
        try {
            ws = new WebSocketService(Configs.getEthHost(), false);
            // subscribeTransactions(ws);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Web3jController() {
        try {
            ws.connect();
            subscribeTransactions(ws);
        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }

    private void subscribeTransactions(WebSocketService ws) {
        Web3j web3 = Web3j.build(ws);
//        web3.transactionFlowable().subscribe(new TransactionConsumer("finish") {
//        });
//        web3.pendingTransactionFlowable().subscribe(new TransactionConsumer("pending") {
//        });
    }

    public class TransactionConsumer implements Consumer<Transaction> {
        private String pending = "";

        public TransactionConsumer(String pending) {
            this.pending = pending;
        }

        private Set<String> addressSet = new HashSet<>();

        private long lastUpdate = new Date().getTime();

        @Override
        public void accept(Transaction transaction) {
            try {
                dealWithPendingTransaction(transaction);
                String tStr = JSON.toJSONString(transaction);
                TransactionMy t = JSON.parseObject(tStr, TransactionMy.class);
                t.setTime(new Date().getTime() + "");
                t.setPending(pending);
                if (hasEthAccountInOurDB(transaction.getFrom())) {
                    insertTransaction(t, TRANSACTION_TABLE_NAME);
                    return;
                }
                if (hasEthAccountInOurDB(transaction.getTo())) {
                    insertTransaction(t, TRANSACTION_TABLE_NAME);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean hasEthAccountInOurDB(String address) {
            if (addressSet.contains(address)) {
                return true;
            } else {
                if (new Date().getTime() - lastUpdate > 10 * 1000) {
                    updateAddressSet();
                    return hasEthAccountInOurDB(address);
                }
                return false;
            }
        }

        private synchronized void updateAddressSet() {
            if (new Date().getTime() - lastUpdate < 10 * 1000) {
                return;
            }
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("coin_name", "ETH");
            //queryMap.put("recharge_address", address);
            queryMap.put("tableName", ACCOUNT_TABLE_NAME);
            var result = baseDao.selectBaseList(queryMap, new BaseModel().setPageNo(1).setPageSize(100000).setOrderAsc("desc"));
            result.forEach(map -> {
                if (map.get("recharge_address") != null) {
                    String address = map.get("recharge_address").toString();
                    addressSet.add(address);
                }
            });
            lastUpdate = new Date().getTime();
        }

    }


    private static boolean isValidHexQuantity(String value) {
        if (value == null) {
            return false;
        } else if (value.length() < 3) {
            return false;
        } else {
            return value.startsWith("0x");
        }
    }

    private static final String DEFAULT_V = "0x111";

    private void dealWithPendingTransaction(Transaction t) {
        if (t.getNonceRaw() == null || t.getNonceRaw().length() == 0) {
            t.setNonce(DEFAULT_V);
        }
        if (t.getBlockNumberRaw() == null || t.getBlockNumberRaw().length() == 0) {
            t.setBlockNumber(DEFAULT_V);
        }
        if (t.getTransactionIndexRaw() == null || t.getTransactionIndexRaw().length() == 0) {
            t.setTransactionIndex(DEFAULT_V);
        }
        if (t.getGasPriceRaw() == null || t.getGasPriceRaw().length() == 0) {
            t.setGasPrice(DEFAULT_V);
        }
        if (t.getGasRaw() == null || t.getGasRaw().length() == 0) {
            t.setGas(DEFAULT_V);
        }
        if (t.getValueRaw() == null || t.getValueRaw().length() == 0) {
            t.setValue(DEFAULT_V);
        }
    }

    private void insertTransaction(TransactionMy transaction, String tableName) {
        Map map = JSON.parseObject(JSON.toJSON(transaction).toString(), Map.class);
        map.put("tableName", tableName);
        System.out.println(JSON.toJSON(map));
        baseDao.insertUpdateBase(map);
    }

    @GetMapping("/v1/web3j/ethHost")
    public ResponseEntity ethHost() {
        return new ResponseEntity(Configs.getEthHost());
    }

    @PostMapping("/v1/web3j/createWallet")
    public ResponseEntity createWallet(@RequestParam("uuid") String uuid) throws CipherException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, IOException {
        Map map = createAccount(uuid);
        return new ResponseEntity(map);
    }

    private static Map createAccount(String uuid) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        Map map = new HashMap();
        String walletFileName = "";//
        String walletFilePath = getWalletFilePath(uuid);
        File dir = new File(walletFilePath);
        dir.setWritable(true, false);
        dir.mkdirs();
        if (dir.list().length == 0) {
            walletFileName = WalletUtils.generateNewWalletFile(pwd, dir, false);
        }
        Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
        String address = credentials.getAddress();
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
        map.put("uuid", uuid);
        map.put("address", address);
        map.put("publickey", publicKey);
        map.put("privatekey", privateKey);
        map.put("walletfilename", walletFileName);
        return map;
    }

    private static String getWalletFilePath(String uuid) {
        String walletFilePath = "./eth_wallets/uid_" + uuid + "/";
        return walletFilePath;
    }

    private static String getWalletFilePathName(String uuid) {
        String walletFilePath = "./eth_wallets/uid_" + uuid + "/";
        File dir = new File(walletFilePath);
        dir.setWritable(true, false);
        String[] files = dir.list();
        if (files != null && files.length != 0) {
            return walletFilePath + files[0];
        }
        return null;
    }

    private static Map createAccountRemote(String uuid) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String urlString = "https://localhost:8080/xxxx/easyJava/v1/web3j/createWallet?uuid=" + uuid;
        String response = HttpsUtils.Post(urlString, new HashMap<>(), null);
        ResponseEntity responseEntity = objectMapper.readValue(response, ResponseEntity.class);
        Map<String, Object> dataMap = responseEntity.getData();
        Map<String, Object> walletMap = (Map<String, Object>) dataMap.get("data");
        return walletMap;
    }

    /**
     *
     * @param uuid
     * @param toAddress
     * @param balance
     * @return
     * @throws Exception
     */
    @PostMapping("/v1/web3j/transferWithoutFee")
    public ResponseEntity transferWithoutFee(@RequestParam("uuid") String uuid, @RequestParam("toAddress") String toAddress
            , @RequestParam("balance") double balance) throws Exception {
        Admin web3 = Admin.build(ws);  // defaults to http://localhost:8545/
        Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                        web3, credentials, toAddress,
                        BigDecimal.valueOf(balance), Convert.Unit.ETHER)
                .send();
        transactionReceipt.setLogsBloom("");
        Map map = MapBeanUtil.object2Map(transactionReceipt);
        map.remove("logs");
        map.remove("logsBloom");
        map.put("tableName", TRANSACTION_RECEIPT_TABLE_NAME);
        baseDao.insertBase(map);
        return new ResponseEntity(transactionReceipt);
    }

    /**
     *
     * @param uuid
     * @param toAddress
     * @param balance
     * @return
     * @throws Exception
     */
    @PostMapping("/v1/web3j/transfer")
    public ResponseEntity transfer(@RequestParam("uuid") String uuid, @RequestParam("toAddress") String toAddress
            , @RequestParam("balance") double balance) throws Exception {
        Admin web3 = Admin.build(ws);  // defaults to http://localhost:8545/
        EthGasPrice ethGasPrice = web3.ethGasPrice().send();
        BigDecimal b = new BigDecimal(balance).multiply(new BigDecimal(ETH_WEI));
        BigDecimal balanceWithoutFee = b.subtract(new BigDecimal(ethGasPrice.getGasPrice().multiply(GAS_LIMIT)))
                .divide(new BigDecimal(ETH_WEI))
                .setScale(15, RoundingMode.DOWN);
        Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                        web3, credentials, toAddress,
                        balanceWithoutFee, Convert.Unit.ETHER)
                .send();
        transactionReceipt.setLogsBloom("");
        Map map = MapBeanUtil.object2Map(transactionReceipt);
        map.remove("logs");
        map.remove("logsBloom");
        map.put("tableName", TRANSACTION_RECEIPT_TABLE_NAME);
        baseDao.insertBase(map);
        return new ResponseEntity(transactionReceipt);
    }

    @GetMapping("/v1/web3j/transfer/history")
    public ResponseEntity history(@RequestParam("uuid") String uuid, @RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize) throws IOException, CipherException {
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(pageNo);
        baseModel.setPageSize(pageSize);
        Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
        Map map = new HashMap();
        map.put("to", credentials.getAddress());
        map.put("from", credentials.getAddress());
        map.put("tableName", TRANSACTION_TABLE_NAME);
        baseModel.setOrderAsc("desc");
        int count = baseDao.selectBaseCountOr(map);
        List<Map> list = baseDao.selectBaseListOr(map, baseModel);
        return new ResponseEntity(list, count, baseModel.getPageNo(), baseModel.getPageSize());
    }

    @GetMapping("/v1/web3j/balance")
    public ResponseEntity ethGetBalance(@RequestParam("address") String address) throws Exception {
        Web3j web3 = Web3j.build(ws);  // defaults to http://localhost:8545/
        try {
            EthGetBalance ret = web3.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
            BigDecimal balance = new BigDecimal(ret.getBalance()).setScale(6)
                    .divide((new BigDecimal(10.000000).pow(18)).setScale(6), RoundingMode.HALF_DOWN).setScale(6);
            logger.info("ethGetBalance,address:" + address + ",balance:" + ret.getBalance());
            return new ResponseEntity(balance.toPlainString());
        } catch (WebsocketNotConnectedException e) {
            initWsToEthNode();
            logger.error("ethGetBalance", e);
        }
        return new ResponseEntity(400, "error");
    }


    @GetMapping("/v1/web3j/gasPrice")
    public ResponseEntity gasPrice() throws Exception {
        Web3j web3 = Web3j.build(ws);
        EthGasPrice ret = web3.ethGasPrice().send();
        return new ResponseEntity(ret);
    }


    @GetMapping("/v1/web3j/getTX")
    public ResponseEntity getTX(@RequestParam("transactionHash") String transactionHash) throws Exception {
        Web3j web3 = Web3j.build(ws);
        EthTransaction ret = web3.ethGetTransactionByHash(transactionHash).send();
        return new ResponseEntity(ret);
    }

    @GetMapping("/v1/web3j/estimate")
    public ResponseEntity estimate(@RequestParam("uuid") String uuid, @RequestParam("toAddress") String toAddress
            , @RequestParam("balance") double balance) {
        Web3j web3 = Web3j.build(ws);
        try {
            BigDecimal v = BigDecimal.valueOf(balance).multiply(new BigDecimal("10000000000000000"));

            EthGasPrice ethGasPrice = web3.ethGasPrice().send();
            Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
            EthEstimateGas gas = web3.ethEstimateGas(new org.web3j.protocol.core.methods.request.Transaction(credentials.getAddress(), BigInteger.valueOf(1),
                    ethGasPrice.getGasPrice(), GAS_LIMIT, toAddress, v.toBigInteger(), "")).send();
            return new ResponseEntity(gas.getResult());
        } catch (Exception e) {
            logger.error("estimate:", e);
        }
        return new ResponseEntity();
    }

    @PostMapping("/v1/web3j/transferETH")
    public ResponseEntity transferETH(@RequestParam("uuid") String uuid, @RequestParam("toAddress") String toAddress
            , @RequestParam("balance") double balance) throws Exception {
        Admin web3 = Admin.build(ws);
        Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                        web3, credentials, toAddress,
                        BigDecimal.valueOf(balance).multiply(new BigDecimal(10).pow(18)), Convert.Unit.WEI)
                .send();
        transactionReceipt.setLogsBloom("");
        return new ResponseEntity(transactionReceipt);
    }

    @PostMapping("/v1/web3j/transferTrash")
    public ResponseEntity transferTrash(@RequestParam("uuid") String uuid, @RequestParam("toAddress") String toAddress
            , @RequestParam("balance") double balance) throws Exception {
        String transactionHash = trashferTrashStatic(ws, uuid, toAddress, balance, new BigInteger("0"));
        System.out.println(transactionHash);
        return new ResponseEntity(transactionHash);
    }

    public static String trashferTrashStatic(WebSocketService ws, String uuid, String toAddress, double balance,
                                             BigInteger noncePlus) {
        logger.info("-------------trashferTrashStatic start--------" + toAddress + "------" + balance);
        String transactionHash = "start";
        try {
            Admin web3j = Admin.build(ws);
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            logger.info("-------------trashferTrashStatic ethGasPrice--------" + ethGasPrice.getGasPrice() + "------");
            Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
            String fromAddress = credentials.getAddress();

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    fromAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            nonce = nonce.add(noncePlus);
            BigDecimal balanceDecimal = new BigDecimal(balance).multiply(new BigDecimal(10).pow(18));
            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(toAddress), new Uint256(balanceDecimal.toBigInteger())),
                    Arrays.asList(new TypeReference<Type>() {
                    }));
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                    ethGasPrice.getGasPrice(),
                    GAS_LIMIT.multiply(new BigInteger("3")),
                    Configs.getTrashContractAddr(), encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            transactionHash = ethSendTransaction.getTransactionHash();
            logger.info("-------------trashferTrashStatic end transactionHash:" + transactionHash + "--------");
            if (transactionHash == null) {
                logger.error("-------------trashferTrashStatic  error:" + ethSendTransaction.getError().getMessage() + "--------");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("trashferTrashStatic", e);
        }
        return transactionHash;
    }

    public Map setMerkleRoot(WebSocketService ws, String uuid, List<List> orders, String contractAddress) {
        logger.info("-------------setMerkleRoot start--------orders:" + JSON.toJSONString(orders) + "---------------");
        String transactionHash = "start";
        Map ret = new HashMap();
        if (orders == null || orders.size() == 0) {
            return null;
        }
        try {
            Admin web3j = Admin.build(ws);
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            logger.info("-------------setMerkleRoot ethGasPrice--------" + ethGasPrice.getGasPrice() + "------");
            Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
            String fromAddress = credentials.getAddress();

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    fromAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            String merkleRootMapStr = "";
            Map<String, String> headers = new HashMap<String, String>();
            Map<String, String> params = new HashMap<String, String>();
            params.put("array", JSON.toJSONString(orders));
            merkleRootMapStr = HttpUtil.get("http://localhost:8080/xxxx/getRoot", params);
            logger.info("trashferTrashNFTMerkleRoot HttpsUtils.Get merkleRoot:" + merkleRootMapStr);
            Map map = JSON.parseObject(merkleRootMapStr);
            String merkleRoot = map.get("root").toString();
            String hasUploadSame = merkleRoot + "_" + contractAddress;
            var has = redisTemplate.opsForValue().get(hasUploadSame);
            ret.put("merkleRoot", merkleRoot);
            if (has != null && has.toString().length() != 0) {
                logger.info("setMerkleRoot hasUploadSame merkleRoot:" + merkleRoot + ",contractAddress:" + contractAddress + ",time:" + has.toString());
                return null;
            } else {
                redisTemplate.opsForValue().set(hasUploadSame, new Date());
            }
            Function function = new Function(
                    "setRoot",
                    Arrays.asList(new Bytes32(Numeric.hexStringToByteArray(merkleRoot))),
                    Arrays.asList(new TypeReference<Type>() {
                    }));
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                    ethGasPrice.getGasPrice(),
                    GAS_LIMIT.multiply(new BigInteger("3")),
                    contractAddress, encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            transactionHash = ethSendTransaction.getTransactionHash();
            ret.put("transactionHash", transactionHash);
            logger.info("-------------setMerkleRoot end transactionHash:" + transactionHash + "--------");
            if (transactionHash == null) {
                logger.error("-------------setMerkleRoot  error:" + ethSendTransaction.getError().getMessage() + "--------");
                redisTemplate.opsForValue().getAndDelete(hasUploadSame);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("setMerkleRoot", e);
            return null;
        }
        return ret;
    }

    public static String mintTrashNFTStatic(WebSocketService ws, String uuid, String toAddress,
                                            BigInteger noncePlus, String contract) {
        logger.info("-------------mintTrashNFTStatic start--------" + toAddress + "------");
        String transactionHash = "start";
        if (toAddress.equalsIgnoreCase("0x0000000000000000000000000000000000000000")) {
            return "not valid address";
        }
        try {
            Admin web3j = Admin.build(ws);
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            logger.info("-------------mintTrashNFTStatic ethGasPrice--------" + ethGasPrice.getGasPrice() + "------");
            Credentials credentials = WalletUtils.loadCredentials(pwd, getWalletFilePathName(uuid));
            String fromAddress = credentials.getAddress();

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    fromAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            nonce = nonce.add(noncePlus);
            Function function = new Function(
                    "mint",
                    Arrays.asList(new Address(toAddress)),
                    Arrays.asList(new TypeReference<Type>() {
                    }));
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                    //gas
                    ethGasPrice.getGasPrice(),
                    GAS_LIMIT.multiply(new BigInteger("3")),
                    contract, encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            transactionHash = ethSendTransaction.getTransactionHash();
            logger.info("-------------mintTrashNFTStatic end transactionHash:" + transactionHash + "--------");
            if (transactionHash == null) {
                logger.error("-------------mintTrashNFTStatic  error:" + ethSendTransaction.getError().getMessage() + "--------");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("mintTrashNFTStatic", e);
        }
        return transactionHash;
    }

    @PostMapping("/v1/web3j/systemAcc")
    public ResponseEntity systemAcc() throws Exception {
        var bigInteger = Numeric.toBigInt(Configs.getSystemPrivate());
        ECKeyPair ecKeyPair = ECKeyPair.create(bigInteger);
        String walletFilePath = "./eth_wallets/uid_system/";
        String filename = WalletUtils.generateWalletFile(pwd, ecKeyPair, new File(walletFilePath), false);
        return new ResponseEntity(filename);
    }

    @PostMapping("/v1/web3j/systemNftAcc")
    public ResponseEntity systemNftAcc() throws Exception {
        var bigInteger = Numeric.toBigInt(Configs.getSystemNftPrivate());
        ECKeyPair ecKeyPair = ECKeyPair.create(bigInteger);
        String walletFilePath = "./eth_wallets/uid_system_nft/";
        String filename = WalletUtils.generateWalletFile(pwd, ecKeyPair, new File(walletFilePath), false);
        return new ResponseEntity(filename);
    }


}
