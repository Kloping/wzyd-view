package io.github.gdpl2112.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CustomProtobufBuilder {
    
    /**
     * 构建Varint编码的字节
     */
    private static byte[] encodeVarint(long value) {
        List<Byte> bytes = new ArrayList<>();
        while (true) {
            if ((value & ~0x7FL) == 0) {
                bytes.add((byte) value);
                break;
            } else {
                bytes.add((byte) (((int) value & 0x7F) | 0x80));
                value >>>= 7;
            }
        }
        
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }
    
    /**
     * 构建字符串字段的编码
     */
    private static byte[] encodeStringField(int fieldNumber, String value) {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] lengthBytes = encodeVarint(stringBytes.length);
        
        // 计算tag: (fieldNumber << 3) | 2 (字符串的线路类型)
        byte tag = (byte) ((fieldNumber << 3) | 2);
        
        byte[] result = new byte[1 + lengthBytes.length + stringBytes.length];
        result[0] = tag;
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(stringBytes, 0, result, 1 + lengthBytes.length, stringBytes.length);
        
        return result;
    }
    
    /**
     * 构建整数字段的编码
     */
    private static byte[] encodeIntField(int fieldNumber, int value) {
        byte[] valueBytes = encodeVarint(value);
        
        // 计算tag: (fieldNumber << 3) | 0 (Varint的线路类型)
        byte tag = (byte) ((fieldNumber << 3) | 0);
        
        byte[] result = new byte[1 + valueBytes.length];
        result[0] = tag;
        System.arraycopy(valueBytes, 0, result, 1, valueBytes.length);
        
        return result;
    }
    
    /**
     * 构建完整的自定义Protobuf消息
     * @param customString 要自定义的字符串（替换原来的"kloping"）
     */
    public static byte[] buildCustomProtobufMessage(String customString) {
        // 按照原始数据的字段顺序构建各字段
        byte[] field1 = encodeIntField(1, 1011);      // 字段1: 1011
        byte[] field2 = encodeStringField(2, customString); // 字段2: 自定义字符串
        byte[] field3 = encodeIntField(3, 1);         // 字段3: 1
        byte[] field4 = encodeIntField(4, 10);         // 字段4: 10
        byte[] field7 = encodeStringField(7, "0");     // 字段7: "0"
        
        // 计算总长度
        int totalLength = field1.length + field2.length + field3.length + field4.length + field7.length;
        byte[] result = new byte[totalLength];
        
        // 合并所有字段
        int position = 0;
        System.arraycopy(field1, 0, result, position, field1.length);
        position += field1.length;
        System.arraycopy(field2, 0, result, position, field2.length);
        position += field2.length;
        System.arraycopy(field3, 0, result, position, field3.length);
        position += field3.length;
        System.arraycopy(field4, 0, result, position, field4.length);
        position += field4.length;
        System.arraycopy(field7, 0, result, position, field7.length);
        
        return result;
    }
    
    /**
     * 将字节数组转换为十六进制字符串（用于验证）
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString().trim();
    }
    
    // 测试示例
    public static void main(String[] args) {
        // 测试1: 使用原始字符串"kloping"
        byte[] originalData = buildCustomProtobufMessage("kloping");
        System.out.println("原始数据重建: " + bytesToHex(originalData));
        
        // 测试2: 使用自定义字符串
        byte[] customData = buildCustomProtobufMessage("hello世界");
        System.out.println("自定义数据: " + bytesToHex(customData));
        
        // 验证与原始数据的对比
        String originalHex = "08 f3 07 12 07 6b 6c 6f 70 69 6e 67 18 01 20 0a 3a 01 30";
        String rebuiltHex = bytesToHex(originalData);
        System.out.println("原始Hex: " + originalHex);
        System.out.println("重建Hex: " + rebuiltHex);
        System.out.println("重建是否一致: " + originalHex.equals(rebuiltHex));
    }
}