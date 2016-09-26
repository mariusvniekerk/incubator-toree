package org.apache.toree

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd}
import org.apache.spark.sql.SparkSession
import org.apache.toree.interpreter.Interpreter
import org.apache.toree.kernel.api._
import org.apache.toree.kernel.interpreter.scala.ScalaInterpreter
import org.apache.toree.plugins.PluginManager

/**
  * This is a simple minimal instance that allow us to bind an interpreter with magic support
  * to a preexisting spark session.  The primary purpose of this is to allow users to supply
  * their own mechanism for communicating with this interactive session.
  */
class SimpleSparkInterpreter(existingSparkSession: SparkSession) extends AbstractKernel {

  override def sparkSession: SparkSession = existingSparkSession

  lazy val config: Config = ConfigFactory.load

  lazy val pluginManager: PluginManager = new PluginManager()

  var _interpreter: Interpreter = _
  def interpreter: Interpreter = _interpreter

  def initializeInterpreter() = {
    _interpreter = new ScalaInterpreter(config)
    _interpreter.init(this)
    _interpreter.start()

    // When the spark context stops, kill the interpreter as well
    sparkContext.addSparkListener(new SparkListener {
      override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
        _interpreter.stop()
      }
    }
    )
  }

  /**
    * Stop the SimpleInterpreter
    */
  def shutdown() = {
    _interpreter.stop()
  }

}
