package io.github.gdpl2112.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.config.BindConfig;
import io.github.gdpl2112.config.ResConfig;
import io.github.gdpl2112.dto.DataZjList;
import io.github.gdpl2112.funs.BattleHistory;
import io.github.gdpl2112.funs.HerosReq;
import io.github.gdpl2112.funs.UserProfile;
import io.github.gdpl2112.funs.dto.*;
import io.github.gdpl2112.utils.BufferedImageUtils;
import io.github.kloping.judge.Judge;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * @author github kloping
 * @date 2025/7/1-09:48
 */
@Slf4j
@Service
public class BattleService {
    public static final String BATTLE_PATH = "battle";
    public static final String FONT_STYLE = "微软雅黑";
    public static final Font FONT_40 = new Font(FONT_STYLE, Font.PLAIN, 40);
    public static final Font FONT_33 = new Font(FONT_STYLE, Font.PLAIN, 33);
    public static final Font FONT_32 = new Font(FONT_STYLE, Font.BOLD, 32);
    public static final Font FONT_30 = new Font(FONT_STYLE, Font.PLAIN, 30);
    public static final Font FONT_18 = new Font(FONT_STYLE, Font.PLAIN, 20);
    public static final Font FONT_20 = new Font(FONT_STYLE, Font.PLAIN, 18);
    public static final Font FONT_26 = new Font(FONT_STYLE, Font.PLAIN, 26);
    @Autowired
    ResConfig resConfig;
    @Autowired
    BindConfig bindConfig;
    @Autowired
    BattleHistory battleHistory;
    @Autowired
    UserProfile userRoleFuns;
    @Autowired
    HerosReq req;

    public BattleService() {
        ImageIO.setUseCache(false);
    }

    /**
     * @param sid 要查询的ID
     * @param opt 选项 排位..巅峰..
     * @return
     */
    public Object history(
            String sid,
            String opt,
            String uid,
            HttpServletResponse response
    ) {
        try {
            if (Judge.isEmpty(uid)) {
                if (Judge.isNotEmpty(sid)) uid = bindConfig.getBind(sid);
            }
            if (Judge.isEmpty(uid)) {
                return ResponseEntity.badRequest().body("未绑定UID");
            }
            log.info("start select battle history: {}", sid);
            Integer optn = null;
            try {
                optn = filterToOpt(opt);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            UserRoleResult userRoleResult = userRoleFuns.getUserRole(uid);
            if (userRoleResult.getReturnCode() != 0) {
                return ResponseEntity.badRequest().body(userRoleResult.getReturnMsg());
            }
            Map<String, Object> rData = userRoleResult.getData().get(0);
            String roleId = rData.get("roleId").toString();
            List<Object> battleList = new LinkedList<>();
            if (optn < 100) {
                BattleResult battleResult = battleHistory.getBattleHistory(uid, optn);
                if (battleResult == null || battleResult.getReturnCode() != 0)
                    return ResponseEntity.badRequest().body(battleResult.getReturnMsg());
                battleList.addAll(battleResult.getData().getList());
            } else {
                Integer serverId = (Integer) rData.get("serverId");
                int lt = Math.toIntExact(System.currentTimeMillis() / 1000);
                //查询单个英雄
                while (true) {
                    BattleOneResult result = battleHistory.getBattleOneHistory(serverId, roleId, optn, lt);
                    DataZjList list = result.getData();
                    battleList.addAll(list.getZjList());
                    if (battleList.size() >= 12) break;
                    if (list.getZjList().size() >= 5) {
                        JSONObject endOne = (JSONObject) battleList.get(battleList.size() - 1);
                        lt = Math.toIntExact(endOne.getLongValue("gameseq"));
                    } else break;
                }
            }
            String game_text;
            int online = (int) rData.get("gameOnline");
            if (online > 0) {
                game_text = "游戏在线";
            } else {
                int appOnline = (int) rData.get("appOnline");
                if (appOnline == 0) game_text = "离线";
                else game_text = "营地在线";
            }

            BufferedImage bg = ImageIO.read(resConfig.getResourceBytes("bg"));
            int h = 2000;
            if (battleList.size() < 12) {
                h = 500 + battleList.size() * 120 + 60;
            }

            bg = BufferedImageUtils.image2size(950, h, bg);

            String roleIcon = (String) rData.get("roleIcon");
            roleIcon = filterTo0Icon(roleIcon);
            BufferedImage roleIconImg = ImageIO.read(new URL(roleIcon));
            roleIconImg = BufferedImageUtils.image2size(320, 320, roleIconImg);
            roleIconImg = BufferedImageUtils.cropToRoundedCorner(roleIconImg, 320);

            BufferedImage title = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, "title.png"));
            BufferedImage ring = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, "avatar_ring.png"));

            Graphics2D g2d = (Graphics2D) bg.getGraphics();
            g2d.setComposite(AlphaComposite.SrcOver);

            g2d.drawImage(roleIconImg, 310, 70, roleIconImg.getWidth(), roleIconImg.getHeight(), null);
            g2d.drawImage(title, 0, 405, title.getWidth(), title.getHeight(), null);

            BufferedImage hero_mask = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, "avatar_mask.png"));
            BufferedImage skill_mask = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, "skill_mask.png"));

            g2d.setColor(new Color(48, 48, 48));

            int index = 0;
            for (Object o : battleList) {
                if (index >= 12) break;
                drawOneBattle((JSONObject) o, roleId, skill_mask, hero_mask, ring, g2d, index);
                index++;
            }

            g2d.setFont(FONT_40);
            g2d.setColor(Color.BLACK);
            g2d.drawString("王者荣耀战绩", 475 -
                    g2d.getFontMetrics().stringWidth("王者荣耀战绩") / 2, 437 + 12);

            g2d.setFont(FONT_30);
            g2d.drawString(game_text, 475 -
                            g2d.getFontMetrics().stringWidth(game_text) / 2
                    , 495 + 10);

            g2d.dispose();

            response.setContentType("image/png");
            ImageIO.write(bg, "png", response.getOutputStream());
            log.info("end select battle list");
            bg.flush();
            return null;
        } catch (IOException e) {
            log.error("getBattleHistoryError: {}", e.getMessage());
            return null;
        } finally {
        }
    }

    /**
     * 返回战斗历史的文本详情。
     *
     * <p>数据源与 {@link #history(String, String, String, HttpServletResponse)} 相同，
     * 但不生成图片，适合机器人、命令行等场景直接展示。</p>
     */
    public ResponseEntity<String> historyText(
            String sid,
            String opt,
            String uid
    ) {
        try {
            uid = resolveUid(sid, uid);
            if (Judge.isEmpty(uid)) {
                return textError("未绑定UID");
            }

            Integer optn;
            try {
                optn = filterToOpt(opt);
            } catch (RuntimeException e) {
                return textError(e.getMessage());
            }

            UserRoleResult userRoleResult = userRoleFuns.getUserRole(uid);
            if (userRoleResult == null || !Objects.equals(userRoleResult.getReturnCode(), 0)) {
                String message = userRoleResult == null ? "获取用户角色失败" : userRoleResult.getReturnMsg();
                return textError(message);
            }
            if (userRoleResult.getData() == null || userRoleResult.getData().isEmpty()) {
                return textError("未找到用户角色");
            }

            Map<String, Object> roleData = userRoleResult.getData().get(0);
            String roleId = stringValue(roleData.get("roleId"));
            List<Object> battleList = loadBattleList(uid, optn, roleData, roleId);
            String text = buildHeroSummary(uid, roleId) + buildHistoryText(battleList, opt);
            log.info("end text battle history: {}", sid);
            return textResponse(text);
        } catch (Exception e) {
            log.error("getBattleHistoryTextError: {}", e.getMessage(), e);
            return textError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户常玩英雄摘要。英雄资料接口失败时不影响战绩文本返回。
     */
    private String buildHeroSummary(String uid, String roleId) {
        StringBuilder result = new StringBuilder("常玩英雄:\n");
        try {
            UserProfileResult heroResult = userRoleFuns.getUserProfileHeroList(uid, roleId);
            if (heroResult == null || (heroResult.getReturnCode() != null
                    && !Objects.equals(heroResult.getReturnCode(), 0))
                    || heroResult.getData() == null) {
                return result.append("暂无数据\n\n").toString();
            }
            JSONArray heroList = heroResult.getData().getJSONArray("heroList");
            if (heroList == null || heroList.isEmpty()) {
                return result.append("暂无数据\n\n").toString();
            }

            List<JSONObject> heroes = new ArrayList<>();
            for (Object value : heroList) {
                if (value instanceof JSONObject hero && hero.getJSONObject("basicInfo") != null) {
                    heroes.add(hero);
                }
            }
            heroes.sort(Comparator.comparingInt((JSONObject hero) -> {
                JSONObject basicInfo = hero.getJSONObject("basicInfo");
                Integer playNum = integerValue(basicInfo.get("playNum"));
                return playNum == null ? 0 : playNum;
            }).reversed());

            int count = Math.min(5, heroes.size());
            if (count == 0) {
                return result.append("暂无数据\n\n").toString();
            }
            for (int i = 0; i < count; i++) {
                JSONObject basicInfo = heroes.get(i).getJSONObject("basicInfo");
                String name = firstNonEmpty(basicInfo, "title", "heroName", "name", "cname");
                String power = valueOrUnknown(firstNonEmpty(basicInfo,
                        "heroFightPower", "fightPower", "power"));
                String winRate = valueOrUnknown(firstNonEmpty(basicInfo, "winRate", "winrate"));
                String playNum = valueOrUnknown(firstNonEmpty(basicInfo, "playNum", "playnum", "games"));
                result.append(String.format("%d. %s | 战力:%s | 胜率:%s | 场次:%s\n",
                        i + 1, valueOrUnknown(name), power, winRate, playNum));
            }
            result.append('\n');
        } catch (Exception e) {
            log.debug("get user hero summary failed: {}", uid, e);
            result.append("暂无数据\n\n");
        }
        return result.toString();
    }

    private String resolveUid(String sid, String uid) {
        if (Judge.isEmpty(uid) && Judge.isNotEmpty(sid)) {
            uid = bindConfig.getBind(sid);
        }
        return uid;
    }

    private List<Object> loadBattleList(String uid, Integer optn, Map<String, Object> roleData, String roleId) {
        List<Object> battleList = new LinkedList<>();
        if (optn < 100) {
            BattleResult battleResult = battleHistory.getBattleHistory(uid, optn);
            if (battleResult == null) {
                throw new RuntimeException("获取战绩失败");
            }
            if (!Objects.equals(battleResult.getReturnCode(), 0)) {
                throw new RuntimeException(defaultMessage(battleResult.getReturnMsg(), "获取战绩失败"));
            }
            if (battleResult.getData() != null && battleResult.getData().getList() != null) {
                battleList.addAll(battleResult.getData().getList());
            }
            return battleList;
        }

        Integer serverId = integerValue(roleData.get("serverId"));
        if (serverId == null || Judge.isEmpty(roleId)) {
            throw new RuntimeException("用户角色信息不完整");
        }
        int lastTime = Math.toIntExact(System.currentTimeMillis() / 1000);
        for (int page = 0; page < 20 && battleList.size() < 12; page++) {
            BattleOneResult result = battleHistory.getBattleOneHistory(serverId, roleId, optn, lastTime);
            if (result == null || !Objects.equals(result.getReturnCode(), 0) || result.getData() == null
                    || result.getData().getZjList() == null || result.getData().getZjList().isEmpty()) {
                break;
            }
            int oldSize = battleList.size();
            battleList.addAll(result.getData().getZjList());
            if (battleList.size() == oldSize) {
                break;
            }
            if (result.getData().getZjList().size() < 5) {
                break;
            }
            JSONObject lastBattle = (JSONObject) battleList.get(battleList.size() - 1);
            Long sequence = longValue(lastBattle, "gameseq", "gameSeq");
            if (sequence == null) {
                break;
            }
            lastTime = Math.toIntExact(sequence);
        }
        return battleList;
    }

    private String buildHistoryText(List<Object> battleList, String opt) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Integer> wins = new LinkedHashMap<>();
        List<JSONObject> battles = new ArrayList<>();
        int totalWins = 0;
        for (Object value : battleList) {
            if (!(value instanceof JSONObject battle)) {
                continue;
            }
            battles.add(battle);
            String mode = getMainBattleType(getBattleType(
                    firstNonEmpty(battle, "mapName", "mapname"),
                    firstNonEmpty(battle, "desc", "matchDesc"), opt));
            counts.merge(mode, 1, Integer::sum);
            if (isWin(battle)) {
                totalWins++;
                wins.merge(mode, 1, Integer::sum);
            }
        }

        StringBuilder result = new StringBuilder("王者荣耀战绩\n");
        result.append(String.format("一共%d局，胜利%d局，失败%d局，胜率%.1f%%\n",
                battles.size(), totalWins, battles.size() - totalWins, percentage(totalWins, battles.size())));
        appendModeSummary(result, "排位赛", counts, wins);
        appendModeSummary(result, "匹配赛", counts, wins);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!entry.getKey().equals("排位赛") && !entry.getKey().equals("匹配赛")) {
                appendModeSummary(result, entry.getKey(), counts, wins);
            }
        }

        result.append("\n逐局明细:\n");
        if (battles.isEmpty()) {
            result.append("暂无战绩\n");
            return result.toString();
        }
        Map<Integer, String> heroNames = new HashMap<>();
        for (int i = 0; i < battles.size(); i++) {
            JSONObject battle = battles.get(i);
            String hero = findHeroName(battle);
            if (Judge.isEmpty(hero)) {
                Integer heroId = integerValue(firstValue(battle, "heroId", "heroid"));
                hero = resolveHeroName(heroId, heroNames);
            }
            String score = firstNonEmpty(battle, "gradeGame", "grade", "score");
            String mvp = firstNonEmpty(battle, "mvp", "mvpFlag");
            if (Judge.isEmpty(mvp) && Judge.isNotEmpty(firstNonEmpty(battle, "mvpUrlV3", "mvpUrl"))) {
                mvp = "是";
            }
            String map = firstNonEmpty(battle, "mapName", "mapname");
            String mode = getBattleType(map, firstNonEmpty(battle, "desc", "matchDesc"), opt);
            result.append(String.format("%d. 时间:%s | 模式:%s | 英雄:%s | KDA:%d/%d/%d | 结果:%s",
                    i + 1,
                    valueOrUnknown(firstNonEmpty(battle, "gametime", "gameTime", "game_time", "createTime")),
                    valueOrUnknown(mode), valueOrUnknown(hero),
                    intValue(battle, "killcnt", "kills", "killCount"),
                    intValue(battle, "deadcnt", "deaths", "deathCount"),
                    intValue(battle, "assistcnt", "assists", "assistCount"),
                    formatResult(battle)));
            if (Judge.isNotEmpty(score)) result.append(" | 评分:").append(score);
            if (Judge.isNotEmpty(mvp)) result.append(" | MVP:").append(mvp);
            if (Judge.isNotEmpty(map) && !map.equals(mode)) result.append(" | 地图:").append(map);
            String duration = firstNonEmpty(battle, "gameDuration", "duration", "gameTimeSec");
            if (Judge.isNotEmpty(duration)) result.append(" | 时长:").append(duration);
            result.append('\n');
        }
        return result.toString();
    }

    private void appendModeSummary(StringBuilder result, String mode,
                                   Map<String, Integer> counts, Map<String, Integer> wins) {
        int count = counts.getOrDefault(mode, 0);
        int win = wins.getOrDefault(mode, 0);
        result.append(String.format("%s:%d局，胜率%.1f%%\n", mode, count, percentage(win, count)));
    }

    private String findHeroName(JSONObject battle) {
        String hero = firstNonEmpty(battle, "heroName", "heroCname", "hero_name", "cname");
        if (Judge.isNotEmpty(hero)) return hero;
        Object heroValue = battle.get("hero");
        if (heroValue instanceof JSONObject heroObject) {
            return firstNonEmpty(heroObject, "name", "cname", "heroName");
        }
        return null;
    }

    private String resolveHeroName(Integer heroId, Map<Integer, String> heroNames) {
        if (heroId == null || heroId <= 0) return null;
        if (heroNames.containsKey(heroId)) return heroNames.get(heroId);
        try {
            List<HeroData> heroes = req.getHeros();
            if (heroes != null) {
                for (HeroData hero : heroes) {
                    if (hero != null && heroId.equals(hero.getEname())) {
                        heroNames.put(heroId, hero.getCname());
                        return hero.getCname();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("resolve hero name failed: {}", heroId, e);
        }
        heroNames.put(heroId, String.valueOf(heroId));
        return String.valueOf(heroId);
    }

    private boolean isWin(JSONObject battle) {
        Object value = firstValue(battle, "gameresult", "gameResult", "result", "status");
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() == 1;
        String text = value == null ? "" : String.valueOf(value).toLowerCase(Locale.ROOT);
        return "1".equals(text) || text.contains("胜") || text.contains("win");
    }

    private String formatResult(JSONObject battle) {
        Object value = firstValue(battle, "gameresult", "gameResult", "result", "status");
        if (value == null) return "未知";
        if (isWin(battle)) return "胜利";
        if (value instanceof Number || "0".equals(String.valueOf(value))) return "失败";
        return String.valueOf(value);
    }

    private int intValue(JSONObject battle, String... keys) {
        Integer value = integerValue(firstValue(battle, keys));
        return value == null ? 0 : value;
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return null;
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long longValue(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value instanceof Number number) return number.longValue();
        if (value == null) return null;
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Object firstValue(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.containsKey(key) && object.get(key) != null) return object.get(key);
        }
        return null;
    }

    private String firstNonEmpty(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.getString(key);
            if (Judge.isNotEmpty(value)) return value;
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String valueOrUnknown(String value) {
        return Judge.isEmpty(value) ? "未知" : value;
    }

    private double percentage(int numerator, int denominator) {
        return denominator <= 0 ? 0 : (double) numerator / denominator * 100;
    }

    private String defaultMessage(String message, String fallback) {
        return Judge.isEmpty(message) ? fallback : message;
    }

    private ResponseEntity<String> textResponse(String body) {
        MediaType mediaType = new MediaType("text", "plain", StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(mediaType).body(body);
    }

    private ResponseEntity<String> textError(String message) {
        MediaType mediaType = new MediaType("text", "plain", StandardCharsets.UTF_8);
        return ResponseEntity.badRequest().contentType(mediaType).body(defaultMessage(message, "查询失败"));
    }

    private String filterTo0Icon(String roleIcon) {
        int i = roleIcon.lastIndexOf("/");
        if (i > 0) {
            String s = roleIcon.substring(0, i + 1);
            return s + "0";
        }
        return roleIcon;
    }

    public void drawOneBattle(JSONObject battle, String roleId, BufferedImage skill_mask, BufferedImage hero_mask, BufferedImage ring, Graphics2D g2d, int index) throws IOException {
        boolean isWin = battle.getIntValue("gameresult") == 1;
        String date = battle.getString("gametime");
        BufferedImage bar_bg = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, isWin ? "win_bg.png" : "lose_bg.png"));

        Graphics2D bgr = (Graphics2D) bar_bg.getGraphics();
        bgr.setComposite(AlphaComposite.SrcOver);
        String bar_text = isWin ? "胜利" : "失败";

        String[] args = getAllArgs(battle);

        BattleDetailResult battleDetailResult = battleHistory.getBattleDetail(args[0], args[1], args[2], roleId, args[3]);

        ResConfig.Dir avatarDir = resConfig.getDir(ResConfig.Dirs.DIR_AVATAR);
        ResConfig.Dir skillDir = resConfig.getDir(ResConfig.Dirs.DIR_SKILL);
        ResConfig.Dir equipDir = resConfig.getDir(ResConfig.Dirs.DIR_EQUIP);
        Integer heroId = battle.getIntValue("heroId");
        String name_path = heroId + ".png";
        File hero_path = avatarDir.getFile(name_path);
        String icon_path = battle.getString("heroIcon");
        //画详细
        if (battleDetailResult != null && battleDetailResult.getReturnCode() == 0) {
            JSONObject slefData = null;
            List<Object> allrole = new LinkedList<>();
            allrole.addAll(battleDetailResult.getData().getRedRoles());
            allrole.addAll(battleDetailResult.getData().getBlueRoles());
            for (Object role : allrole) {
                JSONObject r01 = (JSONObject) role;
                r01 = r01.getJSONObject("basicInfo");
                if (r01.getBooleanValue("isMe")) {
                    slefData = (JSONObject) role;
                    break;
                }
            }
            JSONObject brc = slefData.getJSONObject("battleRecords");
            JSONObject skin0 = brc.getJSONObject("usedSkin");
            if (skin0 != null) {
                int skin_id = skin0.getIntValue("skinId");
                hero_path = avatarDir.getFile(heroId + "-" + skin_id + ".png");
                icon_path = skin0.getString("skinIcon");
            }
            JSONObject skill = brc.getJSONObject("skill");
            String skill_path = skill.get("skillId") + ".png";
            File skill_file = skillDir.saveIfNotExist(skill.getString("skillIcon"), skill_path);
            BufferedImage skill_img = skill_file != null ? ImageIO.read(skill_file) : null;
            if (skill_img != null) {
                skill_img = BufferedImageUtils.image2size(45, 45, skill_img);
                skill_img = BufferedImageUtils.cropToRoundedCorner(skill_img, 45);
                Graphics bagr = bar_bg.getGraphics();
                bagr.drawImage(skill_img, 390, 65, skill_mask.getWidth(), skill_mask.getHeight(), null);
            }
            JSONArray equips = brc.getJSONArray("finalEquips");
            int eix = 1, eiy = -1;
            for (Object equipo : equips) {
                JSONObject equip = (JSONObject) equipo;
                int equip_id = equip.getIntValue("equipId");
                File equip_file = equipDir.saveIfNotExist(equip.getString("equipIcon"), equip_id + ".png");
                BufferedImage equip_img = equip_file != null ? ImageIO.read(equip_file) : null;
                if (equip_img != null) {
                    equip_img = BufferedImageUtils.image2size(45, 45, equip_img);
                    equip_img = BufferedImageUtils.cropToRoundedCorner(equip_img, 45);
                    bgr.drawImage(equip_img, 392 + eix * 50, 63 + eiy * 50, skill_mask.getWidth(), skill_mask.getHeight(), null);
                }
                eix += 1;
                if (eix >= 4) {
                    eix = 1;
                    eiy += 1;
                }
            }
        }

        File hero_file = avatarDir.saveIfNotExist(icon_path, hero_path.getName());
        BufferedImage hero_img = hero_file != null ? ImageIO.read(hero_file) : null;
        if (hero_img != null) {
            if (hero_img.getHeight() > hero_img.getWidth()) {
                int w = hero_img.getWidth();
                hero_img = hero_img.getSubimage(0, 0, w, w);
            }
            hero_img = BufferedImageUtils.image2size(100, 100, hero_img);
            hero_img = BufferedImageUtils.cropToRoundedCorner(hero_img, 100);
            bgr.drawImage(hero_img, 79, 9, hero_mask.getWidth(), hero_mask.getHeight(), null);
        }
        bgr.drawImage(ring, 78, 8, null);

        Integer kill = battle.getIntValue("killcnt");
        int dead = battle.getIntValue("deadcnt");
        int assist = battle.getIntValue("assistcnt");
        bgr.setColor(isWin ? new Color(66, 183, 255) : new Color(255, 66, 66));
        bgr.setFont(FONT_32);
        bgr.drawString(bar_text, 210, 40);

        bgr.setColor(Color.BLACK);
        bgr.setFont(FONT_18);
        bgr.drawString(battle.getString("mapName"), 281, 40);

        String desc = battle.getString("desc");
        if (Judge.isEmpty(desc)) desc = battle.getString("matchDesc");
        if (Judge.isNotEmpty(desc)) {
            bgr.setColor(new Color(241, 224, 198, 33));
            bgr.fillRect(636, 10, 40, 100);
            bgr.setColor(new Color(100, 35, 0, 56));
            bgr.setStroke(new BasicStroke(2));
            bgr.drawRect(636, 10, 40, 100);

            bgr.setFont(FONT_26);
            bgr.setColor(Color.ORANGE);
            for (int ie0 = 0; ie0 < desc.length(); ie0++) {
                char e0 = desc.charAt(ie0);
                bgr.drawString(String.valueOf(e0), 643, 38 + (ie0 * 30));
            }
        }
        bgr.setFont(FONT_26);
        bgr.setColor(Color.RED);
        String grade = battle.getString("gradeGame");
        if (Judge.isEmpty(grade)) grade = battle.getString("grade");
        bgr.drawString(grade, 690, 82);
        bgr.setFont(FONT_18);
        bgr.setColor(Color.BLACK);
        bgr.drawString("评分:", 690, 42);

        bgr.setColor(Color.BLACK);
        bgr.setFont(FONT_33);
        bgr.drawString(kill + " / " + dead + " / " + assist, 210, 83);
        bgr.setFont(FONT_20);
        bgr.drawString(date, 824, 97);
        String evaluateUrlV3 = battle.getString("evaluateUrlV3");
        String mvpUrlV3 = battle.getString("mvpUrlV3");
        if (Judge.isNotEmpty(evaluateUrlV3)) {
            BufferedImage evaluate_img = ImageIO.read(new URL(evaluateUrlV3));
            bgr.drawImage(evaluate_img, 760, 5,
                    (int) (evaluate_img.getWidth() * 0.7)
                    , (int) (evaluate_img.getHeight() * 0.7), null);
        }
        if (Judge.isNotEmpty(mvpUrlV3)) {
            BufferedImage mvp_img = ImageIO.read(new URL(mvpUrlV3));
            bgr.drawImage(mvp_img, 760, 40,
                    (int) (mvp_img.getWidth() * 0.7),
                    (int) (mvp_img.getHeight() * 0.7), null);
        }

        bgr.dispose();

        g2d.drawImage(bar_bg, 0, 540 + index * 120, null);
    }

    private String[] getAllArgs(JSONObject battle) {
        String battleType = battle.getString("battleType");
        String gameSvr = battle.getString("gameSvrId");
        String relaySvr = battle.getString("relaySvrId");
        String gameSeq = battle.getString("gameSeq");
        if (Judge.isEmpty(gameSvr))
            gameSvr = battle.getString("gamesvrentity");

        if (Judge.isEmpty(relaySvr))
            relaySvr = battle.getString("relaysvrentity");

        if (Judge.isEmpty(gameSeq))
            gameSeq = battle.getString("gameseq");

        return new String[]{battleType, gameSvr, relaySvr, gameSeq};
    }

    /**
     * 预览接口 - 返回文字形式的对战统计信息
     *
     * @param sid 要查询的ID
     * @param opt 选项 排位..巅峰..
     * @param uid 营地ID
     * @return 文字形式的统计信息
     */
    public Object preview(
            String sid,
            String opt,
            String uid
    ) {
        try {
            if (Judge.isEmpty(uid)) {
                if (Judge.isNotEmpty(sid)) uid = bindConfig.getBind(sid);
            }
            if (Judge.isEmpty(uid)) {
                return ResponseEntity.badRequest().body("未绑定UID");
            }

            log.info("start preview battle history: {}", sid);

            Integer optn = null;
            try {
                optn = filterToOpt(opt);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            // 获取用户角色信息
            UserRoleResult userRoleResult = userRoleFuns.getUserRole(uid);
            if (userRoleResult.getReturnCode() != 0) {
                return ResponseEntity.badRequest().body(userRoleResult.getReturnMsg());
            }

            Map<String, Object> rData = userRoleResult.getData().get(0);
            String roleId = rData.get("roleId").toString();

            // 获取对战历史数据
            List<Object> battleList = new LinkedList<>();
            if (optn < 100) {
                BattleResult battleResult = battleHistory.getBattleHistory(uid, optn);
                if (battleResult == null || battleResult.getReturnCode() != 0)
                    return ResponseEntity.badRequest().body(battleResult.getReturnMsg());
                battleList.addAll(battleResult.getData().getList());
            } else {
                Integer serverId = (Integer) rData.get("serverId");
                int lt = Math.toIntExact(System.currentTimeMillis() / 1000);
                //查询单个英雄
                while (true) {
                    BattleOneResult result = battleHistory.getBattleOneHistory(serverId, roleId, optn, lt);
                    DataZjList list = result.getData();
                    battleList.addAll(list.getZjList());
                    if (battleList.size() >= 12) break;
                    if (list.getZjList().size() >= 5) {
                        JSONObject endOne = (JSONObject) battleList.get(battleList.size() - 1);
                        lt = Math.toIntExact(endOne.getLongValue("gameseq"));
                    } else break;
                }
            }

            // 统计对战数据
            StringBuilder result = new StringBuilder();
            result.append("成功查询到你的对战记录\n\n");

            // 统计各种对战类型
            Map<String, Integer> battleTypeCount = new HashMap<>();
            Map<String, Integer> winCount = new HashMap<>();
            // 专门统计排位赛子类型
            Map<String, Integer> rankSubTypeCount = new HashMap<>();
            Map<String, Integer> rankSubTypeWinCount = new HashMap<>();
            int totalWins = 0;
            int mvpCount = 0;
            for (Object battleObj : battleList) {
                JSONObject battle = (JSONObject) battleObj;
                String mapName = battle.getString("mapName");
                String desc = battle.getString("desc");
                if (Judge.isEmpty(desc)) desc = battle.getString("matchDesc");
                String mvpUrlV3 = battle.getString("mvpUrlV3");
                if (Judge.isNotEmpty(mvpUrlV3)) mvpCount++;
                // 确定对战类型
                String battleType = getBattleType(mapName, desc, opt);
                String mainType = getMainBattleType(battleType);

                battleTypeCount.put(mainType, battleTypeCount.getOrDefault(mainType, 0) + 1);

                // 统计胜负
                int gameResult = battle.getIntValue("gameresult");
                if (gameResult == 1) {
                    totalWins++;
                    winCount.put(mainType, winCount.getOrDefault(mainType, 0) + 1);
                }

                // 如果是排位赛，统计子类型
                if (mainType.equals("排位赛")) {
                    if (battleType.startsWith("排位赛 ")) {
                        rankSubTypeCount.put(battleType, rankSubTypeCount.getOrDefault(battleType, 0) + 1);
                        if (gameResult == 1) {
                            rankSubTypeWinCount.put(battleType, rankSubTypeWinCount.getOrDefault(battleType, 0) + 1);
                        }
                    }
                }
            }
            result.append("总体统计:\n");
            result.append(String.format("  总场次: %d局\n", battleList.size()));
            //  MVP 次数
            if (mvpCount > 0) result.append(String.format("  MVP次数: %d次\n", mvpCount));
            if (!battleList.isEmpty()) {
                double overallWinRate = (double) totalWins / battleList.size() * 100;
                result.append(String.format("  总胜率: %.1f%%\n", overallWinRate));
            }
            // 构建统计结果
            result.append("\n对战类型统计:\n");
            for (Map.Entry<String, Integer> entry : battleTypeCount.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                int wins = winCount.getOrDefault(type, 0);
                double winRate = count > 0 ? (double) wins / count * 100 : 0;

                if (type.equals("排位赛") && !rankSubTypeCount.isEmpty()) {
                    // 排位赛特殊处理：显示总局数和子类型详情
                    result.append(String.format("  %s: %d局 (胜率: %.1f%%)\n", type, count, winRate));
                    for (Map.Entry<String, Integer> subEntry : rankSubTypeCount.entrySet()) {
                        String subType = subEntry.getKey();
                        int subCount = subEntry.getValue();
                        int subWins = rankSubTypeWinCount.getOrDefault(subType, 0);
                        double subWinRate = subCount > 0 ? (double) subWins / subCount * 100 : 0;
                        String displayName = subType.replace("排位赛 ", "");
                        result.append(String.format("    %s: %d局 (胜率: %.1f%%)\n", displayName, subCount, subWinRate));
                    }
                } else {
                    // 其他类型正常显示
                    result.append(String.format("  %s: %d局 (胜率: %.1f%%)\n", type, count, winRate));
                }
            }


            log.info("end preview battle list");
            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            log.error("getBattlePreviewError: {}", e.getMessage());
            return ResponseEntity.badRequest().body("查询失败: " + e.getMessage());
        } finally {
        }
    }

    /**
     * 获取对战类型的主要类别
     */
    private String getMainBattleType(String battleType) {
        if (battleType.startsWith("排位赛")) return "排位赛";
        return battleType;
    }

    /**
     * 根据地图名称和描述确定对战类型
     */
    private String getBattleType(String mapName, String desc, String opt) {
        // 根据地图名称判断 - 优先处理具体的排位赛类型
        if (Judge.isNotEmpty(mapName)) {
            if (mapName.contains("排位赛 五排")) return "排位赛 五排";
            else if (mapName.contains("排位赛 三排")) return "排位赛 三排";
            else if (mapName.contains("排位赛 双排")) return "排位赛 双排";
            else if (mapName.contains("排位")) return "排位赛 单排";
            else if (mapName.contains("巅峰")) return "巅峰赛";
            else if (mapName.contains("王者峡谷")) return "匹配赛";
            else if (mapName.contains("无限") || mapName.contains("火焰") || mapName.contains("长平"))
                return "娱乐模式";
        }

        // 根据描述判断
        if (Judge.isNotEmpty(desc)) {
            if (desc.contains("排位赛 五排")) return "排位赛 五排";
            else if (desc.contains("排位赛 三排")) return "排位赛 三排";
            else if (desc.contains("排位赛 双排")) return "排位赛 双排";
            else if (desc.contains("排位")) return "排位赛";
            else if (desc.contains("巅峰")) return "巅峰赛";
            else if (desc.contains("匹配")) return "匹配赛";
            else if (desc.contains("娱乐")) return "娱乐模式";
        }

        // 接口筛选条件可作为缺少地图/描述时的最后回退。
        if (Judge.isNotEmpty(opt)) {
            if (opt.startsWith("排位")) return "排位赛";
            if (opt.startsWith("巅峰")) return "巅峰赛";
            if (opt.startsWith("标准")) return "匹配赛";
            if (opt.startsWith("娱乐")) return "娱乐模式";
        }

        return "其他模式";
    }

    private Integer filterToOpt(String opt) {
        if (Judge.isEmpty(opt)) return 0;
        else if (opt.startsWith("排位")) return 1;
        else if (opt.startsWith("标准")) return 2;
        else if (opt.startsWith("娱乐")) return 3;
        else if (opt.startsWith("巅峰")) return 4;
        Integer hi = -1;
        for (HeroData hero : req.getHeros()) {
            if (hero.getCname().equals(opt)) {
                hi = hero.getEname();
                break;
            }
        }
        Set<String> mabey = new HashSet<>();
        if (hi <= 0) {
            for (int i = 0; i < opt.length(); i++) {
                String c0 = String.valueOf(opt.charAt(i));
                for (HeroData hero : req.getHeros()) {
                    if (hero.getCname().contains(c0))
                        mabey.add(hero.getCname());
                }
            }
            StringBuilder sbtips = new StringBuilder();
            for (String m : mabey) {
                sbtips.append(m).append("\r\n");
            }
            throw new RuntimeException("未找到英雄: " + opt + ", 可能的英雄有: \n" + sbtips.toString().trim());
        }
        return hi;
    }
}
