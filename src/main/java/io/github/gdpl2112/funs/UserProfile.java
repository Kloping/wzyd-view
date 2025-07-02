package io.github.gdpl2112.funs;

import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.HttpApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author github kloping
 * @date 2025/7/1-10:05
 */
@Slf4j
@Component
public class UserProfile {

    @Data
    public static class UserRoleResult {
        private Integer result;
        private String returnMsg;
        private Integer returnCode;
        private String time;
        private List<JSONObject> data;
    }

    @Data
    public static class UserProfileResult {
        private Integer result;
        private Integer returnCode;
        private String returnMsg;
        private JSONObject data;
    }

    public static final String DATA_FORMAT = """
            friendUserId=%s&token=%s&userId=%s
            """;
    @Autowired
    HttpApi api;

    public UserRoleResult getUserRole(String yd_user_id) {
        log.info("getUserRole: {}", yd_user_id);
        Map<String, String> headers = api.getRequestHeaders();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Host", "ssl.kohsocialapp.qq.com:10001");
        Connection.Response response = null;
        try {
            response = Jsoup.connect(HttpApi.ALL_ROLE_LIST_V3)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .headers(headers)
                    .data("friendUserId", yd_user_id)
                    .data("token", headers.get("token"))
                    .data("userId", headers.get("userId"))
                    .method(Connection.Method.POST)
                    .execute();
            return JSONObject.parseObject(response.body(), UserRoleResult.class);
        } catch (IOException e) {
            log.error("getUserRoleError:" + yd_user_id, e);
        }
        return null;
    }

    public static final String FORMAT_DATA = """
            {"friendUserId": "%s", "roleId": "%s", "scenario": 0}
            """;

    public UserProfileResult getUserProfile(String yd_user_id, String role_id) {
        log.info("getUserProfile {}-{}", yd_user_id, role_id);
        try {
            Connection.Response response = Jsoup.connect(HttpApi.USER_PROFILE)
                    .ignoreHttpErrors(true).ignoreContentType(true)
                    .headers(api.getRequestHeadersOld())
                    .requestBody(String.format(FORMAT_DATA, yd_user_id, role_id))
                    .method(Connection.Method.POST)
                    .execute();
            return JSONObject.parseObject(response.body(), UserProfileResult.class);
        } catch (IOException e) {
            log.error("getUserProfileError:" + yd_user_id, e);
        }
        return null;
    }

    public static final String DATA_FORMAT_INDEX = """
            {"targetUserId": "%s", "recommendPrivacy": 0, "targetRoleId": "%s"}
            """;

    public UserProfileResult getUserProfileIndex(String yd_user_id, String role_id) {
        log.info("getUserProfileIndex {}-{}", yd_user_id, role_id);
        try {
            Connection.Response response = Jsoup.connect(HttpApi.PROFILE_INDEX)
                    .ignoreHttpErrors(true).ignoreContentType(true)
                    .headers(api.getRequestHeaders())
                    .requestBody(String.format(DATA_FORMAT_INDEX, yd_user_id, role_id))
                    .method(Connection.Method.POST)
                    .execute();
            return JSONObject.parseObject(response.body(), UserProfileResult.class);
        } catch (IOException e) {
            log.error("getUserProfileIndexError:" + yd_user_id, e);
        }
        return null;
    }

    public static final String DATA_FORMAT_HERO_LIST = """
            {"targetUserId": "%s", "recommendPrivacy": 0, "targetRoleId": "%s"}
            """;

    public UserProfileResult getUserProfileHeroList(String yd_user_id, String role_id) {
        log.info("getUserProfileHeroList {}-{}", yd_user_id, role_id);
        try {
            Connection.Response response = Jsoup.connect(HttpApi.PROFILE_HERO_LIST)
                    .ignoreHttpErrors(true).ignoreContentType(true)
                    .headers(api.getRequestHeaders())
                    .requestBody(String.format(DATA_FORMAT_HERO_LIST, yd_user_id, role_id))
                    .method(Connection.Method.POST)
                    .execute();
            return JSONObject.parseObject(response.body(), UserProfileResult.class);
        } catch (IOException e) {
            log.error("getUserProfileHeroListError:" + yd_user_id, e);
        }
        return null;
    }

}
