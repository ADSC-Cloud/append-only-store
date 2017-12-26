package indexingTopology.util.track;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import indexingTopology.api.client.GeoTemporalQueryClient;
import indexingTopology.api.client.GeoTemporalQueryRequest;
import indexingTopology.api.client.QueryResponse;
import indexingTopology.common.data.DataSchema;
import indexingTopology.common.data.DataTuple;
import indexingTopology.common.logics.DataTuplePredicate;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by billlin on 2017/12/15
 */
public class TrackSearchWs implements Serializable{

    private String city;
    private int devbtype;
    private String devid;
    private long startTime;
    private long endTime;
    private String errorCode;
    private String errorMsg;

    public TrackSearchWs(){

//        this.city = (String)businessParams.get("city");
//        this.devbtype = (int)businessParams.get("devbtype");
//        this.devid = (String)businessParams.get("devid");
//        this.startTime = (long)businessParams.get("startTime");
//        this.endTime = (long)businessParams.get("endTime");
    }

    public String services(String permissionParams, String businessParams) {
        JSONObject queryResponse = new JSONObject();
        try{
            JSONObject jsonObject = JSONObject.parseObject(businessParams);
            if(!getQueryJson(jsonObject)){ // query failed,json format is error
                errorCode = "1102";
//            errorMsg = Error(errorCode);
                queryResponse.put("result", null);
                queryResponse.put("errorCode", errorCode);
                queryResponse.put("errorMsg", errorMsg);
                return queryResponse.toString();
            }
        }catch (JSONException e){// query failed, json value invalid
            errorCode = "1001";
//            errorMsg = Error(errorCode);
            queryResponse.put("result", null);
            queryResponse.put("errorCode", errorCode);
            queryResponse.put("errorMsg", errorMsg);
            return queryResponse.toString();
        }

        // query success
        GeoTemporalQueryClient queryClient = new GeoTemporalQueryClient("localhost", 10001);
        try {
            queryClient.connectWithTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataSchema schema = getDataSchema();
        DataTuplePredicate predicate;
        System.out.println("city : " + city);
        System.out.println("devbtype : " + devbtype);
        System.out.println("devid : " + devid);
        System.out.println("startTime : " + startTime);
        System.out.println("endTime : " + endTime);
        predicate = t -> CheckEqual((String)schema.getValue("city", t),(int)schema.getValue("devbtype", t),(String)schema.getValue("devid", t));
        GeoTemporalQueryRequest queryRequest = new GeoTemporalQueryRequest<>(Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE,
                startTime,
                endTime, predicate, null, null, null, null);
        try {
            System.out.println(queryRequest.startTime);
            QueryResponse response = queryClient.query(queryRequest);
            DataSchema outputSchema = response.getSchema();
            System.out.println(outputSchema.getFieldNames());
            System.out.println("datatuples : " + response.dataTuples.size());
            List<DataTuple> tuples = response.getTuples();

            queryResponse.put("success", true);
            JSONArray queryResult = new JSONArray();
            for (int i = 0; i < tuples.size(); i++) {
                queryResult.add(schema.getJsonFromDataTupleWithoutZcode(tuples.get(i)));
                System.out.println(tuples.get(i).toValues());
            }
            queryResponse.put("result", queryResult);
            queryResponse.put("errorCode", null);
            queryResponse.put("errorMsg", null);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            queryClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = JSONObject.toJSONString(queryResponse, SerializerFeature.WriteMapNullValue);
        System.out.println(result);
        return result;
    }

    public boolean getQueryJson(JSONObject businessParams){
        try {
            this.city = (String)businessParams.get("city");
            this.devbtype = (int)businessParams.get("devbtype");
            this.devid = (String)businessParams.get("devid");
            this.startTime = (long)businessParams.get("startTime");
            this.endTime = (long)businessParams.get("endTime");
        }catch (JSONException e){ // jsonObject value format is wrong
            return false;
        }
        return true;
    }

    public boolean CheckEqual(String city, int devbtype, String devid) {
        if (this.city.equals(city) && this.devbtype == devbtype && this.devid.equals(devid)) {
            return true;
        }
        else
            return false;
    }

//    static private DataSchema getDataSchema() {
//        DataSchema schema = new DataSchema();
//
//        schema.addIntField("devbtype");
////        schema.addVarcharField("devstype",32);
//        schema.addVarcharField("devid", 32);
//        schema.addVarcharField("city", 32);
//        schema.addDoubleField("longitude");
//        schema.addDoubleField("latitude");
////        schema.addDoubleField("altitude");
////        schema.addDoubleField("speed");
////        schema.addIntField("direction");
//        schema.addVarcharField("locationtime", 32);
////        schema.addIntField("workstate");
////        schema.addVarcharField("clzl", 32);
////        schema.addVarcharField("hphm", 32);
////        schema.addIntField("jzix");
////        schema.addVarcharField("jybh", 32);
////        schema.addVarcharField("jymc", 32);
////        schema.addVarcharField("lxdh", 32);
////        schema.addVarcharField("ssdwdm", 32);
////        schema.addVarcharField("ssdwmc", 32);
////        schema.addVarcharField("teamno", 32);
////        schema.addVarcharField("dth", 32);
////        schema.addVarcharField("reserve1", 32);
////        schema.addVarcharField("reserve2", 32);
////        schema.addVarcharField("reserve3", 32);
//        return schema;
//    }

    static private DataSchema getDataSchema() {
        DataSchema schema = new DataSchema();
        schema.addDoubleField("lon");
        schema.addDoubleField("lat");
        schema.addIntField("devbtype");
        schema.addVarcharField("devid", 8);
//        schema.addVarcharField("id", 32);
        schema.addVarcharField("city",32);
        schema.addLongField("locationtime");
//        schema.addLongField("timestamp");
        schema.addIntField("zcode");
        schema.setPrimaryIndexField("zcode");

        return schema;
    }

}
