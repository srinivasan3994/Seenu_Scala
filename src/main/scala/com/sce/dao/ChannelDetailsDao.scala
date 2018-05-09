package com.sce.dao

import com.sce.models._
import java.sql.Date
import java.util._
import scala.concurrent.Future
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import com.sce.utils._
import com.google.inject.Inject
import com.sce.utils.AppConf._

object ChannelDetailsDao extends DomainComponent with Profile {
  override val profile: JdbcProfile = SlickDBDriver.getDriver
  val db = new DBConnection(profile).dbObject()

  val platformTbl = Platform.Platform
  val channelTbl = Channel.Channel

  def getPlatformID(platformName: String): Long = db withSession { implicit session =>
    import profile.simple._

    platformTbl.filter(x => x.PlatformName.toLowerCase === platformName.toLowerCase).map(x => x.PlatformID).first
  }

  def getChannelRecord(platformID: Long): TChannel = db withSession { implicit session =>
    import profile.simple._

    channelTbl.filter(x => x.PlatformID === platformID).first

  }
  
  def getPlatformDesc(platformID: Long): String = db withSession { implicit session =>
    import profile.simple._

    platformTbl.filter(x => x.PlatformID === platformID).map(x => x.PlatformName.toLowerCase.trim).first
  }
  
   def getPlatformIDFromChannelID(channelID: Long) : Long= db withSession { implicit session =>
   import profile.simple._

   channelTbl.filter(x => x.ChannelId === channelID).map(x => x.PlatformID)first
   
   
  }
   
   def updatePuristAccessToken(token:String, channelID: Long) = db withSession { implicit session =>
    import profile.simple._

    channelTbl.filter(x => x.ChannelId === channelID).map(x => x.AccessToken).update(Some(token))

  } 

}