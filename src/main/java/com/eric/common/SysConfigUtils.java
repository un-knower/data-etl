/**
 * Created by dongzeheng on 2018/2/10.
 */
package com.eric.common;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 系统配置管理工具
 * ====================================================
 * Modified reason: 对系统配置工具名称进行调整，并剥离对Spring的依赖。
 */
public class SysConfigUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(SysConfigUtils.class);

    public static final String ENCODING = "UTF-8";
    public static final String HBASE_FILE_NAME = "hbase-site.xml";
    private static volatile boolean isRead = false;
    private static volatile boolean isReLoad = false;
    private static byte[] LOCK = {};
    private static CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

    // 正则表达式 - IP地址
    public static final Pattern PATTERN_IP_ADDR = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

    private static CompositeConfiguration getCompositeConfig() {
        if (!isRead) {
            synchronized (SysConfigUtils.class) {
                if (!isRead) {
                    reload();
                    isRead = true;
                }
            }
        } else {
            if (isRead && isReLoad) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }
        return compositeConfiguration;
    }

    /**
     * 重新加载配置文件
     *
     * @Modified reason: 采用UTF-8编码；禁用解析器根据分隔符解析参数值，不需要将逗号分隔的多个参数值解析为列表。
     */
    public static void reload() {
        if (isReLoad) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            return;
        }
        synchronized (LOCK) {
            if (!isReLoad) {
                isReLoad = true;
                compositeConfiguration.clear();
                // 禁用解析器根据分隔符解析参数值，不需要将逗号分隔的多个参数值解析为列表。
                compositeConfiguration.setDelimiterParsingDisabled(true);
                // 加载系统根目录下所有properties配置文件
                try {
                    String rootPath = SysConfigUtils.class.getResource("/").getPath();
                    LOGGER.info("root path =>[{}].", rootPath);
                    Collection<File> files = FileUtils.listFiles(new File(rootPath), new String[]{"properties"}, true);
                    for (File file : files) {
                        LOGGER.info("load prop file =>[{}].", file.getAbsolutePath());
                        load(file);
                    }
                } catch (Throwable e) {
                    LOGGER.error("reload failed.", e);
                }
                // 加载HBase配置文件[hbase-site.xml]
                loadHbase();
                // 设置内部接口允许访问的IP地址列表
                String propKey = "webservice.security.inner.hosts";
                compositeConfiguration.setProperty(propKey, replaceIps2List(compositeConfiguration.getString(propKey)));
                // 设置外部接口允许访问的IP地址列表
                propKey = "webservice.security.outer.hosts";
                compositeConfiguration.setProperty(propKey, replaceIps2List(compositeConfiguration.getString(propKey)));
                // 重置标记位
                isReLoad = false;
            }
        }
    }

    /**
     * 加载HBase配置文件[hbase-site.xml]
     */
    private static void loadHbase() {
        InputStream is = null;
        try {
            is = SysConfigUtils.class.getClassLoader().getResourceAsStream(HBASE_FILE_NAME);
            if (is == null) {
                is = new FileInputStream(HBASE_FILE_NAME);
            }
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            String propKey = "hbase.zk";
            List<Element> list = root.elements();
            for (Element e : list) {
                if (e.element("name").getText().trim().equalsIgnoreCase("hbase.zookeeper.quorum")) {
                    compositeConfiguration.setProperty(propKey, e.element("value").getText().trim());
                }
            }
        } catch (Throwable e) {
            LOGGER.error("load Config file [{}] failed!", HBASE_FILE_NAME, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * 加载指定配置文件
     *
     * @param file
     * @return
     */
    private static Configuration load(File file) {
        if (file == null || !file.exists()) {
            LOGGER.warn("config file is null or not exist.");
            return null;
        }
        InputStream is = null;
        PropertiesConfiguration conf = new PropertiesConfiguration();
        try {
            is = new FileInputStream(file);
            // 采用UTF-8编码解析配置文件；禁用解析器根据分隔符解析参数值，不需要将逗号分隔的多个参数值解析为列表。
            conf.setEncoding(ENCODING);
            conf.setDelimiterParsingDisabled(true);
            conf.load(is);
            compositeConfiguration.addConfiguration(conf);
        } catch (Throwable e) {
            LOGGER.error("load Config file [{}] failed!", file.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return conf;
    }

    /**
     * 加载配置文件
     *
     * @param file
     * @return
     */
    public static Configuration load(String file) {
        if (StringUtils.isEmpty(file)) {
            LOGGER.warn("config file can't null, file = [{}].", file);
            return null;
        }
        PropertiesConfiguration conf = new PropertiesConfiguration();
        InputStream is = null;
        try {
            is = SysConfigUtils.class.getClassLoader().getResourceAsStream(file);
            if (is == null) {
                is = new FileInputStream(file);
            }
            conf.setEncoding(ENCODING);
            conf.setDelimiterParsingDisabled(true);
            conf.load(is);
            compositeConfiguration.addConfiguration(conf);
        } catch (Throwable e) {
            LOGGER.error("load Config file [{}] failed!", file, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return conf;
    }

    /**
     * 加载配置文件
     *
     * @param file
     * @return
     */
    public static Properties loadProperties(String file) {
        if (StringUtils.isEmpty(file)) {
            LOGGER.warn("config file can't be null, file = [{}].", file);
            return null;
        }
        Properties properties = new Properties();
        InputStream is = null;
        Reader reader = null;
        try {
            is = SysConfigUtils.class.getClassLoader().getResourceAsStream(file);
            if (is == null) {
                is = new FileInputStream(file);
            }
            reader = new InputStreamReader(is, ENCODING);
            properties.load(reader);
        } catch (IOException e) {
            LOGGER.error("loadProperties file [{}] failed!", file, e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
        }
        return properties;
    }


    /**
     * 修改属性文件
     * @param key
     * @param value
     * @param file
     */
    public static void updateProperties(String key, String value, String file) {
        try {
            if (StringUtils.isBlank(file)) {
                LOGGER.error("update error for file is null");
                return;
            }
            String rootPath = SysConfigUtils.class.getResource("/").getPath();
//            LOGGER.info("rootPath: {}", rootPath);
            File path = new File(rootPath +"/"+ file);
            if (!path.exists() || !path.isFile()) {
                LOGGER.error("update error for file is illegal");
                return;
            }
            PropertiesConfiguration pro = new PropertiesConfiguration(path);
            pro.setEncoding(ENCODING);
            pro.setProperty(key, value);
            pro.save();
        } catch (ConfigurationException e) {
            LOGGER.error("ConfigurationException.", e);
        } catch (Exception e) {
            LOGGER.error("update error.", e);
        }
    }


    /**
     * 根据前缀获取新的配置，自动删除前缀。
     * 可用于动态增删预设配置项
     *
     * @param prefix 配置前缀，如：vehicle-profile
     * @return 配置接口
     */
    public static Configuration getConfiguration(String prefix) {
        Configuration conf = new CompositeConfiguration();
        if (compositeConfiguration != null) {
            Iterator keys = compositeConfiguration.getKeys(prefix);
            while (keys.hasNext()) {
                String key = (String) keys.next();
                conf.setProperty(key.replaceFirst(prefix + ".", ""), compositeConfiguration.getString(key));
            }
        }
        return conf;
    }

    public static String getValueByKey(String key) {
        return getCompositeConfig().getString(key);
    }

    public static boolean isEmpty() {
        return getCompositeConfig().isEmpty();
    }

    public static String getHbaseZk() {
        return getCompositeConfig().getString("hbase.zk");
    }

    public static String getHbaseMaster() {
        return getCompositeConfig().getString("hbase.master");
    }

    public static String getSparkMaster() {
        return getCompositeConfig().getString("spark.master.url");
    }

    public static String getKafkaZkUrl() {
        return getCompositeConfig().getString("kafka.zkUrl");
    }

    public static String getKafkaServerUrl() {
        return getCompositeConfig().getString("kafka.serverUrl");
    }

    public static String getPgSqlDriver() {
        return getCompositeConfig().getString("driverClassName");
    }

    public static String getPgSqlUrl() {
        return getCompositeConfig().getString("rdb.connection.url");
    }

    public static String getPgSqlUser() {
        return getCompositeConfig().getString("rdb.connection.username");
    }

    public static String getPgSqlPassword() {
        return getCompositeConfig().getString("rdb.connection.password");
    }

    public static String getRedisCluster() {
        return getCompositeConfig().getString("redis.nodes.port");
    }

    public static String getWSEndpointJobSubmit() {
        return getCompositeConfig().getString("ws_endpoint_job_submit");
    }

    public static int getAlgorithmType() {
        return getCompositeConfig().getInt("algorithmType");
    }

    public static String getdestinationUrl() {
        return getCompositeConfig().getString("destinationUrl");
    }

    public static String getCAUser() {
        return getCompositeConfig().getString("ca.username");
    }

    public static String getCAPassword() {
        return getCompositeConfig().getString("ca.password");
    }

    public static String getESCluster() {
        return getCompositeConfig().getString("es.nodes");
    }

    public static String getString(String key) {
        return getCompositeConfig().getString(key);
    }

    public static String getString(String key, String defaultValue) {
        return getCompositeConfig().getString(key, defaultValue);
    }

    public static String getStringWhenBlank2Default(String key, String defaultValue) {
        String value = getString(key);
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    public static int getInt(String key) {
        return getCompositeConfig().getInt(key);
    }

    public static int getInt(String key, int defaultValue) {
        return getCompositeConfig().getInt(key, defaultValue);
    }

    public static long getLong(String key) {
        return getCompositeConfig().getLong(key);
    }

    public static long getLong(String key, long defaultValue) {
        return getCompositeConfig().getLong(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return getCompositeConfig().getBoolean(key);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getCompositeConfig().getBoolean(key, defaultValue);
    }

    /**
     * 获取内部接口允许访问的IP地址列表
     *
     * @return
     */
    public static List<String> getAllowIp4Inner() {
        return getCompositeConfig().getList("webservice.security.inner.hosts");
    }

    /**
     * 获取外部接口允许访问的IP地址列表
     *
     * @return
     */
    public static List<String> getAllowIp4Outer() {
        return getCompositeConfig().getList("webservice.security.outer.hosts");
    }

    /**
     * 将IP地址字符串按逗号拆分为字符串列表，并过滤掉不合法的IP地址
     *
     * @param ips
     * @return
     */
    protected static List<String> replaceIps2List(String ips) {
        //验证参数是否合法
        if (StringUtils.isEmpty(ips)) {
            return Collections.emptyList();
        }
        //替换空格后按逗号拆分
        String[] array = ips.replaceAll("\\s+", "").split(",");
        // 过滤掉不合法的IP地址
        List<String> list = new ArrayList<String>();
        for (String candidate : array) {
            if (PATTERN_IP_ADDR.matcher(candidate).matches()) {
                list.add(candidate);
                LOGGER.debug("valid ip address[{}].", candidate);
            } else {
                LOGGER.debug("invalid ip address[{}].", candidate);
            }
        }
        return Collections.unmodifiableList(list);
    }

}
