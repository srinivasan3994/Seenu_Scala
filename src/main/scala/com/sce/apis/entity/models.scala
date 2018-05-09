/*package com.sce.apis.entity

case class TAction(ActionId: Long, IntentID: Option[Long], EntityID: Option[Long], ActionName: String, CallingInterval: Option[Long], WarningMessage: Option[String])
case class TActionExtn(ActionID: Long, WebhookUrl: String, RequestBody: Option[String], CallMethod: Option[String], SuccessCode: Option[String],
                       ErrorCode: Option[String], SessionParameter: Option[String], LocaleCode: Option[String])
case class TActiontLog(ActionLogID: Long, ActionId: Long, IntentID: Option[Long], EntityID: Option[Long], WebhookUrl: String, RequestBody: String,
                       CallMethod: String, AccessCode: String, Result: String, Created: String, UserID: String, IsSuccess: String)
case class TActionConfirm(ActionID: Long, ConfirmID: Long, ConfirmText: String)
case class TConfirm(ConfirmID: Long, ConfirmationType: String, ConfirmationText: String, ConfirmedOpt: String, UnConfirmedOpt: String,
                    LocaleCode: String, TerminationText: Option[String], ActionID: Option[Long])
case class TAErrorResponse(ErrorID: Long, ActionID: Long, ActionErrorResponse: String, LocaleCode: String)
case class TBackend(BackendID: Long, ProjectID: Option[Long], AccessToken: String, BaseURL: String)
case class TUserBackend(UserBackendID: Long, UserID: String, UserName: String, Password: String)
case class TChannel(ChannelID: Long, ChennelName: String, ProjectID: Option[Long], PlatformID: Long, WebhookUrl: String,
                    ProtocolType: String, LocalUrl: Option[String], Port: Option[Long], AccessToken: Option[String], VerifyToken: Option[String], ChannelSourceID: Option[String])
case class TConversation(ConversationID: Long, IntentID: Option[Long], SessionID: String, StartTime: Option[String], EndTime: Option[String])
case class TEntity(EntityID: Long, EntityName: Option[String], Example: Option[String], EntityTypeCD: Option[String], ProjectID: Option[Long])
case class TEntityIntent(EntityID: Long, IntentID: Long, Sequence: Long, Required: Option[String], MapID: Option[Long], FlowChartID: Option[Long])
case class TEntityQuestions(EntityID: Long, Question: String, Title: Option[String], ButtonText: Option[String], SubTitle: Option[String], ImageUrl: Option[String], EntityQuery: Option[String], QuestionID: Long, LocaleCode: Option[String], EntityExample: Option[String])
case class TEntityType(EntityTypeID: Long, EntityTypeName: Option[String], EntityTypeCode: Option[String], ValidationMessage: Option[String], ProjectID: Option[Long])
case class TErrorResponse(ID: Long, ErrorCode: String, ErrorResponse: String, KUID: Option[Long], ActionID: Option[Long], LocaleCode: String)
case class TFlowChart(FlowChartID: Long, FlowChartInfo: String)
case class TFlowChartSession(SessionID: String, FlowChartID: Long, IntentID: Long, EntityID: Long, FlowChartKey: Long, EntryType: String, EntryName: String, EntryID: Long,
                             ParameterValue: Option[String])
case class T_IMALLogs(LogID: Long, SessionID: String, Message: String, Source: String, Created: String, IntentID: Option[Long], ConversationID: Option[Long])
case class T_IMALSession(IMSessionID: Long, SessionID: String, ProjectID: Option[Long], IM_UserID: String, CreatedAT: String, ExpiredAT: Option[String], Reason: String, ChannelID: Option[Long])
case class T_IMErrorLogs(ELogID: Long, SessionID: Option[String], Message: String, Source: Option[String], IntentID: Option[Long], ConversationID: Option[Long], Created: String)
case class TPlatform(IMPlatformID: Long, PlatformName: String, ActiveInd: String)
case class TIntent(IntentID: Long, IntentDefinition: String, ActiveIND: String, KUID: Long, ProjectID: Option[Long])
case class TIntentExtn(IntentID: Long, IntentName: String, LocaleCode: String)
case class TKeyword(KeywordID: Long, Keyword: String, IntentID: Long, Polarity: String, ProjectID: Option[Long])
case class TKnowledgeUnit(KUID: Long, KUName: String, ActiveIND: String, SpamEnable: String, ProjectID: Option[Long])
case class TKRLog(KRLogID: Long, IMSessionLogID: Long, IntentID: Option[Long], KeywordRate: Option[Int])
case class TLocale(LocaleID: Long, LocaleName: String, LocaleCode: String, LocaleCnfrmMsg: Option[String], LocaleErrorMsg: Option[String], IntentChoiceMsg: Option[String])
case class TLocaleUnicodes(UnicodeSeqID: Long, LocaleCode: String, HeadUnicode: Long, TailUnicode: Long,
                           HeadNumUnicode: Option[Long], TailNumUnicode: Option[Long])
case class TMapRegex(RegexID: Long, MapID: Long, WorkFlowSeqID: Option[Long])
case class TProject(ProjectID: Long, ProjectName: String, ActiveIND: String, Created: String)
case class TProjectKeyword(ProjectKeywordID: Long, ProjectKeyword: String, ProjectKeywordType: String, ProjectID: Option[Long])
case class TProjectLocale(ProjLangID: Long, ProjectID: Option[Long], LocaleCode: String, ActiveIND: String)
case class TRegex(RegExID: Long, RegularExpression: String, ErrorMessage: String)
case class TRegexExtn(RegExnID: Long, ErrorMessage: String, RegExID: Long, LocaleCode: String)
case class TResponse(ResponseID: Long, IntentID: Option[Long], EntityID: Option[Long], ActionID: Option[Long],
                     KUID: Option[Long], ReplyMessage: String, Created: String, Locale_Code: String, MessageID: Option[Long])
case class TSessionRecord(SessionID: String, IntentID: Long, ParameterName: String, ParameterType: String, EntityID: Long, Created: String, LogStatus: String,
                          FlowChartID: Option[Long], FlowChartKey: Option[Long], EntityOrder: Long, ConfirMationType: Option[String])
case class TUserAttachemnts(AttachmentID: Long, IMLogID: Long, AttachmentType: String, AttachmentName: String, SourceFrom: String)
case class TUserMapping(MappingID: Long, IM_UserID: String, IM_Platform: String, Backend_AccessCode: String, AcodeExpiry: Option[String],
                        ChannelID: Option[Long], IMPlatformID: Option[Long])
case class TUserRating(RatingID: Long, UserID: String, SessionID: Option[String], IMSessionLogID: Long, Keywords: String, IntentID: Option[Long])
case class TUserLocale(UserLocaleID: Long, UserID: String, ProjectID: Option[Long], LocaleCode: String)
case class TIntentMapping(IntentSeqID: Long, IntentID: Long, EntryType: String, EntryID: Long, Order: Long, Required: Option[String], WorkFlowID: Option[Long])
case class TConversationCache(SessionID: String, IntentID: Long, EntryType: String, EntryID: Long, CacheData: String, FullFilled: Option[String])
case class TConversationPointer(SessionID: String, PointerType: String, PointerDesc: Option[String], SourceID: Option[Long], isPointed: String, TempCache: Option[String])
case class TWorkFlow(WorkFlowID: Long, FlowChartInfo: String, WorkFlowName: String, IntentID: Long, IntentSeqID: Option[Long], ActiveIND: String)
case class TWorkFlowSequence(WorkFlowSeqID: Long, IntentID: Long, WorkFlowID: Long, WorkFlowSeqKey: String, EntryType: String,
                             EntryExpression: String, PmryDestWrkflwID: Option[Long], PmryDestSeqKey: Option[String], ScndryDestWrkflwID: Option[Long], ScndryDestSeqKey: Option[String], TerminalType: Option[String], Required: Option[String])
case class TMessage(MessageID: Long, MessageCode: Option[String])
    

case class TWorkFlow(WorkFlowID: Long, FlowChartInfo: String,    WorkFlowName: String,
    IntentID: Long, EntityID: Option[Long], ActionID: Option[Long], ActiveIND: String)
case class TWorkFlowSession(SessionID:String, WorkFlowID:Long, IntentID:Long, EntityID:Option[Long], ActionID: Option[Long], WorkFlowKey:Long, EntryType: String, EntryName: String, EntryID:Long, 
    ParameterValue:Option[String], Pointer: Option[String])
case class TWorkFlowSequence(WorkFlowSeqID: Long,WorkFlowID: Long,WorkFlowKey: Long,
    Shape: String, Expression: String, PrimaryLink: Option[Long], SecondaryLink: Option[Long])



*/