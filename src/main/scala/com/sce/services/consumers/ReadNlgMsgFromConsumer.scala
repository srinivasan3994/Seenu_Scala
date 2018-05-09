package com.sce.services.consumers

/**
 * Created by Vinoth on 11/07/2017.
 */

import com.sce.models._
import com.sce.models.NLPStrings._
import com.sce.services._
import spray.json._
import spray.json.lenses.JsonLenses._
import com.sce.dao._
import com.sce.utils.KafkaConfig

import scala.collection.JavaConversions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import java.util.{ Collections, Properties }

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException;

import scala.concurrent.duration.Duration
//import com.sce.apis.kafka._

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import com.sce.models._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

class ReadNlgMsgFromConsumer extends Runnable with FacebookJsonSupport with KafkaJsonSupport {

  val FB_ERROR_MSG = config.getString("FB_ERROR_MSG")
  val logger = Logging(system, this.getClass)
  val IMAL_SEND = config.getString("IMAL_SEND")

  override def run(): Unit =
    {
      var count: Int = 0

      val consumer = new KafkaConsumer[String, String](KafkaConfig.getConsumerConfig)

      consumer.subscribe(Collections.singletonList(IMAL_SEND))

      try {
        while (true) {

          val records = consumer.poll(100)

          count = records.count()

          try {

            for (record <- records) {

              val jsonResponse = record.value()
              logger.info("jsonResponse: {}", jsonResponse)
              val nlgResponseMsg = jsonResponse.parseJson.convertTo[IMSendConsumer]
              val recepient = nlgResponseMsg.msgEvent.platformDtls.userID
              val nlgMessage = nlgResponseMsg.nlgRespObj
              var platform = ChannelDetailsDao.getPlatformDesc(nlgResponseMsg.msgEvent.platformDtls.platformID)
              val channalRec = ChannelDetailsDao.getChannelRecord(nlgResponseMsg.msgEvent.platformDtls.platformID)
              
              logger.info("platform: {}", platform)
              logger.info("channalRec: {}", channalRec)
              platform match {

                case FACEBOOK =>

                  FacebookService.processNlgMsgForFacebook(nlgResponseMsg, channalRec)

                case PURIST =>
                  PuristChatService.processNlgMsgForPurist(nlgResponseMsg: IMSendConsumer, channalRec)
                  
                case _ =>
                  logger.info("No External webhook found for sending message to User.")

              }
            }
          } catch {
            case e: Exception =>
              logger.info("Error occured while sending message to User through web hook ")
              e.printStackTrace()
          }
        }
        (count > 0)
      } catch {

        case e: WakeupException => logger.error("exception caught: {}" + e);

        case e: Exception =>
          logger.error("IO exception {}" + e);
          e.printStackTrace()

      } finally {

        consumer.close()

        logger.info("IMAL-Send Consumer Closing..............");
      }
    }
}

object ReadNlgMsgFromConsumer {

  def ReadNlgMsgFromConsumer = new ReadNlgMsgFromConsumer

}


