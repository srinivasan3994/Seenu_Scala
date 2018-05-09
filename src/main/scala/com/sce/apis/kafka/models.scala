/*package com.sce.apis.kafka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.json.lenses.JsonLenses._
import com.sce.models._

case class ConsumerRecipient(id: String)

case class ConsumerText(text: String)

case class Session(id: String)

case class ActionConsumerMessage(recipient: String,message: String, nlpConsumerMsg: NlpActionConsumerObj, actionResponse: String)

case class IMSendConsumer(msgEvent: NlpReqCommonObj, sessionID: String, intentID: Option[Long], nlgRespObj: nlgResponseObj)

case class ConsumerResponseMessage(sessionId: Session,recipient:ConsumerRecipient,message: ConsumerText)

case class ConsumerLogs(sessionId: String,message: String, messageSource: String, IntentID: Option[Long],conversationID :Option[Long] )

case class ConsumerErrorLogs(senderID:String,sessionId: Option[String],message: String, messageSource: Option[String], IntentID: Option[Long],conversationID :Option[Long] )

trait KafkaJsonSupport extends DefaultJsonProtocol with NlpJsonSupport with SprayJsonSupport {
  
  implicit val ActionConsumerMessageJsonFormat = jsonFormat4(ActionConsumerMessage)
  implicit val ConsumerRecipientJsonFormat = jsonFormat1(ConsumerRecipient)
  implicit val ConsumerTextJsonFormat = jsonFormat1(ConsumerText)
  implicit val SessionJsonFormat = jsonFormat1(Session)
  implicit val IMSendConsumerJsonFormat = jsonFormat4(IMSendConsumer)
  implicit val ConsumerResponseMessageJsonFormat = jsonFormat3(ConsumerResponseMessage)
  implicit val ConsumerLogsJsonFormat = jsonFormat5(ConsumerLogs)
  implicit val ConsumerErrorLogsJsonFormat = jsonFormat6(ConsumerErrorLogs)
}*/