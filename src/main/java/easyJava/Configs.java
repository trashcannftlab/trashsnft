package easyJava;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configs {
    private static volatile String env = "";
    private static volatile String ethHost = "wss://goerli.infura.io/ws/v3/a0d15725f3c2485dac776c3853c381ac";
    //eth trash
    public static volatile String TRASH_CONTRACT_ADDR = "0x12eA68d381Ce97068d0094Bc59bb83e7bA39E2Ca";
    public static volatile String TRASH_NFT_CONTRACT_ADDR = "0x5cD44986C94D0ef2c6e395B34066591Ab56dD503";
    //test
    public static volatile String TRASH_TESTNFT_CONTRACT_ADDR = "0x12eA68d381Ce97068d0094Bc59bb83e7bA39E2Ca";
    public static volatile String SYSTEM_PRIVATE = "8a06a7a22851b7e3a97cbb53641ec2a4b3f287d2844b91ba60823643171c4bdf";
    public static volatile String SYSTEM_ADDRESS = "0x5cD44986C94D0ef2c6e395B34066591Ab56dD503";
    public static volatile String SYSTEM_NFT_PRIVATE = "21d6d0d3efe462b3319627e02e528381a70154438ff3a1ea6f6ffc40029a2055";
    public static volatile String ETH_NETWORK = "goerli";
    //
    public static volatile String BEGIN_TIME = "1669791346148";

    public static String getBeginTime() {
        return BEGIN_TIME;
    }

    @Value("${activity.beginTime}")
    public  void setBeginTime(String beginTime) {
        BEGIN_TIME = beginTime;
    }

    public static String getEthHost() {
        return ethHost;
    }

    @Value("${eth.host}")
    public void setEthHost(String ethHost) {
        Configs.ethHost = ethHost;
    }

    public static String getEnv() {
        return env;
    }

    @Value("${spring.profiles.active}")
    public void setEnv(String env) {
        Configs.env = env;
    }

    public static String getSystemPrivate() {
        return SYSTEM_PRIVATE;
    }

    @Value("${eth.system.private}")
    public void setSystemPrivate(String systemPrivate) {
        SYSTEM_PRIVATE = systemPrivate;
    }

    public static String getSystemAddress() {
        return SYSTEM_ADDRESS;
    }

    @Value("${eth.system.addr}")
    public void setSystemAddress(String systemAddress) {
        SYSTEM_ADDRESS = systemAddress;
    }

    public static String getTrashContractAddr() {
        return TRASH_CONTRACT_ADDR;
    }

    @Value("${eth.trash.contract.addr}")
    public void setTrashContractAddr(String trashContractAddr) {
        TRASH_CONTRACT_ADDR = trashContractAddr;
    }

    public static String getEthNetwork() {
        return ETH_NETWORK;
    }

    @Value("${eth.network}")
    public void setEthNetwork(String ethNetwork) {
        ETH_NETWORK = ethNetwork;
    }


    public static String getTrashNftContractAddr() {
        return TRASH_NFT_CONTRACT_ADDR;
    }

    @Value("${eth.trash.nft.addr}")
    public void setTrashNftContractAddr(String trashNftContractAddr) {
        TRASH_NFT_CONTRACT_ADDR = trashNftContractAddr;
    }

    public static String getTrashTestnftContractAddr() {
        return TRASH_TESTNFT_CONTRACT_ADDR;
    }

    @Value("${eth.trash.testnft.addr}")
    public void setTrashTestnftContractAddr(String trashTestnftContractAddr) {
        TRASH_TESTNFT_CONTRACT_ADDR = trashTestnftContractAddr;
    }

    public static String getSystemNftPrivate() {
        return SYSTEM_NFT_PRIVATE;
    }

    @Value("${eth.system.nft.private}")
    public  void setSystemNftPrivate(String systemNftPrivate) {
        SYSTEM_NFT_PRIVATE = systemNftPrivate;
    }
}