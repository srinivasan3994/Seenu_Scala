package com.sce.utils

import scala.slick.driver.JdbcProfile
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.sce.models.DBEnumeration._
import com.sce.models.NLPStrings._
import java.io.File
import com.sce.main.Main.ENVIRONMENT

class DBConnection(override val profile: JdbcProfile) extends Profile {

  import profile.simple._

  def dbObject(): Database = {

    var env_path = if (ENVIRONMENT == TEST) {
      ENV_TEST_PATH + BC_DB_CONF_PATH

    } else {
      ENV_PROD_PATH + BC_DB_CONF_PATH
    }

    val env = scala.util.Properties.envOrElse("runMode", "postgresql") match {
      case MSSQL =>

        ConfigFactory.parseFile(new File(env_path + MSSQL_CONF_PATH))

      case ORACLE =>

        ConfigFactory.parseFile(new File(env_path + ORACLE_CONF_PATH))

      case POSTGRESQL =>

        ConfigFactory.parseFile(new File(env_path + POSTGRES_CONF_PATH))

      case MYSQL =>

        ConfigFactory.parseFile(new File(env_path + MYSQL_CONF_PATH))

      case _ =>

        ConfigFactory.parseFile(new File(env_path + POSTGRES_CONF_PATH))
    }
    val config = ConfigFactory.load(env)
    val url = config.getString("db.url")
    val username = config.getString("db.username")
    val password = config.getString("db.password")
    val driver = config.getString("db.driver")
    //println("Connection info =>" + "Run mode: " + env + ", db url: " + url + ", driver: " + driver)
    Database.forURL(url, username, password, null, driver)
  }
}