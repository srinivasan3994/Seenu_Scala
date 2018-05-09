

package com.sce.dao

import com.google.inject.Inject
import com.sce.models._
import akka.event.LoggingAdapter
import spray.json._
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.utils.AppConf._
import com.sce.utils.AppUtils._
import com.sce.models._
import com.sce.models.FacebookMessaging
import java.net.URI
import java.nio.file.Paths
import java.nio.file.Files
import sys.process._
import java.net.URL
import java.io.File
import EntityTypeCodes._
import NLPErrorCodes._
import NLPStrings._
import com.sce.models.TSessionRecord
import com.sce.models.TConversationCache
import akka.event.{ Logging }
import scala.slick.jdbc.JdbcBackend.SessionDef
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.JdbcBackend.SessionDef


object NLPDao extends NlpJsonSupport with DomainComponent with Profile {

  override val profile: JdbcProfile = SlickDBDriver.getDriver

  val imalLogs = IMALLogs.IMALLogs
  val db = new DBConnection(profile).dbObject()

  val knowledgeUnit = KnowledgeUnit.KnowledgeUnit
  val Intent = IntentTable.IntentTable
  val Keyword = KeywordTable.KeywordTable
  val entityTbl = Entity.Entity
  val entityQuestions = EntityQuestions.EntityQuestions
  val entityType = EntityType.EntityType
  val regexTable = Regex.Regex
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val userRating = UserRating.UserRating
  val userAttachemntsTbl = UserAttachemnts.UserAttachemnts
  val intentExtnTbl = IntentExtn.IntentExtn
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val conversationTbl = Conversation.Conversation
  val workFlowSequenceTbl = WorkFlowSequence.WorkFlowSequence
  
  
  val intentIdentificationDao = IntentIdentificationDao
  val imSessionDao = IMSessionDao
  val errorResponseDao = ErrorResponseDao
  val actionNlpDao = ActionNlpDao
  val entityValueIdentificationDao = EntityValueIdentificationDao
  val entityProcessingDao = EntityProcessingDao
  val workflowDao = WorkflowDao
  val nlpSessionDao = NLPSessionDao
  val userLanguageDao = UserLanguageDao
  val conversationDao = ConversationDao
  
  val logger = Logging(system, this.getClass)
  /*
  @author Akhil
  Preface for NLP processing:-
  --------------------------
  Here I am going to explaing the total processing of NLP Engine.

  1. First user enters his message in different languages.
  2. If text contains keywords of many languages then we have to show confirmation for language. Allowing user to select specific language for conversation.
  3. Once user selected the language we are checking for no of intents containing in user entered text. There after we are raising confirmation choices for
  		intent to user.
  4. Once the user chooses an intent we have to process the intent in user specified language.
  5. Intent processing will be happen in 3 Scenarios.
  		a. keyword -> response (example is Greeting messages like, Hi.., Hello...,..etc).
  		b. keyword -> action -> response (example is Balance service calling. It means engine no need to ask any question to user.)
  		c. keyword -> entity1 - entity2 - entity3 -> action with or without confirmation -> response
  				(example is transferring amount to beneficiary, For this we need 2 or 3 values from user. For this we are asking question user,
  				 to enter those values. once entered we have to ask confirmation to user (if configured in db). Once user selected option from confirmation
  				  we are going to perform that action and giving a final response for whole intent. ex:- transfer completed  ).
  6. User will stops conversation at the middle, using cancel keywords.
  7. When user enters filler keywords, Engine will not give any response to user.
  For entity types go to raiseQuestion method in entityProcessingDao class.
  Go through comments in code. :)

   * */

  def processConversation(msgEvent: NlpReqCommonObj, sessionID: String, isNewUser: Boolean) (implicit session: JdbcBackend#SessionDef) = {//db withSession { implicit session =>
    import profile.simple._

    var userLang = userLanguageDao.getUserPreferredLocale(msgEvent.platformDtls.userID, msgEvent.platformDtls.channelID.getOrElse(0L))
    msgEvent.platformDtls.userLang = userLang
    try {
      //First we are checking is conersation is started using concersation_pointer table.
       val convExist = conversationPointerTbl.filter(x => x.SessionID === sessionID).list
      //if conversation didn't start create new converstaionID
      if (convExist.isEmpty) {
        
        conversationDao.insertConversationRecord(sessionID, None, Some(SCSUtils.getCurrentDateTime), None)
      }
      imSessionDao.insertIMALLogs(sessionID, msgEvent.msgDtls.msgTxtWithoutPunc, USER, None, None)
      val fillerKwdsInUserMsg = intentIdentificationDao.findFillerKeywordsInUserMsg(msgEvent.msgDtls.msgTxtWithoutPunc, Set.empty)
      logger.info("is filler keywords found: {}", fillerKwdsInUserMsg)
      msgEvent.msgDtls.msgTxtWithoutPunc = fillerKwdsInUserMsg._2
      
      //checking condition if text is empty after removing filler keywords in sentence
      if (msgEvent.msgDtls.msgTxtWithoutPunc.nonEmpty) {
        
        if (convExist.nonEmpty) {

          processConvConfirmation(msgEvent, sessionID)
        } else {
          
          
          //if conversation is not started we are sending user message to language detection (using UTF unicodes) and intent identification
          val keyWordSet = intentIdentificationDao.findKeywordsInSntnce(sessionID, msgEvent.msgDtls.msgTxtWithoutPunc, true, true, Set.empty)

          logger.info("keyWordSet: {}", keyWordSet)
          if (keyWordSet.nonEmpty) {
            val languagesfoundinKeywords = userLanguageDao.getLocales(keyWordSet.mkString(" "))
            logger.info("languagesfoundinKeywords: {}", languagesfoundinKeywords)

            //if more than one language is identified we have to ask language confirmation to user.
            if (languagesfoundinKeywords.length > 1) {
              userLanguageDao.raiseLocaleConfirmation(languagesfoundinKeywords.mkString("~~~~"), msgEvent, sessionID, keyWordSet.mkString(", "))
            } else {

              if (languagesfoundinKeywords.length == 1) {
                userLanguageDao.setUserPreferredLocale(msgEvent.platformDtls.userID, languagesfoundinKeywords.head, msgEvent.platformDtls.channelID)
                userLang = languagesfoundinKeywords.head
                msgEvent.platformDtls.userLang = userLang
              }
              userMsgForKwdIdentification(msgEvent, msgEvent.msgDtls.msgTxtWithoutPunc, sessionID, Some(keyWordSet))
            }
          } else {

            val languagesfoundinText = userLanguageDao.getLocales(msgEvent.msgDtls.msgTxtWithoutPunc)

            if (languagesfoundinText.isEmpty) {

              conversationDao.updtConvIntentID(sessionID, Some(0))
              val ReplyMessage = errorResponseDao.getErrorDescription(SCE_LANG_NOT_FOUND, userLang)
              entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, ReplyMessage)

            } else {
              logger.info("______________No Keywords found_____________")
              conversationDao.updtConvIntentID(sessionID, Some(0))
              entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
            }
          }

          if (isNewUser) {
            Thread.sleep(500)
            val pleaseWaitMsg = errorResponseDao.getErrorDescription(WELCOME_MSG, userLang)
          }
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        userLang = msgEvent.platformDtls.userLang
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def processConvConfirmation(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._

    /* In NLP we are having total 5 types of confirmations.
     * 1. LANGAUGE - Confirmation for selecting language.
     * 2. INTENT_CHOICE - Confirmation for selecting a particular intent from set of intents
     * 3. CANCEL - Confirmation to stop conversation at the middle.
     * 4. ATCMT - Confirmation for allowing user to upload more files.
     * 5. ACTION - Confirmation for performing action.
     * 6. ENTITY_VALUE - Confirmation for entity which is already filled
     * */

    var userLang = msgEvent.platformDtls.userLang
    try {

      val confrimationCacheRecord = conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerType === _CONFIRMATION).first
      logger.info("confrimationCacheRecord: {}", confrimationCacheRecord)
      //In this point the user is in conversation confirmation mode.
      if (confrimationCacheRecord.isPointed == "Y") {

        confrimationCacheRecord.PointerDesc.get match {

          case LANGUAGE =>

            val keywordString = confrimationCacheRecord.TempCache.getOrElse("")
            val languagesfound = userLanguageDao.getAllLocales
            // User selected a language from given choice
            if (languagesfound.contains(msgEvent.msgDtls.msgTxtWithoutPunc)) {
              conversationPointerTbl.filter(x => x.SessionID === sessionID).delete
              userMsgForKwdIdentification(msgEvent, keywordString, sessionID, None)
            } else {
              //if user didn't selected any langauge from confirmation then we are stopping the conversation
              val langRec = userLanguageDao.getLocaleRec(userLang)
              var replymsg = errorResponseDao.getErrorDescription(SCE_INTENT_NOT_FOUND, userLang)
              if (langRec.nonEmpty) {
                replymsg = langRec.get.LocaleErrorMsg.getOrElse("")
              }
              entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, replymsg)

            }

          case INTENT_CHOICE =>

            convFromIntentChoice(msgEvent, sessionID)

          case ACTION =>

            workflowDao.processWrkFlwPointer(sessionID, msgEvent)

          case CANCEL =>

            userChoiceForCnclConv(msgEvent, sessionID)

          case ATCMT =>

            val convCacheRecord = conversationCacheTbl.filter(x => x.SessionID === sessionID).first

            val cnclKwdsInUserInput = intentIdentificationDao.getConvCancelCnfrmtin(sessionID: String, msgEvent.msgDtls.msgTxtWithoutPunc, convCacheRecord.IntentID)
            logger.info("Is cancel keywords in user input: {}", cnclKwdsInUserInput)
            if (cnclKwdsInUserInput == CANCEL) {

              callForCancel(msgEvent, sessionID, convCacheRecord)
            } else {

              workflowDao.wrkflwAttachmentConv(msgEvent, sessionID)

           }

          case ENTITY_VALUE =>

            val convCacheRecord = conversationCacheTbl.filter(x => x.SessionID === sessionID).first
            IMSessionDao.insertIMALLogs(sessionID, msgEvent.msgDtls.msgTxtWithoutPunc, USER, Some(convCacheRecord.IntentID), None)

            val cnclKwdsInUserInput = intentIdentificationDao.getConvCancelCnfrmtin(sessionID: String, msgEvent.msgDtls.msgTxtWithoutPunc, convCacheRecord.IntentID)
            logger.info("Is cancel keywords in user input: {}", cnclKwdsInUserInput)

            if (cnclKwdsInUserInput == CANCEL) {

              callForCancel(msgEvent, sessionID, convCacheRecord)
            } else {

              workflowDao.userChoiceForEntityValue(msgEvent, sessionID)
            }

          case _ =>

            entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
        }
      } else {
        // In some cases user entered text contains cancel keywords. For stopping conversation
        isUserMsgForCnclConv(msgEvent, sessionID)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def userMsgForKwdIdentification(msgEvent: NlpReqCommonObj, messagetext: String, sessionID: String, keyWordSet: Option[Set[String]])(implicit session: JdbcBackend#SessionDef) = { //db withSession { implicit session =>
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang

    try {
      msgEvent.msgDtls.msgTxtWithoutPunc = messagetext
      var intentArray = intentIdentificationDao.keywordIntentIdentificationForConv(sessionID, msgEvent, true, true, keyWordSet)
      val intentsWithMoreThan1Tranact = intentIdentificationDao.listMoreThanOneTrnsactIntent(intentArray)
      logger.info("intentsWithMoreThan1Tranact: {}", intentsWithMoreThan1Tranact)
      if (intentsWithMoreThan1Tranact.length > 1) {
        // if user entered text contains morethan one intent, we have to show intent choices to user
        actionNlpDao.fbReplyMsgForMultplIntnts(intentsWithMoreThan1Tranact, msgEvent, sessionID, userLang)
      } else if (intentArray.length == 1) {
        //need to update conversation record with intentID
        val convID = conversationDao.getConversationID(sessionID)
        conversationTbl.filter(x => x.ConversationID === convID).map(x => x.IntentID).update(Some(intentArray.head))
        // creating nlp cache for intent in database (temporary storage for storing action and entity values).
        nlpSessionDao.nlpCacheCreation(sessionID, intentArray.head)
        //if user enters entity values along with intent keywords before starting conversation, then we have to store that values.
        // One those values stored. No need to ask those entity questions
        entityValueIdentificationDao.entityValuesForConv(sessionID, messagetext, intentArray.head)

        workflowDao.processWrkFlwPointer(sessionID, msgEvent)

      } else {

        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def convFromIntentChoice(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = { // = db withSession { implicit session =>
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {
      var IntentID: Option[Long] = Intent.filter(x => x.IntentDefinition.toLowerCase.trim === msgEvent.msgDtls.msgTxtWithoutPunc).map(x => x.IntentID).firstOption
      logger.info("IntentID: {}", IntentID)
      var messageStoredInCache = conversationPointerTbl.filter(x => x.SessionID === sessionID && x.PointerDesc.getOrElse("") === INTENT_CHOICE).map(_.TempCache.getOrElse("")).first

      if (IntentID.nonEmpty) {

        val intentArray = intentIdentificationDao.keywordIntentIdentificationForConv(sessionID, msgEvent, true, true, None)

        if (intentArray.contains(IntentID.get)) {
          
          // fetch recent insert imSessionLog record
          val recentLogID = userRating.filter(x => x.UserID === msgEvent.platformDtls.userID ).map(x => x.IMSessionLogID).max.getOrElse(-1L)
          
          //fetch keywordpattern for userRatingLogRec for recentLogID 
          val keywordPattern = userRating.filter(x => x.IMSessionLogID === recentLogID).map(x => x.Keywords)
          //update all keyword patterns for that user to selected intent id
          userRating.filter(x => x.UserID === msgEvent.platformDtls.userID && (x.Keywords in keywordPattern)).map(x => x.IntentID).update(IntentID)

          val cacheText = conversationPointerTbl.filter(x => x.SessionID === sessionID).map(x => x.TempCache.getOrElse("")).firstOption.getOrElse("")
          conversationPointerTbl.filter(x => x.SessionID === sessionID).delete

          nlpSessionDao.nlpCacheCreation(sessionID, IntentID.get)
          entityValueIdentificationDao.entityValuesForConv(sessionID, cacheText, IntentID.get)
          workflowDao.processWrkFlwPointer(sessionID, msgEvent)

        } else {
          //if user chooses another intent apart from selection, give invalid selection message.
          val invalidSelection = errorResponseDao.getErrorDescription(INVALID_SELECTION, userLang)
          entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, invalidSelection)
        }

      } else {
        //if user chooses another intent apart from selection, give invalid selection message.
        val invalidSelection = errorResponseDao.getErrorDescription(INVALID_SELECTION, userLang)
        entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, invalidSelection)
      }
      
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def isUserMsgForCnclConv(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = { //db withSession { implicit session =>
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {
      logger.info("inside isUserMsgForCnclConv")
      val convCacheRecord = conversationCacheTbl.filter(x => x.SessionID === sessionID).firstOption
      if (convCacheRecord.nonEmpty) {

        //if no confirmation is intialized we need to check for cancel keywords in user entered message.
        val cnclKwdsInUserInput = intentIdentificationDao.getConvCancelCnfrmtin(sessionID: String, msgEvent.msgDtls.msgTxtWithoutPunc, convCacheRecord.get.IntentID)
        logger.info("Is cancel keywords in user input: {}", cnclKwdsInUserInput)

        if (cnclKwdsInUserInput == CANCEL) {

          callForCancel(msgEvent, sessionID, convCacheRecord.get)
        } else {
          // if no cancel keywords found in user entered text, we have to send user entered text to entity value processing.

          workflowDao.processWrkflwConvEntityValue(msgEvent, sessionID, "")
        }
      }else{
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def callForCancel(msgEvent: NlpReqCommonObj, sessionID: String, convCacheRecord: TConversationCache)(implicit session: JdbcBackend#SessionDef) = { //db withSession { implicit session =>
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {
      val confrmRec = actionNlpDao.getConfirmationRec(CNCLCNFRMTYPE, userLang)

      conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
        && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(Some(CANCEL), Some(confrmRec.ConfirmID), _Y)

      val intentNameRecords = intentExtnTbl.filter(x => x.IntentID === convCacheRecord.IntentID).list
      val intentRecords = Intent.filter(x => x.IntentID === convCacheRecord.IntentID).list.map(x =>
        actionNlpDao.getIntentNamesForChoice(x.IntentID, x.IntentDefinition, intentNameRecords, userLang)).head

      actionNlpDao.sendConfirmationMsg(msgEvent, confrmRec.ConfirmationText + " " + intentRecords._1, sessionID,
        Some(convCacheRecord.IntentID), confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def userChoiceForCnclConv(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = {//db withSession { implicit session =>
    import profile.simple._

    var userLang = msgEvent.platformDtls.userLang
    try {

      val confrmRec = actionNlpDao.getConfirmationRec(CNCLCNFRMTYPE, userLang)
      val convCacheRecord = conversationCacheTbl.filter(x => x.SessionID === sessionID).first

      if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.ConfirmedOpt.trim().toLowerCase()) {
        nlpSessionDao.nlpCacheDeletion(sessionID)
        val nlgObj = new nlgResponseObj(Some(confrmRec.TerminationText.getOrElse("")), None, None, None, None)
        actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, Some(convCacheRecord.IntentID))
      } else if (msgEvent.msgDtls.msgTxtWithoutPunc == confrmRec.UnConfirmedOpt.trim().toLowerCase()) {
        conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
          && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(None, None, _N)

       
          workflowDao.processWrkFlwPointer(sessionID, msgEvent)
        
      } else {
        val intentNameRecords = intentExtnTbl.filter(x => x.IntentID === convCacheRecord.IntentID).list
        val intentRecords = Intent.filter(x => x.IntentID === convCacheRecord.IntentID).list.map(x =>
          actionNlpDao.getIntentNamesForChoice(x.IntentID, x.IntentDefinition, intentNameRecords, userLang)).head

        actionNlpDao.sendConfirmationMsg(msgEvent, confrmRec.ConfirmationText + " " + intentRecords._1, sessionID,
          Some(convCacheRecord.IntentID), confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)
      }

    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def isAttchmntForConv(msgEvent: NlpReqCommonObj, sessionID: String)(implicit session: JdbcBackend#SessionDef) = {//db withSession { implicit session =>
    import profile.simple._

    val userLang = msgEvent.platformDtls.userLang

    val defaultEntityErrMsg = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, userLang) // errorResponseTbl.filter(x => x.ErrorCode === SCE_ENTITY_NOT_FOUND).map(x => x.ErrorResponse).firstOption.getOrElse("")
    try {

      var messagetext = "`user_attachment`" //imlog parameter
      val attachmentObj = msgEvent.msgDtls.attachments
      val attachmentErrorMsg = errorResponseDao.getErrorDescription(SCE_FILEUPLD_ERROR, userLang)
      val confrmRec = actionNlpDao.getConfirmationRec(_ATTACHMENT, userLang)

      val convPointerRecs = conversationPointerTbl.filter(x => x.SessionID === sessionID).list
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID).list
      // condition for whether the conversation is started or not for file processing.
      if (convPointerRecs.length > 0 && convCacheRecs.length > 0) {

        val workFlowPointer = convPointerRecs.filter(x => x.PointerType == _WORKFLOW).headOption
        if (workFlowPointer.nonEmpty) {
          //code for entity action workflow
          if (workFlowPointer.get.SourceID != None) {

            val wrkflwSeqItem = workFlowSequenceTbl.filter(x => x.WorkFlowSeqID === workFlowPointer.get.SourceID.get && x.EntryType === _ENTITY).first
            val entityRecord = entityTbl.filter { x => x.EntityID === wrkflwSeqItem.EntryExpression.toLong }.first
            if (entityRecord.EntityTypeCD.getOrElse("") == ATCMT) {

              val imLogID = imSessionDao.insertIMALLogs(sessionID, messagetext, USER, Some(wrkflwSeqItem.IntentID), None) //.insertIMALLogs(sessionID, messagetext, senderid, sessionIntent)
              for (i <- 0 until attachmentObj.length) {

                val attachmentUrl = attachmentObj(i).atcmtContent
                logger.info("attachmentUrl: {}", attachmentUrl)
                val fileDetails = Paths.get(new URI(attachmentUrl).getPath()).getFileName().toString()
                logger.info("fileDetails: {}", fileDetails)
                val fileExtension = fileDetails.substring(fileDetails.indexOf("."))
                val attachmentName = "./attachments/" + msgEvent.platformDtls.userID + "_" + imLogID + "_" + i + fileExtension
                //downloading file to botchestra server and storing it in above location. File size is lessthan 15 mb.
                val isValidFile = downloadFile(attachmentUrl, attachmentName)

                if (isValidFile) {

                  val pathToFile = Paths.get(attachmentName).toAbsolutePath().toString();
                  logger.info("pathToFile: {}", pathToFile)

                  imSessionDao.insertUserAtcmts(imLogID, attachmentName, attachmentObj(i).atcmtType, msgEvent.platformDtls.channelID.getOrElse(0L), entityRecord.EntityID, _N)
                  conversationPointerTbl.filter(x => x.PointerType === _CONFIRMATION
                    && x.SessionID === sessionID).map(x => (x.PointerDesc, x.SourceID, x.isPointed)).update(Some(ATCMT), Some(confrmRec.ConfirmID), _Y)

                  //once user uploaded attachment, again we are raising confirmation allowing user to enter more files.
                  actionNlpDao.sendConfirmationMsg(msgEvent, confrmRec.ConfirmationText, sessionID, Some(wrkflwSeqItem.IntentID),
                    confrmRec.ConfirmedOpt, confrmRec.UnConfirmedOpt)

                } else {
                  //if user uploads morethan 15 mb file. then we are showing warning message to user
                  val nlgObj = new nlgResponseObj(Some(errorResponseDao.getErrorDescription(SCE_FILELIMIT_ERROR, userLang)), None, None, None, None)
                  actionNlpDao.sendFinalNlg(sessionID, msgEvent, nlgObj, None, Some(wrkflwSeqItem.IntentID))
                }
              }
            } else {
              entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, attachmentErrorMsg)
            }
          }
        }
      } else {
        //User sent attachment before conversation get started then we have to say "We cant process your file".
        conversationDao.insertConversationRecord(sessionID, None, Some(SCSUtils.getCurrentDateTime), None)
        imSessionDao.insertIMALLogs(sessionID, messagetext, USER, None, None)
        entityProcessingDao.trmnteIntentConvWithMsg(sessionID, msgEvent, attachmentErrorMsg)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        entityProcessingDao.errorTerminateIntentConversation(sessionID, userLang, msgEvent)
    }
  }

  def downloadFile(attachmentUrl: String, attachmentName: String): Boolean = {

    var isValidFile = false
    try {

      println("-************************-")
      val destinationFile = attachmentName
      val pathToFile = Paths.get(destinationFile);
      Files.createDirectories(pathToFile.getParent());
      Files.createFile(pathToFile);
      val destFile = new File(destinationFile)
      //downloading file from url using directives.
      new URL(attachmentUrl) #> destFile !!

      logger.info("length of destination file: {}", destFile.length())
      if (destFile.exists()) {
        if (destFile.length() < 12582912) {
          isValidFile = true
        } else {
          destFile.delete()
        }
      }
    } catch {
      case e: Exception => e.printStackTrace() //println(e.getMessage)
    }
    logger.info("isDownloaded file is valid file or not: {}", isValidFile)
    isValidFile
  }

}