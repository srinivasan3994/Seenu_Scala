package com.sce.dao

import com.sce.models._
import java.sql.Date
import scala.concurrent.Future
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.google.inject.Inject
import com.sce.utils.AppConf._
import com.sce.models._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend

object ConversationDao extends DomainComponent with Profile {
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)

  val imalLogs = IMALLogs.IMALLogs
  val userMapping = UserMapping.UserMapping
  val conversationTbl = Conversation.Conversation
  val userAttachemntsTbl = UserAttachemnts.UserAttachemnts
  val imalsession = IMALSession.IMALSession



  def insertConversationRecord(sessionID: String, intentID: Option[Long], startTime: Option[String], endTime: Option[String])(implicit session: JdbcBackend#SessionDef): Long = {
    import profile.simple._
    
    conversationTbl.map(x => (x.SessionID, x.IntentID, x.StartTime, x.EndTime)) += (sessionID, intentID, startTime, None)
    conversationTbl.filter(x => x.SessionID === sessionID).map(x => x.ConversationID).max.run.getOrElse(0L)
    
  }
  
  def updtConvIntentID(sessionID: String, intentID: Option[Long])(implicit session: JdbcBackend#SessionDef) = { 
    import profile.simple._
    logger.info("--------------------Updating ConversationID------------------------")
    ConversationDao.getConversationID(sessionID)
    conversationTbl.filter(x => x.SessionID === sessionID).map(x => x.IntentID).update(intentID)
  }

  def upsertConvRecord(sessionID: String, intentID: Option[Long], startTime: Option[String], endTime: Option[String])(implicit session: JdbcBackend#SessionDef) = { 
    import profile.simple._

    logger.info("Conversation Rec: {}" + sessionID + " \n" + intentID + " \n" + startTime + " \n" + endTime)

    val convID = conversationTbl.filter(x => x.SessionID === sessionID).map(x => x.ConversationID).max.getOrElse(-1L) 
    val convRec = conversationTbl.filter(x => x.ConversationID === convID).firstOption

    if (convRec.nonEmpty) {

      if (convRec.get.EndTime == None) {
        conversationTbl.filter(x => x.ConversationID === convRec.get.ConversationID).map(x => x.IntentID).update(intentID)
      } else {
        conversationTbl.map(x => (x.SessionID, x.IntentID, x.StartTime, x.EndTime)) += (sessionID, intentID, startTime, None)
      }
    } else {
      conversationTbl.map(x => (x.SessionID, x.IntentID, x.StartTime, x.EndTime)) += (sessionID, intentID, startTime, None)
    }
  }
  
  def getAtcmtsForConvID(convID: Long)(implicit session: JdbcBackend#SessionDef): List[TUserAttachemnts] = db withSession {implicit session =>
    
    import profile.simple._
    
    val imLogIds = imalLogs.filter(x => x.ConversationID === convID).map(x => x.LogID)
    
     userAttachemntsTbl.filter(x => x.IMLogID in imLogIds).list
    
    
  } 

  
  def getConversationID(sessionID: String): Long = db withSession{ implicit session =>
    import profile.simple._

    var convID = conversationTbl.filter(x => x.SessionID === sessionID).map(x => x.ConversationID).max.run.getOrElse(-1L)
    if(convID == -1L){
     logger.info("**************************************************************************************Invalid configuration High Alert Please go through conversation logs*********************************************************")
    convID = ConversationDao.insertConversationRecord(sessionID, None, Some(SCSUtils.getCurrentDateTime), None)
    
    }
    
    
    convID
  }

  def imLogsForConversation(conversationID: Long)(implicit session: JdbcBackend#SessionDef): List[Long] = {
    import profile.simple._

    imalLogs.filter(x => x.ConversationID === conversationID).map { x => x.LogID }.list

  }

  def getConversationAttachments(sessionID: String)(implicit session: JdbcBackend#SessionDef): List[TUserAttachemnts] = {
    import profile.simple._

    val conversationID = conversationTbl.filter(x => x.SessionID === sessionID).map(x => x.ConversationID).max.run.getOrElse(-1L)
    val logIDs = imalLogs.filter(x => x.ConversationID === conversationID).map { x => x.LogID }

    userAttachemntsTbl.filter(x => x.IMLogID in logIDs).list

  }

  def updateConversationEndTime(sessionID: String)(implicit session: JdbcBackend#SessionDef) = { 
    import profile.simple._
    val convID = getConversationID(sessionID)
    conversationTbl.filter(x => x.ConversationID === convID).map(x => x.EndTime).update(Some(new java.sql.Timestamp(new java.util.Date().getTime).toString()))

  }
}