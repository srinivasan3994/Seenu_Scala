package com.sce.utils

import scala.slick.driver.JdbcProfile
import scala.slick.driver.H2Driver
import scala.slick.driver.MySQLDriver
import scala.slick.driver.PostgresDriver
import com.typesafe.slick.driver.ms.SQLServerDriver
import com.typesafe.slick.driver.oracle.OracleDriver
import com.sce.models.DBEnumeration

object SlickDBDriver {

  def getDriver: JdbcProfile = {
    import DBEnumeration._
    scala.util.Properties.envOrElse("runMode", "postgresql") match {
      case MSSQL      => SQLServerDriver
      case ORACLE     => OracleDriver
      case POSTGRESQL => PostgresDriver
      case MYSQL      => MySQLDriver
      case _          => PostgresDriver
    }
  }
}


