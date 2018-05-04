package com.eric.utils

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

import scala.reflect.ClassTag

abstract class DataHandler {

  def read[T: ClassTag](sc: SparkContext): RDD[T]

  def save[T: ClassTag](rdd: RDD[T])
}
