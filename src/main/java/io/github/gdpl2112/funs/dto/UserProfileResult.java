package io.github.gdpl2112.funs.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
public class UserProfileResult {
    private Integer result;
    private Integer returnCode;
    private String returnMsg;
    private JSONObject data;

    public UserProfileResult() {
    }
}