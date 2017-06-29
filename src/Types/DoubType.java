package Types;

import SymbolTables.ClassSymbolTable;

public class DoubType extends TypeNode {
	
	private static DoubType instance = null;

	private DoubType(){name = "double";};
	
	public static DoubType getInstance() {
		if (instance == null) {
			instance = new DoubType();
		}
		return instance;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public ClassSymbolTable getCompoundInstance() {
		return null; // int's aren't compound
	}

	@Override
	public boolean isChild() {
		return false;
	}
}
