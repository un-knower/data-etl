package com.eric.utils

import com.eric.common.CommonUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by dongzeheng on 2017/9/30.
  */
object SparkHoldUtil {

  var sparkContext: SparkContext = null

  var sparkSession: SparkSession = null


  /**
    * 获取context
    * @param appName app名称
    * @param sparkMaster spark mater url
    * @return
    */
  def getSparkContext(appName: String = "TestApp",
                      sparkMaster: String = "spark://10.33.26.145:7077",
                      executorMemory:String,
                      cores:String,
                      storageFraction:String,
                      checkpointDir:String): SparkContext = {

    System.setProperty("hadoop.home.dir", "D:\\hadoop")

    val sparkJars = CommonUtils.getSparkLibJars

    val sparkconf = new SparkConf()
      .setAppName(appName)
      .setMaster(sparkMaster)
      .setJars(sparkJars)
      .set("spark.driver.memory", "5g")
      .set("spark.executor.memory", executorMemory)
      .set("spark.cores.max", cores)
      .set("spark.deploy.mode", "client")
      .set("spark.driver.maxResultSize", "5g")
      .set("spark.storage.memoryFraction", storageFraction)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryoserializer.buffer.max", "2000")
      .set("spark.akka.frameSize", "150")
      .set("spark.driver.allowMultipleContexts", "true")
      .set("spark.speculation", "true")
      .set("spark.network.timeout", "3600")
      .set("spark.cleaner.referenceTracking.blocking.shuffle", "true")
      .set("spark.cleaner.referenceTracking.cleanCheckpoints", "true")
      .set("spark.streaming.stopGracefullyOnShutdown", "true")
      .set("spark.streaming.backpressure.enabled", "true")
    new SparkContext(sparkconf)
  }

  def getSparkContext(sparkConf: SparkConf): SparkContext = {
    SparkContext.getOrCreate(sparkConf)
  }

  /**
    * 获取SparkContext上下文对象
    * @return SparkContext实例
    */
  def getSc(sparkMaster:String, executorMemory:String, cores:String, storageFraction:String, checkpointDir:String): SparkContext = {
    if (null == sparkContext) {
      val appName = "data-etl"
      val sparkJars = CommonUtils.getSparkLibJars("F:\\SVN\\trunk\\v3.0.0\\hbsp-traffic-stream-main\\target\\hbsp-traffic-stream-main\\WEB-INF\\lib\\ant-1.9.7.jar")
      //val sparkJars = CommonUtils.getSparkLibJars
      val sparkConf = new SparkConf()
        .setMaster(sparkMaster)
        .setAppName(appName)
        .setJars(sparkJars)
        .set("spark.driver.memory", "5g")
        .set("spark.executor.memory", executorMemory)
        .set("spark.cores.max", cores)
        .set("spark.deploy.mode", "client")
        .set("spark.driver.maxResultSize", "5g")
        .set("spark.storage.memoryFraction", storageFraction)
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.kryoserializer.buffer.max", "2000")
        .set("spark.akka.frameSize", "150")
        .set("spark.driver.allowMultipleContexts", "true")
        .set("spark.speculation", "true")
        .set("spark.network.timeout", "3600")
        .set("spark.cleaner.referenceTracking.blocking.shuffle", "true")
        .set("spark.cleaner.referenceTracking.cleanCheckpoints", "true")
        .set("spark.streaming.stopGracefullyOnShutdown", "true")
        .set("spark.streaming.backpressure.enabled", "true")
      sparkContext = new SparkContext(sparkConf)
      sparkContext.setCheckpointDir(checkpointDir)
    }
    sparkContext
  }

  /**
    * 获取spark session
    *
    * 如果为非本地调试模式，则使用spark-submit提交任务
    *
    * @param appName app名称
    * @param masterURL spark mater url
    * @return
    */
  def getSparkSession(appName: String = "TestApp",
                      masterURL: String = "spark://10.3.72.141:7077",
                      executorMemory:String,
                      cores:String,
                      storageFraction:String,
                      checkpointDir:String): SparkSession = {
    if (sparkSession == null) {
      //调试代码
      //val sparkJars = CommonUtils.getSparkLibJars("F:\\Projects\\Git\\data-etl\\target\\dataExport.jar")

      val sparkJars = CommonUtils.getSparkLibJars

      val sparkconf = new SparkConf()
        .setAppName(appName)
        .setMaster(masterURL)
        .setJars(sparkJars)
        .set("spark.driver.memory", "5g")
        .set("spark.executor.memory", executorMemory)
        .set("spark.cores.max", cores)
        .set("spark.deploy.mode", "client")
        .set("spark.driver.maxResultSize", "5g")
        .set("spark.storage.memoryFraction", storageFraction)
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.kryoserializer.buffer.max", "2000")
        .set("spark.akka.frameSize", "150")
        .set("spark.driver.allowMultipleContexts", "true")
        .set("spark.speculation", "true")
        .set("spark.network.timeout", "3600")
        .set("spark.cleaner.referenceTracking.blocking.shuffle", "true")
        .set("spark.cleaner.referenceTracking.cleanCheckpoints", "true")
      sparkSession = SparkSession.builder().config(sparkconf).getOrCreate()
    }

    sparkSession
  }

  def getSparkSession(sparkConf: SparkConf): SparkSession = {
    SparkSession.builder().config(sparkConf).getOrCreate()
  }

}
