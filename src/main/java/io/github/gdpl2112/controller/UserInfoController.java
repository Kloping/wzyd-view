package io.github.gdpl2112.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.WzryDpApplication;
import io.github.gdpl2112.config.BindConfig;
import io.github.gdpl2112.config.ResConfig;
import io.github.gdpl2112.funs.UserProfile;
import io.github.gdpl2112.funs.dto.UserProfileResult;
import io.github.gdpl2112.funs.dto.UserRoleResult;
import io.github.gdpl2112.utils.BufferedImageUtils;
import io.github.gdpl2112.utils.NameUtils;
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
import java.util.Map;

/**
 * @author github kloping
 * @date 2025/7/1-14:16
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserInfoController {
    public static final String FONT_STYLE = "黑体";
    public static final Font FONT_48 = new Font(FONT_STYLE, Font.PLAIN, 48);
    public static final Font FONT_46 = new Font(FONT_STYLE, Font.PLAIN, 46);
    public static final Font FONT_40 = new Font(FONT_STYLE, Font.PLAIN, 40);
    public static final Font FONT_39 = new Font(FONT_STYLE, Font.PLAIN, 39);
    public static final Font FONT_36 = new Font(FONT_STYLE, Font.PLAIN, 36);
    public static final Font FONT_30 = new Font(FONT_STYLE, Font.PLAIN, 30);
    private static final String INFO_PATH = "info";
    @Autowired
    ResConfig resConfig;
    @Autowired
    BindConfig bindConfig;
    @Autowired
    UserProfile userProfile;

    @RequestMapping("/")
    public ResponseEntity<String> getUserInfo(
            @RequestParam(name = "sid", required = false, defaultValue = "") String sid
            , @RequestParam(name = "uid", required = false, defaultValue = "") String uid
            , HttpServletResponse response
    ) {
        WzryDpApplication.LOCK.lock();
        try {
            if (Judge.isEmpty(uid)) {
                if (Judge.isNotEmpty(sid)) uid = bindConfig.getBind(sid);
            }
            if (Judge.isEmpty(uid)) {
                return ResponseEntity.badRequest().body("未绑定UID");
            }

            UserRoleResult userRoleResult = userProfile.getUserRole(uid);
            if (userRoleResult.getReturnCode() < 0)
                return ResponseEntity.badRequest().body(userRoleResult.getReturnMsg());
            Map<String, Object> rData = userRoleResult.getData().get(0);

            String roleId = (String) rData.get("roleId");
            UserProfileResult profileData = userProfile.getUserProfile(uid, roleId);
            if (profileData.getReturnCode() != 0)
                return ResponseEntity.badRequest().body(profileData.getReturnMsg());
            UserProfileResult profileIndexData = userProfile.getUserProfileIndex(uid, roleId);
            if (profileIndexData.getReturnCode() != 0)
                return ResponseEntity.badRequest().body(profileIndexData.getReturnMsg());

            try {
                JSONObject roleCard = profileData.getData().getJSONObject("roleCard");

                ResConfig.Dir iconDir = resConfig.getDir(ResConfig.Dirs.DIR_ICON);
                int maxw = 950, maxh = 2150;

                BufferedImage bg = ImageIO.read(resConfig.getResourceBytes("bg"));
                bg = BufferedImageUtils.image2size(maxw, maxh, bg);
                String avatar_url = roleCard.getString("roleBigIcon");
                BufferedImage avatar_img = ImageIO.read(new URL(avatar_url));
                avatar_img = BufferedImageUtils.image2size(320, 320, avatar_img);
                avatar_img = BufferedImageUtils.cropToRoundedCorner(avatar_img, 320);
                Graphics2D g2d = (Graphics2D) bg.getGraphics();
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.drawImage(avatar_img, 110, 70, null);
                BufferedImage title = ImageIO.read(resConfig.getResourceBytes(INFO_PATH, "title.png"));
                g2d.drawImage(title, -200, 405, title.getWidth(), title.getHeight(), null);

                g2d.setColor(new Color(10, 10, 10));
                g2d.setFont(FONT_40);
                String tw = (String) rData.get("roleDesc");
                g2d.drawString(tw, 275 - (g2d.getFontMetrics().stringWidth(tw) / 2), 437);
                g2d.setColor(new Color(48, 48, 48));
                g2d.setFont(FONT_30);
                tw = (String) rData.get("roleName");
                g2d.drawString(tw, 275 - (g2d.getFontMetrics().stringWidth(tw) / 2), 495);
                //段位
                int x = 596, y = -80;

                String flag_url = roleCard.getString("flagImg");
                File file = iconDir.saveIfNotExist(flag_url, NameUtils.getNameByUrl(flag_url));
                BufferedImage flag_img = ImageIO.read(file);
                g2d.drawImage(flag_img, x, y, null);


                String role_job_url = roleCard.getString("roleJobIcon");
                File role_job_file = iconDir.saveIfNotExist(role_job_url, NameUtils.getNameByUrl(role_job_url));
                BufferedImage role_job_img = ImageIO.read(role_job_file);
                g2d.drawImage(role_job_img, 539, y + 150, 450, 450, null);


                String star_img_url = roleCard.getString("starImg");
                if (Judge.isNotEmpty(star_img_url)) {
                    File star_img_file = iconDir.saveIfNotExist(star_img_url, NameUtils.getNameByUrl(star_img_url));
                    BufferedImage star_img = ImageIO.read(star_img_file);
                    g2d.drawImage(star_img, x + 55, y + 125, (int) (188 * 1.2), (int) (68 * 1.2), null);
                }

                y = 445;

                JSONObject headData = profileIndexData.getData().getJSONObject("head");
                JSONArray profile_mods = headData.getJSONArray("mods");

                BufferedImage lstar_img = ImageIO.read(resConfig.getResourceBytes(INFO_PATH, "star.png"));
                g2d.drawImage(lstar_img, x + 120, y + 15, null);

                g2d.setColor(new Color(224, 195, 100));
                g2d.setFont(FONT_40);
                g2d.drawString("x" + roleCard.getIntValue("rankingStar"), x + 165, y + 50);

                JSONObject pinnacleData = profile_mods.getJSONObject(2);
                y = 340;
                String pinnacle_job_url = pinnacleData.getString("icon");
                File pinnacle_job_file = iconDir.saveIfNotExist(pinnacle_job_url, NameUtils.getNameByUrl(pinnacle_job_url));
                BufferedImage pinnacle_job_img = ImageIO.read(pinnacle_job_file);
                g2d.drawImage(pinnacle_job_img, x - 65, y + 120, 460, 460, null);
                g2d.setColor(new Color(224, 195, 100));
                g2d.setFont(FONT_36);
                tw = pinnacleData.getString("content");
                g2d.drawString(tw, x + 163 - g2d.getFontMetrics().stringWidth(tw) / 2, y + 320);

                // # 总体资料
                int index0 = 3;
                String battle_score = profile_mods.getJSONObject(index0++).getString("content");
                String MVP = profile_mods.getJSONObject(index0++).getString("content");
                String all_battle_num = profile_mods.getJSONObject(index0++).getString("content");
                String hero_num = profile_mods.getJSONObject(index0++).getString("content");
                String win_rate = profile_mods.getJSONObject(index0++).getString("content");
                String skin_num = profile_mods.getJSONObject(index0++).getString("content");
                BufferedImage base_info_bg = ImageIO.read(resConfig.getResourceBytes(INFO_PATH, "base_info.png"));
                int xoff = 174, yoff = 132;
                x = 122;
                y = 641;
                g2d.setColor(Color.BLACK);
                g2d.setFont(FONT_39);
                g2d.drawImage(base_info_bg, 0, 508, null);
                g2d.drawString(battle_score, x - g2d.getFontMetrics().stringWidth(battle_score) / 2, y);
                g2d.drawString(MVP, x + xoff - g2d.getFontMetrics().stringWidth(MVP) / 2, y);
                g2d.drawString(all_battle_num, x + xoff * 2 - g2d.getFontMetrics().stringWidth(all_battle_num) / 2, y);
                //line2
                g2d.drawString(hero_num, x - g2d.getFontMetrics().stringWidth(hero_num) / 2, y + yoff);
                g2d.drawString(skin_num, x + xoff - g2d.getFontMetrics().stringWidth(win_rate) / 2, y + yoff);
                g2d.drawString(win_rate, x + xoff * 2 - g2d.getFontMetrics().stringWidth(skin_num) / 2, y + yoff);
                //over
                UserProfileResult herosData = userProfile.getUserProfileHeroList(uid, roleId);
                x = 30;
                y = 900;
                if (herosData != null && herosData.getReturnCode() < 0) {
                    g2d.setColor(new Color(224, 200, 160));
                    g2d.setFont(FONT_36);
                    tw = "常用英雄获取失败,请前往王者营地开启陌生人可见";
                    g2d.drawString(tw, 450 - g2d.getFontMetrics().stringWidth(tw) / 2, 1400);
                    tw = herosData.getReturnMsg();
                    g2d.drawString(tw, 450 - g2d.getFontMetrics().stringWidth(tw) / 2, 1500);
                } else {
                    JSONArray heroList = herosData.getData().getJSONArray("heroList");
                    BufferedImage char_mask = ImageIO.read(resConfig.getResourceBytes(INFO_PATH, "char_mask.png"));
                    for (int i = 0; i < heroList.size(); i++) {
                        JSONObject hero = heroList.getJSONObject(i);
                        BufferedImage char_img = ImageIO.read(resConfig.getResourceBytes(INFO_PATH, "char_bg.png"));
                        JSONObject basicInfo = hero.getJSONObject("basicInfo");
                        Graphics2D char2d = char_img.createGraphics();
                        Integer heroId = basicInfo.getIntValue("heroId");
                        String hero_img_url = String.format(
                                "https://game-1255653016.file.myqcloud.com/battle_skin_1250-326/%s00.jpg?imageMogr2/thumbnail/x170/crop/270x170/gravity/east"
                                , heroId);
                        File hero_img_file = iconDir.saveIfNotExist(hero_img_url, "profile-" + heroId + ".png");
                        BufferedImage hero_img = ImageIO.read(hero_img_file);
                        char2d.drawImage(hero_img, 57, 35, char_mask.getWidth(), char_mask.getHeight(), null);

                        String char_name = basicInfo.getString("title");
                        char2d.setColor(Color.BLACK);
                        char2d.setFont(FONT_48);
                        char2d.drawString(char_name, 395, 72);

                        String playNum = basicInfo.get("playNum").toString();
                        char2d.setFont(FONT_46);
                        char2d.drawString(playNum, 458 - char2d.getFontMetrics().stringWidth(playNum) / 2, 144);

                        String winRate = basicInfo.get("winRate").toString();
                        char2d.drawString(winRate, 632 - char2d.getFontMetrics().stringWidth(winRate) / 2, 144);

                        Integer heroFightPower = basicInfo.getIntValue("heroFightPower");
                        char2d.setColor(getFightColor(heroFightPower));
                        char2d.drawString(String.valueOf(heroFightPower),
                                806 - char2d.getFontMetrics().stringWidth(String.valueOf(heroFightPower)) / 2, 144);

                        JSONObject honor = hero.getJSONObject("honorTitle");
                        if (honor != null) {
                            int honorType = honor.getIntValue("type");
                            String honor_bg_img_url = getHonorBgImgUrl(honorType);
                            File honor_bg_img_file = iconDir.saveIfNotExist(honor_bg_img_url, "honor-bg-" + honorType + ".png");
                            BufferedImage honor_bg_img = ImageIO.read(honor_bg_img_file);
                            char2d.drawImage(honor_bg_img, 260, 35, 64, 64, null);

                            String honor_img_url = getHonorImgUrl(honorType);
                            File honor_img_file = iconDir.saveIfNotExist(honor_img_url, "honor-" + honorType + ".png");
                            BufferedImage honor_img = ImageIO.read(honor_img_file);
                            char2d.drawImage(honor_img, 260, 31, 64, 52, null);

                            tw = honor.getJSONObject("desc").getString("abbr");
                            int tww = char2d.getFontMetrics().stringWidth(tw);
                            int nwx = 955 - tww;

                            char2d.setFont(FONT_36);
                            char2d.setColor(new Color(255, 255, 255, 64));
                            char2d.fillRoundRect(nwx - 15, 30, tww - 30, 60, 20, 20);

                            char2d.setFont(FONT_40);
                            char2d.setColor(getFightColor(heroFightPower));
                            char2d.drawString(tw, nwx - 10, 75);
                        }
                        char2d.dispose();
                        g2d.drawImage(char_img, 0, 880 + i * 250, null);
                    }
                }

                g2d.dispose();

                response.setContentType("image/png");
                ImageIO.write(bg, "png", response.getOutputStream());
                bg.flush();
            } catch (IOException e) {
                log.error("getUserProfileError: {}", e.getMessage());
            }
            return null;
        } finally {
            WzryDpApplication.LOCK.unlock();
        }
    }

    private Color getFightColor(int power) {
        if (power <= 2500) {
            return new Color(47, 47, 47);
        } else if (power <= 5000) {
            return new Color(69, 110, 232);
        } else if (power <= 7500) {
            return new Color(91, 0, 227);
        } else if (power <= 10000) {
            return new Color(252, 223, 119);
        }
        return new Color(255, 54, 108);
    }

    private String getHonorImgUrl(int t) {
        if (t == 1) {
            return "https://camp.qq.com/battle/home_v2/icon_honor_county.png";
        } else if (t == 2) {
            return "https://camp.qq.com/battle/home_v2/icon_honor_city.png";
        } else if (t == 3) {
            return "https://camp.qq.com/battle/home_v2/icon_honor_province.png";
        }
        return "https://camp.qq.com/battle/home_v2/icon_honor_contry.png";
    }

    private String getHonorBgImgUrl(int t) {
        return getHonorImgUrl(t).replace("/icon", "/bg");
    }
}
