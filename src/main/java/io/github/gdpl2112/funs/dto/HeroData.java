package io.github.gdpl2112.funs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
public class HeroData {
    private Integer ename;
    private String cname;
    private String id_name;
    private String title;
    private Integer new_type;
    private Integer hero_type;
    private String skin_name;
    private Integer moss_id;

    public HeroData() {
    }
}