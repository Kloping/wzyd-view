package io.github.gdpl2112.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.config.BindConfig;
import io.github.gdpl2112.config.ResConfig;
import io.github.gdpl2112.dto.DataZjList;
import io.github.gdpl2112.funs.BattleHistory;
import io.github.gdpl2112.funs.HerosReq;
import io.github.gdpl2112.funs.UserProfile;
import io.github.gdpl2112.utils.BufferedImageUtils;
import io.github.kloping.judge.Judge;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author github kloping
 * @date 2025/7/1-09:48
 */
@Slf4j
@RestController
@RequestMapping("/battle")
public class BattleController {
    public BattleController() {
        ImageIO.setUseCache(false);
    }

    @Autowired
    ResConfig resConfig;

    @Autowired
    BindConfig bindConfig;

    @Autowired
    BattleHistory battleHistory;

    @Autowired
    UserProfile userRoleFuns;

    public static final String BATTLE_PATH = "battle";

    /**
     * @param sid 要查询的ID
     * @param opt 选项 排位..巅峰..
     * @return
     */
    @RequestMapping("/history")
    public synchronized Object history(
            @RequestParam(name = "sid") String sid,
            @RequestParam(name = "opt", required = false, defaultValue = "") String opt,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid,
            HttpServletResponse response
    ) {
        try {
            if (Judge.isEmpty(uid)) {
                uid = bindConfig.getBind(sid);
            }
            if (Judge.isEmpty(uid)) {
                return ResponseEntity.badRequest().body("未绑定UID");
            }
            log.info("start select battle history: {}", sid);
            Integer optn = filterToOpt(opt);

            UserProfile.UserRoleResult userRoleResult = userRoleFuns.getUserRole(uid);
            if (userRoleResult.getReturnCode() != 0) {
                return ResponseEntity.badRequest().body(userRoleResult.getReturnMsg());
            }
            JSONObject rData = userRoleResult.getData().get(0);
            String roleId = rData.getString("roleId");
            List<Object> battleList = new LinkedList<>();
            if (optn < 100) {
                BattleHistory.BattleResult battleResult = battleHistory.getBattleHistory(uid, optn);
                if (battleResult == null || battleResult.getReturnCode() != 0)
                    return ResponseEntity.badRequest().body(battleResult.getReturnMsg());
                battleList.addAll(battleResult.getData().getList());
            } else {
                Integer serverId = rData.getIntValue("serverId");
                int lt = Math.toIntExact(System.currentTimeMillis() / 1000);
                //查询单个英雄
                while (true) {
                    BattleHistory.BattleOneResult result = battleHistory.getBattleOneHistory(serverId, roleId, optn, lt);
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
            int online = rData.getIntValue("gameOnline");
            if (online > 0) {
                game_text = "游戏在线";
            } else {
                int appOnline = rData.getIntValue("appOnline");
                if (appOnline == 0) game_text = "离线";
                else game_text = "营地在线";
            }

            BufferedImage bg = ImageIO.read(resConfig.getResourceBytes("bg"));
            int h = 2000;
            if (battleList.size() < 12) {
                h = 500 + battleList.size() * 120 + 60;
            }

            bg = BufferedImageUtils.image2size(950, h, bg);

            String roleIcon = rData.getString("roleIcon");
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
        }
        return null;
    }

    public void drawOneBattle(JSONObject battle, String roleId, BufferedImage skill_mask, BufferedImage hero_mask, BufferedImage ring, Graphics2D g2d, int index) throws IOException {
        boolean isWin = battle.getIntValue("gameresult") == 1;
        String date = battle.getString("gametime");
        BufferedImage bar_bg = ImageIO.read(resConfig.getResourceBytes(BATTLE_PATH, isWin ? "win_bg.png" : "lose_bg.png"));

        Graphics2D bgr = (Graphics2D) bar_bg.getGraphics();
        bgr.setComposite(AlphaComposite.SrcOver);
        String bar_text = isWin ? "胜利" : "失败";

        String[] args = getAllArgs(battle);

        BattleHistory.BattleDetailResult battleDetailResult = battleHistory.getBattleDetail(args[0], args[1], args[2], roleId, args[3]);

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
            BufferedImage skill_img = ImageIO.read(skill_file);
            skill_img = BufferedImageUtils.image2size(45, 45, skill_img);
            skill_img = BufferedImageUtils.cropToRoundedCorner(skill_img, 45);
            Graphics bagr = bar_bg.getGraphics();
            bagr.drawImage(skill_img, 390, 65, skill_mask.getWidth(), skill_mask.getHeight(), null);
            JSONArray equips = brc.getJSONArray("finalEquips");
            int eix = 1, eiy = -1;
            for (Object equipo : equips) {
                JSONObject equip = (JSONObject) equipo;
                int equip_id = equip.getIntValue("equipId");
                File equip_file = equipDir.saveIfNotExist(equip.getString("equipIcon"), equip_id + ".png");
                BufferedImage equip_img = ImageIO.read(equip_file);
                equip_img = BufferedImageUtils.image2size(45, 45, equip_img);
                equip_img = BufferedImageUtils.cropToRoundedCorner(equip_img, 45);
                bgr.drawImage(equip_img, 392 + eix * 50, 63 + eiy * 50, skill_mask.getWidth(), skill_mask.getHeight(), null);
                eix += 1;
                if (eix >= 4) {
                    eix = 1;
                    eiy += 1;
                }
            }
        }

        hero_path = avatarDir.saveIfNotExist(icon_path, hero_path.getName());
        BufferedImage hero_img = ImageIO.read(hero_path);
        if (hero_img.getHeight() > hero_img.getWidth()) {
            int w = hero_img.getWidth();
            hero_img = hero_img.getSubimage(0, 0, w, w);
        }
        hero_img = BufferedImageUtils.image2size(100, 100, hero_img);
        hero_img = BufferedImageUtils.cropToRoundedCorner(hero_img, 100);
        bgr.drawImage(hero_img, 79, 9, hero_mask.getWidth(), hero_mask.getHeight(), null);
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

    @Autowired
    HerosReq req;

    private Integer filterToOpt(String opt) {
        if (Judge.isEmpty(opt)) return 0;
        else if (opt.startsWith("排位")) return 1;
        else if (opt.startsWith("标准")) return 2;
        else if (opt.startsWith("娱乐")) return 3;
        else if (opt.startsWith("巅峰")) return 4;
        for (HerosReq.HeroData hero : req.getHeros()) {
            if (hero.getCname().equals(opt)) return hero.getEname();
        }
        return 0;
    }

    public static final String FONT_STYLE = "微软雅黑";

    public static final Font FONT_40 = new Font(FONT_STYLE, Font.PLAIN, 40);
    public static final Font FONT_33 = new Font(FONT_STYLE, Font.PLAIN, 33);
    public static final Font FONT_32 = new Font(FONT_STYLE, Font.BOLD, 32);
    public static final Font FONT_30 = new Font(FONT_STYLE, Font.PLAIN, 30);
    public static final Font FONT_18 = new Font(FONT_STYLE, Font.PLAIN, 20);
    public static final Font FONT_20 = new Font(FONT_STYLE, Font.PLAIN, 18);
    public static final Font FONT_26 = new Font(FONT_STYLE, Font.PLAIN, 26);
}
