package com.sce.controllers

import com.sce.models.PuristJsonSupport
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.Directives._segmentStringToPathMatcher
import akka.http.scaladsl.server.Directives.as
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.entity
import akka.http.scaladsl.server.Directives.get
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.post
import spray.json.JsObject
import com.sce.models.PuristReqModelObj
import com.sce.services.PuristChatService
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

object PuristChatController extends PuristJsonSupport {

  val logger = Logging(system, this.getClass)
  val routes =
    pathPrefix("puristchat") {
      path("getUser") {
        get {
          logger.info("default path")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Purist Chat Service</h1>"))
        }
      } ~
        path("receive") {
          post {

            entity(as[PuristReqModelObj]) { data =>
              logger.info("puristEvent: {}", data)
              PuristChatService.processMsgToNlp(data)
              complete("")
            }

          }
        }
    }
}
    
    
 /*   path("puristwebhook") {
    post {
      try {
        entity(as[PuristReqModelObj]) { data =>
        logger.info("puristEvent: {}", data)
          //PuristChatService.processMsgToNlp(data)
          complete(OK)
        }
      } catch {
        case e: Exception => e.printStackTrace()
        complete(OK)
      }
      
    }
  }
}*/