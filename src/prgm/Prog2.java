package prgm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.ArrayDeque;

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

//		System.out.println("Testing tree initialization");
//		System.out.println("Root: " + this.root.toString());
//		for (int i = 0; i < 10; i++) {
//			HashNode temp = root.getChild(i);
//			System.out.println("\t" + temp.toString());
//		}

		this.readInData();
//		System.out.println("[id][offset][value]");
//		this.printBucketBinFile(this.root);
//		this.tempQuery();
		this.beginQuerying();
		this.closeFiles();

	}

	public void printBucketBinFile(HashNode node) {
//		if (!node.isLeaf()){
//			
//			// then call for each child
//			for (int i = 0; i < 10; i ++) {
//				this.printBucketBinFile(node.getChild(i));
//			}
//		} else {
//			for (int i = 0; i < node.getDataCount(); i ++) {
//				long position = node.getOffset() + i * this.bucketFileLineSize;
//				try {
//					this.bucketBin.seek(position);
//					int id = this.bucketBin.readInt();
//					long offset = this.bucketBin.readLong();
//					System.out.println(String.format("[%s]\t[%s]\t[%s]", offset, id, node.getValue()));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		try {
			long length = this.bucketBin.length() / 12;
			long location = 0;
			for (int i = 0; i < length; i++) {
				this.bucketBin.seek(location);

				int id = this.bucketBin.readInt();
				long offset = this.bucketBin.readLong();
				System.out.println(String.format("[%s]\t[%s]\t\t[%s]", location, id, offset));
				location += 12;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		rootNode.setLevel(0);
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
			binFile.setLength(bucketSize * 10 * this.bucketFileLineSize); // Preallocate initial size
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
//		System.out
//				.println(String.format("There are <%s> fields with line length <%s>", fieldLengths.length, lineLength));

		String[] lineStringList = new String[fieldLengths.length];
//		System.out.println(String.format("Data length excluding headers is <%s>", dataLengthExcludingHeaders));
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
//	private void addToTree(String[] lineStringList, long positionInDataBinFile) {
	// Parse the runner id into an integer. Exit if that fails
//		int level = 0;
//		int id;
//		try {
//			id = Integer.parseInt(lineStringList[0]);
//		} catch (Exception e) {
//			System.out.println("ERROR: Runner id not an int");
//			return;
//		}
//
//		// Set up boolean to track if at leaf, idDigit to track the current digit of the
//		// index,
//		// working backwards, and the current node we are at in the tree
//		System.out.println("Initial id in addToTree is " + id);
//		boolean atLeafNode = false;
//		int idDigit = id % 10;
//		int idToDivide = id;
//		HashNode currentNode = root.getChild(idDigit);
////		System.out.println(String.format("From id <%d> I got hash node <%s>", id, currentNode.toString()));
//
//		// While we have not yet reached the end of the tree
//		while (!atLeafNode) {
//			level ++;
//			System.out.println("+++ LEVEL IS " + level + " AND id IS " + id + " and IDTODIVIDE is " + idToDivide);
//
//			if (currentNode.isLeaf()) { // If we reached a leaf node stop iterating
//				System.out.println("A for idToDivide " + idToDivide);
//				atLeafNode = true;
//				if (currentNode.getDataCount() == this.bucketSize) { // Resize if the tree if full
//					System.out.println("B");
//
//					System.out.println("Supposed to be resizing");
//
////					level++;
//					System.out
//							.println(String.format("CALLING RESIZE FOR level = <%s> and id = <%s> and currentNode <%s>",
//									currentNode.getLevel(), id, currentNode.toString()));
//					this.resizeNode(currentNode, id, level);
//					atLeafNode = false;
//					idToDivide = idToDivide / 10;
//					idDigit = idToDivide % 10;
//					currentNode = currentNode.getChild(idDigit);
//
//				}
//			} else {
//				System.out.println("C");
//
//				// Otherwise move to the next digit in the runner id and the next level of the
//				// tree.
////				System.out.println("Not at leaf node ");
//				// STILL NEED TO IMPLEMENT THIS
//				idToDivide = idToDivide / 10;
//				idDigit = idToDivide % 10;
//				currentNode = currentNode.getChild(idDigit);
////				level++;
//
//			}
//
////			atLeafNode = true;
//			System.out.println("Looping");
//
//		}
//
//		// At this point a leaf node has been reached. Only leaf nodes store actual
//		// references
//		// to elements in the hash bucket binary file.
//
//		// If the current node is full we need to resize it and assign current node to
//		// the
//		// node that should contain the new data
//
//		// then we get the offset of current node in the hashBin file
//		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
//
////		System.out.println(String.format("Adding id <%s> at position <%s>", id, positionInBinFile));
//
//		// Then we seek to this position and write the id and the position in the data
//		// binary file
//		System.out.println("Writing " + id + " at position " + positionInBinFile + " with node of count"
//				+ currentNode.getDataCount());
//		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
//		// Start at the end of the runner id and find that value in the
//		// First level of the tree.
//		// If that hashnode has bucketSize elements then the bucket is full
//		// If not full we skip to the correct position in the hash bucket file
//		// using the number of elements in the hashnode times the length of
//		// each line and starting at the offset of the bucket/
//		// Then we write the runner id and positionInDataBin file to the hash
//		// bucket file
//		// If the hashbucket is full we call resize on the node
//
//	}

	private void addToTree(String[] lineStringList, long positionInDataBinFile) {
		// Parse the runner id into an integer. Exit if that fails
		int level = 1;
		int id;
		try {
			id = Integer.parseInt(lineStringList[0]);
		} catch (Exception e) {
			System.out.println("ERROR: Runner id not an int");
			return;
		}

		// Set up boolean to track if at leaf, idDigit to track the current digit of the
		// index,
		// working backwards, and the current node we are at in the tree
//		System.out.println("Initial id in addToTree is " + id);
		boolean atLeafNode = false;
		int idDigit = id % 10;
		int idToDivide = id;
		HashNode currentNode = root.getChild(idDigit);
//		System.out.println(String.format("From id <%d> I got hash node <%s>", id, currentNode.toString()));

		// While we have not yet reached the end of the tree
		while (!atLeafNode) {

			if (currentNode.isLeaf()) { // If we reached a leaf node stop iterating
//				System.out.println("A for idToDivide " + idToDivide);
				atLeafNode = true;
				if (currentNode.getDataCount() == this.bucketSize) { // Resize if the tree if full
//					System.out.println("B");

//					System.out.println("Supposed to be resizing");

//					System.out.println(String.format("Calling resize for id <%s>, idToDivid<%s>, digit <%s>, and node<%s>", 
//							id, idToDivide, idDigit, currentNode.toString()));
					this.resizeNode(currentNode, id, level + 1);
//					this.printBucketBinFile(root);
					atLeafNode = false;
					idToDivide = idToDivide / 10;
					idDigit = idToDivide % 10;
					currentNode = currentNode.getChild(idDigit);

				}
			} else {
//				System.out.println("C " + currentNode.toString());

				// Otherwise move to the next digit in the runner id and the next level of the
				// tree.
//				System.out.println("Not at leaf node ");
				// STILL NEED TO IMPLEMENT THIS
				idToDivide = idToDivide / 10;
				idDigit = idToDivide % 10;
				currentNode = currentNode.getChild(idDigit);
//				level++;

			}
			level++;
//			atLeafNode = true;
//			System.out.println("Looping");

		}

		// At this point a leaf node has been reached. Only leaf nodes store actual
		// references
		// to elements in the hash bucket binary file.

		// If the current node is full we need to resize it and assign current node to
		// the
		// node that should contain the new data

		// then we get the offset of current node in the hashBin file
		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());

//		System.out.println(String.format("Adding id <%s> at position <%s>", id, positionInBinFile));

		// Then we seek to this position and write the id and the position in the data
		// binary file
//		System.out.println("Writing " + id + " at position " + positionInBinFile + " with node of count"
//				+ currentNode.getDataCount());
		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
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

	private void writeIdAndOffsetToBinFile(long positionInDataBinFile, int id, HashNode currentNode,
			long positionInBinFile) {
//		System.out.println(String.format("writeIdAndOffsetToBinFile called for id=<%s>. currentNode=<%s>", id,
//				currentNode.toString()));
		try {
			this.bucketBin.seek(positionInBinFile);
			this.bucketBin.writeInt(id);
			this.bucketBin.writeLong(positionInDataBinFile);
			currentNode.incrDataCount();
		} catch (IOException e) {
			System.out.println("ERROR: Could not write to bucket binary file");
			System.exit(-1);
		}
	}

//	private void resizeNode(HashNode node, int id, int level) {
//		String idString = String.valueOf(id);
//		if (level > idString.length()) {
////			level = idString.length() -1;
//			this.printBucketBinFile(root);
//			System.out.println(String.format("ERROR: Invalid index in runner id for level <%s> and idString <%s>",
//					level, idString));
//			System.exit(-1);
//		}
//
//		// TODO Auto-generated method stub
//		// Iterate through every element in the current bin file from this node and sort
//		// it
//		// into 10 array lists
//		// Would be nice to keep track of the level
//		// Consider the case where we run out of positions
//		// Create 10 children for the current node and set it as not a
//		// a leaf
//		// Seek the end of the binary file.
//		// Extend the end of the binary file
//		// Assign the 10 slots to the new 10 leaf nodes
//		// Iterate through each sorted array list
//		// Add the elements to the appropriate bucket.
//		// Want to make sure no bucket is already full
//		// Cover this case where say, we have 2 elemens that end in the same two digits
//		// and add
//		// another element that is included in theese last two element. If we resize we
//		// will
//		// run in to the same issue and need to resize again
//		// Probably should check that we do not have the same id's at any point as this
//		// would
//		// cause an issue. Maybe print a warning. Maybe do nothing
//		// Then we add the new element which should be existing code
//		System.out.println(
//				String.format("Calling resize for elem <%s> on level <%s> for node <%s>", id, level, node.toString()));
//
//		// ArrayList to contain each id and offset from the portion of the has bucket
//		// file
//		// that are being resized
//		ArrayList<ValueOffsetPair> dataFromHashBin = new ArrayList<>(this.bucketSize);
//		// Get the offset of the node and how many elements it references
//		long currentOffset = node.getOffset();
//		int elemInNode = node.getDataCount();
//		int idFromHashBin;
//		long offsetFromHashBin;
//		try {
//			// Move to each of those elements and for each one, read the data from the
//			// binary
//			// file and store in in dataFromHashBin
//			this.bucketBin.seek(currentOffset);
//			for (int i = 0; i < elemInNode; i++) {
//				idFromHashBin = this.bucketBin.readInt();
//				offsetFromHashBin = this.bucketBin.readLong();
//				dataFromHashBin.add(new ValueOffsetPair(idFromHashBin, offsetFromHashBin));
//
//			}
//		} catch (Exception e) {
//			System.out.println("ERROR: Could not read from hash bucket binary file while resizing");
//		}
//
//		// Print out the elements that are being resized
//		System.out.println("Resizing the following elements");
//		for (ValueOffsetPair pair : dataFromHashBin) {
//			System.out.println(String.format("\t<%s><%s>", pair.getIndex(), pair.getOffset()));
//		}
//
//		// Set the node to empty as it not longer is a leaf and create its 10 children
//		node.setEmpty();
//		node.createChildren(node.getLevel() + 1);
//		System.out.println(String.format("After finding elements to store we have node as <%s>", node.toString()));
//
//		// Initialize variables to mark the start and end bytes of the children in the
//		// hash bucket binary file
//		long childrenStart = 0;
//		long childrenEnd = 0;
//		try {
//			// Find the start and end locations of these 10 children and seek to that
//			// location
//			childrenStart = this.bucketBin.length();
//			childrenEnd = childrenStart + bucketSize * 10 * this.bucketFileLineSize;
//			this.bucketBin.seek(childrenEnd);
//			System.out.println("Resizing the file to have " + childrenEnd + " bytes from " + childrenStart + " bytes");
//
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not extend hash bucket binary file");
//			System.exit(-1);
////			e.printStackTrace();
//		}
//		// Create the children offsets in this location
//		this.setChildrenOffsets(node, childrenStart, childrenEnd);
//
//		// For each child write their info from dataFromHashBin into the hash bucket
//		// binary file
//
//		System.out.println("We are looking at this digit in the id's workign from the back " + level);
//		System.out.println("Checking that new offsets have been correctly made");
//		for (int i = 0; i < 10; i++) {
//			HashNode temp = node.getChild(i);
//			System.out.println("\t" + temp.toString());
//
//		}
//
////		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
////		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
//		int substringStart;
//		String indexString;
//		HashNode leafNodeToAddTo;
//		long positionInBinFile;
//
//		for (ValueOffsetPair p : dataFromHashBin) {
//			// Get the id from the pair, convert it to String and get the digit we are one
//			// using
//			// level then convert that back to an int
//			indexString = String.valueOf(p.getIndex());
//			System.out.println("Level = " + level);
//			substringStart = indexString.length() - level;
//			String relevantDigitString = null;
//			try {
//				relevantDigitString = indexString.substring(substringStart, substringStart + 1);
//			} catch (Exception e) {
//				this.printBucketBinFile(root);
//				System.out.println(
//						String.format("ERROR: Could not call substring for <%s> with index <%s> and level <%s> and current node <%s>",
//								indexString, substringStart, level, node.toString()));
//				e.printStackTrace();
//				System.exit(-1);
//			}
//			int relevantDigit = Integer.parseInt(relevantDigitString);
//			System.out.println(String.format("For <%s> being resized we are looking at index <%s> or digit <%s>",
//					indexString, substringStart, relevantDigitString));
//
//			// Then get the node we are adding to using that digit and
//			// find the position we should add our data to in the hash bucket bin file
//			// and add it using writeIdAndOffsetToBinFile
//			leafNodeToAddTo = node.getChild(relevantDigit);
//			System.out.println("For this string got node " + leafNodeToAddTo.toString());
//			positionInBinFile = leafNodeToAddTo.getOffset() + (12 * leafNodeToAddTo.getDataCount());
//			System.out.println("Writing " + p.getIndex() + " and " + p.getOffset());
//			writeIdAndOffsetToBinFile(p.getOffset(), p.getIndex(), leafNodeToAddTo, positionInBinFile);
//		}
//
//	}

	private void resizeNode(HashNode node, int id, int level) {
		String idString = String.valueOf(id);
		if (level > idString.length()) {
//			level = idString.length() -1;
//			this.printBucketBinFile(root);
			System.out.println(String.format("ERROR: Invalid index in runner id for level <%s> and idString <%s>",
					level, idString));
			System.exit(-1);
		}

		// TODO Auto-generated method stub
		// Iterate through every element in the current bin file from this node and sort
		// it
		// into 10 array lists
		// Would be nice to keep track of the level
		// Consider the case where we run out of positions
		// Create 10 children for the current node and set it as not a
		// a leaf
		// Seek the end of the binary file.
		// Extend the end of the binary file
		// Assign the 10 slots to the new 10 leaf nodes
		// Iterate through each sorted array list
		// Add the elements to the appropriate bucket.
		// Want to make sure no bucket is already full
		// Cover this case where say, we have 2 elemens that end in the same two digits
		// and add
		// another element that is included in theese last two element. If we resize we
		// will
		// run in to the same issue and need to resize again
		// Probably should check that we do not have the same id's at any point as this
		// would
		// cause an issue. Maybe print a warning. Maybe do nothing
		// Then we add the new element which should be existing code
//		System.out.println(
//				String.format("Calling resize for elem <%s> on level <%s> for node <%s>", id, level, node.toString()));

		// ArrayList to contain each id and offset from the portion of the has bucket
		// file
		// that are being resized
		ArrayList<ValueOffsetPair> dataFromHashBin = new ArrayList<>(this.bucketSize);
		// Get the offset of the node and how many elements it references
		long currentOffset = node.getOffset();
		int elemInNode = node.getDataCount();
		int idFromHashBin;
		long offsetFromHashBin;
		try {
			// Move to each of those elements and for each one, read the data from the
			// binary
			// file and store in in dataFromHashBin
			this.bucketBin.seek(currentOffset);
			for (int i = 0; i < elemInNode; i++) {
				idFromHashBin = this.bucketBin.readInt();
				offsetFromHashBin = this.bucketBin.readLong();
				dataFromHashBin.add(new ValueOffsetPair(idFromHashBin, offsetFromHashBin));

			}
		} catch (Exception e) {
			System.out.println("ERROR: Could not read from hash bucket binary file while resizing");
		}

		// Print out the elements that are being resized
//		System.out.println("Resizing the following elements");
//		for (ValueOffsetPair pair : dataFromHashBin) {
//			System.out.println(String.format("\t<%s><%s>", pair.getIndex(), pair.getOffset()));
//		}

		// Set the node to empty as it not longer is a leaf and create its 10 children
		node.setEmpty();
		node.createChildren();
//		System.out.println("HEREEREERERERER " + node.isLeaf());
//		System.out.println(String.format("After finding elements to store we have node as <%s>", node.toString()));

		// Initialize variables to mark the start and end bytes of the children in the
		// hash bucket binary file
		long childrenStart = 0;
		long childrenEnd = 0;
		try {
			// Find the start and end locations of these 10 children and seek to that
			// location
			childrenStart = this.bucketBin.length();
			childrenEnd = childrenStart + bucketSize * 10 * this.bucketFileLineSize;
			this.bucketBin.seek(childrenEnd);
//			System.out.println("Resizing the file to have " + childrenEnd + " bytes from " + childrenStart + " bytes");

		} catch (IOException e) {
			System.out.println("ERROR: Could not extend hash bucket binary file");
			System.exit(-1);
//			e.printStackTrace();
		}
		// Create the children offsets in this location
		this.setChildrenOffsets(node, childrenStart, childrenEnd);

		// For each child write their info from dataFromHashBin into the hash bucket
		// binary file

//		System.out.println("We are looking at this digit in the id's workign from the back " + level);
//		System.out.println("Checking that new offsets have been correctly made");
//		for (int i = 0; i < 10; i++) {
//			HashNode temp = node.getChild(i);
//			System.out.println("\t" + temp.toString());
//
//		}

//		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
//		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
		int substringStart;
		String indexString;
		HashNode leafNodeToAddTo;
		long positionInBinFile;

		for (ValueOffsetPair p : dataFromHashBin) {
			// Get the id from the pair, convert it to String and get the digit we are one
			// using
			// level then convert that back to an int
			indexString = String.valueOf(p.getIndex());
			substringStart = indexString.length() - level;
			String relevantDigitString = null;
			try {
				relevantDigitString = indexString.substring(substringStart, substringStart + 1);
			} catch (Exception e) {
//				this.printBucketBinFile(root);
				System.out.println(
						String.format("ERROR: Could not call substring for <%s> with index <%s> and level <%s>",
								indexString, substringStart, level));
				e.printStackTrace();
				System.exit(-1);
			}
			int relevantDigit = Integer.parseInt(relevantDigitString);
//			System.out.println(String.format("For <%s> being resized we are looking at index <%s> or digit <%s>",
//					indexString, substringStart, relevantDigitString));

			// Then get the node we are adding to using that digit and
			// find the position we should add our data to in the hash bucket bin file
			// and add it using writeIdAndOffsetToBinFile
			leafNodeToAddTo = node.getChild(relevantDigit);
//			System.out.println("For this string got node " + leafNodeToAddTo.toString());
			positionInBinFile = leafNodeToAddTo.getOffset() + (12 * leafNodeToAddTo.getDataCount());
//			System.out.println("Writing " + p.getIndex() + " and " + p.getOffset());
			writeIdAndOffsetToBinFile(p.getOffset(), p.getIndex(), leafNodeToAddTo, positionInBinFile);
		}

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
			if (query.compareTo("00000000") == 0) {
				scanner.close();
				return;
			}

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
//							System.out.println("In query inner loop");
							// If we reached the end of the query but not a leaf
							// node then we just print all leaf nodes attached
							if (length == 0) {
								found = true;
								this.printNodeResults(runnerId, currentNode);
							}
							if (currentNode.isLeaf()) {
								found = true;
								this.printNodeResults(runnerId, currentNode);

							} else {
								temp = temp / 10;
								digit = temp % 10;
								length--;
								currentNode = currentNode.getChild(digit);

							}

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
//		System.out.println("printNodeResults called");
		if (node.isLeaf()) {
//			this.printNodeResultsHelper(query, node);
//			System.out.println("printNodeResultsHelper called");

			ArrayList<ValueOffsetPair> listOfResults = new ArrayList<ValueOffsetPair>(100);
//			long[] listOfRes = new long[100];
			ArrayList<Long> listOfRes = new ArrayList<Long>(100);
			int k = 0;
			int id;
			String queryString = String.valueOf(query);
			String idString;
			long indexInDataBin;
			long offset = node.getOffset();
			// Print each
			for (int i = 0; i < node.getDataCount(); i++) {
//				System.out.println("Loop A");
				try {
					this.bucketBin.seek(offset);
					id = this.bucketBin.readInt();
//					offset += 4;
					indexInDataBin = this.bucketBin.readLong();
					idString = String.valueOf(id);
//					System.out.println("queryString: " + queryString);
//					System.out.println("idString: " + idString);

					if (idString.endsWith(queryString)) {
//						listOfResults.add(new ValueOffsetPair(id, indexInDataBin));
//						listOfRes[k] = indexInDataBin;
						listOfRes.add(indexInDataBin);
						k++;
					}

//					System.out.println(String.format("[%s][%s]", id, indexInDataBin));

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				offset += this.bucketFileLineSize;

			}

			this.printPositionArray(listOfRes);

		}

	}

	private void printNodeResultsHelper(int query, HashNode node) {
//		System.out.println("printNodeResultsHelper called");

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
		for (int i = 0; i < node.getDataCount(); i++) {
//			System.out.println("Loop A");
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
					k++;
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
		for (int i = 0; i < arr.size(); i++) {
//			System.out.println("Loop B - " + arr.size());

			this.readBinFileLineIntoArrayList(this.fieldLengths, lineStringArr, arr.get(i));
			for (int k = 0; k < lineStringArr.length; k++) {

				System.out.print(String.format("[%s]", lineStringArr[k]));

			}
			System.out.println(); // KEEP THIS FOR ADDING NEW LINE TO OUTPUT
		}
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
		Scanner scanner = new Scanner(System.in);
		String query;
		int queryInt = -1;
		System.out.println("\nEnter a runnerId to search, or enter -1 to exit the program");
		while ((query = scanner.nextLine()).compareTo("00000000") != 0) {
//			System.out.println("query is " + query);
			try {
				queryInt = Integer.parseInt(query);
			} catch (Exception e) {
				System.out.println("Please enter a valid, non-negative integer");
			}
			if (queryInt < 0) {
				System.out.println("Please enter a valid, non-negative integer");

			} else {
				boolean found = false;
				int i = query.length() - 1;
				HashNode currentNode = root;
				while (i >= 0) {

//					System.out.println("i = " + i);
					try {
//						System.out.println("getting child " + query.substring(i, i+1));
						currentNode = currentNode.getChild(Integer.parseInt(query.substring(i, i + 1)));
						
//						System.out.println("current node is " + currentNode.toString() + " for index " + i);

						if (currentNode.isLeaf() && currentNode.getDataCount() == 0) {
							System.out.println("Suffix not found");
							i = -1;
						}
						// Case where we reach end of query before end of tree
						else if (i == 0 || currentNode.isLeaf()) {
							
							this.queryHelper(query, currentNode);
							i = -1;
						}
						i --;
					} catch (Exception e) {
						System.out.println("ERROR: query contains a non int");
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		System.out.println("Goodbye!");
	}
	
	private void queryHelper(String query, HashNode node) {
		ArrayDeque<HashNode> leafNodes = new ArrayDeque<>(20);
		leafNodes.offerLast(node);
		HashNode currentNode;
		while (!leafNodes.isEmpty()) {
			currentNode = leafNodes.poll();
//			System.out.println("From queue drew " + currentNode.toString());
			if (currentNode.isLeaf()) {
				this.printNode(query, currentNode);
			} else {
				for (int i = 0 ; i < 10 ; i ++) {
					leafNodes.offerLast(currentNode.getChild(i));
				}
			}
		}
		
	}

	
	private void printNode(String query, HashNode node) {
		boolean printed = false;
		if (node.getDataCount() == 0) {
			return;
		}
		long offsetInBucketBin = node.getOffset();
		String[] lineStringArr = new String[this.fieldLengths.length];
		try {
			
			this.bucketBin.seek(offsetInBucketBin);
			for (int i = 0; i < node.getDataCount(); i ++) {
				int idFromBin = this.bucketBin.readInt();
				long offsetInDataBin = this.bucketBin.readLong();
				String idStringFromBin = String.valueOf(idFromBin);
//				System.out.println("----------------------\nidStringFromBin is " + idStringFromBin);
//				System.out.println("query is " + query);
//				System.out.println("\t and result of ends with is " + idStringFromBin.endsWith(query));
				if (idStringFromBin.endsWith(query)) {
					
					this.readBinFileLineIntoArrayList(this.fieldLengths, lineStringArr, offsetInDataBin);
					for (int k = 0; k < lineStringArr.length; k++) {
						printed = true;
						System.out.print(String.format("[%s]", lineStringArr[k]));

					}
					System.out.println(); // KEEP THIS FOR ADDING NEW LINE TO OUTPUT
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!printed) {
			System.out.println("Suffix not found");
		}
		
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
//package prgm;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.util.ArrayList;
//import java.util.Scanner;
//
//public class Prog2 {
//
//	private HashNode root;
//	private RandomAccessFile bucketBin;
//	private RandomAccessFile dataBin;
//	private int bucketSize = 100;
//	private int bucketFileLineSize = 12;
//	private int[] fieldLengths;
//
//	/*
//	 * init) Check if the bin file in the input exists, if not then we have an
//	 * issue. Then try to create the hash bucket file, if we cannot do that we also
//	 * have an issue 1) Create the root node which should just be a reference to the
//	 * hashNode class 2) Initialize the first 10 nodes of the hashNode tree. These
//	 * should be sequential and contain a reference to the next node, a pointer to
//	 * where they refer to in the hash bucket file, or a pointer to the next node in
//	 * the tree if another level has been added. The root should just contain an
//	 * array pointing to each of the 10 children. This will be an arrayList. This
//	 * class should take its number when being initialized ?) Read in the file
//	 * produced by program 1A line by line using a RandomAccessFile and the process
//	 * used in Prog1B.java . Should not have to do a lot here
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 */
//
//	public static void main(String[] args) {
//
//		Prog2 program = new Prog2();
//		program.start();
////		Scanner scanner = new Scanner(System.in);
////		String filepath = scanner.nextLine();
////		RandomAccessFile binFile = null;
////		
////
////		scanner.close();
//
//	}
//	/*---------------------------------------------------------------------
//	 * Method	
//	 * 
//	 * Purpose: 
//	 * 
//	 * Pre-condition: 	
//	 * 
//	 * Post-condition:	
//	 * 
//	 * Parameters:	
//	 * 
//	 * Returns: 
//	 * 
//	 *---------------------------------------------------------------------*/
//
//	/*---------------------------------------------------------------------
//	 * Method	start
//	 * 
//	 * Purpose: Initialize the root node of the tree. Call the appropriate function
//	 * 			to initialize the hash bucket binary file. Call the appropriate function
//	 * 			to read in the data binary file and begin data processing. Begin querying
//	 * 			after the data structure has been initialized.
//	 * 
//	 * Pre-condition: 	None
//	 * 
//	 * Post-condition:	User input binary file is read and data structure created allowing 
//	 * 					querying to begin
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns: None
//	 * 
//	 *---------------------------------------------------------------------*/
//	private void start() {
//
//		Scanner scanner = new Scanner(System.in);
//		String filepath = scanner.nextLine();
//		this.dataBin = this.initializeDataBinFile(filepath);
//
////		RandomAccessFile binFile = null;
////		scanner.close();
//
//		this.bucketBin = this.initializeBucketBinaryFile();
//		if (this.bucketBin == null) {
//			System.out.println("ERROR: Failed to create bucket binary file");
//			System.exit(-1);
//		}
//
//		// Root node initialized with 10 children who do not have file pointers
//		// nor children
//		this.root = this.initializeRootNode();
//
//		// Set each child to have an offset position in the bin file
//		try {
//			this.setChildrenOffsets(this.root, 0, this.bucketBin.length());
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not get bucket binary file length");
//			System.exit(-1);
//		}
//
//		if (this.root == null || this.bucketBin == null || this.dataBin == null) {
//			System.out.println("ERROR: Failure to initialize");
//			System.exit(-1);
//		}
//
////		System.out.println("Testing tree initialization");
////		System.out.println("Root: " + this.root.toString());
////		for (int i = 0; i < 10; i++) {
////			HashNode temp = root.getChild(i);
////			System.out.println("\t" + temp.toString());
////		}
//
//		this.readInData();
////		System.out.println("[id][offset][value]");
////		this.printBucketBinFile(this.root);
//		this.tempQuery();
//		this.beginQuerying();
//		this.closeFiles();
//
//	}
//
//	public void printBucketBinFile(HashNode node) {
////		if (!node.isLeaf()){
////			
////			// then call for each child
////			for (int i = 0; i < 10; i ++) {
////				this.printBucketBinFile(node.getChild(i));
////			}
////		} else {
////			for (int i = 0; i < node.getDataCount(); i ++) {
////				long position = node.getOffset() + i * this.bucketFileLineSize;
////				try {
////					this.bucketBin.seek(position);
////					int id = this.bucketBin.readInt();
////					long offset = this.bucketBin.readLong();
////					System.out.println(String.format("[%s]\t[%s]\t[%s]", offset, id, node.getValue()));
////				} catch (IOException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////			}
////		}
//		try {
//			long length = this.bucketBin.length() / 12;
//			long location = 0;
//			for (int i = 0; i < length; i++) {
//				this.bucketBin.seek(location);
//
//				int id = this.bucketBin.readInt();
//				long offset = this.bucketBin.readLong();
////				System.out.println(String.format("[%s]\t[%s]\t\t[%s]", location, id, offset));
//				location += 12;
//
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	initializeRootNode
//	 * 
//	 * Purpose:	Initialize the root HashNode and its 10 children by creating
//	 * 			each object and storing in the root ArrayList. Children will 
//	 * 			be assigned values but not file pointers or children yet.
//	 * 
//	 * Pre-condition:	Root has not been initialized because it will be reassigned
//	 * 
//	 * Post-condition:	Root and its 10 initial children are created
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns:	HashNode to be assigned to root
//	 * 
//	 *---------------------------------------------------------------------*/
//	private HashNode initializeRootNode() {
//		HashNode rootNode = new HashNode(null, false, 0);
//		rootNode.setLevel(0);
//		rootNode.createChildren();
//		return rootNode;
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	setChildrenOffsets
//	 * 
//	 * Purpose:	Take a hashnode and two byte locations and evenly assign those
//	 * 			byte locations to each hashnode to create buckets in the binary 
//	 * 			bucket file
//	 * 
//	 * Pre-condition:	this.bucketBin has been initialized. this.root has
//	 * 					been initialized. 
//	 * 
//	 * Post-condition:	
//	 * 
//	 * Parameters:	
//	 * 
//	 * Returns:	
//	 * 
//	 *---------------------------------------------------------------------*/
//	private void setChildrenOffsets(HashNode node, long start, long end) {
//		if ((this.bucketSize * 12 * 10) != (end - start)) {
//			System.out.println("ERROR: Invalid start and end in setChildrenOffsets");
//			System.exit(-1);
//		}
//		long step = this.bucketSize * 12;
//		for (int i = 0; i < 10; i++) {
//			// For each of the 10 offsets in the binary file
//			// assign those offset to the given HashNode
//			HashNode child = node.getChild(i);
//			if (child == null) {
//				System.out.println("ERROR: Tried to get null child.");
//				System.exit(-1);
//			}
//			child.setChildOffset(start + (i * step));
//		}
//
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	initializeBucketBinaryFile
//	 * 
//	 * Purpose:	Initialize the RandomAccessFile used to store the hash bucket 
//	 * 			index-offset pairs. The file is initialized to 
//	 * 			bucketSize [rows per bucket] * 10 [buckets] * 12 [bytes per row]
//	 * 
//	 * Pre-condition:	?
//	 * 
//	 * Post-condition:	bucketBinaryFile is initialized to the required size 
//	 * 					for the initial set of buckets
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns:	RandomAccessFile containing each bucket
//	 * 
//	 *---------------------------------------------------------------------*/
//	private RandomAccessFile initializeBucketBinaryFile() {
//
//		File binFileRef = null; // Used to check if bin file already exists and delete it
//		RandomAccessFile binFile = null;
//		// Check for and delete existing version
//		try {
//			binFileRef = new File("bucket.bin");
//			if (binFileRef.exists())
//				binFileRef.delete();
//		} catch (Exception e) {
//			System.out.println("ERROR: Could not delete previous instance of .bin file");
//			System.exit(-1);
//		}
//
//		// Create and return the bin file
//
//		try {
//			binFile = new RandomAccessFile("bucket.bin", "rw");
//			binFile.setLength(bucketSize * 10 * this.bucketFileLineSize); // Preallocate initial size
//			return binFile;
//		} catch (FileNotFoundException e) {
//			System.out.println("ERROR: FileNotFoundException - Could not generate bucket binary file");
//			System.exit(-1);
//		} catch (IOException e) {
//			System.out.println("ERROR - IOException - Could not set bucket binary file length");
//			System.exit(-1);
//		}
//		return binFile;
//
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	initializeDataBinFile
//	 * 
//	 * Purpose:	Read in the name of the binary file containing the data from 
//	 * 			the command line and open it as a RandomAccessFile in read mode
//	 * 
//	 * Pre-condition:	None
//	 * 
//	 * Post-condition:	The user input data binary file is stored in a RandomAccessFile reference
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns:	Reference to the RandomAccessFile
//	 * 
//	 *---------------------------------------------------------------------*/
//	private RandomAccessFile initializeDataBinFile(String path) {
//		try {
//			RandomAccessFile file = new RandomAccessFile(path, "r");
//			return file;
//		} catch (FileNotFoundException e) {
//			System.out.println("ERROR: Could not find binary file");
//			System.exit(-1);
//		}
//		return null;
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	readInData
//	 * 
//	 * Purpose:	Sequentially iterate through this.dataBin and add the data 
//	 * 			to the tree data structure by appropriately parsing the data 
//	 * 			and adding to the correctHash node. 
//	 * 
//	 * Pre-condition:	this.dataBin and this.hashBin have been intitialized
//	 * 
//	 * Post-condition:	all data in this.dataBin stored in the tree and hashBin
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns:	None
//	 * 
//	 *---------------------------------------------------------------------*/
//	private void readInData() {
//		// Get the number of bytes
//		long totalBytes = 0;
//		try {
//			totalBytes = this.dataBin.length();
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not read binary file length");
//			System.exit(-1);
//		}
//
//		// Move to the end of the file and read the last byte
//		int numberOfFields = -1;
//		try {
//			this.dataBin.seek(totalBytes - 4);
//			numberOfFields = this.dataBin.readInt();
//
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not read last byte of binary file");
//			System.exit(-1);
//		}
//
//		// Read backwards for the number of fields to get the length of each field,
//		// the data length, and the length of each line
//		this.fieldLengths = getFieldLengthsArray(totalBytes, numberOfFields);
//
//		// Data length is total bytes minus int marking number of fields
//		// minus number of ints used for field lengths
//		long dataLengthExcludingHeaders = totalBytes - 4 - numberOfFields * 4;
//		int lineLength = 0;
//		for (int j = 0; j < fieldLengths.length; j++) {
////			System.out.println(fieldLengths[j]);
//			lineLength += fieldLengths[j] == -1 ? 4 : fieldLengths[j];
//		}
//
//		// All this does is print
////		System.out
////				.println(String.format("There are <%s> fields with line length <%s>", fieldLengths.length, lineLength));
//
//		String[] lineStringList = new String[fieldLengths.length];
////		System.out.println(String.format("Data length excluding headers is <%s>", dataLengthExcludingHeaders));
//		for (long l = 0; l < dataLengthExcludingHeaders; l += lineLength) {
//			this.readBinFileLineIntoArrayList(fieldLengths, lineStringList, l);
////			for (int k = 0; k < lineStringList.length; k ++) {
////				System.out.print(String.format("[%s]", lineStringList[k]));
////			}
////			System.out.println();
//			if (lineStringList[0].length() != 0) {
//				this.addToTree(lineStringList, l);
//			}
//		}
//
//		//////////////////
//
////		// Next we iterate through each line of the file and add it to the tree
////		for (int m = 0; m < dataLengthExcludingHeaders; m += lineLength) {
////			this.addLineToTree
////		}
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method	addToTree
//	 * 
//	 * Purpose: Take a line of the data binary file and its position in the 
//	 * 			the data binary file and iterate through the tree to find 
//	 * 			the location where the runner id should be added to the hash
//	 * 			bucket file
//	 * 
//	 * Pre-condition: 	
//	 * 
//	 * Post-condition:	
//	 * 
//	 * Parameters:	
//	 * 
//	 * Returns: 
//	 * 
//	 *---------------------------------------------------------------------*/
////	private void addToTree(String[] lineStringList, long positionInDataBinFile) {
//		// Parse the runner id into an integer. Exit if that fails
////		int level = 0;
////		int id;
////		try {
////			id = Integer.parseInt(lineStringList[0]);
////		} catch (Exception e) {
////			System.out.println("ERROR: Runner id not an int");
////			return;
////		}
////
////		// Set up boolean to track if at leaf, idDigit to track the current digit of the
////		// index,
////		// working backwards, and the current node we are at in the tree
////		System.out.println("Initial id in addToTree is " + id);
////		boolean atLeafNode = false;
////		int idDigit = id % 10;
////		int idToDivide = id;
////		HashNode currentNode = root.getChild(idDigit);
//////		System.out.println(String.format("From id <%d> I got hash node <%s>", id, currentNode.toString()));
////
////		// While we have not yet reached the end of the tree
////		while (!atLeafNode) {
////			level ++;
////			System.out.println("+++ LEVEL IS " + level + " AND id IS " + id + " and IDTODIVIDE is " + idToDivide);
////
////			if (currentNode.isLeaf()) { // If we reached a leaf node stop iterating
////				System.out.println("A for idToDivide " + idToDivide);
////				atLeafNode = true;
////				if (currentNode.getDataCount() == this.bucketSize) { // Resize if the tree if full
////					System.out.println("B");
////
////					System.out.println("Supposed to be resizing");
////
//////					level++;
////					System.out
////							.println(String.format("CALLING RESIZE FOR level = <%s> and id = <%s> and currentNode <%s>",
////									currentNode.getLevel(), id, currentNode.toString()));
////					this.resizeNode(currentNode, id, level);
////					atLeafNode = false;
////					idToDivide = idToDivide / 10;
////					idDigit = idToDivide % 10;
////					currentNode = currentNode.getChild(idDigit);
////
////				}
////			} else {
////				System.out.println("C");
////
////				// Otherwise move to the next digit in the runner id and the next level of the
////				// tree.
//////				System.out.println("Not at leaf node ");
////				// STILL NEED TO IMPLEMENT THIS
////				idToDivide = idToDivide / 10;
////				idDigit = idToDivide % 10;
////				currentNode = currentNode.getChild(idDigit);
//////				level++;
////
////			}
////
//////			atLeafNode = true;
////			System.out.println("Looping");
////
////		}
////
////		// At this point a leaf node has been reached. Only leaf nodes store actual
////		// references
////		// to elements in the hash bucket binary file.
////
////		// If the current node is full we need to resize it and assign current node to
////		// the
////		// node that should contain the new data
////
////		// then we get the offset of current node in the hashBin file
////		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
////
//////		System.out.println(String.format("Adding id <%s> at position <%s>", id, positionInBinFile));
////
////		// Then we seek to this position and write the id and the position in the data
////		// binary file
////		System.out.println("Writing " + id + " at position " + positionInBinFile + " with node of count"
////				+ currentNode.getDataCount());
////		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
////		// Start at the end of the runner id and find that value in the
////		// First level of the tree.
////		// If that hashnode has bucketSize elements then the bucket is full
////		// If not full we skip to the correct position in the hash bucket file
////		// using the number of elements in the hashnode times the length of
////		// each line and starting at the offset of the bucket/
////		// Then we write the runner id and positionInDataBin file to the hash
////		// bucket file
////		// If the hashbucket is full we call resize on the node
////
////	}
//	
//	private void addToTree(String[] lineStringList, long positionInDataBinFile) {
//		// Parse the runner id into an integer. Exit if that fails
//		int level = 1;
//		int id;
//		try {
//			id = Integer.parseInt(lineStringList[0]);
//		} catch (Exception e) {
//			System.out.println("ERROR: Runner id not an int");
//			return;
//		}
//
//		// Set up boolean to track if at leaf, idDigit to track the current digit of the index,
//		// working backwards, and the current node we are at in the tree
////		System.out.println("Initial id in addToTree is " + id);
//		boolean atLeafNode = false;
//		int idDigit = id % 10;
//		int idToDivide = id;
//		HashNode currentNode = root.getChild(idDigit);
////		System.out.println(String.format("From id <%d> I got hash node <%s>", id, currentNode.toString()));
//
//		// While we have not yet reached the end of the tree
//		while (!atLeafNode) {
//			
//			
//			if (currentNode.isLeaf()) { // If we reached a leaf node stop iterating
////				System.out.println("A for idToDivide " + idToDivide);
//				atLeafNode = true;
//				if (currentNode.getDataCount() == this.bucketSize) { // Resize if the tree if full
////					System.out.println("B");
//
////					System.out.println("Supposed to be resizing");
//					
////					System.out.println(String.format("Calling resize for id <%s>, idToDivid<%s>, digit <%s>, and node<%s>", 
////							id, idToDivide, idDigit, currentNode.toString()));
//					this.resizeNode(currentNode, id, level + 1);
////					this.printBucketBinFile(root);
//					atLeafNode = false;
//					idToDivide = idToDivide / 10;
//					idDigit = idToDivide % 10;
//					currentNode = currentNode.getChild(idDigit);
//
//				}
//			} else {
////				System.out.println("C " + currentNode.toString());
//
//				// Otherwise move to the next digit in the runner id and the next level of the
//				// tree.
////				System.out.println("Not at leaf node ");
//				// STILL NEED TO IMPLEMENT THIS
//				idToDivide = idToDivide / 10;
//				idDigit = idToDivide % 10;
//				currentNode = currentNode.getChild(idDigit);
////				level++;
//
//			}
//			level ++;
////			atLeafNode = true;
////			System.out.println("Looping");
//
//		}
//
//		// At this point a leaf node has been reached. Only leaf nodes store actual
//		// references
//		// to elements in the hash bucket binary file.
//
//		// If the current node is full we need to resize it and assign current node to
//		// the
//		// node that should contain the new data
//
//		// then we get the offset of current node in the hashBin file
//		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
//
////		System.out.println(String.format("Adding id <%s> at position <%s>", id, positionInBinFile));
//
//		// Then we seek to this position and write the id and the position in the data
//		// binary file
////		System.out.println("Writing " + id + " at position " + positionInBinFile + " with node of count"
////				+ currentNode.getDataCount());
//		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
//		// Start at the end of the runner id and find that value in the
//		// First level of the tree.
//		// If that hashnode has bucketSize elements then the bucket is full
//		// If not full we skip to the correct position in the hash bucket file
//		// using the number of elements in the hashnode times the length of
//		// each line and starting at the offset of the bucket/
//		// Then we write the runner id and positionInDataBin file to the hash
//		// bucket file
//		// If the hashbucket is full we call resize on the node
//
//	}
//
//	private void writeIdAndOffsetToBinFile(long positionInDataBinFile, int id, HashNode currentNode,
//			long positionInBinFile) {
////		System.out.println(String.format("writeIdAndOffsetToBinFile called for id=<%s>. currentNode=<%s>", id,
////				currentNode.toString()));
//		try {
//			this.bucketBin.seek(positionInBinFile);
//			this.bucketBin.writeInt(id);
//			this.bucketBin.writeLong(positionInDataBinFile);
//			currentNode.incrDataCount();
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not write to bucket binary file");
//			System.exit(-1);
//		}
//	}
//
////	private void resizeNode(HashNode node, int id, int level) {
////		String idString = String.valueOf(id);
////		if (level > idString.length()) {
//////			level = idString.length() -1;
////			this.printBucketBinFile(root);
////			System.out.println(String.format("ERROR: Invalid index in runner id for level <%s> and idString <%s>",
////					level, idString));
////			System.exit(-1);
////		}
////
////		// TODO Auto-generated method stub
////		// Iterate through every element in the current bin file from this node and sort
////		// it
////		// into 10 array lists
////		// Would be nice to keep track of the level
////		// Consider the case where we run out of positions
////		// Create 10 children for the current node and set it as not a
////		// a leaf
////		// Seek the end of the binary file.
////		// Extend the end of the binary file
////		// Assign the 10 slots to the new 10 leaf nodes
////		// Iterate through each sorted array list
////		// Add the elements to the appropriate bucket.
////		// Want to make sure no bucket is already full
////		// Cover this case where say, we have 2 elemens that end in the same two digits
////		// and add
////		// another element that is included in theese last two element. If we resize we
////		// will
////		// run in to the same issue and need to resize again
////		// Probably should check that we do not have the same id's at any point as this
////		// would
////		// cause an issue. Maybe print a warning. Maybe do nothing
////		// Then we add the new element which should be existing code
////		System.out.println(
////				String.format("Calling resize for elem <%s> on level <%s> for node <%s>", id, level, node.toString()));
////
////		// ArrayList to contain each id and offset from the portion of the has bucket
////		// file
////		// that are being resized
////		ArrayList<ValueOffsetPair> dataFromHashBin = new ArrayList<>(this.bucketSize);
////		// Get the offset of the node and how many elements it references
////		long currentOffset = node.getOffset();
////		int elemInNode = node.getDataCount();
////		int idFromHashBin;
////		long offsetFromHashBin;
////		try {
////			// Move to each of those elements and for each one, read the data from the
////			// binary
////			// file and store in in dataFromHashBin
////			this.bucketBin.seek(currentOffset);
////			for (int i = 0; i < elemInNode; i++) {
////				idFromHashBin = this.bucketBin.readInt();
////				offsetFromHashBin = this.bucketBin.readLong();
////				dataFromHashBin.add(new ValueOffsetPair(idFromHashBin, offsetFromHashBin));
////
////			}
////		} catch (Exception e) {
////			System.out.println("ERROR: Could not read from hash bucket binary file while resizing");
////		}
////
////		// Print out the elements that are being resized
////		System.out.println("Resizing the following elements");
////		for (ValueOffsetPair pair : dataFromHashBin) {
////			System.out.println(String.format("\t<%s><%s>", pair.getIndex(), pair.getOffset()));
////		}
////
////		// Set the node to empty as it not longer is a leaf and create its 10 children
////		node.setEmpty();
////		node.createChildren(node.getLevel() + 1);
////		System.out.println(String.format("After finding elements to store we have node as <%s>", node.toString()));
////
////		// Initialize variables to mark the start and end bytes of the children in the
////		// hash bucket binary file
////		long childrenStart = 0;
////		long childrenEnd = 0;
////		try {
////			// Find the start and end locations of these 10 children and seek to that
////			// location
////			childrenStart = this.bucketBin.length();
////			childrenEnd = childrenStart + bucketSize * 10 * this.bucketFileLineSize;
////			this.bucketBin.seek(childrenEnd);
////			System.out.println("Resizing the file to have " + childrenEnd + " bytes from " + childrenStart + " bytes");
////
////		} catch (IOException e) {
////			System.out.println("ERROR: Could not extend hash bucket binary file");
////			System.exit(-1);
//////			e.printStackTrace();
////		}
////		// Create the children offsets in this location
////		this.setChildrenOffsets(node, childrenStart, childrenEnd);
////
////		// For each child write their info from dataFromHashBin into the hash bucket
////		// binary file
////
////		System.out.println("We are looking at this digit in the id's workign from the back " + level);
////		System.out.println("Checking that new offsets have been correctly made");
////		for (int i = 0; i < 10; i++) {
////			HashNode temp = node.getChild(i);
////			System.out.println("\t" + temp.toString());
////
////		}
////
//////		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
//////		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
////		int substringStart;
////		String indexString;
////		HashNode leafNodeToAddTo;
////		long positionInBinFile;
////
////		for (ValueOffsetPair p : dataFromHashBin) {
////			// Get the id from the pair, convert it to String and get the digit we are one
////			// using
////			// level then convert that back to an int
////			indexString = String.valueOf(p.getIndex());
////			System.out.println("Level = " + level);
////			substringStart = indexString.length() - level;
////			String relevantDigitString = null;
////			try {
////				relevantDigitString = indexString.substring(substringStart, substringStart + 1);
////			} catch (Exception e) {
////				this.printBucketBinFile(root);
////				System.out.println(
////						String.format("ERROR: Could not call substring for <%s> with index <%s> and level <%s> and current node <%s>",
////								indexString, substringStart, level, node.toString()));
////				e.printStackTrace();
////				System.exit(-1);
////			}
////			int relevantDigit = Integer.parseInt(relevantDigitString);
////			System.out.println(String.format("For <%s> being resized we are looking at index <%s> or digit <%s>",
////					indexString, substringStart, relevantDigitString));
////
////			// Then get the node we are adding to using that digit and
////			// find the position we should add our data to in the hash bucket bin file
////			// and add it using writeIdAndOffsetToBinFile
////			leafNodeToAddTo = node.getChild(relevantDigit);
////			System.out.println("For this string got node " + leafNodeToAddTo.toString());
////			positionInBinFile = leafNodeToAddTo.getOffset() + (12 * leafNodeToAddTo.getDataCount());
////			System.out.println("Writing " + p.getIndex() + " and " + p.getOffset());
////			writeIdAndOffsetToBinFile(p.getOffset(), p.getIndex(), leafNodeToAddTo, positionInBinFile);
////		}
////
////	}
//	
//	private void resizeNode(HashNode node, int id, int level) {
//		String idString = String.valueOf(id);
//		if (level > idString.length()) {
////			level = idString.length() -1;
////			this.printBucketBinFile(root);
//			System.out.println(String.format("ERROR: Invalid index in runner id for level <%s> and idString <%s>", level, idString));
//			System.exit(-1);
//		}
//		
//		// TODO Auto-generated method stub
//		// Iterate through every element in the current bin file from this node and sort
//		// it
//		// into 10 array lists
//		// Would be nice to keep track of the level
//		// Consider the case where we run out of positions
//		// Create 10 children for the current node and set it as not a
//		// a leaf
//		// Seek the end of the binary file.
//		// Extend the end of the binary file
//		// Assign the 10 slots to the new 10 leaf nodes
//		// Iterate through each sorted array list
//		// Add the elements to the appropriate bucket.
//		// Want to make sure no bucket is already full
//		// Cover this case where say, we have 2 elemens that end in the same two digits
//		// and add
//		// another element that is included in theese last two element. If we resize we
//		// will
//		// run in to the same issue and need to resize again
//		// Probably should check that we do not have the same id's at any point as this
//		// would
//		// cause an issue. Maybe print a warning. Maybe do nothing
//		// Then we add the new element which should be existing code
////		System.out.println(
////				String.format("Calling resize for elem <%s> on level <%s> for node <%s>", id, level, node.toString()));
//
//		// ArrayList to contain each id and offset from the portion of the has bucket
//		// file
//		// that are being resized
//		ArrayList<ValueOffsetPair> dataFromHashBin = new ArrayList<>(this.bucketSize);
//		// Get the offset of the node and how many elements it references
//		long currentOffset = node.getOffset();
//		int elemInNode = node.getDataCount();
//		int idFromHashBin;
//		long offsetFromHashBin;
//		try {
//			// Move to each of those elements and for each one, read the data from the
//			// binary
//			// file and store in in dataFromHashBin
//			this.bucketBin.seek(currentOffset);
//			for (int i = 0; i < elemInNode; i++) {
//				idFromHashBin = this.bucketBin.readInt();
//				offsetFromHashBin = this.bucketBin.readLong();
//				dataFromHashBin.add(new ValueOffsetPair(idFromHashBin, offsetFromHashBin));
//
//			}
//		} catch (Exception e) {
//			System.out.println("ERROR: Could not read from hash bucket binary file while resizing");
//		}
//
//		// Print out the elements that are being resized
////		System.out.println("Resizing the following elements");
////		for (ValueOffsetPair pair : dataFromHashBin) {
////			System.out.println(String.format("\t<%s><%s>", pair.getIndex(), pair.getOffset()));
////		}
//
//		// Set the node to empty as it not longer is a leaf and create its 10 children
//		node.setEmpty();
//		node.createChildren();
////		System.out.println(String.format("After finding elements to store we have node as <%s>", node.toString()));
//
//		// Initialize variables to mark the start and end bytes of the children in the
//		// hash bucket binary file
//		long childrenStart = 0;
//		long childrenEnd = 0;
//		try {
//			// Find the start and end locations of these 10 children and seek to that
//			// location
//			childrenStart = this.bucketBin.length();
//			childrenEnd = childrenStart + bucketSize * 10 * this.bucketFileLineSize;
//			this.bucketBin.seek(childrenEnd);
////			System.out.println("Resizing the file to have " + childrenEnd + " bytes from " + childrenStart + " bytes");
//
//		} catch (IOException e) {
//			System.out.println("ERROR: Could not extend hash bucket binary file");
//			System.exit(-1);
////			e.printStackTrace();
//		}
//		// Create the children offsets in this location
//		this.setChildrenOffsets(node, childrenStart, childrenEnd);
//
//		// For each child write their info from dataFromHashBin into the hash bucket
//		// binary file
//
////		System.out.println("We are looking at this digit in the id's workign from the back " + level);
////		System.out.println("Checking that new offsets have been correctly made");
////		for (int i = 0; i < 10; i++) {
////			HashNode temp = node.getChild(i);
////			System.out.println("\t" + temp.toString());
////
////		}
//
////		long positionInBinFile = currentNode.getOffset() + (12 * currentNode.getDataCount());
////		writeIdAndOffsetToBinFile(positionInDataBinFile, id, currentNode, positionInBinFile);
//		int substringStart;
//		String indexString;
//		HashNode leafNodeToAddTo;
//		long positionInBinFile;
//
//		for (ValueOffsetPair p : dataFromHashBin) {
//			// Get the id from the pair, convert it to String and get the digit we are one
//			// using
//			// level then convert that back to an int
//			indexString = String.valueOf(p.getIndex());
//			substringStart = indexString.length() - level;
//			String relevantDigitString = null;
//			try {
//				relevantDigitString = indexString.substring(substringStart, substringStart + 1);
//			} catch (Exception e) {
////				this.printBucketBinFile(root);
//				System.out.println(String.format("ERROR: Could not call substring for <%s> with index <%s> and level <%s>",
//						indexString, substringStart, level));
//				e.printStackTrace();
//				System.exit(-1);
//			}
//			int relevantDigit = Integer.parseInt(relevantDigitString);
////			System.out.println(String.format("For <%s> being resized we are looking at index <%s> or digit <%s>",
////					indexString, substringStart, relevantDigitString));
//
//			// Then get the node we are adding to using that digit and
//			// find the position we should add our data to in the hash bucket bin file
//			// and add it using writeIdAndOffsetToBinFile
//			leafNodeToAddTo = node.getChild(relevantDigit);
////			System.out.println("For this string got node " + leafNodeToAddTo.toString());
//			positionInBinFile = leafNodeToAddTo.getOffset() + (12 * leafNodeToAddTo.getDataCount());
////			System.out.println("Writing " + p.getIndex() + " and " + p.getOffset());
//			writeIdAndOffsetToBinFile(p.getOffset(), p.getIndex(), leafNodeToAddTo, positionInBinFile);
//		}
//
//	}
//
//	private void tempQuery() {
//		// Initialize the scanner and necessary variables
//		Scanner scanner = new Scanner(System.in);
//		boolean looping = true;
//		String query;
//		int runnerId = 0;
//		boolean validInt = true;
//		System.out.println("\nEnter a runnerId to search, or enter -1 to exit the program");
//		// Loop until user enters -1
//		while (looping) {
//			query = scanner.nextLine();
//
//			try {
//				// Parse the int from the query or exit if it fails
//				try {
//					runnerId = Integer.parseInt(query);
//					validInt = true;
//				} catch (Exception e) {
//					System.out.println("Please enter an integer");
//					validInt = false;
//				}
//				if (validInt) {
//					if (runnerId == -1) {
//						looping = false;
//					} else {
//						// CHANGE THIS SECTION
//						int temp = runnerId;
//						int digit = temp % 10;
//						int length = query.length();
//						boolean found = false;
//						HashNode currentNode = root.getChild(digit);
//						while (!found) {
//							// If we reached a leaf node then we just
//							// print that node
////							System.out.println("In query inner loop");
//							// If we reached the end of the query but not a leaf
//							// node then we just print all leaf nodes attached
//							if (length == 0) {
//								found = true;
//								this.printNodeResults(runnerId, currentNode);
//							}
//							if (currentNode.isLeaf()) {
//								found = true;
//								this.printNodeResults(runnerId, currentNode);
//
//							} else {
//								temp = temp / 10;
//								digit = temp % 10;
//								length--;
//								currentNode = currentNode.getChild(digit);
//
//							}
//
//						}
//						///////////
//					}
//				}
//
//			} catch (Exception e) {
//				System.out.println("Record not found");
////				e.printStackTrace();
//			}
//
//		}
//		scanner.close();
//
//	}
//
//	private void printNodeResults(int query, HashNode node) {
////		System.out.println("printNodeResults called");
//		if (node.isLeaf()) {
////			this.printNodeResultsHelper(query, node);
////			System.out.println("printNodeResultsHelper called");
//
//			ArrayList<ValueOffsetPair> listOfResults = new ArrayList<ValueOffsetPair>(100);
////			long[] listOfRes = new long[100];
//			ArrayList<Long> listOfRes = new ArrayList<Long>(100);
//			int k = 0;
//			int id;
//			String queryString = String.valueOf(query);
//			String idString;
//			long indexInDataBin;
//			long offset = node.getOffset();
//			// Print each
//			for (int i = 0; i < node.getDataCount(); i++) {
////				System.out.println("Loop A");
//				try {
//					this.bucketBin.seek(offset);
//					id = this.bucketBin.readInt();
////					offset += 4;
//					indexInDataBin = this.bucketBin.readLong();
//					idString = String.valueOf(id);
////					System.out.println("queryString: " + queryString);
////					System.out.println("idString: " + idString);
//
//					if (idString.endsWith(queryString)) {
////						listOfResults.add(new ValueOffsetPair(id, indexInDataBin));
////						listOfRes[k] = indexInDataBin;
//						listOfRes.add(indexInDataBin);
//						k++;
//					}
//
////					System.out.println(String.format("[%s][%s]", id, indexInDataBin));
//
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				offset += this.bucketFileLineSize;
//
//			}
//
//			this.printPositionArray(listOfRes);
//
//		}
//
//	}
//
//	private void printNodeResultsHelper(int query, HashNode node) {
////		System.out.println("printNodeResultsHelper called");
//
//		ArrayList<ValueOffsetPair> listOfResults = new ArrayList<ValueOffsetPair>(100);
////		long[] listOfRes = new long[100];
//		ArrayList<Long> listOfRes = new ArrayList<Long>(100);
//		int k = 0;
//		int id;
//		String queryString = String.valueOf(query);
//		String idString;
//		long indexInDataBin;
//		long offset = node.getOffset();
//		// Print each
//		for (int i = 0; i < node.getDataCount(); i++) {
////			System.out.println("Loop A");
//			try {
//				this.bucketBin.seek(offset);
//				id = this.bucketBin.readInt();
////				offset += 4;
//				indexInDataBin = this.bucketBin.readLong();
//				idString = String.valueOf(id);
////				System.out.println("queryString: " + queryString);
////				System.out.println("idString: " + idString);
//
//				if (idString.endsWith(queryString)) {
////					listOfResults.add(new ValueOffsetPair(id, indexInDataBin));
////					listOfRes[k] = indexInDataBin;
//					listOfRes.add(indexInDataBin);
//					k++;
//				}
//
////				System.out.println(String.format("[%s][%s]", id, indexInDataBin));
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			offset += this.bucketFileLineSize;
//
//		}
//
//		this.printPositionArray(listOfRes);
//
//	}
//
//	private void printPositionArray(ArrayList<Long> arr) {
//		String[] lineStringArr = new String[this.fieldLengths.length];
//		for (int i = 0; i < arr.size(); i++) {
////			System.out.println("Loop B - " + arr.size());
//
//			this.readBinFileLineIntoArrayList(this.fieldLengths, lineStringArr, arr.get(i));
//			for (int k = 0; k < lineStringArr.length; k++) {
//
//				System.out.print(String.format("[%s]", lineStringArr[k]));
//
//			}
//			System.out.println(); // KEEP THIS FOR ADDING NEW LINE TO OUTPUT
//		}
//	}
//
//	/*---------------------------------------------------------------------
//	|  Method getFieldLengthsArray
//	|
//	|  Purpose:	Read the binary file backwards from the last line, where 
//	|			the last 4 bytes of the file is the number of fields, allowing
//	|			this method to know the number of bytes to read backwards
//	|			to get the length of each field. The lengths of each field are 
//	|			placed into an int[] array. 
//	|
//	|  Pre-condition:	binFile is opened, numberOfRecords is the number 
//	|					of bytes in the entire file, and numberOfFields
//	|					is the last int of the file which tells this function
//	|					how many times to iterate
//	|
//	|  Post-condition:	The byte lengths of each field is read into an int[] array
//	|					and returned. Furthermore, -1 is stored in the array 
//	|					for fields that are integers.
//	|
//	|  Parameters:
//	|			binFile -- opened binary file to use seek on
//	|			numberOfRecords - Number of bytes in the file
//	|			numberOfFields - Number of ints to rread from the last line
//	|
//	|  Returns:  int[] array of field lengths 
//	*-------------------------------------------------------------------*/
//	private int[] getFieldLengthsArray(long numberOfRecords, int numberOfFields) {
//		int[] fieldLengths = new int[numberOfFields];
//		int i = numberOfFields;
//		long pos = numberOfRecords - 8;
//		while (i > 0) {
//
//			try {
//				this.dataBin.seek(pos);
//				fieldLengths[i - 1] = this.dataBin.readInt();
//			} catch (IOException e) {
//				System.out.println("ERROR: Could not read field lengths from binary file");
//				System.exit(-1);
//			}
//
//			pos -= 4;
//			i--;
//		}
//		return fieldLengths;
//	}
//
//	/*---------------------------------------------------------------------
//	|  Method readBinFileLineIntoArrayList
//	|
//	|  Purpose:	Take the bin file and an array to place the contents of the bin
//	|			file into and read the number of bytes equivalent to the 
//	|			lengths in fieldLength int[] array. If a -1 in field lengths 
//	|			then 4 bytes are read, else the number of bytes in field lengths is read.
//	|			This data is either read using readInt() or by creating 
//	|			a byte array and converting it to a String. 
//	|
//	|  Pre-condition:	The binary file is open and lineStringList has been
//	|					establish such that each field in the line can 
//	|					be stored in it.
//	|
//	|  Post-condition:	lineStringList is updated to contain each field 
//	|					of the line given by position. 
//	|
//	|  Parameters:
//	|			binFile - the opened RandomAccessFile to read from
//	|			fieldLengths - an int array containing the length of each field in 
//	|				the line, with -1 representing ints, and all other values 
//	|				representing strings.
//	|			lineStringList - The established array to store the line info in
//	|			position - a long marking the byte location of the line to read
//	|				from in the file.
//	|
//	|  Returns:  void
//	*-------------------------------------------------------------------*/
//	private void readBinFileLineIntoArrayList(int[] fieldLengths, String[] lineStringList, long position) {
////		System.out.println(String.format("Position: <%s>", position));
//		try {
//			this.dataBin.seek(position);
//			byte[] lineBytes = null;
//			for (int k = 0; k < fieldLengths.length; k++) {
//				if (fieldLengths[k] == -1) {
//					lineStringList[k] = Integer.toString(this.dataBin.readInt());
//				} else {
//					lineBytes = new byte[fieldLengths[k]];
//					this.dataBin.readFully(lineBytes);
//					lineStringList[k] = new String(lineBytes);
//				}
//
//			}
//
//		} catch (Exception e) {
//			System.out.println("ERROR: Could not parse the binary file line");
//			e.printStackTrace();
//			System.exit(-1);
//
//		}
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	beginQuerying
//	 * 
//	 * Purpose:	Prompt the user to begin inputing queries allowing them 
//	 * 			to search for data using the query as a suffix. For example
//	 * 			5 would return all records that end in 5 but 51295 would return
//	 * 			all queries that end in 51295. Querying ends when the 
//	 *			user inputs -1
//	 * 
//	 * Pre-condition:	this.dataBin, this.hashBin, and the tree structure 
//	 * 					have all been initialized with all of the data
//	 * 
//	 * Post-condition:	Queries are done according to user termination
//	 * 
//	 * Parameters:	None
//	 * 
//	 * Returns:	None
//	 * 
//	 *---------------------------------------------------------------------*/
//	private void beginQuerying() {
//		// TODO Auto-generated method stub
//
//	}
//
//	/*---------------------------------------------------------------------
//	 * Method 	
//	 * 
//	 * Purpose:	
//	 * 
//	 * Pre-condition:	
//	 * 
//	 * Post-condition:	
//	 * 
//	 * Parameters:	
//	 * 
//	 * Returns:	
//	 * 
//	 *---------------------------------------------------------------------*/
//	private void closeFiles() {
//		// TODO Auto-generated method stub
//
//	}
//
//}
