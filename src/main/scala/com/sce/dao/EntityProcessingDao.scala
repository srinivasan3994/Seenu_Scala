
package com.sce.dao

import com.sce.services._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.sce.models._
import spray.json._
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Random
import scala.util.control.Breaks._
import com.sce.models.FacebookQuickReply
import com.jayway.jsonpath.JsonPath
import akka.event.LoggingAdapter
import com.sce.models.EntityTypeCodes._
import scala.util.control.Breaks
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.models.NLPErrorCodes._
import com.sce.models.TSessionRecord
import com.sce.models.NlpJsonSupport
import com.sce.utils.AppConf._
import com.sce.dao._
import com.sce.models.NLPStrings._
import com.sce.models.NLPRegexs._
import com.sce.models.NLPRegexs._
import com.sce.models.TConversationCache
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend
import  com.sce.exception.UserMsgProcessingException

object EntityProcessingDao extends DomainComponent with Profile with NlpJsonSupport {
  
  val logger = Logging(system, this.getClass)
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val knowledgeUnit = KnowledgeUnit.KnowledgeUnit
  val Intent = IntentTable.IntentTable
  val Keyword = KeywordTable.KeywordTable
  val entityTbl = Entity.Entity
  val entityQuestions = EntityQuestions.EntityQuestions
  val entityType = EntityType.EntityType
  val regexTable = Regex.Regex
  val response = Response.Response
  val actionTbl = ActionTable.ActionTable
  val actionLog = ActionLog.ActionLog
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val messageTbl = Message.Message
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer

  val randomizer = new Random();
  val CONSUMER_NLP = config.getString("CONSUMER_NLP")
  
  val errorResponseDao = ErrorResponseDao
  val nlpSessionDao = NLPSessionDao
  val actionNlpDao = ActionNlpDao 
  val userLanguageDao = UserLanguageDao
  val kafkaService = KafkaService
  val jsonPathUtils = JsonPathUtils
   

  def errorTerminateIntentConversation(sessionID: String, userLang: String, msgEvent: NlpReqCommonObj) = {

    logger.info("----------------------------Terminating converstaion-----------------------")
    nlpSessionDao.nlpCacheDeletion(sessionID)
    val defaultErrMsg = errorResponseDao.getErrorDescription(SCE_INTENT_NOT_FOUND, userLang)
    val nlgObj = new nlgResponseObj(Some(defaultErrMsg), None, None, None, None)
    actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)
  }

  def trmnteIntentConvWithMsg(sessionID: String, msgEvent: NlpReqCommonObj, message: String) = {
  
    logger.info("Terminate with message: {}", message)
    nlpSessionDao.nlpCacheDeletion(sessionID)
    val nlgObj = new nlgResponseObj(Some(message), None, None, None, None)
    actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)
  }
  
  def getMessageByID(messageID: Long, userLang: String): String = db withSession
    {
      implicit session =>
        import profile.simple._
        val randomizer = new Random();
        try {

          var intentResponses = response.filter { x => x.MessageID === messageID && x.LocaleCode === userLang }.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          if (intentResponses.isEmpty) {
            intentResponses = response.filter { x => x.MessageID === messageID && x.LocaleCode === DEFAULT_LANG }.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          }
          if (intentResponses.nonEmpty) {
            val randResp = randomizer.nextInt(intentResponses.size)
            return intentResponses.lift(randResp).getOrElse("")
          }
        } catch {
          case e: Exception =>
            logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        }
        ""
    }
  
  def getIntentResponseByID(intentID: Long, userLanguage: String): String = db withSession
    {
      implicit session =>
        import profile.simple._
        val randomizer = new Random();
        val defaultIntentErrMsg = errorResponseDao.getErrorDescription(SCE_INTENT_NOT_FOUND, userLanguage)
        try {

          var intentResponses = response.filter { x => x.IntentID === intentID && x.LocaleCode === userLanguage && x.ActionID.getOrElse(0L) === 0L && x.EntityID.getOrElse(0L) === 0L}.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          if (intentResponses.isEmpty) {
            intentResponses = response.filter { x => x.IntentID === intentID && x.LocaleCode === DEFAULT_LANG && x.ActionID.getOrElse(0L) === 0L && x.EntityID.getOrElse(0L) === 0L}.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          }
          if (intentResponses.nonEmpty) {
            val randResp = randomizer.nextInt(intentResponses.size)
            return intentResponses.lift(randResp).getOrElse(defaultIntentErrMsg)
          }
        } catch {
          case e: Exception =>
            logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        }
        defaultIntentErrMsg
    }
  
  def getActionResponseByID(actionID: Long, userLanguage: String): String = db withSession
    {
      implicit session =>
        import profile.simple._
        val randomizer = new Random();
        try {

          var actionResponses = response.filter { x => x.ActionID === actionID && x.LocaleCode === userLanguage}.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          if (actionResponses.isEmpty) {
            actionResponses = response.filter { x => x.ActionID === actionID && x.LocaleCode === DEFAULT_LANG }.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          }
          if (actionResponses.nonEmpty) {
            val randResp = randomizer.nextInt(actionResponses.size)
            return actionResponses.lift(randResp).getOrElse("")
          }
        } catch {
          case e: Exception =>
            logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        }
        ""
    }

  def parsingRespForEntity(msgEvent: NlpReqCommonObj, entityRec: TEntity, convCacheRecs: List[TConversationCache], sessionID: String, intentID: Long)(implicit session: JdbcBackend#SessionDef): (String, Boolean) = {

    try {
      var entityRespText = msgEvent.msgDtls.messageTxt
      var processUserMsg = true
      var replyMsg = ""
      
      entityRec.EntityTypeCD.getOrElse("") match {

        case EILS | ELS =>
          val listActionResponse = convCacheRecs.filter(x => x.EntryID == entityRec.EntityID.toString() && x.EntryType == _ENTITY).map(x => x.ActionCacheData).head
          val ifUsrMsgNotPrsntInList = chkForUserMsgInEntityActionResp(listActionResponse.getOrElse(""), entityRec: TEntity, msgEvent.msgDtls.messageTxt)

          if (ifUsrMsgNotPrsntInList) {
            
            processUserMsg = false
            val QuestList = entityQuesForLang(entityRec.EntityID, Some(msgEvent.platformDtls.userLang))
            val rand = randomizer.nextInt(QuestList.size)
            val entityQues = QuestList.lift(rand).get
            if (entityQues._2 != "") {
              replyMsg = entityQues._1 + " (" + entityQues._2 + ")"
            } else {
              replyMsg = entityQues._1
            }
            logger.info("reloading question: {}", replyMsg)
            
            val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
            ActionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)
            actionNlpDao.raiseListingQuesForEntity("", listActionResponse.getOrElse(""), msgEvent, entityRec, sessionID, intentID)
            
          }

        case EQRP =>

          val listActionResponse = convCacheRecs.filter(x => x.EntryID == entityRec.EntityID.toString() && x.EntryType == _ENTITY).map(x => x.ActionCacheData).head
          val ifUsrMsgNotPrsntInList = chkForUserMsgInEntityActionResp(listActionResponse.getOrElse(""), entityRec: TEntity, msgEvent.msgDtls.messageTxt)

          if (ifUsrMsgNotPrsntInList) {
            processUserMsg = false
            actionNlpDao.raiseListingQuesForEntity("", listActionResponse.getOrElse(""), msgEvent, entityRec, sessionID, intentID)
          }

        case _ =>
          entityRespText = entityRespText.split("\\s+").map(parseArabicAndAlfaNum(_)).mkString(" ")
      }
      logger.info("user entered text after numerical conversion: {}", entityRespText)
      (entityRespText, processUserMsg)
    } catch {

      case userMsgException: UserMsgProcessingException =>
        throw userMsgException
      case e: Exception =>
        throw new UserMsgProcessingException("User Message Processing Exception")
    }
  }

  
   def chkForUserMsgInEntityActionResp(jsonObject: String, entityRec: TEntity, userMessage: String)(implicit session: JdbcBackend#SessionDef): Boolean = {
    import profile.simple._

    var msgNotFrmEntityList = true
    try {
      var jsArryLoopBreaker = new Breaks()
      
      val entityQuesRecord = entityQuestions.filter(x => x.EntityID === entityRec.EntityID).firstOption
      if (entityQuesRecord.nonEmpty) {
        val fbTitleJsPath = entityQuesRecord.get.Title.getOrElse("")
        val fbPayloadJsPath = entityQuesRecord.get.ButtonText.getOrElse("")

        val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
        var jsArryPath = fbTitleJsPath.substring(1, fbTitleJsPath.indexOf("*") - 1)
        logger.info("jsArryPath.length: {}", jsArryPath.length)
        val jsonArry: Integer = JsonPath.read(document, jsArryPath + ".length()");
        jsArryLoopBreaker.breakable {
        for (index <- 0 until jsonArry) {
          val payloadPath = fbPayloadJsPath.replace("[*]", "[" + index + "]")
          val payload = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, payloadPath)

          if (userMessage == payload) {
            msgNotFrmEntityList = false
           jsArryLoopBreaker.break;
          }
        }
        }
      }
      logger.info("msgNotFrmEntityList: {}", msgNotFrmEntityList)
      msgNotFrmEntityList
    } catch {
      case e: Exception =>
        e.printStackTrace
        throw new UserMsgProcessingException("User Message Processing Exception")
    }
  }

  
  def parseArabicAndAlfaNum(text: String)(implicit session: JdbcBackend#SessionDef): String = {

    var returnText = text
    try {
      val pattern = _ARABIC_ALPHA_NUMERIC_REGEX.r
      val arabicOrAlphaNum = pattern.findFirstIn(returnText)
      if (arabicOrAlphaNum.nonEmpty) {
        logger.info("arabicOrAlphaNum entered by user: {}", arabicOrAlphaNum.get)
        //get LanguageCode for String
        val getLocale = userLanguageDao.getLocales(arabicOrAlphaNum.get)
        if (getLocale.length == 1) {
          returnText = userLanguageDao.langNumReplacement(arabicOrAlphaNum.get, getLocale.head)
        }
      }
    } catch {
      case e: Exception =>
        logger.info("Error in parseArabicAndAlfaNum: {}", e.getMessage)
    }
    returnText
  }

  def raiseEntityQuestion(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, entityID: Long)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang
    var replyMsg = ""
    var finalresponseobj = new NlpMessageDtlsObj("", None, None, Some("RPLYMSG"), None, None)
    var nlpActionObj = new NlpIdentificationDtlsObj(msgEvent, sessionID, "", intentID, 0L, None, None, None, false)

    /*we are having many entity types. Based on the entity type we are raising question to user
     * 1. GEN - Asking question to user to enter some value to that entity.
     * 2. ELS - Calling external service to give list of options to users.
     * 3. EILS - Calling external service to give list of options to users with images.
     * 4. LST - Getting list of options from database.
     * 5. ILST - Getting list of options from database with images.
     * 6. ATCMT - This is for attachment entity. This entity purpose is to ask question repeatedly with confirmation as "Do you want to upload more files".
     * */

    try {

      val QuestList = entityQuesForLang(entityID, Some(userLang))
      val entityRecord = entityTbl.filter { x => x.EntityID === entityID }.first
      val entityTypeRecord = entityType.filter(x => x.EntityTypeCode === entityRecord.EntityTypeCD).first
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      if (QuestList.nonEmpty) {

        val rand = randomizer.nextInt(QuestList.size)

        val entityQues = QuestList.lift(rand).get
        if (entityQues._2 != "") {
          replyMsg = entityQues._1 + " (" + entityQues._2 + ")"
        } else {
          replyMsg = entityQues._1
        }
        logger.info("EntityTypeCD: {}", entityRecord.EntityTypeCD.get)
        entityRecord.EntityTypeCD.get match {

          case ELS | EILS =>

            logger.info("########################"+ entityRecord.EntityTypeCD.get +"#########################")

            val actionRecord = actionTbl.filter(x => x.EntityID === entityRecord.EntityID).firstOption
            logger.info("actionRecord: {}", actionRecord)
            if (actionRecord.nonEmpty) {

               val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
              ActionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)
              
              nlpActionObj = new NlpIdentificationDtlsObj(msgEvent, sessionID, "", intentID, actionRecord.get.ActionId, Some(entityRecord.EntityID), None, None, false)
              finalresponseobj = new NlpMessageDtlsObj("", None, None, None, Some(replyMsg), None)
              kafkaService.sendNlpMsgToKafkaProducer(CONSUMER_NLP, finalresponseobj, sessionID, nlpActionObj)
            }
            
          case EQRP =>
            
             logger.info("######################## "+ entityRecord.EntityTypeCD.get+ "#########################")

            val actionRecord = actionTbl.filter(x => x.EntityID === entityRecord.EntityID).firstOption
            if (actionRecord.nonEmpty) {

              nlpActionObj = new NlpIdentificationDtlsObj(msgEvent, sessionID, "", intentID, actionRecord.get.ActionId, Some(entityRecord.EntityID), None, None, false)
              finalresponseobj = new NlpMessageDtlsObj("", None, None, None, Some(replyMsg), None)
              kafkaService.sendNlpMsgToKafkaProducer(CONSUMER_NLP, finalresponseobj, sessionID, nlpActionObj)
            }

          case QRP | LST | ILST =>
            logger.info("######################## "+ entityRecord.EntityTypeCD.get+ "#########################")
            nlpActionObj = new NlpIdentificationDtlsObj(msgEvent, sessionID, "", intentID, 0L, Some(entityRecord.EntityID), None, None, false)
            finalresponseobj = new NlpMessageDtlsObj("", None, None, Some("RPLYMSG"), Some(replyMsg), None)
            actionNlpDao.dbChoiceListingWithQuickReply(finalresponseobj: NlpMessageDtlsObj, nlpActionObj: NlpIdentificationDtlsObj)

          case ATCMT => 

            logger.info("######################## ATCMT #########################")
            val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
            actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)

          case _ =>
            logger.info("######################## NORMAL #########################")
            val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
            actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)

        }
        logger.info("actionNlp in entityaction 	:		{}", nlpActionObj)
        logger.info("finalresponseobj in entityaction 	:		{}", finalresponseobj)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }
  
  

  def getIntentResponse(IntentID: Long, userLanguage: String)(implicit session: JdbcBackend#SessionDef): String = {
        import profile.simple._
        val randomizer = new Random();
        val defaultIntentErrMsg = errorResponseDao.getErrorDescription(SCE_INTENT_NOT_FOUND, userLanguage)
        try {

          var intentResponses = response.filter { x => x.IntentID.getOrElse(-1L) === IntentID && x.LocaleCode === userLanguage }.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          if (intentResponses.isEmpty) {
            intentResponses = response.filter { x => x.IntentID.getOrElse(-1L) === IntentID && x.LocaleCode === DEFAULT_LANG }.map { x => x.ReplyMessage }.list //.firstOption.getOrElse(defaultIntentErrMsg)
          }
          if (intentResponses.nonEmpty) {
            val randResp = randomizer.nextInt(intentResponses.size)
            return intentResponses.lift(randResp).getOrElse(defaultIntentErrMsg)
          }
        } catch {
          case e: Exception =>
            logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        }
        defaultIntentErrMsg
    }
  
  
  def entityQuesForLang(entityID: Long, languageCode: Option[String])(implicit session: JdbcBackend#SessionDef): List[(String,String)] = {
    import profile.simple._
    
    var entityQues = entityQuestions.filter { x => x.EntityID === entityID && x.LocaleCode === languageCode }.map { x => (x.Question,x.EntityExample.getOrElse("")) }.list
    
    if(entityQues.isEmpty){
      
      entityQues = entityQuestions.filter { x => x.EntityID === entityID  && x.LocaleCode === DEFAULT_LANG  }.map { x => (x.Question,x.EntityExample.getOrElse("")) }.list
    }
    entityQues
  }
  




}

