package common;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Utility {

    public String getStringParam(String text, String parm) {
        JSONObject jb = JSONObject.parseObject(text);
        return jb.getString(parm);
    }

    public Double getDoubleParam(String text, String parm) {
        JSONObject jb = JSONObject.parseObject(text);
        return jb.getDouble(parm);
    }

    public int getIntParam(String text, String parm) {
        JSONObject jb = JSONObject.parseObject(text);
        return jb.getIntValue(parm);
    }

    public List<String> getArrayParam(String text, String parm) {
        JSONObject jb = JSONObject.parseObject(text);
        List<String> ls = JSON.parseArray(jb.getJSONArray(parm).toJSONString(),String.class);
        return ls;
    }
}
