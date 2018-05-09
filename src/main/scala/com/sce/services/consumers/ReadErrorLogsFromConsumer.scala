package com.sce.services.consumers

/**
  * Created by Vinoth on 12/04/2017.
  */

import com.sce.models._
import spray.json._
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.errors.WakeupException;

import scala.concurrent.Future
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util._
import scala.collection.JavaConverters._
import com.sce.services._
import com.sce.utils.KafkaConfig
import org.joda.time.DateTime
//import com.sce.apis.kafka._

import com.sce.dao._

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

class ReadErrorLogsFromConsumer extends Runnable with KafkaJsonSupport {
  
  val logger = Logging(system, this.getClass)
  val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
  
  val now = java.time.LocalDateTime.now().format(polish)
  
  val FB_ERROR_MSG = config.getString("FB_ERROR_MSG")
  
  val IM_LOGS = config.getString("IM_LOGS")
  
  var SEND_GLB=""
  
  override def run() : Unit =
  {
      var count:Int=0
      
      val consumer = new KafkaConsumer[String, String](KafkaConfig.getConsumerConfig)
      
      consumer.subscribe(Collections.singletonList(IM_LOGS))
      
      try{
        
        while(true){
          
          val records=consumer.poll(100)
          
          count=records.count()

          for (record<-records.asScala){
            
            val jsonResponse =record.value()
            
            val messageResponse = jsonResponse.parseJson.convertTo[ConsumerErrorLogs]
            
            val sender = messageResponse.senderID
            val sessionId = messageResponse.sessionId
            val message = messageResponse.message
            val messageSource = messageResponse.messageSource
            val intentID = messageResponse.IntentID
            val conversationID = messageResponse.conversationID
            
            IMSessionDao.insertIMErrorLogs(sessionId, message, messageSource, intentID, conversationID)
            
          }
        }
        (count>0)
      }catch{
        
        case e: WakeupException        => logger.error("exception caught: {}" + e);
        
        case e: Exception              => logger.error("IO exception {}"+e);
      }
    finally{
        
        consumer.close()
        
        logger.info("Read Logs From Consumer Closing..............");
        
        //FacebookService.sendTextMessage(SEND_GLB,FB_ERROR_MSG)
        //IMALLogsDao.insertIMALLogs("Error code", "Error code", "IMAL-Receive consumer Closing..............", None)
        IMSessionDao.insertIMErrorLogs(Some(""), "Read Logs From Consumer Closing", Some(""), Some(0L), Some(0L))
      }
  }
}

                         
object ReadErrorLogsFromConsumer {
  
  def ReadErrorLogsFromConsumer = new ReadErrorLogsFromConsumer
  
}
