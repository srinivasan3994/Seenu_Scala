/*package com.sce.apis.banknet

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.json.lenses.JsonLenses._

case class AccessToken(access_token:String,token_type:String,expires_in:Long,scope:String)
case class AuthCode(authCode:String)

trait BankNetServiceJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  
  implicit val accessTokenJsonFormat = jsonFormat4(AccessToken)
  implicit val AuthCodeJsonFormat = jsonFormat1(AuthCode)
  
}

*/