package io.github.gdpl2112.funs.dto;

import io.github.gdpl2112.dto.DataList;
import lombok.*;
import lombok.experimental.Accessors;

@Data
public class BattleResult {
    public BattleResult() {
    }

    private Integer returnCode = 0;
    private String returnMsg;
    private DataList data;
}