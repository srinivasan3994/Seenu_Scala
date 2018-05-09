package com.sce.services

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import spray.json._
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.errors.WakeupException;

import scala.concurrent.Future
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util._
import scala.collection.JavaConverters._
////import com.sce.apis.kafka._
import com.sce.models._
import com.sce.models._
import com.sce.utils.KafkaConfig
import com.google.inject.name.Named

import com.sce.utils.AppConf._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

object KafkaService extends KafkaJsonSupport with NlpJsonSupport{

  val logger = Logging(system, this.getClass)
  def sendPlatformMsgToNlpKafkaProducer(topic: String, message: String) {
    logger.info("Send Platform Message To Kafka Producer : [{}] to sender [{}]", topic, message)
    try {
      val producer = new KafkaProducer[String, String](KafkaConfig.getProducerConfig)
      logger.info("Topic: {}", topic)
      val record = new ProducerRecord(topic, "key", message)
      println("Sending message")
      producer.send(record)
      producer.close()
    } catch {

      case ee: java.util.concurrent.ExecutionException =>
        logger.error("Kafka server not available so we have to redirect the requests to other source {}", ee.getMessage)

      case e: Exception => // logger.error("Kafka producer exception {}",e)
        e.printStackTrace()
    }
  }
  
  def sendNlpMsgToKafkaProducer(topic: String, messageDtls : NlpMessageDtlsObj,sessionId: String,IdentityDetails: NlpIdentificationDtlsObj) {
      logger.info("Send NLP Message to Kafka Producer : [{}] to sender [{}]", topic, messageDtls)
      try{ 
        val producer = new KafkaProducer[String, String](KafkaConfig.getProducerConfig)
        val jsonFormat = NlpActionConsumerObj(messageDtls, IdentityDetails)
        val consumerJson=jsonFormat.toJson.convertTo[NlpActionConsumerObj]
        logger.info("Producer Cusumer-Nlp Record Json : \n{}",consumerJson.toJson.prettyPrint);
        logger.info("Topic: {}", topic)
        val record = new ProducerRecord(topic,"key",consumerJson.toJson.toString())
        producer.send(record)
        producer.close()
      }catch{
        case ee: java.util.concurrent.ExecutionException =>
        logger.error("Kafka server not available so we have to redirect the requests to other source {}", ee.getMessage)
         case e:Exception => logger.error("Kafka producer exception {}",e)
      }
  }
  
  def sendNlgMsgToKafkaProducer( topic: String, msgEvent:NlpReqCommonObj, sessionID: String, intentID:Option[Long], nlgResObj: nlgResponseObj ) {
      logger.info("Send Nlg Message to Kafka Producer : [{}] to sender [{}]", topic, nlgResObj)
      try{
         val producer = new KafkaProducer[String, String](KafkaConfig.getProducerConfig)
         val jsonFormat = IMSendConsumer(msgEvent, sessionID, intentID, nlgResObj)
         val consumerJson=jsonFormat.toJson.convertTo[IMSendConsumer]
         logger.info("Producer IMAL-Send Record Json : \n{}",consumerJson.toJson.prettyPrint);
         logger.info("Topic: {}", topic)
         val record = new ProducerRecord(topic,"key",consumerJson.toJson.toString())
         producer.send(record)
         producer.close()
      }catch{
        case ee: java.util.concurrent.ExecutionException =>
        logger.error("Kafka server not available so we have to redirect the requests to other source {}", ee.getMessage)
         case e:Exception => logger.error("Kafka producer exception {}",e)
      }
  }
  
  def sendActionMsgToKafkaProducer(topic: String, recipient:String,message: String, nlpDtls: NlpActionConsumerObj, actionResponse: String) {
      logger.info("Send Action Message To Kafka Producer : [{}] to sender [{}]", topic, message)
      try{
         val producer = new KafkaProducer[String, String](KafkaConfig.getProducerConfig)
         val actionConsumerMessage = ActionConsumerMessage(recipient,message, nlpDtls, actionResponse)
         logger.info("Topic: {}", topic)
         val record = new ProducerRecord(topic,"key",actionConsumerMessage.toJson.toString())
         producer.send(record)
         producer.close()
       }catch{
         case ee: java.util.concurrent.ExecutionException =>
        logger.error("Kafka server not available so we have to redirect the requests to other source {}", ee.getMessage)
         case e:Exception => logger.error("Kafka producer exception {}",e)
       }
  }  
  

   def sendErrorLogsToKafkaProducer(senderID:String,sessionId: String, message:String,messageSource: String, IntentID: Long, conversationID: Long) {
      logger.info("Send Logs To Kafka Producer : [{}] to sender [{}]", messageSource, message)
      val TOPIC = config.getString("IM_ERROR_LOGS_TOPIC")
      try{
         val producer = new KafkaProducer[String, String](KafkaConfig.getProducerConfig)
         val actionConsumerMessage = ConsumerErrorLogs(senderID,Some(sessionId), message,Some(messageSource), Some(IntentID), Some(conversationID))
         val record = new ProducerRecord(TOPIC,"key",actionConsumerMessage.toJson.toString())
         producer.send(record)
         producer.close()
       }catch{
         case ee: java.util.concurrent.ExecutionException =>
        logger.error("Kafka server not available so we have to redirect the requests to other source {}", ee.getMessage)
         case e:Exception => logger.error("Kafka producer exception {}",e)
       }
  }
  
}