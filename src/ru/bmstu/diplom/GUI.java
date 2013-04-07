package ru.bmstu.diplom;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * The class implements GUI for operating with Images
 * @author Victor Butenko
 */
public final class GUI extends JFrame   {
	
	//----------------------------FIESDS---------------------------------------------
	
	/** For serialization */
	private static final long serialVersionUID = 1L;

	/** Components for image choosing (Opening from the project directory './imgs' */
	private final JFileChooser fileChooser = new JFileChooser(new File(".//imgs"));
	
	/** Component to display an image*/
    private final JLabel imageView = new JLabel();

    /**Variable with loaded image*/
    private IplImage image = null;
    /**Variable for resetting the changed image to the original */
    private IplImage original = null;
    
    /** Area coordinates*/
    private int x1, y1, x2, y2;
    /** The error of the RGB average*/
    private int error;
    
    /** the matrix of coordinates:  
     * ROAD:   |x1, y1, x2, y2|
	 * FOREST: |x1, y1, x2, y2|
	 * RIVER:  |x1, y1, x2, y2|
	**/
    private int[][] areaArrayCoordinates = new int[3][4];; 
    
    /**
     * The matrix of the average colors values
     * ROAD:   |R, G, B|
	 * FOREST: |R, G, B|
	 * RIVER:  |R, G, B|
     */
    private int[][] areaAverageColors = new int[3][3];
    
    /** Width Height of the image */
    private int  widthIm = 1280, heightIm = 1024; 
    
    /** Spinners for allocating the area */
    private JSpinner spinnerX1,  spinnerY1,  spinnerX2,  spinnerY2;

    /** Spinner for error choosing*/
    private JSpinner spinnerError; 
	
	/** Number models for spinner to create some restrictions (default, min, max, step) values*/    
    private SpinnerNumberModel modelX1, modelY1, modelX2, modelY2;
    private SpinnerModel modError; // Another one model for error
    
    private final String[] areas = {
            "Road",
            "Forest",
            "River" };
    private JComboBox typeBox = new JComboBox(areas);

  //----------------------------FIELDS END---------------------------------------------
	
    
    // -------------------------CONSTRUCTOR START -------------------------------------
    private GUI() throws HeadlessException {
        super("The application for image processing"); 
        
    	
        //Action perfomed when "Reset" button is pressed 
        final Action resetAction = new AbstractAction("Reset") {			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (image != null) {
						image = original.clone();
						imageView.setIcon(new ImageIcon(image.getBufferedImage()));
					} else {
						showMessageDialog(GUI.this, "Image is not opened",getTitle(), ERROR_MESSAGE);
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};
		
		
		
        // Action perfomed when "Save" button is pressed
        final Action saveAction = new AbstractAction("Save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (image != null) {
						final String newImgName = 
								fileChooser.getSelectedFile().getAbsolutePath() + "_snap.jpg";
						cvSaveImage(newImgName, image);
						showMessageDialog(GUI.this, 
								"Image " + newImgName + " was successully saved.", getTitle(), 
								INFORMATION_MESSAGE);
					} else {
						showMessageDialog(GUI.this, "Image is not opened",
                        		getTitle(), ERROR_MESSAGE);
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
				
			}
		};
        
		
		//  Action performed when "Allocate" button is pressed
        final Action allocateAction = new AbstractAction("Allocate") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                	// Handle and renew a screen if an image is loaded
                	if (image != null) {
                        allocateImage(image);
                        imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                    } else {
                        showMessageDialog(GUI.this, "Image is not opened",
                        		getTitle(), ERROR_MESSAGE);
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        allocateAction.setEnabled(false);
        
        
        //  Action performed when "Process" button is pressed
        final Action processAction = new AbstractAction("Process") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                	// Handle and renew a screen if an image is loaded
                	if (image != null) {
                        processImage(typeBox.getSelectedIndex(), image);
                        imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                    } else {
                        showMessageDialog(GUI.this, "Image is not opened",
                        		getTitle(), ERROR_MESSAGE);
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        processAction.setEnabled(false);

      //Action perfomed when "Fix" button is pressed
      		final Action fixAction  = new AbstractAction("Fix") {
      			private static final long serialVersionUID = 1L;
      			
      			@Override
      			public void actionPerformed(ActionEvent e) {
      				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      				try { 
      					if (image != null) {
      						int spinIndex = typeBox.getSelectedIndex();
      						
      						spinnersInit(spinIndex, image); //Init array[][] from spinners
      						countAverage(spinIndex, image); //Calculate averages values
      						
      						processAction.setEnabled(true); //Allow to use the button 'process'
      						showMessageDialog(GUI.this, 
      								"Coordinates of the " + typeBox.getSelectedItem() +" were fixed!", 
      								getTitle(), 
      								INFORMATION_MESSAGE);
      					} else {
      						showMessageDialog(GUI.this, "Image is not opened",
                              		getTitle(), ERROR_MESSAGE);
      					}
      				} finally {
      					setCursor(Cursor.getDefaultCursor());
      				}
      			}
      		};
      		fixAction.setEnabled(false);
      		
      		
        // Action performed when "Open Image" button is pressed
        final Action openImageAction = new AbstractAction("Open Image") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Load image and update display. If new image was not loaded do nothing.
                    final IplImage img = openImage();

                    if (img != null) {
                        image = img;
                        original = image.clone();
                        imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                        
                        // Buttons 'FIX' and 'Allocate' are available after opening
                        allocateAction.setEnabled(true);
                        fixAction.setEnabled(true);
                        
                        //Define the size of the image
                        modelX1.setMaximum(image.width());
                        modelY1.setMaximum(image.height());
                        modelX2.setMaximum(image.width());
                        modelY2.setMaximum(image.height());
                        
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        

        //
        // Create UI
        //

        //--------------------COMBOBOX PART -----------------------------------------------
        //Create the ComboBox for choosing the type of the area
        typeBox.setEditable(false); 
        typeBox.setAlignmentX(CENTER_ALIGNMENT);
        
        //Create a ComboBox Listener
        ActionListener boxListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox box = (JComboBox) e.getSource();
				String area = (String) box.getSelectedItem();
				selectForProcessing(area);
				
			}

			/**
			 * Set x1, y1, x2, y2 depends on the chosed area
			 * FIXME: params to setParams not hardcode recieve 
			 * @param area
			 */
			private void selectForProcessing(String area) {
				if(area.equals("Road")) {
					setParamsXY(0);
				} else if (area.equals("Forest")) {
					setParamsXY(1);
				} else if (area.equals("River")) {
					setParamsXY(2);
				} 
			}
			
			private void setParamsXY(int type) {
				x1 = areaArrayCoordinates[type][0];
				y1 = areaArrayCoordinates[type][1];
				x2 = areaArrayCoordinates[type][2];
				y2 = areaArrayCoordinates[type][3];
			}
		};
		typeBox.addActionListener(boxListener);

		//--------------------COMBOBOX PART -----------------------------------------------
        // Create button panel
        final JPanel buttonsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        buttonsPanel.add(new JButton(openImageAction));
        buttonsPanel.add(typeBox);
        buttonsPanel.add(new JButton(fixAction));
        buttonsPanel.add(new JButton(allocateAction));
        buttonsPanel.add(new JButton(processAction));
        buttonsPanel.add(new JButton(resetAction));
        buttonsPanel.add(new JButton(saveAction));
        
       
        //Create Spinner's listeners for every of coordinates and error
        ChangeListener listenerX1 = new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
                x1 = areaArrayCoordinates[typeBox.getSelectedIndex()][0];
            }
        };
        ChangeListener listenerY1 = new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
                y1 = areaArrayCoordinates[typeBox.getSelectedIndex()][1];
            }
        };
        ChangeListener listenerX2 = new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
                x2 = areaArrayCoordinates[typeBox.getSelectedIndex()][2];
            }
        };
        ChangeListener listenerY2 = new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
                y2 = areaArrayCoordinates[typeBox.getSelectedIndex()][3];
            }
        };
        ChangeListener listenerError = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner js = (JSpinner) e.getSource();
				error = (Integer) js.getValue();
			}
		};
        
	
		/** Default values for allocated area */
		int x1_0 = 206, y1_0 = 135, x2_0 = 258, y2_0 = 173; //For img1.jpg !!!!
		int err_0 = 25, err_max = 50; // error default = 25, max error range = 50. !!
	
         modelX1 = new SpinnerNumberModel(x1_0,  1, widthIm,  1); // (default, min, max, step)
         modelY1 = new SpinnerNumberModel(y1_0,  1, heightIm, 1);
         modelX2 = new SpinnerNumberModel(x2_0,  1, widthIm,  1);
         modelY2 = new SpinnerNumberModel(y2_0,  1, heightIm, 1);
         modError= new SpinnerNumberModel(err_0, 0, err_max,  1); 
        
        //Creating JSpinnser objects
        spinnerX1    = new JSpinner(modelX1);
        spinnerY1    = new JSpinner(modelY1);
        spinnerX2    = new JSpinner(modelX2);
        spinnerY2    = new JSpinner(modelY2);
        spinnerError = new JSpinner(modError);

        
        //Adding listeners for JSpinners
        spinnerX1.addChangeListener(listenerX1);
        spinnerY1.addChangeListener(listenerY1);
        spinnerX2.addChangeListener(listenerX2);
        spinnerY2.addChangeListener(listenerY2);
        spinnerError.addChangeListener(listenerError);
        
        //Creating labels for JSpinners
        JLabel x1Label = new JLabel("x1");
        JLabel y1Label = new JLabel("y1");
        JLabel x2Label = new JLabel("x2");
        JLabel y2Label = new JLabel("y2");
        JLabel erLabel = new JLabel("error");
       
        // Layout frame contents

        // Action buttons on the left
        final JPanel leftPane = new JPanel();
        leftPane.add(buttonsPanel);
        add(leftPane, BorderLayout.WEST);

      //Action spinners on the left
        final JPanel spinnerPane = new JPanel();
        spinnerPane.add(x1Label);
        spinnerPane.add(spinnerX1);
        spinnerPane.add(y1Label);
        spinnerPane.add(spinnerY1);
        spinnerPane.add(x2Label);
        spinnerPane.add(spinnerX2);
        spinnerPane.add(y2Label);
        spinnerPane.add(spinnerY2);
        spinnerPane.add(erLabel);
        spinnerPane.add(spinnerError);

        add(spinnerPane, BorderLayout.BEFORE_FIRST_LINE);
      
      //Define the size of the Screen
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenHeigth = screenSize.height;
        int screenWidth  = screenSize.width;
        // Image display in the center
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.setPreferredSize(new Dimension(screenWidth / 2 , screenHeigth / 2));
        add(imageScrollPane, BorderLayout.CENTER);
        //Set width and heigth of the frame by platform
        setLocationByPlatform(true);
    
    }
    // -------------------------CONSTRUCTOR END -------------------------------------
    
    /**
     * Ask user for location and open new image.
     *
     * @return Opened image or {@code null} if image was not loaded.
     */
    private IplImage openImage() {

        // Ask user for the location of the image file
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        // Load the image
        final String path = fileChooser.getSelectedFile().getAbsolutePath();
        final IplImage newImage = cvLoadImage(path);
       
        if (newImage != null) {
            return newImage;
        } else {
            showMessageDialog(this, "Cannot open image file: " + path, 
            		getTitle(), ERROR_MESSAGE);
            return null;
        }
    }


    /**
     * Process image in place
     * 
     * @param src image to process.
     */
    private void processImage(int type, final IplImage src) {
    	
    	ImageProcessing imgProc = new ImageProcessing(src);
        error = (Integer) spinnerError.getValue();
        int redAvrg   = areaAverageColors[type][0];
        int greenAvrg = areaAverageColors[type][1];
        int blueAvrg  = areaAverageColors[type][2];
        
        imgProc.paintByAvrgs(blueAvrg, greenAvrg, redAvrg, error);
    }

	/**Method for allocating the area 
	 * 
	 * FIXME: Should use x1,y1,x2,y2 instead of direct using spinners.
	 * */
    private void allocateImage (final IplImage src ) {
    	ImageProcessing imgProc = new ImageProcessing(src);
    	imgProc.allocatePart(
    			(Integer) spinnerX1.getValue(),
    			(Integer) spinnerY1.getValue(),
    			(Integer) spinnerX2.getValue(), 
    			(Integer) spinnerY2.getValue()
    			);
    	
    }
    
  /**Additional Method to initialize the coordinates*/
    private void spinnersInit(int type, final IplImage src) {
    	 areaArrayCoordinates[type][0]  = (Integer) spinnerX1.getValue(); //x1
    	 areaArrayCoordinates[type][1]  = (Integer) spinnerY1.getValue(); //y1
    	 areaArrayCoordinates[type][2]  = (Integer) spinnerX2.getValue(); //x2
    	 areaArrayCoordinates[type][3]  = (Integer) spinnerY2.getValue(); //y2
    	 
	}

    /** Method work out the average RGB values and saves them in the matrix*/
    private void countAverage(int type, final IplImage src) {
     
     x1 = areaArrayCoordinates[type][0];
   	 y1 = areaArrayCoordinates[type][1];
   	 x2 = areaArrayCoordinates[type][2];
   	 y2 = areaArrayCoordinates[type][3];
   	
   	 ImageProcessing imgProc = new ImageProcessing(src);
   	 areaAverageColors[type] = imgProc.averageColors(x1, y1, x2, y2);
    }
    
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final GUI frame = new GUI();
                frame.pack();
                // Mark for display in the center of the screen
                frame.setLocationRelativeTo(null);
                // Exit application when frame is closed.
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    
}