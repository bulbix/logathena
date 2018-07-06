package com.bancoazteca.logathena

import org.apache.commons.io.FileUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner)
@SpringBootTest
class LogathenaApplicationTests {

    @Autowired LogQuery logQuery

    @Test
    void testSearchTerm() {
		ParamsBusqueda params = new ParamsBusqueda(folioOper:'1602018629131142873',fechaLog:'2018_06_29',limitResults:1)
        List<Map<String, Object>> documents = logQuery.searchTerm(params)
    }


    @Test
    void testGetThread() {
        //String lineLog = "[#| 2018-06-27 21:13:59,258 ERROR (HTTP-CRED-782) SaldosComponent:471 - Servicio: SALDOS, Sistema: AFORE, Descripcion: TIBCO-AMX-CF-010005: Cannot send message to remote component because no messaging bus has been configured for this node.  |#] "
		List<Map<String, Object>> documents = logQuery.searchTerm("1602018629131142873", 1, null, "logbaz.\"2018_06_29\"")
		def lines = logQuery.getThread(documents[0], "logbaz.\"2018_06_29\"")
        FileUtils.writeLines(new File(String.format("./logBAZ/%s","salida.txt")), lines)

    }


}
