package io.github.gdpl2112.config;

import com.alibaba.fastjson2.JSONObject;
import io.github.kloping.file.FileUtils;
import io.github.kloping.judge.Judge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author github kloping
 * @date 2025/7/1-09:21
 */
@Component
public class BindConfig {

    @Value("${data.path}")
    public String datapath = "./data";
    private final String data = "./bind.json";
    private JSONObject jdata;

    public void reload() {
        getJdata();
    }

    public UserToken getToken() {
        if (jdata == null) getJdata();
        UserToken token = new UserToken();
        token.user = jdata.getString("user");
        token.token = jdata.getString("token");
        return token;
    }

    public String getBind(Object sid) {
        if (jdata == null) getJdata();
        JSONObject binMap = jdata.getJSONObject("bind");
        String binds = binMap.getString(sid.toString());
        if (Judge.isEmpty(binds)) return null;
        String[] ids = binds.split("_");
        return ids[0];
    }

    public List<String> getBinds(Object sid) {
        if (jdata == null) getJdata();
        JSONObject binMap = jdata.getJSONObject("bind");
        String binds = binMap.getString(sid.toString());
        if (Judge.isEmpty(binds)) return null;
        return new java.util.ArrayList<>(Arrays.stream(binds.split("_")).toList());
    }

    public void bind(String sid, String uid) {
        if (jdata == null) getJdata();
        JSONObject binMap = jdata.getJSONObject("bind");
        String binds = binMap.getString(sid);
        if (Judge.isEmpty(binds)) {
            binMap.put(sid, uid);
            jdata.put("bind", binMap);
            flushJdata();
        } else {
            List<String> uids = Arrays.stream(binds.split("_")).toList();
            if (!uids.contains(uid)) {
                binMap.put(sid, uid + "_" + binds);
                jdata.put("bind", binMap);
                flushJdata();
            }
        }
    }

    public void unbind(String sid, String uid) {
        if (jdata == null) getJdata();
        JSONObject binMap = jdata.getJSONObject("bind");
        String binds = binMap.getString(sid);
        if (Judge.isEmpty(binds)) return;
        List<String> uids = new java.util.ArrayList<>(Arrays.stream(binds.split("_")).toList());
        if (uids.contains(uid)) {
            uids.remove(uid);
            binMap.put(sid, String.join("_", uids));
            jdata.put("bind", binMap);
            flushJdata();
        }
    }

    public String switchto(String sid, String uid) {
        if (jdata == null) getJdata();
        JSONObject binMap = jdata.getJSONObject("bind");
        String binds = binMap.getString(sid);
        if (Judge.isEmpty(binds)) return null;
        List<String> uids = new java.util.LinkedList<>(Arrays.stream(binds.split("_")).toList());
        if (Judge.isNotEmpty(uid)) {
            if (uids.contains(uid)) {
                uids.remove(uid);
                uids.add(0, uid);
                binMap.put(sid, String.join("_", uids));
                jdata.put("bind", binMap);
                flushJdata();
            } else return "未绑定该UID";
        } else {
            //把最后一个切换到最前
            uid = uids.remove(uids.size() - 1);
            uids.add(0, uid);
            binMap.put(sid, String.join("_", uids));
            jdata.put("bind", binMap);
            flushJdata();
        }
        return "切换绑定成功;当前UID:" + uid;
    }

    private void flushJdata() {
        File file = new File(datapath, data);
        try {
            FileUtils.testFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileUtils.putStringInFile(jdata.toString(), file);
    }

    private void getJdata() {
        File file = new File(datapath, data);
        try {
            FileUtils.testFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = FileUtils.getStringFromFile(file.getAbsolutePath());
        if (Judge.isEmpty(str)) {
            str = "{\n" +
                    "  \"user\": \"12345\",\n" +
                    "  \"token\": \"{token}\",\n" +
                    "  \"bind\": {\n" +
                    "    \"12344\": \"1233333_2222222\",\n" +
                    "    \"12345\": \"1233333_2222222\"\n" +
                    "  }\n" +
                    "}";
            FileUtils.putStringInFile(str, file);
        }
        jdata = JSONObject.parseObject(str);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserToken {

        public String user;

        public String token;
    }
}
