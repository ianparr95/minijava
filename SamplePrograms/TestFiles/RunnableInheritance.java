class MainClass {
	public static void main(String[] args) {
		System.out.println(17);
	}
}

class A {
    public int a() {
        return 5;
    }
    
    public int b() {
        return 6;
    }
    
    public int z() {
        return 100;
    }
}


class C extends B {
    public int a() {
        return 1;
    }
    public int c() {
        return 4;
    }
    public int d() {
        return 10;
    }
}

class B extends A{
    public int b() {
        return 4;
    }
    public int c(){
        return 11;
    }
}
