/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package org.apache.toree.kernel.interpreter.scala

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.scala.FlinkILoop
import org.apache.toree.interpreter._
import org.apache.toree.kernel.api.BaseKernelLike

import scala.language.reflectiveCalls

class FlinkScalaInterpreter(private val config:Config = ConfigFactory.load) extends ScalaInterpreter {

  // mimic functionality from here
  var flinkILoop: FlinkILoop = _


  /**
    * This has to be called first to initialize all the settings.
    *
    * @return The newly initialized interpreter
    */
   override def init(kernel: BaseKernelLike): Interpreter = {
     super.init(kernel)

     this
   }


   def bindSparkContext() = {
     val bindName = "sc"

     doQuietly {
       logger.info(s"Binding SparkContext into interpreter as $bindName")
      interpret(s"""def $bindName: ${classOf[SparkContext].getName} = kernel.sparkContext""")

       // NOTE: This is needed because interpreter blows up after adding
       //       dependencies to SparkContext and Interpreter before the
       //       cluster has been used... not exactly sure why this is the case
       // TODO: Investigate why the cluster has to be initialized in the kernel
       //       to avoid the kernel's interpreter blowing up (must be done
       //       inside the interpreter)
       logger.debug("Initializing Spark cluster in interpreter")

//       doQuietly {
//         interpret(Seq(
//           "val $toBeNulled = {",
//           "  var $toBeNulled = sc.emptyRDD.collect()",
//           "  $toBeNulled = null",
//           "}"
//         ).mkString("\n").trim())
//       }
     }
   }

  def bindSparkSession(): Unit = {
    val bindName = "spark"

     doQuietly {
       // TODO: This only adds the context to the main interpreter AND
       //       is limited to the Scala interpreter interface
       logger.debug(s"Binding SQLContext into interpreter as $bindName")

      interpret(s"""def $bindName: ${classOf[SparkSession].getName} = kernel.sparkSession""")

//      interpret(
//        s"""
//           |def $bindName: ${classOf[SparkSession].getName} = {
//           |   if (org.apache.toree.kernel.interpreter.scala.InterpreterHelper.sparkSession != null) {
//           |     org.apache.toree.kernel.interpreter.scala.InterpreterHelper.sparkSession
//           |   } else {
//           |     val s = org.apache.spark.repl.Main.createSparkSession()
//           |     org.apache.toree.kernel.interpreter.scala.InterpreterHelper.sparkSession = s
//           |     s
//           |   }
//           |}
//         """.stripMargin)

     }
   }

 }

object FlinkScalaInterpreter {


}

