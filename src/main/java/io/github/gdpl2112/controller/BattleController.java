package io.github.gdpl2112.controller;

import io.github.gdpl2112.service.BattleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Battle HTTP adapter. Business and rendering logic lives in {@link BattleService}.
 */
@RestController
@RequestMapping("/battle")
public class BattleController {
    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @RequestMapping("/history")
    public Object history(
            @RequestParam(name = "sid", required = false, defaultValue = "") String sid,
            @RequestParam(name = "opt", required = false, defaultValue = "") String opt,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid,
            HttpServletResponse response) {
        return battleService.history(sid, opt, uid, response);
    }

    @RequestMapping(value = "/history/text", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> historyText(
            @RequestParam(name = "sid", required = false, defaultValue = "") String sid,
            @RequestParam(name = "opt", required = false, defaultValue = "") String opt,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid) {
        return battleService.historyText(sid, opt, uid);
    }

    @RequestMapping("/preview")
    public Object preview(
            @RequestParam(name = "sid", required = false, defaultValue = "") String sid,
            @RequestParam(name = "opt", required = false, defaultValue = "") String opt,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid) {
        return battleService.preview(sid, opt, uid);
    }
}
