image-proc
==========

Image Processing. JavaCV (OpenCV). DiplomР° project
The current version of the program can just print RGB-clusters of each
pixel to the file. (Reading an image from the file, building an matrix of coordinates and colors(RGB)). 

Next step: Create an mask(filter) of forest (e.g.) and do sliding.

----------------16.03.2013-------------
-find the average values for square (R, G, B). 
- Work out with (x,y) or (y, x). There may  be some problems.
- Calculate an error (range for R, G, B) cluster. 
- Change all colors in the range on the picture to Rav, Gav, Bav.

---------------17.03.2013........00:56 --------------------------
- Некоторые данные по одному изображению. В конкретном диапазоне,
(который можно подсветить красным квадратом) - изучаем дорогу. 
Средние значения по всем пикселям: 
BGR [122, 153, 182];
Средние значения по минимальному-максимальному :
BGR [97, 120, 140];
Blue: 48...146;
Green: 64...176;
Red:   77..203;

Погрешность попробуем принять равной 25. (+-) к усредненным значениям
