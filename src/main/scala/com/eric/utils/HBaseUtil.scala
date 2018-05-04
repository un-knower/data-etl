package com.eric.util

/**
  * Created by dongzeheng on 2018/2/10.
  */

import java.io.IOException

import com.eric.utils.DataHandler
import com.google.protobuf.InvalidProtocolBufferException
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Put, Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableOutputFormat}
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos
import org.apache.hadoop.hbase.util.Base64
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag


object HBaseUtil extends DataHandler {

  override def read[T: ClassTag](sc: SparkContext): RDD[T] = ???

  override def save[T: ClassTag](rdd: RDD[T]): Unit = ???

  /**
    * 加载 HBase 数据，格式解析由外部类执行
    *
    * @param sc   SparkContext
    * @param conf HBase扫描Configuration，可由外部类创建
    * @return
    */
  def loadHBaseData(sc: SparkContext, conf: Configuration): RDD[(ImmutableBytesWritable, Result)] = {
    sc.newAPIHadoopRDD(conf,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result]
    )
  }


  def createHBaseSaveConf(tableName: String): Configuration = {
    val conf = HBaseConfiguration.create()

    val job = Job.getInstance(conf)
    job.getConfiguration.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Put])
    // 返回配置
    job.getConfiguration
  }


  /**
    * 保存数据到HBase
    *
    * @param rdd  HBaseRDDPair, key被忽略
    * @param conf HBaseSaveConf，需要指定 TableOutputFormatClass（必须）, OutputKeyClass 和 OutPutValueClass
    */
  def saveDataAsHBase(rdd: RDD[(ImmutableBytesWritable, Put)], conf: Configuration): Unit = {
    rdd.saveAsNewAPIHadoopDataset(conf)
  }

  @throws[IOException]
  def convertScanToString(scan: Scan): String = {
    val proto: ClientProtos.Scan = ProtobufUtil.toScan(scan)
    Base64.encodeBytes(proto.toByteArray)
  }

  @throws[IOException]
  def convertStringToScan(base64: String): Scan = {
    val decoded: Array[Byte] = Base64.decode(base64)
    var scan: ClientProtos.Scan = null
    try {
      scan = ClientProtos.Scan.parseFrom(decoded)
    }
    catch {
      case ipbe: InvalidProtocolBufferException => throw new IOException(ipbe)
    }
    ProtobufUtil.toScan(scan)
  }


}
