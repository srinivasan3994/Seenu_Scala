package com.sce.services

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{ FormData, HttpMethods, HttpRequest, StatusCodes }
import akka.http.scaladsl.model.headers.{ Authorization, BasicHttpCredentials, OAuth2BearerToken }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.google.inject.{ Inject, Singleton }
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import com.typesafe.config.ConfigFactory
import spray.json._
import com.sce.models.BankNetServiceJsonSupport
import com.sce.models.AccessToken
//import com.sce.apis.banknet._
import scala.concurrent.Future
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.io.Source
import java.io.PrintWriter
import java.io.File
//import com.sce.main.Main.configOut

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import com.sce.dao.ActionNlpDao
import com.sce.models.NlpHttpReqObj
import com.sce.models.NlpJsonSupport
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
object SessionBase extends BankNetServiceJsonSupport with NlpJsonSupport {

  import system.dispatcher

  val logger = Logging(system, this.getClass)

  def getActionOauthToken = {
    try {
      val actionAuthRec = ActionNlpDao.getActionAuthorizationRec
      var reqHeaders = scala.collection.mutable.Map[String, String]()

      for (i <- 0 until actionAuthRec.length) {

        var httpClient = scalaj.http.Http(actionAuthRec(i).AccessTokenUrl.getOrElse(""))
        val nlpReqBodyString = actionAuthRec(i).AccessTokenReqBody.getOrElse("").parseJson.convertTo[NlpHttpReqObj]
        nlpReqBodyString.headers.map(x => reqHeaders += (x.header_key -> x.header_value))
        logger.info("reqHeaders: {}", reqHeaders)
        val rawParameters = nlpReqBodyString.req_body.req_body_params.get
        val formParams = rawParameters.map(x => (x.entitykey, x.entityvalue))
        httpClient = ApiCallExternalService.withFormData(httpClient, Nil, Nil, formParams).headers(reqHeaders.toMap)

        val response = CommonAPICallService.sendHttpRequest(httpClient)
        processAccessToken(response.body, actionAuthRec(i).AAID)

      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def processAccessToken(responseBody: String, aaid: Long) = {
    try {
      val accToken = responseBody.parseJson.convertTo[AccessToken]
      logger.info("accToken: {}", accToken)
      ActionNlpDao.updateAccessToken(accToken.access_token, aaid)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }


 /* def getOauthTokenAccess {
    try {
      val authorization = headers.Authorization(BasicHttpCredentials(USERNAME, PASSWORD))
      val header = headers.Accept(MediaTypes.`application/x-www-form-urlencoded`)

      val data = FormData(Map(
        "grant_type" -> "client_credentials")).toEntity(HttpCharsets.`UTF-8`)

      val result = for {
        response <- http.singleRequest(HttpRequest(
          method = HttpMethods.POST,
          uri = OAUTH_TOKEN_URL,
          headers = List(authorization),
          entity = data))
      } yield {
        response.status match {
          case StatusCodes.OK =>
            for {
              body <- response.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8"))
            } yield {
              logger.debug(body)
              setAccessToken(body.toString())
            }
          case _ =>
            logger.error("returned status {}", response.status.value)
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def setAccessToken(jsonData: String) {
    try {
      val jsonParse = jsonData.parseJson.asJsObject
      val toJson = jsonParse.convertTo[AccessToken]
      val writer = new PrintWriter(new File("./token.txt"))
      writer.write(toJson.access_token)
      writer.close()
      
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }*/

}