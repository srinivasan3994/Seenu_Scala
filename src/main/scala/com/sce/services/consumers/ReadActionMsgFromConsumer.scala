package com.sce.services.consumers

/**
 * Created by Vinoth on 11/07/2017.
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

class ReadActionMsgFromConsumer extends Runnable with KafkaJsonSupport {

  val logger = Logging(system, this.getClass)
  val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")

  val now = java.time.LocalDateTime.now().format(polish)

  val FB_ERROR_MSG = config.getString("FB_ERROR_MSG")

  val ACTION_SEND = config.getString("ACTION_SEND")

  var SEND_GLB = ""

  override def run(): Unit =
    {
      var count: Int = 0

      val consumer = new KafkaConsumer[String, String](KafkaConfig.getConsumerConfig)

      consumer.subscribe(Collections.singletonList(ACTION_SEND))

      try {

        while (true) {

          val records = consumer.poll(100)

          count = records.count()

          for (record <- records.asScala) {

            val jsonResponse = record.value()

            logger.info("jsonResponse: {}", jsonResponse)

            val messageResponse = jsonResponse.parseJson.convertTo[ActionConsumerMessage]

            val sender = messageResponse.recipient

            val text = messageResponse.message
            
            val nlpDtls = messageResponse.nlpConsumerMsg

            val actionResp = messageResponse.actionResponse

            ActionNlpDao.actionResponseprocessing(nlpDtls, actionResp)

            // FacebookService.sendTextMessage(sender, text)

          }

        }
        (count > 0)
      } catch {

        case e: WakeupException => logger.error("exception caught: {}" + e);
        case e: Exception       => logger.error("IO exception {}" + e);
        
      } finally {

        consumer.close()
        logger.info("Read Action Msg From Consumer Closing..............");
        KafkaService.sendErrorLogsToKafkaProducer(SEND_GLB, "", "Read Action Msg From Consumer Closing", "", 0L, 0L)
        
      }

    }

}

object ReadActionMsgFromConsumer {

  def ReadActionMsgFromConsumer = new ReadActionMsgFromConsumer

}
