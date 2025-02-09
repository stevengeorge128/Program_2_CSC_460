package prgm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Prog2 {

	private HashNode root;
	private RandomAccessFile bucketBin;
	private RandomAccessFile dataBin;
	private int bucketSize = 2;

	/*
	 * init) Check if the bin file in the input exists, if not then we have an
	 * issue. Then try to create the hash bucket file, if we cannot do that we also
	 * have an issue 1) Create the root node which should just be a reference to the
	 * hashNode class 2) Initialize the first 10 nodes of the hashNode tree. These
	 * should be sequential and contain a reference to the next node, a pointer to
	 * where they refer to in the hash bucket file, or a pointer to the next node in
	 * the tree if another level has been added. The root should just contain an
	 * array pointing to each of the 10 children. This will be an arrayList. This
	 * class should take its number when being initialized ?) Read in the file
	 * produced by program 1A line by line using a RandomAccessFile and the process
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
		
		Scanner scanner = new Scanner(System.in);
		String filepath = scanner.nextLine();
		this.dataBin = this.initializeDataBinFile(filepath);

//		RandomAccessFile binFile = null;
//		scanner.close();

		this.bucketBin = this.initializeBucketBinaryFile();
		if (this.bucketBin == null) {
			System.out.println("ERROR: Failed to create bucket binary file");
			System.exit(-1);
		}

		// Root node initialized with 10 children who do not have file pointers
		// nor children
		this.root = this.initializeRootNode();
		
		
		// Set each child to have an offset position in the bin file
		try {
			this.setChildrenOffsets(this.root, 0, this.bucketBin.length());
		} catch (IOException e) {
			System.out.println("ERROR: Could not get bucket binary file length");
			System.exit(-1);
		}

		
		
		if (this.root == null || this.bucketBin == null || this.dataBin == null) {
			System.out.println("ERROR: Failure to initialize");
			System.exit(-1);
		}
		
		System.out.println("Testing tree initialization");
		
		
		this.readInData();
		this.beginQuerying();
		this.closeFiles();


	}



	/*---------------------------------------------------------------------
	 * Method 	initializeRootNode
	 * 
	 * Purpose:	Initialize the root HashNode and its 10 children by creating
	 * 			each object and storing in the root ArrayList. Children will 
	 * 			be assigned values but not file pointers or children yet.
	 * 
	 * Pre-condition:	Root has not been initialized because it will be reassigned
	 * 
	 * Post-condition:	Root and its 10 initial children are created
	 * 
	 * Parameters:	None
	 * 
	 * Returns:	HashNode to be assigned to root
	 * 
	 *---------------------------------------------------------------------*/
	private HashNode initializeRootNode() {
		HashNode rootNode = new HashNode(null, false, 0);
		rootNode.createChildren();
		return rootNode;
	}
	
	/*---------------------------------------------------------------------
	 * Method 	setChildrenOffsets
	 * 
	 * Purpose:	Take a hashnode and two byte locations and evenly assign those
	 * 			byte locations to each hashnode to create buckets in the binary 
	 * 			bucket file
	 * 
	 * Pre-condition:	this.bucketBin has been initialized. this.root has
	 * 					been initialized. 
	 * 
	 * Post-condition:	
	 * 
	 * Parameters:	
	 * 
	 * Returns:	
	 * 
	 *---------------------------------------------------------------------*/
	private void setChildrenOffsets(HashNode node, long start, long end) {
		if ((this.bucketSize * 8 * 10) != (end - start)) {
			System.out.println("ERROR: Invalid start and end in setChildrenOffsets");
			System.exit(-1);
		}
		long step = this.bucketSize * 8;
		for (int i = 0; i < 10; i++) {
			// For each of the 10 offsets in the binary file
			// assign those offset to the given HashNode
			HashNode child = node.getChild(i);
			if (child == null) {
				System.out.println("ERROR: Tried to get null child.");
				System.exit(-1);
			}
			child.setChildOffset(start + (i * step));
		}

	}

	/*---------------------------------------------------------------------
	 * Method 	initializeBucketBinaryFile
	 * 
	 * Purpose:	Initialize the RandomAccessFile used to store the hash bucket 
	 * 			index-offset pairs. The file is initialized to 
	 * 			bucketSize [rows per bucket] * 10 [buckets] * 8 [bytes per row]
	 * 
	 * Pre-condition:	?
	 * 
	 * Post-condition:	bucketBinaryFile is initialized to the required size 
	 * 					for the initial set of buckets
	 * 
	 * Parameters:	None
	 * 
	 * Returns:	RandomAccessFile containing each bucket
	 * 
	 *---------------------------------------------------------------------*/
	private RandomAccessFile initializeBucketBinaryFile() {
		
		
		File binFileRef = null; // Used to check if bin file already exists and delete it
		RandomAccessFile binFile = null;
		// Check for and delete existing version
		try {
			binFileRef = new File("bucket.bin");
			if (binFileRef.exists())
				binFileRef.delete();
		} catch (Exception e) {
			System.out.println("ERROR: Could not delete previous instance of .bin file");
			System.exit(-1);
		}

		// Create and return the bin file
		
		try {
			binFile = new RandomAccessFile("bucket.bin", "rw");
			binFile.setLength(bucketSize * 10 * 8); // Preallocate initial size
			return binFile;
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: FileNotFoundException - Could not generate bucket binary file");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("ERROR - IOException - Could not set bucket binary file length");
			System.exit(-1);
		}
		return binFile;

	}

	/*---------------------------------------------------------------------
	 * Method 	initializeDataBinFile
	 * 
	 * Purpose:	Read in the name of the binary file containing the data from 
	 * 			the command line and open it as a RandomAccessFile in read mode
	 * 
	 * Pre-condition:	None
	 * 
	 * Post-condition:	The user input data binary file is stored in a RandomAccessFile reference
	 * 
	 * Parameters:	None
	 * 
	 * Returns:	Reference to the RandomAccessFile
	 * 
	 *---------------------------------------------------------------------*/
	private RandomAccessFile initializeDataBinFile(String path) {
		try {
			RandomAccessFile file = new RandomAccessFile(path, "r");
			return file;
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Could not find binary file");
			System.exit(-1);
		}
		return null;
	}

	/*---------------------------------------------------------------------
	 * Method 	readInData
	 * 
	 * Purpose:	Sequentially iterate through this.dataBin and add the data 
	 * 			to the tree data structure by appropriately parsing the data 
	 * 			and adding to the correctHash node. 
	 * 
	 * Pre-condition:	this.dataBin and this.hashBin have been intitialized
	 * 
	 * Post-condition:	all data in this.dataBin stored in the tree and hashBin
	 * 
	 * Parameters:	None
	 * 
	 * Returns:	None
	 * 
	 *---------------------------------------------------------------------*/
	private void readInData() {
		// TODO Auto-generated method stub

	}

	/*---------------------------------------------------------------------
	 * Method 	beginQuerying
	 * 
	 * Purpose:	Prompt the user to begin inputing queries allowing them 
	 * 			to search for data using the query as a suffix. For example
	 * 			5 would return all records that end in 5 but 51295 would return
	 * 			all queries that end in 51295. Querying ends when the 
	 *			user inputs -1
	 * 
	 * Pre-condition:	this.dataBin, this.hashBin, and the tree structure 
	 * 					have all been initialized with all of the data
	 * 
	 * Post-condition:	Queries are done according to user termination
	 * 
	 * Parameters:	None
	 * 
	 * Returns:	None
	 * 
	 *---------------------------------------------------------------------*/
	private void beginQuerying() {
		// TODO Auto-generated method stub

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
	private void closeFiles() {
		// TODO Auto-generated method stub

	}

}
