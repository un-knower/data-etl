package com.eric.meta;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by dongzeheng on 2018/1/24.
 */
public class HBaseVehicleRecord {
    String rowKey;
    String tfsid;
    String jsonData;
    String[] arrayData;
    public static final String TFS_ID = "tfsid";

    public HBaseVehicleRecord(String rowKey, String jsonData) {
        this.rowKey = rowKey;
        this.jsonData = jsonData;
        this.arrayData = jsonData.split(",");
        if(arrayData.length > 30) this.tfsid = arrayData[17];
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getJsonData() {
        return jsonData;
    }

    public String getTfsid() {
        return tfsid;
    }

    public void setTfsid(String tfsid) {
        this.tfsid = tfsid;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String[] getArrayData() {
        return arrayData;
    }

    public void setArrayData(String[] arrayData) {
        this.arrayData = arrayData;
    }

    public static String getTfsId() {
        return TFS_ID;
    }

    public boolean convert(Map<String, String> convertMap) {
        boolean NEED_CONVERT = convertMap.containsKey(tfsid);
        if (NEED_CONVERT) {
            String new_tfsid = convertMap.get(tfsid);
            arrayData[17] = new_tfsid;
            jsonData = StringUtils.join(arrayData, ",");
        }

        return NEED_CONVERT;
    }
}
