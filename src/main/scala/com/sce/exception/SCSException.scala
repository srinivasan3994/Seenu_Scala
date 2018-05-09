package com.sce.exception

case class SCSException(exception: String)  extends Exception(exception)
case class BCActionFailureException(exception: String ) extends Exception(exception)
case class BCActionConfException(exception: String ) extends Exception(exception)
case class UserMsgProcessingException(exception: String ) extends Exception(exception)
case class KeywordIdentificationException(exception: String ) extends Exception(exception)
case class ScriptEngineException(exception: String ) extends Exception(exception)