package io.github.gdpl2112.funs.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
public class UserRoleResult {
    private Integer result;
    private String returnMsg;
    private Integer returnCode;
    private String time;
    private List<Map<String, Object>> data;

    public UserRoleResult() {
    }
}
