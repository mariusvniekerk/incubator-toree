package org.apache.toree.kernel.api

import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
;


trait SparkKernelLike {

  def sparkContext: SparkContext = sparkSession.sparkContext

  def sparkConf: SparkConf = sparkSession.sparkContext.getConf

  def javaSparkContext: JavaSparkContext = new JavaSparkContext(sparkContext)

  private var _sparkSession: SparkSession = _

  def sparkSession: SparkSession = _sparkSession

  def createSparkConf(): SparkConf = {
    new SparkConf().set("spark.submit.deployMode", "client")
  }

  def createSparkSession(conf: SparkConf) = {
    _sparkSession = SparkSession.builder().config(conf).getOrCreate()
    _sparkSession
  }

}

trait KernelLike extends EvaluateKernelLike with SparkKernelLike {

}
