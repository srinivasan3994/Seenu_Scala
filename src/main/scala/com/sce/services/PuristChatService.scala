package com.sce.services

import scala.util.Try

import com.sce.models.MessageDtlsObj
import com.sce.models.NlpJsonSupport
import com.sce.models.NlpReqCommonObj
import com.sce.models.PlatFormDtlsObj
import com.sce.models.PuristJsonSupport
import com.sce.models.PuristReqModelObj
import com.sce.utils.AppConf.config

import spray.json.pimpAny
import com.sce.dao.ChannelDetailsDao
import com.sce.models.NLPStrings._
import com.sce.models.IMSendConsumer
import com.sce.models.TChannel
import com.sce.models.nlpPuristChatMessage

import com.sce.utils.AppUtils.fm
import com.sce.utils.AppUtils.system
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import com.sce.utils.HttpUtils
import spray.json._
import scala.util.Success
import scala.util.Failure
import com.sce.models.LoginAccessToken
import com.sce.utils.AppConf.SCSUtils

object PuristChatService extends PuristJsonSupport with NlpJsonSupport {

  val logger = Logging(system, this.getClass)
  val IMAL_RECEIVE = config.getString("IMAL_RECEIVE")
  def processMsgToNlp(puristMsgObj: PuristReqModelObj) = {

    try {

      val platformID = ChannelDetailsDao.getPlatformID(PURIST)
      val extConvID = puristMsgObj.conversation_id
      val supportCategoryID = puristMsgObj.support_catagory_id
      var platfomrDtls = new PlatFormDtlsObj(puristMsgObj.end_customer_id.get.toString(), supportCategoryID.getOrElse(0L).toString(), extConvID.flatMap(x => Some(x.toString())), platformID, None, DEFAULT_LANG)
      if (puristMsgObj.body.nonEmpty) {
        
        var msgDtls = new MessageDtlsObj(puristMsgObj.body.get, "TEXT", Nil, "")
        msgDtls.msgTxtWithoutPunc = SCSUtils.removePuchuation(puristMsgObj.body.get)

        val nlpCommonObj = new NlpReqCommonObj(platfomrDtls, msgDtls)
        KafkaService.sendPlatformMsgToNlpKafkaProducer(IMAL_RECEIVE, nlpCommonObj.toJson.toString())
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def processNlgMsgForPurist(nlgResponseMsg: IMSendConsumer, channelRecord: TChannel) = {

    Try {
      var message = ""
      val nlgMessage = nlgResponseMsg.nlgRespObj
      val puristConvID = nlgResponseMsg.msgEvent.platformDtls.ExtnlConvID.getOrElse("")
      val url = channelRecord.WebhookUrl.replace(PURIST_CONVID_URL_TEXT, puristConvID) //http://api.puristchat.com/admin/v1/conversations/{conversationID}/messages/

      if (nlgMessage.simpleMessage != None) {

        message = nlgMessage.simpleMessage.get

      } else if (nlgMessage.confirmationMessage != None) {

        message = nlgMessage.confirmationMessage.get

      } else if (nlgMessage.choiceListing != None) {

        message = nlgMessage.choiceListing.get

      } else if (nlgMessage.imageChoiceListing != None) {

        message = nlgMessage.imageChoiceListing.get

      } else if (nlgMessage.intentChoices != None) {

        message = nlgMessage.intentChoices.get

      }

      val payload = new nlpPuristChatMessage(message).toJson.toString();

      val reqHeaders = List(("Content-Type", "application/json"), ("Charset", "UTF-8"), ("Authorization", "Bearer " + channelRecord.AccessToken.getOrElse("")))

      val puristWebHookResponse = HttpUtils.simpleRestClientForWebhook(payload, url, reqHeaders)
      if (puristWebHookResponse.code == 500 || puristWebHookResponse.code == 401) {
        val token = puristOpertorLogin(channelRecord.ChannelID)
        logger.info("token: {}", token)
        token match {

          case Success(value) =>

            val reqHeaders = List(("Content-Type", "application/json"), ("Charset", "UTF-8"), ("Authorization", "Bearer " + value))
            val puristWebHookResponse = HttpUtils.simpleRestClientForWebhook(payload, url, reqHeaders)
          case Failure(e) =>

            e.printStackTrace()
        }
      }
    }
  }

  def puristOpertorLogin(channelID: Long):Try[String] = {

    Try {

      val operatorLoginUrl = config.getString("TOKEN_URL")
      val operatorUserName = config.getString("OPERATOR_USERNAME")
      val operatorPassword = config.getString("OPERATOR_PASSWORD")
      val reqHeaders = List(("Content-Type", "application/json"))
      val reqBody = OP_LOGIN_REQ_BODY.replace("{username}", operatorUserName).replace("{password}", operatorPassword)
      val response = HttpUtils.simpleRestClientForWebhook(reqBody, operatorLoginUrl, reqHeaders)
      val tokenObj = response.body.parseJson.convertTo[LoginAccessToken]
      logger.info("PuristChat AccessToken: {}",  tokenObj.access_token)
      ChannelDetailsDao.updatePuristAccessToken(tokenObj.access_token, channelID)
      tokenObj.access_token
    }
  }


 

}