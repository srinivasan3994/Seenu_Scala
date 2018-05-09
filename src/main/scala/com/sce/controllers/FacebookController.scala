package com.sce.controllers

import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.sce.models._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
//import com.sce.models.Platform
import com.sce.services._
import spray.json._
import spray.json.lenses.JsonLenses._

import scala.collection.JavaConversions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import java.util.{ Collections, Properties }

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.duration.Duration
import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

object FacebookController extends FacebookJsonSupport {

  import StatusCodes._
  val logger = Logging(system, this.getClass)

  val FB_CALLBACK = config.getString("FB_CALLBACK")
  val HTTP_PORT = config.getString("http.port")

  val routes =
    path(FB_CALLBACK) {
      get {
        parameters("hub.verify_token", "hub.challenge") {
          case (token, challenge) =>
            if (token == "token123") {
              complete(challenge)
            } else {
              complete("Error, invalid token")
            }
        }
      } ~
        post {
          logger.info("Facebook webhook posted")
          try {
            entity(as[FacebookResponse]) { data =>
              logger.info("received body:\n{}", data)
              if (data.obj == "page") {
                data.entry.foreach { entryEvent =>
                  logger.info("messagingEvent:\n{}", entryEvent)
                  entryEvent.messaging.foreach { fbMessagingObj =>
                    
                    FacebookService.processMsgToNlp(fbMessagingObj)
                  }
                }
              } else {
                logger.error("invalid content")
              }
              complete(OK)
            }

          } catch {
            case e: Exception =>
              e.printStackTrace()
              complete(OK)
          }

        }
    } ~
      pathPrefix("img") {
        path(Segment) { filename =>
          getFromResource(s"images/$filename")
        }
      } ~
      path("") {
        logger.info("default path")
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Service " + HTTP_PORT + " Started</h1>"))
      }
}