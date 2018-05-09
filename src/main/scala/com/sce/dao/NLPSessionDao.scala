package com.sce.dao

import com.sce.services._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.sce.models._
import spray.json._
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.models.TFlowChartSession

import com.sce.utils.AppConf._
import com.sce.models.NLPStrings._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import com.sce.exception.SCSException
import java.io.File
import scala.util.Try

object NLPSessionDao extends DomainComponent with Profile with NlpJsonSupport{
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val imalLogs = IMALLogs.IMALLogs
  val Keyword = KeywordTable.KeywordTable
  val Intent = IntentTable.IntentTable
  val entityTbl = Entity.Entity
  val entityType = EntityType.EntityType
  val imalSessionTbl = IMALSession.IMALSession
  val imLogsTbl = IMALLogs.IMALLogs
  val conversationTbl = Conversation.Conversation
  val actionTbl = ActionTable.ActionTable
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val workFlowSequenceTbl = WorkFlowSequence.WorkFlowSequence
  val userAttachemntsTbl = UserAttachemnts.UserAttachemnts
  
  val logger = Logging(system, this.getClass)

  def nlpCacheCreation(SessionID: String, IntentID: Long) = db withSession { implicit session =>
    import profile.simple._

    try {
      logger.info("inside nlp cache creation IntentID: {}", IntentID)
      
      val wrkFlowSeqRecords = workFlowSequenceTbl.filter(x => x.IntentID === IntentID).list
      if (wrkFlowSeqRecords.nonEmpty) {

        logger.info("wrkflwSeqID: {}", wrkFlowSeqRecords.length)

        val wrkflwSeqID = wrkFlowSeqRecords.filter(x => x.TerminalType == Some(_START)).map(x => x.WorkFlowSeqID).headOption
        if (wrkflwSeqID.nonEmpty) {

          //pointer for intent with work flow
          conversationPointerTbl.map(x => (x.SessionID, x.PointerType, x.PointerDesc, x.SourceID, x.isPointed)).insert(SessionID, _WORKFLOW, None, wrkflwSeqID, _Y)
          //pointer for Confirmation Type
          conversationPointerTbl.map(x => (x.SessionID, x.PointerType, x.PointerDesc, x.SourceID, x.isPointed)).insert(SessionID, _CONFIRMATION, None, None, _N)

          val cacheEntries = wrkFlowSeqRecords.filter(x => x.EntryType == ACTION || x.EntryType == _ENTITY).map(x => (x.EntryType, x.EntryExpression)).toSet.toList
          logger.info("cacheEntries: {}", cacheEntries)
          for (k <- 0 until cacheEntries.length) {
            conversationCacheTbl.map(x => (x.SessionID, x.EntryType, x.EntryID, x.IntentID, x.CacheData)).insert((SessionID, cacheEntries(k)._1, cacheEntries(k)._2, IntentID, ""))
          }

        }else{
          throw new SCSException("Error in workflowSeq Configuration")
        }

      }else{
        throw new SCSException("Error in workflowSeq Configuration")
      }
    } catch {
      case scs:SCSException =>
        throw scs
      case e: Exception => e.printStackTrace()
    }
  }

  

  def nlpCacheDeletion(sessionID: String) = db withSession { implicit session =>
    import profile.simple._
    try {

      val conversationID = ConversationDao.getConversationID(sessionID)
      conversationTbl.filter(x => x.ConversationID === conversationID).map(x => x.EndTime).update(Some(SCSUtils.getCurrentDateTime))
      conversationPointerTbl.filter(x => x.SessionID === sessionID).delete
      conversationCacheTbl.filter(x => x.SessionID === sessionID).delete
      deleteAtcmtsForSession(List(sessionID))
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def deleteAtcmtsForSession(sessionIDs: List[String]) = db withSession { implicit session =>
    import profile.simple._
    try {
      
      logger.info("---------------------------------delete attachments----------------------------------")
      val imLogIDs = imLogsTbl.filter(x => x.SessionID inSet sessionIDs).map(x => x.LogID)
      val atcmtRecs = userAttachemntsTbl.filter(x => x.IMLogID in imLogIDs).list
     
      for(j <- 0 until atcmtRecs.length){
        val destFile = new File(atcmtRecs(j).AttachmentName)
        logger.info("------------Deleting File: {}-----------", destFile)
        Try(destFile.delete())
      }
      userAttachemntsTbl.filter(x => x.IMLogID in imLogIDs).map(x => x.IsDeleted).update(Some(_Y))

    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def deleteSession(sessionIDs: List[String]) = db withSession { implicit request =>
    import profile.simple._
    try {
      if (sessionIDs.length > 0) {
        conversationTbl.filter(x => (x.SessionID inSet sessionIDs) && x.EndTime.getOrElse("") === "").map(x => x.EndTime).update(Some(new java.sql.Timestamp(new java.util.Date().getTime).toString()))
        conversationPointerTbl.filter(x => x.SessionID inSet sessionIDs).delete
        conversationCacheTbl.filter(x => x.SessionID inSet sessionIDs).delete
        deleteAtcmtsForSession(sessionIDs)

      }
    } catch { case e: Exception => e.printStackTrace() }
  }
}