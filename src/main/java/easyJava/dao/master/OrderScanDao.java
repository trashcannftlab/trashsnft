package easyJava.dao.master;

import easyJava.entity.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderScanDao {

    int updateOrderOutOfDate();
    Map getReport(@Param("map") Map map);
    List<Map> getReportList(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);
    int getReportListCount(@Param("map") Map map);
}
