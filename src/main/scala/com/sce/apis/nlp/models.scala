/*package com.sce.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.json.lenses.JsonLenses._

case class NlpReqCommonObj(var platformDtls: PlatFormDtlsObj, var msgDtls: MessageDtlsObj)

case class PlatFormDtlsObj(var userID: String, var receiverID: String, var ExtnlConvID: Option[String], var platformID: Long)

case class NLPCommonAtcmtObj(var atcmtContent: String, var atcmtType: String)

case class MessageDtlsObj(var messageTxt: String, var messageType: String, var attachments: List[NLPCommonAtcmtObj], var msgTxtWithoutPunc: String)

case class NlpMessageDtlsObj(message: String, reqBody: Option[String], reqURL: Option[String], reqType: Option[String], replyMessage: Option[String], reqMethod: Option[String])

case class nlpExtrespobj(Operation: String, Error: String, result: String)

case class NlpIdentificationDtlsObj(msgEvent: NlpReqCommonObj, sessionID: String, recepientID: String, intentID: Long, actionId: Long, entityID: Option[Long], FlowChartID: Option[Long], workFlowSeqID: Option[Long], validateFlowchart: Boolean)

case class NlpActionConsumerObj(messageDtls: NlpMessageDtlsObj, identityDtls: NlpIdentificationDtlsObj)

case class NodeDataArrayLayout(key: Long, category: Option[String], loc: String, text: String, figure: Option[String])

case class LinkDataArrayLayout(from: Long, to: Long, fromPort: String, toPort: String, points: Option[Array[Double]], text: Option[String])

case class FlowChartLayout(`class`: String, linkFromPortIdProperty: String, linkToPortIdProperty: String, nodeDataArray: Array[NodeDataArrayLayout], linkDataArray: Array[LinkDataArrayLayout])

case class nlgResponseText(Sourct: String, ResponseText: String)

case class nlgResponseMessage(message: String, user_sessionid: Option[String])

case class keywordChkObj(id: Option[Long], keywordField: String, polarity: String, intent: Option[Long])

case class intentChkDtlsObj(name: String, kuId: Long, keywords: Array[keywordChkObj])

case class intentChkObj(intent: intentChkDtlsObj)

case class errorCodeObj(errorCode: String, errorDescription: String)

case class successCodeObj(data: String)

case class nlgResponseObj(var simpleMessage: Option[String], confirmationMessage: Option[String],
                          choiceListing: Option[String], imageChoiceListing: Option[String], intentChoices: Option[String])

trait NlpJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val nlpResponseObjJsonFormat = jsonFormat6(NlpMessageDtlsObj)
  implicit val nlpExtrespobjJsonFormat = jsonFormat3(nlpExtrespobj)
  implicit val nlpNodeDataArrayLayout = jsonFormat5(NodeDataArrayLayout)
  implicit val nlpLinkDataArrayLayout = jsonFormat6(LinkDataArrayLayout)
  implicit val nlpFlowChartLayout = jsonFormat5(FlowChartLayout)
  implicit val errorCodeObjJsonFormat = jsonFormat2(errorCodeObj)
  implicit val nlgResponseTextJsonFormat = jsonFormat2(nlgResponseText)
  implicit val nlgResponseMessageJsonFormat = jsonFormat2(nlgResponseMessage)
  implicit val successCodeObjJsonFormat = jsonFormat1(successCodeObj)
  implicit val keywordChkObjJsonFormat = jsonFormat4(keywordChkObj)
  implicit val intentChkDtlsObjJsonFormat = jsonFormat3(intentChkDtlsObj)
  implicit val intentChkObjJsonFormat = jsonFormat1(intentChkObj)
  implicit val PlatFormDtlsObjJsonFormat = jsonFormat4(PlatFormDtlsObj)
  implicit val NLPCommonAtcmtObjJsonFormat = jsonFormat2(NLPCommonAtcmtObj)
  implicit val MessageDtlsObjJsonFormat = jsonFormat4(MessageDtlsObj)
  implicit val NlpReqCommonObjJsonFormat = jsonFormat2(NlpReqCommonObj)
  implicit val nlpActionJsonFormat = jsonFormat9(NlpIdentificationDtlsObj)
  implicit val nlgMessageJsonFormat = jsonFormat5(nlgResponseObj)
  implicit val nlpConsumerMessageJsonFormat = jsonFormat2(NlpActionConsumerObj)

}

*/