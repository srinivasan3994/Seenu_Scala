package com.sce.controllers

import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import spray.json._
import spray.json.lenses.JsonLenses._
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import java.util.{Collections, Properties}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.duration.Duration
import com.sce.models._
import com.sce.dao._
import com.sce.services._
import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

                                   
object UserRegistrationController extends NlpJsonSupport{

  val logger = Logging(system, this.getClass)
  val routes =
    pathPrefix("scs") {
    path("testUser"){
       get{
            logger.info("default path")
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>User Registration Service</h1>"))
        }
      }~
      path("callback"){
       post{
          logger.info("User Webhook Posted")
          entity(as[JsValue]){data=>
            logger.info("User received body:\n{}",data.prettyPrint)
            
            val parseMsg = data.convertTo[nlgResponseMessage]
            
            //val response = NLPDao.userReplayMessage(parseMsg.message, parseMsg.user_sessionid.getOrElse(""))
            //logger.info("Transfer response Text  :\n{}",response)
            complete("")
          }
        }
      }~
      path("user"){
       post{
          logger.info("User Webhook Posted")
          entity(as[JsValue]){data=>
            logger.info("User received body:\n{}",data.prettyPrint)
            val response =""//restAPIService.getJsonRequest(data)
            logger.info("Balance response Text  :\n{}",response)
            complete(response)
          }
        }
      }~
      path("checkKeyword"){
       post{
          logger.info("checking keywords for another intent")
          entity(as[JsValue]){data=>
            
            val chkIntentObj = data.convertTo[intentChkObj]
            val response =IntentIdentificationDao.checkForKeywords(chkIntentObj)
            logger.info("Is keywords for another Intent  :\n{}",response)
            complete(response)
          }
        }
      }
    }
}