package com.sce.dao

import com.sce.services._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.sce.models._
import spray.json._
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Random
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.models.TKeyword
import scala.util.control.Breaks._
import com.sce.utils.AppConf._
import com.sce.models._
import EntityTypeCodes._
import NLPErrorCodes._
import NLPStrings._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend

object EntityValueIdentificationDao extends DomainComponent with Profile {
  
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)
  
  val Keyword = KeywordTable.KeywordTable
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val workFlowSequenceTbl = WorkFlowSequence.WorkFlowSequence
  var keyWordSet: Set[String] = Set.empty
  
 

  def entityValuesForConv(sessionID: String, messagetext: String, intentID: Long)(implicit session: JdbcBackend#SessionDef) = {
    import profile.simple._
    try {
    logger.info("inside entityValuesForConv ")
      val entitiesInConv = workFlowSequenceTbl.filter(x => x.IntentID === intentID && x.EntryType === _ENTITY && x.InitialValidation === _Y).list.map(x => x.EntryExpression)
      val convCacheRecs = conversationCacheTbl.filter(x => x.SessionID === sessionID && x.EntryType === _ENTITY && (x.EntryID inSet entitiesInConv)).list
      if (convCacheRecs.nonEmpty) {


        var trimmedMsgtext = messagetext
        val puncMesgText = messagetext.split("\\s+").map(_.replaceAll("^[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]|[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]$", "")).mkString(" ")
        val keywords = IntentIdentificationDao.findKeywordsInSntnce(sessionID, puncMesgText, true, true, Set.empty).toList

        for (k <- 0 until keywords.length) {
          trimmedMsgtext = trimmedMsgtext.replace(keywords(k), "")
        }

        trimmedMsgtext = trimmedMsgtext.trim().replaceAll("\\s+", " ")
        logger.info("user text for entity values: {}", trimmedMsgtext)
        if (trimmedMsgtext != "") {
          for (i <- 0 until entitiesInConv.length) {

            val entityValue = RegexValidationDao.entityValuesInInitialSntnce(intentID, entitiesInConv(i).toLong, trimmedMsgtext)
            logger.info("entityValue: {}",entityValue)
            if (entityValue != "") {
              
              conversationCacheTbl.filter(x => x.SessionID === sessionID && x.EntryType === _ENTITY && x.EntryID === entitiesInConv(i)).map(x => (x.CacheData, x.FullFilled)).update(entityValue, Some("Y"))
              trimmedMsgtext = trimmedMsgtext.replace(entityValue, "").trim().replaceAll("\\s+", " ")
            }
          }

        }
        logger.info("trimmedMsgtext for converstaion values: {}", trimmedMsgtext)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

}








