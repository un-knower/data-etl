package com.eric

import com.eric.data.VehicleData
import com.eric.process.{CreatGraphJob, DataConvertJob}
import com.eric.utils.SparkHoldUtil
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object GraphExportTask {

  val LOGGER = LoggerFactory.getLogger(GraphExportTask.getClass)

  def execute(sparkSession: SparkSession, zk: String) = {
    val graph = CreatGraphJob.createGraph(sparkSession, "1711", "17119999999999999999")
    import sparkSession.implicits._
    graph.vertices.repartition(1).saveAsTextFile("graph/vertices")
    graph.edges.repartition(1).saveAsTextFile("graph/edges")
  }

  def main(args: Array[String]): Unit = {
//    if (args.length != 6) {
//      System.err.println(
//        s"""
//           |Usage: GraphExportTask
//           |  <sparkMaster> the Spark master URL to connect to
//           |  <kafkaZK> zookeeper to connect to
//           |  <checkPointDir> dir to checkpoint
//           |  <executorMemory> executor memory to use
//           |  <cores> cores
//           |  <storageFraction> the fraction of memory used for storage
//           |
//         """.stripMargin)
//      return
//    }

//    val Array(sparkMaster,
//    kafkaZK,
//    checkPointDir,
//    executorMemory,
//    cores,
//    storageFraction) = args

    //val sparkSession = SparkHoldUtil.getSparkSession("Graph-generate", sparkMaster, executorMemory, cores, storageFraction, checkPointDir)

    val sparkSession = SparkSession
      .builder
      .appName("Spark Pi")
      .getOrCreate()
    val kafkaZK = args.apply(1)
    execute(sparkSession, kafkaZK)
  }

}