package com.bancoazteca.logathena;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import com.mongodb.MongoClientURI;

@Configuration
public class MongoConfig {
	
	
	@Bean
	public MongoClientURI mongoClient() throws Exception {
		return new MongoClientURI("mongodb://UsrBazDigital:B4zD1g1T4l20164$78@10.63.32.180:27021/BAZBDMDESA");
 
	}
 
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
 
		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongoClient());
		MongoTemplate mongoTemplate = new MongoTemplate(simpleMongoDbFactory);
		((MappingMongoConverter) mongoTemplate.getConverter()).setTypeMapper(new DefaultMongoTypeMapper(null));
		return mongoTemplate;
 
	}

}
