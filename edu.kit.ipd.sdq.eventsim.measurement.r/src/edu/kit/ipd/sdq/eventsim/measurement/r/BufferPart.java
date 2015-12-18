package edu.kit.ipd.sdq.eventsim.measurement.r;

public class BufferPart {

	public String[] type;
	public String[] id;
	public String[] name;

	public BufferPart(int capacity) {
		type = new String[capacity];
		id = new String[capacity];
		name = new String[capacity];
	}

	public String[] getType() {
		return type;
	}

	public String[] getId() {
		return id;
	}

	public String[] getName() {
		return name;
	}

	public void shrink(int size) {
		id = Buffer.shrinkArray(id, size);
		type = Buffer.shrinkArray(type, size);
		name = Buffer.shrinkArray(name, size);
	}

}
