package io.github.gdpl2112.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.gdpl2112.config.BindConfig;
import io.github.gdpl2112.funs.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author github kloping
 * @date 2025/7/1-10:21
 */
@RestController
@RequestMapping("/bind")
public class BindController {

    @Autowired
    BindConfig bindConfig;

    @RequestMapping("/")
    public ResponseEntity<String> bind(@RequestParam(name = "sid") String sid
            , @RequestParam(name = "uid") String uid) {
        bindConfig.bind(sid, uid);
        return ResponseEntity.ok("绑定成功");
    }

    @RequestMapping("/un")
    public ResponseEntity<String> unbind(@RequestParam(name = "sid") String sid
            , @RequestParam(name = "uid") String uid) {
        bindConfig.unbind(sid, uid);
        return ResponseEntity.ok("解除绑定uid成功");
    }

    //切换
    @RequestMapping("/switch")
    public ResponseEntity<String> switchBind(@RequestParam(name = "sid") String sid
            , @RequestParam(name = "uid", required = false) String uid) {
        String msg = bindConfig.switchto(sid, uid);
        return ResponseEntity.ok(msg);
    }

    @Autowired
    UserProfile userProfile;

    private static final SimpleDateFormat SF_0 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @RequestMapping("/get")
    public ResponseEntity<String> get(@RequestParam(name = "sid") String sid) {
        StringBuilder sb = new StringBuilder("已绑定UID:");
        List<String> uids = bindConfig.getBinds(sid);
        if (uids != null) {
            for (String uid : uids) {
                sb.append("\n--------\n").append(uid).append("->");
                UserProfile.UserRoleResult userRoleResult = userProfile.getUserRole(uid);
                JSONObject data = userRoleResult.getData().get(0);
                JSONArray arr = data.getJSONArray("roleText");
                sb.append(arr.get(0)).append("\n");
                sb.append(arr.get(1)).append("->").append(data.getString("roleName")).append(" ");
                sb.append(arr.get(2));
                sb.append("\n当前游戏: ").append(data.getIntValue("gameOnline") > 0 ? "\uD83D\uDFE2在线" : "⚪离线");
                long lu = data.getLongValue("lu") * 1000;
                sb.append("\n营地最近在线: ").append(SF_0.format(new Date(lu)));
                long z = data.getLongValue("z") * 1000;
                sb.append("\n游戏最近在线: ").append(SF_0.format(new Date(z)));
            }
            return ResponseEntity.ok(sb.toString().trim());
        } else return ResponseEntity.ok("未绑定UID");
    }

    @RequestMapping("/reload")
    public ResponseEntity<String> reload() {
        bindConfig.reload();
        return ResponseEntity.ok("重载成功");
    }
}
