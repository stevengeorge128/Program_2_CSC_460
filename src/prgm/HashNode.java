package prgm;

import java.util.ArrayList;
import java.util.Optional;

public class HashNode {
	
//	private int hashBucketFilePointer;
//	private ArrayList<HashNode> children = null;
	
	private Optional<Long> hashBucketFilePointer = Optional.empty();
	private int value;
	private Optional<ArrayList<HashNode>> children = Optional.empty();
	private int currentDataCount = 0;
	private ArrayList<indexOffsetPair> offsets = null; // Probably do not want to use this. Actually what I am saying is 
	// that this should only exists for resizing.
	
	/*---------------------------------------------------------------------
	 * Method 	HashNode
	 * 
	 * Purpose:	Contstructor that sets the given instance variables using 
	 * 			the user inputs, or keeps them as Optional empty values if 
	 * 			they are null which is allowed, for example if dealing with 
	 * 			the root node. If told to, initializes children nodes. Node
	 * 			is initialized with its value in the tree (0-9)
	 * 
	 * Pre-condition:	None
	 * 
	 * Post-condition:	HashNode is initialized with the given values
	 * 
	 * Parameters:	
	 * 			bucektFilePtr -- int byte offset pointer to location of bucket
	 * 							in hash bucket random access file
	 * 			
	 * 
	 * Returns:	
	 * 
	 *---------------------------------------------------------------------*/
	public HashNode(Long bucketFilePtr, boolean createChildren, int value) {
		if (value < 0 || value > 9) {
			System.out.println("ERROR: Invalid hash node value");
			System.exit(-1);
		}
		if (bucketFilePtr != null && createChildren) { // Check for mismatch
			System.out.println("ERROR: Could not create new node." 
					+ "\nMistmatch between file ptr and children ptr");
			System.exit(-1);
		}
		if (bucketFilePtr != null) { // Assign file ptr if this is a leaf node
			this.hashBucketFilePointer = Optional.of(bucketFilePtr);
			
		} else if (createChildren) { // Otherwise create its children
			this.children = Optional.of(new ArrayList<HashNode>(10));
			for (int i = 0; i < 10; i ++) {
				// Set children to nodes without file pointer or children
				this.children.get().set(i, new HashNode(null, false, i));
			}
		}
		
		
		
		
	}
	
	/*---------------------------------------------------------------------
	 * Method	setChildOffset
	 * 
	 * Purpose: Assign the given long value to the private instance variable 
	 * 			hashBucketFilePointer
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
	public void setChildOffset(long val) {
		if (val < 0) {
			System.out.println("ERROR: Negative offset in set child offset");
			System.exit(-1);
		}
		this.hashBucketFilePointer = Optional.of(val);
		
	}
	
	/*---------------------------------------------------------------------
	 * Method	createChildren
	 * 
	 * Purpose: Initialize 10 child nodes
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
	public void createChildren() {
		this.children = Optional.of(new ArrayList<HashNode>(10));
		for (int i = 0; i < 10; i ++) {
			this.children.get().add(new HashNode(null, false, i));
		}
	}
	
	public HashNode getChild(int i) {
		return this.children.get().get(i);
	}

}
