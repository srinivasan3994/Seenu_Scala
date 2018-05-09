package com.sce.dao

import scala.slick.jdbc.StaticQuery.interpolation

import com.sce.models._
import com.sce.services._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.sce.models._
import spray.json._
import akka.event.LoggingAdapter
import com.typesafe.config.Config

import akka.http.scaladsl.model._
import scala.slick.driver.JdbcProfile
import com.sce.models._
import com.jayway.jsonpath.JsonPath
import com.sce.utils.JsonPathUtils
//import com.sce.models.ConformationSelectionStrategy
import scala.util.control.Breaks._
import java.sql.Connection
import java.sql.ResultSet
import com.sce.utils._
import com.sce.dao._
import javax.inject._
import com.sce.models.TSessionRecord
import com.sce.models.TSessionRecord
import com.sce.models.TFlowChartSession
import com.sce.models.NLPErrorCodes._
import com.sce.utils.AppConf._
import com.sce.models.NLPStrings._
import com.sce.models.NLGJsonSupport
import com.sce.models._
import com.sce.models.TConfirm
import scala.util.Random
import com.sce.models.TIntentExtn
import com.sce.models.TConversationCache
import com.sce.models.NLPRegexs._
import com.sce.models.TActionExtn
import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.MultiPart
import scalaj.http.HttpResponse
import java.nio.file.{ Path, Paths ,Files }
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.io.Source
import com.sce.exception.BCActionConfException
import scala.slick.jdbc.JdbcBackend
import  com.sce.exception.UserMsgProcessingException

object ActionNlpDao extends NlpJsonSupport with NLGJsonSupport with DomainComponent with Profile {


  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)
  
  var IMAL_SEND_TOPIC = ""
  var ACTION_TOPIC = ""
  val action = ActionTable.ActionTable
  val actionExtnTbl = ActionExtn.ActionExtn
  val actionLog = ActionLog.ActionLog
  val intentResponse = Response.Response
  val userMapping = UserMapping.UserMapping
  val keywordTable = KeywordTable.KeywordTable
  val intentTable = IntentTable.IntentTable
  val entityQuestions = EntityQuestions.EntityQuestions
  val response = Response.Response
  val entityRecord = Entity.Entity
  val entityType = EntityType.EntityType
  val actionErrorResponse = ActionErrorResponse.ActionErrorResponse
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val knowledgeUnit = KnowledgeUnit.KnowledgeUnit
  val conversationTbl = Conversation.Conversation
  val confirmTbl = Confirm.Confirm
  val localeTbl = Locale.Locale
  val intentExtnTbl = IntentExtn.IntentExtn
  val intentTbl = IntentTable.IntentTable
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val workFlowSequenceTbl = WorkFlowSequence.WorkFlowSequence
  val actionAuthorization = ActionAuthorization.ActionAuthorization
  
  
  
  val conversationDao = ConversationDao
  val iMSessionDao = IMSessionDao
  val kafkaService = KafkaService
  val errorResponseDao = ErrorResponseDao
  val nlpSessionDao = NLPSessionDao
  val entityProcessingDao = EntityProcessingDao
  val jsonPathUtils = JsonPathUtils

  def sendFinalNlg(sessionID: String, msgEvent: NlpReqCommonObj, nlgMessage: nlgResponseObj, convID: Option[Long], intentID: Option[Long]) {

      val conversationID = if (convID == None) {
        conversationDao.getConversationID(sessionID)
      } else {
        convID.get
      }
      var message = if (nlgMessage.simpleMessage != None) {
        val plain_msg = nlgMessage.simpleMessage.get
        nlgMessage.simpleMessage = Some(NLPSampleOutMsg(plain_msg).toJson.toString())
        nlgMessage.simpleMessage.get
      } else if (nlgMessage.confirmationMessage != None) {
        nlgMessage.confirmationMessage.get
      } else if (nlgMessage.choiceListing != None) {
        nlgMessage.choiceListing.get
      } else if (nlgMessage.imageChoiceListing != None) {
        nlgMessage.imageChoiceListing.get
      } else if (nlgMessage.intentChoices != None) {
        nlgMessage.intentChoices.get
      } else {
        ""
      }
      logger.info("***************************************************message: {}****************************************************", message)
      iMSessionDao.insertIMALLogs(sessionID, message, BOT, intentID, Some(conversationID))
      val IMAL_SEND = config.getString("IMAL_SEND")
      kafkaService.sendNlgMsgToKafkaProducer(IMAL_SEND, msgEvent, sessionID, intentID, nlgMessage)
  }

  def sendConfirmationMsg(msgEvent: NlpReqCommonObj, replyMessage: String, sessionID: String, intentID: Option[Long], CnfrmOpt: String, UnCnfrmOpt: String)(implicit session: JdbcBackend#SessionDef) = {// = db withSession { implicit session =>
    import profile.simple._

    val payload = NlpJsonGeneratorService.getQuickReplyJson(msgEvent.platformDtls.userID, replyMessage, CnfrmOpt: String, UnCnfrmOpt: String).toJson.toString()
    var intID: Option[Long] = intentID
    val nlgObj = new nlgResponseObj(None, Some(payload), None, None, None)
    sendFinalNlg(sessionID, msgEvent, nlgObj, None, intID)
    
  }

  def getActionForLang(actionID: Long, userLang: String)(implicit session: JdbcBackend#SessionDef):Option[TActionExtn] = {//: Option[TActionExtn] = db withSession { implicit session =>
    import profile.simple._

    val randomizer = new Random();
    var actExtnRec: Option[TActionExtn] = None
    try {

      actExtnRec = actionExtnTbl.filter(x => x.ActionID === actionID && x.LocaleCode === userLang).firstOption
      if (actExtnRec.isEmpty) {
        
        actExtnRec = actionExtnTbl.filter(x => x.ActionID === actionID && x.LocaleCode === DEFAULT_LANG).firstOption
      }
    } catch {
      case e: Exception =>
        logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
    }
    actExtnRec
  }

  def callAction(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, actionID: Long, messagetext: String, workflowSeqID: Long) = db withSession { implicit session =>
    import profile.simple._

    var replyMsg = ""
    val userLang = msgEvent.platformDtls.userLang
    IMAL_SEND_TOPIC = config.getString("IMAL_SEND")
    ACTION_TOPIC = config.getString("CONSUMER_NLP")

    try {

      val actionRec = action.filter(x => x.ActionId === actionID).first
      var nlpActionIdentObj = new NlpIdentificationDtlsObj(msgEvent, sessionID, "", intentID, actionID, None, None, Some(workflowSeqID), true)
      var finalresponseobj = NlpMessageDtlsObj(messagetext, None, None, None, None, None)
      kafkaService.sendNlpMsgToKafkaProducer(ACTION_TOPIC, finalresponseobj, sessionID, nlpActionIdentObj)

    } catch {
      case e: Exception =>
        e.printStackTrace()
        val replyMsg = ErrorResponseDao.getErrorDescription(ACTION_CONF_EXCEPTION, userLang)
        EntityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, replyMsg)
    }
  }
  
  
  def rplceTextWithActionEntityValues(rawString: String, intentID: Long, replacers: List[TConversationCache]): String = { //= db withSession { implicit session =>
    import profile.simple._

    var modifiedString = rawString
    try {
      val entityPlaceHolders = ENTITY_PLACEHOLDER_REGEX.r
      val actionPlaceHolders = ACTION_PLACEHOLDER_REGEX.r

      //replacers contains sequenceIDs but raw string contains there respective entityids and action ids
      //but raw string contains action and entity place holders. so first we are taking entity ids and action ids.
      //for entity and action ids we are getting sequence numbers from intent_sequence table
      
      val stringEntityIDs = entityPlaceHolders.findAllMatchIn(rawString).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
      val stringActionIDs = actionPlaceHolders.findAllMatchIn(rawString).map(x => x.toString.substring(1, x.toString.length - 1)).toSet.toList
      logger.info("stringEntityIDs: {}", stringEntityIDs)
      logger.info("stringActionIDs: {}", stringActionIDs)
      if (stringEntityIDs.length > 0 || stringActionIDs.length > 0) {
        
        for(id <- 0 until stringEntityIDs.length){
          val dataString = replacers.filter(x => x.EntryID == stringEntityIDs(id) && x.EntryType == _ENTITY).map(_.CacheData).headOption.getOrElse("")
          logger.info("dataString: {}", dataString)
          modifiedString = modifiedString.replace("%" + stringEntityIDs(id) + "%", dataString)
        }
        for(id <- 0 until stringActionIDs.length){
          val dataString = replacers.filter(x => x.EntryID == stringActionIDs(id) && x.EntryType == ACTION).map(_.CacheData).headOption.getOrElse("")
          logger.info("dataString: {}", dataString)
          modifiedString = modifiedString.replace("@" + stringActionIDs(id) + "@", dataString)
        }
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    logger.info("modifiedString: {}", modifiedString)
    modifiedString
  }
  
  def getConfirmationRec(ActionID: Long, userLang: String)(implicit session: JdbcBackend#SessionDef): Option[TConfirm] = { //db withSession    {      implicit session =>
        var confirmRec: Option[TConfirm] = None
        try {

          import profile.simple._
          val randomizer = new Random();
          var actionConfirmationRecords = confirmTbl.filter(x => x.ActionID.getOrElse(-1L) === ActionID && x.LocaleCode === userLang).list
          if (actionConfirmationRecords.isEmpty) {

            actionConfirmationRecords = confirmTbl.filter(x => x.ActionID === ActionID && x.LocaleCode === DEFAULT_LANG).list
          }

          if (actionConfirmationRecords.nonEmpty) {
            val rand = randomizer.nextInt(actionConfirmationRecords.size)

            confirmRec = Some(actionConfirmationRecords.lift(rand).get)
          }
        } catch {
          case e: Exception =>
            logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        }
        logger.info("confirmRec: {}", confirmRec)
        confirmRec
    }

  def getConfirmationRec(ConfirmCode: String, userLang: String)(implicit session: JdbcBackend#SessionDef): TConfirm = { //: Boolean = db withSession    {      implicit session =>

    try {
      import profile.simple._
      val randomizer = new Random();

      val confrmRecords = confirmTbl.filter(x => x.ConfirmationType === ConfirmCode && x.LocaleCode === userLang).list

      val rand = randomizer.nextInt(confrmRecords.size)

      confrmRecords.lift(rand).get

    } catch {
      case e: Exception =>
        logger.info("Failed while fetching confirmation for Intent: {}", e.getMessage)
        null
    }
  }

  def isActionCalledInInterval(UserID: String, ActionID: Long, RequestBody: String, sessionID: String, localeCode: String)(implicit session: JdbcBackend#SessionDef): Boolean = { //: Boolean = db withSession    {      implicit session =>
    import profile.simple._

    var isWarningRequired = false
    try {

      val actionRec = action.filter(x => x.ActionId === ActionID).firstOption
      if (actionRec != None) {

        val intervalTime = new java.sql.Timestamp(new java.util.Date().getTime - 1000 * actionRec.get.CallingInterval.getOrElse(0L))
        logger.info("intervalTime: ", intervalTime)
        val isAtionCalledBforInterval = actionLog.filter(x => x.UserID === UserID &&
          x.ActionId === ActionID && x.IsSuccess === "Y" && x.RequestBody === RequestBody &&
          x.Created >= intervalTime.toString()).list

        if (isAtionCalledBforInterval.length > 0) {
          nlpSessionDao.nlpCacheDeletion(sessionID)
          isWarningRequired = true
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace
        isWarningRequired
    }
    isWarningRequired
  }


  def insertActionLogs(actionId: Long, intentID: Long, entityID: Option[Long], webhookUrl: String, RequestBody: String, accessCode: String, result: String, userID: String, isSuccess: String)(implicit session: JdbcBackend#SessionDef) = { // = db withSession { implicit session =>
    import profile.simple._

    actionLog.map { x => ( x.ActionId, x.IntentID, x.EntityID, x.WebhookUrl, x.RequestBody, x.CallMethod, x.AccessCode, x.Result, x.Created, x.UserID, x.IsSuccess) } += (
      
      actionId, Some(intentID), entityID, webhookUrl, RequestBody, "POST", accessCode, result, SCSUtils.getCurrentDateTime, userID, isSuccess)
    val logId = actionLog.filter(x => x.UserID === userID).map(x => x.ActionLogId).max.run.getOrElse(0L)

  }

  def entityResponseHandlerForImage(jsonObject: String, finalresponseobj: NlpMessageDtlsObj, nlpAction: NlpIdentificationDtlsObj) = db withSession { implicit session =>
    import profile.simple._

    val userLang = nlpAction.msgEvent.platformDtls.userLang
    var whnExcepRaised = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, userLang)
    logger.info("--------------------entityResponseHandlerForImage-----------------------")
    try {
      
      val entityRec = entityRecord.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      raiseListingQuesForEntity("", jsonObject: String, nlpAction.msgEvent, entityRec, nlpAction.sessionID, nlpAction.intentID)
      
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID, nlpAction.msgEvent, whnExcepRaised)
    }
  }

 
  def raiseListingQuesForEntity(userMessage: String, jsonObject: String, msgEvent: NlpReqCommonObj, entityRec: TEntity, sessionID: String, intentID: Long)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    try {
      val entityQuesRecord = entityQuestions.filter(x => x.EntityID === entityRec.EntityID).firstOption
      if (entityQuesRecord.nonEmpty) {
        val fbTitleJsPath = entityQuesRecord.get.Title.getOrElse("")
        val fbPayloadJsPath = entityQuesRecord.get.ButtonText.getOrElse("")
        val fbSubtitleJsPath = entityQuesRecord.get.SubTitle.getOrElse("")
        val fbImageUrlJsPath = entityQuesRecord.get.ImageUrl.getOrElse("")
        var quickReplies: List[NLPImageListElements] = Nil

        val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
        var jsArryPath = fbTitleJsPath.substring(1, fbTitleJsPath.indexOf("*") - 1)
        logger.info("jsArryPath: {}", jsArryPath)
        val jsonArry: Integer = JsonPath.read(document, jsArryPath + ".length()");

        if (jsonArry > 0) {

          //for entity listing we need to store action response in database cache
          conversationCacheTbl.filter(x => x.SessionID === sessionID && x.EntryType === _ENTITY && x.EntryID === entityRec.EntityID.toString)
            .map(x => x.ActionCacheData).update(Some(jsonObject))

          if (entityRec.EntityTypeCD.getOrElse("") == _EQRP) {
            var facebookQuickReply: List[NLPQuickReply] = Nil
            for (index <- 0 until jsonArry) {

              val titlePath = fbTitleJsPath.replace("[*]", "[" + index + "]")
              val payloadPath = fbPayloadJsPath.replace("[*]", "[" + index + "]")

              val fbTitle: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, titlePath)
              val fbPayload: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, payloadPath)

              facebookQuickReply = NLPQuickReply(
                contentType = "text",
                title = fbTitle,
                payload = fbPayload) :: facebookQuickReply
            }
            logger.info("facebookQuickReply: {}", facebookQuickReply)

            val noOfSets = (jsonArry / 10) + 1

            for (j <- 0 until noOfSets) {

              val offset = j * 10
              val elemtnsInSet = quickReplies.drop(offset).take(10)
              val text = entityQuesRecord.get.Question
              val payload = getFacebookQuickReplyTemplate(msgEvent.platformDtls.userID, text, facebookQuickReply).toJson.toString()

              logger.info("payload: {}" + payload)
              sendFinalNlg(sessionID, msgEvent, new nlgResponseObj(None, None, None, None, Some(payload)), None, None)

            }

          } else {

            for (index <- 0 until jsonArry) {

              val titlePath = fbTitleJsPath.replace("[*]", "[" + index + "]")
              val payloadPath = fbPayloadJsPath.replace("[*]", "[" + index + "]")
              val subtitlePath = fbSubtitleJsPath.replace("[*]", "[" + index + "]")
              val imageUrlPath = fbImageUrlJsPath.replace("[*]", "[" + index + "]")

              val fbTitle: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, titlePath)
              val fbPayload: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, payloadPath)
              val fbSubtitle: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, subtitlePath)
              val fbImageUrl: String = jsonPathUtils.sentenceResponseParserWithoutErr(jsonObject, imageUrlPath)
              val facebookListButton = Array(NLPImageListButton("postback", fbTitle, fbPayload))
              quickReplies = NLPImageListElements(
                title = fbTitle,
                subtitle = fbSubtitle,
                imageUrl = fbImageUrl,
                buttons = facebookListButton) :: quickReplies
            }

            val noOfSets = (jsonArry / 10) + 1 //no of sets = 3

            for (j <- 0 until noOfSets) {

              val offset = j * 10
              val elemtnsInSet = quickReplies.drop(offset).take(10)
              val payload = getNlpImageListReplyTemplate(msgEvent.platformDtls.userID, elemtnsInSet)
              logger.info("payload: {}" + payload)
              //FacebookService.sendFacebookImageListTemplate(payload)
              val nlgObj = new nlgResponseObj(None, None, None, Some(payload.toJson.toString()), None)
              sendFinalNlg(sessionID, msgEvent, nlgObj, None, Some(intentID))

            }
          }
        } else {

          val entityTypeRecord = entityType.filter(x => x.EntityTypeCode === entityRec.EntityTypeCD).first
          entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, entityTypeRecord.ValidationMessage.getOrElse(""))
        }
      }
    } catch {

      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, msgEvent.platformDtls.userLang, msgEvent)
    }
  }
  
  def dbChoiceListing(finalresponseobj: NlpMessageDtlsObj, nlpAction: NlpIdentificationDtlsObj, userLanguage: String)(implicit session: JdbcBackend#SessionDef) = { // = db withSession { implicit session =>

    import profile.simple._
    
    val senderID = nlpAction.msgEvent.platformDtls.userID
    var whnExcepRaised = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, userLanguage)//errorResponseTbl.filter(x => x.ErrorCode === SCE_ENTITY_NOT_FOUND).map(x => x.ErrorResponse).firstOption.getOrElse("")

    var connection: Connection = null
    try {
      var quickReplies: List[NLPListElements] = Nil

      val entityQuesTbl = entityQuestions.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityRecordTbl = entityRecord.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityTypeRecord = entityType.filter(x => x.EntityTypeCode === entityRecordTbl.EntityTypeCD).first

      connection = db.createConnection()
      // create the statement, and run the select query
      val statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
      val resultSet = statement.executeQuery(entityQuesTbl.EntityQuery.getOrElse(""))
      resultSet.last();
      val noOfRecords = resultSet.getRow();
      resultSet.beforeFirst();
      logger.info("noOfRecords: {}" + noOfRecords)
      if (noOfRecords > 0) {
        while (resultSet.next()) {
          val title = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.Title.getOrElse(""), resultSet)
          val subTitle = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.SubTitle.getOrElse(""), resultSet)
          val ButtonText = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.ButtonText.getOrElse(""), resultSet)
          /* val title = resultSet.getString(entityQuesTbl.Title.getOrElse("").toString)
          val subTitle = resultSet.getString(entityQuesTbl.SubTitle.getOrElse("").toString)
          val ButtonText = resultSet.getString(entityQuesTbl.ButtonText.getOrElse("").toString)*/
          val choiceListButton = Array(NLPListButton("postback", title, ButtonText))

          quickReplies = NLPListElements(
            title = title,
            subtitle = subTitle,
            buttons = choiceListButton) :: quickReplies

        }

        val noOfSets = (noOfRecords / 10) + 1 //no of sets = 3
        val pageNumber = 1
        for (j <- 0 until noOfSets) {
          val offset = j * 10
          val elemtnsInSet = quickReplies.drop(offset).take(10)
          val payload = getFacebookListReplyTemplate(senderID,  elemtnsInSet)
          logger.info("payload: {}" + payload)
          val nlgObj = new nlgResponseObj(None, None, Some(payload.toJson.toString()), None, None)
          sendFinalNlg(nlpAction.sessionID, nlpAction.msgEvent, nlgObj, None, Some(nlpAction.intentID))

        }
      } else {
        entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID, nlpAction.msgEvent, entityTypeRecord.ValidationMessage.getOrElse(""))
      }
    } catch {

      case e: Exception =>
        e.printStackTrace()
       entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID,  nlpAction.msgEvent, whnExcepRaised)
    } finally {
      connection.close()
    }
  }
  
  
   def dbChoiceListingWithQuickReply(finalresponseobj: NlpMessageDtlsObj, nlpAction: NlpIdentificationDtlsObj)(implicit session: JdbcBackend#SessionDef) = { // = db withSession { implicit session =>

    import profile.simple._
    val senderID = nlpAction.msgEvent.platformDtls.userID
    val userLang = nlpAction.msgEvent.platformDtls.userLang
    var whnExcepRaised = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, nlpAction.actionId, userLang)

    var facebookQuickReply: List[NLPQuickReply] = Nil
    var connection: Connection = null
    try {
      var quickReplies: List[FacebookListElements] = Nil

      val entityQuesTbl = entityQuestions.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityRecordTbl = entityRecord.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityTypeRecord = entityType.filter(x => x.EntityTypeCode === entityRecordTbl.EntityTypeCD).first

      connection = db.createConnection()
      // create the statement, and run the select query
      val statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
      val resultSet = statement.executeQuery(entityQuesTbl.EntityQuery.getOrElse(""))
      resultSet.last();
      val noOfRecords = resultSet.getRow();
      resultSet.beforeFirst();
      logger.info("noOfRecords: {}" + noOfRecords)
      if (noOfRecords > 0) {
        while (resultSet.next()) {
          val title = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.Title.getOrElse(""), resultSet)
          val subTitle = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.SubTitle.getOrElse(""), resultSet)
          val ButtonText = jsonPathUtils.dbCoiceListConstructor(entityQuesTbl.ButtonText.getOrElse(""), resultSet)
          
          val facebookListButton = Array(FacebookListButton("postback", title, ButtonText))

          facebookQuickReply = NLPQuickReply(
            contentType = "text",
            title = title,
            payload = title) :: facebookQuickReply

        }
        logger.info("quickReplies: {}",  quickReplies)

        val noOfSets = (noOfRecords / 10) + 1 //no of sets = 3
        for (j <- 0 until noOfSets) {
          val offset = j * 10
          val elemtnsInSet = quickReplies.drop(offset).take(10)
          val text = entityQuesTbl.Question
          val payload = getFacebookQuickReplyTemplate(senderID, text, facebookQuickReply).toJson.toString()

          logger.info("payload: {}" + payload)
          sendFinalNlg(nlpAction.sessionID, nlpAction.msgEvent, new nlgResponseObj(None, None, None, None, Some(payload)), None, None)

        }
      } else {
        entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID,  nlpAction.msgEvent, entityTypeRecord.ValidationMessage.getOrElse(""))
      }

    } catch {

      case e: Exception =>
        e.printStackTrace()
       entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID,  nlpAction.msgEvent, whnExcepRaised)
        
    } finally {
      connection.close()
    }

  }

  def dbImageChoiceListing(finalresponseobj: NlpMessageDtlsObj, nlpAction: NlpIdentificationDtlsObj, userLang: String)(implicit session: JdbcBackend#SessionDef) = { 

    import profile.simple._

    val senderID = nlpAction.msgEvent.platformDtls.userID
    var whnExcepRaised = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, userLang)
    var connection: Connection = null
    try {
      var quickReplies: List[NLPImageListElements] = Nil

      val entityQuesTbl = entityQuestions.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityRecordTbl = entityRecord.filter(x => x.EntityID === nlpAction.entityID.getOrElse(0L)).first
      val entityTypeRecord = entityType.filter(x => x.EntityTypeCode === entityRecordTbl.EntityTypeCD).first

      connection = db.createConnection()
      // create the statement, and run the select query
      val statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
      val resultSet = statement.executeQuery(entityQuesTbl.EntityQuery.getOrElse(""))
      resultSet.last();
      val noOfRecords = resultSet.getRow();
      resultSet.beforeFirst();
      logger.info("noOfRecords: {}" + noOfRecords)
      if (noOfRecords > 0) {
        while (resultSet.next()) {
          val title = jsonPathUtils.dbCoiceImageListConstructor(entityQuesTbl.Title.getOrElse(""), resultSet)
          val subTitle = jsonPathUtils.dbCoiceImageListConstructor(entityQuesTbl.SubTitle.getOrElse(""), resultSet)
          val imgUrl = jsonPathUtils.dbCoiceImageListConstructor(entityQuesTbl.ImageUrl.getOrElse(""), resultSet)
          val ButtonText = jsonPathUtils.dbCoiceImageListConstructor(entityQuesTbl.ButtonText.getOrElse(""), resultSet)
          val facebookListButton = Array(NLPImageListButton("postback", title, ButtonText))

          quickReplies = NLPImageListElements(
            title = title,
            subtitle = subTitle,
            imageUrl = imgUrl,
            buttons = facebookListButton) :: quickReplies

        }

        val noOfSets = (noOfRecords / 10) + 1 //no of sets = 3
        val pageNumber = 1
        for (j <- 0 until noOfSets) {
          val offset = j * 10
          val elemtnsInSet = quickReplies.drop(offset).take(10)
          val payload = getNlpImageListReplyTemplate(senderID,  elemtnsInSet)
          logger.info("payload: {}" + payload)
          val nlgObj = new nlgResponseObj(None, None, None, Some(payload.toJson.toString()), None)
          sendFinalNlg(nlpAction.sessionID, nlpAction.msgEvent, nlgObj, None, Some(nlpAction.intentID))
        }
      } else {
        entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID,  nlpAction.msgEvent, entityTypeRecord.ValidationMessage.getOrElse(""))
      }

    } catch {

      case e: Exception =>
        e.printStackTrace()
       entityProcessingDao.trmnteIntentConvWithMsg(nlpAction.sessionID,  nlpAction.msgEvent, whnExcepRaised)
        
    } finally {
      connection.close()
    }

  }


  def fbReplyMsgForMultplIntnts(transactIntentIds: List[Long], msgEvent: NlpReqCommonObj,  sessionID: String, userLanguage:String)(implicit session: JdbcBackend#SessionDef) = { //= db withSession { implicit session =>
    import profile.simple._
    var facebookQuickReply: List[NLPQuickReply] = Nil
    val intentNameRecords = intentExtnTbl.filter(x => x.IntentID inSet transactIntentIds).list
    val intentRecords = intentTable.filter(x => x.IntentID inSet transactIntentIds).list.map(x => getIntentNamesForChoice(x.IntentID, x.IntentDefinition,intentNameRecords,userLanguage))//intentTable.filter(x => x.IntentID inSet transactIntentIds).map(x => (x.IntentID, x.IntentName)).list
    val keywordsForIntent = keywordTable.filter(x => x.IntentID inSet transactIntentIds).map(x => (x.IntentID, x.Keyword)).list
    val intntChoiceTxt = localeTbl.filter(x => x.LocaleCode === userLanguage).map(x => x.IntentChoiceMsg.getOrElse("")).firstOption.getOrElse("Which one do u want to perform ?")
    println(keywordsForIntent)
    for (i <- 0 until intentRecords.length) {
     // val kwywordTxt = keywordsForIntent.filter(x => x._1 == intentRecords(i)._1).map(_._2).head
      facebookQuickReply = NLPQuickReply(
        contentType = "text",
        title = intentRecords(i)._1,
        payload = intentRecords(i)._2) :: facebookQuickReply
    }
    logger.info("quickReplies 	:		{}", facebookQuickReply)
    conversationPointerTbl.map(x =>(x.SessionID, x.PointerType, x.PointerDesc, x.SourceID, x.isPointed, x.TempCache)).insert(
          sessionID, _CONFIRMATION, Some(INTENT_CHOICE), None, _Y, Some(msgEvent.msgDtls.msgTxtWithoutPunc))
    val payload = getFacebookQuickReplyTemplate(msgEvent.platformDtls.userID, intntChoiceTxt, facebookQuickReply).toJson.toString()
    logger.info("payload 	:		{}", payload)

    sendFinalNlg(sessionID, msgEvent, new nlgResponseObj(None, None, None, None, Some(payload)), None, None)

  }

  def getIntentNamesForChoice(intentID: Long, intentDefinition: String, intentRecords: List[TIntentExtn], userLang: String): (String, String) = {

    try {
      var nameRecords = intentRecords.filter(x => x.IntentID == intentID && x.LocaleCode == userLang).map(x => x.IntentName)

      if (nameRecords.isEmpty) {
        nameRecords = intentRecords.filter(x => x.IntentID == intentID).map(x => x.IntentName)
      }
      var intentNameForLang = intentDefinition
      if (nameRecords.nonEmpty) {

        val randomizer = new Random();
        val rand = randomizer.nextInt(nameRecords.size)
        intentNameForLang = nameRecords.lift(rand).get

      }
      (intentNameForLang, intentDefinition)

    } catch {
      case e: Exception =>
        ("", "")
    }
  }
  
  
  def getFacebookQuickReplyTemplate(sender: String, text: String, facebookQuickReplyList: List[NLPQuickReply]): NLPQuickReplyMessage = {
     NLPQuickReplyMessage(
      text = text,
      facebookQuickReplyList)
  }

  def getFacebookListReplyTemplate(sender: String, replay: List[NLPListElements]): NLPBaseAttachment = {
     NLPBaseAttachment(
      NLPListAttachement(attachmentType = "template", NLPListPaylods(templateType = "generic", replay)))
  }

  def getNlpImageListReplyTemplate(sender: String,  replay: List[NLPImageListElements]): NLPImageListBaseAttachment = {
     NLPImageListBaseAttachment(
      NLPImageListAttachement(attachmentType = "template", NLPImageListPaylods(templateType = "generic", replay)))
  }

  def actionResponseprocessing(nlpDtls: NlpActionConsumerObj, actionResponse: String) = db withSession { implicit session =>
    import profile.simple._

    val sender = nlpDtls.identityDtls.msgEvent.platformDtls.userID

    val url = nlpDtls.messageDtls.reqURL.getOrElse("")
    val actionId = nlpDtls.identityDtls.actionId
    val intentId = nlpDtls.identityDtls.intentID
    val entityId = nlpDtls.identityDtls.entityID
    val sessionID = nlpDtls.identityDtls.sessionID
    val requestBody = nlpDtls.messageDtls.reqBody.getOrElse("")
    val requestMethod = nlpDtls.messageDtls.reqMethod.getOrElse("")
    val requestMessage = nlpDtls.messageDtls.message
    val userLang = nlpDtls.identityDtls.msgEvent.platformDtls.userLang
    var successNode = ""
    var errorNode = ""

    try {

      val accessCode = UserMappingDao.getAccessCode(sender)
      if (nlpDtls.identityDtls.entityID == None && nlpDtls.identityDtls.entityID.getOrElse(0L) == 0L) {
      logger.info("----------intent action flow--------------------")
        actionValueForWrkflwConv(nlpDtls, actionResponse)
      } else {
        logger.info("----------entity action flow--------------------")
        insertActionLogs(actionId, intentId, entityId, nlpDtls.messageDtls.reqURL.getOrElse(""), requestBody, accessCode, actionResponse, sender, _Y)
        val actionExtnRec = actionExtnTbl.filter(x => x.ActionID === actionId && x.LocaleCode === userLang).first
        var successNode = actionExtnRec.SuccessCode.getOrElse("")
        var errorNode = actionExtnRec.ErrorCode.getOrElse("")
        ActionNlpDao.entityResponseHandlerForImage(actionResponse, nlpDtls.messageDtls, nlpDtls.identityDtls)
      }
    } catch {

      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(nlpDtls.identityDtls.sessionID, userLang, nlpDtls.identityDtls.msgEvent)
    }
  }

  def actionValueForWrkflwConv(nlpDtls: NlpActionConsumerObj, actionResponse: String)(implicit session: JdbcBackend#SessionDef) ={// = db withSession { implicit session =>
    import profile.simple._

    val senderID = nlpDtls.identityDtls.msgEvent.platformDtls.userID
    val url = nlpDtls.messageDtls.reqURL.getOrElse("")
    val actionId = nlpDtls.identityDtls.actionId
    val intentId = nlpDtls.identityDtls.intentID
    val entityId = nlpDtls.identityDtls.entityID
    val sessionID = nlpDtls.identityDtls.sessionID
    val requestBody = nlpDtls.messageDtls.reqBody.getOrElse("")
    val requestMethod = nlpDtls.messageDtls.reqMethod.getOrElse("")
    val requestMessage = nlpDtls.messageDtls.message
    val workflowSeqID = nlpDtls.identityDtls.workFlowSeqID.getOrElse(0L)
    val userLang = nlpDtls.identityDtls.msgEvent.platformDtls.userLang

    try {

      val actionRec = action.filter(x => x.ActionId === actionId).first
      val actionExtnRec = actionExtnTbl.filter(x => x.ActionID === actionId && x.LocaleCode === userLang).first

      val accessCode = UserMappingDao.getAccessCode(senderID)
      var successNode = actionExtnRec.SuccessCode.getOrElse("")
      var errorNode = actionExtnRec.ErrorCode.getOrElse("")
      val actionReplymsg = entityProcessingDao.getActionResponseByID(actionId, userLang)

      logger.info("successNode :\n{}", successNode)
      logger.info("errorNode :\n{}", errorNode)

      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list

      val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).head
      if (workFlowPointer.SourceID != None) {

        //val intentSeqItem = intentMappingTbl.filter(x => x.IntentSeqID === nonWorkFlowPointer.SourceID.get && x.EntryType === _ACTION).first
        val wrkflwSeqItem = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowPointer.SourceID.get).first
        
        if (jsonPathUtils.checkResponse(actionResponse, successNode)) {
          insertActionLogs(actionId, intentId, entityId, nlpDtls.messageDtls.reqURL.getOrElse(""), requestBody, accessCode, actionResponse, senderID, _Y)
          val actionParameter = jsonPathUtils.getActionValueFromJson(actionResponse, actionExtnRec.ResponsePath.getOrElse(""))
          logger.info("actionParameter: {}", actionParameter)
          if (actionParameter.nonEmpty) {
            this.synchronized {
              val updt = conversationCacheTbl.filter(x => x.SessionID === sessionID && x.IntentID === intentId
                && x.EntryID === wrkflwSeqItem.EntryExpression && x.EntryType === wrkflwSeqItem.EntryType).map(x => x.CacheData).update(actionParameter.get)
              val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
              if (actionReplymsg != "") {

                val replyMsg = rplceTextWithActionEntityValues(actionReplymsg, intentId, convCacheRecs)
                logger.info("action response: ", replyMsg)
                val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
                sendFinalNlg(sessionID, nlpDtls.identityDtls.msgEvent, nlgObj, None, None)
              }
            }
          }
          val nextPointerSeqID = WorkflowDao.getNextWrkFlwElement(workflowSeqID, _PRIMARY, sessionID, intentId)
          if (nextPointerSeqID != 0L) {
            //updating next element in conversation cache pointer
            conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
            WorkflowDao.processWrkFlwPointer(sessionID, nlpDtls.identityDtls.msgEvent)
          } else {
            nlpSessionDao.nlpCacheDeletion(sessionID)
          }

        } else if (jsonPathUtils.checkResponse(actionResponse, errorNode)) {
          insertActionLogs(actionId, intentId, entityId, nlpDtls.messageDtls.reqURL.getOrElse(""), requestBody, accessCode, actionResponse, senderID, _N)
          val message = errorResponseDao.errorResponseParser(actionResponse, errorNode, actionId, userLang)
          entityProcessingDao.trmnteIntentConvWithMsg(sessionID, nlpDtls.identityDtls.msgEvent, message)

        } else {
          insertActionLogs(actionId, intentId, entityId, nlpDtls.messageDtls.reqURL.getOrElse(""), requestBody, accessCode, actionResponse, senderID, _Y)
          val actionParameter = jsonPathUtils.getActionValueFromJson(actionResponse, actionExtnRec.ResponsePath.getOrElse(""))
          logger.info("actionParameter: {}", actionParameter)
          if (actionParameter.nonEmpty) {
            this.synchronized {
              val updt = conversationCacheTbl.filter(x => x.SessionID === sessionID && x.IntentID === intentId
                && x.EntryID === wrkflwSeqItem.EntryExpression && x.EntryType === wrkflwSeqItem.EntryType).map(x => x.CacheData).update(actionParameter.get)
              val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
              if (actionReplymsg != "") {

                val replyMsg = rplceTextWithActionEntityValues(actionReplymsg, intentId, convCacheRecs)
                logger.info("action response: ", replyMsg)
                val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
                sendFinalNlg(sessionID, nlpDtls.identityDtls.msgEvent, nlgObj, None, None)

              }
            }
          }
          val nextPointerSeqID = WorkflowDao.getNextWrkFlwElement(workflowSeqID, _PRIMARY, sessionID, intentId)
          if (nextPointerSeqID != 0L) {
            //updating next element in conversation cache pointer
            conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
            WorkflowDao.processWrkFlwPointer(sessionID, nlpDtls.identityDtls.msgEvent)
          } else {
            nlpSessionDao.nlpCacheDeletion(sessionID)
          }
        }
      }
    } catch {

      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(nlpDtls.identityDtls.sessionID, userLang, nlpDtls.identityDtls.msgEvent)

    }
  }

  def getActionAuthorizationRec: List[TActionAuthorization] = db withSession { implicit session =>
    import profile.simple._
    
    actionAuthorization.list
    
  }
    
  def updateAccessToken(token: String, aaid: Long) = db withSession { implicit session =>
    import profile.simple._
    
    actionAuthorization.filter(x => x.AAID === aaid).map(x => (x.AccessToken, x.TokenCreatedTime)).update((Some(token),Some(SCSUtils.getCurrentDateTime)))
  }
   
  

}

