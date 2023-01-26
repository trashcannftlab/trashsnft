package easyJava.dao.master;

import easyJava.entity.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NftEventDao {

    List<Map> selectTrashOrder(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    Integer selectTrashOrderCount();


}
