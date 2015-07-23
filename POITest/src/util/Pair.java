package util;

public class Pair<E, F> {

	private E first;
	private F second;
	
	public Pair(E first, F second) {
		super();
		this.first = first;
		this.second = second;
	}

	public E getFirst() {
		return first;
	}

	public void setFirst(E first) {
		this.first = first;
	}

	public F getSecond() {
		return second;
	}

	public void setSecond(F second) {
		this.second = second;
	}

	@Override
	public String toString() {
		return "[first=" + first + ", second=" + second + "]";
	}
}
