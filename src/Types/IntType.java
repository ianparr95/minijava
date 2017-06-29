package Types;

import SymbolTables.ClassSymbolTable;

public class IntType extends TypeNode {
	
	private static IntType instance = null;

	private IntType(){name = "int";};
	
	public static IntType getInstance() {
		if (instance == null) {
			instance = new IntType();
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
