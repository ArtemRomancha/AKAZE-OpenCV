import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_videostab;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;

import static org.bytedeco.javacpp.opencv_core.KeyPointVector;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_features2d.Feature2D;
import static org.bytedeco.javacpp.opencv_features2d.drawKeypoints;
import static org.bytedeco.javacpp.opencv_features2d.evaluateFeatureDetector;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_xfeatures2d.SURF;
import static org.bytedeco.javacpp.opencv_calib3d.*;

public class Main {
    public static void main(String[] args) {
        String bookObject = "data/1outputImage.jpg";
        String bookScene = "data/outputImage2.jpg";

        System.out.println("Started....");
        System.out.println("Loading images...");
        Mat objectImage = imread(bookObject, CV_LOAD_IMAGE_COLOR);
        Mat sceneImage = imread(bookScene, CV_LOAD_IMAGE_COLOR);

        KeyPointVector objectKeyPoints = new KeyPointVector();
        KeyPointVector sceneKeyPoints = new KeyPointVector();
        Mat objectDescriptors = new Mat();
        Mat sceneDescriptors = new Mat();
        Mat objectOutputImage = new Mat();
        Mat sceneOutputImage = new Mat();

        Feature2D feature2D = AKAZE.create();

        System.out.println("Detecting key points and computing descriptors in object image...");
        feature2D.detectAndCompute(objectImage, new Mat(), objectKeyPoints, objectDescriptors);
        //drawKeypoints(objectImage, objectKeyPoints, objectOutputImage, new Scalar(255, 255, 255, 0), DrawMatchesFlags.DEFAULT);

        System.out.println("Detecting key points and computing descriptors in background image...");
        feature2D.detectAndCompute(sceneImage, new Mat(), sceneKeyPoints, sceneDescriptors);
        //drawKeypoints(sceneImage, sceneKeyPoints, sceneOutputImage, new Scalar(255, 255, 255, 0), DrawMatchesFlags.DEFAULT);

        System.out.println("Matching descriptor vectors using FLANN matcher...");
        DescriptorMatcher matcher = BFMatcher.create();
        DMatchVectorVector matches = new DMatchVectorVector();
        matcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

        DMatchVector goodMatches = new DMatchVector();
        for(int i = 0; i < Math.min(sceneDescriptors.rows() - 1, matches.size()); i++) {
            if((matches.get(i).get(0).distance() < 0.6 * (matches.get(i).get(1).distance())) &&
                    (matches.get(i).size() <= 2 && matches.get(i).size() > 0)) {
                goodMatches.push_back(matches.get(i).get(0));
            }
        }

        Point2fVector objectP2fVector = new Point2fVector();
        Point2fVector sceneP2fVector = new Point2fVector();

        for(int i = 0; i < goodMatches.size(); i++) {
            objectP2fVector.push_back(objectKeyPoints.get(goodMatches.get(i).queryIdx()).pt());
            sceneP2fVector.push_back(sceneKeyPoints.get(goodMatches.get(i).trainIdx()).pt());
        }

        Mat outputImage = new Mat();
        drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, outputImage,
                Scalar.all(-1), Scalar.all(-1), new byte[0], DrawMatchesFlags.DEFAULT);
        imwrite("data/outputImage.jpg", outputImage);

        Mat obj = new Mat(objectP2fVector);
        Mat scn = new Mat(sceneP2fVector);
        Mat out = new Mat();
        //Mat homography = findHomography(obj, scn, out);


        //imwrite("data/sceneOutput.jpg", sceneOutputImage);
        //imwrite("data/objectOutput.jpg", objectOutputImage);

        System.out.println("Ended....");
//        // Create the matrix for outputImage image.
//        Mat objectOutputImage = new Mat(objectImage.rows(), objectImage.cols(), CV_LOAD_IMAGE_COLOR);
//
//        System.out.println("Drawing key points on object image...");
//        drawKeypoints(objectImage, objectKeyPoints, objectOutputImage, new Scalar(255, 255, 255, 0), DrawMatchesFlags.DEFAULT);
    }
}
