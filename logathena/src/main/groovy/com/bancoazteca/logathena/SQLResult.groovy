package com.bancoazteca.logathena

import java.util.List
import java.util.Map
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "SQLResult")
class SQLResult implements Serializable {
	
	@Id String sql
	List<Map<String,Object>> resultTerm
	List<String> resultThread

}
