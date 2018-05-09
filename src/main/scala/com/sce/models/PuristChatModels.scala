package com.sce.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class PuristReqModelObj(
  event:                 Option[String],
  conversation_id:       Option[Long],
  support_catagory_id:   Option[Long],
  support_catagory_name: Option[String],
  operator_id:           Option[Long],
  operator_name:         Option[String],
  end_customer_id:       Option[Long],
  end_customer_name:     Option[String],
  body:                  Option[String],
  file_url:              Option[String],
  sessionId:             Option[String])

case class nlpPuristChatMessage(body: String)

case class LoginAccessToken(
  access_token:  String,
  token_type:    Option[String],
  token_timeout: Option[Double],
  info:          Option[Info])
case class Info(
  `type`:            Option[String],
  email:             Option[String],
  username:          Option[String],
  name:              Option[String],
  chat_enabled:      Option[Boolean],
  ticketing_enabled: Option[Boolean],
  operator_id:       Option[Double])

trait PuristJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val PuristReqModelObjJsonFormat = jsonFormat11(PuristReqModelObj)
  implicit val nlpPuristChatMessageJsonFormat = jsonFormat1(nlpPuristChatMessage)
  implicit val infoJsonFormat = jsonFormat7(Info)
  implicit val loginAccessTokenJsonFormat = jsonFormat4(LoginAccessToken)
}
  
