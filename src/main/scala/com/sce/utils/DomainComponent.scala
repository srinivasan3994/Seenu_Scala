package com.sce.utils

import scala.slick.driver.JdbcProfile
import java.sql.Date
import com.sce.models._

trait Profile {
  val profile: JdbcProfile
}

trait DomainComponent { this: Profile =>
  import profile.simple._

  class ActionTable(tag: Tag) extends Table[TAction](tag, "t_action") {

    def ActionId = column[Long]("action_id", O NotNull)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def EntityID = column[Option[Long]]("entity_id", O Nullable)
    def ActionName = column[Option[String]]("action_name", O Nullable)
    def CallingInterval = column[Option[Long]]("calling_interval", O Nullable)
    def WarningMessage = column[Option[String]]("warning_message", O Nullable)
    def DataType = column[Option[String]]("data_type", O Nullable)
    
    def * = (ActionId, IntentID, EntityID, ActionName, CallingInterval, WarningMessage, DataType).<>((TAction.apply _).tupled, TAction.unapply)

  }

  object ActionTable {
    def ActionTable = TableQuery[ActionTable]
  }

  class ActionExtn(tag: Tag) extends Table[TActionExtn](tag, "t_action_extn") {

    def ActionID = column[Long]("action_id", O NotNull)
    def WebhookUrl = column[String]("webhook_url", O NotNull)
    def RequestBody = column[Option[String]]("request_body", O Nullable)
    def CallMethod = column[Option[String]]("call_method", O Nullable)
    def SuccessCode = column[Option[String]]("success_code", O Nullable)
    def ErrorCode = column[Option[String]]("error_code", O Nullable)
    def ResponsePath = column[Option[String]]("response_path", O Nullable)
    def LocaleCode = column[Option[String]]("locale_code", O Nullable)

    def * = (ActionID, WebhookUrl, RequestBody, CallMethod, SuccessCode, ErrorCode, ResponsePath, LocaleCode).<>((TActionExtn.apply _).tupled, TActionExtn.unapply)

  }

  object ActionExtn {
    def ActionExtn = TableQuery[ActionExtn]
  }

  class ActionErrorResponse(tag: Tag) extends Table[TAErrorResponse](tag, "t_a_error_response") {

    def ErrorID = column[Long]("error_id", O NotNull)
    def ActionID = column[Long]("action_id", O NotNull)
    def ActionErrorResponse = column[String]("action_error_response", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)

    def * = (ErrorID, ActionID, ActionErrorResponse, LocaleCode).<>((TAErrorResponse.apply _).tupled, TAErrorResponse.unapply)

  }

  object ActionErrorResponse {
    def ActionErrorResponse = TableQuery[ActionErrorResponse]
  }

  class ActionLog(tag: Tag) extends Table[TActiontLog](tag, "t_action_log") {

    def ActionLogId = column[Long]("action_log_id", O PrimaryKey)
    def ActionId = column[Long]("action_id", O NotNull)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def EntityID = column[Option[Long]]("entity_id", O Nullable)
    def WebhookUrl = column[String]("webhook_url", O NotNull)
    def RequestBody = column[String]("request_body")
    def CallMethod = column[String]("call_method")
    def AccessCode = column[String]("access_code")
    def Result = column[String]("result")
    def Created = column[String]("created", O NotNull)
    def UserID = column[String]("user_id", O NotNull)
    def IsSuccess = column[String]("is_success", O NotNull)

    def * = (ActionLogId, ActionId, IntentID, EntityID, WebhookUrl, RequestBody, CallMethod, AccessCode, Result, Created, UserID, IsSuccess).<>((TActiontLog.apply _).tupled, TActiontLog.unapply)

  }

  object ActionLog {
    def ActionLog = TableQuery[ActionLog]
  }
  
  
  
  class ActionAuthorization(tag: Tag) extends Table[TActionAuthorization](tag, "t_action_authorization") {

    def AAID = column[Long]("action_authorization_id", O PrimaryKey)
    def AccessTokenUrl = column[Option[String]]("access_token_url", O Nullable)
    def AccessTokenReqBody = column[Option[String]]("access_token_req_body", O Nullable)
    def AccessTokenReqMethod = column[Option[String]]("access_token_req_method", O Nullable)
    def AccessToken = column[Option[String]]("access_token", O Nullable)
    def ExpiryInterval = column[Option[Long]]("expiry_interval", O Nullable)
    def TokenCreatedTime = column[Option[String]]("token_creation_time", O Nullable)
    def UserAccessCode = column[Option[String]]("user_access_code", O Nullable)

    def * = (AAID, AccessTokenUrl, AccessTokenReqBody, AccessTokenReqMethod, AccessToken, ExpiryInterval, TokenCreatedTime, UserAccessCode).<>((TActionAuthorization.apply _).tupled, TActionAuthorization.unapply)

  }

  object ActionAuthorization {
    def ActionAuthorization = TableQuery[ActionAuthorization]
  }            

  class Channel(tag: Tag) extends Table[TChannel](tag, "t_channel") {

    def ChannelId = column[Long]("channel_id", O PrimaryKey, O AutoInc)
    def ChannelName = column[String]("chennel_name", O NotNull)
    //def ProjectID = column[Option[Long]]("project_id", O Nullable)
    def PlatformID = column[Long]("platform_id", O NotNull)
    def WebhookUrl = column[String]("webhook_url", O NotNull)
    def ProtocolType = column[String]("protocol_type", O NotNull)
    def LocalUrl = column[Option[String]]("local_url", O Nullable)
    def Port = column[Option[Long]]("port", O Nullable)
    def AccessToken = column[Option[String]]("access_token", O Nullable)
    def VerifyToken = column[Option[String]]("verify_token", O Nullable)
    def ChannelSourceID = column[Option[String]]("channel_source_id", O Nullable)

    def * = (ChannelId, ChannelName, /*ProjectID,*/ PlatformID, WebhookUrl, ProtocolType, LocalUrl, Port, AccessToken, VerifyToken, ChannelSourceID).<>((TChannel.apply _).tupled, TChannel.unapply)

  }

  object Channel {
    def Channel = TableQuery[Channel]
  }

  class Conversation(tag: Tag) extends Table[TConversation](tag, "t_conversation") {

    def ConversationID = column[Long]("conversation_id", O NotNull, O.AutoInc)
    def IntentID = column[Option[Long]]("intent_id", O NotNull)
    def SessionID = column[String]("session_id", O NotNull)
    def StartTime = column[Option[String]]("start_time", O Nullable)
    def EndTime = column[Option[String]]("end_time", O Nullable)

    def * = (ConversationID, IntentID, SessionID, StartTime, EndTime).<>((TConversation.apply _).tupled, TConversation.unapply)

  }

  object Conversation {
    def Conversation = TableQuery[Conversation]
  }

  class ConversationCache(tag: Tag) extends Table[TConversationCache](tag, "t_conversation_cache") {

    def SessionID = column[String]("session_id", O.NotNull)
    def IntentID = column[Long]("intent_id", O NotNull)
    def EntryType = column[String]("entry_type", O NotNull)
    def EntryID = column[String]("entry_id", O NotNull)
    def CacheData = column[String]("cache_data", O NotNull)
    def FullFilled = column[Option[String]]("fullfilled", O Nullable)
    def ActionCacheData = column[Option[String]]("action_cache_data", O Nullable)

    def * = (SessionID, IntentID, EntryType, EntryID, CacheData, FullFilled, ActionCacheData).<>((TConversationCache.apply _).tupled, TConversationCache.unapply)

  }

  object ConversationCache {
    def ConversationCache = TableQuery[ConversationCache]
  }

  class ConversationPointer(tag: Tag) extends Table[TConversationPointer](tag, "t_conversation_pointer") {

    def SessionID = column[String]("session_id", O.PrimaryKey)
    def PointerType = column[String]("pointer_type", O NotNull)
    def PointerDesc = column[Option[String]]("pointer_desc", O Nullable)
    def SourceID = column[Option[Long]]("source_id", O NotNull)
    def isPointed = column[String]("is_pointed", O NotNull)
    def TempCache = column[Option[String]]("temp_cache", O Nullable)
    def * = (SessionID, PointerType, PointerDesc, SourceID, isPointed, TempCache).<>((TConversationPointer.apply _).tupled, TConversationPointer.unapply)

  }

  object ConversationPointer {
    def ConversationPointer = TableQuery[ConversationPointer]
  }

  class Confirm(tag: Tag) extends Table[TConfirm](tag, "t_confirm") {

    def ConfirmID = column[Long]("confirm_id", O NotNull)
    def ConfirmationType = column[String]("confirmation_type", O NotNull)
    def ConfirmationText = column[String]("confirmation_text", O NotNull)
    def ConfirmedOpt = column[String]("confirmed_opt", O NotNull)
    def UnConfirmedOpt = column[String]("unconfirmed_opt", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)
    def TerminationText = column[Option[String]]("termination_text", O Nullable)
    def ActionID = column[Option[Long]]("action_id", O Nullable)

    def * = (ConfirmID, ConfirmationType, ConfirmationText, ConfirmedOpt, UnConfirmedOpt, LocaleCode, TerminationText, ActionID).<>((TConfirm.apply _).tupled, TConfirm.unapply)

  }

  object Confirm {
    def Confirm = TableQuery[Confirm]
  }

  class Entity(tag: Tag) extends Table[TEntity](tag, "t_entity") {

    def EntityID = column[Long]("entity_id", O NotNull)
    def EntityName = column[Option[String]]("entity_name", O Nullable)
    def Example = column[Option[String]]("example", O Nullable)
    def EntityTypeCD = column[Option[String]]("entity_type_code", O Nullable)
    def DataType = column[Option[String]]("data_type", O Nullable)
    //def ProjectID = column[Option[Long]]("project_id", O NotNull)

    def * = (EntityID, EntityName, Example, EntityTypeCD, DataType/*, ProjectID*/).<>((TEntity.apply _).tupled, TEntity.unapply)

  }

  object Entity {
    def Entity =
      TableQuery[Entity]
  }

  class EntityQuestions(tag: Tag) extends Table[TEntityQuestions](tag, "t_entity_question") {

    def EntityID = column[Long]("entity_id", O NotNull)
    def Question = column[String]("question", O NotNull)
    def Title = column[Option[String]]("title", O Nullable)
    def ButtonText = column[Option[String]]("button_text", O Nullable)
    def SubTitle = column[Option[String]]("sub_title", O Nullable)
    def ImageUrl = column[Option[String]]("image_url", O Nullable)
    def EntityQuery = column[Option[String]]("entity_query", O Nullable)
    def QuestionID = column[Long]("question_id", O NotNull)
    def LocaleCode = column[Option[String]]("locale_code", O Nullable)
    def EntityExample = column[Option[String]]("entity_example", O Nullable)

    def * = (EntityID, Question, Title, ButtonText, SubTitle, ImageUrl, EntityQuery, QuestionID, LocaleCode, EntityExample).<>((TEntityQuestions.apply _).tupled, TEntityQuestions.unapply)

  }

  object EntityQuestions {
    def EntityQuestions = TableQuery[EntityQuestions]
  }
  
  class EntityRegex(tag: Tag) extends Table[TEntityRegex](tag,"t_entity_regex"){
    
    def EntityID = column[Long]("entity_id", O NotNull)
    def RegexID = column[Long]("regex_id", O NotNull)
    
    def * = (EntityID, RegexID).<>((TEntityRegex.apply _).tupled, TEntityRegex.unapply)
    
  }
  
  object EntityRegex {
    def EntityRegex = TableQuery[EntityRegex]
  }
  
  class EntityType(tag: Tag) extends Table[TEntityType](tag, "t_entity_type") {

    def EntityTypeID = column[Long]("entity_type_id", O NotNull)
    def EntityTypeName = column[Option[String]]("entity_type_name", O Nullable)
    def EntityTypeCode = column[Option[String]]("entity_type_code", O Nullable)
    def ValidationMessage = column[Option[String]]("invalid_message", O Nullable)
    //def ProjectID = column[Option[Long]]("project_id", O Nullable)

    def * = (EntityTypeID, EntityTypeName, EntityTypeCode, ValidationMessage/*, ProjectID*/).<>((TEntityType.apply _).tupled, TEntityType.unapply)

  }

  object EntityType {

    def EntityType = TableQuery[EntityType]

  }

  class ErrorResponse(tag: Tag) extends Table[TErrorResponse](tag, "t_error_response") {

    def ID = column[Long]("id", O NotNull)
    def ErrorCode = column[String]("error_code", O NotNull)
    def ErrorResponse = column[String]("error_response", O NotNull)
    def KUID = column[Option[Long]]("kuid", O NotNull)
    def ActionID = column[Option[Long]]("action_id", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)
    def * = (ID, ErrorCode, ErrorResponse, KUID, ActionID, LocaleCode).<>((TErrorResponse.apply _).tupled, TErrorResponse.unapply)

  }

  object ErrorResponse {
    def ErrorResponse = TableQuery[ErrorResponse]
  }

  class IMErrorLogs(tag: Tag) extends Table[T_IMErrorLogs](tag, "t_im_error_log") {

    def ELogID = column[Long]("elog_id", O.AutoInc, O.PrimaryKey)
    def SessionID = column[Option[String]]("session_id", O Nullable)
    def Message = column[String]("message")
    def Source = column[Option[String]]("source", O Nullable)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def ConversationID = column[Option[Long]]("conversation_id", O Nullable)
    def Created = column[String]("created")

    def * = (ELogID, SessionID, Message, Source, IntentID, ConversationID, Created).<>((T_IMErrorLogs.apply _).tupled, T_IMErrorLogs.unapply)

  }

  object IMErrorLogs {
    def IMErrorLogs = TableQuery[IMErrorLogs]
  }

  class IMALLogs(tag: Tag) extends Table[T_IMALLogs](tag, "t_im_session_log") {

    def LogID = column[Long]("log_id", O.PrimaryKey)
    def SessionID = column[String]("session_id")
    def Message = column[String]("message")
    def Source = column[String]("source")
    def Created = column[String]("created")
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def ConversationID = column[Option[Long]]("conversation_id", O Nullable)

    def * = (LogID, SessionID, Message, Source, Created, IntentID, ConversationID).<>((T_IMALLogs.apply _).tupled, T_IMALLogs.unapply)

  }

  object IMALLogs {
    def IMALLogs = TableQuery[IMALLogs]
  }

  class IMALSession(tag: Tag) extends Table[T_IMALSession](tag, "t_im_session") {

    def IMSessionID = column[Long]("im_session_id", O.PrimaryKey)
    def SessionID = column[String]("session_id")
    def IM_UserID = column[String]("im_user_id")
    def CreatedAT = column[String]("created_at")
    def ExpiredAT = column[Option[String]]("expired_at")
    def Reason = column[String]("reason")
    def ChannelID = column[Option[Long]]("channel_id")

    def * = (IMSessionID, SessionID, IM_UserID, CreatedAT, ExpiredAT, Reason, ChannelID).<>((T_IMALSession.apply _).tupled, T_IMALSession.unapply)

  }

  object IMALSession {
    def IMALSession = TableQuery[IMALSession]
  }

  class IntentTable(tag: Tag) extends Table[TIntent](tag, "t_intent") {

    def IntentID = column[Long]("intent_id", O PrimaryKey)
    def IntentDefinition = column[String]("intent_definition", O NotNull)
    def ActiveIND = column[String]("active_ind", O NotNull)
    def KUID = column[Long]("kuid", O NotNull)
    //def ProjectID = column[Option[Long]]("project_id", O NotNull)

    def * = (IntentID, IntentDefinition, ActiveIND, KUID/*, ProjectID*/).<>((TIntent.apply _).tupled, TIntent.unapply)

  }

  object IntentTable {
    def IntentTable =
      TableQuery[IntentTable]
  }

  class IntentMapping(tag: Tag) extends Table[TIntentMapping](tag, "t_intent_mapping") {

    def IntentSeqID = column[Long]("map_id", O.PrimaryKey)
    def IntentID = column[Long]("intent_id", O NotNull)
    def EntryType = column[String]("entry_type", O NotNull)
    def EntryID = column[String]("entry_id", O NotNull)
    def Order = column[Long]("order_id", O NotNull)
    def Required = column[Option[String]]("required", O Nullable)
    def WorkFlowID = column[Option[Long]]("workflow_id", O Nullable)

    def * = (IntentSeqID, IntentID, EntryType, EntryID, Order, Required, WorkFlowID).<>((TIntentMapping.apply _).tupled, TIntentMapping.unapply)

  }

  object IntentMapping {
    def IntentMapping = TableQuery[IntentMapping]
  }

  class IntentExtn(tag: Tag) extends Table[TIntentExtn](tag, "t_intent_extn") {

    def IntentID = column[Long]("intent_id", O NotNull)
    def IntentName = column[String]("intent_name", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)

    def * = (IntentID, IntentName, LocaleCode).<>((TIntentExtn.apply _).tupled, TIntentExtn.unapply)

  }

  object IntentExtn {
    def IntentExtn =
      TableQuery[IntentExtn]
  }

  class KeywordTable(tag: Tag) extends Table[TKeyword](tag, "t_keyword") {

    def KeywordID = column[Long]("keyword_id", O PrimaryKey)
    def Keyword = column[String]("keyword", O NotNull)
    def IntentID = column[Long]("intent_id", O NotNull)
    def Polarity = column[String]("polarity", O NotNull)
    //def ProjectID = column[Option[Long]]("project_id", O Nullable)

    def * = (KeywordID, Keyword, IntentID, Polarity/*, ProjectID*/).<>((TKeyword.apply _).tupled, TKeyword.unapply)

  }

  object KeywordTable {
    def KeywordTable = TableQuery[KeywordTable]
  }

  class KnowledgeUnitTable(tag: Tag) extends Table[TKnowledgeUnit](tag, "t_ku") {

    def KUID = column[Long]("kuid", O NotNull)
    def KUName = column[String]("ku_name", O NotNull)
    def ActiveIND = column[String]("active_ind", O NotNull)
    def SpamEnable = column[String]("spam_enable", O NotNull)
    def IsRankable = column[Option[String]]("is_rankable", O Nullable)

    def * = (KUID, KUName, ActiveIND, SpamEnable, IsRankable).<>((TKnowledgeUnit.apply _).tupled, TKnowledgeUnit.unapply)

  }

  object KnowledgeUnit {
    def KnowledgeUnit = TableQuery[KnowledgeUnitTable]
  }

  class KRLog(tag: Tag) extends Table[TKRLog](tag, "t_kr_log") {

    def KRLogID = column[Long]("kr_log_id", O NotNull)
    def IMSessionLogID = column[Long]("im_session_log_id", O NotNull)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def KeywordRate = column[Option[Int]]("keyword_rate", O Nullable)

    def * = (KRLogID, IMSessionLogID, IntentID, KeywordRate).<>((TKRLog.apply _).tupled, TKRLog.unapply)

  }

  object KRLog {
    def KRLog = TableQuery[KRLog]
  }

  class LocaleUnicodes(tag: Tag) extends Table[TLocaleUnicodes](tag, "t_locale_unicodes") {

    def UnicodeSeqID = column[Long]("unicode_seq_id", O PrimaryKey, O AutoInc)
    def LocaleCode = column[String]("locale_code", O NotNull)
    def HeadUnicode = column[Long]("head_unicode", O NotNull)
    def TailUnicode = column[Long]("tail_unicode", O NotNull)
    def HeadNumUnicode = column[Option[Long]]("head_num_unicode", O NotNull)
    def TailNumUnicode = column[Option[Long]]("tail_num_unicode", O NotNull)

    def * = (UnicodeSeqID, LocaleCode, HeadUnicode, TailUnicode, HeadNumUnicode, TailNumUnicode).<>((TLocaleUnicodes.apply _).tupled, TLocaleUnicodes.unapply)

  }

  object LocaleUnicodes {
    def LocaleUnicodes = TableQuery[LocaleUnicodes]
  }

  class Locale(tag: Tag) extends Table[TLocale](tag, "t_locale") {

    def LocaleID = column[Long]("locale_id", O PrimaryKey, O AutoInc)
    def LocaleName = column[String]("locale_name", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)
    def LocaleCnfrmMsg = column[Option[String]]("locale_cnfrm_msg", O Nullable)
    def LocaleErrorMsg = column[Option[String]]("locale_error_msg", O Nullable)
    def IntentChoiceMsg = column[Option[String]]("intent_choice_msg", O Nullable)

    def * = (LocaleID, LocaleName, LocaleCode, LocaleCnfrmMsg, LocaleErrorMsg, IntentChoiceMsg).<>((TLocale.apply _).tupled, TLocale.unapply)

  }

  object Locale {
    def Locale = TableQuery[Locale]
  }

  class Message(tag: Tag) extends Table[TMessage](tag, "t_message") {

    def MessageID = column[Long]("message_id", O PrimaryKey)
    def MessageCode = column[Option[String]]("expression", O NotNull)

    def * = (MessageID, MessageCode).<>((TMessage.apply _).tupled, TMessage.unapply)

  }

  object Message {
    def Message = TableQuery[Message]
  }

  class MapRegex(tag: Tag) extends Table[TMapRegex](tag, "t_map_regex") {

    def RegexID = column[Long]("regex_id", O NotNull)
    def MapID = column[Long]("map_id", O NotNull)
    def WorkFlowSeqID = column[Option[Long]]("workflow_sequence_id", O Nullable)

    def * = (RegexID, MapID, WorkFlowSeqID).<>((TMapRegex.apply _).tupled, TMapRegex.unapply)

  }

  object MapRegex {
    def MapRegex = TableQuery[MapRegex]
  }

  class ProjectKeyword(tag: Tag) extends Table[TProjectKeyword](tag, "t_project_keyword") {

    def ProjectKeywordID = column[Long]("project_keyword_id", O NotNull)
    def ProjectKeyword = column[String]("project_keyword", O NotNull)
    def ProjectKeywordType = column[String]("project_keyword_type", O NotNull)
    //def ProjectID = column[Option[Long]]("project_id", O Nullable)

    def * = (ProjectKeywordID, ProjectKeyword, ProjectKeywordType/*, ProjectID*/).<>((TProjectKeyword.apply _).tupled, TProjectKeyword.unapply)

  }

  object ProjectKeyword {
    def ProjectKeyword = TableQuery[ProjectKeyword]
  }

  class Platform(tag: Tag) extends Table[TPlatform](tag, "t_platform") {

    def PlatformID = column[Long]("platform_id", O NotNull)
    def PlatformName = column[String]("platform_name", O NotNull)
    def ActiveID = column[String]("active_id", O NotNull)

    def * = (PlatformID, PlatformName, ActiveID).<>((TPlatform.apply _).tupled, TPlatform.unapply)

  }

  object Platform {
    def Platform = TableQuery[Platform]
  }

  /*class Project(tag: Tag) extends Table[TProject](tag, "t_project") {

    //def ProjectID = column[Long]("project_id", O NotNull)
    def ProjectName = column[String]("project_name", O NotNull)
    def ActiveIND = column[String]("active_id", O NotNull)
    def Created = column[String]("created", O NotNull)

    def * = (ProjectID, ProjectName, ActiveIND, Created).<>((TProject.apply _).tupled, TProject.unapply)

  }

  object Project {
    def Project = TableQuery[Project]
  }*/

  class Regex(tag: Tag) extends Table[TRegex](tag, "t_regex") {

    def RegExID = column[Long]("regex_id", O PrimaryKey)
    def RegularExpression = column[String]("expression", O NotNull)
    def ErrorMessage = column[Option[String]]("nm_message", O Nullable)

    def * = (RegExID, RegularExpression, ErrorMessage).<>((TRegex.apply _).tupled, TRegex.unapply)

  }

  object Regex {
    def Regex = TableQuery[Regex]
  }

  class RegexExtn(tag: Tag) extends Table[TRegexExtn](tag, "t_regex_extn") {

    def RegExnID = column[Long]("id", O NotNull)
    def ErrorMessage = column[String]("error_message", O NotNull)
    def RegExID = column[Long]("regex_id", O NotNull)
    def LocaleCode = column[String]("locale_code", O NotNull)

    def * = (RegExnID, ErrorMessage, RegExID, LocaleCode).<>((TRegexExtn.apply _).tupled, TRegexExtn.unapply)

  }

  object RegexExtn {
    def RegexExtn = TableQuery[RegexExtn]
  }

  class Response(tag: Tag) extends Table[TResponse](tag, "t_response") {

    def ResponseID = column[Long]("response_id", O PrimaryKey)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)
    def EntityID = column[Option[Long]]("entity_id", O Nullable)
    def ActionID = column[Option[Long]]("action_id", O Nullable)
    def KUID = column[Option[Long]]("kuid", O Nullable)
    def ReplyMessage = column[String]("response", O NotNull)
    def Created = column[String]("created", O Nullable)
    def LocaleCode = column[String]("locale_code", O Nullable)
    def MessageID = column[Option[Long]]("message_id", O Nullable)

    def * = (ResponseID, IntentID, EntityID, ActionID, KUID, ReplyMessage, Created, LocaleCode, MessageID).<>((TResponse.apply _).tupled, TResponse.unapply)

  }

  object Response {
    def Response = TableQuery[Response]
  }

  class UserMapping(tag: Tag) extends Table[TUserMapping](tag, "t_user_mapping") {

    def MappingID = column[Long]("mapping_id", O PrimaryKey)
    def IM_UserID = column[String]("im_user_id", O NotNull)
    def Backend_AccessCode = column[String]("backend_accesscode", O NotNull)
    def AcodeExpiry = column[Option[String]]("a_code_expiry")
    def ChannelID = column[Option[Long]]("channel_id", O Nullable)

    def * = (MappingID, IM_UserID, Backend_AccessCode, AcodeExpiry, ChannelID).<>((TUserMapping.apply _).tupled, TUserMapping.unapply)

  }

  object UserMapping {
    def UserMapping = TableQuery[UserMapping]
  }

  class UserAttachemnts(tag: Tag) extends Table[TUserAttachemnts](tag, "t_user_attachments") {

    def AttachmentID = column[Long]("attachment_id", O PrimaryKey, O AutoInc)
    def IMLogID = column[Long]("im_log_id", O NotNull)
    def AttachmentType = column[String]("attachment_type", O NotNull)
    def AttachmentName = column[String]("attachment_name", O NotNull)
    def ChannelID = column[Long]("channel_id", O NotNull)
    def EntityID = column[Long]("entity_id", O NotNull)
    def IsDeleted = column[Option[String]]("is_deleted", O Nullable)

    def * = (AttachmentID, IMLogID, AttachmentType, AttachmentName, ChannelID, EntityID, IsDeleted).<>((TUserAttachemnts.apply _).tupled, TUserAttachemnts.unapply)

  }

  object UserAttachemnts {
    def UserAttachemnts = TableQuery[UserAttachemnts]
  }

  class UserRating(tag: Tag) extends Table[TUserRating](tag, "t_user_rating") {

    def RatingID = column[Long]("rating_id", O PrimaryKey)
    def UserID = column[String]("user_id", O NotNull)
    def SessionID = column[Option[String]]("session_id", O Nullable)
    def IMSessionLogID = column[Long]("im_session_log_id", O NotNull)
    def Keywords = column[String]("keywords", O NotNull)
    def IntentID = column[Option[Long]]("intent_id", O Nullable)

    def * = (RatingID, UserID, SessionID, IMSessionLogID, Keywords, IntentID).<>((TUserRating.apply _).tupled, TUserRating.unapply)

  }

  object UserRating {
    def UserRating = TableQuery[UserRating]
  }

  class UserLocale(tag: Tag) extends Table[TUserLocale](tag, "t_user_locale") {

    def UserLocaleID = column[Long]("user_locale_id", O AutoInc, O PrimaryKey)
    def UserID = column[String]("user_id", O NotNull)
    def ChannelID = column[Option[Long]]("channel_id", O Nullable)
    def LocaleCode = column[String]("locale_code", O NotNull)

    def * = (UserLocaleID, UserID, ChannelID, LocaleCode).<>((TUserLocale.apply _).tupled, TUserLocale.unapply)

  }

  object UserLocale {
    def UserLocale = TableQuery[UserLocale]
  }

  /*class ProjectLocale(tag: Tag) extends Table[TProjectLocale](tag, "t_project_locale") {

    def ProjLangID = column[Long]("proj_lang_id", O NotNull)
    //def ProjectID = column[Option[Long]]("project_id", O Nullable)
    def LocaleCode = column[String]("locale_code", O NotNull)
    def ActiveIND = column[String]("active_id", O NotNull)

    def * = (ProjLangID, ProjectID, LocaleCode, ActiveIND).<>((TProjectLocale.apply _).tupled, TProjectLocale.unapply)

  }

  object ProjectLocale {
    def ProjectLocale = TableQuery[ProjectLocale]
  }*/

  class WorkFlow(tag: Tag) extends Table[TWorkFlow](tag, "t_workflow") {

    def WorkFlowID = column[Long]("workflow_id", O PrimaryKey, O AutoInc)
    def FlowChartInfo = column[String]("metadata", O NotNull)
    def WorkFlowName = column[String]("workflow_name", O NotNull)
    def IntentID = column[Long]("intent_id", O NotNull)
    def IntentSeqID = column[Option[Long]]("intent_seq_id", O Nullable)
    def ActiveIND = column[String]("active_ind", O NotNull)

    def * = (WorkFlowID, FlowChartInfo, WorkFlowName, IntentID, IntentSeqID, ActiveIND).<>((TWorkFlow.apply _).tupled, TWorkFlow.unapply)

  }

  object WorkFlow {
    def WorkFlow = TableQuery[WorkFlow]
  }

  class WorkFlowSequence(tag: Tag) extends Table[TWorkFlowSequence](tag, "t_workflow_sequence") {

    def WorkFlowSeqID = column[Long]("workflow_sequence_id", O PrimaryKey)
    def IntentID = column[Long]("intent_id", O NotNull)
    def WorkFlowID = column[Long]("workflow_id", O NotNull)
    def WorkFlowSeqKey = column[String]("workflow_sequence_key", O NotNull)
    def EntryType = column[String]("entry_type", O NotNull)
    def EntryExpression = column[String]("entry_expression", O NotNull)
    def PmryDestWrkflwID = column[Option[Long]]("primary_dest_wrkflw_id", O Nullable)
    def PmryDestSeqKey = column[Option[String]]("primary_dest_sequence_key", O Nullable)
    def ScndryDestWrkflwID = column[Option[Long]]("secondary_dest_wrkflw_id", O Nullable)
    def ScndryDestSeqKey = column[Option[String]]("secondary_dest_sequence_key", O Nullable)
    def TerminalType = column[Option[String]]("terminal_type", O Nullable)
    def Required = column[Option[String]]("required", O Nullable)
    def InitialValidation = column[Option[String]]("initial_validation", O Nullable)

    def * = (WorkFlowSeqID, IntentID, WorkFlowID, WorkFlowSeqKey, EntryType, EntryExpression, PmryDestWrkflwID,
      PmryDestSeqKey, ScndryDestWrkflwID, ScndryDestSeqKey, TerminalType, Required, InitialValidation).<>((TWorkFlowSequence.apply _).tupled, TWorkFlowSequence.unapply)

  }
  object WorkFlowSequence {
    def WorkFlowSequence = TableQuery[WorkFlowSequence]
  }

}

