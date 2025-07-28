package io.github.gdpl2112.funs;

import com.alibaba.fastjson2.JSON;
import io.github.gdpl2112.HttpApi;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author github kloping
 * @date 2025/7/8-09:44
 */
@Slf4j
@Service
public class HerosReq {

    @Data
    @Accessors(chain = true)
    public static class HeroData {
        private Integer ename;
        private String cname;
        private String id_name;
        private String title;
        private Integer new_type;
        private Integer hero_type;
        private String skin_name;
        private Integer moss_id;
    }

    private List<HeroData> cache;

    public synchronized List<HeroData> getHeros() {
        if (cache != null && !cache.isEmpty()) return cache;
        try {
            Document doc0 = Jsoup.connect(HttpApi.HERO_LIST)
                    .ignoreContentType(true).ignoreHttpErrors(true)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                    .get();
            String json = doc0.body().text();
            return cache = JSON.parseArray(json, HeroData.class);
        } catch (IOException e) {
            log.error("getHerosError", e);
        }
        return null;
    }

    @Scheduled(cron = "0 59 0 * * ?")
    public void updateHeros() {
        if (cache == null) cache.clear();
    }
}
