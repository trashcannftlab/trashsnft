package easyJava.etherScan;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public interface ScanService {

    List<Map> doScanEvent() ;

    /**
     * @return
     */
    List<Map> doScanEthToken(String myAddress) ;
    List<Map> doScanETH() ;

}
