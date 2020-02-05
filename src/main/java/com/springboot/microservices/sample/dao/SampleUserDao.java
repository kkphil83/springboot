package com.springboot.microservices.sample.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.springboot.microservices.sample.model.SampleUser;

@Mapper
public interface SampleUserDao {

	/**
	 * 사용자 전체 정보 가져오기 
	 * @return
	 * @throws Exception
	 */
	List<SampleUser> selectUser() throws Exception;		
	
	int selectTest() throws Exception;		
}
			