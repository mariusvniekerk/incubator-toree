package org.apache.spark.compat

import java.net.URL

import org.apache.spark.util.MutableURLClassLoader

/**
  * Created by mniekerk on 11/4/16.
  */
object SparkClassLoaderHelper {

  def addjars(classLoader: ClassLoader, jars: URL*): Unit = {

    classLoader match {
      case cl: MutableURLClassLoader =>
        jars.foreach(cl.addURL)
    }
  }

}
