/*package com.sce.test.dao

import org.scalatest._
import java.sql.Date
import java.util._
import com.google.inject.Guice
import com.sce.modules.akkaguice.{AkkaModule, GuiceAkkaExtension}
import com.sce.modules.config.ConfigModule
import com.sce.modules.conversation.ConversationModule
import com.typesafe.config.Config
import net.codingwell.scalaguice.InjectorExtensions._
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.sce.dao._
import org.joda.time.DateTime

class TestMsSqlDao extends FlatSpec{
  
   val injector = Guice.createInjector(
    new ConfigModule(),
    new LoggingModule(),
    new AkkaModule(),
    new ConversationModule()
  )
   val imLogsDao = injector.instance[IMALLogsDao]
   val imSessionDao = injector.instance[IMALSessionDao]
   val actionNlpDao = injector.instance[ActionNlpDao]
   val userMappingDao = injector.instance[UserMappingDao]
   val errorResponseDao = injector.instance[ErrorResponseDao]
   val logger = injector.instance[LoggingAdapter]
   val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
   val now = java.time.LocalDateTime.now().format(polish)
  
  "Test Audit Logs" should
    "Insert Logs Quries Test" in{
    
     val errorcode = "UNAUTHORIZED"
     
     //val parsedSentence = errorResponseDao.getErrorDescription(errorcode)
    // println("parsedSentence",parsedSentence)
    //imLogsDao.insertIMALLogs(UUID.randomUUID().toString(),  "Hello Test",  "Message receive from Facebook Messanger")
    //imSessionDao.insertSession("test-oracle", "5551092945050811598", DateTime.now(), Some(DateTime.now()), "Test")
    //val accessCode = userMappingDao.getAccessCodeFirstOption
    //println("accessCode       :",accessCode)
  }
}*/