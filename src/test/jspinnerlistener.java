package test;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class jspinnerlistener {
	
    public static void main(String[] args) {
    	
        JFrame frame = new JFrame("Слушатель JSpinner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        JLabel myLabel = new JLabel("myLabel");
        
        // Сам слушатель:
        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner js = (JSpinner) e.getSource();
                System.out.println("Введенное значение: " + js.getValue());
            }
        };
    // Конец слушателя 
    
        // Объявление модели JSpinner'а
        SpinnerModel model = new SpinnerNumberModel();
        //Объявление JSpinner'а, которого будем слушать
        JSpinner spinner = new JSpinner(model);
        spinner.addChangeListener(listener);

        frame.add(myLabel, BorderLayout.EAST);
        frame.add(spinner, BorderLayout.SOUTH);
        frame.setSize(200, 100);
        frame.setVisible(true);
    }
}
 