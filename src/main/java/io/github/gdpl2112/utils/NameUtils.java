package io.github.gdpl2112.utils;

/**
 * @author github kloping
 * @date 2025/7/3-11:27
 */
public class NameUtils {
    public static String getNameByUrl(String url) {
        int index = url.length() - 1;
        int i = 1;
        while (index > 0) {
            index--;
            if (url.charAt(index) == '/') {
                if (i == 2) {
                    break;
                } else i++;
            }
        }
        String endName = url.substring(index + 1);
        endName = endName.replace("/", ".");
        return endName;
    }
}
