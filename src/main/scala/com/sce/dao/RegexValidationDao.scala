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
import scala.util.control.Breaks
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.sce.models.NLPErrorCodes._
import com.sce.models.NLPStrings._
import scala.util.control.Exception.Catch

import com.sce.utils.AppConf._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

object RegexValidationDao extends DomainComponent with Profile {
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val imalLogs = IMALLogs.IMALLogs
  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)
  val Intent = IntentTable.IntentTable
  val entityRecord = Entity.Entity
  val entityQuestions = EntityQuestions.EntityQuestions
  val entityType = EntityType.EntityType
  val regexTable = Regex.Regex
  val entityRegex = EntityRegex.EntityRegex
  val errorResponseTbl = ErrorResponse.ErrorResponse
  val intentMappingTbl = IntentMapping.IntentMapping
  val conversationCacheTbl = ConversationCache.ConversationCache
  val conversationPointerTbl = ConversationPointer.ConversationPointer
  val regexExtn = RegexExtn.RegexExtn

  val errorResponseDao = ErrorResponseDao
  
   def validateForEntityID(entityID: Long, messagetext: String, userLang: String): (String, String) = db withSession { implicit session =>
    import profile.simple._
    val defaultIntentErrMsg = errorResponseDao.getErrorDescription(SCE_ENTITY_NOT_FOUND, userLang) //errorResponseTbl.filter(x => x.ErrorCode === SCE_ENTITY_NOT_FOUND).map( x => x.ErrorResponse).firstOption.getOrElse("")
    val loop = new Breaks;
    val entriesInMsgTxt = messagetext.split(" ")
    var errorMsg = ""
    val regexloop = new Breaks;
    var processedText = messagetext
    try {
      val regexIDs = entityRegex.filter { x => x.EntityID === entityID }.map { x => x.RegexID }.list
      val regexTblExpr = regexTable.filter { x => x.RegExID inSet regexIDs }.list
      val regexExtnTblRecs = regexExtn.filter { x => x.RegExID inSet regexIDs }.list
      
      
        for (i <- 0 until entriesInMsgTxt.length) {
          logger.info("Validating for message word: {}", entriesInMsgTxt)
          var isValidEntry = true
          regexloop.breakable(
            for (j <- 0 until regexIDs.length) {
              val regex = regexTblExpr.filter { x => x.RegExID == regexIDs(j) }.headOption
              logger.info("regex: {}", regex)
              if (regex.nonEmpty) {
                val regpat = regex.get.RegularExpression.r
                val hasEntityVal = regpat.findFirstIn(entriesInMsgTxt(i)).getOrElse("")
                if (hasEntityVal == "") {
                  isValidEntry = false
                  val regexID = regex.get.RegExID
                  val errorMessage = regexExtnTblRecs.filter { x => x.RegExID == regexID && x.LocaleCode == userLang }.map { x => x.ErrorMessage }.headOption.getOrElse("")
                  errorMsg = errorMessage.toString()
                  regexloop.break
                } else {
                  processedText = hasEntityVal
                }
              }
            })

          if (isValidEntry) {
            return ("", processedText)
          }
        }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    logger.info("(errorMsg, processedText): {}", (errorMsg, processedText))
    (errorMsg, processedText)
  }

  
  def entityValuesInInitialSntnce(intentID: Long, entityID: Long, messageText: String): String = db withSession { implicit session =>
    import profile.simple._

    val entriesInMsgTxt = messageText.split(" ")
    val regexIDs = entityRegex.filter { x => x.EntityID === entityID }.map { x => x.RegexID }.list
    val regexTblExpr = regexTable.filter { x => x.RegExID inSet regexIDs }.firstOption
    val regexloop = new Breaks;
    var processedText = ""
    for (i <- 0 until entriesInMsgTxt.length) {
      var isValidEntry = true

      regexloop.breakable(
        for (j <- 0 until regexIDs.length) {
          val regex = regexTblExpr.filter { x => x.RegExID == regexIDs(j) }.headOption
           logger.info("regularExpression: {}", regex)
          if (regex.nonEmpty) {
            val regpat = regex.get.RegularExpression.r
            val hasEntityVal = regpat.findFirstIn(entriesInMsgTxt(i)).getOrElse("")

            if (hasEntityVal == "") {
              isValidEntry = false
              regexloop.break
            } else {
              processedText = hasEntityVal

            }
          }
        })

      if (isValidEntry) {
        processedText
      }
    }
    logger.info("processedText: {}", processedText)
    processedText
  }

  
  // Code for fetching entity values from initial sentence. 
  //ex: i want to transfer it in next month. 
  //Result: I can fetch "next month" using this code. Apart from only "next" or "month" 
  def entityValueForConvCache(intentID: Long, entityID: Long, messageText: String): String = db withSession { implicit session =>
    import profile.simple._

    var processedText = ""
    val regexloop = new Breaks;
    println("----------------" + messageText)

    try {
      val regexIDs = entityRegex.filter { x => x.EntityID === entityID }.map { x => x.RegexID }.list
      val regexTblExpr = regexTable.filter { x => x.RegExID inSet regexIDs }.list
      var elementsForEntRegxs: List[String] = Nil
      var isFirstIteration = true

      regexloop.breakable(
        for (i <- 0 until regexTblExpr.length) {
          logger.info("regularExpression: {}", regexTblExpr(i).RegularExpression)
          if (isFirstIteration) {
            val regpat = regexTblExpr(i).RegularExpression.r
            elementsForEntRegxs = regpat.findAllIn(messageText).toList
            isFirstIteration = false
          } else {
            if (elementsForEntRegxs.nonEmpty) {
              elementsForEntRegxs = regexMtchItemsInList(regexTblExpr(i).RegularExpression, elementsForEntRegxs)
            } else {
              regexloop.break
            }
          }
        })
      processedText = elementsForEntRegxs.headOption.getOrElse("")
    } catch {
      case e: Exception =>
    }
    logger.info("processedText: {}", processedText)
    
    processedText
  }

  def regexMtchItemsInList(regex: String, elements: List[String]): List[String] = {

    var regexElements: List[String] = Nil
    try {
      val regpat = regex.r
      for (j <- 0 until elements.length) {
        val hasEntityVal = regpat.findFirstIn(elements(j)).getOrElse("")

        if (hasEntityVal != "") {
          regexElements = hasEntityVal :: regexElements
        }
      }
    } catch {
      case e: Exception =>
    }
    regexElements
  }

}