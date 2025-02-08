/*+----------------------------------------------------------------------
 ||
 ||  Class Field 
 ||
 ||         Author:  Steven George
 ||
 ||        Purpose:	This class contains the basic information about a column
 ||					or "Field" of the csv file in Prog1A.java, specifically it 
 ||					stores the name of the field, if it is an int or String,
 ||					and the max length of all Strings in the data for this field.
 ||					This is useful for padding String and not ints when parsing 
 ||					the csv file and writing it to the binary file. 
 ||
 ||  Inherits From:  None
 ||
 ||     Interfaces:  None
 ||
 |+-----------------------------------------------------------------------
 ||
 ||      Constants:  No public class constants
 |+-----------------------------------------------------------------------
 ||
 ||   Constructors:  Field(String name, boolean isInteger, String value)
 ||						Expects non null strings
 ||
 ||  Class Methods:  No class/static methods
 ||
 ||  Inst. Methods:  [List the names, arguments, and return types of all
 ||                   public instance methods.]
 ||
 ||					void checkMaxLength(String word)
 ||						Takes a String word and determines if its length is 
 ||						greater than the private variable maxLength. If yes,
 ||						the private instance variable maxLength is updated
 ||						to the length of word.
 ||					
 ||					boolean isInteger()
 ||						Getter for private boolean isInteger field. 
 ||					
 ||					int getMaxLength()
 ||						Getter for private int maxLength field used for Fields 
 ||						that represent Strings
 ||					
 ||					String getFieldName()
 ||						Getter for private STring fieldName
 ||						
 ||					String toString()
 ||						override method that gives the three important variables
 ++-----------------------------------------------------------------------*/
package prgm;


public class Field {
	
	private String fieldName = null;
	private boolean isInteger = false;
	private int maxLength = 0;
	
	
	/*---------------------------------------------------------------------
	|  Method Field
	|
	|  Purpose:	Constructor for class that assigns all private instance variables
	|			based on the given values
	|
	|  Pre-condition:	parameters are not null
	|
	|  Post-condition:	object is instantiated to represent the given user input 
	|					to allow the checking of whether a field is an integer or not,
	|					what its maxLength is if yes. Also allows functionality to update
	|					length. 
	|
	|  Parameters:
	|		name -- String name of field. Transferred into method
	|		isInteger -- Boolean showing if Field is integer. True if yes. Transferred into method
	|		value -- String version of data. May be parsed into integer elsewhere. Transferred into method.
	|		
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	public Field(String name, boolean isInteger, String value) {
		if (name != null) {
			this.fieldName = name;
		}
		if (isInteger)
			this.isInteger = true;
		if (value != null && !isInteger) {
			checkMaxLength(value);
		}
		
	
	}
	
	/*---------------------------------------------------------------------
	|  Method checkMaxLength
	|
	|  Purpose:	Take a new String word and check if its length is greater than
	|			the maxLength currently stored in this object. If it is then the 
	|			new length is stored in this.maxLength
	|
	|  Pre-condition:	word is a non-null String and corresponds to this Field
	|
	|  Post-condition:	maxLength updated to word.length() if word length greater
	|					than current maxLength
	|
	|  Parameters:
	|		word -- String to check max length against 
	|
	|  Returns:  void
	*-------------------------------------------------------------------*/
	public void checkMaxLength(String word) {
		if (word.length() > this.maxLength) {
			this.maxLength = word.length();
		}
	}
	

	
	@Override
	public String toString() {
		String res =  "[ " + this.fieldName + ", " + this.isInteger;
		if (isInteger) {
			return res + "]";
		} else {
			return res + ", " + this.maxLength + "]";
		}
	}
	
	// Getters and Setters. See block comment
	
	public boolean isInteger() {
		return this.isInteger;
	}
	
	public int getMaxLength() {
		return this.maxLength;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
}
