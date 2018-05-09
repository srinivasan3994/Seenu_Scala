package com.sce.utils

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.sce.utils.AppConf._
import com.sce.models.NLPStrings._
import com.sce.services._
import java.io.FileReader
import javax.script.Invocable
import com.sce.exception.SCSException
import akka.event.{ Logging }
import com.sce.utils.AppUtils._
import com.sce.exception.ScriptEngineException

object ScriptEngineProcessor {

  val logger = Logging(system, this.getClass)
  
  def evlutSimplDiamndExpr(expr: String): Boolean = {

    try {

      
      logger.info("expr: {}", expr)
      val sem: ScriptEngineManager = new ScriptEngineManager(null)
      val factories = sem.getEngineFactories
      logger.info("sem.getEngineFactories: " + sem.getEngineFactories.size())
      val se: ScriptEngine = sem.getEngineByName(SCRIPT_ENGINE_NAME)
      val resultObj = se.eval(expr)
      val finalResult = resultObj.toString.toBoolean
      logger.info("finalResult: {}", finalResult)
      finalResult

    } catch {

      case e: ScriptException =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        logger.info(e.getMessage)
        // e.printStackTrace();
        throw new ScriptEngineException("SCRIPT_ENGINE_EXCEPTION")
      case e: Exception =>
        logger.info(e.getMessage)
        //  e.printStackTrace();
        throw new ScriptEngineException("SCRIPT_ENGINE_EXCEPTION")
    }
  }

  def booleanExprEvaluator(expr: String) = {

    try {

      logger.info("expr: {}", expr)
      val sem: ScriptEngineManager = new ScriptEngineManager(null)
      val factories = sem.getEngineFactories
      logger.info("sem.getEngineFactories: " + sem.getEngineFactories.size())
      val se: ScriptEngine = sem.getEngineByName("nashorn")

      //date comparison code starts from here
      se.eval("var dateValue = " + "new Date(2012, 02, 01,12,30);");
      se.eval("var test = 2012-02-01 <=" + " new Date(2012, 12, 01,12,00);")
      val finalResult = se.get("test").toString.toBoolean

      /*  logger.info("finalResult: {}", finalResult)
      finalResult


       se.eval("function sum(a, b) { return a + b; }");
    System.out.println(se.eval("sum(1, 2);"));*/

      //se.eval(new FileReader("F:/ScriptEngine/mustache.js-master/mustache.js"));
      val invocable = se.asInstanceOf[Invocable];

      /*se.eval("function myFunction(name){var output = '';"
          + "  for (i = 0; i <= name.length; i++) {output = name.charAt(i)+'-'+ output"
          + "  } return output;}");*/

      se.eval(new FileReader("F:/ScriptEngine/moment.js"))
      se.eval(new FileReader("F:/ScriptEngine/sample.js"))

      val o = invocable.invokeFunction("nextDay", "01-23-2016");
      System.out.println(o);
      /* val result = se.get("nextDay").toString
      println("result: "+result)
      println("dfgfh: "+se.get("d").toString)
    */

      /* logger.info("expr: {}", expr)
      val sem: ScriptEngineManager = new ScriptEngineManager(null)
      val factories = sem.getEngineFactories
      println("sem.getEngineFactories: " + sem.getEngineFactories.size())
      val se: ScriptEngine = sem.getEngineByName("JavaScript")

      val resultObj = se.eval(expr)
      val finalResult = resultObj.toString.toBoolean
      logger.info("finalResult: {}", finalResult)
      finalResult*/

      true
    } catch {

      case e: ScriptException =>
        KafkaService.sendErrorLogsToKafkaProducer("", "", e.getMessage, "", 0L, 0L)
        logger.info(e.getMessage)
        // e.printStackTrace();
        false
      case e: Exception =>
        logger.info(e.getMessage)
        //  e.printStackTrace();
        false
    }

  }

}
