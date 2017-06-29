package Types;

import SymbolTables.ClassSymbolTable;

public class VoidType extends TypeNode {
	private static VoidType instance = null;

	private VoidType(){name = "void";};
	
	public static VoidType getInstance() {
		if (instance == null) {
			instance = new VoidType();
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
