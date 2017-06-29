package Types;

import SymbolTables.ClassSymbolTable;

public class IntArrType extends TypeNode {


	private int numElements;
	
	public IntArrType() {
		// -1 cause initially unknown
		numElements = -1;
		name = "int[]";
	}
	
	public int length() {
		return numElements;
	}
	
	public void setLength(int length) {
		numElements = length;
	}
	
	@Override
	public boolean isCompound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	public ClassSymbolTable getCompoundInstance() {
		return null;
	}

}
