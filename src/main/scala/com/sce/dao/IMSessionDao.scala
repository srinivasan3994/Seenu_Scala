package com.sce.dao

import com.sce.models._
import java.sql.Date
import scala.concurrent.Future
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.google.inject.Inject

import com.sce.utils.AppConf._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend

object IMSessionDao extends DomainComponent with Profile {
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val logger = Logging(system, this.getClass)
  val imalsession = IMALSession.IMALSession
  val imalLogs = IMALLogs.IMALLogs
  val imErrorLogs = IMErrorLogs.IMErrorLogs
  val userMappingTbl = UserMapping.UserMapping
  val channelTbl = Channel.Channel
  val userAttachemntsTbl = UserAttachemnts.UserAttachemnts

  def insertSession(sessionId: String, userId: String, startDate: String, reason: String, channelID: Long)(implicit session: JdbcBackend#SessionDef) = {//db withSession { implicit session =>
    import profile.simple._

    imalsession.map { x => (x.SessionID, x.IM_UserID, x.CreatedAT, x.ExpiredAT, x.Reason, x.ChannelID) } += (sessionId, userId, startDate.toString(), None, reason, Some(channelID))
    logger.info("New record inserted")

  }
  
  def insertUserAtcmts(imLogID: Long, attachmentName: String, attachmentType: String, channelID: Long, entityID: Long, isDeletedFlag: String)(implicit session: JdbcBackend#SessionDef): Long = {//db withSession { implicit session =>
    import profile.simple._
  
   userAttachemntsTbl.map(x => (x.IMLogID, x.AttachmentName, x.AttachmentType, x.ChannelID, x.EntityID, x.IsDeleted)) += ((imLogID, attachmentName, attachmentType, channelID, entityID, Some(isDeletedFlag)))
  }

  def insertIMErrorLogs(sessionId: Option[String], message: String, messageSource: Option[String], IntentID: Option[Long], conversationID: Option[Long]): Long = db withSession { implicit session =>
    import profile.simple._

    var convID = conversationID

    if (convID == None) {
      convID = Some(ConversationDao.getConversationID(sessionId.getOrElse("")))
    }

    imErrorLogs.map { x => (x.SessionID, x.Message, x.Source, x.IntentID, x.ConversationID) } += (sessionId, message, messageSource, IntentID, convID)

    val imErrorLogID = imErrorLogs.filter(x => x.SessionID === sessionId).map(x => x.ELogID).max.run.getOrElse(0L)
    println("imSessionLogID: " + imErrorLogID)
    imErrorLogID
  }

  def checkUserIMSession(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef): Boolean = {//db withSession { implicit session =>
    import profile.simple._
    var flag: Boolean = false;
    try {
      val userIDLst = imalsession.filter { x => x.IM_UserID === userId && x.ChannelID === channelID}.firstOption
      if (userIDLst.nonEmpty) {
        flag = true;
      }
    } catch {
      case e: Exception =>
        logger.info("Exception: {}", e.getMessage)
    }
    logger.info("checkUserIMSession: {}", flag)
    return flag
  }

  def checkIfUserIsRegistered(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef): Boolean = {//db withSession { implicit session =>
    import profile.simple._
    var flag: Boolean = false;
    try {
      val userIDLst = userMappingTbl.filter { x => x.IM_UserID === userId && x.ChannelID === channelID}.firstOption

      if (userIDLst.nonEmpty) {
        flag = true;
      }
    } catch {
      case e: Exception =>
        logger.info("Exception: {}", e.getMessage)
    }
    logger.info("checkIfUserIsRegistered: {}", flag)
    return flag
  }

  def insertUserMapEntry(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef) = {//db withSession { implicit session =>
    import profile.simple._
    userMappingTbl.map { x => (x.IM_UserID, x.Backend_AccessCode, x.ChannelID) } += (userId, "K+u0K8R52vMZErhD4Yt4h50nvbFQA0QMOrUOE2PcMgw=", Some(channelID))
  }

  def checkExpiredStatus(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef): String = {//db withSession { implicit session =>
    import profile.simple._

    val recentUserIMID = imalsession.filter { x => x.IM_UserID === userId && x.ChannelID === channelID }.map { x => x.IMSessionID }.max.run.getOrElse(-1L)

    val expiredStatus = imalsession.filter { x => x.IMSessionID === recentUserIMID }.map { x => x.ExpiredAT.getOrElse("") }.firstOption.getOrElse("")

    logger.info("Expired Status : {}", expiredStatus)

    return expiredStatus
  }

  def getSessionId(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef): String = {//db withSession { implicit session =>
    import profile.simple._

    val recentUserIMID = imalsession.filter { x => x.IM_UserID === userId && x.ChannelID === channelID  }.map { x => x.IMSessionID }.max.run.getOrElse(-1L)

    val sessionID = imalsession.filter { x => x.IMSessionID === recentUserIMID }.map { x => x.SessionID }.firstOption.getOrElse("")

    return sessionID
  }

  def resetSessionId(userId: String, sessionId: String) = db withSession { implicit session =>
    import profile.simple._
    imalsession.filter { x => x.IM_UserID === userId }.map { x => x.SessionID }.update(sessionId)

  }

  def updateExpiredDate(userId: String) = db withSession { implicit session =>
    import profile.simple._
    imalsession.filter { x => x.IM_UserID === userId }.map { x => x.ExpiredAT }.update(Some(DateTime.now().toString()))

  }

  def updateSessionId(userId: String, sessionId: String) = db withSession { implicit session =>
    import profile.simple._
    imalsession.filter { x => x.IM_UserID === userId }.map { x => x.SessionID }.update(sessionId)

  }

  def IsEndSessionTimeOut(/*timeBforeOneHour: String*/) = db withSession { implicit session =>
    import profile.simple._
    
    logger.info("################################## Scheduler Starting ###########################################")
    val timeBforeOneHour = new java.sql.Timestamp(new java.util.Date().getTime - 1000 * 60 * 60).toString()
    logger.info("Time Before One Hour: {}", timeBforeOneHour.toString())
    val sessionlst = imalLogs.filter { x => x.Created >= timeBforeOneHour.toString() }.map { x => x.SessionID }.list
    logger.info("Session List: {}", sessionlst.length)

    val sessionIDs = imalsession.filterNot { x => (x.SessionID inSet sessionlst) }.filter { x => x.ExpiredAT.getOrElse("") === "" }.map { x => x.SessionID }.list
    imalsession.filter { x => (x.SessionID inSet sessionIDs) }.map { x => (x.ExpiredAT, x.Reason) }.update(Some(SCSUtils.getCurrentDateTime), "User session has expired")
    NLPSessionDao.deleteSession(sessionIDs)

  }

  def getPlatformId(userId: String)(implicit session: JdbcBackend#SessionDef): Long = {//db withSession { implicit session =>
    import profile.simple._

    println("Hello")

    val platformID = userMappingTbl.filter { x => x.IM_UserID === userId }.map { x => x.ChannelID.getOrElse(0L) }.firstOption.getOrElse(0L)

    println("platformID", platformID)

    return platformID
  }

  def getChannelID(platformID: Long)(implicit session: JdbcBackend#SessionDef): Long = {//db withSession { implicit session =>
    import profile.simple._

    val channelID = channelTbl.filter { x => x.PlatformID === platformID }.map { x => x.ChannelId }.firstOption.getOrElse(0L)

    println("channelID: {}", channelID)

    return platformID
  }

  /* def insertIMALLogs(sessionId: String, message: String, messageSource: String, IntentID: Option[Long], conversationID: Option[Long]): Long = db withSession { implicit session =>
    import profile.simple._

    var convID = conversationID
    if (convID == None) {
      convID = Some(ConversationDao.getConversationID(sessionId))
    }
    var imSessionLogID = 0L
    this.synchronized {
      imSessionLogID = imalLogs.map(x => x.LogID).max.run.getOrElse(0L) + 1
      imalLogs.map { x => (x.LogID, x.SessionID, x.Message, x.Source, x.IntentID, x.ConversationID) } += (imSessionLogID, sessionId, message, messageSource, IntentID, convID)

      logger.info("imSessionLogID: {}" , imSessionLogID)
    }
    imSessionLogID
  }*/
  def insertIMALLogs(sessionId: String, message: String, messageSource: String, IntentID: Option[Long], conversationID: Option[Long]): Long = db withSession { implicit session =>
    import profile.simple._

    var convID = conversationID
    if (convID == None) {
      convID = Some(ConversationDao.getConversationID(sessionId))
    }

    imalLogs.map { x => (x.SessionID, x.Message, x.Source, x.IntentID, x.ConversationID) } += (sessionId, message, messageSource, IntentID, convID)
    var imSessionLogID = imalLogs.filter(x => x.SessionID === sessionId).map(x => x.LogID).max.run.getOrElse(0L)
    logger.info("imSessionLogID: {}", imSessionLogID)

    imSessionLogID
    
  }
  
   def getCurrentIMSessionLogID(sessionID: String)(implicit session: JdbcBackend#SessionDef):Long = {
    import profile.simple._
    
    val imlogID = imalLogs.filter(x => x.SessionID === sessionID).map(x => x.LogID).max.run.getOrElse(0L)
    logger.info("Recent Inserted IMSessionLogID: {}", imlogID)
    imlogID
  }
  
   def updtIMLogWithIntID(imLogID: Long, intentID: Option[Long])(implicit session: JdbcBackend#SessionDef) = {
     import profile.simple._
     imalLogs.filter(x => x.LogID === imLogID ).map(x => x.IntentID).update(intentID)
     
   }
  

}