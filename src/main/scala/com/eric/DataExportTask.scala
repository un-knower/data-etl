package com.eric

import com.eric.data.{VehicleData, VehicleDateData}
import com.eric.meta.HBaseVehicleRecord
import com.eric.process.DataConvertJob
import com.eric.utils.SparkHoldUtil
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

/**
  * Created by dongzeheng on 2018/2/27.
  */
object DataExportTask {

  val LOGGER = LoggerFactory.getLogger(DataExportTask.getClass)

  def execute(sparkSession: SparkSession, zk: String) = {
    val df = DataConvertJob.loadData(sparkSession, "1711", "17119999999999999999")
    import sparkSession.implicits._
    DataConvertJob.convert(sparkSession, df.as[VehicleData])
    //DataConvertJob.saveData(df)
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 6) {
      System.err.println(
        s"""
           |Usage: RealTimeStat
           |  <sparkMaster> the Spark master URL to connect to
           |  <kafkaZK> zookeeper to connect to
           |  <checkPointDir> dir to checkpoint
           |  <executorMemory> executor memory to use
           |  <cores> cores
           |  <storageFraction> the fraction of memory used for storage
           |
         """.stripMargin)
      return
    }

    val Array(sparkMaster,
    kafkaZK,
    checkPointDir,
    executorMemory,
    cores,
    storageFraction) = args

    val sparkSession = SparkHoldUtil.getSparkSession("data-etl", sparkMaster, executorMemory, cores, storageFraction, checkPointDir)
    execute(sparkSession, kafkaZK)
  }

}
