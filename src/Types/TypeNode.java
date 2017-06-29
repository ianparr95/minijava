package Types;

import SymbolTables.ClassSymbolTable;

public abstract class TypeNode {
	
	protected String name;
	protected int offset;
	
	public String getName() {
		return name;
	}
	
	public abstract boolean isCompound();
	public abstract boolean isChild();
	public abstract ClassSymbolTable getCompoundInstance();
	
	public String toString() {
		return name;
	}
}
