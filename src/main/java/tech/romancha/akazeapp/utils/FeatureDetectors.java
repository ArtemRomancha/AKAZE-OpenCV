package tech.romancha.akazeapp.utils;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_xfeatures2d.SIFT;
import static org.bytedeco.javacpp.opencv_xfeatures2d.SURF;

public class FeatureDetectors {
    public static String method = Methods.AKAZE;
    public static int knnParam = 2;
    public static double goodMathesParam = 0.7;

    public static Mat features(Mat sceneImage, Mat objectImage) {
        KeyPointVector sceneKeyPoints = new KeyPointVector();
        KeyPointVector objectKeyPoints = new KeyPointVector();

        Mat sceneDescriptors = new Mat();
        Mat objectDescriptors = new Mat();

        Mat sceneOutputImage = new Mat();

        Feature2D feature2D;
        switch (method) {
            case (Methods.SURF):
                feature2D = SURF.create();
                break;
            case (Methods.SIFT):
                feature2D = SIFT.create();
                break;
            default:
                feature2D = AKAZE.create();
                break;
        }

        feature2D.detectAndCompute(sceneImage, new Mat(), sceneKeyPoints, sceneDescriptors);

        if (!objectImage.empty()) {
            feature2D.detectAndCompute(objectImage, new Mat(), objectKeyPoints, objectDescriptors);
        } else {
            drawKeypoints(sceneImage, sceneKeyPoints, sceneOutputImage, new Scalar(255, 255, 255, 0), DrawMatchesFlags.DEFAULT);
            return sceneOutputImage;
        }

        DescriptorMatcher matcher = BFMatcher.create();
        DMatchVectorVector matches = new DMatchVectorVector();
        matcher.knnMatch(objectDescriptors, sceneDescriptors, matches, knnParam);

        DMatchVector goodMatches = new DMatchVector();
        for (int i = 0; i < Math.min(sceneDescriptors.rows() - 1, matches.size()); i++) {
            if ((matches.get(i).get(0).distance() < goodMathesParam * (matches.get(i).get(1).distance())) &&
                    (matches.get(i).size() <= 2 && matches.get(i).size() > 0)) {
                goodMatches.push_back(matches.get(i).get(0));
            }
        }

        Mat outputImage = new Mat();
        drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, outputImage,
                Scalar.all(-1), Scalar.all(-1), new byte[0], DrawMatchesFlags.NOT_DRAW_SINGLE_POINTS);

        return outputImage;
    }
}
