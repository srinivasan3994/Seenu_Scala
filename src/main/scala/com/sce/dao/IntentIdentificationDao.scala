package com.sce.dao

import com.sce.services._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.sce.models._
import com.sce.models._
import spray.json._
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Random
import scala.collection.immutable.TreeSet
import scala.util.control.Breaks._
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.models.NLPErrorCodes._
import com.sce.models.EntityTypeCodes._
import com.sce.models.intentChkObj
import com.sce.models.errorCodeObj
import com.sce.models.successCodeObj
import com.sce.models.NLPStrings._

import com.sce.utils.AppConf._
import scala.annotation.tailrec
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend
import com.sce.exception.KeywordIdentificationException

object IntentIdentificationDao extends NlpJsonSupport with DomainComponent with Profile {

  override val profile: JdbcProfile = SlickDBDriver.getDriver
  implicit val logger = Logging(system, this.getClass)

  val db = new DBConnection(profile).dbObject()

  val imalLogs = IMALLogs.IMALLogs.sortBy(x => x.LogID.desc)
  val Keyword = KeywordTable.KeywordTable
  val Intent = IntentTable.IntentTable
 
  val knowledgeUnit = KnowledgeUnit.KnowledgeUnit
  val conversationTbl = Conversation.Conversation
  val KRLogTable = KRLog.KRLog
  val userRating =UserRating.UserRating
  val projectKeywordTbl = ProjectKeyword.ProjectKeyword
  
  
  val conversationDao = ConversationDao
  val imSessionDao = IMSessionDao
  

  def getConvCancelCnfrmtin(sessionID: String, messagetext: String, intentID: Long)(implicit session: JdbcBackend#SessionDef):String = {//: String = db withSession { implicit session =>

    import profile.simple._
    //cancelKeyWordSet = Set.empty

    var cnfrmtType = ""
    try {
      // val puncMesgText = messagetext.split("\\s+").map(_.replaceAll("^[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]|[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]$", "")).mkString(" ")
      var cnclKeyWordSet = findCancelKeywordsInUserMsg( messagetext, Set.empty)
      logger.info("cnclKeyWordSet: {}", cnclKeyWordSet)

      val cancelKeywords = projectKeywordTbl.filter { x => (x.ProjectKeyword.toLowerCase inSet cnclKeyWordSet._1) && (x.ProjectKeywordType === CANCEL) }.list 

      //transfer
      if (cancelKeywords.nonEmpty) {
        cnfrmtType = CANCEL
       }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    println("cnfrmtType : {}", cnfrmtType)
    cnfrmtType
  }
  
  def keywordIntentIdentificationForConv(sessionID: String, msgEvent: NlpReqCommonObj, activateKU: Boolean, activateIntent: Boolean, keywords: Option[Set[String]])(implicit session: JdbcBackend#SessionDef):List[Long] = {//: List[Long] = db withSession { implicit session =>
    import profile.simple._
    
    var intentArray: List[Long] = List(0L)
    var keyWordSet: Set[String] = Set.empty
    var ratedIntents: List[(Long, Int)] = Nil
    
    try {
      
      var puncMesgText = msgEvent.msgDtls.msgTxtWithoutPunc//MessageText.split("\\s+").map(_.replaceAll("^[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]|[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]$", "")).mkString(" ")
      logger.info("MessageText main: {}", puncMesgText)
      if (keywords.nonEmpty) {
        keyWordSet = keywords.get
      } else {
        keyWordSet = findKeywordsInSntnce(sessionID, puncMesgText, true, true, Set.empty)
      }
      val positiveIntentIds = Keyword.filter { x => (x.Keyword.toLowerCase.trim inSet keyWordSet) && x.Polarity === "P" }.map(x => x.IntentID).list
      if (positiveIntentIds.length > 0) {

        val positiveIntentRate = positiveIntentIds.map(x => (x, 1)).groupBy(x => x).map(x => (x._1._1, x._2.map(_._2).sum)).toList
        val maxIntentCount = positiveIntentRate.map(x => x._2).max
        val maxRatedIntents = positiveIntentRate.filter(x => x._2 == maxIntentCount)
        val negativeIntentIDs = Keyword.filter { x => (x.Keyword.toLowerCase.trim inSet keyWordSet) && (x.IntentID inSet maxRatedIntents.map(x => x._1)) && x.Polarity === "N" }
          .map(x => x.IntentID).list

        ratedIntents = maxRatedIntents.filterNot(x => negativeIntentIDs.contains(x._1)) //.map(x => x._1)
        intentArray = ratedIntents.map(x => x._1) //4,5,6

        if (activateKU) {

          val activeKus = knowledgeUnit.filter(x => x.ActiveIND === "Y").map(x => x.KUID).list
          intentArray = Intent.filter(x => (x.IntentID inSet intentArray) && (x.KUID inSet activeKus)).map(x => x.IntentID).list
        }
        if (activateIntent) {

          val activeIntents = Intent.filter(x => x.ActiveIND === "Y").map(x => x.IntentID).list
          intentArray = Intent.filter(x => (x.IntentID inSet activeIntents) && (x.IntentID inSet intentArray)).map(x => x.IntentID).list
        }
        
        logger.info("intentArray: {}" , intentArray)
        val iMSessionLogID = imSessionDao.getCurrentIMSessionLogID(sessionID: String)
        
        if (intentArray.length == 0) {
          logger.info("_____________________________NO INTENT FOUND FOR KEYWORDS________________________________")
          conversationDao.updtConvIntentID(sessionID, Some(0))
          
        } else if (intentArray.length == 1) {
          
          conversationDao.updtConvIntentID(sessionID, intentArray.headOption)
        } else {

          val sortedKwywords = collection.immutable.SortedSet[String]() ++ keyWordSet
          val keywordString = sortedKwywords.mkString(",")
          logger.info("keywordString: {}" , keywordString)
          
          val ifIntentAlreadyChoosed = userRating.filter(x => x.UserID === msgEvent.platformDtls.userID && x.Keywords === keywordString && (x.IntentID.getOrElse(-1L) inSet intentArray)).map(x => x.IntentID.getOrElse(0L)).firstOption.getOrElse(0L)
          if (ifIntentAlreadyChoosed == 0L) {
            
            insertUserRatingLogs(msgEvent.platformDtls.userID, iMSessionLogID, keywordString, None, sessionID)
          } else {
            
            intentArray = List(ifIntentAlreadyChoosed)
            ConversationDao.updtConvIntentID(sessionID, Some(ifIntentAlreadyChoosed))
          }
          ratedIntents.foreach(f =>
           insertKRLogs(iMSessionLogID, f._1, f._2))

        }
      }
    } catch {
      case e: Exception =>
    }
    intentArray
  }
  
  
  def insertKRLogs(iMSessionLogID: Long, intentID: Long, keywordRate: Int)(implicit session: JdbcBackend#SessionDef) = {//= db withSession { implicit session =>
    import profile.simple._
    KRLogTable.map(x => (x.IMSessionLogID, x.IntentID, x.KeywordRate)) += ((iMSessionLogID, Some(intentID), Some(keywordRate)))
  
  }

  

  def insertUserRatingLogs(userID: String, iMSessionLogID: Long, keywords: String, intentID: Option[Long], sessionID: String): Long = db withSession { implicit session =>
    import profile.simple._
    
    (userRating.map { x => (x.IMSessionLogID, x.Keywords, x.IntentID, x.UserID, x.SessionID) }) += (iMSessionLogID, keywords, intentID, userID, Some(sessionID))
    val ratingID = userRating.filter(x => x.UserID === userID).map(x => x.RatingID).max.run.getOrElse(0L)
    ratingID

  }
  
  def findCancelKeywordsInUserMsg( MessageText: String, cancelKeywords: Set[String])(implicit session: JdbcBackend#SessionDef):(Set[String],String) = {//: Set[String]  = db withSession { implicit session =>

    import profile.simple._
    var CnclkeyWordSet = cancelKeywords
    try {

      var trimmedCancelKeywordSentence = MessageText.trim
      var result = MessageText.trim.split("\\s+")
      var CancelKeywordsInSentence: List[TProjectKeyword] = Nil
      var cancelFreeText = MessageText.trim

      //This for is to fetch available intents in sentence
      for (keyword <- result) {

        val keywordRecordList = projectKeywordTbl.filter { x => (x.ProjectKeyword.toLowerCase.trim like ("%" + keyword.toLowerCase() + "%")) && (x.ProjectKeywordType === CANCEL)}.list
        if (keywordRecordList.nonEmpty) {
          CancelKeywordsInSentence = keywordRecordList ::: CancelKeywordsInSentence
        } else {
          trimmedCancelKeywordSentence = trimmedCancelKeywordSentence.replace(keyword, "").replaceAll("\\s+", " ")
        }
      }

      CancelKeywordsInSentence = CancelKeywordsInSentence.toSet.toList
      var intentsWithDegree = CancelKeywordsInSentence.map(x => (x.ProjectKeyword.toLowerCase, x.ProjectKeywordID, x.ProjectKeyword.split(" ").length))
      intentsWithDegree = intentsWithDegree.sortBy(x => x._3).reverse

        for (j <- 0 until intentsWithDegree.length) {
          if (trimmedCancelKeywordSentence.contains(intentsWithDegree(j)._1)) {
            CnclkeyWordSet += intentsWithDegree(j)._1
            trimmedCancelKeywordSentence = trimmedCancelKeywordSentence.replace(intentsWithDegree(j)._1, "").replaceAll("\\s+", " ")
            cancelFreeText = cancelFreeText.replace(intentsWithDegree(j)._1, "").replaceAll("\\s+", " ")
            logger.info("trimmedCancelKeywordSentence: {}",trimmedCancelKeywordSentence)
          }
      }
      (CnclkeyWordSet, cancelFreeText.trim)
    } catch {

      case e: Exception => e.printStackTrace()
      throw new KeywordIdentificationException("Cancel Keyword Exception")
    }
    
  }

  def findFillerKeywordsInUserMsg(MessageText: String, fillerKeywords: Set[String])(implicit session: JdbcBackend#SessionDef): (Set[String], String) = {

    import profile.simple._
    var fllrKeyWordSet = fillerKeywords
    try {

      var trimmedFillerKeywordSentence = MessageText
      var result = MessageText.split("\\s+")
      var fillerKeywordsInSentence: List[TProjectKeyword] = Nil
      var fillerFreeText = MessageText

      //This for is to fetch available keywords in sentence
      for (keyword <- result) {

        val keywordRecordList = projectKeywordTbl.filter { x => (x.ProjectKeyword.toLowerCase.trim like ("%" + keyword.toLowerCase() + "%")) && (x.ProjectKeywordType === _FILLER) }.list
        if (keywordRecordList.nonEmpty) {
          fillerKeywordsInSentence = keywordRecordList ::: fillerKeywordsInSentence
        } else {
          trimmedFillerKeywordSentence = trimmedFillerKeywordSentence.replace(keyword, "").replaceAll("\\s+", " ")
        }
      }

      fillerKeywordsInSentence = fillerKeywordsInSentence.toSet.toList
      logger.info("fillerKeywordsInSentence: {}", fillerKeywordsInSentence)
      var fillerWithDegree = fillerKeywordsInSentence.map(x => (x.ProjectKeyword.toLowerCase, x.ProjectKeywordID, x.ProjectKeyword.split(" ").length))
      fillerWithDegree = fillerWithDegree.sortBy(x => x._3).reverse
      
      for (j <- 0 until fillerWithDegree.length) {
        if (trimmedFillerKeywordSentence.contains(fillerWithDegree(j)._1)) {

          fllrKeyWordSet += fillerWithDegree(j)._1
          trimmedFillerKeywordSentence = trimmedFillerKeywordSentence.replace(fillerWithDegree(j)._1, "").replaceAll("\\s+", " ")
          fillerFreeText = fillerFreeText.replace(fillerWithDegree(j)._1, "").replaceAll("\\s+", " ")
        }
      }
      (fllrKeyWordSet, fillerFreeText.trim)
    } catch {

      case e: Exception =>
        e.printStackTrace()
        throw new KeywordIdentificationException("Filler keyword Exception")
    }

  }

  def findKeywordsInSntnce(sessionID: String, MessageText: String, activateKU: Boolean, activateIntent: Boolean, keywords: Set[String])(implicit session: JdbcBackend#SessionDef): Set[String] = { //: Set[String] = db withSession { implicit session =>

    import profile.simple._
    var keyWordSet = keywords

    try {
      var trimmedKeywordSentence = MessageText.trim
      var result = MessageText.trim.split("\\s+")
      var keywordsInSentence: List[TKeyword] = Nil

      //This for is to fetch available intents in sentence
      for (keyword <- result) {
        val keywordRecordList = Keyword.filter { x => (x.Keyword.toLowerCase.trim like ("%" + keyword.toLowerCase() + "%")) && x.Polarity === "P" }.list
        if (keywordRecordList.nonEmpty) {
          keywordsInSentence = keywordRecordList ::: keywordsInSentence
        } else {
          trimmedKeywordSentence = trimmedKeywordSentence.replace(keyword, "").replaceAll("\\s+", " ")
        }
      }

      keywordsInSentence = keywordsInSentence.toSet.toList
      logger.info("keywordsInSentence: {}", keywordsInSentence)
      var intentsWithDegree = keywordsInSentence.map(x => (x.IntentID, x.Keyword.toLowerCase.trim, x.KeywordID, x.Polarity, x.Keyword.split(" ").length))

      if (activateKU) {
        val enabledKUIDs = knowledgeUnit.filter(x => x.ActiveIND === "Y").map(x => x.KUID)
        val enabledKUIDints = Intent.filter(x => x.KUID in enabledKUIDs).map(x => x.IntentID).list
        intentsWithDegree = intentsWithDegree.filter(p => enabledKUIDints.contains(p._1))
      }

      if (activateIntent) {

        val eanbleIntents = Intent.filter(x => x.ActiveIND === "Y").map(x => x.IntentID).list
        intentsWithDegree = intentsWithDegree.filter(p => eanbleIntents.contains(p._1))

      }
      intentsWithDegree = intentsWithDegree.sortBy(x => x._5).reverse
      logger.info("intentsWithDegree: {}", intentsWithDegree)
      //based degree it is going to validate the high degree intent id and then trimming that intent from sentence

      for (j <- 0 until intentsWithDegree.length) {
        if (trimmedKeywordSentence.contains(intentsWithDegree(j)._2)) {

          keyWordSet += intentsWithDegree(j)._2
          logger.info("keyWordSet with value: {}", keyWordSet)
          trimmedKeywordSentence = trimmedKeywordSentence.replace(intentsWithDegree(j)._2, "").replaceAll("\\s+", " ")
          logger.info("trimmedKeywordSentence: {}", trimmedKeywordSentence)
        }
      }
      keyWordSet
    } catch {

      case e: Exception =>
        e.printStackTrace()
        throw new KeywordIdentificationException("Intent keyword Exception")
    }
  }
  
  
  
  def listMoreThanOneTrnsactIntent(userIntentIDs: List[Long])(implicit session: JdbcBackend#SessionDef):List[Long] = {//: List[Long] = db withSession { implicit request =>
 import profile.simple._
    //checking for greeting intents
    val greetingKUID = knowledgeUnit.filter(x => x.IsRankable === _N).map(x => x.KUID)
    val intent = Intent.filter(x => x.KUID in greetingKUID).map(x => x.IntentID).list

    val transactionIntents = userIntentIDs.filter(!intent.contains(_)).toList
    transactionIntents

  }
  

  def checkForKeywords(chkIntentObj: intentChkObj): JsValue = db withSession { implicit request =>
    import profile.simple._

    var response: JsValue = new successCodeObj("success").toJson
    var intentName = ""
    
    try {
      
      val keywordsArrayObj = chkIntentObj.intent.keywords
      val intentName = chkIntentObj.intent.name.toLowerCase
     logger.info("intentName: {}", intentName)
      val currentIntentID = Intent.filter{x => x.IntentID === chkIntentObj.intent.intentId.getOrElse(-1L) }.map(x => x.IntentID).firstOption.getOrElse(-1L)
       logger.info("currentIntentID: {}", currentIntentID)
      
      var positiveKeywords: List[String] = Nil
      val reqPositiveKeywords = keywordsArrayObj.filter(x => x.polarity == "P").map(x => x.keywordField.toLowerCase()).toList
      positiveKeywords = reqPositiveKeywords
      
      val intentNamesAsKeywords = chkIntentObj.intent.names.map(x => x.name.toLowerCase().trim)
      logger.info("intentNamesAsKeywords: {} ", intentNamesAsKeywords)
      
      intentNamesAsKeywords.foreach(x =>
        if (!positiveKeywords.contains(x)) {
          positiveKeywords = x :: positiveKeywords
        })
      
      val negitiveKeywords = keywordsArrayObj.filter(x => x.polarity == "N").map(x => x.keywordField.toLowerCase())

      val sortedPositiveKwywords = collection.immutable.SortedSet[String]() ++ positiveKeywords
      val sortedNegitiveKwywords = collection.immutable.SortedSet[String]() ++ negitiveKeywords
      //val sortedKwywords = collection.immutable.SortedSet[String]() ++ keywords

      val posKeywordString = sortedPositiveKwywords.mkString(",").trim()
      val negKeywordString = sortedNegitiveKwywords.mkString(",").trim()

      logger.info("posKeywordString: {} ", posKeywordString)
      logger.info("negKeywordString: {} ", negKeywordString)

      val poskeywordRecords = Keyword.filter(x => x.Polarity === "P").list
        .groupBy(x => x.IntentID)
        .map {
          case (intentID, group) =>
            (intentID, group.map(x => x.Keyword.toLowerCase()).sorted.mkString(",").trim())
        }.toList

      val negkeywordRecords = Keyword.filter(x => x.Polarity === "N").list
        .groupBy(x => x.IntentID)
        .map {
          case (intentID, group) =>
            (intentID, group.map(x => x.Keyword.toLowerCase()).sorted.mkString(",").trim())
        }.toList

      val isIntentWithPosKeywordExists = poskeywordRecords.filter(x => x._2 == posKeywordString).filterNot(x=> x._1 == currentIntentID)
      val isIntentWithNegKeywordExists = negkeywordRecords.filter(x => x._2 == negKeywordString).filterNot(x=> x._1 == currentIntentID)
      
       logger.info("isIntentWithPosKeywordExists: {}", isIntentWithPosKeywordExists)
        logger.info("isIntentWithNegKeywordExists: {}", isIntentWithNegKeywordExists)
      
      
      var isPositiveKwdsNotExist = true
      var isNegitiveKwdsNotExist = true

      if (posKeywordString != "" && isIntentWithPosKeywordExists.length > 0) {
        isPositiveKwdsNotExist = false
      }

      if (negKeywordString != "" && isIntentWithNegKeywordExists.length > 0) {
        isNegitiveKwdsNotExist = false
      }
      logger.info("isPositiveKwdsNotExist: {}", isPositiveKwdsNotExist)
      logger.info("isNegitiveKwdsNotExist: {}", isNegitiveKwdsNotExist)
      
      if (!(isPositiveKwdsNotExist && isNegitiveKwdsNotExist)) {
        
        val alreadyExistedIntID = isIntentWithPosKeywordExists.head._1
        
        val intentName = Intent.filter{x => x.IntentID === alreadyExistedIntID}.map{x => x.IntentDefinition}.firstOption.getOrElse("")
        
        val errorObj = new errorCodeObj("Keyword_Already_Exist", "Same set of Keywords is already mapped with "+intentName+ " intent")
        response = errorObj.toJson
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val errorObj = new errorCodeObj("Something_Went_Wrong", "Keyword processing failed because of internal failure.")
        response = errorObj.toJson
       // println(e.getMessage)
    }
     logger.info("response: {}", response)
    response
  }

}