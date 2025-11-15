package io.github.gdpl2112.config;

import io.github.kloping.rand.RandomUtils;
import io.github.kloping.url.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author github kloping
 * @date 2025/6/30-21:52
 */
@Slf4j
@Component
public class ResConfig {
    @Value("${data.path}")
    public String PF = "./data";
    @Value("${data.res}")
    private String res;

    public Dir getDir(Dirs dir) {
        return new Dir(PF + "/" + dir.dir);
    }

    //从类路径中获取 随机的文件
    public File getResourceBytes(String path) {
        File dir = new File(res, path);
        int r = dir.listFiles().length;
        return dir.listFiles()[RandomUtils.RANDOM.nextInt(r)];
    }

    /**
     * 从类路径中加载
     *
     * @param path
     * @param name
     * @return
     */
    public File getResourceBytes(String path, String name) {
        File dir = new File(res, path);
        File file = new File(dir, name);
        return file;
    }

    public enum Dirs {
        DIR_AVATAR("avatar"),
        DIR_EQUIP("equip"),
        DIR_ICON("icon"),
        DIR_SKILL("skill"),
        ;

        public final String dir;

        Dirs(String dir) {
            this.dir = dir;
        }
    }

    public record Dir(String path) {
        public File getFile(String name) {
            return new File(path + "/" + name);
        }

        public File saveIfNotExist(String url, String name) {
            File file = getFile(name);
            if (!file.exists()) file.getParentFile().mkdirs();
            if (file.exists()) return file;
            log.info("downloading {} to {}", url, file.getAbsolutePath());
            byte[] bytes = UrlUtils.getBytesFromHttpUrl(url);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("downloaded {} to {}", url, file.getAbsolutePath());
            return file;
        }
    }
}
