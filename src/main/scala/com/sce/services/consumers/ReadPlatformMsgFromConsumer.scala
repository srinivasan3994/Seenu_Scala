package com.sce.services.consumers

import akka.actor.ActorSystem

import akka.stream.Materializer
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

import com.sce.dao._

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import com.sce.models.NLPStrings._
import com.sce.models._
import com.sce.models.NLPErrorCodes._
import com.sce.models.nlgResponseObj
//import scala.concurrent.ExecutionContext.Implicits.global
import com.sce.models.NlpReqCommonObj
import com.sce.models.NlpJsonSupport
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import com.sce.utils.DomainComponent
import com.sce.utils.Profile
import scala.slick.driver.JdbcProfile
import com.sce.utils.SlickDBDriver
import com.sce.utils.DBConnection
import scala.slick.jdbc.JdbcBackend

/**
 * Created by Vinoth on 11/07/2017.
 */

class ReadPlatformMsgFromConsumer extends Runnable with NLGJsonSupport with NlpJsonSupport  with DomainComponent with Profile {

  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val FB_ERROR_MSG = config.getString("FB_ERROR_MSG")
  val logger = Logging(system, this.getClass)
  val IMAL_RECEIVE = config.getString("IMAL_RECEIVE")

  override def run(): Unit =
    {
      val consumer = new KafkaConsumer[String, String](KafkaConfig.getConsumerConfig)

      consumer.subscribe(Collections.singletonList(IMAL_RECEIVE))

      try {

        while (true) {

          val records = consumer.poll(100)
          for (record <- records.asScala) {
            val f: Future[String] = Future {
              try {

                db withSession { implicit session =>

                  var sessionID = ""
                  val reason = REASON
                  var isNewUser = false;
                  val jsonResponse: JsValue = record.value().parseJson
                  logger.info("Read Platform Message From KafkaConsumer : {}", jsonResponse)

                  val event = jsonResponse.convertTo[NlpReqCommonObj]
                  logger.info("event: {}", event)

                  val platformID = event.platformDtls.platformID
                  val userID = event.platformDtls.userID

                  val channelID = IMSessionDao.getChannelID(platformID)
                  logger.info("channelID: {}", channelID)
                  if (!IMSessionDao.checkIfUserIsRegistered(userID, channelID)) {
                    //Registering user with botChestra
                    logger.info("Registering user with bot chestra")
                    IMSessionDao.insertUserMapEntry(userID, channelID)
                  }
                  logger.info("###############Checking if user session is already created or not.#####################")
                  if (IMSessionDao.checkUserIMSession(userID, channelID)) {
                    //Checking user Session is Expired or not.
                    val isSessionExpired = IMSessionDao.checkExpiredStatus(userID, channelID)
                    logger.info("Checking user Session is Expired or not. {}", isSessionExpired)
                    if (isSessionExpired != "") {

                      //If user session expired creates new sessionID
                      sessionID = UUID.randomUUID().toString()
                      logger.info("Creating new Session. {}", sessionID)
                      IMSessionDao.insertSession(sessionID, userID, new java.sql.Timestamp(new java.util.Date().getTime).toString(), reason, channelID)
                    } else {
                      //fetching existing session id for user
                      logger.info("###################Already existing user sessionID.#####################")
                      sessionID = IMSessionDao.getSessionId(userID, channelID)
                    }
                  } else {

                    logger.info("New IMSession Entry")
                    isNewUser = true;
                    sessionID = UUID.randomUUID().toString()
                    IMSessionDao.insertSession(sessionID, userID, new java.sql.Timestamp(new java.util.Date().getTime).toString(), reason, channelID)
                  }
                  event.platformDtls.channelID = Some(channelID)

                  logger.info("sessionID: {}", sessionID)

                  if (event.msgDtls.messageType == "ATCMT") {
                    //if user sent files then the processing will start from below method.

                    NLPDao.isAttchmntForConv(event, sessionID)
                  } else {
                    // if user enters plain text message then processing starts from below method
                    processMessage(event, sessionID, isNewUser)
                  }

                }
                ""
              } catch {
                case e: Exception =>
                  ""
              }
            }
          }
        }
        //(count > 0)
      } catch {

        case e: WakeupException => logger.error("exception caught: {}" + e)
        case e: Exception =>
          logger.error("IO exception {}" + e)
          e.printStackTrace()

      } finally {

        consumer.close()
        logger.info("IMAL-Receive Consumer Closing..............")
        IMSessionDao.insertIMErrorLogs(Some(""), "Read Logs From Consumer Closing", Some(""), Some(0L), Some(0L))

      }
    }

  def processMessage(event: NlpReqCommonObj, sessionID: String, isNewUser: Boolean)(implicit session: JdbcBackend#SessionDef) = {

    try {

      logger.info("Message from platform: {}", event.msgDtls.msgTxtWithoutPunc.trim().toLowerCase())
      NLPDao.processConversation(event, sessionID, isNewUser)

    } catch {

      case e: Exception =>
        NLPSessionDao.nlpCacheDeletion(sessionID)
        val userLang = UserLanguageDao.getUserPreferredLocale(event.platformDtls.userID, event.platformDtls.channelID.getOrElse(0L))
        val defaultErrMsg = ErrorResponseDao.getErrorDescription(SCE_SERVER_DOWN, userLang)
        val nlgObj = new nlgResponseObj(Some(defaultErrMsg), None, None, None, None)
        ActionNlpDao.sendFinalNlg(sessionID, event, nlgObj, None, None)
        KafkaService.sendErrorLogsToKafkaProducer(event.platformDtls.userID, "", defaultErrMsg, "", 0L, 0L)

    }

  }
}

object ReadPlatformMsgFromConsumer {

  def ReadPlatformMsgFromConsumer = new ReadPlatformMsgFromConsumer

}