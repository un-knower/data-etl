package com.eric.utils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

object FileUtil extends DataHandler {

  override def read[T: ClassTag](sc: SparkContext): RDD[T] = ???

  override def save[T: ClassTag](rdd: RDD[T]): Unit = ???
}
