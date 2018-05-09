


CREATE SEQUENCE "tku_kuid_seq"
    START WITH 1
    INCREMENT BY 1;
    
    /

    
CREATE TABLE "t_ku" (
    "kuid" number  primary key ,
    "ku_name" varchar2(3000),
    "created" varchar2(3000)
    );

/
set define off
CREATE OR REPLACE TRIGGER "tku_kuid_seq_trigger"
    BEFORE INSERT 
    ON "t_ku"
    FOR EACH ROW
    BEGIN
    :NEW."kuid":="tku_kuid_seq".nextval;
END;

/

CREATE SEQUENCE "tintent_intentid_seq"
    START WITH 1
    INCREMENT BY 1;
    
/
CREATE TABLE "t_intent" (
    "intent_id" number primary key,
    "intent_definition" varchar2(3000),
    "kuid" number,
    "created" varchar2(3000),
	
	CONSTRAINT "t_intent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE
);
/

set define off
CREATE OR REPLACE TRIGGER "tintent_intentid_seq_trigger"
    BEFORE INSERT 
    ON "t_intent"
    FOR EACH ROW
    BEGIN
    :NEW."intent_id":="tintent_intentid_seq".nextval;
END;

/


CREATE SEQUENCE "tenttyp_etid_seq"
    START WITH 1
    INCREMENT BY 1;
    
/
    
CREATE TABLE "t_entity_type" (
    "entity_type_id" number primary key ,
    "entity_type_code" varchar2(3000) UNIQUE,
    "entity_type_name" varchar2(3000),
    "invalid_message" varchar2(3000),
    "created" varchar2(3000),
    "kuid" number,
	
	CONSTRAINT "t_enttyp_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE
);
/
set define off
CREATE OR REPLACE TRIGGER "tintent_intentid_seq_trigger"
    BEFORE INSERT 
    ON "t_intent"
    FOR EACH ROW
    BEGIN
    :NEW."intent_id":="tintent_intentid_seq".nextval;
END;
/

   
CREATE INDEX "tenttyp_code_idx"
   ON "t_entity_type" ("entity_type_code");



    
   /

CREATE SEQUENCE "tent_eid_seq"
    START WITH 1
    INCREMENT BY 1;
    
/
	
CREATE TABLE "t_entity" (
    "entity_id" number primary key ,
    "entity_name" varchar2(3000),
    "example" varchar2(3000),
    "created" varchar2(3000),
    "entity_type_code" varchar2(3000),
    "kuid" number,
	
	CONSTRAINT "t_ent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE
);
/

set define off
CREATE OR REPLACE TRIGGER "tent_eid_seq_trigger"
    BEFORE INSERT 
    ON "t_entity"
    FOR EACH ROW
    BEGIN
    :NEW."entity_id":="tent_eid_seq".nextval;
END;

/
CREATE SEQUENCE "taction_actionid_seq"
    START WITH 1
    INCREMENT BY 1;
    /


CREATE TABLE "t_action" (
    "action_id" number primary key,
    "intent_id" number,
    "action_name" varchar2(3000),
    "webhook_url" varchar2(3000),
    "request_body" varchar2(3000),
    "call_method" varchar2(3000),
    "created" varchar2(3000),
    "kuid" number,
    "entity_id" number,
    "success_code" varchar2(3000),
    "error_code" varchar2(3000),
    "session_parameter" varchar2(3000),
	
	CONSTRAINT "t_act_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_act_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_act_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       
       ON DELETE CASCADE
);

/
set define off
CREATE OR REPLACE TRIGGER "taction_actionid_seq_trigger"
    BEFORE INSERT 
    ON "t_action"
    FOR EACH ROW
    BEGIN
    :NEW."action_id":="taction_actionid_seq".nextval;
END;

	/
   

CREATE INDEX "tact_iid_idx"
   ON "t_action" ("intent_id");
   /

CREATE INDEX "tact_eid_idx"
   ON "t_action" ("entity_id");

   
   /
   

CREATE TABLE "t_im_session" (
    "session_id" varchar2(3000) PRIMARY key,
    "im_user_id" varchar2(3000),
    "created_at" varchar2(3000),
    "expired_at" varchar2(3000),
    "reason" varchar2(3000)
)
;
   
   /

CREATE SEQUENCE "tconv_cid_seq"
    START WITH 1
    INCREMENT BY 1;
    
    /


CREATE TABLE "t_conversation" (
    "conversation_id" number primary key,
    "intent_id" number,
    "message_id" varchar2(3000),
    "probability" varchar2(3000),
    "total_intent" number,
    "created" varchar2(3000),
    "kuid" number,
	
	CONSTRAINT "t_conv_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id")        
       ON DELETE CASCADE,
	CONSTRAINT "t_conv_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid")        
       ON DELETE CASCADE
)
;
/

set define off
CREATE OR REPLACE TRIGGER "tconv_cid_seq_trigger"
    BEFORE INSERT 
    ON "t_conversation"
    FOR EACH ROW
    BEGIN
    :NEW."conversation_id":="tconv_cid_seq".nextval;
END;


/

CREATE INDEX "tconv_iid_idx"
   ON "t_conversation" ("intent_id");


/
CREATE SEQUENCE "tentques_qid_seq"
    START WITH 1
    INCREMENT BY 1;
        
    
/

CREATE TABLE "t_entity_question" (
    "question_id" number primary key,
    "entity_id" number,
    "question" varchar2(3000),
    "created" varchar2(3000),
    "kuid" number,
    "title" varchar2(3000),
    "button_text" varchar2(3000),
    "sub_title" varchar2(3000),
    "entity_query" varchar2(3000),
	
	CONSTRAINT "t_entque_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_entque_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       
       ON DELETE CASCADE
)
;

/
set define off
CREATE OR REPLACE TRIGGER "tconv_cid_seq_trigger"
    BEFORE INSERT 
    ON "t_conversation"
    FOR EACH ROW
    BEGIN
    :NEW."conversation_id":="tconv_cid_seq".nextval;
END;
/
CREATE INDEX "tentque_eid_idx"
   ON "t_entity_question" ("entity_id");
   

/





CREATE SEQUENCE "tflwchrt_fid_seq"
    START WITH 1
    INCREMENT BY 1;
/
CREATE TABLE "t_flowchart" (
    "flowchart_id" number primary key ,
    "flowchart_info" varchar2(3000),
    "created" varchar2(3000)
	)
;
    /
set define off
CREATE OR REPLACE TRIGGER "tflwchrt_fid_seq_trigger"
    BEFORE INSERT 
    ON "t_flowchart"
    FOR EACH ROW
    BEGIN
    :NEW."flowchart_id":="tflwchrt_fid_seq".nextval;
END;

/


CREATE SEQUENCE "timal_logid_seq"
    START WITH 1
    INCREMENT BY 1;
    /
    
    
CREATE TABLE "t_im_session_log" (
    "log_id" number primary key ,
    "session_id" varchar2(3000),
    "message" varchar2(3000),
    "source" varchar2(3000),
    "created" varchar2(3000),
    "intent_id" number,
	   CONSTRAINT "t_imsl_t_ims_ssid_fkey" FOREIGN KEY ("session_id")
       REFERENCES "t_im_session" ("session_id") 
       
       ON DELETE CASCADE
)
;
/
    
set define off
CREATE OR REPLACE TRIGGER "timal_logid_seq_trigger"
    BEFORE INSERT 
    ON "t_im_session_log"
    FOR EACH ROW
    BEGIN
    :NEW."log_id":="timal_logid_seq".nextval;
END;
  
/
      
CREATE INDEX "timseslog_sid_idx"
   ON "t_im_session_log" ("session_id");


/

CREATE SEQUENCE "tkrlog_krlogid_seq"
    START WITH 1
    INCREMENT BY 1;
    
	/
CREATE TABLE "t_kr_log" (
    "kr_log_id" number primary key ,
    "im_session_log_id" number,
    "intent_id" number,
    "keyword_rate" integer,
    "created" varchar2(3000),

  
	   CONSTRAINT "t_krlog_t_imsl_iid_fkey" FOREIGN KEY ("im_session_log_id")
       REFERENCES "t_im_session_log" ("log_id") 
       
       ON DELETE CASCADE
)
;
/

   set define off
CREATE OR REPLACE TRIGGER "tkrlog_krlogid_seq_trigger"
    BEFORE INSERT 
    ON "t_kr_log"
    FOR EACH ROW
    BEGIN
    :NEW."kr_log_id":="tkrlog_krlogid_seq".nextval;
END;

/

   
CREATE INDEX "tKRLog_IMSLID_idx"
   ON "t_kr_log" ("im_session_log_id");
   
   /
CREATE INDEX "tKRLog_IID_idx"
   ON "t_kr_log" ("intent_id");

/



CREATE SEQUENCE "tkeywrd_kid_seq"
    START WITH 1
    INCREMENT BY 1;
    
   / 
    
CREATE TABLE "t_keyword" (
    "keyword_id" number primary key ,
    "intent_id" number,
    "keyword" varchar2(3000),
    "polarity" varchar2(3000),
    "created" varchar2(3000),
    "kuid" number,
	
	CONSTRAINT "t_ku_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_keywrd_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE
);
/

   set define off
CREATE OR REPLACE TRIGGER "tkeywrd_kid_seq_trigger"
    BEFORE INSERT 
    ON "t_keyword"
    FOR EACH ROW
    BEGIN
    :NEW."keyword_id":="tkeywrd_kid_seq".nextval;
END;

/

CREATE SEQUENCE "tregex_rid_seq"
    START WITH 1
    INCREMENT BY 1;
    
    /
CREATE TABLE "t_regex" (
    "regex_id" number primary key,
    "expression" varchar2(3000),
    "nm_message" varchar2(3000),
    "created" varchar2(3000),
    "regex_name" varchar2(3000)
);
/
   set define off
CREATE OR REPLACE TRIGGER "tregex_rid_seq_trigger"
    BEFORE INSERT 
    ON "t_regex"
    FOR EACH ROW
    BEGIN
    :NEW."regex_id":="tregex_rid_seq".nextval;
END;
/

CREATE SEQUENCE "tuserrating_rid_seq"
    START WITH 1
    INCREMENT BY 1;
 
/


CREATE TABLE "t_user_rating" (
    "rating_id" number primary key,
    "user_id" varchar2(3000),
    "im_session_log_id" number,
    "keywords" varchar2(3000),
    "intent_id" number,
    "created" varchar2(3000),
	
	   CONSTRAINT "t_usrrat_t_imsl_iid_fkey" FOREIGN KEY ("im_session_log_id")
       REFERENCES "t_im_session_log" ("log_id") 
       
       ON DELETE CASCADE
)
;
/
  set define off
CREATE OR REPLACE TRIGGER "tuserrating_rid_seq_trigger"
    BEFORE INSERT 
    ON "t_user_rating"
    FOR EACH ROW
    BEGIN
    :NEW."rating_id":="tuserrating_rid_seq".nextval;
END;
/
   
CREATE INDEX "tusrrat_imslid_idx"
   ON "t_user_rating" ("im_session_log_id");
   
   /
CREATE INDEX "tusrrat_iid_idx"
   ON "t_user_rating" ("intent_id");
/


   


CREATE SEQUENCE "taerrresp_eid_seq"
    START WITH 1
    INCREMENT BY 1;
/
CREATE TABLE "t_a_error_response" (
    "error_id" number primary key,
    "action_id" number,
    "action_error_response" varchar2(3000),
    "created" varchar2(3000),
	
	CONSTRAINT "t_aerrresp_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       
       ON DELETE CASCADE
)
;
/

  set define off
CREATE OR REPLACE TRIGGER "taerrresp_eid_seq_trigger"
    BEFORE INSERT 
    ON "t_a_error_response"
    FOR EACH ROW
    BEGIN
    :NEW."error_id":="taerrresp_eid_seq".nextval;
END;


/

CREATE TABLE "t_action_confirm" (
    "action_id" number NOT NULL,
    "confirm_id" number NOT NULL,
    "confirm_text" varchar2(3000)
)
;
/

CREATE INDEX "tactcnfrm_aid_idx"
   ON "t_action_confirm" ("action_id");

/

CREATE SEQUENCE "t_actlog_alid_seq"
    START WITH 1
    INCREMENT BY 1;
  /

CREATE TABLE "t_action_log" (
    "action_log_id" number primary key,
    "action_id" number,
    "intent_id" number,
    "webhook_url" varchar2(3000),
    "call_method" varchar2(3000),
    "request_body" varchar2(3000),
    "access_code" varchar2(3000),
    "result" varchar2(3000),
    "created" varchar2(3000),
    "entity_id" number,
	
	CONSTRAINT "t_act_log_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       
       ON DELETE CASCADE
)
;
/
 set define off
CREATE OR REPLACE TRIGGER "t_actlog_alid_seq_trigger"
    BEFORE INSERT 
    ON "t_action_log"
    FOR EACH ROW
    BEGIN
    :NEW."action_log_id":="t_actlog_alid_seq".nextval;
END;

/
CREATE INDEX "tactlog_aid_idx"
   ON "t_action_log" ("action_id");
   
/
CREATE INDEX "tactlog_iid_idx"
   ON "t_action_log" ("intent_id");
   
/
CREATE INDEX "tactlog_eid_idx"
   ON "t_action_log" ("entity_id");
/



CREATE SEQUENCE "t_error_resp_id_seq"
    START WITH 1
    INCREMENT BY 1;

/

CREATE TABLE "t_error_response" (
    "id" number primary key,
    "error_code" varchar2(3000),
    "error_response" varchar2(3000),
    "kuid" number,
    "created" varchar2(3000),
    "action_id" number,
	CONSTRAINT "t_errmsg_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_errresp_tact_aid_fkey" FOREIGN KEY ("action_id")
       REFERENCES "t_action" ("action_id") 
       
       ON DELETE CASCADE
)
;
/
 set define off
CREATE OR REPLACE TRIGGER "t_error_resp_id_seq_trigger"
    BEFORE INSERT 
    ON "t_error_response"
    FOR EACH ROW
    BEGIN
    :NEW."id":="t_error_resp_id_seq".nextval;
END;
/

CREATE TABLE "t_flowchart_session" (
    "session_id" varchar2(3000),
    "flowchart_id" number,
    "intent_id" number,
    "entity_id" number,
    "flowchart_key" number,
    "entry_type" varchar2(3000),
    "entry_name" varchar2(3000),
    "entry_id" number,
    "parameter_value" varchar2(3000),
	CONSTRAINT "t_flw_sion_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE
)
;

/








CREATE TABLE "t_intent_entity" (
    "intent_id" number,
    "entity_id" number,
    "created" varchar2(3000),
    "order" number,
    "kuid" number,
    "map_id" number,
    "required" varchar2(3000),
    "flowchart_id" number,
	CONSTRAINT "t_int_ent_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_int_ent_t_ku_kuid_fkey" FOREIGN KEY ("kuid")
       REFERENCES "t_ku" ("kuid") 
       
       ON DELETE CASCADE
)
;

/
       
CREATE INDEX "tintent_iid_idx"
   ON "t_intent_entity" ("intent_id");
   
   /
CREATE INDEX "tint_ent_iid_idx"
   ON "t_intent_entity" ("entity_id");
   /
   
CREATE INDEX "tentint_mid_idx"
   ON "t_intent_entity" ("map_id");


/


CREATE TABLE "t_map_regex" (
    "regex_id" number NOT NULL,
    "map_id" number NOT NULL,
    "created" varchar2(3000),
    "kuid" number
)
;
/
 
   
CREATE INDEX "t_map_regex_mid_idx"
   ON "t_map_regex" ("map_id");
   /
   
CREATE INDEX "t_map_regex_rxid_idx"
   ON "t_map_regex" ("regex_id");
   
/

CREATE SEQUENCE "t_resp_id_seq"
    START WITH 1
    INCREMENT BY 1;
/

CREATE TABLE "t_response" (
    "response_id" number primary key,
    "intent_id" number,
    "response" varchar2(3000),
    "created" varchar2(3000),
    "kuid" number,
    "entity_id" number,
    "error_response" varchar2(3000),
	
	CONSTRAINT "t_res_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE
)
;
/
 set define off
CREATE OR REPLACE TRIGGER "t_resp_id_seq_trigger"
    BEFORE INSERT 
    ON "t_response"
    FOR EACH ROW
    BEGIN
    :NEW."response_id":="t_resp_id_seq".nextval;
END;

   /
CREATE INDEX "tresp_iid_idx"
   ON "t_response" ("intent_id");
   /
   
CREATE INDEX "tresp_eid_idx"
   ON "t_response" ("entity_id");
   
   /
   

CREATE TABLE "t_session_record" (
    "intent_id" number,
    "parameter_name" varchar2(3000),
    "parameter_type" varchar2(3000),
    "session_id" varchar2(3000),
    "entity_id" number,
    "created" varchar2(3000),
    "log_status" varchar2(3000),
    "flowchart_id" number,
    "flowchart_key" number,
    "entity_order" number,
	CONSTRAINT "t_ses_rec_t_int_iid_fkey" FOREIGN KEY ("intent_id")
       REFERENCES "t_intent" ("intent_id") 
       
       ON DELETE CASCADE,
	CONSTRAINT "t_ses_rec_t_ent_eid_fkey" FOREIGN KEY ("entity_id")
       REFERENCES "t_entity" ("entity_id") 
       
       ON DELETE CASCADE,	   
	CONSTRAINT "t_sesrec_t_enttyp_etid_fkey" FOREIGN KEY ("parameter_type")
       REFERENCES "t_entity_type" ("entity_type_code") 
       
       ON DELETE CASCADE
	   
)
;
/


CREATE TABLE "t_user_mapping" (
    "mapping_id" number NOT NULL,
    "im_user_id" varchar2(3000) ,
    "im_platform" varchar2(3000),
    "backend_accesscode" varchar2(3000),
    "a_code_expiry" varchar2(3000),
    "created" varchar2(3000)
)
;

/
   
CREATE INDEX "tusrmap_imuid_idx"
   ON "t_user_mapping" ("im_user_id");
   

/

CREATE TABLE user_details (
    user_id number NOT NULL,
    username varchar2(3000),
    first_name varchar2(3000),
    last_name varchar2(3000),
    gender varchar2(3000),
    password varchar2(3000),
    status number
)
;

/
