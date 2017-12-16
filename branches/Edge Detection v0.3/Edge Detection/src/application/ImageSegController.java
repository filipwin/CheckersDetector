package application;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageSegController {

	// FXML buttons
	@FXML
	private Button cameraButton;
	@FXML
	private Button saveButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// checkbox for enabling/disabling Canny
	@FXML
	private CheckBox canny;
	// canny threshold value
	@FXML
	private Slider threshold;
	// checkbox for enabling/disabling background removal
	@FXML
	private CheckBox dilateErode;
	// inverse the threshold value for background removal
	@FXML
	private CheckBox inverse;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	static VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive;
	ByteArrayInputStream matCopy;
	int[][] colorValues = new int[640][480];
	int[][] contours = new int[640][480];
	BufferedImage bImage;
	BufferedImage bImageBold;

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		// set a fixed width for the frame
		originalFrame.setFitWidth(380);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);

		if (!this.cameraActive) {
			// disable setting checkboxes
			this.canny.setDisable(true);
			this.dilateErode.setDisable(true);

			// start the video capture
			this.capture.open(1);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						Image imageToShow = grabFrame();
						originalFrame.setImage(imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			// enable setting checkboxes
			this.canny.setDisable(false);
			this.dilateErode.setDisable(false);
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
			// clean the frame
			this.originalFrame.setImage(null);
		}
	}

	@FXML
	private void saveData() {
		Image img = this.grabFrame();
		this.saveToFile(img);
		this.extractImageData();
		this.redrawContours();
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame() {
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// handle edge detection
					if (this.canny.isSelected()) {
						frame = this.doCanny(frame);
					}
					// foreground detection
					else if (this.dilateErode.isSelected()) {
						frame = this.doBackgroundRemoval(frame);
					}

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

	/**
	 * Perform the operations needed for removing a uniform background
	 * 
	 * @param frame
	 *            the current frame
	 * @return an image with only foreground objects
	 */
	private Mat doBackgroundRemoval(Mat frame) {
		// init
		Mat hsvImg = new Mat();
		List<Mat> hsvPlanes = new ArrayList<>();
		Mat thresholdImg = new Mat();

		int thresh_type = Imgproc.THRESH_BINARY_INV;
		if (this.inverse.isSelected())
			thresh_type = Imgproc.THRESH_BINARY;

		// threshold the image with the average hue value
		hsvImg.create(frame.size(), CvType.CV_8U);
		Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2GRAY);
		Core.split(hsvImg, hsvPlanes);

		// get the average hue value of the image
		double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

		Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

		Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

		// dilate to fill gaps, erode to smooth edges
		Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
		Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

		Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

		// create the new image
		Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
		frame.copyTo(foreground, thresholdImg);

		return foreground;
	}

	/**
	 * Get the average hue value of the image starting from its Hue channel
	 * histogram
	 * 
	 * @param hsvImg
	 *            the current frame in HSV
	 * @param hueValues
	 *            the Hue component of the current frame
	 * @return the average Hue value
	 */
	private double getHistAverage(Mat hsvImg, Mat hueValues) {
		// init
		double average = 0.0;
		Mat hist_hue = new Mat();
		// 0-180: range of Hue values
		MatOfInt histSize = new MatOfInt(180);
		List<Mat> hue = new ArrayList<>();
		hue.add(hueValues);

		// compute the histogram
		Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

		// get the average Hue value of the image
		// (sum(bin(h)*h))/(image-height*image-width)
		// -----------------
		// equivalent to get the hue of each pixel in the image, add them, and
		// divide for the image size (height and width)
		for (int h = 0; h < 180; h++) {
			// for each bin, get its value and multiply it for the corresponding
			// hue
			average += (hist_hue.get(h, 0)[0] * h);
		}

		// return the average hue of the image
		return average = average / hsvImg.size().height / hsvImg.size().width;
	}

	/**
	 * Apply Canny
	 * 
	 * @param frame
	 *            the current frame
	 * @return an image elaborated with Canny
	 */
	private Mat doCanny(Mat frame) {
		// init
		Mat grayImage = new Mat();
		Mat detectedEdges = new Mat();

		// convert to grayscale
		Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

		// reduce noise with a 3x3 kernel
		Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

		// canny detector, with ratio of lower:upper threshold of 10:1
		Imgproc.Canny(detectedEdges, detectedEdges, this.threshold.getValue(), this.threshold.getValue() * 10);

		// using Canny's output as a mask, display the result
		Mat dest = new Mat();
		frame.copyTo(dest, detectedEdges);

		return dest;
	}

	/**
	 * Action triggered when the Canny checkbox is selected
	 * 
	 */
	@FXML
	protected void cannySelected() {
		// check whether the other checkbox is selected and deselect it
		if (this.dilateErode.isSelected()) {
			this.dilateErode.setSelected(false);
			this.inverse.setDisable(true);
		}

		// enable the threshold slider
		if (this.canny.isSelected())
			this.threshold.setDisable(false);
		else
			this.threshold.setDisable(true);

		// now the capture can start
		this.cameraButton.setDisable(false);
	}

	/**
	 * Action triggered when the "background removal" checkbox is selected
	 */
	@FXML
	protected void dilateErodeSelected() {
		// check whether the canny checkbox is selected, deselect it and disable
		// its slider
		if (this.canny.isSelected()) {
			this.canny.setSelected(false);
			this.threshold.setDisable(true);
		}

		if (this.dilateErode.isSelected())
			this.inverse.setDisable(false);
		else
			this.inverse.setDisable(true);

		// now the capture can start
		this.cameraButton.setDisable(false);
	}

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		matCopy = new ByteArrayInputStream(buffer.toArray());
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	public void saveToFile(Image image) {
		Path deletePath = Paths.get("C:/Users/Filip/Desktop/output.png");
		Path deletePathContour = Paths.get("C:/Users/Filip/Desktop/output_contour_tmp.png");
		try {
			Files.deleteIfExists(deletePath);
			Files.deleteIfExists(deletePathContour);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File outputFile = new File("C:/Users/Filip/Desktop/output" + ".png");
		File contourFile = new File("C:/Users/Filip/Desktop/output_contour_tmp.png");
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		this.bImage = bImage;
		try {
			ImageIO.write(bImage, "png", outputFile);
			ImageIO.write(bImage, "png", contourFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void extractImageData() {
		for (int i = 0; i < colorValues.length; i++) {
			for (int j = 0; j < colorValues[0].length; j++) {
				colorValues[i][j] = bImage.getRGB(i, j);
				System.out.print(colorValues[i][j] + ", ");
			}
			System.out.print("\n");
		}
	}
	
	public void redrawContours(){
		File img_contour = new File("C:\\Users\\Filip\\Desktop\\output_contour_tmp.png");
		Path deleteTmpPath = Paths.get("C:\\Users\\Filip\\Desktop\\output_contour_tmp.png");
		try {
			bImageBold = ImageIO.read(img_contour);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int val;
		for (int i = 0; i < colorValues.length; i++) {
			for (int j = 0; j < colorValues[0].length; j++) {
				val = this.bImage.getRGB(i, j);
				if(val > -16777000){
					this.bImageBold.setRGB(i, j, -1);
					if((i > 0 && j > 0) && 
							(i < colorValues.length - 1 && j < colorValues[0].length - 1)){
						this.bImageBold.setRGB(i-1, j, -1);
						this.bImageBold.setRGB(i+1, j, -1);
						this.bImageBold.setRGB(i, j-1, -1);
						this.bImageBold.setRGB(i, j+1, -1);
					}
				}
				else{
					this.bImageBold.setRGB(i, j, -16777216);
				}
			}
		}
		File outputFile = new File("C:/Users/Filip/Desktop/output_contours" + ".png");
		try {
			Files.deleteIfExists(deleteTmpPath);
			ImageIO.write(bImageBold, "png", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}