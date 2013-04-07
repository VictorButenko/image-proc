package ru.bmstu.diplom;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


/**
 * Class for image processing. It's used by 'GUI.class'
 * 
 * @author Butenko Victor, BMSTU, Spring 2013
 * @see GUI.java
 */
public  class ImageProcessing {
	
	/**Image for processing, an instance of JavaCV class */
	private CvMat image;  
	
	/** Constructor (initialization) */
	public ImageProcessing(IplImage thisImage) {
		image = thisImage.asCvMat();
	}
	

	/**
	 * The method allocate the area 
	 * (which is bounded by parameters) with Red color. 
	 * Every pixel in the area is processing pixel by pixel. 
	 * 
	 * FIXME: Refactor. Not process all image, only the area. 
	 */
	public void allocatePart(int x1, int y1, int x2, int y2) {
		for (int i = 0; i < image.rows(); i ++) {
			for ( int j = 0; j < image.cols(); j ++ ) {
				// If pixel enteres in the area, then paint it to red.
				if((i >= x1) && (i <= x2))
					if ((j >= y1) && (j <= y2)) 
						image.put(j, i, 2, 255); // NOTE: [y,x], NOT [x,y] !!`
				}
			}
	}
	
	
	/**
	 * Find average values of colors of the area 
	 */
	public int[] averageColors(int x1, int y1, int x2, int y2) {
		
		int blueAvrg  = 0;
		int greenAvrg = 0;
		int redAvrg   = 0;
		int currColorBlue = 0, currColorGreen = 0, currColorRed = 0;
		int countPixels = (x2 - x1) * (y2 - y1); // The count of the pixels in the area

		System.out.println("pixels: " + (x2-x1) + " * " + (y2-y1) + " = " + countPixels);
		
		for (int i = 0; i < image.rows(); i ++) {
			for ( int j = 0; j < image.cols(); j ++ ) {
				if((i >= x1) && (i <= x2))
					if ((j >= y1) && (j <= y2)) {		
						//Pull the current color values from the pixel
						currColorBlue  = (int) image.get(j, i, 0);
						currColorGreen = (int) image.get(j, i, 1);
						currColorRed   = (int) image.get(j, i, 2);
						
						// Accumulate the sum of the color components
						blueAvrg  += currColorBlue;
						greenAvrg += currColorGreen;
						redAvrg   += currColorRed;
					}						
				}
			}

		// count up the averages values of every color component 
		if (countPixels != 0) {
			blueAvrg  = blueAvrg / countPixels ;
			greenAvrg = greenAvrg / countPixels;
			redAvrg   = redAvrg   / countPixels;
		} else {
			System.err.println("Error! An empty area !!!");
		}
		System.out.println("blue average : " + blueAvrg + 
			   "; green average : " + greenAvrg  + "; Red Average :  " + redAvrg);
		
		//Return the array of the avrg. colors for processing
		int[] avrgValues = { redAvrg, greenAvrg, blueAvrg };
		return avrgValues;
	}
	
	/**
	 * The Method find averages values and  paint every pixel to red, 
	 * if this pixel is entered to the range of the color avrg. values
	 */
	@Deprecated
	public void findArea(int x1, int y1, int x2, int y2, int error) {

		int[] avrgValues = averageColors(x1, y1, x2, y2);
		int redAvrg   = avrgValues[0];
		int greenAvrg = avrgValues[1];
		int blueAvrg  = avrgValues[2];
		// Painting
		paintByAvrgs(blueAvrg, greenAvrg, redAvrg, error);
	}
	
	/**
	 * This method paint every pixel to red, 
	 * if this pixel is entered to the range of the color avrg. values
	 * 
	 * @param blueAvrg
	 * @param greenAvrg
	 * @param redAvrg
	 */
	public void paintByAvrgs(int blueAvrg, int greenAvrg, int redAvrg, int error) {

		int blueColor, greenColor, redColor;
				
		//Add the error(estimate) to the average values
		int blueMin = blueAvrg - error;
		int blueMax = blueAvrg + error;
		int greenMin = greenAvrg - error;
		int greenMax = greenAvrg + error;
		int redMin = redAvrg - error;
		int redMax = redAvrg + error;
		
        for (int i = 0; i < image.rows(); i++) {
        	for (int j = 0; j < image.cols(); j++) {
        		blueColor = (int) image.get(i, j, 0);
        		greenColor = (int) image.get(i, j, 1);
        		redColor =   (int) image.get(i, j , 2);
        		
        		// It the value of color for every component is entered into the range 
        		if ( (blueColor < blueMax )  && (blueColor > blueMin)    &&
        			 (greenColor < greenMax) && (greenColor > greenMin)  &&
        			 (redColor < redMax)     && (redColor > redMin) ) {
        				image.put(i, j, 2, 255);  // paint the pixel into the RED
        				}
        		}
        	}
        }


	/**
	 * Print the matrix presentation of the image to the file 'out.txt'
	 * The format of the output is:
	 * (y,x,c) where 'y' and 'x' are coordinates, 
	 * 'c' - is a color (BGR  - Blue, Green, Red (0..255)
	 * |y11, y12, y13, .., y1N |
	 * |y21, y22, y23, .., y2N |
	 * |...................    |
	 * |yN1, yN2, yN3, .., yNN |
	 * where y(i,j) = [0..255][0..255][0..255] - BGR
	 * 
	 * Beside, output of the digital values in this method
	 * redirect the standard output (System.out) stream 
	 * to the file 'out.txt'
	 *  
	 */
	public void doMatrix() {
		
		try {
			System.setOut(new PrintStream(new FileOutputStream(new File("out.txt"))));
		} catch (FileNotFoundException e) {
			System.err.println("Нужно создать файл out.txt!!!");
			e.printStackTrace();
		}

		//Process an image as Matrix
		for(int i = 0; i < image.rows(); i++) {
			System.out.println("");
			for ( int j = 0; j < image.cols(); j++) {
				System.out.print("[ ");
				for (int c = 0; c < 3; c++) {
					System.out.print(image.get(i,j,c) + ", ");
				}
				System.out.print("]");
			}
		}
		//Redirect the output to standard stream back
		System.setOut(System.out); 
	}
	
}
