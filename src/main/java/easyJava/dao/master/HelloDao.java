package easyJava.dao.master;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import easyJava.entity.HelloEntity;

@Mapper
public interface HelloDao {
	public List<HelloEntity> getHello();
}
