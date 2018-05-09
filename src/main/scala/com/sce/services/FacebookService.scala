package com.sce.services

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.sce.models._
import com.google.inject.{ Inject, Singleton }
import com.typesafe.config.Config
//import com.sce.models.{ Item, ItemLinkAction, ItemPostbackAction, UserProfile }
import spray.json._

import scala.concurrent.Future
//import com.sce.main.Main.configOut

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import com.sce.models.NlpMessageDtlsObj
import com.sce.models.PlatFormDtlsObj
import com.sce.models.MessageDtlsObj
import com.sce.models.NlpReqCommonObj
import com.sce.models.NLPCommonAtcmtObj
import com.sce.models.NlpJsonSupport
import com.sce.dao.ChannelDetailsDao
import com.sce.dao.ErrorResponseDao
import com.sce.models.NLPStrings._
import com.sce.dao.UserLanguageDao
import com.sce.dao.IMSessionDao
import com.sce.utils.HttpUtils
import com.sce.models.NLPStrings._
import scala.util.Try
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
object FacebookService extends FacebookJsonSupport with NlpJsonSupport {

  import system.dispatcher

  val http = Http()
  val logger = Logging(system, this.getClass)
  val FB_URL = config.getString("FB_URI")
  val FB_ACCESS_TOKEN = config.getString("FB_ACCESS_TOKEN")
  val IMAL_RECEIVE = config.getString("IMAL_RECEIVE")
  
  val kafkaService = KafkaService
  val channelDetailsDao = ChannelDetailsDao
  val httpUtils = HttpUtils
  
  
  def processMsgToNlp(fbMessagingObj: FacebookMessaging) = {

    try {
      val platformID = channelDetailsDao.getPlatformID(FACEBOOK)
      
      var platfomrDtls = new PlatFormDtlsObj("", "", None, platformID, None, DEFAULT_LANG)

      var msgDtls = new MessageDtlsObj("", "TEXT", Nil, "")

      platfomrDtls.userID = fbMessagingObj.sender.id
      platfomrDtls.receiverID = fbMessagingObj.recipient.id
      if (fbMessagingObj.postback.nonEmpty) {

        msgDtls.messageTxt = fbMessagingObj.postback.get.payload
        msgDtls.msgTxtWithoutPunc = SCSUtils.removePuchuation(fbMessagingObj.postback.get.payload)

      } else if (fbMessagingObj.message.nonEmpty) {

        if (fbMessagingObj.message.get.quick_reply.nonEmpty) {

          msgDtls.messageTxt = fbMessagingObj.message.get.quick_reply.get.payload
          msgDtls.msgTxtWithoutPunc = SCSUtils.removePuchuation(fbMessagingObj.message.get.quick_reply.get.payload)

        } else if (fbMessagingObj.message.get.attachments.nonEmpty) {

          msgDtls.messageType = "ATCMT"
          msgDtls.attachments = atcmtObj(fbMessagingObj.message.get.attachments.get)

        } else if (fbMessagingObj.message.get.text.nonEmpty) {

          msgDtls.messageTxt = fbMessagingObj.message.get.text.get
          msgDtls.msgTxtWithoutPunc = SCSUtils.removePuchuation(fbMessagingObj.message.get.text.get)

        }
      }
      val nlpCommonObj = new NlpReqCommonObj(platfomrDtls, msgDtls)
      val typing_on = "typing_on"
      logger.info("sending typing action for facebook ")
      FacebookService.sendSenderAction(typing_on, nlpCommonObj)
      logger.info("nlpCommonObj: {}", nlpCommonObj)
      kafkaService.sendPlatformMsgToNlpKafkaProducer(IMAL_RECEIVE, nlpCommonObj.toJson.toString())
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

  }

  def atcmtObj(fbAtcmts: Array[FacebookAttachment]): List[NLPCommonAtcmtObj] = {
    var atcmts: List[NLPCommonAtcmtObj] = Nil

    fbAtcmts.foreach(x => atcmts = new NLPCommonAtcmtObj( x.payload.url, x.`type`) :: atcmts)

    atcmts
  }
  
  def processNlgMsgForFacebook(nlgResponseMsg: IMSendConsumer, channelRecord: TChannel) = {

    Try {
    val fbUserID = FacebookRecipient(nlgResponseMsg.msgEvent.platformDtls.userID)
    var payload = ""
    val nlgMessage = nlgResponseMsg.nlgRespObj
    val url = channelRecord.WebhookUrl + FB_URL_TOKEN_TXT + channelRecord.AccessToken.getOrElse("")

    if (nlgMessage.simpleMessage != None) {
      
     val simpleRplyMsg = nlgMessage.simpleMessage.get.parseJson.convertTo[FacebookSimpleMsg]
     payload = FacebookSimpleMsgTemplate(fbUserID, simpleRplyMsg).toJson.toString()

    } else if (nlgMessage.confirmationMessage != None) {

      val confrmRplyMsg = nlgMessage.confirmationMessage.get.parseJson.convertTo[FacebookQuickReplyMessage]
      payload = FacebookQuickReplyTemplate(fbUserID, confrmRplyMsg).toJson.toString()

    } else if (nlgMessage.choiceListing != None) {

      val lstMsg = nlgMessage.choiceListing.get.parseJson.convertTo[FacebookBaseAttachment]
      payload = FacebookListReplyTemplate(fbUserID, lstMsg).toJson.toString()

    } else if (nlgMessage.imageChoiceListing != None) {
      
      val imgLstMsg = nlgMessage.imageChoiceListing.get.parseJson.convertTo[FacebookImageListBaseAttachment]
      payload = FacebookImageListReplyTemplate(fbUserID, imgLstMsg).toJson.toString()
      
    } else if (nlgMessage.intentChoices != None) {
      
      val choiceRplyMsg = nlgMessage.intentChoices.get.parseJson.convertTo[FacebookQuickReplyMessage]
      payload = FacebookQuickReplyTemplate(fbUserID, choiceRplyMsg).toJson.toString()
      
    }
    
    val reqHeaders = List(("Content-Type", "application/json"),("Charset", "UTF-8"))
    
    httpUtils.simpleRestClientForWebhook(payload, url, reqHeaders)
    }
  }
  
  def sendSenderAction( text: String, msgEvent: NlpReqCommonObj) = {
    logger.debug("processing event")

    logger.info("sending text message: [{}] to sender [{}]", text, msgEvent.platformDtls.userID)
    import Builder._

    val payload = new FacebookSenderAction(new FacebookRecipient(msgEvent.platformDtls.userID), text).toJson.toString()
    logger.debug("sending payload:\n{}", payload)
    val channalRec = channelDetailsDao.getChannelRecord(msgEvent.platformDtls.platformID)
    val url = channalRec.WebhookUrl + FB_URL_TOKEN_TXT + channalRec.AccessToken.getOrElse("")
    val reqHeaders = List(("Content-Type", "application/json"),("Charset", "UTF-8"))
    httpUtils.simpleRestClientForWebhook(payload, url, reqHeaders)
    
   
  }

  /*def sendTextMessage(sender: String, text: String) = {
    logger.debug("processing event")

    logger.info("sending text message: [{}] to sender [{}]", text, sender)
    import Builder._

    val payload = messageElement forSender sender withText text build ()

    logger.debug("sending payload:\n{}", payload.toJson.prettyPrint)
    for {
      request <- Marshal(payload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }

  def sendSenderAction(sender: String, text: String) = {
    logger.debug("processing event")

    logger.info("sending text message: [{}] to sender [{}]", text, sender)
    import Builder._

    val payload = new FacebookSenderAction(new FacebookRecipient(sender), text)

    logger.debug("sending payload:\n{}", payload.toJson.prettyPrint)
    for {
      request <- Marshal(payload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }

  def sendQuickReply(sender: String, text: String, fbPayload: FacebookQuickReplyTemplate): Future[SendResponse] = {
    logger.info("sending quick reply to sender [{}]", sender)
    import Builder._
    
    logger.debug("sending payload:\n{}", fbPayload.toJson.prettyPrint)
    for {
      request <- Marshal(fbPayload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }

  def sendIntentChoiceReply(payload: FacebookQuickReplyTemplate): Future[SendResponse] = {
    logger.debug("sending payload:\n{}", payload.toJson.prettyPrint)
    for {
      request <- Marshal(payload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }

  def sendFacebookListTemplate(payload: FacebookListReplyTemplate): Future[SendResponse] = {
    logger.info("sending quick reply to sender [{}]", payload)

    logger.debug("sending payload:\n{}", payload.toJson.prettyPrint)
    for {
      request <- Marshal(payload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }

  def sendFacebookImageListTemplate(payload: FacebookImageListReplyTemplate): Future[SendResponse] = {
    logger.info("sending quick reply to sender [{}]", payload)

    logger.debug("sending payload:\n{}", payload.toJson.prettyPrint)
    for {
      request <- Marshal(payload).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = FB_URL + "access_token=" + FB_ACCESS_TOKEN,
        headers = List(headers.Accept(MediaTypes.`application/json`)),
        entity = request))
      entity <- Unmarshal(response.entity).to[FacebookAttachmentReuseResponse]
    } yield SendResponse(entity.messageId)
  }*/

  
  
  
}

