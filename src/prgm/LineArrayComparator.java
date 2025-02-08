/*+----------------------------------------------------------------------
 ||
 ||  Class LineArrayComparator 
 ||
 ||         Author:  Steven George
 ||
 ||        Purpose:	Comparator for String[] that compares the two given arrays
 ||					based on the index given in the constructor
 ||					NOTE!!!!! 
 ||						ASSUMES THE STRINGS FOR COMPARISON ARE INTEGERS
 ||
 ||  Inherits From:  None
 ||
 ||     Interfaces:  Comparator
 ||
 |+-----------------------------------------------------------------------
 ||
 ||      Constants:  No public class constants
 |+-----------------------------------------------------------------------
 ||
 ||   Constructors: LineArrayComparator(int indexToSortArrayBy) 
 ||						Stores the given in in a instance variable
 ||						used to access the given array in compare in a specific
 ||						position for sorting to occur
 ||
 ||  Class Methods:  No class/static methods
 ||
 ||  Inst. Methods:  Compare
 ||						The required compare method. Compares the arrays
 ||						using the strings in index. 
 ++-----------------------------------------------------------------------*/
package prgm;



import java.util.Comparator;

public class LineArrayComparator implements Comparator<String[]> {

	private int index;

	public LineArrayComparator(int indexToSortArrayBy) {
		if (indexToSortArrayBy < 0) {
			System.out.println("ERROR: Invalid index to sort String[] by");
			System.exit(-1);

		}
		index = indexToSortArrayBy;

	}

	@Override
	/*---------------------------------------------------------------------
	|  Method compare
	|
	|  Purpose:	Compare the strings at index in s1 and s2 by parsing them 
	|			as ints and using Integer.compare and returning the result
	|
	|  Pre-condition:	Strings at index in s1 and s2 are integers and index
	|					is a valid index in the arrays
	|
	|  Post-condition:	Integer.compare() results returned. 
	|					< 0 if s1 < s2, 0 if same, >0 if s2 before s1 
	|
	|  Parameters:
	|			s1 -- String array
	|			s2 -- String array
	|
	|  Returns:  int comparison result of Integer.compare()
	*-------------------------------------------------------------------*/
	public int compare(String[] s1, String[] s2) {
		try {
			int i1 = Integer.parseInt(s1[index]);
			int i2 = Integer.parseInt(s2[index]);
			return Integer.compare(i1, i2);
		} catch (Exception e) {
			System.out.println("ERROR: The indexes to sort by were not integers in the array");
			System.exit(-1);
		}
		return 0;

	}

}
