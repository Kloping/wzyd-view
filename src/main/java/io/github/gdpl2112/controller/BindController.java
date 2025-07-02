package io.github.gdpl2112.controller;

import io.github.gdpl2112.config.BindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/get")
    public ResponseEntity<String> get(@RequestParam(name = "sid") String sid) {
        StringBuilder sb = new StringBuilder("已绑定UID:\n");
        List<String> uids = bindConfig.getBinds(sid);
        if (uids != null) {
            for (String uid : uids) {
                sb.append(uid).append("\n");
            }
            return ResponseEntity.ok(sb.toString());
        } else return ResponseEntity.ok("未绑定UID");
    }

    @RequestMapping("/reload")
    public ResponseEntity<String> reload() {
        bindConfig.reload();
        return ResponseEntity.ok("重载成功");
    }
}
