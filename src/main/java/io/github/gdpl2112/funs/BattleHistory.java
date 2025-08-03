package io.github.gdpl2112.funs;

import com.alibaba.fastjson2.JSON;
import io.github.gdpl2112.HttpApi;
import io.github.gdpl2112.funs.dto.BattleDetailResult;
import io.github.gdpl2112.funs.dto.BattleOneResult;
import io.github.gdpl2112.funs.dto.BattleResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * {"option":0,"friendRoleId":"1662968996","isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"534469328"}
 *
 * @author github kloping
 * @date 2025/6/30-22:43
 */
@Component
@Slf4j
public class BattleHistory {
    public static final String DATA_ROLE_FORMAT = """
            {"option":%s,"friendRoleId":"%s","isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"%s"}
            """;
    public static final String DATA_FORMAT = """
            {"option":%s,"isMultiGame":1,"apiVersion":5,"lastTime":0,"recommendPrivacy":0,"friendUserId":"%s"}
            """;
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
    public static final String ONE_HERO_DATA_FORMAT = """
            {"heroid": "%s", "lastTime": %s, "roleId": "%s", "recommendPrivacy": 0}
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
            BattleResult result = new BattleResult();
            result.setReturnCode(-1);
            result.setReturnMsg("请求失败:" + e.getMessage());
            return result;
        }
    }

    public BattleDetailResult getBattleDetail(String battleType, String gameSvr, String relaySvr, String wz_user_id, String gameSeq) {
        log.info("getBattleDetail: {}-{}", wz_user_id, gameSeq);
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
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public BattleOneResult getBattleOneHistory(Integer serverId, String roleId, Integer heroId, Integer lt) {
        Connection.Response response = null;
        log.info("getBattleOneHistory: {} {} {} {}", serverId, roleId, heroId, lt);
        try {
            Map<String, String> headers = api.getRequestHeaders();
            headers.put("serverId", serverId.toString());
            headers.put("gameServerId", serverId.toString());
            response = Jsoup.connect(HttpApi.HERO_HISTORY).ignoreHttpErrors(true).ignoreContentType(true)
                    .headers(headers)
                    .method(Connection.Method.POST)
                    .requestBody(String.format(ONE_HERO_DATA_FORMAT, heroId, lt, roleId))
                    .execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return JSON.parseObject(response.body(), BattleOneResult.class);
    }

}
