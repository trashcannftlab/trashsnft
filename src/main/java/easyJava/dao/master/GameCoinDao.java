package easyJava.dao.master;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GameCoinDao {

    List<Map> selectSumInviteBonus(@Param("ids") List<Integer> ids);


}
