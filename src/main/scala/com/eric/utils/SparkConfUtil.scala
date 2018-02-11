package com.eric.util

import com.eric.common.{CommonUtils, SysConfigUtils}
import org.apache.spark.SparkConf
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by dongzeheng on 2018/2/10.
  */
object SparkConfUtil {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
    * 从 analysis配置文件里，根据前缀获取对应配置，实例化为SparkConf，可以存储非Spark配置项
    * 支持 Spark 加载本地配置文件
    *
    * @param prefix 配置前缀名，如：traffic-analysis，默认后面会 加一个 任意字符 分隔配置
    * @return
    */
  def getSparkConf(prefix: String): SparkConf = {
    val sparkConf = new SparkConf()
    // Spark 配置文件加载逻辑，从Spark里面加载

    sparkConf.setMaster(SysConfigUtils.getSparkMaster)

    val conf = SysConfigUtils.getConfiguration(prefix)

    val keys = conf.getKeys()
    while (keys.hasNext) {
      val key = keys.next().asInstanceOf[String]
      sparkConf.set(key, conf.getString(key))
    }
    val nodeNum = 5
    if (nodeNum > 1) {
      val value = (sparkConf.get("spark.cores.max").toInt * nodeNum).toString
      sparkConf.set("spark.cores.max", value)
    }
    //===============================
    sparkConf.setJars(CommonUtils.getSparkLibJars)
    logger.info("spark conf: {}", sparkConf.toDebugString)

    sparkConf
  }


}
