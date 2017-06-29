class MainClass {
	public static void main(String[] args) {
		System.out.println(new Test().test());
	}
}

class Test {
	double a;
	public int test() {
		double d;
		int c;
		d = 5.1;
		a = 1.5;
		if (d + d  < 9.9) {
			c = 1;
		} else {
			c = 5;
		}
		return c;
	}
}
