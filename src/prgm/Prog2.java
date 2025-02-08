package prgm;

import java.io.RandomAccessFile;
import java.util.Scanner;

public class Prog2 {
	
	
	private HashNode root;
	private RandomAccessFile bucketBin;
	private int bucketSize = 2;
	/*
	 * init) Check if the bin file in the input exists, if not then we have an issue. Then try to create 
	 * the hash bucket file, if we cannot do that we also have an issue
	 * 1) Create the root node which should just be a reference to the hashNode class
	 * 2) Initialize the first 10 nodes of the hashNode tree. These should be sequential and contain 
	 * a reference to the next node, a pointer to where they refer to in the hash bucket file, or a pointer 
	 * to the next node in the tree if another level has been added.
	 * 		The root should just contain an array pointing to each of the 10 children. This will be an arrayList.
	 * 	This class should take its number when being initialized
	 * ?) Read in the file produced by program 1A line by line using a RandomAccessFile and the process
	 * used in Prog1B.java . Should not have to do a lot here
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	public static void main(String[] args) {
		
		Prog2 program = new Prog2();
		program.start();
//		Scanner scanner = new Scanner(System.in);
//		String filepath = scanner.nextLine();
//		RandomAccessFile binFile = null;
//		
//
//		scanner.close();

	}
	
	/*---------------------------------------------------------------------
	 * Method 	
	 * 
	 * Purpose:	
	 * 
	 * Pre-condition:	
	 * 
	 * Post-condition:	
	 * 
	 * Parameters:	
	 * 
	 * Returns:	
	 * 
	 *---------------------------------------------------------------------*/
	
	/*---------------------------------------------------------------------
	 * Method	start
	 * 
	 * Purpose: Initialize the root node of the tree. Call the appropriate function
	 * 			to initialize the hash bucket binary file. Call the appropriate function
	 * 			to read in the data binary file and begin data processing. Begin querying
	 * 			after the data structure has been initialized.
	 * 
	 * Pre-condition: 	None
	 * 
	 * Post-condition:	User input binary file is read and data structure created allowing 
	 * 					querying to begin
	 * 
	 * Parameters:	None
	 * 
	 * Returns: None
	 * 
	 *---------------------------------------------------------------------*/
	private void start() {
		this.root = initializeRootNode();
		this.bucketBin = initializeBucketBinaryFile();
		
		
		
//		Scanner scanner = new Scanner(System.in);
//		String filepath = scanner.nextLine();
//		RandomAccessFile binFile = null;
//		scanner.close();
	}
	
	

}
