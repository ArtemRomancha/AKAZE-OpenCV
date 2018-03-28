package tech.romancha.akazeapp.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.Java2DFrameUtils;

import static org.bytedeco.javacpp.opencv_core.Mat;

public final class Utils {
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(Java2DFrameUtils.toBufferedImage(frame), null);
        } catch (Exception e) {
            System.err.println("Cannot convert the Mat obejct: " + e);
        }
        return null;
    }
}