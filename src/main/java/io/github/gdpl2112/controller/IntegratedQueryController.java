package io.github.gdpl2112.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.HttpApi;
import io.github.gdpl2112.dto.DataZjList;
import io.github.gdpl2112.dto.UserDataProto;
import io.github.gdpl2112.funs.BattleHistory;
import io.github.gdpl2112.funs.UserProfile;
import io.github.gdpl2112.funs.dto.BattleOneResult;
import io.github.gdpl2112.funs.dto.BattleResult;
import io.github.gdpl2112.funs.dto.UserProfileResult;
import io.github.gdpl2112.funs.dto.UserRoleResult;
import io.github.gdpl2112.utils.CustomProtobufBuilder;
import io.github.kloping.judge.Judge;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 集成查询控制器
 * 将查询用户、用户信息、战斗信息整合到一个API接口
 *
 * @author github kloping
 */
@Slf4j
@RestController
@RequestMapping("/integrated")
public class IntegratedQueryController {

    @Autowired
    HttpApi api;

    @Autowired
    UserProfile userProfile;

    @Autowired
    BattleHistory battleHistory;

    OkHttpClient client = new OkHttpClient();

    /**
     * 用户简要信息
     */
    public static class UserBrief {
        public String uid;
        public String name;
        public String dw;
        public String region;
        public String level;
        public String avatar;
    }

    /**
     * 集成查询接口
     *
     * @param name 查询昵称（用于模糊搜索）
     * @param uid  指定用户ID（用于精确查询）
     * @param opt  战斗类型选项（可选：排位/巅峰/标准/娱乐/英雄名，默认全部）
     * @return 查询结果
     *
     * 使用方式：
     * 1. /integrated/query?name=xxx  → 通过昵称查询用户
     *    - 如果查到0个用户：返回错误
     *    - 如果查到1个用户：直接返回完整信息（用户信息+战斗历史）
     *    - 如果查到多个用户：返回用户列表，需要选择
     * 2. /integrated/query?uid=xxx   → 直接查询指定用户的完整信息
     * 3. /integrated/query?uid=xxx&opt=排位 → 查询指定用户的排位战斗历史
     */
    @RequestMapping("/query")
    public Object query(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid,
            @RequestParam(name = "opt", required = false, defaultValue = "") String opt
    ) {
        try {
            // 如果传入了 uid，直接查询完整信息
            if (Judge.isNotEmpty(uid)) {
                return queryFullInfo(uid, opt);
            }

            // 如果传入了 name，先查询用户列表
            if (Judge.isEmpty(name)) {
                return ResponseEntity.badRequest().body("请传入 name（查询昵称）或 uid（指定用户ID）参数");
            }

            // 通过昵称查询用户列表
            List<UserBrief> userList = searchUsersByName(name);

            if (userList.isEmpty()) {
                return ResponseEntity.badRequest().body("未找到相关用户: " + name);
            }

            // 如果只有一个用户，直接返回完整信息
            if (userList.size() == 1) {
                UserBrief user = userList.get(0);
                return queryFullInfo(user.uid, opt);
            }

            // 如果有多个用户，返回列表供选择
            JSONObject result = new JSONObject();
            result.put("needSelect", true);
            result.put("message", "查询到多个用户，请选择其中一个进行查询");
            result.put("userCount", userList.size());

            JSONArray users = new JSONArray();
            for (UserBrief user : userList) {
                JSONObject userObj = new JSONObject();
                userObj.put("uid", user.uid);
                userObj.put("name", user.name);
                userObj.put("region", user.region);
                userObj.put("level", user.level);
                userObj.put("avatar", user.avatar);
                userObj.put("dw", user.dw);
                users.add(userObj);
            }
            result.put("users", users);

            return result;

        } catch (Exception e) {
            log.error("integrated query error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定用户的完整信息（用户信息 + 战斗历史）
     */
    private Object queryFullInfo(String uid, String opt) {
        log.info("query full info for uid: {}, opt: {}", uid, opt);

        JSONObject result = new JSONObject();
        result.put("uid", uid);

        // 1. 获取用户角色信息
        UserRoleResult userRoleResult = userProfile.getUserRole(uid);
        if (userRoleResult.getReturnCode() == null || userRoleResult.getReturnCode() != 0) {
            return ResponseEntity.badRequest().body(
                    "获取用户角色信息失败: " + (userRoleResult.getReturnMsg() != null ? userRoleResult.getReturnMsg() : "未知错误")
            );
        }

        if (userRoleResult.getData() == null || userRoleResult.getData().isEmpty()) {
            return ResponseEntity.badRequest().body("未找到该用户的角色信息");
        }

        Map<String, Object> rData = userRoleResult.getData().get(0);
        String roleId = (String) rData.get("roleId");

        // 2. 构建用户基本信息
        JSONObject userInfo = new JSONObject();
        userInfo.put("roleId", roleId);
        userInfo.put("roleName", rData.get("roleName"));
        userInfo.put("roleDesc", rData.get("roleDesc"));
        userInfo.put("roleIcon", rData.get("roleIcon"));
        userInfo.put("serverId", rData.get("serverId"));
        userInfo.put("gameOnline", rData.get("gameOnline"));
        userInfo.put("appOnline", rData.get("appOnline"));
        result.put("userInfo", userInfo);

        // 3. 获取用户详细资料
        UserProfileResult profileData = userProfile.getUserProfile(uid, roleId);
        if (profileData != null && profileData.getReturnCode() != null && profileData.getReturnCode() == 0) {
            result.put("profile", profileData.getData());
        }

        UserProfileResult profileIndexData = userProfile.getUserProfileIndex(uid, roleId);
        if (profileIndexData != null && profileIndexData.getReturnCode() != null && profileIndexData.getReturnCode() == 0) {
            result.put("profileIndex", profileIndexData.getData());
        }

        UserProfileResult heroListData = userProfile.getUserProfileHeroList(uid, roleId);
        if (heroListData != null && heroListData.getReturnCode() != null && heroListData.getReturnCode() == 0) {
            result.put("heroList", heroListData.getData());
        }

        // 4. 获取战斗历史
        Integer optn = filterToOpt(opt);
        List<Object> battleList = new LinkedList<>();

        if (optn < 100) {
            BattleResult battleResult = battleHistory.getBattleHistory(uid, optn);
            if (battleResult != null && battleResult.getReturnCode() != null && battleResult.getReturnCode() == 0) {
                if (battleResult.getData() != null && battleResult.getData().getList() != null) {
                    battleList.addAll(battleResult.getData().getList());
                }
            }
        } else {
            Integer serverId = (Integer) rData.get("serverId");
            int lt = Math.toIntExact(System.currentTimeMillis() / 1000);
            while (true) {
                BattleOneResult oneResult = battleHistory.getBattleOneHistory(serverId, roleId, optn, lt);
                if (oneResult == null || oneResult.getData() == null) break;
                DataZjList list = oneResult.getData();
                if (list.getZjList() != null) {
                    battleList.addAll(list.getZjList());
                }
                if (battleList.size() >= 12) break;
                if (list.getZjList() != null && list.getZjList().size() >= 5) {
                    JSONObject endOne = (JSONObject) battleList.get(battleList.size() - 1);
                    lt = Math.toIntExact(endOne.getLongValue("gameseq"));
                } else break;
            }
        }

        // 5. 战斗统计
        JSONObject battleStats = new JSONObject();
        battleStats.put("totalCount", battleList.size());

        int totalWins = 0;
        int mvpCount = 0;
        Map<String, Integer> battleTypeCount = new HashMap<>();
        Map<String, Integer> winCount = new HashMap<>();

        for (Object battleObj : battleList) {
            JSONObject battle = (JSONObject) battleObj;
            String mapName = battle.getString("mapName");
            String desc = battle.getString("desc");
            if (Judge.isEmpty(desc)) desc = battle.getString("matchDesc");
            String mvpUrlV3 = battle.getString("mvpUrlV3");
            if (Judge.isNotEmpty(mvpUrlV3)) mvpCount++;

            int gameResult = battle.getIntValue("gameresult");
            if (gameResult == 1) {
                totalWins++;
            }

            String battleType = getBattleType(mapName, desc);
            battleTypeCount.put(battleType, battleTypeCount.getOrDefault(battleType, 0) + 1);
            if (gameResult == 1) {
                winCount.put(battleType, winCount.getOrDefault(battleType, 0) + 1);
            }
        }

        battleStats.put("winCount", totalWins);
        battleStats.put("mvpCount", mvpCount);
        if (!battleList.isEmpty()) {
            double overallWinRate = (double) totalWins / battleList.size() * 100;
            battleStats.put("winRate", String.format("%.1f%%", overallWinRate));
        }

        // 战斗类型统计
        JSONArray typeStats = new JSONArray();
        for (Map.Entry<String, Integer> entry : battleTypeCount.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            int wins = winCount.getOrDefault(type, 0);
            double winRate = count > 0 ? (double) wins / count * 100 : 0;

            JSONObject typeObj = new JSONObject();
            typeObj.put("type", type);
            typeObj.put("count", count);
            typeObj.put("wins", wins);
            typeObj.put("winRate", String.format("%.1f%%", winRate));
            typeStats.add(typeObj);
        }
        battleStats.put("typeStats", typeStats);

        result.put("battleStats", battleStats);
        result.put("battleList", battleList);

        return result;
    }

    /**
     * 通过昵称搜索用户列表
     */
    private List<UserBrief> searchUsersByName(String name) {
        List<UserBrief> result = new LinkedList<>();

        byte[] bytes = CustomProtobufBuilder.buildCustomProtobufMessage(name);
        RequestBody body = RequestBody.create(bytes, MediaType.parse("application/x-protobuf"));

        Map<String, String> headers = api.getRequestHeaders();
        Request request = new Request.Builder()
                .url("https://kohcamp.qq.com/search/getbytype")
                .post(body)
                .headers(Headers.of(headers))
                .build();

        try (Response response = client.newCall(request).execute()) {
            byte[] data = response.body().bytes();
            UserDataProto.Response allData = UserDataProto.Response.parseFrom(data);

            for (UserDataProto.UserGroup userGroup : allData.getData().getUserGroupsList()) {
                for (UserDataProto.User user : userGroup.getUsersList()) {
                    UserBrief userBrief = new UserBrief();
                    userBrief.uid = String.valueOf(user.getDetail().getFriendUid());
                    userBrief.name = user.getDetail().getNickname();
                    userBrief.region = user.getDetail().getRegion();
                    userBrief.level = String.valueOf(user.getDetail().getLevel());
                    userBrief.avatar = user.getDetail().getAvatarUrl();
                    userBrief.dw = user.getDetail().getTitle1();
                    result.add(userBrief);
                }
            }
        } catch (Exception e) {
            log.error("search users by name error: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 根据地图名称和描述确定对战类型
     */
    private String getBattleType(String mapName, String desc) {
        if (Judge.isNotEmpty(mapName)) {
            if (mapName.contains("排位")) return "排位赛";
            else if (mapName.contains("巅峰")) return "巅峰赛";
            else if (mapName.contains("王者峡谷")) return "匹配赛";
            else if (mapName.contains("无限") || mapName.contains("火焰") || mapName.contains("长平"))
                return "娱乐模式";
        }
        if (Judge.isNotEmpty(desc)) {
            if (desc.contains("排位")) return "排位赛";
            else if (desc.contains("巅峰")) return "巅峰赛";
            else if (desc.contains("匹配")) return "匹配赛";
            else if (desc.contains("娱乐")) return "娱乐模式";
        }
        return "其他模式";
    }

    /**
     * 将 opt 字符串转换为选项数字
     */
    private Integer filterToOpt(String opt) {
        if (Judge.isEmpty(opt)) return 0;
        else if (opt.startsWith("排位")) return 1;
        else if (opt.startsWith("标准")) return 2;
        else if (opt.startsWith("娱乐")) return 3;
        else if (opt.startsWith("巅峰")) return 4;
        return 0;
    }
}
