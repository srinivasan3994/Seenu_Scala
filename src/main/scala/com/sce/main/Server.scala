package com.sce.main

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.google.inject.Guice
import akka.http.scaladsl.server.{ MalformedRequestContentRejection, RejectionHandler }
import akka.stream.ActorMaterializer
import com.sce.controllers._
//import com.sce.modules.config.ConfigModule
//import com.sce.modules.conversation.ConversationModule
//import com.sce.modules.logging.LoggingModule
import net.codingwell.scalaguice.InjectorExtensions._
import akka.http.scaladsl.server.Directives
import com.sce.services._
import com.sce.services.consumers.ReadPlatformMsgFromConsumer
import com.sce.services.consumers.ReadNlpMsgFromConsumer
import com.sce.services.consumers.ReadNlgMsgFromConsumer
import com.sce.services.consumers.ReadActionMsgFromConsumer
import com.sce.dao._
import scala.io.StdIn
import akka.actor.Props
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import com.sce.utils.AppConf._
import com.sce.utils.AppUtils._

import java.io.InputStream
import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }

object Main extends App with CorsSupport {

  final val ENVIRONMENT = "PROD"

  /*val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystore: InputStream = getClass.getClassLoader.getResourceAsStream("wildcard-s4m.ae.pfx")
  require(keystore != null, "Keystore required!")
  val password: Array[Char] = "".toCharArray // do not store passwords in code, read them from somewhere safe!
  ks.load(keystore, password)
  val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)
  val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  tmf.init(ks)
  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)*/

  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")

  system.scheduler.schedule(0 seconds, 20 minutes)(SessionBase.getActionOauthToken)
  system.scheduler.schedule(0 seconds, 1 minutes)(IMSessionDao.IsEndSessionTimeOut)

  val cunsumerBase = ReadPlatformMsgFromConsumer.ReadPlatformMsgFromConsumer
  val consumerResponse = ReadNlgMsgFromConsumer.ReadNlgMsgFromConsumer
  val consumerNlp = ReadNlpMsgFromConsumer.ReadNlpMsgFromConsumer
  val actionSendConsumer = ReadActionMsgFromConsumer.ReadActionMsgFromConsumer

  new Thread(cunsumerBase).start()
  new Thread(consumerResponse).start()
  new Thread(consumerNlp).start()
  new Thread(actionSendConsumer).start()

  val routes = FacebookController.routes ~
    UserRegistrationController.routes ~
    PuristChatController.routes

  val bindingFuture = Http().bindAndHandle(corsHandler(routes), interface, port /*,connectionContext = https*/ )

  StdIn.readLine()

  bindingFuture.flatMap(_.unbind())
  system.terminate()

}
