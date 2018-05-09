package com.sce.utils

import com.sce.utils.AppUtils.fm
import com.sce.utils.AppUtils.system
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

import scalaj.http.Http
import scalaj.http.HttpOptions
import scalaj.http.HttpResponse

object HttpUtils {

  import system.dispatcher
  val logger = Logging(system, this.getClass)

  def simpleRestClientForWebhook(payload: String, url: String, headers: List[(String, String)]): HttpResponse[String] = {
    try {
      logger.info("payload: {}", payload)
      logger.info("url: {}", url)
      val result = Http(url).postData(payload).headers(headers).asString
      logger.info("result: {}", result)
      result
    } catch {
      case e: Exception =>
        throw new Exception("Opertor Login Exception")
    }
  }
}