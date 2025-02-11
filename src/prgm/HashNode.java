package prgm;

import java.util.ArrayList;
import java.util.Optional;

public class HashNode {

//	private int hashBucketFilePointer;
//	private ArrayList<HashNode> children = null;

	private Optional<Long> offsetStart = Optional.empty();
	private int value;
	private Optional<ArrayList<HashNode>> children = Optional.empty();
	private int currentDataCount = 0;
	private int level = -1;
//	private ArrayList<indexOffsetPair> indexOffsetPairArray = null; // Probably do not want to use this. Actually what I am saying is
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
	public HashNode(Long bucketFilePtr, boolean createChildren, int val) {
		if (value < 0 || value > 9) {
			System.out.println("ERROR: Invalid hash node value");
			System.exit(-1);
		}
		this.value = val;
		if (bucketFilePtr != null && createChildren) { // Check for mismatch
			System.out.println("ERROR: Could not create new node." + "\nMistmatch between file ptr and children ptr");
			System.exit(-1);
		}
		if (bucketFilePtr != null) { // Assign file ptr if this is a leaf node
			this.offsetStart = Optional.of(bucketFilePtr);

		} else if (createChildren) { // Otherwise create its children
			this.children = Optional.of(new ArrayList<HashNode>(10));
			for (int i = 0; i < 10; i++) {
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
		this.offsetStart = Optional.of(val);

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
		for (int i = 0; i < 10; i++) {
			this.children.get().add(new HashNode(null, false, i));
		}
	}

	public HashNode getChild(int i) {
		return this.children.get().get(i);
	}

	@Override
	public String toString() {
		if (this.children.isEmpty()) { // If no children 
			if (this.offsetStart.isEmpty()) { // And if not offset
				return String.format("value: %s, children: %s, offsets: %s, isLeaf: %s, level: %s", this.value, "not init",
						"not init", this.isLeaf(), this.level);

			} else {
				return String.format("value: %s, children: %s, offsets: %s, isLeaf: %s, level: %s", this.value, "not init",
						this.offsetStart.get(), this.isLeaf(), this.level);

			}
		} else {
			if (this.offsetStart.isEmpty()) {
				return String.format("value: %s, children: %s, offsets: %s, isLeaf: %s, level: %s", this.value, this.children.get().size(),
						"not init", this.isLeaf(), this.level);

			}
		}
		return String.format("value: %s, size: %s, offsets: %s, isLeaf: %s, level: %s", this.value, this.children.get().size(),
				this.offsetStart.get(), this.isLeaf(), this.level);

	}
	
	public boolean isLeaf() {
		return this.children.isEmpty();
	}
	
	public int getDataCount() {
		return this.currentDataCount;
	}
	
	public long getOffset() {
		return this.offsetStart.get();
	}
	
	public void incrDataCount() {
		this.currentDataCount ++ ;
	}
	
	public void setEmpty() {
		this.children = Optional.empty();
		this.offsetStart = Optional.empty();
		
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setLevel(int l) {
		this.level = l;
	}
	
	public int getLevel() {
		return this.level;
	}
}
