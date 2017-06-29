class MainClass {
	public static void main(String[] args) {
		System.out.println(new A().start());
	}
}

class A {
	public int start() {
		int x;
		int y;
		int[] z;
		int[] w;

		z = new int[this.second()];
		w = z;

		x = 0;
		y = 9;

		if (true) {

		} else {

		}
		while (x < (y + 1)) {
			x = x + 1;
			System.out.println(x);
		}

		return 0 - 1;
	}

	public int second() {
		return 2;
	}
}