package com.eric.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;


public class ConfigurationFactory {
    public static final String FS_HDUSER_VALUE = "hdfs";


    public static Configuration getConfiguration() {
        String hdfs = FS_HDUSER_VALUE;
        System.setProperty("HADOOP_USER_NAME", hdfs);

        Configuration conf = HBaseConfiguration.create();

        // 缺省值参考: org.apache.hadoop.hbase.HConstants
        conf.setLong("hbase.rpc.timeout", 10000);
        conf.setInt("hbase.client.retries.number", 3);
        conf.setLong("zookeeper.session.timeout", 30000);
        conf.setLong("hbase.htable.threads.keepalivetime", 3600);
        return conf;
    }


    public static Configuration getConfigurationForSpark(String zkNode) {
        String hdfs = FS_HDUSER_VALUE;
        System.setProperty("HADOOP_USER_NAME", hdfs);

        Configuration configForSpark = HBaseConfiguration.create();
        configForSpark.set("hbase.zookeeper.quorum", zkNode);
        configForSpark.setLong("hbase.rpc.timeout", 10000);
        configForSpark.setInt("hbase.client.retries.number", 3);
        configForSpark.setLong("zookeeper.session.timeout", 30000);
        configForSpark.setLong("hbase.htable.threads.keepalivetime", 600000);
        return configForSpark;
    }
}
