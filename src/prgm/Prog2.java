package prgm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class Prog2 {

	private HashNode root;
	private RandomAccessFile bucketBin;
	private RandomAccessFile dataBin;
	private int bucketSize = 2;
	private int bucketFileLineSize = 12;
	private int[] fieldLengths;

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
		System.out.println("Root: " + this.root.toString());
		for (int i = 0; i < 10; i++) {
			HashNode temp = root.getChild(i);
			System.out.println("\t" + temp.toString());
		}

		this.readInData();
		this.tempQuery();
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
		if ((this.bucketSize * 12 * 10) != (end - start)) {
			System.out.println("ERROR: Invalid start and end in setChildrenOffsets");
			System.exit(-1);
		}
		long step = this.bucketSize * 12;
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
	 * 			bucketSize [rows per bucket] * 10 [buckets] * 12 [bytes per row]
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
			binFile.setLength(bucketSize * 10 * 12); // Preallocate initial size
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
		// Get the number of bytes
		long totalBytes = 0;
		try {
			totalBytes = this.dataBin.length();
		} catch (IOException e) {
			System.out.println("ERROR: Could not read binary file length");
			System.exit(-1);
		}

		// Move to the end of the file and read the last byte
		int numberOfFields = -1;
		try {
			this.dataBin.seek(totalBytes - 4);
			numberOfFields = this.dataBin.readInt();

		} catch (IOException e) {
			System.out.println("ERROR: Could not read last byte of binary file");
			System.exit(-1);
		}

		// Read backwards for the number of fields to get the length of each field,
		// the data length, and the length of each line
		this.fieldLengths = getFieldLengthsArray(totalBytes, numberOfFields);

		// Data length is total bytes minus int marking number of fields
		// minus number of ints used for field lengths
		long dataLengthExcludingHeaders = totalBytes - 4 - numberOfFields * 4;
		int lineLength = 0;
		for (int j = 0; j < fieldLengths.length; j++) {
//			System.out.println(fieldLengths[j]);
			lineLength += fieldLengths[j] == -1 ? 4 : fieldLengths[j];
		}

		// All this does is print
		System.out
				.println(String.format("There are <%s> fields with line length <%s>", fieldLengths.length, lineLength));

		String[] lineStringList = new String[fieldLengths.length];
		System.out.println(String.format("Data length excluding headers is <%s>", dataLengthExcludingHeaders));
		for (long l = 0; l < dataLengthExcludingHeaders; l += lineLength) {
			this.readBinFileLineIntoArrayList(fieldLengths, lineStringList, l);
//			for (int k = 0; k < lineStringList.length; k ++) {
//				System.out.print(String.format("[%s]", lineStringList[k]));
//			}
//			System.out.println();
			if (lineStringList[0].length() != 0) {
				this.addToTree(lineStringList, l);
			}
		}

		//////////////////

//		// Next we iterate through each line of the file and add it to the tree
//		for (int m = 0; m < dataLengthExcludingHeaders; m += lineLength) {
//			this.addLineToTree
//		}
	}

	/*---------------------------------------------------------------------
	 * Method	addToTree
	 * 
	 * Purpose: Take a line of the data binary file and its position in the 
	 * 			the data binary file and iterate through the tree to find 
	 * 			the location where the runner id should be added to the hash
	 * 			bucket file
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
	private void addToTree(String[] lineStringList, long positionInDataBinFile) {
		// Parse the runner id into an integer. Exit if that fails
		int id;
		try {
			id = Integer.parseInt(lineStringList[0]);
		} catch (Exception e) {
			System.out.println("ERROR: Runner id not an int");
			return;
		}
		boolean atLeafNode = false;
		int idDigit = id % 10;
		HashNode currentNode = root.getChild(idDigit);
//		System.out.println(String.format("From id <%d> I got hash node <%s>", id, currentNode.toString()));
		while (!atLeafNode) {
			if (currentNode.isLeaf()) { // If we reached a leaf node stop iterating
				atLeafNode = true;
//				System.out.println(String.format("At leaf node for id <%s> and digit <%s>", id, idDigit));
			} else { // Otherwise move to the next digit in the runner id and the next level of the
						// tree
//				System.out.println("Not at leaf node ");
				// STILL NEED TO IMPLEMENT THIS
			}

			atLeafNode = true;
		}

		if (currentNode.getDataCount() == this.bucketSize) { // Resize if the tree if full
			this.resizeNode();

		}
		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());

		System.out.println(String.format("Adding id <%s> at position <%s>", id, positionInBinFile));

		try {
			this.bucketBin.seek(positionInBinFile);
			this.bucketBin.writeInt(id);
			this.bucketBin.writeLong(positionInDataBinFile);
			currentNode.incrDataCount();
		} catch (IOException e) {
			System.out.println("ERROR: Could not write to bucket binary file");
			System.exit(-1);
		}
		// Start at the end of the runner id and find that value in the
		// First level of the tree.
		// If that hashnode has bucketSize elements then the bucket is full
		// If not full we skip to the correct position in the hash bucket file
		// using the number of elements in the hashnode times the length of
		// each line and starting at the offset of the bucket/
		// Then we write the runner id and positionInDataBin file to the hash
		// bucket file
		// If the hashbucket is full we call resize on the node

	}

	private void tempQuery() {
		// Initialize the scanner and necessary variables
		Scanner scanner = new Scanner(System.in);
		boolean looping = true;
		String query;
		int runnerId = 0;
		boolean validInt = true;
		System.out.println("\nEnter a runnerId to search, or enter -1 to exit the program");
		// Loop until user enters -1
		while (looping) {
			query = scanner.nextLine();

			try {
				// Parse the int from the query or exit if it fails
				try {
					runnerId = Integer.parseInt(query);
					validInt = true;
				} catch (Exception e) {
					System.out.println("Please enter an integer");
					validInt = false;
				}
				if (validInt) {
					if (runnerId == -1) {
						looping = false;
					} else {
						// CHANGE THIS SECTION
						int temp = runnerId;
						int digit = temp % 10;
						int length = query.length();
						boolean found = false;
						HashNode currentNode = root.getChild(digit);
						while (!found) {
							// If we reached a leaf node then we just 
							// print that node
							System.out.println("In query inner loop");
							// If we reached the end of the query but not a leaf 
							// node then we just print all leaf nodes attached
							if (length == 0) {
								found = true;
								this.printNodeResults(runnerId, currentNode);
							}
							if (currentNode.isLeaf()) {
								found = true;
								this.printNodeResults(runnerId, currentNode);

							} 
							
							
							
							temp = temp / 10;
							digit = temp % 10;
							length --;
							
						}
						///////////
					}
				}

			} catch (Exception e) {
				System.out.println("Record not found");
//				e.printStackTrace();
			}

		}
		scanner.close();

	}

	private void printNodeResults(int query, HashNode node) {
		
		if (node.isLeaf()) {
			this.printNodeResultsHelper(query, node);
		}
		
		
	}

	private void printNodeResultsHelper(int query, HashNode node) {
		ArrayList<ValueOffsetPair> listOfResults = new ArrayList<ValueOffsetPair>(100);
//		long[] listOfRes = new long[100];
		ArrayList<Long> listOfRes = new ArrayList<Long>(100);
		int k = 0; 
		int id;
		String queryString = String.valueOf(query);
		String idString;
		long indexInDataBin;
		long offset = node.getOffset();
		// Print each 
		for (int i = 0; i < node.getDataCount(); i ++) {
			System.out.println("Loop A");
			try {
				this.bucketBin.seek(offset);
				id = this.bucketBin.readInt();
//				offset += 4;
				indexInDataBin = this.bucketBin.readLong();
				idString = String.valueOf(id);
//				System.out.println("queryString: " + queryString);
//				System.out.println("idString: " + idString);

				if (idString.endsWith(queryString)) {
//					listOfResults.add(new ValueOffsetPair(id, indexInDataBin));
//					listOfRes[k] = indexInDataBin;
					listOfRes.add(indexInDataBin);
					k ++;
				}
				
//				System.out.println(String.format("[%s][%s]", id, indexInDataBin));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			offset += this.bucketFileLineSize;
			
		}
		
		
		this.printPositionArray(listOfRes);
		
	}
	
	private void printPositionArray(ArrayList<Long> arr) {
		String[] lineStringArr = new String[this.fieldLengths.length];
		for (int i = 0; i < arr.size(); i ++) {
			System.out.println("Loop B - " + arr.size());

			this.readBinFileLineIntoArrayList(this.fieldLengths, lineStringArr, arr.get(i));
			for (int k = 0; k < lineStringArr.length; k ++) {
				

				System.out.print(String.format("[%s]", lineStringArr[k]));
				
			}
			System.out.print("Loop C");
			System.out.println();
		}
	}
	
	
	private void resizeNode() {
		// TODO Auto-generated method stub

	}

	/*---------------------------------------------------------------------
	|  Method getFieldLengthsArray
	|
	|  Purpose:	Read the binary file backwards from the last line, where 
	|			the last 4 bytes of the file is the number of fields, allowing
	|			this method to know the number of bytes to read backwards
	|			to get the length of each field. The lengths of each field are 
	|			placed into an int[] array. 
	|
	|  Pre-condition:	binFile is opened, numberOfRecords is the number 
	|					of bytes in the entire file, and numberOfFields
	|					is the last int of the file which tells this function
	|					how many times to iterate
	|
	|  Post-condition:	The byte lengths of each field is read into an int[] array
	|					and returned. Furthermore, -1 is stored in the array 
	|					for fields that are integers.
	|
	|  Parameters:
	|			binFile -- opened binary file to use seek on
	|			numberOfRecords - Number of bytes in the file
	|			numberOfFields - Number of ints to rread from the last line
	|
	|  Returns:  int[] array of field lengths 
	*-------------------------------------------------------------------*/
	private int[] getFieldLengthsArray(long numberOfRecords, int numberOfFields) {
		int[] fieldLengths = new int[numberOfFields];
		int i = numberOfFields;
		long pos = numberOfRecords - 8;
		while (i > 0) {

			try {
				this.dataBin.seek(pos);
				fieldLengths[i - 1] = this.dataBin.readInt();
			} catch (IOException e) {
				System.out.println("ERROR: Could not read field lengths from binary file");
				System.exit(-1);
			}

			pos -= 4;
			i--;
		}
		return fieldLengths;
	}

	/*---------------------------------------------------------------------
	|  Method readBinFileLineIntoArrayList
	|
	|  Purpose:	Take the bin file and an array to place the contents of the bin
	|			file into and read the number of bytes equivalent to the 
	|			lengths in fieldLength int[] array. If a -1 in field lengths 
	|			then 4 bytes are read, else the number of bytes in field lengths is read.
	|			This data is either read using readInt() or by creating 
	|			a byte array and converting it to a String. 
	|
	|  Pre-condition:	The binary file is open and lineStringList has been
	|					establish such that each field in the line can 
	|					be stored in it.
	|
	|  Post-condition:	lineStringList is updated to contain each field 
	|					of the line given by position. 
	|
	|  Parameters:
	|			binFile - the opened RandomAccessFile to read from
	|			fieldLengths - an int array containing the length of each field in 
	|				the line, with -1 representing ints, and all other values 
	|				representing strings.
	|			lineStringList - The established array to store the line info in
	|			position - a long marking the byte location of the line to read
	|				from in the file.
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private void readBinFileLineIntoArrayList(int[] fieldLengths, String[] lineStringList, long position) {
//		System.out.println(String.format("Position: <%s>", position));
		try {
			this.dataBin.seek(position);
			byte[] lineBytes = null;
			for (int k = 0; k < fieldLengths.length; k++) {
				if (fieldLengths[k] == -1) {
					lineStringList[k] = Integer.toString(this.dataBin.readInt());
				} else {
					lineBytes = new byte[fieldLengths[k]];
					this.dataBin.readFully(lineBytes);
					lineStringList[k] = new String(lineBytes);
				}

			}

		} catch (Exception e) {
			System.out.println("ERROR: Could not parse the binary file line");
			e.printStackTrace();
			System.exit(-1);

		}
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
