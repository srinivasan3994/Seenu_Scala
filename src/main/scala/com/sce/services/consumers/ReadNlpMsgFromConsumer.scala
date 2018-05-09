package com.sce.services.consumers

import akka.http.scaladsl.model._
import com.sce.models._
import com.sce.services._
import spray.json._
import spray.json.lenses.JsonLenses._
import com.sce.dao._
import com.sce.models._
import com.sce.utils.KafkaConfig

import scala.collection.JavaConversions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import java.util.{ Collections, Properties }

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException;

import scala.concurrent.duration.Duration
//import com.sce.apis.kafka._

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.Logging

/**
  * Created by Vinoth on 11/07/2017.
  */
      
class ReadNlpMsgFromConsumer extends Runnable with KafkaJsonSupport with FacebookJsonSupport with NlpJsonSupport {

  val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
  val logger = Logging(system, this.getClass)
  val now = java.time.LocalDateTime.now().format(polish)

 // val FB_ERROR_MSG = config.getString("FB_ERROR_MSG")

  val CONSUMER_NLP = config.getString("CONSUMER_NLP")

  var SEND_GLB = ""

  override def run(): Unit =
    {
      var count: Int = 0

      val consumer = new KafkaConsumer[String, String](KafkaConfig.getConsumerConfig)

      consumer.subscribe(Collections.singletonList(CONSUMER_NLP))

      try {
        while (true) {

          try {

            val records = consumer.poll(100)

            count = records.count()

            for (record <- records) {
              
              
              logger.info("Topic: {}", CONSUMER_NLP)
              val jsonResponse = record.value()
              logger.info("jsonResponse: {}", jsonResponse)
              val messageResponse = jsonResponse.parseJson.convertTo[NlpActionConsumerObj]
              val sender = messageResponse.identityDtls.msgEvent.platformDtls.userID
              SEND_GLB = sender
              logger.info("in Consumer NLP message response \n{}", messageResponse)

              ApiCallExternalService.callWorkflowAction(messageResponse)

            }
          } catch {

            case e: Exception =>
             e.printStackTrace()
          }
        }
        (count > 0)
      } catch {

        case e: WakeupException => logger.error("exception caught: " + e)

        case e: Exception       => logger.error("IO exception" + e)

      } finally {
        
        consumer.close()
        
        logger.info("Consumer NLP Closing..............")
        
      
        
        
      }
    }
}

object ReadNlpMsgFromConsumer {
  
  def ReadNlpMsgFromConsumer = new ReadNlpMsgFromConsumer
  
}