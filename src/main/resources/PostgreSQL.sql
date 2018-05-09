


--platform values

INSERT INTO public.t_platform ( platform_name, active_id, created) VALUES ( 'facebook', NULL, '2018-04-17 10:41:19.039416+04');
INSERT INTO public.t_platform ( platform_name, active_id, created) VALUES ( 'purist', NULL, '2018-04-17 10:41:32.708851+04');


--entity type values

INSERT INTO public.t_entity_type ( entity_type_code, entity_type_name, invalid_message, created, kuid, project_id) VALUES ( 'GEN', 'Generic', 'Enter valid message', '2018-01-10 10:53:38.21466+04', NULL, NULL);
INSERT INTO public.t_entity_type ( entity_type_code, entity_type_name, invalid_message, created, kuid, project_id) VALUES ( 'ATCMT', 'UploadAttachment', 'Media Type Required', '2018-02-01 14:51:42.688833+04', NULL, NULL);
INSERT INTO public.t_entity_type ( entity_type_code, entity_type_name, invalid_message, created, kuid, project_id) VALUES ( 'ELS', 'ExternalService Listing', 'No Info Available', '2018-02-20 17:36:47.671523+04', NULL, NULL);
INSERT INTO public.t_entity_type ( entity_type_code, entity_type_name, invalid_message, created, kuid, project_id) VALUES ( 'EILS', 'External Image Listing', 'List is   not Proper', '2018-03-08 11:23:59.657468+04', NULL, NULL);
INSERT INTO public.t_entity_type ( entity_type_code, entity_type_name, invalid_message, created, kuid, project_id) VALUES ( 'EQRP', 'Quick Replies', 'List is   not Proper', '2018-03-08 11:24:52.253494+04', NULL, NULL);

--locale

INSERT INTO public.t_locale ( locale_name, locale_code, locale_cnfrm_msg, locale_error_msg, intent_choice_msg) VALUES ( 'English', 'en', 'Sure I can speak both languages, shall we carry on in English or Arabic?', 'Sorry I haven''t learnt this language yet, I think I''m good in عربى and English', 'which one do you want to perform?');
INSERT INTO public.t_locale ( locale_name, locale_code, locale_cnfrm_msg, locale_error_msg, intent_choice_msg) VALUES ( 'Arabic', 'ar', 'أكيد أنا أجيد كلا اللغتين, هل نحول اللغة إلى الإنجليزية أو العربية', 'للأسف مازلت أفكر في تعلم هذه اللغة، في الوقت الحالي أعتقد أني قادر على التفاهم معك باللغة العربية أو الإنكليزية', 'أي واحد تريد أن تؤدي؟');

--locale unicodes

INSERT INTO public.t_locale_unicodes ( locale_code, head_unicode, tail_unicode, head_num_unicode, tail_num_unicode, created) VALUES ( 'en', 65, 90, NULL, NULL, '2018-01-17 11:09:39.733127+04');
INSERT INTO public.t_locale_unicodes ( locale_code, head_unicode, tail_unicode, head_num_unicode, tail_num_unicode, created) VALUES ( 'en', 48, 58, NULL, NULL, '2017-01-17 11:09:39.733127+04');
INSERT INTO public.t_locale_unicodes ( locale_code, head_unicode, tail_unicode, head_num_unicode, tail_num_unicode, created) VALUES ( 'en', 97, 122, NULL, NULL, '2018-01-17 10:15:02.674846+04');
INSERT INTO public.t_locale_unicodes ( locale_code, head_unicode, tail_unicode, head_num_unicode, tail_num_unicode, created) VALUES ( 'ar', 1536, 1791, 1632, 1641, '2018-01-17 10:15:51.980946+04');
 
--authorization values

INSERT INTO public.t_action_authorization ( access_token_url, created, access_token_req_body, access_token_req_method, access_token, expiry_interval, token_creation_time, action_name, user_access_code) VALUES ( 'http://10.10.10.212:7001/SCS_UPDATES/oauth/token', '2018-05-07 15:38:29.612581+04', '{
	"service_type": "X_URL_FORM_ENCODED",
	"headers": [{
		"header_key": "Content-Type",
		"header_value": "application/x-www-form-urlencoded"
	}, {
		"header_key": "Authorization",
		"header_value": "Basic Z21KSFJlMEJxQ1pMQThrT1F6QktQbW14d2pIRW91MVNacDNMYjZLMDozdXVjUndhZHFQUmNxYlVRTzNyRmVMUWVyemU4M1pHNG1ueUFGek1T"
	}],
	"req_body": {
		"req_body_params": [{
			"entitykey": "grant_type",
			"entityvalue": "client_credentials"
		}]
	}
}', 'POST', 'efeaa8d6-2fcd-4fab-8982-5f65c26b9276', NULL, '2018-05-08 17:07:08.582', NULL, 'K+u0K8R52vMZErhD4Yt4h50nvbFQA0QMOrUOE2PcMgw=');
 
 

--error responses


INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'BC_PLS_UPLOAD_FILE', 'Please upload a file.', NULL, '2018-01-31 10:08:02.941114+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'BC_PLS_UPLOAD_FILE', 'معالجةالمرفق,', NULL, '2018-01-31 10:08:02.941114+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_PLSWAIT_MSG', 'Please wait, while we process your request.', NULL, '2017-12-19 11:49:52.592463+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_LANG_NOT_FOUND', 'Sorry I haven''t learnt this language yet, I  think I''m good in   Arabic andEnglish', NULL, '2018-02-15 18:10:51.000683+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_INTENT_NOT_FOUND', 'sorry, I didn''t get that :(', NULL, '2017-08-10 17:22:23.80954+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_ENTITY_NOT_FOUND', 'Sorry, I didn''t get that :(.', NULL, '2017-08-10 17:22:23.80954+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_SERVER_DOWN', 'Server down,please contact system admin.', NULL, '2017-12-19 12:39:11.781571+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CALL_INTERVAL_ERRMSG', 'You have performed this transaction', NULL, '2017-12-1913:31:44.967542+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'FILE_UPLOAD_ERROR', 'Sorry we can''t process your attachment', NULL, '2017-12-20 10:49:53.329998+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_FILELIMIT_ERROR', 'File Cannot Exceed 12 mb', NULL, '2017-12-20 14:41:21.825179+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_INTENT_NOT_FOUND', 'آسف، أنا لا أفهم ما تحاول القيام به.', NULL, '2018-04-19 10:06:26.015421+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'INVALID_SELECTION', 'You have selected invalid option.', NULL, '2018-05-08 10:36:16.581262+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCRIPT_ENGINE_EXCEPTION', 'Oops, Internal Error Occured.', NULL, '2018-05-08 12:41:03.292578+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CONF_EXCEPTION', 'Resource Unavailable.', NULL, '2018-05-08 12:43:01.086624+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_LANG_NOT_FOUND', 'آسف لم أتعلم هذه اللغة بعد ، أعتقد أنني جيد باللغة العربية والإنجليزية', NULL, '2018-05-08 12:49:31.237597+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_ENTITY_NOT_FOUND', 'آسف، أنا لا أفهم ما تحاول القيام به.', NULL, '2018-05-08 12:49:31.237597+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_SERVER_DOWN', 'الخادم ، يرجى الاتصال بمشرف النظام.', NULL, '2018-05-08 12:52:30.05857+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CONF_EXCEPTION', 'الموارد غير متوفرة.', NULL, '2018-05-08 12:52:30.05857+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CALL_EXCEPTION', 'غير متاح للحصول على المعلومات.', NULL, '2018-05-08 12:49:31.237597+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CALL_EXCEPTION', 'Unavailable to get information.', NULL, '2018-05-08 12:43:01.086624+04', NULL, 'en', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCRIPT_ENGINE_EXCEPTION', 'عفوًا ، حدث خطأ داخلي.', NULL, '2018-05-08 12:54:10.816313+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'FILE_UPLOAD_ERROR', 'عذرًا ، لا يمكننا معالجة المرفق الخاص بك', NULL, '2018-05-08 12:56:06.495559+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'SCE_FILELIMIT_ERROR', 'لا يمكن أن يتجاوز الملف 12 ميغابايت', NULL, '2018-05-08 12:56:06.495559+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'ACTION_CALL_INTERVAL_ERRMSG', 'لقد أجريت هذه المعاملة', NULL, '2018-05-08 12:56:06.495559+04', NULL, 'ar', NULL);
INSERT INTO public.t_error_response ( error_code, error_response, kuid, created, action_id, locale_code, regex_id) VALUES ( 'INVALID_SELECTION', 'لقد حددت خيارًا غير صالح.', NULL, '2018-05-08 12:56:43.946066+04', NULL, 'ar', NULL);


--channel values

INSERT INTO public.t_channel ( chennel_name, webhook_url, protocol_type, local_url, port, access_token, created, project_id, platform_id, verify_token, channel_source_id) VALUES ( 'Demo Chat', NULL, 'http', '10.10.10.212', '4200', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.t_channel ( chennel_name, webhook_url, protocol_type, local_url, port, access_token, created, project_id, platform_id, verify_token, channel_source_id) VALUES ( 'purist', 'http://api.puristchat.com/admin/v1/conversations/{conversationID}/messages/', 'http', NULL, NULL, '73e84ccd6e4177d7d92d9fefee438b5e', NULL, NULL, Null, NULL, NULL);
INSERT INTO public.t_channel ( chennel_name, webhook_url, protocol_type, local_url, port, access_token, created, project_id, platform_id, verify_token, channel_source_id) VALUES ( 'Facebook', 'https://graph.facebook.com/v2.8/me/messages?', 'https', 'localhost', '5003', 'EAAFyYLQ8ZBdUBAB8ALDwPwRZCzYoNvpby2hS1tTE0BexSSHVjQFB731vZCEBQVz9gYk4sG5yjB7DgVbk3Ui1ZB3sx6hKVC6amXTHZCcFcqRFVzVvHUEkBoaVTxakqH1MxzXU5ux7ZCssoMEDxyC3oc7vxfOnLyKK2ifGtuFdUJXwZDZD', NULL, NULL, Null, NULL, NULL);

update t_channel set platfrom_id = (select platform_id from t_platform where platform_name = t_channel.chennel_name limit 1)

--confirmation values

INSERT INTO public.t_confirm ( confirmation_type, confirmation_text, confirmed_opt, unconfirmed_opt, locale_code, created, termination_text, action_id, kuid) VALUES (26, 'ATTACHMENT', 'Are you sure do you want to upload more files ?', 'yes', 'no', 'en', '2018-05-08 14:23:38.264095+04', NULL, NULL, NULL);
INSERT INTO public.t_confirm ( confirmation_type, confirmation_text, confirmed_opt, unconfirmed_opt, locale_code, created, termination_text, action_id, kuid) VALUES (27, 'ATTACHMENT', 'هل تريد بالتأكيد تحميل المزيد من الملفات؟', 'نعم فعلا', 'لا', 'ar', '2018-05-08 14:23:38.264095+04', NULL, NULL, NULL);
INSERT INTO public.t_confirm ( confirmation_type, confirmation_text, confirmed_opt, unconfirmed_opt, locale_code, created, termination_text, action_id, kuid) VALUES (10, 'ENTITY_VALUE', '{EntityName}: {EntityUserValue}', 'Continue', 'Change', 'en', '2017-12-13 14:43:02.390352+04', 'Transaction Terminated', NULL, NULL);
INSERT INTO public.t_confirm ( confirmation_type, confirmation_text, confirmed_opt, unconfirmed_opt, locale_code, created, termination_text, action_id, kuid) VALUES (9, 'CANCEL', 'Do you want to terminate ', 'yes', 'no', 'en', '2017-12-13 14:43:02.390352+04', 'Transaction Terminated', NULL, NULL);
INSERT INTO public.t_confirm ( confirmation_type, confirmation_text, confirmed_opt, unconfirmed_opt, locale_code, created, termination_text, action_id, kuid) VALUES (11, 'CANCEL', 'هل تريد الالغاء', 'نعم فعلا', 'لا', 'ar', '2018-04-19 10:04:56.382129+04', 'Transaction Terminated', NULL, NULL);



CREATE TABLE public.t_conversation_cache
(
	conv_cache_id bigserial primary key,
    session_id character varying COLLATE pg_catalog."default",
    intent_id bigint,
    cache_data character varying COLLATE pg_catalog."default",
    fullfilled character varying COLLATE pg_catalog."default",
    entry_type character varying COLLATE pg_catalog."default",
    entry_id bigint,
	created character varying COLLATE pg_catalog."default" DEFAULT (now())::character varying,
    CONSTRAINT tconvche_tint_intid_fkey FOREIGN KEY (intent_id)
        REFERENCES public.t_intent (intent_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
);

CREATE TABLE public.t_conversation_pointer
(
	conv_pointer_id bigserial primary key,
    session_id character varying COLLATE pg_catalog."default",
    pointer_type character varying COLLATE pg_catalog."default",
    pointer_desc character varying COLLATE pg_catalog."default",
    source_id bigint,
    is_pointed character varying COLLATE pg_catalog."default",
    temp_cache character varying COLLATE pg_catalog."default",
    created character varying COLLATE pg_catalog."default" DEFAULT (now())::character varying
    
)
WITH (
    OIDS = FALSE
);





CREATE TABLE public.t_platform
(
    platform_id bigserial PRIMARY KEY,
    platform_name character varying COLLATE pg_catalog."default" NOT NULL,
    active_id character varying COLLATE pg_catalog."default",
    created character varying COLLATE pg_catalog."default" DEFAULT (now())::character varying
)
WITH (
    OIDS = FALSE
);




CREATE TABLE public.t_channel
(
    channel_id bigserial,
    chennel_name character varying COLLATE pg_catalog."default",
    webhook_url character varying COLLATE pg_catalog."default",
    protocol_type character varying COLLATE pg_catalog."default",
    local_url character varying COLLATE pg_catalog."default",
    port character varying COLLATE pg_catalog."default",
    access_token character varying COLLATE pg_catalog."default",
    created timestamp with time zone,
    
    platform_id bigint,
    verify_token character varying COLLATE pg_catalog."default",
   
    CONSTRAINT tchannel_pkey PRIMARY KEY (channel_id)
)
WITH (
    OIDS = FALSE
);





CREATE TABLE public.t_entity_regex
(
    entity_regex_id bigserial primary key,
    entity_id bigint,
    regex_id bigint,
    created character varying COLLATE pg_catalog."default" DEFAULT (now())::character varying,
    CONSTRAINT t_entity_regex_pkey PRIMARY KEY (entity_regex_id),
    CONSTRAINT tentrgx_tent_eid FOREIGN KEY (entity_id)
        REFERENCES public.t_entity (entity_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
)




CREATE TABLE public.t_workflow_sequence
(
    workflow_sequence_id bigserial,
    workflow_id bigint NOT NULL,
    entry_type character varying COLLATE pg_catalog."default" NOT NULL,
    entry_expression character varying COLLATE pg_catalog."default" NOT NULL,
    primary_dest_wrkflw_id bigint,
    secondary_dest_wrkflw_id bigint,
    intent_seq_id bigint,
    required character varying COLLATE pg_catalog."default",
    terminal_type character varying COLLATE pg_catalog."default",
    intent_id bigint,
    primary_dest_sequence_key character varying COLLATE pg_catalog."default",
    workflow_sequence_key character varying COLLATE pg_catalog."default",
    secondary_dest_sequence_key character varying COLLATE pg_catalog."default",
    kuid bigint,
    intial_validation character varying COLLATE pg_catalog."default",
    CONSTRAINT t_workflow_seq_pkey PRIMARY KEY (workflow_sequence_id)
)
WITH (
    OIDS = FALSE
);




ALTER TABLE public.t_user_locale
    ADD COLUMN channel_id character varying COLLATE pg_catalog."default";


ALTER TABLE public.t_entity
    ADD COLUMN data_type character varying COLLATE pg_catalog."default";



CREATE TABLE public.t_intent_mapping
(
    map_id bigserial,
    entry_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    kuid bigint NOT NULL,
    order_id bigint NOT NULL,
    required character varying(255) COLLATE pg_catalog."default" NOT NULL,
    intent_id bigint NOT NULL,
    workflow_id bigint NOT NULL,
    entry_id character varying COLLATE pg_catalog."default",
    CONSTRAINT t_intent_mapping_pkey PRIMARY KEY (map_id),
  
    CONSTRAINT tintmap_tint_intentid_fkey FOREIGN KEY (intent_id)
        REFERENCES public.t_intent (intent_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
);


CREATE TABLE public.t_message
(
    message_id bigserial,
    message_code character varying COLLATE pg_catalog."default",
    created character varying COLLATE pg_catalog."default" DEFAULT (now())::character varying,
    CONSTRAINT "messageId_pkey" PRIMARY KEY (message_id)
)
WITH (
    OIDS = FALSE
)




ALTER TABLE public.t_response
    ADD COLUMN action_id bigint;
    
    
ALTER TABLE public.t_response
    ADD COLUMN message_id bigint;


CREATE TABLE public.t_action_authorization
(
    action_authorization_id bigserial PRIMARY KEY,
    access_token_url character varying,
    created character varying DEFAULT (now())::character varying,
    access_token_req_body character varying,
    access_token_req_method character varying,
    access_token character varying,
    expiry_interval bigint,
    token_creation_time character varying,
    action_name character varying,
    user_access_code character varying
)
WITH (
    OIDS = FALSE
)





alter table t_user_mapping add column channel_id bigint

alter table t_conversation_cache add column action_cache_data character varying


alter table t_user_attachments add column channel_id bigint











