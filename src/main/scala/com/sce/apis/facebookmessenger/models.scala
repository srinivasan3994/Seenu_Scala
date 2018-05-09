/*package com.sce.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import spray.json.lenses.JsonLenses._

case class FacebookSender(id: String)

case class FacebookRecipient(id: String)

case class QuickReplyObj(payload: String)

case class FacebookMessage(mid: String, seq: Int, text: Option[String],attachments: Option[Array[FacebookAttachment]], quick_reply: Option[QuickReplyObj])

case class FacebookAttachment(`type`: String,payload: FacebookAttachmentPayload)

case class FacebookAttachmentPayload(url: String)

case class FacebookSenderAction(recipient: FacebookRecipient,sender_action:String)

// attachment_id is only returned when the is_reusable flag is set to true
// on messages sent with a multimedia attachment
case class FacebookAttachmentReuseResponse(recipientId: String, messageId: String, attachmentId: Option[String])

case class FacebookPostback(payload: String)

case class FacebookMessaging(sender: FacebookSender,
                             recipient: FacebookRecipient,
                             timestamp: Long,
                             message: Option[FacebookMessage],
                             postback: Option[FacebookPostback])
                             

case class FacebookEntry(id: String, time: Long, messaging: List[FacebookMessaging])

case class FacebookResponse(obj: String, entry: List[FacebookEntry])

case class FacebookReceiptElement(title: String, subtitle: String, quantity: Int, price: BigDecimal, currency: String, imageURL: String)

case class FacebookUserProfile(firstName: String, lastName: String, picture: String, locale: String, timezone: Int, gender: String)

//Facebook Quick Reply

case class FacebookQuickReply(contentType: String, title: String, payload: String)

case class FacebookQuickReplyMessage(text: String, quickReplies: List[FacebookQuickReply])

case class FacebookQuickReplyTemplate(recipient: FacebookRecipient, message: FacebookQuickReplyMessage)

//Facebook Listing

case class FacebookListButton(buttonType:String,title:String,payload:String)

case class FacebookListElements(title:String,subtitle:String,buttons:Array[FacebookListButton])

case class FacebookListPaylods(templateType:String,elements:List[FacebookListElements])

case class FacebookListAttachement(attachmentType:String,payload:FacebookListPaylods)

case class FacebookBaseAttachment(attachment: FacebookListAttachement)

case class FacebookListReplyTemplate(recipient: FacebookRecipient, message: FacebookBaseAttachment)

//Facebook Image Listing

case class FacebookImageListButton(buttonType:String,title:String,payload:String)

case class FacebookImageListElements(title:String,subtitle:String,imageUrl:String,buttons:Array[FacebookImageListButton])

case class FacebookImageListPaylods(templateType:String,elements:List[FacebookImageListElements])

case class FacebookImageListAttachement(attachmentType:String,payload:FacebookImageListPaylods)

case class FacebookImageListBaseAttachment(attachment: FacebookImageListAttachement)

case class FacebookImageListReplyTemplate(recipient: FacebookRecipient, message: FacebookImageListBaseAttachment)


trait FacebookJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

   implicit val facebookSenderJsonFormat = jsonFormat1(FacebookSender)
   implicit val quickReplyObjJsonFormat = jsonFormat1(QuickReplyObj)
  implicit val FacebookAttachmentPayloadJsonFormat = jsonFormat1(FacebookAttachmentPayload)
  implicit val FacebookAttachmentJsonFormat = jsonFormat2(FacebookAttachment)
  implicit val facebookRecipientJsonFormat = jsonFormat1(FacebookRecipient)
  implicit val facebookMessageJsonFormat = jsonFormat5(FacebookMessage)
  implicit val facebookResponseJsonFormat = jsonFormat(FacebookAttachmentReuseResponse, "recipient_id", "message_id", "attachment_id")
  implicit val facebookPostbackJsonFormat = jsonFormat1(FacebookPostback)
  implicit val facebookMessagingJsonFormat = jsonFormat5(FacebookMessaging)
  implicit val facebookEntryJsonFormat = jsonFormat3(FacebookEntry)
  implicit val facebookQuickReplyResponseJsonFormat = jsonFormat(FacebookResponse, "object", "entry")
  implicit val facebookSenderActionResponseJsonFormat = jsonFormat2(FacebookSenderAction)
 
  implicit val facebookReceiptElementJsonFormat = jsonFormat(FacebookReceiptElement, "title", "subtitle", "quantity", "price", "currency", "image_url")
  implicit val facebookUserProfileJsonFormat = jsonFormat(FacebookUserProfile, "first_name", "last_name", "profile_pic", "locale", "timezone", "gender")

  //Quick Replay Message Template Builder
  implicit val facebookQuickReplyJsonFormat = jsonFormat(FacebookQuickReply, "content_type", "title", "payload")
  implicit val facebookQuickReplyMessageJsonFormat = jsonFormat(FacebookQuickReplyMessage, "text", "quick_replies")
  implicit val facebookQuickReplyTemplateJsonFormat = jsonFormat2(FacebookQuickReplyTemplate)
 
  //Generic Message Template Builder
  implicit val facebookListButtonJsonFormat = jsonFormat(FacebookListButton, "type", "title", "payload")
  implicit val facebookListElementsJsonFormat = jsonFormat(FacebookListElements, "title","subtitle","buttons")
  implicit val facebookListPaylodsJsonFormat = jsonFormat(FacebookListPaylods, "template_type", "elements")
  implicit val facebookListAttachementJsonFormat = jsonFormat(FacebookListAttachement, "type", "payload")
  implicit val facebookBaseAttachmentJsonFormat = jsonFormat1(FacebookBaseAttachment)
  implicit val facebookListReplyTemplateJsonFormat = jsonFormat2(FacebookListReplyTemplate)
  
  //Generic Message Template Builder for Image List
  implicit val facebookImageListButtonJsonFormat = jsonFormat(FacebookImageListButton, "type", "title", "payload")
  implicit val facebookImageListElementsJsonFormat = jsonFormat(FacebookImageListElements, "title","subtitle","image_url","buttons")
  implicit val facebookImageListPaylodsJsonFormat = jsonFormat(FacebookImageListPaylods, "template_type", "elements")
  implicit val facebookImageListAttachementJsonFormat = jsonFormat(FacebookImageListAttachement, "type", "payload")
  implicit val facebookImageListBaseAttachmentJsonFormat = jsonFormat1(FacebookImageListBaseAttachment)
  implicit val facebookImageListReplyTemplateJsonFormat = jsonFormat2(FacebookImageListReplyTemplate)
  
}

object Builder {

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
      FacebookQuickReplyTemplate(FacebookRecipient(sender.get), FacebookQuickReplyMessage(
        text = text.get,
        quickReplies = FacebookQuickReply(
          contentType = "text",
          title = "Yes",
          payload = "yes"
        ) :: FacebookQuickReply(
          contentType = "text",
          title = "No",
          payload = "no"
        ) :: Nil
      ))

  }

  def quickReply = new QuickReplyBuilder(None, None)

}*/