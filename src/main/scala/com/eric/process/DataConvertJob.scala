package com.eric.process

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

/**
  * 数据转换Job
  * Created by dongzeheng on 2018/2/10.
  */
object DataConvertJob {
  val LOGGER = LoggerFactory.getLogger(DataConvertJob.getClass)
  val PATH = "/home/recources/data1.csv"

  def loadData(sparkSession: SparkSession, startTime: String, stopTime: String):DataFrame = {
    val ds = DataLoadJob.loadFromHBase(sparkSession, startTime, stopTime)
    ds.toDF()
  }

  def convert(sparkSession: SparkSession) = {
    import sparkSession.implicits._



  }

  def saveData(vehicleDF: DataFrame) = {

    vehicleDF.write.csv(PATH)

  }



}
