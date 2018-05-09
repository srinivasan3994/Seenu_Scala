package com.sce.utils

import com.typesafe.config.ConfigFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import akka.event.{ Logging }
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import com.sce.models.NLPStrings._
import com.sce.main.Main.ENVIRONMENT

object AppUtils {
  import AppConf._

  implicit val system = ActorSystem("main-actor-system", ConfigFactory.load())
  implicit val fm = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val sslConfig = AkkaSSLConfig(system)

}

object AppLogging {
  import AppUtils._
  implicit val logger = Logging(system, this.getClass)

}

object AppConf {
  import AppLogging._

  var env_path =
    if (ENVIRONMENT == TEST) {

      ENV_TEST_PATH + BC_APP_CONF_PATH
    } else {

      ENV_PROD_PATH + BC_APP_CONF_PATH
    }

  implicit val config = getConfiguration(env_path + APP_CONF_PATH)

  def getConfiguration(confFilePath: String) = {

    var configOut: Config = null

    try {
      configOut = ConfigFactory.parseFile(new File(confFilePath))
    } catch {
      case ex: FileNotFoundException => {
        logger.error("Missing file exception")
      }
      case ex: IOException => {
        logger.error("IO Exception")
      }
    }
    configOut
  }
  object SCSUtils {

    def getCurrentDateTime = new java.sql.Timestamp(new java.util.Date().getTime).toString()
    
    def removePuchuation(text: String): String = {
      text.toLowerCase().split("\\s+").map(_.replaceAll("^[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]|[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]$", "")).mkString(" ").trim
    }
  }
}