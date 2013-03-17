package ru.bmstu.diplom;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;


/**
 * Песочница. 
 * Базовый класс для загрузки, отображения и обработки изображения, построенный на JavaCV
 * 
 * Погрешность принимаем равное 25 единицам (получено экспериментальным путем).TODO: check??
 * 
 * @author Butenko Victor, BMSTU, Spring 2013
 */
public class ImageProcessing {
	
	private CvMat image; 	      // Изображение для обработки, экземпляр класса JavaCV
	private CvMat original;	      // Оригинал изображения (загруженного)
	private CanvasFrame canvas;   // Фрэйм для отображения изображения, экземпляр класса JavaCV

	//TODO: delete hardcode !!
	private static final int x1 = 206, y1 = 135, x2 = 258, y2 = 173; //Для img1 !!!!
	private static final int error  = 25;
	
	
	//Конструктор  ( Инициализация) 
	public ImageProcessing() {
		
		// Чтение изображение, прямое получение матрицы пикселей :TODO (загрузку изображений!!)
		image = cvLoadImageM("imgs/img1.jpg");

		// Убеждаемся, что изображения были успешно загружены
		if (image == null) {
			System.out.println("original image not found!");
			System.exit(1);
		}
		
		// Сохраняем копию оригинала.
		original = image.clone();
				
		// Создать JavaCV-ое окно с изображением (1 указывает на отсутствие коррекции гаммы)
		canvas = new CanvasFrame("Image", 1);

		// Запрос на закрытие приложения во время закрытия окна с изображением
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
	}
	
	
	/**
	 * Главный цикл. Получение ввода от пользователя и соответствующая обработка 
	 */
	public void run() {
		Scanner in = new Scanner(System.in);
		System.out.println("Добро пожаловать в альфа-версию программы по обработке изображений.");
	    System.out.println("Введите 'do' для построение RGB-кластера из картинки;");
		System.out.println("ВВедите 'f' чтобы перевернуть изображение;");
		System.out.println("Введите 's' чтобы сохранить изображение");
		System.out.println("Введите 'o' чтобы восстановить начальное изображение ");
		System.out.println("Введите 'lol' чтобы выделить целевую область");
		System.out.println("Введите 'ro' чтобы вычислить среднее значение кластера RGB для дороги");

		while (true) {	// Бесконечный цикл
			// Отобразить изображение 
			canvas.showImage(image.asIplImage());
			
			// Get operation and dispatch to function to process it.
			// Получение команды и отправка её к фукнции обработки. 
			System.out.println("Operation >");
			String op = in.nextLine();
			
			if (op.isEmpty()) {
				continue;
			}
			else if (op.equals("do")) {
				doMatrix();
			}
			else if (op.equals("lol")) {
				allocatePart(x1, y1, x2, y2);
			}
			else if (op.equals("ro")) {
				findRoad(x1, y1, x2, y2);
			}
			else if (op.equals("o")) {
				// Восстановить оригинал
				image = original.clone();
			}
			else if (op.equals("f")) {
				flip();
			}
			else if (op.equals("s")) {
				//Сохранить текущее изображение в папке
				cvSaveImage("imgs/snapshot.jpg", image.asIplImage());
			}
			else {
				System.out.println("Unknown operation");
			}
		}
	}

	/**
	 * 1) Берем кусок дороги
     * 2) Считаем для него по всем точкам RBGср. 
	 * 3) Запоминаем крайние точки. По ним считается погрешность E. 
	 * 4) Проводим попиксельный просмотр, и если R, G и B попадает в диапазон E, 
	 *    то перекрашиваем пиксель в красный цвет
	 */

	/**
	 * Метод выделяет красным цветом область на изображении,
	 * ограниченную параметрами.
	 */
	private void allocatePart(int x1, int y1, int x2, int y2) {
		// Цикл по всем строкам (i), столбцам (j)			
		for (int i = 0; i < image.rows(); i ++) {
			for ( int j = 0; j < image.cols(); j ++ ) {
				// Выбираем целевые пиксели. 
				if((i >= x1) && (i <= x2))
					if ((j >= y1) && (j <= y2)) 
						image.put(j, i, 2, 255); // Note! Координаты черед-ся (y,x). FIXME:
				}
			}
	}
	
	/**
	 * Метод выделяет красным цветом область на изображении,
	 * ограниченную параметрами.
	 */
	private void findRoad(int x1, int y1, int x2, int y2) {

		int blueAvrg  = 0;
		int greenAvrg = 0;
		int redAvrg   = 0;
		int currColorBlue = 0, currColorGreen = 0, currColorRed = 0;
		int countPixels = (x2 - x1) * (y2 - y1); //Кол-во пикселей в данной области
		System.out.println("pixels: " + countPixels);
		
		// Цикл по всем строкам (i), столбцам (j), и цветам (c)				
		for (int i = 0; i < image.rows(); i ++) {
			for ( int j = 0; j < image.cols(); j ++ ) {
				
				// Выбираем целевые пиксели. 
				if((i >= x1) && (i <= x2))
					if ((j >= y1) && (j <= y2)) {		
						// Вытаскиваем текущие значения пикселей в RGB
						currColorBlue  = (int) image.get(j, i, 0);
						currColorGreen = (int) image.get(j, i, 1);
						currColorRed   = (int) image.get(j, i, 2);
						
						//Накапливаем сумму значений цветовых компонент 
						//для вычисления среднего значения
						blueAvrg  += currColorBlue;
						greenAvrg += currColorGreen;
						redAvrg   += currColorRed;
					}						
				}
			}
		// Посчитать среднее значение каждой компоненты RGB 
		if (countPixels != 0) {
			blueAvrg  = blueAvrg / countPixels ;
			greenAvrg = greenAvrg / countPixels;
			redAvrg   = redAvrg   / countPixels;
		} else {
			System.err.println("Error! An empty area !!!");
		}
		System.out.println("blue average : " + blueAvrg + 
			   "; green average : " + greenAvrg  + "; Red Average :  " + redAvrg);
		
		// Разрисовка пикселей. 
		paintRoad(blueAvrg, greenAvrg, redAvrg);
	}
	
	/**
	 * Вспомогательный метод. Производит попиксельный проход по изображению, 
	 * проверяя цвет пикселя на принадлежность к усредненному диапазону значений 
	 * для каждой цветовой компоненты. В случае успеха приравнивает компоненту 
	 * Red этого пикселя к 255. (Разукрашивает дорогу в красный цвет)
	 * 
	 * @param blueAvrg
	 * @param greenAvrg
	 * @param redAvrg
	 */
	private void paintRoad(int blueAvrg, int greenAvrg, int redAvrg) {

		int blueColor, greenColor, redColor;
				
		//К усредненым значениям добавляем погрешность error
		int blueMin = blueAvrg - error;
		int blueMax = blueAvrg + error;
		int greenMin = greenAvrg - error;
		int greenMax = greenAvrg + error;
		int redMin = redAvrg - error;
		int redMax = redAvrg + error;
		
		// Цикл по всем строкам (i), столбцам (j) 
        for (int i = 0; i < image.rows(); i++) {
        	for (int j = 0; j < image.cols(); j++) {
        		//Элементы проходим в порядке (y,x). (Не влияет на результат)
        		blueColor = (int) image.get(i, j, 0);
        		greenColor = (int) image.get(i, j, 1);
        		redColor =   (int) image.get(i, j , 2);
        		
        		// Если цвет пикселя попадает в диапазон значений с погрешностью 
        		if ( (blueColor < blueMax )  && (blueColor > blueMin)    &&
        			 (greenColor < greenMax) && (greenColor > greenMin)  &&
        			 (redColor < redMax)     && (redColor > redMin) ) {
        				image.put(i, j, 2, 255);  //Покрасить дорогу в красный цвет.
        				}
        		}
        	}
        }


	/**
	 * Простов вывод матричного представления изображения в файл.
	 * Сохраняем вывод в файл. Формат следующий ( y,x,c) - 
	 * y, x - координаты, c - цвет (BGR  - Blue, Green, Red (0..255))
	 * |y11, y12, y13, .., y1N |
	 * |y21, y22, y23, .., y2N |
	 * |...................    |
	 * |yN1, yN2, yN3, .., yNN |
	 * где y(i,j) = [0..255][0..255][0..255] - BGR
	 * 
	 * Кроме того, вывод цифровых значений в этом методе
	 * осуществляется в файл. Для этого перенаправляем 
	 * поток стандартного ввода (System.out) в текстовый файл.
	 *  
	 */
	private void doMatrix() {
		
		 //Создать текстовый файл для вывода
		 //Перенаправить вывод из консоли в файл.
		 try {
			System.setOut(new PrintStream(new FileOutputStream(new File("out.txt"))));
		} catch (FileNotFoundException e) {
			System.err.println("Нужно создать файл out.txt!!!");
			e.printStackTrace();
		}

		 //Обрабатываем изображение как матрицу
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
		//Перенаправить стандартный вывод назад в консоль.
		System.setOut(System.out); 
	}
	
	/**
	 * Перевернуть изображение вверх ногами
	 */
	private void flip() {
		// Создать новое изображение, в которое будут вставлены результирующие пиксели
		CvMat result = CvMat.create(image.rows(), image.cols(), image.type());
		// Цикл по всем строкам (i), столбцам (j), и цветам (c) 
		for (int i = 0; i < image.rows(); i++) {
			for (int j = 0; j < image.cols(); j++) {
				for (int c = 0; c < 3; c++) {
					int i2 = image.rows() - 1 - i; // Индекс последней строки =  (rows-1)
					result.put(i2, j, c, image.get(i, j, c));
				}
			}
		}
		// Make the current image be this new imagе
		image = result;
	}
	
	/**
	 * Создает экземпляр изображения и запускает обработку.
	 * @param args	ignored
	 */
	public static void main(String[] args) {
		ImageProcessing proc = new ImageProcessing();
		proc.run();
	}

}
