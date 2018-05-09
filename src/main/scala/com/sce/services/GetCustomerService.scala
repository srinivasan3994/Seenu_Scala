/*package com.sce.services

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
//import com.sce.apis.banknet._
import com.sce.dao._

import scala.concurrent.Future
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken, Cookie }
import java.io.PrintWriter
import java.io.File
import scala.io.Source
//import com.sce.main.Main.configOut

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.{ Logging }
@Singleton
class GetCustomerService @Inject()(config: Config,
                                logger: LoggingAdapter,
                                userMappingDao: UserMappingDao,
                                implicit val system: ActorSystem,
                                implicit val fm: Materializer)

object GetCustomerService extends NLGJsonSupport with BankNetServiceJsonSupport {

  import system.dispatcher
  val logger = Logging(system, this.getClass)
  val http = Http()

  def getCustomer: String = {

    val ACCESS_TOKEN = Source.fromFile("./token.txt").mkString

    val GET_CUSTOMER_URL = config.getString("getCustomer_url")

    //val AUTHCODE = config.getString("authCode")

    val authorization = headers.Authorization(OAuth2BearerToken(ACCESS_TOKEN))
    val header = headers.Accept(MediaTypes.`application/json`)

    val accessCode = UserMappingDao.getAccessCodeFirstOption
    val authCode = new AuthCode(accessCode)
    val data = authCode.toJson

    logger.info("access_token    :        {}      ", ACCESS_TOKEN)

    val result = for {
      request <- Marshal(data).to[RequestEntity]
      response <- http.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = GET_CUSTOMER_URL,
        headers = List(authorization),
        entity = request))
      entity <- Unmarshal(response.entity).to[JsObject]
    } yield {

      response.status match {

        case StatusCodes.OK =>
          parseHeader(response.headers.toString())

        case _ =>
          logger.error("returned status {}", response.status.value)
      }
    }

    //logger.info("Result   {}:",Await.result(result, 30.seconds))
    logger.info("Result   {}:", result)

    return result.toString()
  }

  def parseHeader(headers: String) {

    val index = headers.indexOf("JSESSIONID=")

    val endIndex = headers.indexOf(";", index)

    val sessionId = headers.substring(index + "JSESSIONID=".length(), endIndex)

    logger.info("headers    :\n{}", headers)

    logger.info("sessionId    :\n{}", sessionId)
    //System.setProperty("JSESSIONID", sessionId)
    val writer = new PrintWriter(new File("./session.txt"))
    writer.write(sessionId)
    writer.close()

  }

}*/