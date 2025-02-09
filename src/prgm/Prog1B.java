/*=============================================================================
 |   Assignment:  	Program #1B
 |       Author:  	Steven George stevengeorge@arizona.edu
 |					Also credit to Dr. Lester McCann for IO example used as basis at times
 |
 |       Course:  	CSC 460 Database Design Spring 2025
 |   Instructor:  	Dr. Lester McCann
 | 		   TA's:  	Xinyu (Joyce) Guo, Jianwei (James) Shen
 |	   Due Date:  	30 January 2025 at the start of class
 |
 |     Language:  	Java (Java SE 16.0.2)
 |     Packages:  	java.io
 |					java.util.*
 |  Compile/Run: 	Ensure Field.java file included in pwd. Ensure .bin file exists by running part A
 |					JDK:  Compile: javac *.java
 |                        Run: 
 |
 +-----------------------------------------------------------------------------
 |  Description:  	This program takes a binary file constructed by Part A and
 |					reads the file such that the user is given the first five, middle
 |					5, and last 5 lines of the file through the console. Then, the number
 |					of records is printed. Then the top ten bib numbers are printed 
 |					after the bin file is searched for these value. Lastly a query loop
 |					is started that allows the user to type in runner id numbers and receive info
 |					information about that runner assuming that they exist.
 |
 |        Input:  	The filepath to the binary .bin file created by part A.
 |
 |       Output:  	See description. Multiple printouts to console and search ability 
 |					through the console. 
 |
 |   Techniques:  	
 |					1. Read the file into a RandomAccessFile
 |					2. From the last line of the file read the number of fields 
 |						and the length of each field. This is integrated into the 
 |						binary file by part A. Use seek() to get to the end of the 
 |						file and work backwards. Use readInt()
 |					3. Print the first five, middle five, and last five lines
 |						by using seek and calculations involving line positons. 
 |						The information about column byte lengths allows the calculation
 |						of the line length of each row in bytes, allowing simple iteration
 |						through the file once the starting position of a row is found.
 |						Use .readFully()
 |					4. Print the number of records using the file length excluding 
 |						the information about columns
 |					5. Iterate through the entire file, pulling the bib number from each line
 |						after reading the line into an Array of strings. Add the ten
 |						highest bib numbers and their corresponding information to a 
 |						sorted list the is printed to the user. Use readInt() and readFully()
 |					6. Create a while loop that prompts the user for runnerId input to search with.
 |						After input checking, an interpolation search is run on the binary file
 |						to find the desired information, or to tell the user it does not exist
 |
 |   Required Features Not Included:  None
 |
 |
 |   Known Bugs:  None
 |
 *===========================================================================*/
package prgm;

import java.io.*;
import java.util.*;

public class Prog1B {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String filepath = scanner.nextLine();
		RandomAccessFile binFile = null;
		readBin(filepath, binFile);
		scanner.close();

	}

	/*---------------------------------------------------------------------
	|  Method readBin
	|
	|  Purpose:	Take the filepath of a binary file and read the contents
	|			as a RandomAccesssFile and create data structure to allow 
	|			querying. This means opening the file, getting its length, 
	|			reading the last 4 bytes to get the number of fields, 
	|			calling a method to build an array of the field lengths
	|			using this info, and calling a method to print all of the
	|			expected information
	|			 
	|
	|  Pre-condition:	filepath is absolute or relative file path to the binary file
	|					binFile is null
	|
	|  Post-condition:	The entirety of the Program1 spec has been completed 
	|					meaning that all req info if printed and querying has occured
	|
	|  Parameters:
	|			filepath -- String filepath to the binary file
	|			binFile -- Unitialized RadomAccessFile reference
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void readBin(String filepath, RandomAccessFile binFile) {

		// Open the bin file
		try {
			binFile = new RandomAccessFile(filepath, "r");
		} catch (Exception e) {
			System.out.println("ERROR: Could not open the binary file");
			System.exit(-1);
		}

		// Get the number of bytes
		long numberOfRecords = 0;
		try {
			numberOfRecords = binFile.length();
		} catch (IOException e) {
			System.out.println("ERROR: Could not read binary file length");
			System.exit(-1);
		}

		// Move to the end of the file and read the last byte
		int numberOfFields = -1;
		try {
			binFile.seek(numberOfRecords - 4);
			numberOfFields = binFile.readInt();

		} catch (IOException e) {
			System.out.println("ERROR: Could not read last byte of binary file");
			System.exit(-1);
		}

		// Read backwards for the number of fields
		int[] fieldLengths = getFieldLengthsArray(binFile, numberOfRecords, numberOfFields);
		long dataLengthExcludingHeaders = numberOfRecords - 1 - numberOfFields;
		for (int j = 0; j < fieldLengths.length; j++) {
			System.out.println(fieldLengths[j]);
		}
		int lineLengthBytes = getLineLengthInBytes(fieldLengths);
		System.out
		.println(String.format("There are <%s> fields with line length <%s>", fieldLengths.length, lineLengthBytes));

		handleExpectedPrintedLines(binFile, fieldLengths, lineLengthBytes, dataLengthExcludingHeaders);

	}

	/*---------------------------------------------------------------------
	|  Method handleExpectedPrintedLines
	|
	|  Purpose:	Call the necessary methods to print out the information 
	|			listed in the project spec. This means the first 5 lines,
	|			middle 5 lines, and last 5 lines are printed. Then the total
	|			number of records is printed.  Then the top 
	|			10 bib numbers are printed in descending order, then querying begins.
	|	
	|
	|  Pre-condition:	binFile has been opened in read mode
	|					fieldLengths has been initialized
	|
	|  Post-condition:	All desired information is printed and querying too. 
	|					The bin file is closed after this function
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLengthBytes --  The byte length of a line in the bin file
	|			dataLEngth -- The number of data bytes in the file
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void handleExpectedPrintedLines(RandomAccessFile binFile, int[] fieldLengths, int lineLengthBytes,
			long dataLength) {
		int[] indexesToPrint = { 0, 1, 3, 8 };
		printFirstFiveLines(binFile, fieldLengths, lineLengthBytes, dataLength, indexesToPrint);
		printMiddleFiveLines(binFile, fieldLengths, lineLengthBytes, dataLength, indexesToPrint);
		printLastFiveLines(binFile, fieldLengths, lineLengthBytes, dataLength, indexesToPrint);
		displayTotalNumberOfRecords(dataLength, lineLengthBytes);
		displayTopTenBibNumbers(binFile, fieldLengths, lineLengthBytes, dataLength);
		beginUserQuerying(binFile, fieldLengths, lineLengthBytes, dataLength, indexesToPrint);
		endProgram(binFile);
	}

	/*---------------------------------------------------------------------
	|  Method getLineLengthInBytes
	|
	|  Purpose:	Take an array of field lengths and convert to the overall 
	|			byte length of each line in the binary file by summing 
	|			up the length of each field, either 4 bytes for ints 
	|			or the length of the string for Strings. ints of value 
	|			-1 in the array are assumed to represent ints
	|
	|  Pre-condition:	fieldLengths has been initialized with -1 representing int
	|					lengths. 
	|
	|  Post-condition:	Will provide sum of field lengths under 
	|					assumption that all ints are 4 bytes
	|
	|  Parameters:		
	|				fieldLengths -- int array containing byte 
	|					length of each string in each record
	|					or -1 when a 4 byte int is being referenced. 
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static int getLineLengthInBytes(int[] fieldLengths) {
		int sum = 0;
		for (int i = 0; i < fieldLengths.length; i++) {
			if (fieldLengths[i] == -1) {
				sum += 4;
			} else {
				sum += fieldLengths[i];
			}
		}
		return sum;
	}

	/*---------------------------------------------------------------------
	|  Method displayTopTenBibNumbers
	|
	|  Purpose:	Display to the console the top ten bib numbers in the entire binary
	|			file which is done with a sequential search to find and update 
	|			the top ten bib numbers. This data is printed in descending order
	|			by bib number. 
	|
	|  Pre-condition:	binFile has been opened.
	|					fieldLengths has been properly initialized
	|
	|  Post-condition:	 The top ten bib numbers are printed in descending order 
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLEngth -- The number of data bytes in the file
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void displayTopTenBibNumbers(RandomAccessFile binFile, int[] fieldLengths, int lineLength,
			long dataLength) {

		// Create variables to store key positons
		long position = 0;
		long numberOfLines = dataLength / lineLength;
		int i = 0;

		// Create String array to store the read in line and ArrayList
		// to contain the top ten bib
		String[] lineStringList = new String[fieldLengths.length];
		ArrayList<String[]> topTenBibs = new ArrayList<>(10);

		// Iterate through each line of the binary file that contains relevant data
		while (i < numberOfLines) { // Iterate through each line of the binary file
			try {
				// Read that line
				binFile.seek(position);
				readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, position);

				if (topTenBibs.size() < 10) {
					// If we have not yet searched through ten elements, add the current record
					// to the topTenBibsArrayList
					topTenBibs.add(lineStringList.clone());
				} else {
					// Otherwise, we sort the top ten bibs and check if the current line is greater
					// than the minimum of the bib numbers. If yes we exchange it with the
					// minimum in the topTenBibs arrayList
					Collections.sort(topTenBibs, new LineArrayComparator(2));
					// THIS IS A POINT OF FAILURE. LIKE BIG
					// TIME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					if (Integer.parseInt(lineStringList[2]) > Integer.parseInt(topTenBibs.get(0)[2])) {
						topTenBibs.set(0, lineStringList.clone());
					}
				}

			} catch (IOException e) {
				System.out.println("ERROR: Could not seek while searching for tep ten bibs");
				System.exit(-1);
			}

			i++;
			position += lineLength;
		}
		// Sort the array and reverse for desired printing order
		Collections.sort(topTenBibs, new LineArrayComparator(2));
		Collections.reverse(topTenBibs); // Reverse to print in decsending order

		System.out.println("Top ten bib numbers: [name][bib number]");
		for (String[] s : topTenBibs) {

			String nameToPrint = s[1];
			String bibToPrint = s[2];

			// If any of the desired fields are empty then format them to be null and
			// of the same length as before
			if (nameToPrint.strip().length() == 0) {
				nameToPrint = String.format("%-" + s[1].length() + "s", "null");

			}
			if (bibToPrint.strip().length() == 0) {
				bibToPrint = String.format("%-" + s[2].length() + "s", "null");

			}
			System.out.println("[" + nameToPrint + "][" + bibToPrint + "]");
		}

	}

	/*---------------------------------------------------------------------
	|  Method displayTotalNumberOfRecords
	|
	|  Purpose:	Print the number of records in the file using a byte calculation
	|
	|  Pre-condition:	lineLength is not zero
	|
	|  Post-condition:	 Total number of records is printed. Variables remaind
	|
	|  Parameters:
	|			dataLength -- The number of bytes of data in the file. This excludes the 
	|						field length data and number of fields int
	|			lineLength -- The number of bytes of data in a record
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void displayTotalNumberOfRecords(long dataLength, int lineLength) {
		try {
			System.out.println("\nTotal Number of Records is <" + dataLength / lineLength + ">\n");
		} catch (Exception e) {
			System.out.println("ERROR: Invalid file length. Possible division by zero");
		}
	}

	/*---------------------------------------------------------------------
	|  Method endProgram
	|
	|  Purpose:	Close the binary file and exit if that erros
	|
	|  Pre-condition:	binFile is open
	|
	|  Post-condition:	 binFile is closed
	|
	|  Parameters:
	|		binFile -- opened RandomAccessFile this program has been reading from
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void endProgram(RandomAccessFile binFile) {
		try {
			binFile.close();
		} catch (IOException e) {
			System.out.println("ERROR: Could not close binary file");
			System.exit(-1);
		}

	}

	/*---------------------------------------------------------------------
	|  Method beginUserQuerying
	|
	|  Purpose:	Initialize the process of allowing the user to query the binary 
	|			file for data by using the runnerID as the search key. Keeps a loop running where
	|			Strings are not recognized as valid input, integers are search for, and -1
	|			ends the program. Uses a scanner
	|
	|  Pre-condition:	binFile is opened to read from. Fields lengths has 
	|					been initialized, and indexes to print has been initialized
	|
	|  Post-condition:	The user is continously prompter for input for searches and the 
	|					search results are printed until the user quits the program
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLength -- Number of bytes of data
	|			indexesToPrint -- int array of indexes from the search to print
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void beginUserQuerying(RandomAccessFile binFile, int[] fieldLengths, int lineLength, long dataLength,
			int[] indexesToPrint) {
		// Initialize the scanner and necessary variables
		Scanner scanner = new Scanner(System.in);
		boolean looping = true;
		String query;
		int runnerId = 0;
		long numberOfLines = dataLength / lineLength;
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
						// Use interpolation search to find the result
						String[] result = interpolationSearch(binFile, lineLength, fieldLengths, numberOfLines, query,
								0, numberOfLines - 1);
						String nameToPrint = result[1];
						String timeToPrint = result[8];

						// If name or time to print are empty strings then print null with
						// correct spacing instead
						if (nameToPrint.strip().length() == 0) {
							nameToPrint = String.format("%-" + result[1].length() + "s", "null");

						}
						if (timeToPrint.strip().length() == 0) {
							timeToPrint = String.format("%-" + result[8].length() + "s", "null");

						}

						// Print the result
						System.out.println(
								"[" + result[0] + "][" + nameToPrint + "][" + result[3] + "][" + timeToPrint + "]");
					}}
				

			} catch (Exception e) {
				System.out.println("Record not found");
//				e.printStackTrace();
			}

		}
		scanner.close();

	}

	/*---------------------------------------------------------------------
	|  Method interpolationSearch
	|
	|  Purpose:	See https://en.wikipedia.org/wiki/Interpolation_search
	|			Use line numbers not byte position for the search. 
	|			Check that low and high position are the desired String, if not 
	|			calculate the probe index which is the main way this search is different
	|			from a binary search. Then update the section of the list we are searching
	|			Search for query String at index 0 !! of each line
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					lineLength contains the length of each line in bytes
	|					numberOflines contains the number of records in the file and is not empty;
	|					No null inputs
	|
	|  Post-condition:	The string array containing the info about the discovered line is 
	|					returned, or a not found message is printed and null is returned.
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			numberOfLines -- The number of records in the file
	|			query -- The string to find at index 0 of each line
	|			low -- The inital low line number to start the search at
	|			high -- The intial high line number to start the search at
	|
	|  Returns:  String[] result of search, or null
	*-------------------------------------------------------------------*/
	private static String[] interpolationSearch(RandomAccessFile binFile, int lineLength, int[] fieldLengths,
			long numberOfLines, String query, long low, long high) {

		long probeIndex;
		while (low <= high) {
			// Get the element at the low location
			String lowKey = getBinFileElementAtLineAndIndex(binFile, low, lineLength, 0, fieldLengths);

			// Check if that element is the query
			if (lowKey.compareTo(query) == 0) {
				String[] lineStringList = new String[fieldLengths.length];
				readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, low * lineLength);
				return lineStringList;
			}

			// Get the element at the high location
			String highKey = getBinFileElementAtLineAndIndex(binFile, high, lineLength, 0, fieldLengths);

			// Check if that element is the query
			if (highKey.compareTo(query) == 0) {
//				System.out.println("highKey is <" + highKey + ">");

				String[] lineStringList = new String[fieldLengths.length];
				readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, high * lineLength);
				return lineStringList;
			}

			// Turn specific values into ints for calculation purposes
			// The whole reason I use Strings at all is because
			// this allows the reuse of the readBinFileLineIntoArrayList function
			// which only returns an array of Strings
			int lowKeyInt = Integer.parseInt(lowKey.strip());
			int highKeyInt = Integer.parseInt(highKey.strip());
			int queryInt = Integer.parseInt(query);

			probeIndex = (int) (low + (((queryInt - lowKeyInt) * 1.0 / (highKeyInt - lowKeyInt)) * (high - low)));

			if (probeIndex < 0 || probeIndex >= numberOfLines || low == high) {
//				System.out.println("Record not found");
				low = high + 1; // break the loop condition
			} else {
				// Then we get the key at the probeIndex location
				String keyAtProbeString = getBinFileElementAtLineAndIndex(binFile, probeIndex, lineLength, 0,
						fieldLengths);

				// Then we check if that is the query we are to find
				int keyAtProbeInt = Integer.parseInt(keyAtProbeString.strip());
				if (keyAtProbeInt == queryInt) {
					String[] lineStringList = new String[fieldLengths.length];
					readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, probeIndex * lineLength);
					return lineStringList;
				}

				// If not we compare the query to the keyAtProbeInt
				if (queryInt < keyAtProbeInt) {
					high = probeIndex - 1;
				} else {
					low = probeIndex + 1;
				}

			}

		}
		return null;
	}

	/*---------------------------------------------------------------------
	|  Method getBinFileElementAtLineAndIndex
	|
	|  Purpose:	Used by interpolation search to return the String at indexInLine
	|			of the specified line in the binary file. Acts like telling a dictionary
	|			a key and getting the value. Takes the line number of the record to access
	|			and converts to byte position using lineLength
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					line gives the line number of the file but not the byte position
	|
	|  Post-condition:	 Returns the string at line*linenumber [indexInLine] 
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|
	|  Returns:  String at desired location
	*-------------------------------------------------------------------*/
	private static String getBinFileElementAtLineAndIndex(RandomAccessFile binFile, long line, int lineLength,
			int indexInLine, int[] fieldLengths) {
		String result = "not found";
		try {
			// Calc the position in the binary file and allocate an array
			long position = line * lineLength;
			String[] lineStringList = new String[fieldLengths.length];
			// Read the line into the array and get the desired string
			readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, position);
			result = lineStringList[indexInLine];

		} catch (Exception e) {
			System.out.println("ERROR: Could not seek in binary file for query");
			System.exit(-1);
		}
		return result;
	}

	/*---------------------------------------------------------------------
	|  Method printSpecifiedNumberOfLines
	|
	|  Purpose:	Starting at "position" in the binary file print the number of 
	|			lines given using the indexes of the line to print. Print in the
	|			format: [field1][field ...][field n]
	|			Account for when there are less lines in the file than the given
	|			number of lines to print.
	|
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					lineLength contains the length of each line in bytes
	|					dataLength contains the length of the data only portion of 
	|					the file in bytes. This excludes the line length info at the end
	|					indexesToPrint contains the indexes of the line to display and the 
	|					indexes are in the range 0 to n-1 where n is the number of fields.
	|
	|  Post-condition:	 The expected lines are printed to the console.
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLength -- The byte length of the data in the file excluding field lengths
	|			indexesToPrint -- Valid indexes in the line to print 
	|			numberOfLinesToPrint -- long representing how many lines from the bin file to print
	|			long position -- byte position in the file to start from. Should be the start of a record
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void printSpecifiedNumberOfLines(RandomAccessFile binFile, int[] fieldLengths, int lineLength,
			long dataLength, int[] indexesToPrint, long numberOfLinesToPrint, long position) {
		// Create array to store strings from the line in the bin file
		String[] lineStringList = new String[fieldLengths.length];
		int i = 0;
		// While lines to print and valid position in the file
		while (i < numberOfLinesToPrint && position <= (dataLength - lineLength)) { // ENSURE THIS LOOP CONDITIONS IS
																					// VALID!!!!!!
			try {
				// Read the binary file into lineStringList and then print the array list
				readBinFileLineIntoArrayList(binFile, fieldLengths, lineStringList, position);
				printArrayListIndexes(lineStringList, indexesToPrint);
			} catch (Exception e) {
				System.out.println("ERROR: Could not print specified number of lines");
				System.exit(-1);
			}
			i++;
			position += lineLength;

		}
	}

	/*---------------------------------------------------------------------
	|  Method printFirstFiveLines
	|
	|  Purpose:	Print the first five lines of the bin file, or all of the lines
	|			if there is less than five lines in the file. 
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					lineLength contains the length of each line in bytes
	|					dataLength contains the length of the data only portion of 
	|					the file in bytes. This excludes the line length info at the end
	|					indexesToPrint contains the indexes of the line to display and the 
	|					indexes are in the range 0 to n-1 where n is the number of fields.
	|
	|  Post-condition:	First five lines of the file are printed to the console
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLength -- The byte length of the data in the file excluding field lengths
	|			indexesToPrint -- Valid indexes in the line to print 
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void printFirstFiveLines(RandomAccessFile binFile, int[] fieldLengths, int lineLength,
			long dataLength, int[] indexesToPrint) {
		// Start at first line and print five lines.
		// printSpecifiedNumberOfLines account for fewer lines.
		int position = 0;
		int numberOfLinesToPrint = 5;
		System.out.println("\nFirst 5 Lines:");
		printSpecifiedNumberOfLines(binFile, fieldLengths, lineLength, dataLength, indexesToPrint, numberOfLinesToPrint,
				position);
	}

	/*---------------------------------------------------------------------
	|  Method printMiddleFiveLines
	|
	|  Purpose:	Print the middle five lines of the binary file by finding
	|			the length of the file and calculating the middle. This means 
	|			six lines are printed if the file is of even length. All data will
	|			be printed if less than 5 or 6 lines.
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					lineLength contains the length of each line in bytes
	|					dataLength contains the length of the data only portion of 
	|					the file in bytes. This excludes the line length info at the end
	|					indexesToPrint contains the indexes of the line to display and the 
	|					indexes are in the range 0 to n-1 where n is the number of fields.
	|
	|  Post-condition:	 Middle five or six lines are printed to the console
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLength -- The byte length of the data in the file excluding field lengths
	|			indexesToPrint -- Valid indexes in the line to print 
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void printMiddleFiveLines(RandomAccessFile binFile, int[] fieldLengths, int lineLength,
			long dataLength, int[] indexesToPrint) {
		// Calculate the number of lines
		long numberOfLines = dataLength / lineLength;

		// If even numberOfLines, print 6 lines, else print 5 lines
		long numberOfLinesToPrint = (numberOfLines % 2 == 0) ? 6 : 5;
		// If even numberOfLines, start 2 lines before the integer middle of the file,
		// else print starting 2 lines from the middle calculated using the
		// number of lines minus 1
		long position = ((numberOfLines % 2 == 0) ? (numberOfLines / 2 - 2) : ((numberOfLines - 1) / 2) - 2);

		// Position is 0 if less than zero, otherwise subtract 1 to account
		// for starting position using line length
		position = position <= 0 ? 0 : (position - 1) * lineLength;

		System.out.println("\nMiddle 5 Lines (6 if even number of data fields):");
		printSpecifiedNumberOfLines(binFile, fieldLengths, lineLength, dataLength, indexesToPrint, numberOfLinesToPrint,
				position);

	}

	/*---------------------------------------------------------------------
	|  Method printLastFiveLines
	|
	|  Purpose:	Print the last five lines of the binary file, covering the case
	|			where there is 5 or less lines in the file. In this case print all the 
	|			available lines. 
	|
	|  Pre-condition:	binFile is opened. fieldLengths contains lengths of each field.
	|					lineLength contains the length of each line in bytes
	|					dataLength contains the length of the data only portion of 
	|					the file in bytes. This excludes the line length info at the end
	|					indexesToPrint contains the indexes of the line to display and the 
	|					indexes are in the range 0 to n-1 where n is the number of fields.
	|			
	|
	|  Post-condition:	 Last 5 data lines of the file will be printed. Or all the lines
	|					if there is 5 or less lines
	|
	|  Parameters:
	|			binFile -- the opened RandomAccessFile to read from
	|			fieldLengths -- int array containing lengths of each field in the line in bytes.
	|						-1 represents integers of length 4.
	|			lineLength --  The byte length of a line in the bin file
	|			dataLength -- The byte length of the data in the file excluding field lengths
	|			indexesToPrint -- Valid indexes in the line to print 
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void printLastFiveLines(RandomAccessFile binFile, int[] fieldLengths, int lineLength,
			long dataLength, int[] indexesToPrint) {
		// Get the number of lines in the file and
		long numberOfLines = dataLength / lineLength;

		// If more than five lines start five lines from the end, else start
		// at the beginnning of the file
		long position = numberOfLines > 5 ? numberOfLines - 5 : 0;
		position = position * lineLength;

		// If more than five lines print five lines, else print the number
		// of lines
		long numberOfLinesToPrint = (numberOfLines >= 5 ? 5 : numberOfLines);

		System.out.println("\nLast 5 Lines:");
		printSpecifiedNumberOfLines(binFile, fieldLengths, lineLength, dataLength, indexesToPrint, numberOfLinesToPrint,
				position);

	}

	/*---------------------------------------------------------------------
	|  Method printArrayListIndexes
	|
	|  Purpose:	Take an array of the strings from a specific line and the indexes
	|			to print from that line array. If a string is empty, print null 
	|			with the appropriate number of spaces to keep the same length of the 
	|			string. 
	|
	|  Pre-condition:	lineStringList contains each non-null string from the file line
	|					and indexesToPrint has 0 or more indexes in it.
	|					indexesToPrint are VALID for the array
	|
	|  Post-condition:	 Each index of the lineStringList is printed in this format
	|						[field1][field...][field n]
	|
	|  Parameters:
	|			lineStringList -- String array containing strings to print
	|			indexesToPrint -- int array of indexes to print in the String array
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	private static void printArrayListIndexes(String[] lineStringList, int[] indexesToPrint) {
		try {
			for (int i = 0; i < indexesToPrint.length; i++) {
				// If empty string of all spaces, sub in null with number of spaces from before
				// - 4
				if (lineStringList[indexesToPrint[i]].strip().length() == 0) {
					String toPrint;
					if (lineStringList[indexesToPrint[i]].length() > 4) {
						toPrint = String.format("%-" + lineStringList[indexesToPrint[i]].length() + "s", "null");

					} else {
						toPrint = "null";
					}
					System.out.print("[" + toPrint + "]");

				} else {
					System.out.print("[" + lineStringList[indexesToPrint[i]] + "]");

				}

			}
			System.out.println();

		} catch (Exception e) {
			System.out.println("ERROR: Could not print specified indexes of line");
			System.exit(-1);

		}

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
	private static void readBinFileLineIntoArrayList(RandomAccessFile binFile, int[] fieldLengths,
			String[] lineStringList, long position) {
		try {
			binFile.seek(position);
			byte[] lineBytes = null;
			for (int k = 0; k < fieldLengths.length; k++) {
				if (fieldLengths[k] == -1) {
					lineStringList[k] = Integer.toString(binFile.readInt());
				} else {
					lineBytes = new byte[fieldLengths[k]];
					binFile.readFully(lineBytes);
					lineStringList[k] = new String(lineBytes);
				}

			}

		} catch (Exception e) {
			System.out.println("ERROR: Could not parse the binary file line");
			System.exit(-1);

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
	private static int[] getFieldLengthsArray(RandomAccessFile binFile, long numberOfRecords, int numberOfFields) {
		int[] fieldLengths = new int[numberOfFields];
		int i = numberOfFields;
		long pos = numberOfRecords - 8;
		while (i > 0) {

			try {
				binFile.seek(pos);
				fieldLengths[i - 1] = binFile.readInt();
			} catch (IOException e) {
				System.out.println("ERROR: Could not read field lengths from binary file");
				System.exit(-1);
			}

			pos -= 4;
			i--;
		}
		return fieldLengths;
	}

}
