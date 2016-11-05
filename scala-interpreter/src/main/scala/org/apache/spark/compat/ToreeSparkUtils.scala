package org.apache.spark.compat

import org.apache.spark.util.{MutableURLClassLoader, Utils}

/**
  * Created by mniekerk on 11/4/16.
  */
object ToreeSparkUtils {

  def getSparkClassLoader: ClassLoader =
    Utils.getContextOrSparkClassLoader

  // This is used to ensure that the correct classloader is used during
  // testing.
  def makeClassloader(parent: ClassLoader): MutableURLClassLoader = {
    new MutableURLClassLoader(Array.empty, parent)
  }

}
