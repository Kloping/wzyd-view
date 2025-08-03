package io.github.gdpl2112.funs.dto;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
public class BattleDetailResultData {
    public BattleDetailResultData() {
    }

    private JSONObject head;
    private JSONObject battle;
    private JSONObject redTeam;
    private JSONObject blueTeam;
    private JSONArray redRoles;
    private JSONArray blueRoles;
    private String jumpUrl;
}