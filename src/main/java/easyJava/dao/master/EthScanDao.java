package easyJava.dao.master;

import easyJava.entity.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EthScanDao {

    List<Map> selectBaseList(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    List<Map> selectListByHash(@Param("list") List<String> list);

}
