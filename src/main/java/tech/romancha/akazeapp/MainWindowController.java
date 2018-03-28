package tech.romancha.akazeapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tech.romancha.akazeapp.utils.FeatureDetectors;
import tech.romancha.akazeapp.utils.Methods;
import tech.romancha.akazeapp.utils.Utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_videoio.VideoCapture;


public class MainWindowController {

    @FXML
    private Button startCameraBT;

    @FXML
    private Button shotCameraBT;

    @FXML
    private ImageView cameraImageView;

    @FXML
    private ComboBox<String> featureMethod;

    @FXML
    private Spinner<Integer> knnParam;

    @FXML
    private Spinner<Double> goodMatchParam;

    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;
    private int fps = 30;
    private Mat shot = new Mat();
    private volatile boolean shoting = false;

    @FXML
    public void initialize() {
        featureMethod.getItems().addAll(Methods.AKAZE, Methods.SURF, Methods.SIFT);
        featureMethod.getSelectionModel().selectFirst();
        featureMethod.setOnAction((e) -> FeatureDetectors.method = featureMethod.getValue());

        SpinnerValueFactory<Integer> knnValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 2);

        knnParam.setValueFactory(knnValueFactory);
        knnParam.valueProperty().addListener((e) -> FeatureDetectors.knnParam = knnParam.getValue());

        SpinnerValueFactory<Double> goodMatchValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.7, 0.1);

        goodMatchParam.setValueFactory(goodMatchValueFactory);
        goodMatchParam.valueProperty().addListener((e) -> FeatureDetectors.goodMathesParam = goodMatchParam.getValue());
    }

    @FXML
    protected void cameraSwitch(ActionEvent event) {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                Runnable frameGrabber = () -> {
                    Mat frame = grabFrame();
                    if (shoting) {
                        shot = frame.clone();
                        shoting = false;
                    }
                    frame = FeatureDetectors.features(frame, shot);

                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(cameraImageView, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, fps, TimeUnit.MILLISECONDS);

                this.startCameraBT.setText("Stop Camera");
            } else {
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            this.cameraActive = false;
            this.startCameraBT.setText("Start Camera");
            this.stopAcquisition();
            this.setClosed();
        }
    }

    @FXML
    void shotBtClick(ActionEvent event) {
        shoting = true;
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);

                if (!frame.empty()) {
                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    cvtColor(frame, frame, CV_LOAD_IMAGE_COLOR);
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(fps, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Platform.runLater(() -> view.imageProperty().set(image));
    }

    protected void setClosed() {
        this.stopAcquisition();
    }
}