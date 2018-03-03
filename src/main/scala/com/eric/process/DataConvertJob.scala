package com.eric.process

import com.eric.common.DateTimeUtils
import com.eric.data.VehicleData
import org.apache.spark.sql.{Dataset, DataFrame, SparkSession}
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

  def convert(sparkSession: SparkSession, dataSet: Dataset[VehicleData]) = {
    import sparkSession.implicits._
    val dateDS = dataSet.map(record => {
      val a = record.copy(passtime = {
        DateTimeUtils.format(record.passtime.toLong, "yyyy-MM-dd HH:mm")
      })
      a
    })
    val grouped = dateDS.groupBy("passtime", "corssid").count()
    grouped

  }

  def saveData(vehicleDF: DataFrame) = {

    vehicleDF.write.csv(PATH)

  }



}
