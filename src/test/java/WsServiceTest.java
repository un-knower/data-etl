
/**
 * Created by dongzeheng on 2018/1/24.
 */

import com.alibaba.fastjson.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xudejun 2016/9/14
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2016/9/14
 * @modify by reason:{方法名}:{原因}
 */
public class WsServiceTest {

    public static final String URL = "http://10.65.223.221:8181/traffic/ws/operateData";
    public static final int count = 500;
//    public static final long TIME = 1516774080000L;
    public static final long TIME = 1519635325000L;

    public static void main(String[] args) throws InterruptedException {

        List<String> picMap = new ArrayList<>();
        picMap.add("http://10.15.33.15:8088/image/vrb2/i2/92cfb2b33166463f91be728656dbb979/00002?key=576b&amp;offset=161692371&amp;high=313556");
        picMap.add("http://10.15.33.15:8088/image/vrb2/i2/92cfb2b33166463f91be728656dbb979/00002?key=576b&amp;offset=161692371&amp;high=313556");
        picMap.add("http://10.15.33.15:8088/image/vrb2/i2/92cfb2b33166463f91be728656dbb979/00002?key=576b&amp;offset=161692371&amp;high=313556");
        picMap.add("http://10.15.33.15:8088/image/vrb2/i2/92cfb2b33166463f91be728656dbb979/00002?key=576b&amp;offset=161692371&amp;high=313556");
        picMap.add("http://10.15.33.15:8088/image/vrb2/i2/92cfb2b33166463f91be728656dbb979/00002?key=576b&amp;offset=161692371&amp;high=313556");


        String[] plateNochar = {"A","B","C","D","E","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","W","X","Y","Z","1","2","3","4","5","6","7","8","9"};

        List<String> platenoMap = new ArrayList<>();
        StringBuffer prePlate = new StringBuffer("浙A");

//        //车牌集合
        for (int i = 0; i < 70; i++) {
            for (int j = 0; j < 5; j++) {
                prePlate.append(plateNochar[new Random().nextInt(32)]);
            }

            platenoMap.add(prePlate.toString());
            prePlate = new StringBuffer("浙A");
        }

//        platenoMap.add("浙A66666");
//        platenoMap.add("浙A55555");
//        platenoMap.add("浙A44444");
//        platenoMap.add("浙A33333");

        //卡口集合
        List<String> crossingMap = new ArrayList<>();
        for (int i = 1; i < 21; i++) {
            crossingMap.add(i + "");
        }

        //方向集合
        List<String> dicMap = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            dicMap.add(i + "");
        }

        System.out.println(platenoMap.size() + "===" + crossingMap.size() + "===" + dicMap.size());

//        sendMsg(platenoMap,crossingMap,dicMap);

        ExecutorService service = Executors.newFixedThreadPool(4);
//
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            service.submit(new SendMessage(platenoMap, crossingMap, dicMap,i*150+(30240)));
        }
//        service.submit(new SendMessage(platenoMap, crossingMap, dicMap,1296000));

        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        System.out.println("耗时====" + (System.currentTimeMillis() - t1));

    }


    static class SendMessage implements Runnable{
        List<String> platenoMap = null;
        List<String> crossingMap = null;
        List<String> dicMap = null;
        int time = 0;

        public SendMessage(List<String> platenoMap,List<String> crossingMap,List<String>dicMap,int time) {
            this.platenoMap = platenoMap;
            this.crossingMap = crossingMap;
            this.dicMap = dicMap;
            this.time = time;
        }

        @Override
        public void run() {
            try {
                //服务的地址
                HttpURLConnection conn = null;
                URL wsUrl = null;
                OutputStream os = null;

                Map map = null;

                long passtime = TIME;
//                long passtime = 1502553600000L;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                List<String> passInfoList = new ArrayList<>();

                String soap = "";
                InputStream is = null;
                for (int i = 1; i < count; i++) {
                    passtime = passtime + time;

                    map = new HashMap();

                    map.put("pass_id", i + ""); //
                    map.put("pass_time", sdf.format(passtime)); //

                    map.put("crossing_id", crossingMap.get(new Random().nextInt(20)));
//                    map.put("crossing_id",  "99");
                    map.put("vehaddpicurl1", "");
                    map.put("hovflag", "0");
                    map.put("lane_no", "1");
//                    map.put("direction_index", dicMap.get(new Random().nextInt(8)));
                    map.put("plate_no",platenoMap.get(new Random().nextInt(70)));

                    map.put("direction_index", "1");
//                    map.put("plate_no","浙A12345");

                    map.put("plate_type", "1");
                    map.put("plate_color", "2");
                    map.put("plate_state", "1");
                    map.put("vehicle_type", "13");
                    map.put("vehicle_color", "5");
                    map.put("vehicle_color_depth", "2");
                    map.put("vehicle_state", "0");
                    map.put("vehicle_len", "0");
                    map.put("vehicle_speed", "0");
                    map.put("vehicle_info_level", "1");
                    map.put("vehicle_logo", "42");
                    map.put("vehicle_sublogo", "16");
                    map.put("vehicle_model", "4");
                    map.put("pilotsafebelt", "1");
                    map.put("vicepilotsafebelt", "0");
                    map.put("pilotsunvisor", "2");
                    map.put("vicepilotsunvisor", "1");
                    map.put("uphone", "1");
                    map.put("envprosign", "1");
                    map.put("dangmark", "0");
                    map.put("pendant", "2");
                    map.put("pdvs", "0");
                    map.put("vehiclesign", "4");
                    map.put("vehiclelamp", "0");
                    map.put("copilot", "1");
                    map.put("platepicurl", "");
                    map.put("vehiclepicurl", "http://10.15.48.11:8088/image");
                    map.put("picurl_num", "2");
                    map.put("tfs_id", "4");
                    map.put("vmodel_x", "200");
                    map.put("vmodel_y", "200");
                    map.put("vmodel_w", "200");
                    map.put("vmodel_h", "180");
                    map.put("vmodel", "");
                    map.put("resstr3", "");
                    map.put("data_type", "1");

                    String passInfo = JSONObject.toJSONString(map);
                    passInfoList.add(passInfo);
                    if (i % 200 == 0) {
                        wsUrl = new URL(URL);
                        conn = (HttpURLConnection) wsUrl.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
                        os = conn.getOutputStream();

                        soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://www.hikvision.com/traffic/ws/\">\n" +
                                "   <soapenv:Header/>\n" +
                                "   <soapenv:Body>\n" +
                                "      <ws:addPassRecords>\n" +
                                "         <dataJson>" + passInfoList.toString() + "</dataJson>\n" +
                                "      </ws:addPassRecords>\n" +
                                "   </soapenv:Body>\n" +
                                "</soapenv:Envelope>";
                        os.write(soap.getBytes());
                        is = conn.getInputStream();
                        byte[] b = new byte[1024];
                        int len = 0;
                        String s = "";
                        while ((len = is.read(b)) != -1) {
                            String ss = new String(b, 0, len, "UTF-8");
                            s += ss;
                        }
                        System.out.println("返回结果===" + s);

                        if (s.contains("<resultCode>0</resultCode>") || s.contains("<resultCode>-1</resultCode>")) {
                            System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆=" + i);
                        }

                        passInfoList.clear();
                    }

//                try {
//                    Thread.currentThread().sleep(2);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                }

                if (passInfoList.size() > 0) {

                    wsUrl = new URL(URL);

                    conn = (HttpURLConnection) wsUrl.openConnection();

                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");

                    os = conn.getOutputStream();

                    soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://www.hikvision.com/traffic/ws/\">\n" +
                            "   <soapenv:Header/>\n" +
                            "   <soapenv:Body>\n" +
                            "      <ws:addPassRecords>\n" +
                            "         <dataJson>" + passInfoList.toString() + "</dataJson>\n" +
                            "      </ws:addPassRecords>\n" +
                            "   </soapenv:Body>\n" +
                            "</soapenv:Envelope>";


                    os.write(soap.getBytes());

                    is = conn.getInputStream();

                    byte[] b = new byte[1024];
                    int len = 0;
                    String s = "";
                    while ((len = is.read(b)) != -1) {
                        String ss = new String(b, 0, len, "UTF-8");
                        s += ss;
                    }
                    System.out.println("返回结果===" + s);

                }

                is.close();
                os.close();
                conn.disconnect();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
