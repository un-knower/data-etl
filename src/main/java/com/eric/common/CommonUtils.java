package com.eric.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类
 *
 */
public class CommonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

    // 正则表达式 - SQL特殊字符列表
    public static final Pattern PATTERN_SOLR_SPECIAL_CHAR = Pattern.compile("[+\\-&|!(){}\\[\\]^\"~*?:(\\)]");

    // 正则表达式 - 过车图片前缀
    public static final Pattern PATTERN_TRAFFIC_IMAGE_PREFIX = Pattern.compile("^((?i)http|(?i)ftp)://(\\w+\\:\\w+@)?((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(:\\d+)?");

    // 正则表达式 - IP地址
    public static final Pattern PATTERN_IP_ADDR = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

    // 正则表达式 - IP:PORT地址--add by chengqiujiang
    public static final Pattern PATTERN_IP_PORT = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9]):([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])$");

    /*正则表达式，车牌尾号*/
    public static final Pattern PATTERN_PLATE_TAIL = Pattern.compile(".*([0-9])");

    // 过车图片相对路径: 1
    private final static String PIC_RELATIVE_PATH = "1";

    // 过车图片相对路径或者绝对路径的配置: 相对路径: 1, 绝对路径: 0
    public final static String PIC_URL_CONFIG = SysConfigUtils.getValueByKey("collector.pic.url");

    /**
     * 静默休眠n微秒
     *
     * @param timeout
     */
    public static void sleepQuietlyInSeconds(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 静默休眠n分钟
     *
     * @param timeout
     */
    public static void sleepQuietlyInMinutes(int timeout) {
        try {
            TimeUnit.MINUTES.sleep(timeout);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 静默休眠n毫秒
     *
     * @param timeout
     */
    public static void sleepQuietlyInMillis(int timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 静默休眠n微秒
     *
     * @param timeout
     */
    public static void sleepQuietlyInMicros(int timeout) {
        try {
            TimeUnit.MICROSECONDS.sleep(timeout);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 对solr查询字符串特殊字符进行转义处理
     *
     * @param input
     * @return
     */
    public static String escapeSolrMetaCharactor(String input) {
        // 验证参数是否合法
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        Matcher matcher = PATTERN_SOLR_SPECIAL_CHAR.matcher(input);
        StringBuffer strBuffer = new StringBuffer(input.length() * 2);
        while (matcher.find()) {
            matcher.appendReplacement(strBuffer, "\\\\" + matcher.group());
        }
        matcher.appendTail(strBuffer);
        return strBuffer.toString();
    }

    /**
     * 删除图片路径前缀（对协议、IP和端口进行清理）
     *
     * @param path
     * @return
     */
    public static String removeImagePathPrefix(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        if (PIC_RELATIVE_PATH.equalsIgnoreCase(PIC_URL_CONFIG)) {
            Matcher matcher = PATTERN_TRAFFIC_IMAGE_PREFIX.matcher(path);
            return matcher.replaceFirst("");
        } else {
            return path;
        }
    }

    /**
     * 获取Spark任务jar文件列表
     *
     * @param
     * @return ============================================
     * Modified reason: 调整实现逻辑，去除对tomcat环境的依赖。
     */
    public static String[] getSparkLibJars() {
        String jarFilePath = CommonUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File jarFile = new File(jarFilePath);
        if (null == jarFile || !jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
            LOG.error("Spark jar文件路径：{}不正确", jarFile.getName());
            return new String[]{};
        }
        File[] files = jarFile.getParentFile().listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return new String[]{};
        }
        ArrayList<String> list = new ArrayList<String>();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (filePath.toLowerCase().endsWith(".jar")) {
                list.add(filePath);
            }
        }
        return list.toArray(new String[list.size()]);
    }


    public static String[] getSparkLibJars(String jarFilePath) {
        File jarFile = new File(jarFilePath);
        if (null == jarFile || !jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
            LOG.error("Spark jar文件路径：{}不正确", jarFile.getName());
            return new String[]{};
        }
        File[] files = jarFile.getParentFile().listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return new String[]{};
        }
        ArrayList<String> list = new ArrayList<String>();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (filePath.toLowerCase().endsWith(".jar")) {
                list.add(filePath);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 是否本地模式
     *
     * @return
     */
    public static boolean isLocalRunMode() {
        return "local".equalsIgnoreCase(System.getProperty("deploy-mode"));
    }

    /**
     * 判断图片路径是相对还是绝对路径
     *
     * @param path
     * @return
     */
    public static boolean isPicPathAbsolute(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        Matcher matcher = null;
        try {
            matcher = PATTERN_TRAFFIC_IMAGE_PREFIX.matcher(path);
        } catch (Exception e) {
            LOG.error("picURL match error", e);
            return false;
        }
        return matcher.find();
    }

    /**
     * 获取图片前缀
     *
     * @param path
     * @return
     */
    public static String getPicPrefix(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        Matcher matcher = null;
        try {
            matcher = PATTERN_TRAFFIC_IMAGE_PREFIX.matcher(path);
        } catch (Exception e) {
            LOG.error("picURL match error", e);
            return null;
        }
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static String getPlateTail(String platNo) {
        if (platNo == null) {
            return null;
        }
        Matcher matcher = null;
        try {
            matcher = PATTERN_PLATE_TAIL.matcher(platNo);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            LOG.error("picURL match error", e);
        }
        return null;
    }
}
