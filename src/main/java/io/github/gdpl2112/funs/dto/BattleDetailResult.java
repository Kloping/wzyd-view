package io.github.gdpl2112.funs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
public class BattleDetailResult {
    public BattleDetailResult() {
    }

    private Integer result;
    private Integer returnCode = 0;
    private String returnMsg;
    private BattleDetailResultData data;
}
