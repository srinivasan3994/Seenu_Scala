package com.sce.dao
import com.sce.models._
import akka.event.LoggingAdapter
import spray.json._
import scala.slick.driver.JdbcProfile
import com.sce.utils._

import com.sce.utils.AppConf._
import com.sce.models._
import com.sce.models._
import com.sce.models._
import com.sce.models.NLPStrings._

import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend

object UserLanguageDao extends NlpJsonSupport with NLGJsonSupport with DomainComponent with Profile  {

  override val profile: JdbcProfile = SlickDBDriver.getDriver

  val imalLogs = IMALLogs.IMALLogs
  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)

  val knowledgeUnit = KnowledgeUnit.KnowledgeUnit
  val intentTable = IntentTable.IntentTable
  val keywordTable = KeywordTable.KeywordTable
  val entityRecord = Entity.Entity
  val entityQuestions = EntityQuestions.EntityQuestions
  val entityType = EntityType.EntityType
  val regexTable = Regex.Regex
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val userRating = UserRating.UserRating
  val userAttachemntsTbl = UserAttachemnts.UserAttachemnts
  val localeUnicodesTbl = LocaleUnicodes.LocaleUnicodes
  val localeTbl = Locale.Locale
  val userLocaleTbl = UserLocale.UserLocale
  val conversationPointerTbl = ConversationPointer.ConversationPointer

  val imSessionDao = IMSessionDao
  val actionNlpDao = ActionNlpDao
  
  def getLocales(keywordText: String)(implicit session: JdbcBackend#SessionDef): List[String] = {
    import profile.simple._

    var langaugesDetected: List[String] = Nil
    if (keywordText.nonEmpty) {
      val textChars = keywordText.toCharArray().map(_.toInt).toList
      logger.info("textChars:  {}",textChars)// transfer ^%^&* -> List(54,767,3,6,8,3,89098,34)
      langaugesDetected = localeUnicodesTbl.list.filter(getLangForUnicodes(_, textChars)).map(x => x.LocaleCode).toSet.toList

      logger.info("langaugesDetected: {}", langaugesDetected)
    }
    langaugesDetected
  }
  
  def getAllLocales()(implicit session: JdbcBackend#SessionDef):List[String] = {
    import profile.simple._

    var langaugesDetected: List[String] = Nil
    
    langaugesDetected = localeTbl.map(x => x.LocaleCode).list
    
    if (langaugesDetected.nonEmpty) {
      
      langaugesDetected = List(DEFAULT_LANG)

      
    }
    logger.info("langaugesDetected: {}", langaugesDetected)
    langaugesDetected
  }
  
   def getLocaleRec(localeCode: String)(implicit session: JdbcBackend#SessionDef):Option[TLocale] = {
    import profile.simple._

   localeTbl.filter(x => x.LocaleCode === localeCode).firstOption
  } 
  
  def getLangForUnicodes(UnicodeRec: TLocaleUnicodes, unicodeChars: List[Int])(implicit session: JdbcBackend#SessionDef):Boolean = {
    import profile.simple._

    for (i <- 0 until unicodeChars.length) {

      if (unicodeChars(i) >= UnicodeRec.HeadUnicode && unicodeChars(i) <= UnicodeRec.TailUnicode) {
        logger.info("**********language detected*********")
        return true

      }

    }
    return false
  }
  
  def getUserPreferredLocale(userID: String, channelID: Long):String = db withSession { implicit session =>
    import profile.simple._
    var userPreferredLocale = DEFAULT_LANG
    
    try{
    
    val userLocaleRec = userLocaleTbl.filter(x => x.UserID === userID && x.ChannelID === channelID).map(x => x.LocaleCode).firstOption
    
    if(userLocaleRec.nonEmpty){
     userPreferredLocale = userLocaleRec.get
    }
    }catch {
      case e:Exception => logger.info("Error: {}", e.getMessage)
    }
    userPreferredLocale
  }

  def setUserPreferredLocale(userID: String, LocaleCode: String, channelID: Option[Long]) (implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._
    logger.info("language for user: {}", userID)
    logger.info("LocaleCode for user: {}", LocaleCode)

    val userLocaleRecord = userLocaleTbl.filter(x => x.UserID === userID  && x.ChannelID === channelID).list

    if (userLocaleRecord.isEmpty) {

      userLocaleTbl.map(x => (x.UserID, x.LocaleCode, x.ChannelID)).insert((userID, LocaleCode, channelID))
    } else {

      userLocaleTbl.filter(x => x.UserID === userID && x.ChannelID === channelID).map(x => x.LocaleCode).update(LocaleCode)
    }
  }
  

  def getSessionLocale(messageText: String)(implicit session: JdbcBackend#SessionDef):String = {
    import profile.simple._

    var result = "en"
    try {
      val textChars = messageText.toCharArray().map(_.toInt).toList

      val langaugesDetected = localeUnicodesTbl.list.filter(getLangForUnicodes(_, textChars)).map(x => x.LocaleCode)

      if (langaugesDetected.length == 1) {
        result = langaugesDetected.head
      }
    } catch {
      case e: Exception => logger.info("Error While getting user Locale")
    }
    result
  }

  

  def isUserLocale(langCode: String)(implicit session: JdbcBackend#SessionDef):Boolean = {
    import profile.simple._
    var langFound = false
    try {

      val langRecords = localeTbl.filter(x => x.LocaleCode === langCode).list
      
      if(langRecords.nonEmpty){
        langFound = true
      }
      
      
    } catch {
      case e: Exception => logger.info("Error While getting user Locale")
    }
    langFound
  }
  
  
  def langNumReplacement(numericalString: String, LocaleCode: String)(implicit session: JdbcBackend#SessionDef): String = {

    import profile.simple._
    
    var numReplcmntString = numericalString
    
    val langUnicodeRec = localeUnicodesTbl.filter(x => x.LocaleCode === LocaleCode).firstOption
    if(langUnicodeRec.nonEmpty){
      if(langUnicodeRec.get.HeadNumUnicode.nonEmpty && langUnicodeRec.get.TailNumUnicode.nonEmpty ){
        val charArry = numericalString.toCharArray().map(replaceNumericals(_, langUnicodeRec.get.HeadNumUnicode.get, langUnicodeRec.get.TailNumUnicode.get))
       numReplcmntString = String.valueOf(charArry)
      }
    }
    numReplcmntString
  }
  
  def replaceNumericals(character: Char, headNumCode: Long, tailNumCode: Long)(implicit session: JdbcBackend#SessionDef):Char = {
    import profile.simple._
    var num = character
    try {
      val charUnicode = character.toInt
      if (charUnicode >= headNumCode && charUnicode <= tailNumCode) {
        println("charUnicode: " + charUnicode)
        val enNumber = charUnicode - headNumCode

        num = enNumber.toString().charAt(0)

      }
    } catch {
      case e: Exception =>
    }
    println("num: " + num)
    return num
  }
  
  
   def raiseLocaleConfirmation(langCodeString: String, msgEvent: NlpReqCommonObj, sessionID: String, keywordString: String)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._
    try {

      //ConversationDao.insertConversationRecord(sessionID, None, Some(SCSUtils.getCurrentDateTime), None)
      
      var LocaleQuickReply: List[NLPQuickReply] = Nil
      val userLang = msgEvent.platformDtls.userLang
      val langCodes = langCodeString.split("~~~~")
      val langRecords = localeTbl.filter(x => x.LocaleCode inSet langCodes).list
      logger.info("langRecords: {}", langRecords)
      var cnfrmHeadingText = langRecords.filter(x=> x.LocaleCode == userLang).map(x => x.LocaleCnfrmMsg.getOrElse("")).mkString("\n")
      
      for (i <- 0 until langRecords.length) {
        LocaleQuickReply = NLPQuickReply(
          contentType = "text",
          title = langRecords(i).LocaleName,
          payload = langRecords(i).LocaleCode) :: LocaleQuickReply
      }
      
      logger.info("quickReplies 	:		{}", LocaleQuickReply)
      val payload = getLocaleConfirmationTemplate(msgEvent.platformDtls.userID, cnfrmHeadingText, LocaleQuickReply).toJson.toString()
      logger.info("payload 	:		{}", payload)
      //creating conversation pointer for language confirmation
      conversationPointerTbl.map(x =>(x.SessionID, x.PointerType, x.PointerDesc, x.SourceID, x.isPointed, x.TempCache)).insert(
          sessionID, _CONFIRMATION, Some(LANGUAGE), None, _Y, Some(msgEvent.msgDtls.msgTxtWithoutPunc))
      actionNlpDao.sendFinalNlg(sessionID, msgEvent, new nlgResponseObj(None, None, None, None, Some(payload)), None, None)
    } catch {

      case e: Exception => e.printStackTrace()
    }
  }

  def getLocaleConfirmationTemplate(sender: String, text: String, facebookQuickReplyList: List[NLPQuickReply]): NLPQuickReplyMessage = {
    NLPQuickReplyMessage(
      text = text,
      facebookQuickReplyList)
  }

}