package com.sce.dao

import com.sce.models._
import java.sql.Date
import java.util._
import scala.concurrent.Future
import com.google.inject.Inject
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import scala.slick.jdbc.JdbcBackend

object UserMappingDao extends DomainComponent with Profile {

  override val profile: JdbcProfile = SlickDBDriver.getDriver

  val db = new DBConnection(profile).dbObject()
  val logger = Logging(system, this.getClass)
  val imalLogs = IMALLogs.IMALLogs
  val actionAuthorization = ActionAuthorization.ActionAuthorization

  val userMapping = UserMapping.UserMapping

  def getAccessCode(userId: String)(implicit session: JdbcBackend#SessionDef): String = {
    import profile.simple._
    val accessCode = userMapping.filter { x => x.IM_UserID === userId }.map { x => x.Backend_AccessCode }.firstOption.getOrElse("")
    println("accessCode: " + accessCode)
    return accessCode
  }

  def getProjectAccessCode(userId: String, channelID: Long)(implicit session: JdbcBackend#SessionDef): String = {
    import profile.simple._

    val accessCode = userMapping.filter { x => x.IM_UserID === userId && x.ChannelID === channelID }.map { x => x.Backend_AccessCode }.firstOption.getOrElse("")
    println("accessCode: " + accessCode)
    return accessCode
  }



  def getAccessToken: Option[String] = db withSession { implicit session =>
    import profile.simple._
    actionAuthorization.map(x => x.AccessToken.getOrElse("")).firstOption

  }

}