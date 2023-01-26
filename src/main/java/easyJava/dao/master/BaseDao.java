package easyJava.dao.master;

import easyJava.entity.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseDao {
    int insertBase(@Param("map") Map map);

    int insertUpdateBase(@Param("map") Map map);

    int insertIgnoreBase(@Param("map") Map map);

    Map selectBaseByPrimaryKey(@Param("map") Map map);

    int updateBaseByPrimaryKey(@Param("map") Map map);

    List<Map> selectBaseList(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    List<Map> selectBaseListOrder(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    int selectBaseCount(@Param("map") Map map);

    Integer selectMaxId(@Param("map") Map map);

    List<Map> selectBaseListOr(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    List<Map> selectBaseListOrAnd(@Param("tableName") String tableName,@Param("mapOr") Map mapOr,@Param("mapAnd") Map mapAnd,
                                  @Param("baseModel") BaseModel baseModel);

    int selectBaseCountOr(@Param("map") Map map);

    List<Map> selectBaseShowTableColumns(@Param("map") Map map);

    List<Map> selectBaseTableName();
    int delete(@Param("map") Map map);

}
