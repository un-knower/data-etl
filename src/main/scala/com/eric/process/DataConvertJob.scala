package com.eric.process

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.slf4j.LoggerFactory

/**
  * 数据转换Job
  * Created by dongzeheng on 2018/2/10.
  */
object DataConvertJob {
  val LOGGER = LoggerFactory.getLogger(DataConvertJob.getClass)

  def loadData(sparkSession: SparkSession, startTime: String, stopTime: String) = {

  }

  def convert(sparkSession: SparkSession) = {
    import sparkSession.implicits._



  }



}
