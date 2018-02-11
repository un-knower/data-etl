package com.eric.process

import com.eric.common.DateTimeUtils
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

/**
  * Created by dongzeheng on 2018/2/11.
  */
object DataLoadJob {

  val LOGGER = LoggerFactory.getLogger(DataLoadJob.getClass)

  val vehicleInfoTable = "BAYONET_VEHICLEPASS"

  val vehicleInfoTablePropertyFamily = "cf"

  val vehicleInfoTablePropertyColumn = "property"

  val vehicleInfoTableCountFamily = "cfd"

  val scanBatch = 500

  def loadFromHBase(sparkSession: SparkSession, startTime: String, stopTime: String) = {
    val startTime = System.currentTimeMillis()
    val conf = HBaseConfiguration.create()
    conf.set(TableInputFormat.INPUT_TABLE, vehicleInfoTable)
    val scan = new Scan()

    // (vehiclelogo, vehicletype, vehiclecolor)
    scan.addFamily(Bytes.toBytes(vehicleInfoTablePropertyFamily))
    scan.addColumn(Bytes.toBytes(vehicleInfoTablePropertyFamily), Bytes.toBytes(vehicleInfoTablePropertyColumn))

    val benchmarkTime = DateTimeUtils.getDateBeginTime(System.currentTimeMillis()) //当天0点
    LOGGER.info("benchmarkTime: {}", benchmarkTime)
    // (daycount, totlecount)
    scan.addFamily(Bytes.toBytes(vehicleInfoTableCountFamily))
    scan.addColumn(Bytes.toBytes(vehicleInfoTableCountFamily), Bytes.toBytes(DateTimeUtils.format(benchmarkTime)))
    LOGGER.info("cfd: {}", DateTimeUtils.format(benchmarkTime))

    scan.setCaching(scanBatch)
    scan.setCacheBlocks(false)
    val proto = ProtobufUtil.toScan(scan)
    conf.set(TableInputFormat.SCAN, Base64.encodeBytes(proto.toByteArray()))

    // 获取过车分析结果表记录
    val flowResultRDD = sparkSession.sparkContext.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])

    val vehicleInfoRDD = flowResultRDD.filter(
      res=> {
        //过滤掉最近一个月未出现的车辆
        val oldest = Bytes.toString(res._2.getValue(Bytes.toBytes(vehicleInfoTableCountFamily), Bytes.toBytes(DateTimeUtils.format(benchmarkTime , "yyMMdd"))))
        val latest = Bytes.toString(res._2.getValue(Bytes.toBytes(vehicleInfoTableCountFamily), Bytes.toBytes(DateTimeUtils.format(benchmarkTime , "yyMMdd"))))
        (null != latest) && ((null == oldest) || (latest.split(",")(1).toInt - oldest.split(",")(1).toInt >= 1))
      }).map(res =>{
      var plateNo = Bytes.toString(res._2.getRow)
      if (plateNo.length <= 2) {
        plateNo = "4-车牌"
      }

      val platebelong = plateNo.substring(2, 4)
      val property = Bytes.toString(res._2.getValue(Bytes.toBytes(vehicleInfoTablePropertyFamily), Bytes.toBytes(vehicleInfoTablePropertyColumn)))
      val propertys = property.split(",")
      if (3 != propertys.length) {
        // (vehiclelogo, vehicletype, vehiclecolor)
        propertys(0) = "0"
        propertys(1) = "0"
        propertys(2) = "0"
      }
      val benchmarkCount = Bytes.toString(res._2.getValue(Bytes.toBytes(vehicleInfoTableCountFamily), Bytes.toBytes(DateTimeUtils.format(benchmarkTime , "yyMMdd"))))
      val firstCount = benchmarkCount.split(",")(0).toLong
      val totalCount = benchmarkCount.split(",")(1).toInt

    })
    import sparkSession.implicits._
    val vehicleInfoDS = vehicleInfoRDD
    vehicleInfoDS.repartition(3)

    LOGGER.info("vehicleInfoDS count: {}", vehicleInfoDS.count())
    val time = System.currentTimeMillis() - startTime
    LOGGER.info("fetch vehicleInfoDS take time: {}", time)
    vehicleInfoDS
  }

  def loadFromKafka(sparkSession: SparkSession, startOffSet:String , count: Int) = {}

  def loadFromElasticSearch(sparkSession: SparkSession, index: String) = {}

}
