package com.sce.services

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.google.inject.{ Inject, Singleton }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import spray.json._
import com.sce.models._
import com.sce.dao._
import com.sce.utils._

import scala.concurrent.Promise
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken, Cookie }
import scala.io.Source
import com.sce.models.NlpActionConsumerObj
import com.jayway.jsonpath.JsonPath
import java.util.concurrent.TimeoutException
import scala.util.{ Failure, Success }
import com.google.inject.name.Named

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm

import java.io.File
import java.nio.file.{ Path, Paths, Files }
import com.sce.models.NLPErrorCodes._
import akka.dispatch.OnSuccess
import akka.dispatch.OnFailure
import com.sce.exception.BCActionFailureException
import akka.event.{ Logging }

object CommonAPICallService extends NLGJsonSupport {

  import system.dispatcher

  val http = Http()
  val logger = Logging(system, this.getClass)
  val ACTION_SEND = config.getString("ACTION_SEND")

  def getActionResponse(httpRequest: scalaj.http.HttpRequest, nlpActionObject: NlpActionConsumerObj) = {

    val futureHttpResponse = sendHttpRequest(httpRequest, nlpActionObject)
    futureHttpResponse.onComplete {

      case Success(value) =>
      
        KafkaService.sendActionMsgToKafkaProducer(ACTION_SEND, nlpActionObject.identityDtls.msgEvent.platformDtls.userID, "", nlpActionObject, value.body)
      case Failure(e) => e match {

        case e1: BCActionFailureException =>
          val replyMsg = ErrorResponseDao.getErrorDescription(ACTION_CONF_EXCEPTION, e1.getMessage)
          EntityProcessingDao.trmnteIntentConvWithMsg(nlpActionObject.identityDtls.sessionID, nlpActionObject.identityDtls.msgEvent, replyMsg)
        case e2: Exception =>

          EntityProcessingDao.errorTerminateIntentConversation(nlpActionObject.identityDtls.sessionID,nlpActionObject.identityDtls.msgEvent.platformDtls.userLang, nlpActionObject.identityDtls.msgEvent)
      }
    }
  }
  
  
  
  def sendHttpRequest(httpRequest: scalaj.http.HttpRequest, nlpActionObject: NlpActionConsumerObj): Future[scalaj.http.HttpResponse[String]] = Future {

      httpRequest.asString
   
  }
  
  def sendHttpRequest(httpRequest: scalaj.http.HttpRequest): scalaj.http.HttpResponse[String] =  {
    try {

     val httpResponse =  httpRequest.asString
     logger.info("httpResponse: {}", httpResponse)
     httpResponse
    } catch {
      case e: Exception =>
        throw new BCActionFailureException(ACTION_CALL_EXCEPTION)
    }
  }

  def sendResponseToKafka(requestJson: NlpActionConsumerObj) {

    val sender = requestJson.identityDtls.msgEvent.platformDtls.userID
    val userLang = requestJson.identityDtls.msgEvent.platformDtls.userLang

    try {
      val res = callBanknetAPI(requestJson.messageDtls.reqURL.getOrElse(""), requestJson.messageDtls.reqBody.getOrElse(""), requestJson.messageDtls.reqMethod.getOrElse("")).onComplete {
        case Success(result) => {
          if (result != null) {

            logger.info("Result Response Json :\n{}", result.prettyPrint)

            //val accessCode = getAccessCode(requestBody)

            val responseParse = result.toJson.toString()
            
            KafkaService.sendActionMsgToKafkaProducer(ACTION_SEND, sender, "", requestJson, responseParse)

          } else {
            EntityProcessingDao.errorTerminateIntentConversation(requestJson.identityDtls.sessionID, userLang, requestJson.identityDtls.msgEvent)

          }
        }
        case Failure(e) => {
          e.printStackTrace
          KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
          EntityProcessingDao.errorTerminateIntentConversation(requestJson.identityDtls.sessionID, userLang, requestJson.identityDtls.msgEvent)

        }
      }
    } catch {

      case e: Exception =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.printStackTrace()
        NLPSessionDao.nlpCacheDeletion(requestJson.identityDtls.sessionID)

    }
  }

  def callBanknetAPI(URL: String, requestBody: String, requestMethod: String): Future[JsValue] = {

    try {
      val returnJs: JsValue = null
      val ACCESS_TOKEN = Source.fromFile("./token.txt").mkString
      val JSESSIONID = Source.fromFile("./session.txt").mkString

      logger.info("JSESSIONID       :\n{}", JSESSIONID)

      val authorization = headers.Authorization(OAuth2BearerToken(ACCESS_TOKEN))
      val cookieHeader = headers.Cookie("JSESSIONID", JSESSIONID)
      val header = headers.Accept(MediaTypes.`application/json`)

      if (requestMethod.toUpperCase() == "POST") {
        val data = requestBody.parseJson

        logger.info("authCode Json :\n{}", data.prettyPrint)

        for {
          request <- Marshal(data).to[RequestEntity]
          response <- http.singleRequest(HttpRequest(
            method = HttpMethods.POST,
            uri = URL,
            headers = List(authorization, cookieHeader),
            entity = request))
          entity <- Unmarshal(response.entity).to[JsValue]
        } yield {
          entity
        }
      } else {
        for {
          response <- http.singleRequest(HttpRequest(
            method = HttpMethods.GET,
            uri = URL,
            headers = List(headers.Accept(MediaTypes.`application/json`), authorization)))
          entity <- Unmarshal(response.entity).to[JsValue]
        } yield {
          entity
        }
      }
    } catch {
      case e: Exception =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        e.printStackTrace()
        null
    }
  }

  def getAccessCode(requestBody: String): String = {
    var accesscode = ""

    try {
      if (requestBody != "") {
        val index = requestBody.indexOf("authCode")
        val endIndex = requestBody.indexOf("}", index)
        accesscode = requestBody.substring(index + "authCode=".length() + 2, endIndex - 1)
      }
    } catch {
      case e: Exception =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        logger.info("Exception       :{}", e.getMessage)
    }
    return accesscode
  }

  def createEntity(filename: String): Future[RequestEntity] = {

    val file = new File(filename)
    require(file.exists())
    val formData =
      Multipart.FormData(
        akka.stream.scaladsl.Source(
          List(
            Multipart.FormData.BodyPart.fromPath("image", MediaTypes.`image/png`, Paths.get(filename)),
            Multipart.FormData.BodyPart.Strict("subject", "Please help me!"),
            Multipart.FormData.BodyPart.Strict("text", "I am not able to upload my path."),
            Multipart.FormData.BodyPart.Strict("category", "suggestion"),
            Multipart.FormData.BodyPart.Strict("reported_object", "path"),
            Multipart.FormData.BodyPart.Strict("reported_object_id", "2601"),
            Multipart.FormData.BodyPart.Strict("version", "1.5.2"))))
    Marshal(formData).to[RequestEntity]
  }

  def createRequest(filename: String, reqUrl: String): Future[HttpRequest] = {
    val authorization = headers.Authorization(OAuth2BearerToken("969cdfc2-70f6-d7e5-cfe3-c17fbca14e7a"))
    for {
      e ← createEntity(filename)
    } yield HttpRequest(HttpMethods.POST, uri = reqUrl, headers = List(headers.Accept(MediaTypes.`multipart/form-data`), authorization), entity = e)
  }

  def multipartClient(fileName: String, requestUrl: String): Future[String] = {

    try {
      val file = new File(fileName)
      val result =
        for {
          req <- createRequest(fileName, requestUrl)
          _ = println(s"Running request, uploading test file of size ${file.length} bytes")
          response ← Http().singleRequest(req)
          responseBodyAsString ← Unmarshal(response.entity).to[String]
        } yield responseBodyAsString

      result
    } catch {

      case e: Exception =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        null
    }
    /*println("result: " + result)

    result.onComplete { res ⇒
      println(s"The result was $res")
      system.shutdown()
    }*/

  }

  /*def getResultResponse(reqParameters: nlpConsumerMessage): String = {

    val ERROR_LOG = config.getString("ERROR_LOG")
    val TIMEOUT_ERROR_LOG = config.getString("TIMEOUT_ERROR_LOG")
    val accessCode = getAccessCode(reqParameters.message.reqBody.getOrElse(""))
    var res_param = ""
    try {
      val res = Await.result(callBanknetAPI(reqParameters.message.reqURL.getOrElse(""), reqParameters.message.reqBody.getOrElse(""), reqParameters.message.reqMethod.getOrElse("")), 60.seconds)
      logger.info("final response :\n{}", res)
      ActionNlpDao.insertActionLogs(reqParameters.action.actionId,
        reqParameters.action.intentID,
        reqParameters.action.entityID,
        reqParameters.message.reqURL.getOrElse(""),
        reqParameters.message.reqBody.getOrElse(""),
        accessCode,
        res.toString())
        if (!res.toString().contains("errorMessage")) {

        res.toString()

      } else {

        return ERROR_LOG
      }
      res.toString()
    } catch {

      case e: Exception =>
        e.printStackTrace()
        ActionNlpDao.insertActionLogs(reqParameters.action.actionId,
          reqParameters.action.intentID,
          reqParameters.action.entityID,
          reqParameters.message.reqURL.getOrElse(""),
          reqParameters.message.reqBody.getOrElse(""),
          accessCode,
          TIMEOUT_ERROR_LOG)

        return TIMEOUT_ERROR_LOG
    }

  }*/

}