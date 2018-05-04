package com.eric.process

import com.eric.data.VehicleData
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.LoggerFactory
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD



object CreatGraphJob {
  val LOGGER = LoggerFactory.getLogger(CreatGraphJob.getClass)
  case class coloredVehicleData(plateColorNumber: String, crossid: String, passtime: String)

  def createGraph(sparkSession: SparkSession, startTime: String, stopTime: String): Graph[Long, Int] = {
    val ds = DataLoadJob.loadFromHBase(sparkSession, startTime, stopTime)
    val vertexRDD = createVertex(ds)

    val test = vertexRDD.take(10)

    val edgeRDD = createEdge(ds)
    Graph(vertexRDD, edgeRDD)
  }

  /**
    * 获取vertex Dataset
    * @param vehicleDS
    * @return vertexDS
    */
  def createVertex(vehicleDS: Dataset[VehicleData]): RDD[(VertexId, Long)] = {
    import vehicleDS.sparkSession.implicits._
    val vertexDS = vehicleDS.map( vehicledata => (vehicledata.crossid.toLong, vehicledata.crossid.toLong)).distinct()
    vertexDS.rdd
  }

  /**
    * 获取Edge DataSet
    * @param vehicleDS
    * @return edgeDS
    */
  def createEdge(vehicleDS: Dataset[VehicleData]): RDD[Edge[Int]] = {
    import vehicleDS.sparkSession.implicits._
    val coloredehicleDS = vehicleDS.map( vehicleData => {
      val color_plateno = vehicleData.plateColor + "-" + vehicleData.plateno
      (color_plateno, Seq(vehicleData))
    }).rdd
    val groupedDS = coloredehicleDS.reduceByKey((a, b) => {
      if(a.size < 20) {
        (a ++: b)
      }
      else
        a
    }).map(_._2)

    val test1 = groupedDS.take(10)
    println(test1.apply(1))

    val traceDS = groupedDS.flatMap(record => getTraces(record))
    val edgeRDD = traceDS.map(x => (x._1 + "," + x._2, 1)).reduceByKey((a, b) => a + b).map(x => {
      val src = x._1.split(",")(0).toLong
      val des = x._1.split(",")(1).toLong
      val count = x._2
      Edge(srcId = src, dstId = des, attr = count)
    })
    edgeRDD

  }

  /**
    * 将过车时间序列转化成点对关系
    * @param vehicleList
    * @return 点对关系的List
    */
  def getTraces(vehicleList: Seq[VehicleData]): Seq[(String, String)] = {
    val sortedList = vehicleList.sortBy(_.passtime)
    val removeFirtList = sortedList.drop(1)
    val zipedList = (sortedList zip removeFirtList) map { vehicleDataPair => {
      val passtime = vehicleDataPair._2.passtime + vehicleDataPair._1.passtime
      (vehicleDataPair._1.crossid, vehicleDataPair._2.crossid)
    }}
    zipedList
  }

}
