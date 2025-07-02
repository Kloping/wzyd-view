package io.github.gdpl2112.utils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class BufferedImageUtils {

    public static BufferedImage image2size(int w, int h, BufferedImage image) {
        BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, w, h, null);
        g2d.dispose();
        return scaledImage;
    }

    public static BufferedImage cropToRoundedCorner(BufferedImage image, int radius) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        // 启用抗锯齿 + 设置圆角裁剪区域
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return output;
    }
}