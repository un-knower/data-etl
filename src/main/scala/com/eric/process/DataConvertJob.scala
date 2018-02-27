package com.eric.process

import com.eric.data.VehicleData
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.slf4j.LoggerFactory

/**
  * 数据转换Job
  * Created by dongzeheng on 2018/2/10.
  */
object DataConvertJob {
  val LOGGER = LoggerFactory.getLogger(DataConvertJob.getClass)
  val PATH = "/home/recources/data.csv"

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
