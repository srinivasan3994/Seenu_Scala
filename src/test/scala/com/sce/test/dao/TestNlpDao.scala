package com.sce.test.dao

import org.scalatest._
import java.sql.Date
//import java.util._
import com.google.inject.Guice

import com.typesafe.config.Config
import net.codingwell.scalaguice.InjectorExtensions._
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.sce.dao.NLPDao
//import com.sce.services.Consumer
import spray.json._
import com.sce.utils.JsonPathUtils
import com.sce.services.KafkaService
import com.sce.services.CommonAPICallService
import com.sce.utils.ScriptEngineProcessor
import com.sce.models.NlpReqCommonObj
import com.sce.models.NlpJsonSupport
import com.sce.models.NLGJsonSupport
import com.sce.services.consumers.ReadPlatformMsgFromConsumer
import com.sce.utils.HttpUtils
import com.sce.dao.WorkflowDao
import com.sce.services.SessionBase
import com.sce.services.ApiCallExternalService
import com.sce.dao.IntentIdentificationDao

class TestNlpDao extends FlatSpec with NLGJsonSupport with NlpJsonSupport {

  /*val injector = Guice.createInjector(
    new ConfigModule(),
    new LoggingModule(),
    new AkkaModule(),
    new ConversationModule()
  )*/
  /*val consumerbase = injector.instance[Consumer]
   val flowchartProcess = injector.instance[NLPSessionDao]
   val logger = injector.instance[LoggingAdapter]
   val userMappingDao = injector.instance[UserMappingDao]
   val jsonPathUtils = injector.instance[JsonPathUtils]
   val polish = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm:ss")
   val now = java.time.LocalDateTime.now().format(polish)
   val actionNlp = injector.instance[ActionNlpDao]
   */
  val message = "10"
  //val jsonPathUtils = injector.instance[JsonPathUtils]
  "Test Audit Logs" should
    "Insert Logs Quries Test" in {

      val event = """{	"sender": {		"id": "1092945050811598"	},	"recipient": {		"id": "1273566346065018"	},	"timestamp": 1490682491649,	"message": {		"mid": "mid.$cAATcu8CB-HFhRN77AVbE5m5Y0iKS",		"seq": 3430,	
      	"text": "akhil"	}}""".asJson //PPPPPPPPPPPPPPPPPPPPPPPPPP

      println("-----------------------")

      // ScriptEngineProcessor.evlutSimplDiamndExpr("(10+20)>29")

      val msgEvent = """{"platformDtls":{"userID":"1267305460038370","receiverID":"324816377959097","platformID":1,"userLang":"en"},"msgDtls":{"messageTxt":"hi","messageType":"TEXT","attachments":[],"msgTxtWithoutPunc":"hi"}}""".parseJson.convertTo[NlpReqCommonObj]

      val actionResponse = """{"balance":47916.0}"""
      //JsonPathUtils.getActionValueFromJson(actionResponse, "$.balance")
      
      val atcmtEvent = """{"platformDtls":{"userID":"1645669245517206","receiverID":"1208861125911410","platformID":1,"userLang":"en"},"msgDtls":{"messageTxt":"","messageType":"ATCMT","attachments":[{"atcmtContent":"image","atcmtType":"https://scontent-ort2-1.xx.fbcdn.net/v/t1.15752-9/31154140_412021989222202_7148749304985812992_n.jpg?_nc_cat=0&_nc_ad=z-m&_nc_cid=0&oh=fb881fde4d6b455f083898902aba184f&oe=5B58E192"}],"msgTxtWithoutPunc":""}}""".parseJson.convertTo[NlpReqCommonObj]
      
   //    NLPDao.isAttchmntForConv(atcmtEvent, "ca434148-4e90-4cd2-9458-4515f471502d")
      
      
     // ApiCallExternalService.multipartReqBuilder
    /*  for(i <- 0 until 200){
      println(atcmtEvent, msgEvent)
      }*/
      
      val fillerKwdsInUserMsg = IntentIdentificationDao.findFillerKeywordsInUserMsg("hi dfdf", Set.empty)
      println("is filler keywords found: {}", fillerKwdsInUserMsg)
      //  WorkflowDao.sendMessageToUserFrmWrkflw("4ea1e559-916f-4461-9db2-4f42b65f57ab", msgEvent, 32, 5064, 176)
      // SessionBase.getBackendOauthToken
       //NLPDao.processConversation(msgEvent: NlpReqCommonObj, "KBJH-J098ut_on8fo4-443", false)
      //val latestEvent = """{"platformDtls":{"userID":"1877272202293613","receiverID":"374230289690031","platformID":1,"userLang":"en"},"msgDtls":{"messageTxt":"Hi","messageType":"TEXT","attachments":[],"msgTxtWithoutPunc":"hi"}}""".parseJson.convertTo[NlpReqCommonObj]

      // WorkflowDao.se
      //WorkflowDao.sendMessageToUserFrmWrkflw(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong, wrkflwSeqItem.WorkFlowSeqID)
      //new ReadPlatformMsgFromConsumer().processMessage(latestEvent, "50c10962-d3af-4615-9a5a-83e1e571b536", false)

      //val callFBEvent = """{"recipient":{"id":"1877272202293613"},"message":{"text":"Hi, How may help you"}}"""
      // val url = """https://graph.facebook.com/v2.8/me/messages?access_token=EAAEfvcbaaMQBAKjjAgSYYLza4cQMbpB6yeriMRj4UpDZAmH1Vth4AZC1TJM83MqAc3tY7MLOn1odKPCRx4F3Anz0IcWFI4rHqBEmkgZBq5qISz62EZCcZArfskTsStHBCA85UHIjimKbrNaK3sOBGARPfhA2w0LQKcO8ZAvK4IXQZDZD"""

      // val typingEvent = """{"recipient":{"id":"1877272202293613"},"sender_action":"typing_on"}"""

      //val reqHeaders = List(("Content-Type", "application/json"),("Charset", "UTF-8"))

      // HttpUtils.simpleRestClientForWebhook(typingEvent, url, reqHeaders)

      //NLPDao.processConversation("1273566346065018", "1273566346065018", "yes", "96667ba3-b0ee-4a24-b07f-6f15fd5c6f57",true)

      //  EntityProcessingDao.getNextElementPointer(6,1);

      //IntentIdentificationDao.intentIdentification("5d2d6846-52c7-4e65-b075-958f2ba3cf57", "transfer", "mid.$cAATcu8CB-HFhRN77AVbE5m5Y0iKS", "1273566346065018", true, true)
      //EntityProcessingDao.parsingRespForEntity(".٦٧.٦٧", "OUIH", "en")
      // consumerbase.converJson(event)

      // flowchartProcess.createFlowChartSession(6L,2L,"5d2d6846-52c7-4e65-b075-958f2ba3cf57",1L)
      // NLPDao.testRegex("Hi?")

      //NLPDao.parseUserMessage("1092945050811598", "1273566346065018", "+971521234567", "1490682491699", "5d2d6846-52c7-4e65-b075-958f2ba3cf57")
      //  NLPDao.parseUserMessage("1092945050811598", "1273566346065018", "transfer", "1490682491699", "5d2d6846-52c7-4e65-b075-958f2ba3cf57")
      //NLPDao.parseUserMessage("1092945050811598", "1273566346065018", "transfer نقل", "1490682491699", "5d2d6846-52c7-4e65-b075-958f2ba3cf57")
      // NLPDao.parseUserMessage("1092945050811598", "1273566346065018", "نقل توازن", "1490682491699", "5d2d6846-52c7-4e65-b075-958f2ba3cf57")
      //CommonAPICallService.callBanknetAPI("http://10.10.10.212:7001/SCS_UPDATES/api/balance1", """{"authCode": "K+u0K8R52vMZErhD4Yt4h50nvbFQA0QMOrUOE2PcMgw="}""", "POST")
      //ActionNlpDao.getConfirmationRec(10, "en")
      //KafkaService.sendPlatformMsgToKafkaProducer("akhil","akhil")
      //بيتزا
      //NLPSessionDao.deleteSession("5d2d6846-52c7-4e65-b075-958f2ba3cf57")
      //NLPDao.downloadFile("""https://cdn.fbsbx.com/v/t59.2708-21/25415104_811063522421513_7350063626264248320_n.pdf/POWERSECTION1.pdf?oh=6bec44321715713ebb1db8c747272948&oe=5A3C8B66""","""C:/SCS-JAR/userattachments/1457451467677315_5984_0.pdf""")
      //صغير
      //اثنان
      //١٢٣
      // KafkaService.sendPlatformMsgToKafkaProducer("my", "message")

      /*val resp = actionNlp.isActionCalledInInterval("6545562214785445", 2L, "")

     println("Response: "+ resp)*/
      // jsonPathUtils.checkResponse(event.toString(),"#$.sender.id#")

      // userMappingDao.getAccessCode("1092945050811598")
      // IMALSessionDao.IsEndSessionTimeOut("""2017-11-14T10:12:44.089+0400""")

      //val dfs = "౫౬౦౧౨౩౪౫౬౭౮".toCharArray().map(x => UserLanguageDao.replaceNumericals(x,""))
      /*val dfs = "൦൧൨൩൪൫൬൭൮൯".toCharArray().map(x => UserLanguageDao.replaceNumericals(x,""))

   val fsdf = String.valueOf(dfs)

    println("fsdf: "+fsdf)*/

    }

}

