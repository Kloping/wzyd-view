package io.github.gdpl2112.funs.dto;

import io.github.gdpl2112.dto.DataZjList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
public class BattleOneResult {
    public BattleOneResult() {
    }

    private Integer returnCode = 0;
    private String returnMsg;
    private DataZjList data;
}
