/*****************************************************************/
/* Copyright 2009 avajava.com                                    */
/* This code may be freely used and distributed in any project.  */
/* However, please do not remove this credit if you publish this */
/* code in paper or electronic form, such as on a web site.      */
/*****************************************************************/

package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class RedirectSystemOut {

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("This goes to the console");
		PrintStream console = System.out;

		File file = new File("out.txt");
		FileOutputStream fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		System.out.println("This goes to out.txt");

		System.setOut(console);
		System.out.println("This also goes to the console");
	}
}