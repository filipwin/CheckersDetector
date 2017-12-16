package application;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.AWTException;
//getColor
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Robot;

public class ObjRecognitionController {
	public static final int BOARD_X = 9;
	public static final int BOARD_Y = 9;
	private int cameraIndex = 0;
	Image board = new Image("file:resources/Chess-Board-Brown.jpg");
	Image checkerWhite = new Image("file:resources/white.png");
	Image checkerBlack = new Image("file:resources/black.png");
	Image checkerWhiteSmall = new Image("file:resources/white_small.png");
	Image checkerBlackSmall = new Image("file:resources/black_small.png");
	@FXML
	private GridPane boardGridPane;
	@FXML
	private ImageView s00, s01, s02, s03, s04, s05, s06, s07, s10, s11, s12, s13, s14, s15, s16, s17,
	s20, s21, s22, s23, s24, s25, s26, s27, s30, s31, s32, s33, s34, s35, s36, s37, s40, s41, s42, s43,
	s44, s45, s46, s47, s50, s51, s52, s53, s54, s55, s56, s57, s60, s61, s62, s63, s64, s65, s66, s67,
	s70, s71, s72, s73, s74, s75, s76, s77, checkerP1, checkerP2;
	@FXML
	private ImageView s001, s011, s021, s031, s041, s051, s061, s071, s101, s111, s121, s131, s141, s151, s161, s171,
	s201, s211, s221, s231, s241, s251, s261, s271, s301, s311, s321, s331, s341, s351, s361, s371, s401, s411, s421, s431,
	s441, s451, s461, s471, s501, s511, s521, s531, s541, s551, s561, s571, s601, s611, s621, s631, s641, s651, s661, s671,
	s701, s711, s721, s731, s741, s751, s761, s771;
	@FXML
	private TextField colortext;
	@FXML
	private Button cameraButton;
	@FXML
	private Button captureButton;
	@FXML
	private Button exitButton;
	@FXML
	private Button saveButton;
	@FXML
	private CheckBox hsvOnExit;
	@FXML
	private ImageView boardFrame, boardFrame1;
	@FXML
	private ImageView originalFrame;
	@FXML
	private ImageView maskImage1;
	@FXML
	private ImageView maskImage2;
	@FXML
	private ImageView maskImage3;
	@FXML
	private ListView<String> hsvProfilesList;
	@FXML
	private ListView<Integer> movesHistoryList;
	@FXML
	private Slider hueStart1;
	@FXML
	private Slider hueStop1;
	@FXML
	private Slider saturationStart1;
	@FXML
	private Slider saturationStop1;
	@FXML
	private Slider valueStart1;
	@FXML
	private Slider valueStop1;
	@FXML
	private Slider hueStart2;
	@FXML
	private Slider hueStop2;
	@FXML
	private Slider saturationStart2;
	@FXML
	private Slider saturationStop2;
	@FXML
	private Slider valueStart2;
	@FXML
	private Slider valueStop2;
	@FXML
	private Slider hueStart3;
	@FXML
	private Slider hueStop3;
	@FXML
	private Slider saturationStart3;
	@FXML
	private Slider saturationStop3;
	@FXML
	private Slider valueStart3;
	@FXML
	private Slider valueStop3;
	//private Point[][] allEdges;
	private ScheduledExecutorService timer;
	static VideoCapture capture = new VideoCapture();
	private boolean cameraActive;
	private boolean processingState = false;
	private boolean listenerInitialized = false;
	private boolean loadStage = false;
	List<MatOfPoint> mask1Contours;
	List<MatOfPoint> mask2Contours;
	List<MatOfPoint> mask3Contours;
	List<Integer> movesIndexes = new ArrayList<Integer>();
	List<Integer> boardHsvValues = new ArrayList<>();
	List<Integer> p1HsvValues = new ArrayList<>();
	List<Integer> p2HsvValues = new ArrayList<>();
	List<String> profiles;
	//BoardSquare[][] squares;
	ImageView[][] boardImgRefs;
	ImageView[][] historyBoardImgRefs;
	XmlParser currentXml = new XmlParser();
	List<CapturedFrame> frameBuffer = new ArrayList<CapturedFrame>();
	List<CapturedFrame> frameHistory = new ArrayList<CapturedFrame>();
	CapturedFrame currentlyProcessedFrame;

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	private void loadHsvValues(String filename) {
		boardHsvValues = currentXml.readXml(filename, "board");
		p1HsvValues = currentXml.readXml(filename, "p1");
		p2HsvValues = currentXml.readXml(filename, "p2");

		hueStart1.setValue(boardHsvValues.get(0));
		hueStop1.setValue(boardHsvValues.get(1));
		saturationStart1.setValue(boardHsvValues.get(2));
		saturationStop1.setValue(boardHsvValues.get(3));
		valueStart1.setValue(boardHsvValues.get(4));
		valueStop1.setValue(boardHsvValues.get(5));

		hueStart2.setValue(p1HsvValues.get(0));
		hueStop2.setValue(p1HsvValues.get(1));
		saturationStart2.setValue(p1HsvValues.get(2));
		saturationStop2.setValue(p1HsvValues.get(3));
		valueStart2.setValue(p1HsvValues.get(4));
		valueStop2.setValue(p1HsvValues.get(5));

		hueStart3.setValue(p2HsvValues.get(0));
		hueStop3.setValue(p2HsvValues.get(1));
		saturationStart3.setValue(p2HsvValues.get(2));
		saturationStop3.setValue(p2HsvValues.get(3));
		valueStart3.setValue(p2HsvValues.get(4));
		valueStop3.setValue(p2HsvValues.get(5));
	}

	@FXML
	private void saveHsvProfile() {
		TextInputDialog saveDialog = new TextInputDialog("");
		saveDialog.setTitle("Save HSV");
		saveDialog.setHeaderText("Choose name for your HSV profile");
		saveDialog.setContentText("File Name: ");
		Optional<String> result = saveDialog.showAndWait();
		String entered = "";
		if (result.isPresent()) {
			entered = result.get();
		}
		Path profilePath = Paths.get("profiles/" + entered + ".xml");
		if (entered.equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Profile name cannot be empty!");
			// alert.setContentText("Ooops, there was an error!");
			alert.showAndWait();
		} else if (Files.exists(profilePath)) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Profile name already exists!");
			// alert.setContentText("Ooops, there was an error!");
			alert.showAndWait();
		} else {
			this.updateHsvValues();
			this.currentXml.createXml(entered, boardHsvValues, p1HsvValues, p2HsvValues);
			this.fillProfilesList();
		}
	}

	@FXML
	private void initialize(){
		this.cameraIndex = this.readPropertyFile();
	}
	
	@FXML
	private void closeWindow() {
		if (hsvOnExit.isSelected()) {
			this.updateHsvValues();
			// System.out.print("SELECTED");
			this.currentXml.createXml("hsvLast", boardHsvValues, p1HsvValues, p2HsvValues);
		}
		Stage stage = (Stage) exitButton.getScene().getWindow();
		stage.close();
		System.exit(0);
	}

	@FXML
	private void getColor() throws Exception {
		originalFrame.setOnMouseClicked( (event) -> {
			Color color = null;
			Robot robot;
			try {
				robot = new Robot();
			java.awt.Point coord = null;
			coord = MouseInfo.getPointerInfo().getLocation();
		    color = robot.getPixelColor(coord.x, coord.y);
		    double[] hsv = ColorConverter.RGBToHSV((double)color.getRed(), (double)color.getGreen(), (double)color.getBlue());
		    System.out.println(color);
		    System.out.println("[h="+(int)hsv[0]+",s="+(int)hsv[1]+",v="+(int)hsv[2]+"]");
		    String colorstring = color.toString();
		    originalFrame.setOnMouseClicked(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	@FXML
	private void startCamera() {
		if (!listenerInitialized)
			this.loadHsvValues("hsvLast");
		this.fillProfilesList();
		this.initProfilesListListener();
		this.initImages();
		boardImgRefs = this.boardImagesToArray();
		historyBoardImgRefs = this.historyBoardImagesToArray();

		// set a fixed width for all the image to show and preserve image ratio
		this.imageViewProperties(this.originalFrame, 400);
		this.imageViewProperties(this.maskImage1, 200);
		this.imageViewProperties(this.maskImage2, 200);
		this.imageViewProperties(this.maskImage3, 200);

		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(cameraIndex);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;
				this.captureButton.setDisable(false);

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						if (!processingState) {
							Image imageToShow = grabFrame();
							originalFrame.setImage(imageToShow);
						}
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.cameraButton.setText("Stop Camera");

			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(null);
				alert.setContentText("Cannot open camera connection! Check camera index!");
				alert.showAndWait();
				System.exit(0);
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			this.captureButton.setDisable(true);

			// stop the timer
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			// release the camera
			this.capture.release();
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */

	@FXML
	public void captureImageBuffer(){
		int numberOfFramesCaptured = 5;
		for(int i = 0; i < numberOfFramesCaptured; i++){
			frameBuffer.add(this.captureImage());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		CapturedFrame max = new CapturedFrame();
		for(int i = 0; i < numberOfFramesCaptured; i++){
			if(i == 0) max = frameBuffer.get(i);
			if(frameBuffer.get(i).compareTo(max) == 1){
				max = frameBuffer.get(i);
			}
		}
		this.frameHistory.add(max);
		this.updateMovesList();
		this.clearBoardImages(frameHistory.get(frameHistory.size()-1), 0);
		this.frameBuffer.clear();
		this.putCheckers(frameHistory.get(frameHistory.size()-1), 0);
	}
	
	public CapturedFrame captureImage() {
		processingState = true;
		double[] avgCoords = new double[2];
		this.clearAll();
		//CapturedFrame returnedFrame = new CapturedFrame();
		currentlyProcessedFrame = new CapturedFrame();
		if (!mask1Contours.isEmpty()) {
			for (int i = 0; i < mask1Contours.size(); i++) {
				MatOfPoint current = mask1Contours.get(i);
				List<Point> markerPoints_tmp = current.toList();
				avgCoords = this.getPointsAveragePosition(markerPoints_tmp);
				currentlyProcessedFrame.markerPoints.add(new Point(avgCoords[0], avgCoords[1]));
			}
		}
		if (!mask2Contours.isEmpty()) {
			for (int i = 0; i < mask2Contours.size(); i++) {
				MatOfPoint current = mask2Contours.get(i);
				List<Point> player1Points_tmp = current.toList();
				avgCoords = this.getPointsAveragePosition(player1Points_tmp);
				currentlyProcessedFrame.player1Points.add(new Point(avgCoords[0], avgCoords[1]));
			}
		}
		if (!mask3Contours.isEmpty()) {
			for (int i = 0; i < mask3Contours.size(); i++) {
				MatOfPoint current = mask3Contours.get(i);
				List<Point> player2Points_tmp = current.toList();
				avgCoords = this.getPointsAveragePosition(player2Points_tmp);
				currentlyProcessedFrame.player2Points.add(new Point(avgCoords[0], avgCoords[1]));
			}
		}
		this.sortMarkerPoints();
		currentlyProcessedFrame.player1Points = this.removeDuplicatePoints(currentlyProcessedFrame.player1Points);
		currentlyProcessedFrame.player2Points = this.removeDuplicatePoints(currentlyProcessedFrame.player2Points);
		System.out.print("Markers: " + currentlyProcessedFrame.markerPoints.size() + "\n");
		System.out.print("P1 :" + currentlyProcessedFrame.player1Points.size() + "\n");
		System.out.print("P2 :" + currentlyProcessedFrame.player2Points.size() + "\n");
		this.generateBorderPoints();
		processingState = false;
		//this.debug_printPoints(currentlyProcessedFrame.allEdges);
		this.findAllCheckers();
		//this.printBoard();
		CapturedFrame returnedFrame = new CapturedFrame(this.currentlyProcessedFrame);
		return returnedFrame;
	}

	private Image grabFrame() {
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				try {
					this.capture.read(frame);
				} catch (Exception e) {
					System.out.print(e.getMessage());
				}

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// init
					Mat blurredImage = new Mat();
					Mat hsvImage = new Mat();
					Mat mask1 = new Mat();
					Mat mask2 = new Mat();
					Mat mask3 = new Mat();
					Mat morphOutput1 = new Mat();
					Mat morphOutput2 = new Mat();
					Mat morphOutput3 = new Mat();

					Imgproc.blur(frame, blurredImage, new Size(7, 7));

					Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

					Scalar minValuesChessboard = new Scalar(this.hueStart1.getValue(), this.saturationStart1.getValue(),
							this.valueStart1.getValue());
					Scalar maxValuesChessboard = new Scalar(this.hueStop1.getValue(), this.saturationStop1.getValue(),
							this.valueStop1.getValue());
					Scalar minValuesPlayer1 = new Scalar(this.hueStart2.getValue(), this.saturationStart2.getValue(),
							this.valueStart2.getValue());
					Scalar maxValuesPlayer1 = new Scalar(this.hueStop2.getValue(), this.saturationStop2.getValue(),
							this.valueStop2.getValue());
					Scalar minValuesPlayer2 = new Scalar(this.hueStart3.getValue(), this.saturationStart3.getValue(),
							this.valueStart3.getValue());
					Scalar maxValuesPlayer2 = new Scalar(this.hueStop3.getValue(), this.saturationStop3.getValue(),
							this.valueStop3.getValue());

					Core.inRange(hsvImage, minValuesChessboard, maxValuesChessboard, mask1);
					Core.inRange(hsvImage, minValuesPlayer1, maxValuesPlayer1, mask2);
					Core.inRange(hsvImage, minValuesPlayer2, maxValuesPlayer2, mask3);

					Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
					Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

					Imgproc.erode(mask1, morphOutput1, erodeElement);
					Imgproc.erode(mask1, morphOutput1, erodeElement);
					Imgproc.erode(mask2, morphOutput2, erodeElement);
					Imgproc.erode(mask2, morphOutput2, erodeElement);
					Imgproc.erode(mask3, morphOutput3, erodeElement);
					Imgproc.erode(mask3, morphOutput3, erodeElement);

					Imgproc.dilate(mask1, morphOutput1, dilateElement);
					Imgproc.dilate(mask1, morphOutput1, dilateElement);
					Imgproc.dilate(mask2, morphOutput2, dilateElement);
					Imgproc.dilate(mask2, morphOutput2, dilateElement);
					Imgproc.dilate(mask3, morphOutput3, dilateElement);
					Imgproc.dilate(mask3, morphOutput3, dilateElement);

					// show the partial output
					this.onFXThread(this.maskImage1.imageProperty(), this.mat2Image(morphOutput1));
					this.onFXThread(this.maskImage2.imageProperty(), this.mat2Image(morphOutput2));
					this.onFXThread(this.maskImage3.imageProperty(), this.mat2Image(morphOutput3));

					// find the tennis ball(s) contours and show them
					frame = this.findAndDrawMesh(morphOutput1, morphOutput2, morphOutput3, frame);

					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image(frame);
				}

			} catch (Exception e) {
				// log the (full) error
				System.err.print("ERROR");
				e.printStackTrace();
			}
		}

		return imageToShow;
	}

	private Mat findAndDrawMesh(Mat mask1, Mat mask2, Mat mask3, Mat frame) {
		// init
		if(this.currentlyProcessedFrame == null){
			this.currentlyProcessedFrame = new CapturedFrame();
		}
		mask1Contours = new ArrayList<>();
		mask2Contours = new ArrayList<>();
		mask3Contours = new ArrayList<>();
		Mat hierarchy1 = new Mat();
		Mat hierarchy2 = new Mat();
		Mat hierarchy3 = new Mat();
		MatOfPoint mopMarkers = new MatOfPoint();
		List<MatOfPoint> lmopMarkers = new ArrayList<>();
		MatOfPoint mopPlayer1 = new MatOfPoint();
		MatOfPoint mopPlayer2 = new MatOfPoint();
		List<MatOfPoint> lmopPlayer1 = new ArrayList<>();
		List<MatOfPoint> lmopPlayer2 = new ArrayList<>();
		MatOfPoint mopAll = new MatOfPoint();
		List<MatOfPoint> lmopAll = new ArrayList<>();
		if (!currentlyProcessedFrame.markerPoints.isEmpty()) {
			mopMarkers.fromList(currentlyProcessedFrame.markerPoints);
			lmopMarkers.add(mopMarkers);
		}
		if (!currentlyProcessedFrame.player1Points.isEmpty()) {
			mopPlayer1.fromList(currentlyProcessedFrame.player1Points);
			lmopPlayer1.add(mopPlayer1);
		}
		if (!currentlyProcessedFrame.player2Points.isEmpty()) {
			mopPlayer2.fromList(currentlyProcessedFrame.player2Points);
			lmopPlayer2.add(mopPlayer2);
		}
		if (!currentlyProcessedFrame.allPoints.isEmpty()) {
			mopAll.fromList(currentlyProcessedFrame.allPoints);
			lmopAll.add(mopAll);
		}

		// find contours
		Imgproc.findContours(mask1, mask1Contours, hierarchy1, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(mask2, mask2Contours, hierarchy2, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.findContours(mask3, mask3Contours, hierarchy3, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		// if any contour exist...
		if (hierarchy1.size().height > 0 && hierarchy1.size().width > 0) {
			for (int idx = 0; idx >= 0; idx = (int) hierarchy1.get(0, idx)[0]) {
				Imgproc.drawContours(frame, mask1Contours, idx, new Scalar(250, 0, 0));

				if (!currentlyProcessedFrame.player1Points.isEmpty()) {
					Imgproc.drawContours(frame, lmopPlayer1, 0, new Scalar(0, 250, 0), 5);
				}
				if (!currentlyProcessedFrame.markerPoints.isEmpty()) {
					Imgproc.drawContours(frame, lmopMarkers, 0, new Scalar(0, 0, 250), 5);
				}
				if (!currentlyProcessedFrame.allPoints.isEmpty()) {
					Imgproc.drawContours(frame, lmopAll, 0, new Scalar(250, 250, 250), 5);
				}
			}
		}
		if (hierarchy2.size().height > 0 && hierarchy2.size().width > 0) {
			for (int idx = 0; idx >= 0; idx = (int) hierarchy2.get(0, idx)[0]) {
				Imgproc.drawContours(frame, mask2Contours, idx, new Scalar(0, 0, 250));
			}
		}
		if (hierarchy3.size().height > 0 && hierarchy3.size().width > 0) {
			for (int idx = 0; idx >= 0; idx = (int) hierarchy3.get(0, idx)[0]) {
				Imgproc.drawContours(frame, mask3Contours, idx, new Scalar(0, 250, 0));
			}
		}
		return frame;
	}

	private void imageViewProperties(ImageView image, int dimension) {
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}

	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	private <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				property.set(value);
			}
		});
	}

	private double[] getPointsAveragePosition(List<Point> points) {
		int xSum = 0;
		int ySum = 0;
		double[] result = new double[2];
		for (int i = 0; i < points.size(); i++) {
			xSum += points.get(i).x;
			ySum += points.get(i).y;
		}
		result[0] = xSum / points.size();
		result[1] = ySum / points.size();
		return result;
	}

	private List<Point> removeDuplicatePoints(List<Point> pts) {
		List<Integer> toDelete = new ArrayList<>();
		Set<Integer> tmp = new HashSet<Integer>(); // items added to set to
													// remove duplicates
		Point compared;
		for (int i = 0; i < pts.size(); i++) {
			compared = pts.get(i);
			for (int j = 0; j < pts.size(); j++) {
				if ((Math.abs(compared.x - pts.get(j).x) <= 10 && Math.abs(compared.y - pts.get(j).y) <= 10) && i != j
						&& !isMember(i, toDelete)) {
					// pts.remove(j);
					toDelete.add(j);
				}
			}
		}
		tmp.addAll(toDelete);
		toDelete.clear();
		toDelete.addAll(tmp);
		for (int i = 0; i < toDelete.size(); i++) {
			pts.remove(toDelete.get(i) - i);
		}
		return pts;
	}

	private void sortMarkerPoints() {
		Point upperLeft = new Point(0, 0);
		Point upperRight = new Point(0, 0);
		Point lowerRight = new Point(0, 0);
		Point lowerLeft = new Point(0, 0);
		Point tmp = new Point(0, 0);
		double midPoint = 0;
		double ySum = 0;
		List<Point> upper = new ArrayList<Point>();
		List<Point> lower = new ArrayList<Point>();

		// find middle point
		for (int i = 0; i < currentlyProcessedFrame.markerPoints.size(); i++) {
			ySum += currentlyProcessedFrame.markerPoints.get(i).y;
		}
		midPoint = ySum / currentlyProcessedFrame.markerPoints.size();
		// assign points to upper/lower half
		for (int i = 0; i < currentlyProcessedFrame.markerPoints.size(); i++) {
			if (currentlyProcessedFrame.markerPoints.get(i).y < midPoint) {
				upper.add(currentlyProcessedFrame.markerPoints.get(i));
			} else {
				lower.add(currentlyProcessedFrame.markerPoints.get(i));
			}
		}
		// find upper left
		for (int i = 0; i < upper.size(); i++) {
			if (i == 0)
				tmp = upper.get(i);
			if (tmp.x > upper.get(i).x) {
				tmp = upper.get(i);
			}
		}
		upperLeft = tmp;
		tmp = new Point(0, 0);
		// find upper right
		for (int i = 0; i < upper.size(); i++) {
			if (i == 0)
				tmp = upper.get(i);
			if (tmp.x < upper.get(i).x) {
				tmp = upper.get(i);
			}
		}
		upperRight = tmp;
		tmp = new Point(0, 0);
		// find lower right
		for (int i = 0; i < lower.size(); i++) {
			if (i == 0)
				tmp = lower.get(i);
			if (tmp.x < lower.get(i).x) {
				tmp = lower.get(i);
			}
		}
		lowerRight = tmp;
		tmp = new Point(0, 0);
		// find lower left
		for (int i = 0; i < lower.size(); i++) {
			if (i == 0)
				tmp = lower.get(i);
			if (tmp.x > lower.get(i).x) {
				tmp = lower.get(i);
			}
		}
		lowerLeft = tmp;
		tmp = new Point(0, 0);
		currentlyProcessedFrame.markerPoints.removeAll(currentlyProcessedFrame.markerPoints);
		currentlyProcessedFrame.markerPoints.add(upperLeft);
		currentlyProcessedFrame.markerPoints.add(upperRight);
		currentlyProcessedFrame.markerPoints.add(lowerRight);
		currentlyProcessedFrame.markerPoints.add(lowerLeft);
	}

	public void generateBorderPoints() {
		double xDiff1 = 0;
		double yDiff1 = 0;
		double xDiff2 = 0;
		double yDiff2 = 0;
		double xDiff3 = 0;
		double yDiff3 = 0;
		double xDiff4 = 0;
		double yDiff4 = 0;
		List<Point> borders = new ArrayList<Point>();
		currentlyProcessedFrame.allEdges = new Point[9][9];

		xDiff1 = (currentlyProcessedFrame.markerPoints.get(1).x - currentlyProcessedFrame.markerPoints.get(0).x) / (BOARD_X - 1);
		yDiff1 = (currentlyProcessedFrame.markerPoints.get(1).y - currentlyProcessedFrame.markerPoints.get(0).y) / (BOARD_Y - 1);
		xDiff2 = (currentlyProcessedFrame.markerPoints.get(2).x - currentlyProcessedFrame.markerPoints.get(1).x) / (BOARD_X - 1);
		yDiff2 = (currentlyProcessedFrame.markerPoints.get(2).y - currentlyProcessedFrame.markerPoints.get(1).y) / (BOARD_Y - 1);
		xDiff3 = (currentlyProcessedFrame.markerPoints.get(3).x - currentlyProcessedFrame.markerPoints.get(2).x) / (BOARD_X - 1);
		yDiff3 = (currentlyProcessedFrame.markerPoints.get(3).y - currentlyProcessedFrame.markerPoints.get(2).y) / (BOARD_Y - 1);
		xDiff4 = (currentlyProcessedFrame.markerPoints.get(0).x - currentlyProcessedFrame.markerPoints.get(3).x) / (BOARD_X - 1);
		yDiff4 = (currentlyProcessedFrame.markerPoints.get(0).y - currentlyProcessedFrame.markerPoints.get(3).y) / (BOARD_Y - 1);

		// upper-left to upper-right
		for (int i = 0; i < BOARD_X; i++) {
			if (i == 0)
				borders.add(currentlyProcessedFrame.markerPoints.get(0));
			else if (i == BOARD_X - 1)
				borders.add(currentlyProcessedFrame.markerPoints.get(1));
			else {
				borders.add(new Point(currentlyProcessedFrame.markerPoints.get(0).x + i * xDiff1, currentlyProcessedFrame.markerPoints.get(0).y + i * yDiff1));
			}
		}
		// upper-right to lower-right
		for (int i = 0; i < BOARD_Y; i++) {
			if (i == BOARD_Y - 1)
				borders.add(currentlyProcessedFrame.markerPoints.get(2));
			else {
				if (i >= 1)
					borders.add(new Point(currentlyProcessedFrame.markerPoints.get(1).x + i * xDiff2, currentlyProcessedFrame.markerPoints.get(1).y + i * yDiff2));
			}
		}
		// lower-right to lower-left
		for (int i = 0; i < BOARD_Y; i++) {
			if (i == BOARD_Y - 1)
				borders.add(currentlyProcessedFrame.markerPoints.get(3));
			else {
				if (i >= 1)
					borders.add(new Point(currentlyProcessedFrame.markerPoints.get(2).x + i * xDiff3, currentlyProcessedFrame.markerPoints.get(2).y + i * yDiff3));
			}
		}
		// lower-left to upper-left
		for (int i = 0; i < BOARD_Y; i++) {
			if (i == BOARD_Y - 1) {
			} else {
				if (i >= 1)
					borders.add(new Point(currentlyProcessedFrame.markerPoints.get(3).x + i * xDiff4, currentlyProcessedFrame.markerPoints.get(3).y + i * yDiff4));
			}
		}
		currentlyProcessedFrame.borderPoints = borders;
		this.addBordersToAll();
		for (int i = 1; i < BOARD_Y - 1; i++) {
			this.generatePointsHorizontal(borders.get(32 - i), borders.get(8 + i), i);
		}
		// debug-only
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				currentlyProcessedFrame.allEdges[i][j] = currentlyProcessedFrame.allPoints.get(i * 9 + j);
			}
		}

		this.toPointList(currentlyProcessedFrame.allEdges);
	}

	public List<Point> toPointList(Point[][] points) {
		List<Point> resultList = new ArrayList<Point>();
		List<Point> rowList = new ArrayList<Point>();
		for (int i = 0; i < 9; i++) {
			rowList = Arrays.asList(points[i]);
			resultList.addAll(rowList);
		}
		// this.debug_printPoints(points);
		return resultList;
	}

	public void generatePointsHorizontal(Point start, Point end, int rowIndex) {
		double xDiff = 0;
		double yDiff = 0;

		xDiff = (end.x - start.x) / (BOARD_X - 1);
		yDiff = (end.y - start.y) / (BOARD_X - 1);
		for (int i = 1; i < BOARD_X - 1; i++) {
			currentlyProcessedFrame.allPoints.add(rowIndex * BOARD_X + i, new Point(start.x + i * xDiff, start.y + i * yDiff));
		}
	}

	public void addBordersToAll() {
		for (int i = 0; i < 9; i++) {
			currentlyProcessedFrame.allPoints.add(currentlyProcessedFrame.borderPoints.get(i));
		}
		for (int i = 1; i < 8; i++) {
			currentlyProcessedFrame.allPoints.add(currentlyProcessedFrame.borderPoints.get(32 - i));
			currentlyProcessedFrame.allPoints.add(currentlyProcessedFrame.borderPoints.get(8 + i));
		}
		for (int i = 0; i < 9; i++) {
			currentlyProcessedFrame.allPoints.add(currentlyProcessedFrame.borderPoints.get(24 - i));
		}
	}

	private boolean isMember(int element, List<Integer> objectList) {
		for (int i = 0; i < objectList.size(); i++) {
			if (element == objectList.get(i)) {
				return true;
			}
		}
		return false;
	}

	private void fillProfilesList() {
		// hsvProfilesList = new ListView<String>();
		profiles = new ArrayList<>();
		profiles = this.findProfiles();
		this.loadStage = true;
		ObservableList<String> observableProfiles = FXCollections.observableArrayList(profiles);
		hsvProfilesList.setItems(observableProfiles);
		this.loadStage = false;
	}

	private List<String> findProfiles() {
		File profileDir = new File("profiles/");
		FileFilter profileFilter = new WildcardFileFilter("*.xml");
		File[] files = profileDir.listFiles(profileFilter);
		List<String> filePaths = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			String currentPath = files[i].getPath();
			String trimmedPath = currentPath.replace("profiles\\", "").replace(".xml", "");
			filePaths.add(trimmedPath);
		}
		return filePaths;
	}

	public void debug_printPoints(Point[][] points) {
		String path = "C:/Users/Filip/Desktop/img.png";
		Path deletePath = Paths.get(path);
		try {
			Files.deleteIfExists(deletePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File img = new File(path);
		BufferedImage bImg = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < bImg.getWidth(); x++) {
			for (int y = 0; y < bImg.getHeight(); y++) {
				bImg.setRGB(x, y, -1);
			}
		}
		for (int x = 0; x < points.length; x++) {
			for (int y = 0; y < points[0].length; y++) {
				bImg.setRGB((int) points[x][y].x, (int) points[x][y].y, -16777215);
				bImg.setRGB((int) points[x][y].x, (int) points[x][y].y + 1, -16777215);
				bImg.setRGB((int) points[x][y].x + 1, (int) points[x][y].y + 1, -16777215);
				bImg.setRGB((int) points[x][y].x + 1, (int) points[x][y].y, -16777215);
				if ((x == 0 && y == 0) || (x == 8 && y == 0) || (x == 8 && y == 8) || (x == 0 && y == 8)) {
					bImg.setRGB((int) points[x][y].x + 2, (int) points[x][y].y, -16777215);
					bImg.setRGB((int) points[x][y].x + 2, (int) points[x][y].y + 1, -16777215);
					bImg.setRGB((int) points[x][y].x, (int) points[x][y].y + 2, -16777215);
					bImg.setRGB((int) points[x][y].x + 1, (int) points[x][y].y + 2, -16777215);
					bImg.setRGB((int) points[x][y].x, (int) points[x][y].y - 1, -16777215);
					bImg.setRGB((int) points[x][y].x + 1, (int) points[x][y].y - 1, -16777215);
					bImg.setRGB((int) points[x][y].x - 1, (int) points[x][y].y, -16777215);
					bImg.setRGB((int) points[x][y].x - 1, (int) points[x][y].y + 1, -16777215);
				}
			}
		}
		for (int i = 0; i < currentlyProcessedFrame.player1Points.size(); i++) {
			bImg.setRGB((int) currentlyProcessedFrame.player1Points.get(i).x, (int) currentlyProcessedFrame.player1Points.get(i).y, -3734982);
			bImg.setRGB((int) currentlyProcessedFrame.player1Points.get(i).x, (int) currentlyProcessedFrame.player1Points.get(i).y + 1, -3734982);
			bImg.setRGB((int) currentlyProcessedFrame.player1Points.get(i).x + 1, (int) currentlyProcessedFrame.player1Points.get(i).y, -3734982);
			bImg.setRGB((int) currentlyProcessedFrame.player1Points.get(i).x + 1, (int) currentlyProcessedFrame.player1Points.get(i).y + 1, -3734982);
		}
		for (int i = 0; i < currentlyProcessedFrame.player2Points.size(); i++) {
			bImg.setRGB((int) currentlyProcessedFrame.player2Points.get(i).x, (int) currentlyProcessedFrame.player2Points.get(i).y, -10734982);
			bImg.setRGB((int) currentlyProcessedFrame.player2Points.get(i).x, (int) currentlyProcessedFrame.player2Points.get(i).y + 1, -10734982);
			bImg.setRGB((int) currentlyProcessedFrame.player2Points.get(i).x + 1, (int) currentlyProcessedFrame.player2Points.get(i).y, -10734982);
			bImg.setRGB((int) currentlyProcessedFrame.player2Points.get(i).x + 1, (int) currentlyProcessedFrame.player2Points.get(i).y + 1, -10734982);
		}
		try {
			ImageIO.write(bImg, "png", img);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clearAll() {
		currentlyProcessedFrame.markerPoints.clear();
		currentlyProcessedFrame.player1Points.clear();
		currentlyProcessedFrame.player2Points.clear();
		currentlyProcessedFrame.borderPoints.clear();
		currentlyProcessedFrame.allPoints.clear();
	}

	private void updateHsvValues() {
		boardHsvValues.clear();
		p1HsvValues.clear();
		p2HsvValues.clear();

		boardHsvValues.add(new Integer((int) hueStart1.getValue()));
		boardHsvValues.add(new Integer((int) hueStop1.getValue()));
		boardHsvValues.add(new Integer((int) saturationStart1.getValue()));
		boardHsvValues.add(new Integer((int) saturationStop1.getValue()));
		boardHsvValues.add(new Integer((int) valueStart1.getValue()));
		boardHsvValues.add(new Integer((int) valueStop1.getValue()));

		p1HsvValues.add(new Integer((int) hueStart2.getValue()));
		p1HsvValues.add(new Integer((int) hueStop2.getValue()));
		p1HsvValues.add(new Integer((int) saturationStart2.getValue()));
		p1HsvValues.add(new Integer((int) saturationStop2.getValue()));
		p1HsvValues.add(new Integer((int) valueStart2.getValue()));
		p1HsvValues.add(new Integer((int) valueStop2.getValue()));

		p2HsvValues.add(new Integer((int) hueStart3.getValue()));
		p2HsvValues.add(new Integer((int) hueStop3.getValue()));
		p2HsvValues.add(new Integer((int) saturationStart3.getValue()));
		p2HsvValues.add(new Integer((int) saturationStop3.getValue()));
		p2HsvValues.add(new Integer((int) valueStart3.getValue()));
		p2HsvValues.add(new Integer((int) valueStop3.getValue()));

	}

	private void initProfilesListListener() {
		if (!listenerInitialized) {
			hsvProfilesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
				public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
					// System.out.println(newValue);
					if (!loadStage) {
						loadHsvValues(newValue);
					}
				}
			});
			movesHistoryList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
				public void changed(ObservableValue<? extends Integer> ov, Integer oldValue, Integer newValue) {
					// System.out.println(newValue);
					if (!loadStage) {
						//loadHsvValues(newValue);
						if(oldValue != null) clearBoardImages(frameHistory.get(oldValue), 1);
						if(newValue != null) putCheckers(frameHistory.get(newValue), 1);
					}
				}
			});
		}
		listenerInitialized = true;
	}
	
	private void findAllCheckers(){
		currentlyProcessedFrame.squares = new BoardSquare[BOARD_X - 1][BOARD_Y - 1];
		for(int x = 0; x < currentlyProcessedFrame.squares.length; x++){
			for(int y = 0; y < currentlyProcessedFrame.squares[0].length; y++){
				currentlyProcessedFrame.squares[x][y] = new BoardSquare(false, 0);
			}
		}
		int[] XYPos = new int[2];
		for(int p1 = 0; p1 < currentlyProcessedFrame.player1Points.size(); p1++){
			XYPos = this.findCheckerPos(currentlyProcessedFrame.player1Points.get(p1));
			currentlyProcessedFrame.squares[XYPos[0]][XYPos[1]] = new BoardSquare(true, 1);
		}
		for(int p2 = 0; p2 < currentlyProcessedFrame.player2Points.size(); p2++){
			XYPos = this.findCheckerPos(currentlyProcessedFrame.player2Points.get(p2));
			currentlyProcessedFrame.squares[XYPos[0]][XYPos[1]] = new BoardSquare(true, 2);
		}
	}
	
	private int[] findCheckerPos(Point checker){
		int[] result = new int[2];
		for(int x = 0; x < currentlyProcessedFrame.allEdges[0].length - 1; x++){//find column number
			if(currentlyProcessedFrame.allEdges[0][x].x < checker.x && currentlyProcessedFrame.allEdges[0][x+1].x > checker.x){
				result[0] = x;
			}
		}
		for(int y = 0; y < currentlyProcessedFrame.allEdges.length - 1; y++){//find row number
			if(currentlyProcessedFrame.allEdges[y][0].y < checker.y && currentlyProcessedFrame.allEdges[y+1][0].y > checker.y){
				result[1] = y;
			}
		}
		return result;
	}
	
	private void printBoard(){
		for(int x = 0; x < currentlyProcessedFrame.squares.length; x++){
			for(int y = 0; y < currentlyProcessedFrame.squares[0].length; y++){
				if(currentlyProcessedFrame.squares[y][x].player == 1){
					System.out.print("  1  ");
				}
				else if(currentlyProcessedFrame.squares[y][x].player == 2){
					System.out.print("  2  ");
				}
				else{
					System.out.print("  X  ");
				}
			}
			System.out.print("\n");
		}
	}
	private void initImages(){
		this.boardFrame.setImage(board);
		this.boardFrame1.setImage(board);
		this.checkerP1.setImage(checkerWhiteSmall);
		this.checkerP2.setImage(checkerBlackSmall);
	}
	
	private void putCheckers(CapturedFrame frame, int mode){
		//System.out.println("p1--- " + frame.player1Points.size());
		//System.out.println("p2--- " + frame.player2Points.size());
		for(int x = 0; x < frame.squares.length; x++){
			for(int y = 0; y < frame.squares[0].length; y++){
				if(frame.squares[y][x].player == 1){
					if(mode == 0) this.boardImgRefs[x][y].setImage(checkerWhite);
					if(mode == 1) this.historyBoardImgRefs[x][y].setImage(checkerWhite);
				}
				else if(frame.squares[y][x].player == 2){
					if(mode == 0) this.boardImgRefs[x][y].setImage(checkerBlack);
					if(mode == 1) this.historyBoardImgRefs[x][y].setImage(checkerBlack);
				}
			}
		}
	}
	
	private ImageView[][] boardImagesToArray(){
		ImageView[][] result = new ImageView[8][8];
		result[0][0] = s00; result[0][1] = s01; result[0][2] = s02; result[0][3] = s03; result[0][4] = s04; result[0][5] = s05; result[0][6] = s06; result[0][7] = s07;
		result[1][0] = s10; result[1][1] = s11; result[1][2] = s12; result[1][3] = s13; result[1][4] = s14; result[1][5] = s15; result[1][6] = s16; result[1][7] = s17;
		result[2][0] = s20; result[2][1] = s21; result[2][2] = s22; result[2][3] = s23; result[2][4] = s24; result[2][5] = s25; result[2][6] = s26; result[2][7] = s27;
		result[3][0] = s30; result[3][1] = s31; result[3][2] = s32; result[3][3] = s33; result[3][4] = s34; result[3][5] = s35; result[3][6] = s36; result[3][7] = s37;
		result[4][0] = s40; result[4][1] = s41; result[4][2] = s42; result[4][3] = s43; result[4][4] = s44; result[4][5] = s45; result[4][6] = s46; result[4][7] = s47;
		result[5][0] = s50; result[5][1] = s51; result[5][2] = s52; result[5][3] = s53; result[5][4] = s54; result[5][5] = s55; result[5][6] = s56; result[5][7] = s57;
		result[6][0] = s60; result[6][1] = s61; result[6][2] = s62; result[6][3] = s63; result[6][4] = s64; result[6][5] = s65; result[6][6] = s66; result[6][7] = s67;
		result[7][0] = s70; result[7][1] = s71; result[7][2] = s72; result[7][3] = s73; result[7][4] = s74; result[7][5] = s75; result[7][6] = s76; result[7][7] = s77;
		
		return result;
	}
	
	private ImageView[][] historyBoardImagesToArray(){
		ImageView[][] result = new ImageView[8][8];
		result[0][0] = s001; result[0][1] = s011; result[0][2] = s021; result[0][3] = s031; result[0][4] = s041; result[0][5] = s051; result[0][6] = s061; result[0][7] = s071;
		result[1][0] = s101; result[1][1] = s111; result[1][2] = s121; result[1][3] = s131; result[1][4] = s141; result[1][5] = s151; result[1][6] = s161; result[1][7] = s171;
		result[2][0] = s201; result[2][1] = s211; result[2][2] = s221; result[2][3] = s231; result[2][4] = s241; result[2][5] = s251; result[2][6] = s261; result[2][7] = s271;
		result[3][0] = s301; result[3][1] = s311; result[3][2] = s321; result[3][3] = s331; result[3][4] = s341; result[3][5] = s351; result[3][6] = s361; result[3][7] = s371;
		result[4][0] = s401; result[4][1] = s411; result[4][2] = s421; result[4][3] = s431; result[4][4] = s441; result[4][5] = s451; result[4][6] = s461; result[4][7] = s471;
		result[5][0] = s501; result[5][1] = s511; result[5][2] = s521; result[5][3] = s531; result[5][4] = s541; result[5][5] = s551; result[5][6] = s561; result[5][7] = s571;
		result[6][0] = s601; result[6][1] = s611; result[6][2] = s621; result[6][3] = s631; result[6][4] = s641; result[6][5] = s651; result[6][6] = s661; result[6][7] = s671;
		result[7][0] = s701; result[7][1] = s711; result[7][2] = s721; result[7][3] = s731; result[7][4] = s741; result[7][5] = s751; result[7][6] = s761; result[7][7] = s771;
		
		return result;
	}
	
	private void clearBoardImages(CapturedFrame frame, int mode){
		for(int x = 0; x < frame.squares.length; x++){
			for(int y = 0; y < frame.squares[0].length; y++){
				if(mode == 0) boardImgRefs[x][y].setImage(null);
				if(mode == 1) historyBoardImgRefs[x][y].setImage(null);
			}
		}
	}
	
	private void updateMovesList(){
		movesIndexes.add(frameHistory.size()-1);
		ObservableList<Integer> indexes = FXCollections.observableArrayList(movesIndexes);
		movesHistoryList.setItems(indexes);	
	}
	
	private int readPropertyFile(){
		String result = "";
		Properties prop = new Properties();
		String propName = "config.properties";
		
		InputStream is = getClass().getClassLoader().getResourceAsStream(propName);
		
		if(is != null){
			try {
				prop.load(is);
				result = prop.getProperty("cameraIndex");
				return Integer.parseInt(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				throw new FileNotFoundException("property file " + propName + "not found!");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Alert noFile = new Alert(AlertType.ERROR);
				noFile.setHeaderText(null);
				noFile.setContentText("Property file not found!");
				noFile.showAndWait();
				System.exit(0);
			}
		}
		return 0;
		
	}
}