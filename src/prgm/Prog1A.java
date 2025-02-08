/*=============================================================================
 |   Assignment:  	Program #1A
 |       Author:  	Steven George stevengeorge@arizona.edu
 |					Also credit to Dr. Lester McCann for IO example used as basis at times
 |
 |       Course:  	CSC 460 Database Design Spring 2025
 |   Instructor:  	Dr. Lester McCann
 | 		   TA's:  	Xinyu (Joyce) Guo, Jianwei (James) Shen
 |	   Due Date:  	23 January 2025 at the start of class
 |
 |     Language:  	Java (Java SE 16.0.2)
 |     Packages:  	java.io
 |					java.util.*
 |  Compile/Run: 	Ensure Field.java file included in pwd
 |					JDK:  Compile: javac *.java
 |                        Run: java Prog1A
 |
 +-----------------------------------------------------------------------------
 |  Description:  	This program takes a csv file input of unknown length with 
 |					the first line being the headers and writes it to a binary 
 |					file using the java.io library. Integers are directly written
 |					to the csv file and Strings are padded with spaces to the 
 |					length of the longest string in its respective column.  Filepath
 |					is given as command line argument. 
 |
 |		   Note: 	Regex Description:
 |							 Split on comma
 |					?= means only if the following expression is true
 |	 				[^\"] matches for not a quote after the start of the string
 |	 				pipe means OR
 |	 				\"[^\"]*\" means match if there is a quote \" followed by
 |	 				any amount of characters that are not quotes [^\"]*
 |	 				followed by another quote \". The * means mqtch 0 or more times					 The * at the end means check for 0 or more occurences and the $ means move
 |					to the end
 |					Used this guide: "https://www.w3schools.com/java/java_regex.asp"
 |					lineElements = line.split(",(?=([^\"]|\"[^\"]*\")*$)");
 |
 |					split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)
 |					This one is basically the same but ensures that empty field are kept 
 |					using the negative one and has the non capturing group because it 
 |					is faster
 |
 |
 |
 |        Input:  	A .csv filepath. Expects a csv file containing only ASCII 
 |					characters where each field is either and integer or a String. 
 |					Expects the first line to be the csv headers. Given as command line
 |					argument upon running the program.
 |
 |       Output:  	A .bin file of the same name as the read csv file that contains 
 |					all data in the csv file. 
 |
 |   Techniques:  	1. Use a buffered reader to read each line of the file 
 | 					2. Store the headers from the first line into an ArrayList of 
 | 					Field objects which store the field name, type, and size if a String
 | 					3. Create a bin file to output to
 | 					4. Re-iterate through the csv file using a buffered reader. Split again
 | 					and then write each value to the csv file, padding with spaces for 
 | 					strings using the max string length given in that data value's Field 
 | 					object
 | 
 |
 |
 |   Required Features Not Included:  None
 |
 |   Known Bugs:  None
 |
 *===========================================================================*/
package prgm;

import java.io.*;
import java.util.*;

public class Prog1A {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String filepath = scanner.nextLine();
		scanner.close();
		readCSV(filepath);
		System.out.println("Binary file successfully created");

	}

	/*---------------------------------------------------------------------
	|  Method readCSV
	|
	|  Purpose:	Handles the main functionality and method calls of Prog1A.
	|			Method checks for existence of given file in filepath, reads
	|			in the file using a buffer where each line is split and
	|			used to update max String length for each column. Then the 
	|			binary file is created and the file is re-read, but this time
	|			each data value is written to the binary file using java.io. 
	|
	|  Pre-condition:	A string filepath as been input by the user and fed this
	|					method.
	|
	|  Post-condition:	A binary file is created of the same name as the given csv
	|					file and contains all of the strings and integers in the given
	|					csv file, alongside the header line from the csv file. 
	|
	|  Parameters:
	|		filepath -- The string filepath of the csv file to read. Transferred into method
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/

	private static void readCSV(String filepath) {
		// Establish a reference to the file for useful methods like exists()
		File fileReference = null;
		ArrayList<String[]> fileLineList = new ArrayList<>(1000);

		// Check for presence of the csv file
		try {
			fileReference = new File(filepath);
			if (!fileReference.exists()) {
				System.out.println("ERROR: File not found");
				System.exit(-1);
			}
		} catch (Exception e) {

			System.out.println("ERROR: Failed to detect CSV file");
			System.exit(-1);
		}

		// Read in the file using a buffered reader
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileReference));

			// Get the first line and store values. Do not read as data
			String originalHeader = reader.readLine(); // ERROR CHECK FOR NO HEADER LINE
			if (originalHeader == null) {
				System.out.println("ERROR: Invalid header line and/or empty csv file");
				System.exit(-1);
			}

			// Split the header line on commas. See documentation for the description of the
			// regex
			String[] titles = originalHeader.split(",(?=([^\"]|\"[^\"]*\")*$)");

			int numberFields = titles.length;

			// Establish a list of field objects used to store the name of each field, its
			// type, and its max length if a string
			ArrayList<Field> fieldList = null;

			String line = null;
			// For each file parse the line and update String lengtsh in fieldList
			while ((line = reader.readLine()) != null) {
				if (line.length() != 0) {
					fieldList = parseCSVIntoFieldObjects(titles, numberFields, fieldList, line);
					String[] lineStrings = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

					// Check for lines that end with commas meaning there is missing data
					lineStrings = trimQuotes(lineStrings);
					if (lineStrings[0].length() == 0) {
						lineStrings[0] = "0";
					}
					fileLineList.add(lineStrings);
				}
			}
			if (fieldList == null) {
				System.out.println("ERROR: CSV file contains no data");
				System.exit(-1);
			}

			int sortIndex = 0; // COLUMN INDEX TO SORT BY
			Collections.sort(fileLineList, new LineArrayComparator(sortIndex));

			// Reset the reader and skip the header line of the csv file
			reader = new BufferedReader(new FileReader(fileReference));
			reader.readLine();

			// Set up the binary file. Partial Credit to McCann
			RandomAccessFile binFile = generateEmptyBinFile(fileReference);

			// For each line in the csv file, write the line to the generated bin file
			for (String[] fileLine : fileLineList) {
				writeLineToBinFile(fieldList, fileLine, binFile);

			}

			writeColumnLengthsToBinFile(fieldList, binFile);

			// Close the bin file
			try {

				binFile.close();
			} catch (IOException e) {
				System.out.println("ERROR: Could not close bin file");
				System.exit(-1);
			}

		} catch (Exception e) {
			System.out.println("ERROR: Could not parse file"); // MAKE THIS BETTER
			e.printStackTrace(); // Print full stack trace
			System.exit(-1);
		}

	}

	/*---------------------------------------------------------------------
	|  Method writeColumnLengthsToBinFile
	|
	|  Purpose:	Write the lengths of each column into the given bin file. 
	|			Should be adding at the end of the bin file as the last line.
	|			-1 will be added if it refers to an int column, otherwise 
	|			the length of the string column will be written. Also write the 
	|			number of columns as the very last int of the bin file
	|
	|  Pre-condition:	binFile is open to be written to and field list 
	|					has been properly initialized 
	|
	|  Post-condition:	Bin file contains length of each field in 
	|					bytes as last line of bin file with final int 
	|					representing the number of columns.
	|
	|  Parameters:
	|		fieldList -- ArrayList of Field objects used to write the correct integer 
	|					to the binary file
	|		binFile -- The RandomAccessFile to write to
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void writeColumnLengthsToBinFile(ArrayList<Field> fieldList, RandomAccessFile binFile) {
		for (Field f : fieldList) {
			if (f.isInteger()) {
				try {
					binFile.writeInt(-1);
				} catch (IOException e) {
					System.out.println("ERROR: Could not write field length of int to binary file");
					System.exit(-1);
				}
			} else {
				try {
					binFile.writeInt(f.getMaxLength());

				} catch (IOException e) {
					System.out.println("ERROR: Could not write field length of String to binary file");
					System.exit(-1);
				}

			}
		}
		try {
			binFile.writeInt(fieldList.size());
		} catch (IOException e) {
			System.out.println("ERROR: Could not write number of fields to binary file");
			System.exit(-1);
		}

	}

	/*---------------------------------------------------------------------
	|  Method generateEmptyBinFile
	|
	|  Purpose:	Reduce the complexity of the readCSV method by extracting 
	|			the functionality to create the RandomAccessFile and assign
	|			the bin file the same name as the given CSV file. Handles 
	|			errors and exiting upon bin file creation error. Extracts
	|			the filename from the given filepath in the user terminal input.
	|			Will check for existing bin file and delete it if present
	|
	|  Pre-condition: 	fileReference established to allow access to fileName using 
	|					File getName() method. 
	|
	|  Post-condition: binFile created with same name as csv file given in File fileReference getName()
	|
	|  Parameters:
	|		fileReference -- Reference to the csv file object used to get the name of 
	|							the csv file. Transferred into method
	|
	|  Returns:  RandomAccessFile binFile reference to the created binary file.
	*-------------------------------------------------------------------*/
	private static RandomAccessFile generateEmptyBinFile(File fileReference) {
		File binFileRef = null; // Used to check if bin file already exists and delete it
		RandomAccessFile binFile = null;
		// Check for and delete existing version
		try {
			binFileRef = new File(
					fileReference.getName().substring(0, fileReference.getName().lastIndexOf('.')) + ".bin");
			if (binFileRef.exists())
				binFileRef.delete();
		} catch (Exception e) {
			System.out.println("ERROR: Could not delete previous instance of .bin file");
			System.exit(-1);
		}

		// Create and return the bin file
		try {
			binFile = new RandomAccessFile(binFileRef, "rw");
		} catch (Exception e) {
			System.out.println("ERROR: Failed to create the Random Access File");
			System.exit(-1);

		}
		return binFile;
	}

	/*---------------------------------------------------------------------
	|  Method writeLineToBinFile
	|
	|  Purpose:	Write the given line to the given RandomAccessFile binary file 
	|			by splitting the line using our regular expression described in
	|			external documentation. If a string in the split line is 
	|			less than the length listed in its corresponding Field object 
	|			in fieldList, pad the String with spaces before writing to the 
	|			binary file. Method uses writeInt and writeBtye from the java.io library
	|
	|  Pre-condition: 	fieldList contains initialized Field objets. line not null. 
	|					binFile has been created and opened.
	|
	|  Post-condition: 	binFile contains all elements of given line split using 
	|					our regular expression. fieldList remains unchanged. 
	|
	|  Parameters:
	|		fieldList -- Initialized ArrayList of initialized Field objects containing 
	|						the necessary data to pad all string less than the max String
	|						length in their column. Transferred into method
	|		lineElements - String containing the split line array to add to the bin file
	|		binFile - RandomAccessFile that has been initialized and opened in "rw" mode. 
	|					Transferred into method but accessed outside method after this code.
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void writeLineToBinFile(ArrayList<Field> fieldList, String[] lineElements,
			RandomAccessFile binFile) {
		Field f;
		String elem;
		// for each element in the line, write to the bin file using writeInt if the
		// Field object for the element says it is an int, else pad the String using
		// the length in the corresponding Field object and then call writeByte to
		// add the String to the binary file.

		for (int i = 0; i < lineElements.length; i++) {
			f = fieldList.get(i);
			elem = lineElements[i];

			// If int
			if (f.isInteger()) {
				try {
					if (elem == "") {
						binFile.writeInt(0); // THIS needs some work
					} else {
						binFile.writeInt(Integer.parseInt(elem));
					}
				} catch (Exception e) {

					System.out.println("ERROR: Data recorded as int but was not");
					System.out.println("<" + elem + ">");
					System.exit(-1);
				}
			}

			// If String
			else {

				// Pad using string format
				// https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
				String paddedElem = null;
				if (elem.length() < f.getMaxLength()) {
					paddedElem = String.format("%-" + f.getMaxLength() + "s", elem);
				} else {
					paddedElem = elem;
				}

				try {
					binFile.writeBytes(paddedElem);
				} catch (Exception e) {
					System.out.println("ERROR: Could not write String to bin file");
					System.exit(-1);
				}
			}
		}
	}

	/*---------------------------------------------------------------------
	|  Method ExIntoFieldObjects
	|
	|  Purpose:	For the given line of the csv file, split on commas and 
	|			iterate through each element. For each, use the ArrayList 
	|			of Field objects to check if the element is an int. If not,
	|			then give the String to it's corresponding Field object to 
	|			update the max length of the Strings in that Field object (the 
	|			data column). Nothing will happen if the String is shorter
	|			than the current max string length. This function is called 
	|			on each line of the csv file. This function will construct 
	|			the Field object ArrayList if it is null, meaning the line and 
	|			titles fed to this function on its first call will describe the
	|			format of all data and will cause errors if not matched later.  
	|
	|			
	|
	|  Pre-condition: 	titles is the header column split on commas and contains 
	|					no null values. numberFields matches length of title. 
	|					line contains the unaltered current line of the csv file.
	|
	|  Post-condition:	fieldList is updated to contain Field objects if null, otherwise
	|					fieldList Field objects are updated to contain the most 
	|					recent max string lengths to be used for String padding 
	|					later on
	|
	|  Parameters:
	|			titles -- String[] array containing field titles from first line of 
	|				csv file. Transferred into method
	|			numberFields -- int length of title. Transferred into method. 
	|			fieldList -- ArrayList<Field> used to store Field objects that describe 
	|				each column in titles. Must match ordering of titles. May be null.
	|				Transferring into method but used elsewhere outside this method. 
	|			line -- Current line from CSV file. String. Not split or parsed in any way.
	|					Transfered into this method.
	|
	|  Returns:	ArrayList<Field> fieldList to update the fieldList object after its 
	|			creation in this method.
	*-------------------------------------------------------------------*/
	private static ArrayList<Field> parseCSVIntoFieldObjects(String[] titles, int numberFields,
			ArrayList<Field> fieldList, String line) {

		// See documentation for regex explanation
		String[] lineElements = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

		// Cover case when line start with a comma
		lineElements = trimQuotes(lineElements);

		if (fieldList == null) {
			// Create fieldList if null and establish Field objects
			fieldList = new ArrayList<Field>(numberFields);
			constructFieldArrayList(titles, lineElements, fieldList);
		} else {
			// If the fieldList of Field objects has been created, then, for each
			// field in the current line, if it is a string in the fieldList Field object
			// feed the string to the fieldObject to update the possible max length
			Field f = null;
			for (int i = 0; i < lineElements.length; i++) {
				f = fieldList.get(i);
				if (!(f.isInteger())) {
					f.checkMaxLength(lineElements[i]);
				}

			}
		}

		// Error if invalid number of fields from a line
		if (lineElements.length != numberFields) {
			System.out.println("ERROR: Missing fields in file"); // MAKE THIS BETTER
			System.out.println("Note: This does not mean an empty field");
			System.out.print("Responsible Line: <" + line + ">");
			System.exit(-1);
		}
		return fieldList;
	}

	/*---------------------------------------------------------------------
	|  Method trimQuotes
	|
	|  Purpose:	Take an array of string and return an array where 
	|			any element that was surrounded by quotes is no longer
	|			surrounded by quotes. Creates a new array, does not 
	|			do this in place
	|
	|  Pre-condition: 	strings contains no null values
	|
	|  Post-condition:	strings still exists in its original format but a new 
	|					array of trimmed strings has been created. 
	|
	|  Parameters:	strings -- A String[] array containing elelements to be trimmed
	|			
	|
	|  Returns:	A new String[] array 
	*-------------------------------------------------------------------*/
	private static String[] trimQuotes(String[] strings) {
		String[] trimmedStrings = new String[strings.length];
		String str;
		// For each string
		for (int i = 0; i < strings.length; i++) {
			str = strings[i];
			str = str.strip();
			if (str.length() != 0) {
				// If not null
				try {
					// Remove first quote
					if (str.indexOf("\"") == 0) {
						str = str.substring(1, str.length());

					}
					// Remove last quote
					if (str.lastIndexOf("\"") == str.length() - 1) {
						str = str.substring(0, str.length() - 1);

					}
				} catch (Exception e) {
					System.out.println("ERROR: Could not trim quoutes");
					System.exit(-1);
				}
			}
			// Add to trimmed strings array
			trimmedStrings[i] = str;
		}
		return trimmedStrings;
	}

	/*---------------------------------------------------------------------
	|  Method constructFieldArrayList
	|
	|  Purpose:	Create an ArrayList of Field objects for each respective
	|			field/column in the csv header line. Retains ordering of fields 
	|			as they appear in header file. This ArrayList will be used 
	|			to check if csv values are ints or Strings, and what the correct
	|			padding is for String data value by accessing the max length of that 
	|			String field using the Field object. 
	|
	|  Pre-condition:	All parameters not null. String arrays both retain order
	|					of appearance in csv file. fieldList has been initialized 
	|					to an ArrayList of type Field, ideally of length = title.length
	|
	|  Post-condition: 	ArrayList<Field> fieldList updated to contain initialized Field
	|					objects instead of null values. fields and titles remains 
	|					unchanged. 
	|
	|  Parameters:
	|		titles -- String[] array of each column title that appears in csv file.
	|					  Transferred into this method. 
	|		fields -- String[] array of fields that correspond with titles' Strings by 
	|					index. Transferred into this method. 
	|				For example
	|					titles: "age, birthday, weight"
	|					fields: "25, 1 January 2000, 200"
	|		fieldList -- ArrayList<Field> initialized to length of titles but containing null
	|						values. Transferred into this method. Used elsewhere as well
	|
	|
	|  Returns: void  
	*-------------------------------------------------------------------*/
	private static void constructFieldArrayList(String[] titles, String[] fields, ArrayList<Field> fieldList) {
		// Check for consistent number of elements in titles and fields
		if (titles.length != fields.length) {
			System.out.println("ERROR: Inconsistent header vs data length");
			System.exit(-1);
		}

		// Create Field object reference for iteration
		Field fieldObj = null;
		boolean isInt = false;

		// For each title, check if the corresponding field is an int. If yes
		// set isInt to true. Then create Field object and add to fieldList ArrayList
		for (int i = 0; i < titles.length; i++) {
			try {
				Integer.parseInt(fields[i]);
				isInt = true;
			} catch (Exception e) {
				isInt = false;

			}
			fieldObj = new Field(titles[i], isInt, fields[i]);
			fieldList.add(fieldObj);
		}

	}

}
