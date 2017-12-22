package edu.kit.ipd.sdq.eventsim.measurement.r;

public class BufferPart {

	public Column<String> type;
	public Column<String> id;
	public Column<String> name;

	public BufferPart(String namePrefix, int capacity) {
		type = new Column<String>(String.class, namePrefix + ".type", capacity, true);
		id = new Column<String>(String.class, namePrefix + ".id", capacity, true);
		name = new Column<String>(String.class, namePrefix + ".name", capacity, true);
	}

	public Column<String> getType() {
		return type;
	}

	public Column<String> getId() {
		return id;
	}

	public Column<String> getName() {
		return name;
	}

	public void shrink(int size) {
		id.shrink(size);
		type.shrink(size);
		name.shrink(size);
	}

}
