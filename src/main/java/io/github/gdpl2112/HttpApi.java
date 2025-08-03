package io.github.gdpl2112;

import io.github.gdpl2112.config.BindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author github kloping
 * @date 2025/6/30-22:17
 */
@Component
public class HttpApi {

    public static final String MAIN_URL = "https://kohcamp.qq.com";
    public static final String GAME_URL = "https://ssl.kohsocialapp.qq.com:10001";

    public static final String MGAME_URL = MAIN_URL + "/game";
    //营地
    public static final String BATTLE_HISTORY = MGAME_URL + "/morebattlelist";
    public static final String BATTLE_DETAIL = MGAME_URL + "/battledetail";
    public static final String SEASON_STATUS = MGAME_URL + "/seasonpage";
    public static final String PROFILE_INDEX = MGAME_URL + "/profile/index";
    public static final String PROFILE_HERO_LIST = MGAME_URL + "/profile/herolist";
    public static final String HERO_LIST = "https://pvp.qq.com/web201605/js/herolist.json";
    //基本
    public static final String USER_PROFILE = MAIN_URL + "/userprofile/profile";
    //特殊
    public static final String ALL_ROLE_LIST_V3 = GAME_URL + "/game/allrolelistv3";
    public static final String SKIN_LIST = GAME_URL + "/play/h5getheroskinlist";

    //资料信息
    public static final String HERO_RANK_LIST = MAIN_URL + "/hero/getmaintranklist";
    public static final String PAGE_HERO_HISTORY = MAIN_URL + "/gametoolbox/hero/record/pagedetails";
    public static final String HERO_HISTORY = MAIN_URL + "/gametoolbox/hero/record/historydetails";

    @Autowired
    BindConfig bindConfig;

    private static String getCRand() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String getOpenId() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        return uuid.toUpperCase();
    }

    public Map<String, String> getRequestHeaders() {
        BindConfig.UserToken token = bindConfig.getToken();
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json; charset=UTF-8");
        map.put("User-Agent", "okhttp/4.9.1");
        map.put("Accept-Encoding", "gzip");
        map.put("Host", "kohcamp.qq.com");
        map.put("Connection", "Keep-Alive");
        map.put("kohDimGender", "2");
        map.put("NOENCRYPT", "1");
        map.put("X-Client-Proto", "https");
        map.put("Content-Length", "132");
        map.put("Accept-Encrypt", "");
        map.put("Content-Encrypt", "");
        map.put("tinkerId", "2047929308_64_0");
        map.put("token", token.token);
        map.put("userId", token.user);
        map.put("gameOpenId", getOpenId());
        map.put("gameId", "20001");
        map.put("gameAreaId", "1");
        map.put("cSupportArm64", "true");
        map.put("cSystemVersionName", "7.1.2");
        map.put("cSystemVersionCode", "25");
        map.put("cSystem", "android");
        map.put("cRand", getCRand());
        map.put("cIsArm64", "true");
        map.put("cGzip", "1");
        map.put("cGameId", "20001");
        map.put("cCurrentGameId", "20001");
        map.put("cChannelId", "10003391");
        map.put("cClientVersionName", "9.103.0611");
        map.put("cClientVersionCode", "2047929308");
        map.put("isTRPCRequest", "true");
        map.put("cpuHardware", "qcom");
        map.put("cDeviceId", "9c5c0c0c-c5c0-c5c0-c5c0-c5c0c5c5c5c5");
        return map;
    }

    public Map<String, String> getRequestHeadersOld() {
        Map<String, String> map = getRequestHeaders();
        map.put("cClientVersionName", "8.92.0125");
        map.put("cclientversioncode", "2037857908");
        return map;
    }
}
