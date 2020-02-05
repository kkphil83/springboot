package com.springboot.microservices.sample.config;
//package com.koscom.microservices.sample.config;
//
//import javax.sql.DataSource;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import com.koscom.microservices.sample.SampleServiceApplication;
//
//@Configuration
//@MapperScan(basePackages = "com.koscom.microservices.sample.**")
//@EnableTransactionManagement
//public class SampleMybatisConfig {
//	@Autowired
//	private SampleServiceApplication applicationContext;
//	
//	@Bean
//	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//		sqlSessionFactoryBean.setDataSource(dataSource);
//		sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
//				 .getResources("classpath*:mapper/**/*.xml"));
//		return sqlSessionFactoryBean.getObject();
//	}
//	
//
//	@Bean
//	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
//		return new SqlSessionTemplate(sqlSessionFactory);
//	}    
//    
//}
