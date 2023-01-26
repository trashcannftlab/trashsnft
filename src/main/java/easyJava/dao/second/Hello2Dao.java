package easyJava.dao.second;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import easyJava.entity.HelloEntity;

@Mapper
public interface Hello2Dao {
	public List<HelloEntity> getHello();
}
