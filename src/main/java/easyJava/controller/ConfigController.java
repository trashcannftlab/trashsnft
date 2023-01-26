package easyJava.controller;

import easyJava.Configs;
import easyJava.dao.master.BaseDao;
import easyJava.entity.ResponseEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {
    private static final Logger logger = LogManager.getLogger(ConfigController.class);
    @Autowired
    BaseDao baseDao;

    @RequestMapping("/trash/config")
    public ResponseEntity config() {
        Map ret = new HashMap();
        ret.put("network", Configs.getEthNetwork());
        ret.put("systemAddress", Configs.getSystemAddress());
        ret.put("nftContract", Configs.getTrashNftContractAddr());
        ret.put("trashCoinContract", Configs.getTrashContractAddr());
        ret.put("activityStartTime", Configs.getBeginTime());
        return new ResponseEntity(ret);
    }
}
