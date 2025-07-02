package io.github.gdpl2112.funs;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.HttpApi;
import io.github.gdpl2112.dto.DataList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * {"option":0,"friendRoleId":"1662968996","isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"534469328"}
 *
 * @author github kloping
 * @date 2025/6/30-22:43
 */
@Component
@Slf4j
public class BattleHistory {
    @Data
    @Accessors(chain = true)
    public static class BattleResult {
        private Integer returnCode = 0;
        private String returnMsg;
        private DataList data;
    }

    //     "result": 0,
//  "returnCode": 0,
//  "returnMsg": "",
//  "data": {
//    "head": {
//      "gameResult": true,
//      "acntCamp": 1,
//      "mapName": "无限乱斗",
//      "gradeGame": "7.2",
//      "kda": "17.25",
//      "labels": [],
//      "killCnt": 1,
//      "deadCnt": 2,
//      "assistCnt": 22,
//      "roleId": "321970740",
//      "roleIcon": "https://thirdqq.qlogo.cn/ek_qqapp/AQKMakB5Xm7XkWssUb5LcBlwVUhSPB4uD7gPZSPeIkObpFsAticV1xaM14h7Tibj5CsVsZduHSJQGeQQuiaClibwahVaQrl9NmrZOBibiaXDlOWLpZsgZ3ANgiblPxmWahdlw/100",
//      "roleName": "唯爱小祁呆猫",
//      "bgImg": "https://game-1255653016.file.myqcloud.com/manage/custom_wzry_D1/2d62bed9f28785d89ae6303d462034d0.png",
//      "teamNum": 2,
//      "matchDesc": "",
//      "tips": "这局你能扛能打，真滴猛😱",
//      "userId": "94113009",
//      "heroId": 175,
//      "heroName": "钟馗",
//      "playerId": "6616182426531185405",
//      "is10v10": false
//    },
//
    @Data
    @Accessors(chain = true)
    public static class BattleDetailResult {
        private Integer result;
        private Integer returnCode = 0;
        private String returnMsg;
        private BattleDetailResultData data;
    }

    @Data
    @Accessors(chain = true)
    public static class BattleDetailResultData {
        private JSONObject head;
        private JSONObject battle;
        private JSONObject redTeam;
        private JSONObject blueTeam;
        private JSONArray redRoles;
        private JSONArray blueRoles;
        private String jumpUrl;
    }

    public static final String DATA_ROLE_FORMAT = """
            {"option":%s,"friendRoleId":"%s","isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"%s"}
            """;
    public static final String DATA_FORMAT = """
            {"option":%s,"isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"%s"}
            """;

    @Autowired
    HttpApi api;

    public BattleResult getBattleHistory(String friendId) {
        return getBattleHistory(friendId, 0);
    }

    public BattleResult getBattleHistory(String friendId, Integer opt) {
        Connection.Response response = null;
        try {
            log.info("getBattleHistory: {}", friendId);
            response = Jsoup.connect(HttpApi.BATTLE_HISTORY)
                    .headers(api.getRequestHeaders())
                    .requestBody(String.format(DATA_FORMAT, opt, friendId))
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true).method(Connection.Method.POST)
                    .execute();
            String json = response.body();
            return JSON.parseObject(json, BattleResult.class);
        } catch (IOException e) {
            log.error("getBattleHistoryError: {}", friendId);
            return new BattleResult().setReturnCode(-1).setReturnMsg("请求失败:" + e.getMessage());
        }
    }

    public static final String BATTLE_DATA = """
            {
              "recommendPrivacy": 0,
              "battleType": %s,
              "gameSvr": "%s",
              "relaySvr": "%s",
              "targetRoleId": "%s",
              "gameSeq": "%s"
            }
            """;

    public BattleDetailResult getBattleDetail(String battleType, String gameSvr, String relaySvr, String wz_user_id, String gameSeq) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(HttpApi.BATTLE_DETAIL)
                    .headers(api.getRequestHeaders())
                    .requestBody(String.format(BATTLE_DATA, battleType, gameSvr, relaySvr, wz_user_id, gameSeq))
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true).method(Connection.Method.POST)
                    .execute();
            String json = response.body();
            return JSON.parseObject(json, BattleDetailResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
