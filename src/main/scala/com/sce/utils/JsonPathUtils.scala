package com.sce.utils

import com.google.inject.Inject
import com.jayway.jsonpath.JsonPath
import com.typesafe.config.Config
import akka.event.LoggingAdapter
import java.sql.ResultSet
import scala.util.control.Breaks
import com.sce.dao._


import com.sce.utils.AppConf._

import com.sce.services._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._


object JsonPathUtils {

  val ERROR_LOG = config.getString("FB_ERROR_LOG")
  val logger = Logging(system, this.getClass)

  def checkResponse(jsonObject: String, successResponse: String): Boolean = {
    logger.info("jsonObject : {}", jsonObject)
    logger.info("Response Node : {}", successResponse)
    var status = false
    try {
      val columnString = config.getString("js_path_finder").r
      val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject)
      logger.info("document : {}", document)
        var parsedSentence = successResponse
        var jsVariablesInSentnce = columnString.findAllMatchIn(parsedSentence).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
        logger.info("jsVariablesInSentnce : {}",jsVariablesInSentnce)
        var questionLoopBreaker = new Breaks()
        questionLoopBreaker.breakable(
        for (i <- 0 until jsVariablesInSentnce.length) {
            val jsValue = JsonPath.read(document, jsVariablesInSentnce(i)).toString();
            logger.info("jsValue : {}",jsValue)
            if(jsValue!=null){
              status = true
            }else{
              status = false
              questionLoopBreaker.break()
            }
        })
       
        logger.info("jsValue : {}",status)
        status
    } catch {
      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.getMessage()
       status
    }
     
   }
  
    def sentenceResponseParserWithoutErr(jsonObject: String, responseSentence: String): String  ={ 
    try {
      val jsString = config.getString("js_path_finder").r
      val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);

      var parsedSentence = responseSentence
      var jsVariablesInSentnce = jsString.findAllMatchIn(parsedSentence).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
      
      if (jsVariablesInSentnce.length > 0) {

        for (j <- 0 until jsVariablesInSentnce.length) {

          val jsValue: String = JsonPath.read(document, jsVariablesInSentnce(j)).toString();
          parsedSentence = parsedSentence.replace("#" + jsVariablesInSentnce(j) + "#", jsValue)

        }
        parsedSentence

      } else {
        responseSentence
      }
    } catch {
      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.getMessage()
      responseSentence
    }

  }

  def isRespHavingErrorMsg(jsonObject: String, errorPath: String): String = {

    try {
    
      val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
      val jsValue: String = JsonPath.read(document, errorPath.substring(1, errorPath.length()-1)).toString();
      jsValue
    } catch {

      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.getMessage
        ""

    }

  } 
  
  
    def getValueFromJson(jsonObject: String, jsPath: String): String = {

    try {
    
      val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
      val jsValue: String = JsonPath.read(document, jsPath).toString();
      jsValue
    } catch {

      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        println(e.getMessage)
        ""

    }

  }

  def getActionValueFromJson(jsonObject: String, jsPath: String): Option[String] = {

    var actionValue: Option[String] = None
    try {
    if(jsPath != ""){
      val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
      actionValue = Some(JsonPath.read(document, jsPath).toString())
    }
      

    } catch {

      case e: Exception =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        logger.info(e.getMessage)
        throw new Exception("Json Evaluation Exception")
        
    }
    logger.info("actionValue: {}", actionValue)
    actionValue
  } 
  
  
    def dbCoiceListConstructor(textparser: String, resultSet: ResultSet): String  ={ 
    try {
      val columnString = config.getString("js_path_finder").r

      var parsedSentence = textparser
      var columnVariablesInSentnce = columnString.findAllMatchIn(parsedSentence).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
      //jsVariablesInSentnce =  jsVariablesInSentnce.map(x => x.replace("{", "[").replace("}", "]"))
      
      if (columnVariablesInSentnce.length > 0) {

        for (j <- 0 until columnVariablesInSentnce.length) {

          val columnValue: String = resultSet.getString(columnVariablesInSentnce(j).toString)
          parsedSentence = parsedSentence.replace("#" + columnVariablesInSentnce(j).toString/*.replace("[", "{").replace("]", "}")*/ + "#", columnValue)

        }
        parsedSentence

      } else {
        ""
      }
    } catch {
      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.getMessage()
      ""
    }

  }
    
    def dbCoiceImageListConstructor(textparser: String, resultSet: ResultSet): String  ={ 
    try {
      val columnString = config.getString("js_path_finder").r

      var parsedSentence = textparser
      var columnVariablesInSentnce = columnString.findAllMatchIn(parsedSentence).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
      
      
      if (columnVariablesInSentnce.length > 0) {

        for (j <- 0 until columnVariablesInSentnce.length) {

          val columnValue: String = resultSet.getString(columnVariablesInSentnce(j).toString)
          parsedSentence = parsedSentence.replace("#" + columnVariablesInSentnce(j).toString + "#", columnValue)

        }
        parsedSentence

      } else {
        ""
      }
    } catch {
      case e: Exception => KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.getMessage()
      ""
    }

  }

  
}