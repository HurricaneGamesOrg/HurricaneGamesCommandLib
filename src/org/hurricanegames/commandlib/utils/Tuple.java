package org.hurricanegames.commandlib.utils;

public class Tuple<T1, T2> {

	public static <T1, T2> Tuple<T1, T2> fromObject1(T1 o1) {
		return new Tuple<>(o1, null);
	}

	public static <T1, T2> Tuple<T1, T2> fromObject2(T2 o2) {
		return new Tuple<>(null, o2);
	}

	protected final T1 o1;
	protected final T2 o2;

	public Tuple(T1 o1, T2 o2) {
		this.o1 = o1;
		this.o2 = o2;
	}

	public T1 getObject1() {
		return o1;
	}

	public T2 getObject2() {
		return o2;
	}

}
