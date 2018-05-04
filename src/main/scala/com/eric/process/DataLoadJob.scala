package com.eric.process

import com.eric.common.DateTimeUtils
import com.eric.data.VehicleData
import com.eric.meta.HBaseVehicleRecord
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.LoggerFactory

/**
  * Created by dongzeheng on 2018/2/11.
  */
object DataLoadJob {

  val LOGGER = LoggerFactory.getLogger(DataLoadJob.getClass)

  val vehicleInfoTable = "BAYONET_VEHICLEPASS"

  val vehicleInfoTablePropertyFamily = "cf"

  val vehicleDataPropertyColumn = "data"

  val vehicleInfoTableCountFamily = "cfd"

  val scanBatch = 500

  def loadFromHBase(spark: SparkSession, startTime: String, stopTime: String): Dataset[VehicleData] = {
    import spark.implicits._

    val conf = HBaseConfiguration.create()
    conf.set(TableInputFormat.INPUT_TABLE, vehicleInfoTable)
    val scan = new Scan()

    // (vehiclelogo, vehicletype, vehiclecolor)
    scan.addFamily(Bytes.toBytes(vehicleInfoTablePropertyFamily))
    scan.addColumn(Bytes.toBytes(vehicleInfoTablePropertyFamily), Bytes.toBytes(vehicleDataPropertyColumn))
    scan.setStartRow(Bytes.toBytes(startTime))
    scan.setStopRow(Bytes.toBytes(stopTime))
    val benchmarkTime = DateTimeUtils.getDateBeginTime(System.currentTimeMillis()) //当天0点

    LOGGER.info("cfd: {}", DateTimeUtils.format(benchmarkTime))

    scan.setCaching(scanBatch)
    scan.setCacheBlocks(false)
    val proto = ProtobufUtil.toScan(scan)
    conf.set(TableInputFormat.SCAN, Base64.encodeBytes(proto.toByteArray()))

    val flowResultRDD = spark.sparkContext.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])


    val vehicleRDD = flowResultRDD.map(
      res=> {
        //过滤掉最近一个月未出现的车辆
        val rowKey: String = Bytes.toString(res._2.getRow)
        val dataJson: String = Bytes.toString(res._2.getValue(Bytes.toBytes("cf"), Bytes.toBytes("data")))
        val hBaseVehicleRecord = new HBaseVehicleRecord(rowKey, dataJson).getArrayData
        val vehicleData =  VehicleData(hBaseVehicleRecord(6), hBaseVehicleRecord(8), hBaseVehicleRecord(3), hBaseVehicleRecord(2))
        vehicleData
      })
    val vehicleDS = vehicleRDD.toDS()

    LOGGER.info("vehicleInfoDS count: {}", vehicleRDD.take(2))
    val time = System.currentTimeMillis()
    LOGGER.info("fetch vehicleInfoDS take time: {}", time)
    vehicleDS
  }

  def loadFromKafka(sparkSession: SparkSession, startOffSet:String , count: Int) = {}

  def loadFromElasticSearch(sparkSession: SparkSession, index: String) = {}

}
