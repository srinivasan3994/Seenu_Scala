package com.sce.models


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.json.lenses.JsonLenses._

/**
  * Created by Vinoth on 11/06/2017.
  */


case class NLPSender(id: String)

case class NLPRecipient(id: String)

case class NLPMessage(mid: String, seq: Int, text: String)

// attachment_id is only returned when the is_reusable flag is set to true
// on messages sent with a multimedia attachment
case class NLPAttachmentReuseResponse(recipientId: String, messageId: String, attachmentId: Option[String])

case class NLPPostback(payload: String)

case class NLPMessaging(sender: NLPSender,
                             recipient: NLPRecipient,
                             timestamp: Long,
                             message: Option[NLPMessage],
                             postback: Option[NLPPostback])

case class NLPEntry(id: String, time: Long, messaging: List[NLPMessaging])

case class NLPResponse(obj: String, entry: List[NLPEntry])

case class NLPReceiptElement(title: String, subtitle: String, quantity: Int, price: BigDecimal, currency: String, imageURL: String)

case class NLPUserProfile(firstName: String, lastName: String, picture: String, locale: String, timezone: Int, gender: String)

case class NLPSampleOutMsg(text: String)

//NLP Quick Reply

case class NLPQuickReply(contentType: String, title: String, payload: String)

case class NLPQuickReplyMessage(text: String, quickReplies: List[NLPQuickReply])

case class NLPQuickReplyTemplate(recipient: NLPRecipient, message: NLPQuickReplyMessage)

//NLP Listing

case class NLPListButton(buttonType:String,title:String,payload:String)

case class NLPListElements(title:String,subtitle:String,buttons:Array[NLPListButton])

case class NLPListPaylods(templateType:String,elements:List[NLPListElements])

case class NLPListAttachement(attachmentType:String,payload:NLPListPaylods)

case class NLPBaseAttachment(attachment: NLPListAttachement)

case class NLPListReplyTemplate(recipient: NLPRecipient, message: NLPBaseAttachment)

//NLP Image Listing

case class NLPImageListButton(buttonType:String,title:String,payload:String)

case class NLPImageListElements(title:String,subtitle:String,imageUrl:String,buttons:Array[NLPImageListButton])

case class NLPImageListPaylods(templateType:String,elements:List[NLPImageListElements])

case class NLPImageListAttachement(attachmentType:String,payload:NLPImageListPaylods)

case class NLPImageListBaseAttachment(attachment: NLPImageListAttachement)

case class NLPImageListReplyTemplate(recipient: NLPRecipient, message: NLPImageListBaseAttachment)

//Common Fields

case class NLPConformationCheck(sessionId: String,intentID:Long,ConversationID:Long,quickReply:String)


trait NLGJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val NLPSenderJsonFormat = jsonFormat1(NLPSender)
  implicit val NLPRecipientJsonFormat = jsonFormat1(NLPRecipient)
  implicit val NLPMessageJsonFormat = jsonFormat3(NLPMessage)
  implicit val NLPResponseJsonFormat = jsonFormat(NLPAttachmentReuseResponse, "recipient_id", "message_id", "attachment_id")
  implicit val NLPPostbackJsonFormat = jsonFormat1(NLPPostback)
  implicit val NLPMessagingJsonFormat = jsonFormat5(NLPMessaging)
  implicit val NLPEntryJsonFormat = jsonFormat3(NLPEntry)
  implicit val NLPQuickReplyResponseJsonFormat = jsonFormat(NLPResponse, "object", "entry")

 
  implicit val NLPReceiptElementJsonFormat = jsonFormat(NLPReceiptElement, "title", "subtitle", "quantity", "price", "currency", "image_url")
  implicit val NLPUserProfileJsonFormat = jsonFormat(NLPUserProfile, "first_name", "last_name", "profile_pic", "locale", "timezone", "gender")
  
  //generic sample reply message
  implicit val NLPSampleOutMsgJsonFormat = jsonFormat1(NLPSampleOutMsg)

  //Quick Replay Message Template Builder
  implicit val NLPQuickReplyJsonFormat = jsonFormat(NLPQuickReply, "content_type", "title", "payload")
  implicit val NLPQuickReplyMessageJsonFormat = jsonFormat(NLPQuickReplyMessage, "text", "quick_replies")
  implicit val NLPQuickReplyTemplateJsonFormat = jsonFormat2(NLPQuickReplyTemplate)
 
  //Generic Message Template Builder
  implicit val NLPListButtonJsonFormat = jsonFormat(NLPListButton, "type", "title", "payload")
  implicit val NLPListElementsJsonFormat = jsonFormat(NLPListElements, "title","subtitle","buttons")
  implicit val NLPListPaylodsJsonFormat = jsonFormat(NLPListPaylods, "template_type", "elements")
  implicit val NLPListAttachementJsonFormat = jsonFormat(NLPListAttachement, "type", "payload")
  implicit val NLPBaseAttachmentJsonFormat = jsonFormat1(NLPBaseAttachment)
  implicit val NLPListReplyTemplateJsonFormat = jsonFormat2(NLPListReplyTemplate)

  //Generic Message Template Builder for Image List
  implicit val NLPImageListButtonJsonFormat = jsonFormat(NLPImageListButton, "type", "title", "payload")
  implicit val NLPImageListElementsJsonFormat = jsonFormat(NLPImageListElements, "title","subtitle","image_url","buttons")
  implicit val NLPImageListPaylodsJsonFormat = jsonFormat(NLPImageListPaylods, "template_type", "elements")
  implicit val NLPImageListAttachementJsonFormat = jsonFormat(NLPImageListAttachement, "type", "payload")
  implicit val NLPImageListBaseAttachmentJsonFormat = jsonFormat1(NLPImageListBaseAttachment)
  implicit val NLPImageListReplyTemplateJsonFormat = jsonFormat2(NLPImageListReplyTemplate)
 
}

object Builder1 {

  class MessageBuilder(sender: Option[String], text: Option[String]) {

    def forSender(value: String) = new MessageBuilder(Some(value), text)

    def withText(value: String) = new MessageBuilder(sender, Some(value))

    def build() =
      JsObject(
        "recipient" -> JsObject("id" -> JsString(sender.get)),
        "message" -> JsObject("text" -> JsString(text.get))
      )

  }

  def messageElement = new MessageBuilder(None, None)

  class QuickReplyBuilder(sender: Option[String], text: Option[String]) {

    def forSender(value: String) = new QuickReplyBuilder(Some(value), text)

    def withText(value: String) = new QuickReplyBuilder(sender, Some(value))

    def build() =
      NLPQuickReplyTemplate(NLPRecipient(sender.get), NLPQuickReplyMessage(
        text = text.get,
        quickReplies = NLPQuickReply(
          contentType = "text",
          title = "Yes",
          payload = "yes"
        ) :: NLPQuickReply(
          contentType = "text",
          title = "No",
          payload = "no"
        ) :: Nil
      ))

  }

  def quickReply = new QuickReplyBuilder(None, None)

}