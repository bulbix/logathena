package com.bancoazteca.logathena

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SQLResultRepository extends CrudRepository<SQLResult,String> {

}
