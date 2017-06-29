package Types;

import SymbolTables.ClassSymbolTable;

public class ObjectType extends TypeNode {

	private ClassSymbolTable objTable;
	private boolean isChild = false;
	
	public ObjectType(ClassSymbolTable cst) {
		objTable = cst;
		if (cst != null) {
			name = cst.getClassName();
			if (cst.isExtension()) {
				isChild = true;
			}
		}
	}
	
	@Override
	public boolean isCompound() {
		return true;
	}

	@Override
	public ClassSymbolTable getCompoundInstance() {
		return objTable;
	}
	
	@Override
	public boolean isChild() {
		return isChild;
	}

}
