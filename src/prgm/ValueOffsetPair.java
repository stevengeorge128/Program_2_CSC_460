package prgm;

public class ValueOffsetPair {
	
	private int index;
	private long offset;
	
	public ValueOffsetPair(int i, long o) {
		this.index = i; // Contain the index value from the binary file
		this.offset = o; // Contain the byte location in the binary file
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public long getOffset() {
		return this.offset;
	}

}
