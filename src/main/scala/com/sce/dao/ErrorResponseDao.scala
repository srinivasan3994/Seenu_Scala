package com.sce.dao

import com.sce.models._
import java.sql.Date
import java.util._
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.google.inject.Inject
import com.typesafe.config.Config
import akka.event.LoggingAdapter
import scala.util.control.Breaks
import com.jayway.jsonpath.JsonPath
import com.sce.utils.AppConf._
import com.sce.utils.AppUtils.system
import com.sce.utils.AppUtils.fm
import akka.event.{ Logging }
import com.sce.utils.AppUtils._

object ErrorResponseDao extends DomainComponent with Profile {
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val logger = Logging(system, this.getClass)

  val imalLogs = IMALLogs.IMALLogs
  val errorResponse = ErrorResponse.ErrorResponse
  val actionErrorResponse = ActionErrorResponse.ActionErrorResponse
  val db = new DBConnection(profile).dbObject()

  
  val ERROR_LOG = config.getString("FB_ERROR_LOG")

  def errorResponseParser(jsonObject: String, errorNode: String, actionId: Long, userLanguage: String): String = {

    logger.info("jsonObject : {}", jsonObject)
    logger.info("Error Node : {}", errorNode)
    var parsedSentence = ERROR_LOG

    try {

      if (chkActionHasErrResp(actionId, userLanguage)) {
        parsedSentence = getErrorDescriptionByActionID(actionId)
      } else {

        val jsString = config.getString("js_path_finder").r
        val document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(jsonObject);
        parsedSentence = errorNode
        logger.info("errorResponseParser document : {}", document)
        val jsVariablesInSentnce = jsString.findFirstIn(parsedSentence).map(x => x.toString.substring(1, x.toString.length - 1)).getOrElse("")
        logger.info("jsVariablesInSentnce : {}", jsVariablesInSentnce)
        if (jsVariablesInSentnce.length() > 0) {
          val errorCode: String = JsonPath.read(document, jsVariablesInSentnce).toString()
          logger.info("errorCode : {}", errorCode)
          parsedSentence = getErrorDescription(errorCode, actionId, userLanguage)
          logger.info("parsedSentence : {}", parsedSentence)
        }
      }
      logger.info("return errorCode : {}", parsedSentence)
      parsedSentence
    } catch {
      case e: Exception =>
        e.printStackTrace()
        ERROR_LOG
    }
  }

  def chkActionHasErrResp(actionID: Long, userLanguage: String): Boolean = db withSession { implicit session =>
    import profile.simple._
    val actionErrList = actionErrorResponse.filter { x => x.ActionID === actionID && x.LocaleCode === userLanguage }.list

    logger.info("intentIDLst : {}", actionErrList)
    logger.info("intentIDLst.length : {}", actionErrList.length)

    var flag: Boolean = false;

    if (actionErrList.length != 0) {
      flag = true;
    } else {
      flag = false;
    }
    logger.info("chkActionHasErrResp Status: {}", flag)
    return flag
  }

  def getErrorDescription(errorCode: String, ActionID: Long, userLanguage: String): String = db withSession { implicit session =>
    import profile.simple._

    var errorDescription = ""
    val errorDescRecords = errorResponse.filter { x => x.ErrorCode === errorCode && x.ActionID === ActionID && x.LocaleCode === userLanguage }.map { x => x.ErrorResponse }.list
    if (errorDescRecords.length > 0) {
      val rand = randomizer.nextInt(errorDescRecords.size)
      errorDescription = errorDescRecords.lift(rand).getOrElse("")
    }
    return errorDescription
  }

  val randomizer = new Random();

  def getErrorDescription(errorCode: String, localeCode: String): String = db withSession { implicit session =>
    import profile.simple._

    var errorDescription = ""
    var errorDescRecords = errorResponse.filter { x => x.ErrorCode === errorCode && x.LocaleCode === localeCode }.map { x => x.ErrorResponse }.list
    if (errorDescRecords.isEmpty) {
      errorDescRecords = errorResponse.filter { x => x.ErrorCode === errorCode }.map { x => x.ErrorResponse }.list
    }
    if (errorDescRecords.length != 0) {
      val rand = randomizer.nextInt(errorDescRecords.size)
      errorDescription = errorDescRecords.lift(rand).getOrElse("")
    }
    return errorDescription
  }

  def getErrorDescriptionByActionID(actionId: Long): String = db withSession { implicit session =>
    import profile.simple._

    val errorForAction = actionErrorResponse.filter { x => x.ActionID === actionId }.map(x => x.ActionErrorResponse).list //map{x => x.ActionErrorResponse}.firstOption.getOrElse("")
    var errorDescription = ""
    if (errorForAction.length != 0) {

      val rand = randomizer.nextInt(errorForAction.size)
      errorDescription = errorForAction.lift(rand).getOrElse("")
    }
    return errorDescription
  }
}