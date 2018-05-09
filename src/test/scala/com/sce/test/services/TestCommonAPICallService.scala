/*package com.sce.test.services

import org.scalatest._
import com.sce.models._
import java.util._
import com.google.inject.Guice
import com.sce.modules.akkaguice.{AkkaModule, GuiceAkkaExtension}
import com.sce.modules.config.ConfigModule
import com.sce.modules.conversation.ConversationModule
import com.typesafe.config.Config
import net.codingwell.scalaguice.InjectorExtensions._
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.sce.services._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import scala.concurrent.{Await,Future}
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class TestCommonAPICallService extends FlatSpec{
  

   val injector = Guice.createInjector(
    new ConfigModule(),
    new LoggingModule(),
    new AkkaModule(),
    new ConversationModule()
  )
  
  implicit val system = injector.instance[ActorSystem]
  implicit val fm = ActorMaterializer()
  import system.dispatcher
  
  val http = Http()
  
   val testApiCall = injector.instance[CommonAPICallService]
   val logger = injector.instance[LoggingAdapter]
   val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
   val now = java.time.LocalDateTime.now().format(polish)
  
  "Test Api Call External Service" should
    "API Call Service" in{
     
     
     
     val res = testApiCall.callBanknetAPI("http://10.10.10.212:7001/SCS/api/mockResponse", "", "GET").onComplete{
       
       case Success(result) => {
         logger.info("Result Response Json :\n{}", result.prettyPrint)
       }
        case Failure(e) => {
        e.printStackTrace
       
      }
       
       
       
     }
     PostgresDriver
topic: Consumer-NLP
recipient: 1273566346065018
text:  nlpResponseObj(,Some({"authCode": "Jr2MCNiBTkCCd0bxJAgT+a5d4MYyWkET1J4eYc
/e/cM="}),Some(http://10.10.10.212:7001/SCS/api/balance),None,None,Some(POST))
nlpAction: nlpAction(1092945050811598,74864829-9e66-4a14-a53e-b267a0d385cf,12735
66346065018,6,2,Some(2),Some(1),Some(-8),true)
replyMessage: Kindly enter the amount (10 AED)
finalresponseobj: nlpResponseObj(10,None,None,Some(RPLYMSG),None,None)
app:  app1
nlpAction: nlpAction(1092945050811598,74864829-9e66-4a14-a53e-b267a0d385cf,12735
66346065018,0,0,None,None,None,false)
     
     val reqBody = """{"authCode": "Jr2MCNiBTkCCd0bxJAgT+a5d4MYyWkET1J4eYc/e/cM="}"""
     
     val message = new nlpResponseObj("",Some(reqBody),Some("http://10.10.10.212:7001/SCS/api/balance"), None, None,Some("POST"))
     
     val action = new nlpAction("1092945050811598", "74864829-9e66-4a14-a53e-b267a0d385cf", "1273566346065018", 6,2, 
         Some(2), Some(1), Some(-12),true)

     val requestJson =new nlpConsumerMessage("1273566346065018",message: nlpResponseObj,"74864829-9e66-4a14-a53e-b267a0d385cf","1092945050811598",action)
     
     
     
     
     testApiCall.sendResponseToKafka(requestJson)
     
    
     
     
     
     val response:Future[HttpResponse]=http.singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = s"http://localhost:3000/api?cmd=transfer&amount=10&phone=+9712434234"
        ))
        
        val result = response.map{
       case HttpResponse(StatusCodes.OK,headers,entity,_)=>
         Unmarshal(entity).to[String]
         
       case x =>s"Unexpected status code ${x.status}"
     }
     
     //val result = testApiCall.externalServiceApiCallPost(consumerMessage)
        
     //logger.info("Result      :\n{}",result)
     //logger.info("Result      :\n{}",Await.result(result, 10.seconds))
     
       
    
  }
  
}*/