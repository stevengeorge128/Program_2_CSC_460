package prgm;

public class indexOffsetPair {
	
	private int index;
	private int offset;
	
	public void indexOffsetPair(int i, int o) {
		this.index = i; // Contain the index value from the binary file
		this.offset = o; // Contain the byte location in the binary file
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public int getOffset() {
		return this.offset;
	}

}
