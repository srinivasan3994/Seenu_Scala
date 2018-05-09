


CREATE SEQUENCE "tku_kuid_seq"
    START WITH 1
    INCREMENT BY 1
    
    

    
CREATE TABLE "t_ku" (
    "kuid" bigint  DEFAULT(NEXT VALUE FOR "tku_kuid_seq") NOT NULL ,
    "ku_name" NVARCHAR(max),
    "created" NVARCHAR(max),
	CONSTRAINT "t_ku_pkey" PRIMARY KEY ("kuid")
)
GO



CREATE SEQUENCE "tintent_intentid_seq"
    START WITH 1
    INCREMENT BY 1
    

CREATE TABLE "t_intent" (
    "intent_id" bigint DEFAULT(NEXT VALUE FOR "tintent_intentid_seq") NOT NULL ,
    "intent_definition" NVARCHAR(max),
    "kuid" bigint,
    "created" NVARCHAR(max),
	CONSTRAINT "t_intent_pkey" PRIMARY KEY ("intent_id"),
	CONSTRAINT "t_intent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO




CREATE SEQUENCE "tenttyp_etid_seq"
    START WITH 1
    INCREMENT BY 1
    

    
CREATE TABLE "t_entity_type" (
    "entity_type_id" bigint DEFAULT(NEXT VALUE FOR "tenttyp_etid_seq") ,
    "entity_type_code" NVARCHAR(100) UNIQUE,
    "entity_type_name" NVARCHAR(max),
    "invalid_message" NVARCHAR(max),
    "created" NVARCHAR(max),
    "kuid" bigint,
	CONSTRAINT "t_entity_type_pkey" PRIMARY KEY ("entity_type_id"),
	CONSTRAINT "t_enttyp_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO


   
CREATE INDEX "tenttyp_code_idx"
   ON "t_entity_type" ("entity_type_code");



    
   

CREATE SEQUENCE "tent_eid_seq"
    START WITH 1
    INCREMENT BY 1
    

	
CREATE TABLE "t_entity" (
    "entity_id" bigint DEFAULT(NEXT VALUE FOR "tent_eid_seq") NOT NULL ,
    "entity_name" NVARCHAR(max),
    "example" NVARCHAR(max),
    "created" NVARCHAR(max),
    "entity_type_code" NVARCHAR(100),
    "kuid" bigint,
	CONSTRAINT "t_entity_pkey" PRIMARY KEY ("entity_id"),
	CONSTRAINT "t_ent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO


CREATE SEQUENCE "taction_actionid_seq"
    START WITH 1
    INCREMENT BY 1
    


CREATE TABLE "t_action" (
    "action_id" bigint DEFAULT(NEXT VALUE FOR "taction_actionid_seq") NOT NULL,
    "intent_id" bigint,
    "action_name" NVARCHAR(max),
    "webhook_url" NVARCHAR(max),
    "request_body" NVARCHAR(max),
    "call_method" NVARCHAR(max),
    "created" NVARCHAR(max),
    "kuid" bigint,
    "entity_id" bigint,
    "success_code" NVARCHAR(max),
    "error_code" NVARCHAR(max),
    "session_parameter" NVARCHAR(max),
	CONSTRAINT "t_action_pkey" PRIMARY KEY ("action_id"),
	CONSTRAINT "t_act_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE,
	CONSTRAINT "t_act_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
	CONSTRAINT "t_act_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION
)
GO


	
CREATE INDEX "tact_aid_idx"
   ON "t_action" ("action_id");
   

CREATE INDEX "tact_iid_idx"
   ON "t_action" ("intent_id");
   

CREATE INDEX "tact_eid_idx"
   ON "t_action" ("entity_id");

   
   
   

CREATE TABLE "t_im_session" (
    "session_id" NVARCHAR(200) PRIMARY key,
    "im_user_id" NVARCHAR(max),
    "created_at" NVARCHAR(max),
    "expired_at" NVARCHAR(max),
    "reason" NVARCHAR(max)
)
GO
   
   

CREATE SEQUENCE "tconv_cid_seq"
    START WITH 1
    INCREMENT BY 1
    
    


CREATE TABLE "t_conversation" (
    "conversation_id" bigint DEFAULT(NEXT VALUE FOR "tconv_cid_seq") NOT NULL,
    "intent_id" bigint,
    "message_id" NVARCHAR(max),
    "probability" NVARCHAR(max),
    "total_intent" bigint,
    "created" NVARCHAR(max),
    "kuid" bigint,
	CONSTRAINT "t_conversation_pkey" PRIMARY KEY ("conversation_id"),
	CONSTRAINT "t_conv_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE,
	CONSTRAINT "t_conv_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION
)
GO



CREATE INDEX "tconv_iid_idx"
   ON "t_conversation" ("intent_id");



CREATE SEQUENCE "tentques_qid_seq"
    START WITH 1
    INCREMENT BY 1
        
    


CREATE TABLE "t_entity_question" (
    "question_id" bigint DEFAULT(NEXT VALUE FOR "tentques_qid_seq") NOT NULL ,
    "entity_id" bigint,
    "question" NVARCHAR(max),
    "created" NVARCHAR(max),
    "kuid" bigint,
    "title" NVARCHAR(max),
    "button_text" NVARCHAR(max),
    "sub_title" NVARCHAR(max),
    "entity_query" NVARCHAR(max),
	CONSTRAINT "t_entity_question_pkey" PRIMARY KEY ("question_id"),
	CONSTRAINT "t_entque_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
	CONSTRAINT "t_entque_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO



CREATE INDEX "tentque_eid_idx"
   ON "t_entity_question" ("entity_id");
   







CREATE SEQUENCE "tflwchrt_fid_seq"
    START WITH 1
    INCREMENT BY 1

CREATE TABLE "t_flowchart" (
    "flowchart_id" bigint DEFAULT(NEXT VALUE FOR "tflwchrt_fid_seq") NOT NULL ,
    "flowchart_info" NVARCHAR(max),
    "created" NVARCHAR(max),
	CONSTRAINT "t_flowchart_pkey" PRIMARY KEY ("flowchart_id")
	)
GO
    




CREATE SEQUENCE "timal_logid_seq"
    START WITH 1
    INCREMENT BY 1
    
    
    
CREATE TABLE "t_im_session_log" (
    "log_id" bigint DEFAULT(NEXT VALUE FOR "timal_logid_seq") NOT NULL ,
    "session_id" NVARCHAR(200),
    "message" NVARCHAR(max),
    "source" NVARCHAR(max),
    "created" NVARCHAR(max),
    "intent_id" bigint,
	CONSTRAINT "t_im_session_log_pkey" PRIMARY KEY ("log_id"),
	   CONSTRAINT "t_imsl_t_ims_ssid_fkey" FOREIGN KEY ("session_id")
       REFERENCES "t_im_session" ("session_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO


  

      
CREATE INDEX "timseslog_sid_idx"
   ON "t_im_session_log" ("session_id");




CREATE SEQUENCE "tkrlog_krlogid_seq"
    START WITH 1
    INCREMENT BY 1
    
	
CREATE TABLE "t_kr_log" (
    "kr_log_id" bigint DEFAULT(NEXT VALUE FOR "tkrlog_krlogid_seq") NOT NULL ,
    "im_session_log_id" bigint,
    "intent_id" bigint,
    "keyword_rate" integer,
    "created" NVARCHAR(max),
	CONSTRAINT "t_kr_log_pkey" PRIMARY KEY ("kr_log_id"),
  
	   CONSTRAINT "t_krlog_t_imsl_iid_fkey" FOREIGN KEY ("im_session_log_id")
       REFERENCES "t_im_session_log" ("log_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO

   
   
CREATE INDEX "tKRLog_IMSLID_idx"
   ON "t_kr_log" ("im_session_log_id");
   
   
CREATE INDEX "tKRLog_IID_idx"
   ON "t_kr_log" ("intent_id");





CREATE SEQUENCE "tkeywrd_kid_seq"
    START WITH 1
    INCREMENT BY 1
    
    
    
CREATE TABLE "t_keyword" (
    "keyword_id" bigint DEFAULT(NEXT VALUE FOR "tkeywrd_kid_seq") NOT NULL ,
    "intent_id" bigint,
    "keyword" NVARCHAR(max),
    "polarity" NVARCHAR(max),
    "created" NVARCHAR(max),
    "kuid" bigint,
	CONSTRAINT "t_keyword_pkey" PRIMARY KEY ("keyword_id"),
	CONSTRAINT "t_ku_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE,
	CONSTRAINT "t_keywrd_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION
)
GO





CREATE SEQUENCE "tregex_rid_seq"
    START WITH 1
    INCREMENT BY 1
    
    
CREATE TABLE "t_regex" (
    "regex_id" bigint DEFAULT(NEXT VALUE FOR "tregex_rid_seq") NOT NULL ,
    "expression" NVARCHAR(max),
    "nm_message" NVARCHAR(max),
    "created" NVARCHAR(max),
    "regex_name" NVARCHAR(max),
	CONSTRAINT "t_regex_pkey" PRIMARY KEY ("regex_id")
)
GO




CREATE SEQUENCE "tuserrating_rid_seq"
    START WITH 1
    INCREMENT BY 1
 



CREATE TABLE "t_user_rating" (
    "rating_id" bigint DEFAULT(NEXT VALUE FOR "tuserrating_rid_seq") NOT NULL ,
    "user_id" NVARCHAR(max),
    "im_session_log_id" bigint,
    "keywords" NVARCHAR(max),
    "intent_id" bigint,
    "created" NVARCHAR(max),
	CONSTRAINT "t_user_rating_pkey" PRIMARY KEY ("rating_id"),
	   CONSTRAINT "t_usrrat_t_imsl_iid_fkey" FOREIGN KEY ("im_session_log_id")
       REFERENCES "t_im_session_log" ("log_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO



   
CREATE INDEX "tusrrat_imslid_idx"
   ON "t_user_rating" ("im_session_log_id");
   
   
CREATE INDEX "tusrrat_iid_idx"
   ON "t_user_rating" ("intent_id");



   


CREATE SEQUENCE "taerrresp_eid_seq"
    START WITH 1
    INCREMENT BY 1

CREATE TABLE "t_a_error_response" (
    "error_id" bigint DEFAULT(NEXT VALUE FOR "taerrresp_eid_seq") NOT NULL ,
    "action_id" bigint,
    "action_error_response" NVARCHAR(max),
    "created" NVARCHAR(max),
	CONSTRAINT "t_a_error_response_pkey" PRIMARY KEY ("error_id"),
	CONSTRAINT "t_aerrresp_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO






CREATE TABLE "t_action_confirm" (
    "action_id" bigint NOT NULL,
    "confirm_id" bigint NOT NULL,
    "confirm_text" NVARCHAR(max)
)
GO


CREATE INDEX "tactcnfrm_aid_idx"
   ON "t_action_confirm" ("action_id");



CREATE SEQUENCE "t_action_log_action_log_id"
    START WITH 1
    INCREMENT BY 1
  

CREATE TABLE "t_action_log" (
    "action_log_id" bigint DEFAULT(NEXT VALUE FOR "t_action_log_action_log_id") NOT NULL ,
    "action_id" bigint,
    "intent_id" bigint,
    "webhook_url" NVARCHAR(max),
    "call_method" NVARCHAR(max),
    "request_body" NVARCHAR(max),
    "access_code" NVARCHAR(max),
    "result" NVARCHAR(max),
    "created" NVARCHAR(max),
    "entity_id" bigint,
	CONSTRAINT "t_action_log_pkey" PRIMARY KEY ("action_log_id"),
	CONSTRAINT "t_act_log_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO


CREATE INDEX "tactlog_aid_idx"
   ON "t_action_log" ("action_id");
   

CREATE INDEX "tactlog_iid_idx"
   ON "t_action_log" ("intent_id");
   

CREATE INDEX "tactlog_eid_idx"
   ON "t_action_log" ("entity_id");




CREATE SEQUENCE "t_error_resp_id_seq"
    START WITH 1
    INCREMENT BY 1



CREATE TABLE "t_error_response" (
    "id" bigint DEFAULT(NEXT VALUE FOR "t_error_resp_id_seq") NOT NULL ,
    "error_code" NVARCHAR(max),
    "error_response" NVARCHAR(max),
    "kuid" bigint,
    "created" NVARCHAR(max),
    "action_id" bigint,
	CONSTRAINT "t_error_response_pkey" PRIMARY KEY ("id"),
	CONSTRAINT "t_errmsg_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
	CONSTRAINT "t_errresp_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO




CREATE TABLE "t_flowchart_session" (
    "session_id" NVARCHAR(max),
    "flowchart_id" bigint,
    "intent_id" bigint,
    "entity_id" bigint,
    "flowchart_key" bigint,
    "entry_type" NVARCHAR(max),
    "entry_name" NVARCHAR(max),
    "entry_id" bigint,
    "parameter_value" NVARCHAR(max),
	CONSTRAINT "t_flw_sion_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
GO










CREATE TABLE "t_intent_entity" (
    "intent_id" bigint,
    "entity_id" bigint,
    "created" NVARCHAR(max),
    "order" bigint,
    "kuid" bigint,
    "map_id" bigint,
    "required" NVARCHAR(max),
    "flowchart_id" bigint,
	CONSTRAINT "t_int_ent_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE,
	CONSTRAINT "t_int_ent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION
)
GO


       
CREATE INDEX "tintent_iid_idx"
   ON "t_intent_entity" ("intent_id");
   
   
CREATE INDEX "tint_ent_iid_idx"
   ON "t_intent_entity" ("entity_id");
   
   
CREATE INDEX "tentint_mid_idx"
   ON "t_intent_entity" ("map_id");





CREATE TABLE "t_map_regex" (
    "regex_id" bigint NOT NULL,
    "map_id" bigint NOT NULL,
    "created" NVARCHAR(max),
    "kuid" bigint
)
GO

 
   
CREATE INDEX "t_map_regex_mid_idx"
   ON "t_map_regex" ("map_id");
   
   
CREATE INDEX "t_map_regex_rxid_idx"
   ON "t_map_regex" ("regex_id");
   


CREATE SEQUENCE "t_resp_id_seq"
    START WITH 1
    INCREMENT BY 1


CREATE TABLE "t_response" (
    "response_id" bigint DEFAULT(NEXT VALUE FOR "t_resp_id_seq") NOT NULL ,
    "intent_id" bigint,
    "response" NVARCHAR(max),
    "created" NVARCHAR(max),
    "kuid" bigint,
    "entity_id" bigint,
    "error_response" NVARCHAR(max),
	CONSTRAINT "t_response_pkey" primary KEY ("response_id"),
	CONSTRAINT "t_res_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
)
go

   
CREATE INDEX "tresp_iid_idx"
   ON "t_response" ("intent_id");
   
   
CREATE INDEX "tresp_eid_idx"
   ON "t_response" ("entity_id");
   
   
   

CREATE TABLE "t_session_record" (
    "intent_id" bigint,
    "parameter_name" NVARCHAR(max),
    "parameter_type" NVARCHAR(100),
    "session_id" NVARCHAR(max),
    "entity_id" bigint,
    "created" NVARCHAR(max),
    "log_status" NVARCHAR(max),
    "flowchart_id" bigint,
    "flowchart_key" bigint,
    "entity_order" bigint,
	CONSTRAINT "t_ses_rec_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
	CONSTRAINT "t_ses_rec_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,	   
	CONSTRAINT "t_sesrec_t_enttyp_etid_fkey" FOREIGN KEY ("parameter_type")
       REFERENCES "t_entity_type" ("entity_type_code") 
       ON UPDATE CASCADE
       ON DELETE CASCADE
	   
)
GO



CREATE TABLE "t_user_mapping" (
    "mapping_id" bigint NOT NULL,
    "im_user_id" NVARCHAR(100) ,
    "im_platform" NVARCHAR(max),
    "backend_accesscode" NVARCHAR(max),
    "a_code_expiry" NVARCHAR(max),
    "created" NVARCHAR(max)
)
GO


   
CREATE INDEX "tusrmap_imuid_idx"
   ON "t_user_mapping" ("im_user_id");
   



CREATE TABLE user_details (
    user_id bigint NOT NULL,
    username NVARCHAR(max),
    first_name NVARCHAR(max),
    last_name NVARCHAR(max),
    gender NVARCHAR(max),
    password NVARCHAR(max),
    status bigint
)
GO

















	
   
   
   

	

   
   
   
   
   
   
   
   

   

      
   
   


   
  
     

   
























