package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.lang.reflect.Array;

public class Column<T> {

	private Class<T> type;
	
	private String name;
	
	private T[] values;
	
	private boolean factorial;
	
	public Column(Class<T> type, String name, int capacity, boolean factorial) {
		this.type = type;
		this.name = name;
		this.values = (T[]) Array.newInstance(type, capacity);
		this.factorial = factorial;
	}
	
	public void set(int index, T value) {
		values[index] = value;
	}
	
	public void shrink(int size) {
		T[] dest = (T[]) Array.newInstance(type, size);
		System.arraycopy(values, 0, dest, 0, size);
		values = dest;
	}
	
	public T[] values() {
		return values;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isFactorial() {
		return factorial;
	}
	
}
