package easyJava.dao.master;

import easyJava.entity.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ScnDao {

    List<Map> selectBaseListOr(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);
    Long selectBaseCountOr(@Param("map") Map map);


}
