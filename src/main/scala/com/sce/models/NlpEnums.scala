package com.sce.models


object DBTables extends Enumeration {

  type DBTables = Value

  val TENTITYQUESTIONS = "t_entity_question"
  val TKEYWORD = "t_keyword"

}

object DBEnumeration extends Enumeration {

  type DBEnumeration = Value

  val POSTGRESQL = "postgresql"
  val MSSQL = "mssql"
  val ORACLE = "oracle"
  val MYSQL = "mysql"
}


object EntityTypeCodes extends Enumeration {

  type EntityTypeCodes = Value

  val ELS = "ELS"
  val EILS = "EILS"
  val LST = "LST"
  val ILST = "ILST"
  val ATCMT = "ATCMT"
  val LNR = "LNR"
  val EQRP = "EQRP"
  val QRP = "QRP"

}

object NLPRegexs extends Enumeration {

  type NLPRegexs = Value
  
  val _PUNCTUATION_REGEX = "^[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]|[]<>\\[.,#@\\%\\^\\*\\&!`\\+~\\{}()?'\"\\\\:;/=_-]$"
  //val _ARABIC_NUMERIC_REGEX = """^-?[]*(\.[٠١٢٣٤٥٦٧٨٩]+)?$"""
  //val _ALPHA_NUMERIC_REGEX = """^-?[0-9]*(\.[0-9]+)?$"""
  
 val _ARABIC_ALPHA_NUMERIC_REGEX = """^(-|\+)?[0-9]*(\.[0-9]+)?$|^(-|\+)?[٠١٢٣٤٥٦٧٨٩]*(\.[٠١٢٣٤٥٦٧٨٩]+)?$"""
  // val _ARABIC_ALPHA_NUMERIC_REGEX = """^-?[0-9]*(\.[0-9]+)?$|^-?(+[٠١٢٣٤٥٦٧٨٩].\)*[٠١٢٣٤٥٦٧٨٩]?$"""
  //$?(+[٠١٢٣٤٥٦٧٨٩].\)*[٠١٢٣٤٥٦٧٨٩]?-^|$?(+[0-9].\)*[0-9]?-^
 
 val ENTITY_PLACEHOLDER_REGEX = "\\%(.*?)\\%"
 val ACTION_PLACEHOLDER_REGEX = "\\@(.*?)\\@"
}

object NLPStrings extends Enumeration {

  type NLPStrings = Value
  
  val TEST = "TEST"
  val DEV = "DEV"
  val PROD = "PROD"
  val JSON = "json"
  val X_WWW_FORM_URLENCODED = "x-www-form-urlencoded"
  val REST_GET = "GET"
  val TEXT = "TEXT"
  val FILE = "FILE"
  val FORM_DATA = "multipart/form-data"
  val ENV_PROD_PATH = "./"
  val ENV_TEST_PATH = "C:/SCS-JAR/"
  val BC_DB_CONF_PATH = "config/dbconfig/"
  val BC_APP_CONF_PATH = "config/appconfig/"
  val ORACLE_CONF_PATH = "oracle.conf"
  val MYSQL_CONF_PATH = "mysql.conf"
  val MSSQL_CONF_PATH = "mssql.conf"
  val POSTGRES_CONF_PATH = "postgresql.conf"
  val APP_CONF_PATH = "application.conf"
  val IMAL_RECEIVE = "IMAL_RECEIVE"
  val FACEBOOK = "facebook"
  val PURIST = "purist"
  val PURIST_CONVID_URL_TEXT = "{conversationID}"
  val FB_URL_TOKEN_TXT = "access_token="
  val USER = "USER"
  val BOT = "BOT"
  val PLSWAIT = "SCE_PLSWAIT_MSG"
  val REASON = "New User Session has Created"
  val _NO_SOURCE_RESPONSE = "NO_SOURCE_RESPONSE"
  val CNCLCNFRMTYPE = "CANCEL"
  val ATCMTCNFRMTYPE = "ATCMT"
  val LANGCNFRMTYPE = "LANGUAGE"
  val LANGUAGE = "LANGUAGE"
  val INVLDCNFRMTYPE = "INVALID"
  val LANGCNFRM_DEFLT_MSG = "LANGCNFRM_DEFLT_MSG"
  val _YES = "yes"
  val _NO = "no"
  val DEFAULT_LANG = "en"
  val _PENDING = "PENDING"
  val _INIT = "INIT"
  val _ATTACHMENT = "ATTACHMENT"
  val ENTITY_VALUE = "ENTITY_VALUE"
  val _INTENT = "INTENT"
  val _WORKFLOW = "WORKFLOW"
  val _CONFIRMATION = "CONFIRMATION" 
  val ACTION = "ACTION"
  val INTENT_CHOICE = "INTENT_CHOICE"
  val _ENTITY = "ENTITY"
  val CANCEL = "CANCEL"
  val _FILLER = "FILLER"
  val _RESPONSE = "RESPONSE"
  val _Y = "Y"
  val _N = "N"  
  val _START = "START"
  val _PRIMARY = "PRIMARY"
  val _SECONDARY = "SECONDARY"
  val WELCOME_MSG = "WELCOME_MSG"
  val _EQRP = "EQRP"
  val SCRIPT_ENGINE_NAME = "nashorn"
  val ENTITY_NAME = "{EntityName}"
  val ENTITY_USER_VALUE = "{EntityUserValue}"
  val AUTHORIZATION = "Authorization"
  val BEARER = "Bearer "
  val TOKEN_URL = "TOKEN_URL"
  val OPERATOR_PASSWORD = "OPERATOR_PASSWORD"
  val OPERATOR_USERNAME = "OPERATOR_USERNAME"
  val OP_LOGIN_REQ_BODY = """{"grant_type": "password", "username":"{username}", "password":"{password}", "client_id":"na"}"""

  
}


object NLPErrorCodes extends Enumeration {

  type NLPErrorCodes = Value

  val SCE_INTENT_NOT_FOUND = "SCE_INTENT_NOT_FOUND"
  val SCE_ENTITY_NOT_FOUND = "SCE_ENTITY_NOT_FOUND"
  val SCRIPT_ENGINE_EXCEPTION = "SCRIPT_ENGINE_EXCEPTION"
  val INVALID_SELECTION = "INVALID_SELECTION"
  val SCE_SERVER_DOWN = "SCE_SERVER_DOWN"
  val SCE_TRNSACT_TRMNATE = "Transaction was terminated"
  val SCE_TRNSACT_CNCL_CNFRM = "Do you want to cancel "
  val SCE_FILEUPLD_ERROR = "FILE_UPLOAD_ERROR"
  val SCE_FILELIMIT_ERROR = "SCE_FILELIMIT_ERROR"
  val BC_PLS_UPLOAD_FILE = "BC_PLS_UPLOAD_FILE"
  val SCE_LANG_NOT_FOUND = "SCE_LANG_NOT_FOUND"
  val ACTION_CALL_INTERVAL_ERRMSG = "ACTION_CALL_INTERVAL_ERRMSG"
  val ACTION_CALL_EXCEPTION = "ACTION_CALL_EXCEPTION"
  val ACTION_CONF_EXCEPTION = "ACTION_CONF_EXCEPTION"

}



