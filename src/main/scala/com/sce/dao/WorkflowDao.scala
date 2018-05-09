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
import com.sce.models.TConversationCache
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend
import com.sce.exception.ScriptEngineException

object WorkflowDao extends DomainComponent with Profile with NlpJsonSupport {

  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val Intent = IntentTable.IntentTable
  val entityTbl = Entity.Entity
  val entityQuestions = EntityQuestions.EntityQuestions
  val entityType = EntityType.EntityType
  val regexTable = Regex.Regex
  val response = Response.Response
  val actionTbl = ActionTable.ActionTable
  val actionLog = ActionLog.ActionLog
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val workFlowSequenceTbl = WorkFlowSequence.WorkFlowSequence
  val logger = Logging(system, this.getClass)
  
  val actionNlpDao = ActionNlpDao
  val entityProcessingDao = EntityProcessingDao
  val regexValidationDao = RegexValidationDao
  val nlpSessionDao = NLPSessionDao
  val errorResponseDao = ErrorResponseDao
  

  def processWrkflwConvEntityValue(msgEvent: NlpReqCommonObj, sessionID: String, ReplyMessage: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {

      logger.info("inside processWrkflwConventityvalue")
      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      if (convPointerRecs.length > 0 && convCacheRecs.length > 0) {

        val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).head
        val intentID = convCacheRecs.head.IntentID
        //logic for assigning user message to entity  in conversation_cache table
        if (workFlowPointer.SourceID != None) {
          
          val wrkflwSeqItem = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowPointer.SourceID.get && x.EntryType === _ENTITY).first
          val entityRecord = entityTbl.filter { x => x.EntityID === wrkflwSeqItem.EntryExpression.toLong }.first

          //if user enters text apart from uploading file then we have to raise warning message to user, saying that please upload file
          if (entityRecord.EntityTypeCD.getOrElse("") == ATCMT) {

            val nlgObj = new nlgResponseObj(Some(errorResponseDao.getErrorDescription(BC_PLS_UPLOAD_FILE, userLang)), None, None, None, None)
            actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, Some(intentID))

          } else {

            val processEntityAnswer = entityProcessingDao.parsingRespForEntity(msgEvent, entityRecord, convCacheRecs, sessionID, intentID)
            logger.info("processEntityAnswer: {}", processEntityAnswer)
            if(processEntityAnswer._2){
            val regexResponse = regexValidationDao.validateForEntityID(wrkflwSeqItem.EntryExpression.toLong, processEntityAnswer._1, userLang)
            logger.info("regexResponse: {}", regexResponse)
            if (regexResponse._1 == "") {
              conversationCacheTbl.filter(x => x.SessionID === sessionID && x.IntentID === intentID
                && x.EntryType === wrkflwSeqItem.EntryType && x.EntryID === wrkflwSeqItem.EntryExpression).map(x => x.CacheData).update(regexResponse._2)

              val nextPointerWrkflwSeqID = getNextWrkFlwElement(wrkflwSeqItem.WorkFlowSeqID, _PRIMARY, sessionID, intentID)
              if (nextPointerWrkflwSeqID != 0L) {
                //updating next element in conversation cache pointer
                conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerWrkflwSeqID))
                processWrkFlwPointer(sessionID, msgEvent)
              } else {
                nlpSessionDao.nlpCacheDeletion(sessionID)
              }

            } else {
              val nlgObj = new nlgResponseObj(Some(regexResponse._1), None, None, None, None)
              actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)
            }
          }
          }
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def userChoiceForEntityValue(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {

      val confrmRec = actionNlpDao.getConfirmationRec(ENTITY_VALUE, userLang)
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list

      val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).head
      val intentID = convCacheRecs.head.IntentID
      val wrkflwSeqItem = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowPointer.SourceID.get && x.EntryType === _ENTITY).first
      val entityID = wrkflwSeqItem.EntryExpression.toLong
      //When User clicks on continue it has to go for next node.
      logger.info("msgEvent.msgDtls.msgTxtWithoutPunc: {}", msgEvent.msgDtls.msgTxtWithoutPunc)
      if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.ConfirmedOpt.trim().toLowerCase()) {

        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(None, None, _N)

        val nextPointerWrkflwSeqID = getNextWrkFlwElement(workFlowPointer.SourceID.get, _PRIMARY, sessionID, intentID)
        if (nextPointerWrkflwSeqID != 0L) {
          //updating next element in conversation cache pointer
          conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerWrkflwSeqID))
          processWrkFlwPointer(sessionID, msgEvent)
        } else {
          nlpSessionDao.nlpCacheDeletion(sessionID)
        }

      } else if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.UnConfirmedOpt.trim().toLowerCase()) {
        //If user clicks change it has to raise current question
        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(None, None, _N)

        entityProcessingDao.raiseEntityQuestion(sessionID, msgEvent, intentID, entityID)

      } else {

        val entityRec = entityTbl.filter(x => x.EntityID === entityID).first
        val confrmRec = actionNlpDao.getConfirmationRec(ENTITY_VALUE, userLang)
        val isEntityHasValue = convCacheRecs.filter(x => x.EntryType == _ENTITY && x.EntryID == entityID.toString()).map(_.CacheData).headOption.getOrElse("")
        val confrmText = confrmRec.ConfirmationText.replace(ENTITY_NAME, entityRec.EntityName.getOrElse("Value")).replace(ENTITY_USER_VALUE, isEntityHasValue)
        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(Some(ENTITY_VALUE), Some(confrmRec.ConfirmID), _Y)
        actionNlpDao.sendConfirmationMsg(msgEvent, confrmText, sessionID,
          Some(intentID), confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)

      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def wrkflwAttachmentConv(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {

      val confrmRec = actionNlpDao.getConfirmationRec(_ATTACHMENT, userLang)
      val convCacheRecord = conversationCacheTbl.filter(x => x.SessionID === sessionID).first
      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list
      val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).head

      if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.ConfirmedOpt.toLowerCase().trim()) {

        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(None, None, _N)

        processWrkFlwPointer(sessionID, msgEvent)
      } else if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.UnConfirmedOpt.toLowerCase().trim()) {

        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(None, None, _N)
        val nextPointerWrkflwSeqID = getNextWrkFlwElement(workFlowPointer.SourceID.getOrElse(0L), _PRIMARY, sessionID, convCacheRecord.IntentID)
        if (nextPointerWrkflwSeqID != 0L) {
          //updating next element in conversation cache pointer
          conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerWrkflwSeqID))
          processWrkFlwPointer(sessionID, msgEvent)
        } else {
          nlpSessionDao.nlpCacheDeletion(sessionID)
        }

      } else {

        actionNlpDao.sendConfirmationMsg(msgEvent, confrmRec.ConfirmationText, sessionID, Some(convCacheRecord.IntentID),
          confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def processWrkFlwPointer(sessionID: String, msgEvent: NlpReqCommonObj)(implicit session: JdbcBackend#SessionDef):Unit = {
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {

      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      logger.info("convCacheRecs: {}", convCacheRecs)
      if (convPointerRecs.length > 0 /* && convCacheRecs.length > 0*/ ) {

        val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).head
        logger.info("workFlowPointer: {}", workFlowPointer)
        if (workFlowPointer.SourceID != None) {
          //code for entity workflow
          val wrkflwSeqItem = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowPointer.SourceID.get).first
          logger.info("wrkflwSeqItem: {}", wrkflwSeqItem)
          wrkflwSeqItem.EntryType match {
            case "ACTION" =>

              /*actionNlpDao.callWorkflowAction(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong,
                msgEvent.msgDtls.msgTxtWithoutPunc, wrkflwSeqItem.WorkFlowSeqID)*/
              actionNlpDao.callAction(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong,
                msgEvent.msgDtls.msgTxtWithoutPunc, wrkflwSeqItem.WorkFlowSeqID)
            case "ENTITY" =>

              checkEntityValueHadFilled(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong, convCacheRecs)
            case "DIAMOND" =>

              wrkflwConditionEvaluator(wrkflwSeqItem.WorkFlowSeqID, sessionID, msgEvent)
            case "RESPONSE" =>

              sendResponseToUserFrmWrkflw(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong, wrkflwSeqItem.WorkFlowSeqID, msgEvent.msgDtls.msgTxtWithoutPunc)
            case "MESSAGE" =>

              sendMessageToUserFrmWrkflw(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong, wrkflwSeqItem.WorkFlowSeqID)
            case "CLEAR" =>

              clearWrkFlwCache(sessionID, msgEvent, wrkflwSeqItem.IntentID, wrkflwSeqItem.EntryExpression.toLong, wrkflwSeqItem.WorkFlowSeqID)
            case _ =>

              entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
          }
        } else {
          entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
        }
      } else {
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def checkEntityValueHadFilled(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, entityID: Long, convCacheRecs: List[TConversationCache])(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang

    try {

      val isEntityHasValue = convCacheRecs.filter(x => x.EntryType == _ENTITY && x.EntryID == entityID.toString()).map(_.CacheData).headOption.getOrElse("")
      logger.info("isEntityHasValue: {}", isEntityHasValue)
      if (isEntityHasValue == "") {

        entityProcessingDao.raiseEntityQuestion(sessionID, msgEvent, intentID, entityID)
      } else {

        raiseEntityValueConfirmation(sessionID, msgEvent, intentID, entityID, isEntityHasValue)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def raiseEntityValueConfirmation(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, entityID: Long, isEntityHasValue: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang

    try {

      val entityRec = entityTbl.filter(x => x.EntityID === entityID).first
      val confrmRec = actionNlpDao.getConfirmationRec(ENTITY_VALUE, userLang)

      val confrmText = confrmRec.ConfirmationText.replace(ENTITY_NAME, entityRec.EntityName.getOrElse("Value")).replace(ENTITY_USER_VALUE, isEntityHasValue)
      conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
        && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(Some(ENTITY_VALUE), Some(confrmRec.ConfirmID), _Y)
      actionNlpDao.sendConfirmationMsg(msgEvent, confrmText, sessionID,
        Some(intentID), confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def clearWrkFlwCache(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, entityID: Long, wrkflowSeqID: Long)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang
    try {

      conversationCacheTbl.filter(x => x.SessionID === sessionID && x.EntryType === _ENTITY && x.EntryID === entityID.toString).map(x => x.FullFilled).update(None)
      val nextPointerSeqID = getNextWrkFlwElement(wrkflowSeqID, _PRIMARY, sessionID, intentID)
      logger.info("nextPointerSeqID: {}", nextPointerSeqID)
      if (nextPointerSeqID != 0L) {
        //updating next element in conversation cache pointer
        conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
        processWrkFlwPointer(sessionID, msgEvent)
      } else {
        nlpSessionDao.nlpCacheDeletion(sessionID)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def sendMessageToUserFrmWrkflw(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, messageID: Long, wrkflowSeqID: Long) (implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang
    try {

      val message = entityProcessingDao.getMessageByID(messageID, userLang)
      logger.info("message for messageID: {}", message)
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      val replyMsg = actionNlpDao.rplceTextWithActionEntityValues(message, intentID, convCacheRecs)
      val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
      actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)

      val nextPointerSeqID = getNextWrkFlwElement(wrkflowSeqID, _PRIMARY, sessionID, intentID)
      logger.info("nextPointerSeqID: {}", nextPointerSeqID)
      if (nextPointerSeqID != 0L) {
        //updating next element in conversation cache pointer
        conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
        processWrkFlwPointer(sessionID, msgEvent)
      } else {
        nlpSessionDao.nlpCacheDeletion(sessionID)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def wrkflwConditionEvaluator(workFlowSeqID: Long, sessionID: String, msgEvent: NlpReqCommonObj) (implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang
    try {
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      if (convCacheRecs.nonEmpty) {
        val seqRecord = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowSeqID).first
        val conditionExpression = rplceTextWithActionEntityValuesForCond(seqRecord.EntryExpression, seqRecord.IntentID, convCacheRecs, sessionID)
        val exprResult = ScriptEngineProcessor.evlutSimplDiamndExpr(conditionExpression)
        var nextPointerSeqID = 0L
        if (exprResult) {
          nextPointerSeqID = getNextWrkFlwElement(seqRecord.WorkFlowSeqID, _PRIMARY, sessionID: String, seqRecord.IntentID)
        } else {
          nextPointerSeqID = getNextWrkFlwElement(seqRecord.WorkFlowSeqID, _SECONDARY, sessionID: String, seqRecord.IntentID)
        }
        logger.info("nextPointerSeqID: {}", nextPointerSeqID)
        if (nextPointerSeqID != 0L) {
          //updating next element in conversation cache pointer
          conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
          WorkflowDao.processWrkFlwPointer(sessionID, msgEvent)
        } else {
          nlpSessionDao.nlpCacheDeletion(sessionID)
        }
      } else {
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
      }
    } catch {
      case see: ScriptEngineException => 
        
         val invalidSelection = errorResponseDao.getErrorDescription(SCRIPT_ENGINE_EXCEPTION, userLang)
        entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, invalidSelection)
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def rplceTextWithActionEntityValuesForCond(rawString: String, intentID: Long, replacers: List[TConversationCache], sessionID: String) (implicit session: JdbcBackend#SessionDef): String = {
    import profile.simple._

    var modifiedString = rawString
    try {
      val entityPlaceHolders = ENTITY_PLACEHOLDER_REGEX.r
      val actionPlaceHolders = ACTION_PLACEHOLDER_REGEX.r
      logger.info("replacers: {}", replacers)
      //replacers contains sequenceIDs but raw string contains there respective entityids and action ids
      //but raw string contains action and entity place holders. so first we are taking entity ids and action ids.
      //for entity and action ids we are getting sequence numbers from intent_sequence table

      val stringEntityIDs = entityPlaceHolders.findAllMatchIn(rawString).map(x => x.toString.substring(1, x.toString.length - 1).toLong).toSet.toList
      val stringActionIDs = actionPlaceHolders.findAllMatchIn(rawString).map(x => x.toString.substring(1, x.toString.length - 1).toLong).toSet.toList
      logger.info("stringEntityIDs: {}", stringEntityIDs)
      logger.info("stringActionIDs: {}", stringActionIDs)
      val entityDatatypeRecords = entityTbl.filter(x => x.EntityID inSet stringEntityIDs).map(x => (x.EntityID, x.DataType)).list
      val actionDatatypeRecords = actionTbl.filter(x => x.ActionId inSet stringActionIDs).map(x => (x.ActionId, x.DataType)).list

      logger.info("modifiedString: {}", modifiedString)

      if (stringEntityIDs.length > 0 || stringActionIDs.length > 0) {

        for (i <- 0 until entityDatatypeRecords.length) {

          val dataString = replacers.filter(x => x.EntryID == entityDatatypeRecords(i)._1.toString && x.EntryType == _ENTITY).map(_.CacheData).headOption.getOrElse("")
          logger.info("dataString: {}", dataString)
          if (entityDatatypeRecords(i)._2 == Some("STRING")) {
            modifiedString = modifiedString.replace("%" + entityDatatypeRecords(i)._1 + "%", "'%" + entityDatatypeRecords(i)._1 + "%'")
              .replace("%" + entityDatatypeRecords(i)._1 + "%", dataString)

          } else {
            modifiedString = modifiedString.replace("%" + entityDatatypeRecords(i)._1 + "%", dataString)

          }
        }
        logger.info("modifiedString: {}", modifiedString)
        for (j <- 0 until actionDatatypeRecords.length) {

          val dataString = replacers.filter(x => x.EntryID == actionDatatypeRecords(j)._1.toString && x.EntryType == ACTION).map(_.CacheData).headOption.getOrElse("")
          logger.info("dataString: {}", dataString)
          if (actionDatatypeRecords(j)._2 == Some("STRING")) {

            modifiedString = modifiedString.replace("@" + actionDatatypeRecords(j)._1 + "@", "'@" + actionDatatypeRecords(j)._1 + "@'")
              .replace("@" + actionDatatypeRecords(j)._1 + "@", dataString)
          } else {

            modifiedString = modifiedString.replace("@" + actionDatatypeRecords(j)._1 + "@", dataString)
          }
        }
        logger.info("modifiedString: {}", modifiedString)
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    modifiedString
  }

  def sendResponseToUserFrmWrkflw(sessionID: String, msgEvent: NlpReqCommonObj, intentID: Long, responseID: Long, wrkflwSeqID: Long, messagetext: String) (implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang
    try {

      val response = entityProcessingDao.getIntentResponseByID(intentID, userLang)
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      val replyMsg = actionNlpDao.rplceTextWithActionEntityValues(response, intentID, convCacheRecs)
      val nlgObj = new nlgResponseObj(Some(replyMsg), None, None, None, None)
      actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, None)

      val nextPointerSeqID = getNextWrkFlwElement(wrkflwSeqID, _PRIMARY, sessionID, intentID)
      logger.info("nextPointerSeqID: {}", nextPointerSeqID)
      if (nextPointerSeqID != 0L) {
        //updating next element in conversation cache pointer
        conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _WORKFLOW).map(x => x.SourceID).update(Some(nextPointerSeqID))
        processWrkFlwPointer(sessionID, msgEvent)
      } else {
        nlpSessionDao.nlpCacheDeletion(sessionID)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def getNextWrkFlwElement(wrkflowSeqID: Long, linkType: String, sessionID: String, intentID: Long) (implicit session: JdbcBackend#SessionDef): Long = {
    import profile.simple._

    var seqID = 0L
    try {
      val wrkflwSeqRecords = workFlowSequenceTbl.filter(x => x.IntentID === intentID).list
      val convRecords = conversationCacheTbl.filter(x => x.SessionID === sessionID).list

      val currentRec = wrkflwSeqRecords.filter(x => x.WorkFlowSeqID == wrkflowSeqID).head
      logger.info("currentWrkflwRec: {}", currentRec)
      if (linkType == _PRIMARY) {
        logger.info("currentRec.IntentID: {}", currentRec.IntentID)
        logger.info("currentRec.PmryDestWrkflwID: {}", currentRec.PmryDestWrkflwID)
        logger.info("currentRec.PmryDestSeqKey: {}", currentRec.PmryDestSeqKey)

        val nxtElement = wrkflwSeqRecords.filter(x => x.IntentID == currentRec.IntentID && Some(x.WorkFlowID) == currentRec.PmryDestWrkflwID && Some(x.WorkFlowSeqKey) == currentRec.PmryDestSeqKey).headOption
        logger.info("nxt workflow Element: {}", nxtElement)
        if (nxtElement.nonEmpty) {

          if (nxtElement.get.Required != Some("Y")) {
            return getNextWrkFlwElement(nxtElement.get.WorkFlowSeqID, _PRIMARY, sessionID, intentID)
          } /*else if (nxtElement.get.EntryType == ACTION || nxtElement.get.EntryType == _ENTITY) {
            
            val chkForFulfilldRecord = convRecords.filter(x => x.EntryType == nxtElement.get.EntryType &&
              x.EntryID == nxtElement.get.EntryExpression.toLong && x.FullFilled == _Y).headOption
            if (chkForFulfilldRecord.nonEmpty) {
              return getNextWrkFlwElement(nxtElement.get.WorkFlowSeqID, _PRIMARY, sessionID, intentID)
            } else {
              seqID = nxtElement.get.WorkFlowSeqID
            }
            
          }*/ else {
            seqID = nxtElement.get.WorkFlowSeqID
          }

        } else {
          seqID = 0L
        }

      } else if (linkType == _SECONDARY) {
        val nxtElement = wrkflwSeqRecords.filter(x => x.IntentID == currentRec.IntentID && Some(x.WorkFlowID) == currentRec.ScndryDestWrkflwID && Some(x.WorkFlowSeqKey) == currentRec.ScndryDestSeqKey).headOption
        logger.info("nxt workflow Element: {}", nxtElement)
        if (nxtElement.nonEmpty) {

          if (nxtElement.get.Required != Some("Y")) {
            return getNextWrkFlwElement(nxtElement.get.WorkFlowSeqID, _PRIMARY, sessionID, intentID)
          } /*else if (nxtElement.get.EntryType == ACTION || nxtElement.get.EntryType == _ENTITY) {
            
            val chkForFulfilldRecord = convRecords.filter(x => x.EntryType == nxtElement.get.EntryType &&
              x.EntryID == nxtElement.get.EntryExpression.toLong && x.FullFilled == _Y).headOption
            if (chkForFulfilldRecord.nonEmpty) {
              return getNextWrkFlwElement(nxtElement.get.WorkFlowSeqID, _PRIMARY, sessionID, intentID)
            } else {
              seqID = nxtElement.get.WorkFlowSeqID
            }
            
          }*/ else {
            seqID = nxtElement.get.WorkFlowSeqID
          }

        } else {
          seqID = 0L
        }

      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    logger.info("seqID: {}", seqID)
    seqID
  }

}