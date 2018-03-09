package com.eric.process

import com.eric.common.DateTimeUtils
import com.eric.data.VehicleData
import org.apache.spark
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
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
        val date = DateTimeUtils.format(record.passtime.toLong, "yyyy-MM-dd HH:mm")
        date.substring(0, date.length - 1) + "0"
      })
      a
    })

    val grouped = dateDS.groupBy("passtime", "crossid").count().toJSON
    val dataArray = grouped.take(10)
    grouped.toDF().repartition(1).write.csv("/home/17111")
    grouped.collect()
    dataArray
  }

  def listToString(list: List[String]) = {

  }

  def saveData(vehicleDF: DataFrame) = {

    vehicleDF.write.csv(PATH)

  }

  def saveData(dataArray: Array[String]) = {
    dataArray
  }



}
