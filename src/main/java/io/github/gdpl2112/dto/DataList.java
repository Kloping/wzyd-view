package io.github.gdpl2112.dto;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author github kloping
 * @date 2025/6/30-22:48
 */
@Data
@Accessors(chain = true)
public class DataList {
    private JSONArray list;
}
