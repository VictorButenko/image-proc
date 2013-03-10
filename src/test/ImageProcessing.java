package test;
import com.googlecode.javacv.CanvasFrame;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.util.*;

/**
 * A basic class to load, display, and process an image, built on JavaCV
 * representations (though not utilizing the OpenCV image processing operations)
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 */
public class ImageProcessing {
	
	
	private CvMat image; 				// the image being processed, an instance of a JavaCV class
	private CvMat original;			// the image as initially loaded
	private CvMat mixin;				// another image (of the same dimensions), to be blended
	private CanvasFrame canvas; // a frame to display the image, an instance of a JavaCV class

	public ImageProcessing() {
		// Read images, directly getting pixel matrices.
		image = cvLoadImageM("imgs/baker.jpg");
		mixin = cvLoadImageM("imgs/img1.jpg");

		// Make sure they were successfully loaded.
		if (image == null) {
			System.out.println("original image not found!");
			System.exit(1);
		}
		if (mixin == null) {
			System.out.println("mixin image not found!");
			System.exit(1);
		}
		
		// Keep a copy of the original, so can revert.
		original = image.clone();
		
		// Create JavaCV image window. (1 indicates no gamma correction.)
		canvas = new CanvasFrame("Image", 1);

		// Request closing of the application when the image window is closed.
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Main loop: gets user input as to how to modify the image.
	 */
	public void run() {
		Scanner in = new Scanner(System.in);

		while (true) {	// Loop forever
			// Display image.
			canvas.showImage(image.asIplImage());
			// Get operation and dispatch to function to process it.
			// Note that there are some magic numbers here that you can play with.
			// (Having magic numbers buried like this is not generally good practice,
			// but this is a hodge-podge of examples.)
			System.out.println("Operation >");
			String op = in.nextLine();
			if (op.isEmpty()) {
				continue;
			}
			else if (op.equals("a")) {
				average(1);
			}
			else if (op.equals("b")) {
				blend(0.7);
			}
			else if (op.equals("d")) {
				dim(0.9);
			}
			else if (op.equals("f")) {
				flip();
			}
			else if (op.equals("g")) {
				gray();
			}
			else if (op.equals("h")) {
				sharpen(3);
			}
			else if (op.equals("m")) {
				scramble(5);
			}
			else if (op.equals("n")) {
				noise(20);
			}
			else if (op.equals("o")) {
				image = original.clone();
			}
			else if (op.equals("s")) {
				cvSaveImage("img/snapshot.jpg", image.asIplImage());
			}
			else {
				System.out.println("Unknown operation");
			}
		}
	}

	/**
	 * Blurs the current image by setting each pixel's values to the average of those in a radius-sized box around it.
	 * Use a smallish box (e.g., 1) unless the image is small or you're willing to wait a while.
	 * @param radius		size of box; e.g., 1 indicates +-1 around the pixel
	 */
	private void average(int radius) {
		// Create a new image into which the resulting pixels will be stored.
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					double sum = 0;
					int n = 0;
					// Loop over neighbor rows (ni) and columns (nj)
					// but be careful not to go outside image (max, min stuff).
					for (int ni = Math.max(0, i - radius); 
							ni < Math.min(image.rows(), i + 1 + radius); 
							ni++) {
						for (int nj = Math.max(0, j - radius); 
								nj < Math.min(image.cols(), j + 1 + radius);
								nj++) {
							sum += image.get(ni, nj, c);
							n++;
						}
					}
					result.put(i, j, c, sum / n);
				}
			}
		}
		// Make the current image be this new image.
		image = result;
	}

	/**
	 * Blends the current image with the mixin
	 * @param w		weight on current image (0-1), with 1-w on the mixin
	 */
	private void blend(double w) {
		// Create a new image into which the resulting pixels will be stored.
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					double blended = image.get(i, j, c) * w + mixin.get(i, j, c) * (1 - w);
					result.put(i, j, c, blended);
				}
			}
		}
		// Make the current image be this new image.
		image = result;
	}

	/**
	 * Dims the current image by scaling it by the specified amount.
	 * @param scale		how much to scale the pixel values (0-1)
	 */
	private void dim(double scale) {
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					double scaled = image.get(i, j, c) * scale;
					image.put(i, j, c, scaled);
				}
			}
		}
	}

	/**
	 * Flips the current image upside down.
	 */
	private void flip() {
		// Create a new image into which the resulting pixels will be stored.
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					int i2 = image.rows() - 1 - i; // note that last row index is rows-1
					result.put(i2, j, c, image.get(i, j, c));
				}
			}
		}
		// Make the current image be this new image.
		image = result;
	}

	/**
	 * Computes the luminosity of an rgb value by one standard formula.
	 * @param r		red value (0-255)
	 * @param g		green value (0-255)
	 * @param b		blue value (0-255)
	 * @return		luminosity (0-255)
	 */
	private static double luminosity(double r, double g, double b) {
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

	/**
	 * Makes the current image look grayscale (though still represented as BGR).
	 */
	private void gray() {
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				double gray = luminosity(image.get(i, j, 2), image.get(i, j, 1), image.get(i, j, 0));
				// Stuff the gray-scale value into each channel.
				for (int c = 0; c < 3; c++) {
					image.put(i, j, c, gray);
				}
			}
		}
	}

	/**
	 * Returns a value that is one of val (if it's between min or max) or min or max (if it's outside that range).
	 * @param val
	 * @param min
	 * @param max
	 * @return constrained value
	 */
	private static double constrain(double val, double min, double max) {
		if (val < min) {
			return min;
		}
		else if (val > max) {
			return max;
		}
		return val;
	}

	/**
	 * Adds random noise to each pixel.
	 * @param scale		maximum value of the noise to be added
	 */
	private void noise(double scale) {
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					// Add noise (from -scale to +scale) but don't go outside 0-255 range.
					double noise = scale * (2*Math.random() - 1);
					double noisified = constrain(image.get(i, j, c) + noise, 0, 255);
					image.put(i, j, c, noisified);
				}
			}
		}
	}

	/**
	 * Scrambles the current image by setting each pixel from some nearby pixel.
	 * @param radius		maximum distance (+- that amount in x and y) of "nearby"
	 */
	private void scramble(int radius) {
		// Create a new image into which the resulting pixels will be stored.
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				// Random neighbors in i and j; constrain to image
				int ni = (int) constrain(i + radius * (2*Math.random() - 1), 0, image.rows() - 1);
				int nj = (int) constrain(j + radius * (2*Math.random() - 1), 0, image.cols() - 1);
				for (int c = 0; c < 3; c++) {
					result.put(i, j, c, image.get(ni, nj, c));
				}
			}
		}
		// Make the current image be this new image.
		image = result;
	}

	/**
	 * Sharpens the current image by setting each pixel's values to subtract out those in a radius-sized box around it.
	 * Use a smallish box (e.g., 1) unless the image is small or you're willing to wait a while.
	 * @param radius		size of box; e.g., 1 indicates +-1 around the pixel
	 */
	private void sharpen(int radius) {
		// Create a new image into which the resulting pixels will be stored.
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Loop over rows (i), columns (j), and colors (c).
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					double sum = 0;
					int n = 0;
					// Loop over neighbor rows (ni) and columns (nj)
					// but be careful not to go outside image (max, min stuff).
					for (int ni = Math.max(0, i - radius); 
							ni < Math.min(image.rows(), i + 1 + radius); 
							ni++) {
						for (int nj = Math.max(0, j - radius); 
								nj < Math.min(image.cols(), j + 1 + radius);
								nj++) {
							// Subtract out all the neighbors
							if (ni != i || nj != j) {
								sum -= image.get(ni, nj, c);
								n++;
							}
						}
					}
					// Add in the pixel's own value, weighted so as to contribute 1 more than the neighbors
					// (but when setting, be sure not to go outside 0-255).
					sum += (n + 1) * image.get(i, j, c);
					result.put(i, j, c, constrain(sum, 0, 255));
				}
			}
		}
		// Make the current image be this new image.
		image = result;
	}

	/**
	 * Creates the image processing instance and start it running.
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		ImageProcessing proc = new ImageProcessing();
		proc.run();
	}
}