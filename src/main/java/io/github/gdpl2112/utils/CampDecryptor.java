package io.github.gdpl2112.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

/**
 * 王者营地 HTTP 响应解密器
 * 算法: XXTEA | 密钥: 16字节 来自 DataProvider.getNetworkSymmetricKey()
 * 用法: String json = CampDecryptor.decryptResponse(base64Body, "key");
 */
public class CampDecryptor {

    private static final long DELTA = 0x9E3779B9L;

    /**
     * 解密 campencrypt 响应体，返回 JSON 字符串
     */
    public static String decryptResponse(String base64Body, String key) {
        byte[] encrypted = Base64.getDecoder().decode(base64Body.trim());
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = xxteaDecrypt(encrypted, keyBytes);
        if (decrypted == null) return null;
        byte[] result = tryDecompress(decrypted);
        int end = result.length;
        while (end > 0 && result[end - 1] == 0) end--;
        return new String(result, 0, end, StandardCharsets.UTF_8);
    }

    /**
     * XXTEA 解密 (标准 btea 实现，匹配 libcppservice.so)
     */
    public static byte[] xxteaDecrypt(byte[] data, byte[] key) {
        if (data.length < 8) return null;
        key = padKey(key);

        int n = data.length / 4;
        long[] v = bytesToUint32Array(data, n);
        long[] k = bytesToUint32Array(key, 4);

        int rounds = 6 + 52 / n;
        long sum = ((rounds & 0xFFFFFFFFL) * DELTA) & 0xFFFFFFFFL;

        long y = v[0];
        while (rounds-- > 0) {
            long e = (sum >>> 2) & 3;
            for (int p = n - 1; p > 0; p--) {
                long z = v[p - 1];
                long mx = mx(z, y, sum, k[(int) ((p & 3) ^ e)]);
                y = v[p] = (v[p] - mx) & 0xFFFFFFFFL;
            }
            long z = v[n - 1];
            long mx = mx(z, y, sum, k[(int) e]);
            y = v[0] = (v[0] - mx) & 0xFFFFFFFFL;
            sum = (sum - DELTA) & 0xFFFFFFFFL;
        }

        byte[] result = uint32ArrayToBytes(v, n);

        long originalLength = v[n - 1];
        long totalBytes = (long) n * 4;
        if (originalLength > 0 && originalLength <= totalBytes - 4 && originalLength >= totalBytes - 7) {
            return Arrays.copyOf(result, (int) originalLength);
        }
        return result;
    }

    private static long mx(long z, long y, long sum, long ke) {
        return (((z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4)) ^ ((sum ^ y) + (ke ^ z))) & 0xFFFFFFFFL;
    }

    private static byte[] padKey(byte[] key) {
        if (key.length >= 16) return Arrays.copyOf(key, 16);
        byte[] padded = new byte[16];
        System.arraycopy(key, 0, padded, 0, key.length);
        return padded;
    }

    private static long[] bytesToUint32Array(byte[] data, int count) {
        long[] arr = new long[count];
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) {
            arr[i] = buf.getInt() & 0xFFFFFFFFL;
        }
        return arr;
    }

    private static byte[] uint32ArrayToBytes(long[] arr, int count) {
        ByteBuffer buf = ByteBuffer.allocate(count * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) {
            buf.putInt((int) arr[i]);
        }
        return buf.array();
    }

    private static byte[] tryDecompress(byte[] data) {
        if (data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B) {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = gis.read(buffer)) != -1) bos.write(buffer, 0, len);
                return bos.toByteArray();
            } catch (Exception ignored) {
            }
        }
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (!inflater.finished()) {
                int len = inflater.inflate(buffer);
                if (len == 0) break;
                bos.write(buffer, 0, len);
            }
            inflater.end();
            if (bos.size() > 0) return bos.toByteArray();
        } catch (Exception ignored) {
        }
        return data;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CampDecryptor <key> <base64_body>");
            return;
        }
        String json = decryptResponse(args[1], args[0]);
        if (json != null) System.out.println(json);
        else System.out.println("Decryption failed");
    }
}