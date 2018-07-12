package com.bancoazteca.logathena


import groovy.util.logging.Slf4j
import io.krakens.grok.api.Grok
import io.krakens.grok.api.GrokCompiler
import io.krakens.grok.api.Match
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@Component
@Slf4j
class LogQuery {

    @Autowired JdbcTemplate jdbcTemplate
	@Autowired SQLResultRepository repository

    List<Map<String,Object>> searchTerm(ParamsBusqueda params) {
        String query = """SELECT * FROM logbaz."${params.fechaLog.replace("-","_")}" """
		
		query += params.textoLibre?" WHERE body = '${params.textoLibre}'":""
		
		if(params.alias) {
			query += query.contains("WHERE")?" AND ":" WHERE " + "alias = '${params.alias}'"
		}
		
		if(params.codError) {
			query += query.contains("WHERE")?" AND ":" WHERE " + "cod = '${params.codError}'"
		}
		
		if(params.msgError) {
			query += query.contains("WHERE")?" AND ":" WHERE " + "msg = '${params.msgError}'"
		}
		
		if(params.pathServicio) {
			query += query.contains("WHERE")?" AND ":" WHERE " + "path = '${params.pathServicio}'"
		}
		
		if(params.folioOper) {
			query += query.contains("WHERE")?" AND ":" WHERE " + "folio = '${params.folioOper}'"
		}
		
		if(params.rangoTiempo) {
			String[] rangoTiempo = params.rangoTiempo.split("-");
			query+=" AND date BETWEEN parse_datetime('${params.fechaLog} ${rangoTiempo[0]}','yyyy-MM-dd HH:mm:ss') AND parse_datetime('${params.fechaLog} ${rangoTiempo[1]}','yyyy-MM-dd HH:mm:ss')"
		}
		
		query += " LIMIT ${params.limitResults}"
      
        log.info(query)
		
		List<Map<String,Object>> result = repository.findById(query.replaceAll("\n","").replaceAll(" ", ""))?.value?.resultTerm
		
		if(!result) {
			result = jdbcTemplate.queryForList(query)
			repository.save(new SQLResult(sql:query.replaceAll("\n","").replaceAll(" ", ""),resultTerm:result,resultThread:null))
		}
		else {
			result.forEach{
				if(it.date instanceof Date) {
					it.date = new java.sql.Timestamp(it.date.getTime())
				}
			}
		}
		
        return result
    }

    List<String> getThread(Map<String,Object> document, String tableName){
        String query = '''SELECT * FROM %s 
            WHERE thread = '%s'
            AND date BETWEEN parse_datetime('%s','yyyy-MM-dd HH:mm:ss.SSS') AND parse_datetime('%s','yyyy-MM-dd HH:mm:ss.SSS') 
            ORDER BY date ASC'''

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
		String fechaInicial, fechaFinal
		
		if(document.date instanceof Date) {
			Instant instantDateLog = document.date.toInstant();
			LocalDateTime dtLog = LocalDateTime.ofInstant(instantDateLog, ZoneId.systemDefault())
			fechaInicial = dtLog.minusSeconds(2).format(formatter)
			fechaFinal = dtLog.plusSeconds(2).format(formatter)
		}
		else {
			fechaInicial = document.date.toLocalDateTime().minusSeconds(2).format(formatter)
			fechaFinal = document.date.toLocalDateTime().plusSeconds(2).format(formatter)
		}

      

        String sql = String.format(query,tableName,document.thread,fechaInicial,fechaFinal)
        log.info(sql)
		
		List<String> result = repository.findById(sql.replaceAll("\n","").replaceAll(" ", ""))?.value?.resultThread
		
		if(!result) {
			result = jdbcTemplate.queryForList(sql).collect {
				
				LocalDateTime dtLog
				if(document.date instanceof Date) {
					Instant instantDateLog = document.date.toInstant();
					dtLog = LocalDateTime.ofInstant(instantDateLog, ZoneId.systemDefault())
				}
				else {
					dtLog = document.date.toLocalDateTime()
				}
				
	            String.format("%s %s  %s %s - %s", dtLog.format(formatter),it.level,it.thread,it.class,it.body)
	        }
			repository.save(new SQLResult(sql:sql.replaceAll("\n","").replaceAll(" ", ""),resultTerm:null,resultThread:result))
		}
		
		return result

    }

    List<String> getThread(String linelog, String tableName){
        Map<String,Object> document = new HashMap<>(getFieldsFromLineLog(linelog))
        DateTimeFormatter dtf  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")
        document.put("date", Timestamp.valueOf(LocalDateTime.parse(document.get("date"),dtf)))
        return getThread(document, tableName)
    }

    Map<String,Object> getFieldsFromLineLog(String linealog){
        GrokCompiler grokCompiler = GrokCompiler.newInstance()
        grokCompiler.registerDefaultPatterns()
        final Grok grok = grokCompiler.compile("%{TIMESTAMP_ISO8601:date}\\s%{LOGLEVEL:level}\\s+%{DATA:thread}\\s%{DATA:class}\\s-\\s%{GREEDYDATA:body}")
        Match gm = grok.match(linealog)
        return gm.capture()
    }



}
