package com.eric.common;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;

import java.io.IOException;

public class HbaseUtils {
	private static Configuration config = ConfigurationFactory.getConfiguration();
	private static Configuration configForSpark = null;

	private static Connection connection;

	public static synchronized Connection getConn() {
		if (connection == null) {
			try {
				connection = ConnectionFactory.createConnection(config);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return connection;
	}

	static synchronized Configuration getConfForSpark(String zkNode) {

		if (configForSpark == null) {
			try {
				configForSpark =  ConfigurationFactory.getConfigurationForSpark(zkNode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return configForSpark;
	}

	public static HTable _getTableForSpark(String tableName, String zkNode) throws IOException {
		if (StringUtils.isEmpty(tableName)){
			return null;
		}
		return new HTable(getConfForSpark(zkNode),tableName);
	}

	/**
	 * 从连接池获取HTable对象
	 *
	 * @param tableName
	 * @return
	 * @throws IOException
	 */
	public static HTable getTable(String tableName) throws IOException {
		return new HTable(config, tableName);
	}

	public static Table _getTable(String tableName) throws IOException {
		if (StringUtils.isEmpty(tableName)){
			return null;
		}

		return getConn().getTable(TableName.valueOf(tableName));
	}

    /**
     * 判断是否存在表
     *
     * @param tableName
     * @return
     * @throws IOException
     */
    public static boolean existTable(String tableName) throws IOException {
		if (StringUtils.isEmpty(tableName)) {
			return false;
		}

		Admin admin = getAdmin();
        if (admin == null) {
            return false;
        }

        return admin.tableExists(TableName.valueOf(tableName));
    }

	public static Admin getAdmin() throws IOException {
		return getConn().getAdmin();
	}

	/**
	 * 释放HTable对象
	 * @param table
	 * @throws IOException
	 */
	public static void doReleaseTable(HTable table) {
		if (table == null) {
			return;
		}
		try {
			table.flushCommits();
			table.close();
			table=null;
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void doCloseTable(HTable table) {
		if (table == null) {
			return;
		}
		try {
			table.close();
			table=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeTable(Table table) {
		if (table == null) {
			return;
		}
		try {
			table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static AggregationClient getAggregationClient() {
		return new AggregationClient(config);
	}

}
