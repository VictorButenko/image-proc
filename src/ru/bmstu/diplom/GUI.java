package ru.bmstu.diplom;

import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import test.Ex2MyFirstGUIAppJava;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Класс, реализующий GUI (пользовательский интерфейс) для работы с 
 * изображениями.
 * 
 * @author Victor Butenko
 */
public final class GUI extends JFrame {
	
	//----------------------------ПОЛЯ---------------------------------------------
	
	/** Для корректной работы сериализации */
	private static final long serialVersionUID = 1L;

	/**Компонент для выбора файла изображения (Открытие из текущей директории проекта/imgs) */
	private final JFileChooser fileChooser = new JFileChooser(new File(".//imgs"));
	
	/** Компонент для отображения изображений*/
    private final JLabel imageView = new JLabel();

    /**Переменная, содержащая загруженное изображение*/
    private IplImage image = null;
  //----------------------------ПОЛЯ---------------------------------------------
	
    
    // Конструктор
    private GUI() throws HeadlessException {
        super("The application for image processing"); 
        
        // Define actions


        // Action perfomed when "Save" button is pressed
        final Action saveAction = new AbstractAction("Save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (image != null) {
						final String path = fileChooser.getSelectedFile().getAbsolutePath();
						String newImgName = path + "_snap.jpg";
						cvSaveImage(newImgName, image);
						showMessageDialog(GUI.this, 
								"Image " + newImgName + " was successully saved.", getTitle(), 
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						showMessageDialog(GUI.this, "Image not opened",
                        		getTitle(), ERROR_MESSAGE);
					}
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
				
			}
		};
        
        //  Action performed when "Process" button is pressed
        final Action processAction = new AbstractAction("Process") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // Обработать и обновить экран, если изображение загружено
                	if (image != null) {
                        processImage(image);
                        imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                    } else {
                        showMessageDialog(GUI.this, "Image not opened",
                        		getTitle(), ERROR_MESSAGE);
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        processAction.setEnabled(false);

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
                        imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                        processAction.setEnabled(true);

                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };


        //
        // Create UI
        //

        // Create button panel
        final JPanel buttonsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        buttonsPanel.add(new JButton(openImageAction));
        buttonsPanel.add(new JButton());//TODO
        buttonsPanel.add(new JButton(processAction));
        buttonsPanel.add(new JButton());//TODO
        buttonsPanel.add(new JButton(saveAction));//TODO
        

        // Layout frame contents

        // Action buttons on the left
        final JPanel leftPane = new JPanel();
        leftPane.add(buttonsPanel);
        add(leftPane, BorderLayout.WEST);

        // Image display in the center
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.setPreferredSize(new Dimension(640, 480));
        add(imageScrollPane, BorderLayout.CENTER);
    
    }
	
    
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
    private void processImage(final IplImage src) {
        // Flip upside down
        cvFlip(src, src, 0);
        // Swap red and blue channels
        cvCvtColor(src, src, CV_BGR2RGB);
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