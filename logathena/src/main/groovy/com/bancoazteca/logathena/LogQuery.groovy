package com.bancoazteca.logathena


import groovy.util.logging.Slf4j
import io.krakens.grok.api.Grok
import io.krakens.grok.api.GrokCompiler
import io.krakens.grok.api.Match
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@Slf4j
class LogQuery {

    @Autowired JdbcTemplate jdbcTemplate

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
        return jdbcTemplate.queryForList(query)
    }

    List<String> getThread(Map<String,Object> document, String tableName){
        String query = '''SELECT * FROM %s 
            WHERE thread = '%s'
            AND date BETWEEN parse_datetime('%s','yyyy-MM-dd HH:mm:ss.SSS') AND parse_datetime('%s','yyyy-MM-dd HH:mm:ss.SSS') 
            ORDER BY date ASC'''

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        String fechaInicial = document.date.toLocalDateTime().minusSeconds(2).format(formatter)
        String fechaFinal = document.date.toLocalDateTime().plusSeconds(2).format(formatter)

        String sql = String.format(query,tableName,document.thread,fechaInicial,fechaFinal)
        log.info(sql)

        return jdbcTemplate.queryForList(sql).collect {
            String.format("%s %s  %s %s - %s", it.date.toLocalDateTime().format(formatter),it.level,it.thread,it.class,it.body)
        }

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
