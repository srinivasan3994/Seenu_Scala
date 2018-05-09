package com.sce.services

import com.sce.models._

/**
  * Created by Vinoth on 10/07/2017.
  */

object NlpJsonGeneratorService extends NLGJsonSupport{
  
  def getQuickReplyJson(sengerID:String, responseText:String, CnfrmOpt: String, UnCnfrmOpt: String):NLPQuickReplyMessage = {
    
      NLPQuickReplyMessage(
        text = responseText,
        quickReplies = NLPQuickReply(
          contentType = "text",
          title = CnfrmOpt,
          payload = CnfrmOpt
        ) :: NLPQuickReply(
          contentType = "text",
          title = UnCnfrmOpt,
          payload = UnCnfrmOpt
        ) :: Nil
      )
  }
  
  def getNlpQuickReplyTemplate(sender: String, text: String, facebookQuickReplyList: List[NLPQuickReply]): NLPQuickReplyTemplate = {
    NLPQuickReplyTemplate(NLPRecipient(sender), NLPQuickReplyMessage(text = text,facebookQuickReplyList))
  }

  def getNlpListReplyTemplate(sender: String, text: String, relay: List[NLPListElements]): NLPListReplyTemplate = {
    NLPListReplyTemplate(NLPRecipient(sender),NLPBaseAttachment(NLPListAttachement(attachmentType="template",NLPListPaylods(templateType = "generic",relay))))
  }
  
  def getNlpImageListReplyTemplate(sender: String, text: String, relay: List[NLPImageListElements]): NLPImageListReplyTemplate = {
    NLPImageListReplyTemplate(NLPRecipient(sender),NLPImageListBaseAttachment(NLPImageListAttachement(attachmentType="template",NLPImageListPaylods(templateType = "generic",relay))))
  }
  
}