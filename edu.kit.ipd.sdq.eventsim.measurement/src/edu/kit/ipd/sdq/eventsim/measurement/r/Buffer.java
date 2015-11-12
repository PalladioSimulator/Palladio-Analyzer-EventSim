package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;

/**
 * Buffers multiple measurements waiting to be transferred to R later on.
 * 
 * @author Philipp Merkle
 *
 */
public class Buffer {

	private String[] what;
	private String[] whereFirstId;
	private String[] whereFirstType;
	private String[] whereSecondId;
	private String[] whereSecondType;
	private String[] whereProperty;
	private String[] whoType;
	private String[] whoId;
	private double[] value;
	private double[] when;

	private Map<String, String[]> contexts;

	private IdProvider extractors;

	/**
	 * the number of elements effectively contained in this buffer. Once this number equals the buffer size, the buffer
	 * is considered full
	 */
	private int size = 0;

	private final int capacity;

	public Buffer(int capacity, IdProvider extractors) {
		this.capacity = capacity;
		this.extractors = extractors;

		what = new String[capacity];
		whereFirstId = new String[capacity];
		whereFirstType = new String[capacity];
		whereSecondId = new String[capacity];
		whereSecondType = new String[capacity];
		whereProperty = new String[capacity];
		whoType = new String[capacity];
		whoId = new String[capacity];
		value = new double[capacity];
		when = new double[capacity];

		contexts = new HashMap<>();
	}

	public <F extends Entity, S extends Entity, T> void putPair(Measurement<Pair<F, S>, T> m) {
		F first = m.getWhere().getElement().getFirst();
		S second = m.getWhere().getElement().getSecond();
		whereFirstId[size] = extractors.toIdString(first);
		whereFirstType[size] = toTypeString(first);
		whereSecondId[size] = extractors.toIdString(second);
		whereSecondType[size] = toTypeString(second);

		whereProperty[size] = m.getWhere().getProperty();

		putCommonProperties(m);
		size++;
	}

	private String toTypeString(Object o) {
		if (EObject.class.isInstance(o)) {
			return stripNamespace(((EObject) o).eClass().getInstanceClassName());

		} else {
			return stripNamespace(o.getClass().getName());
		}
	}

	private String stripNamespace(String fqn) {
		int startOfClassName = fqn.lastIndexOf(".");
		return fqn.substring(startOfClassName + 1, fqn.length());
	}

	public <E> void put(Measurement<E, ?> m) {
		whereFirstId[size] = extractors.toIdString(m.getWhere().getElement());
		whereFirstType[size] = toTypeString(m.getWhere().getElement());
		whereSecondId[size] = null;
		whereSecondType[size] = null;
		whereProperty[size] = m.getWhere().getProperty();

		putCommonProperties(m);
		size++;
	}

	private <E> void putCommonProperties(Measurement<E, ?> m) {
		what[size] = m.getWhat().toString();

		for (Object o : m.getWhere().getContexts()) {
			String key = toTypeString(o);
			if (!contexts.containsKey(key)) {
				contexts.put(key, new String[capacity]);
			}
			contexts.get(key)[size] = extractors.toIdString(o);
		}

		if (m.getWho() != null) {
			whoType[size] = toTypeString(m.getWho());
			whoId[size] = extractors.toIdString(m.getWho());
		} else {
			whoType[size] = null;
			whoId[size] = null;
		}
		value[size] = m.getValue();
		when[size] = m.getWhen();
	}

	public void shrinkToSize() {
		what = shrinkArray(what, size);
		whereFirstId = shrinkArray(whereFirstId, size);
		whereFirstType = shrinkArray(whereFirstType, size);
		whereSecondId = shrinkArray(whereSecondId, size);
		whereSecondType = shrinkArray(whereSecondType, size);
		whereProperty = shrinkArray(whereProperty, size);

		whoType = shrinkArray(whoType, size);
		whoId = shrinkArray(whoId, size);
		value = shrinkArray(value, size);
		when = shrinkArray(when, size);

		for (String key : contexts.keySet()) {
			contexts.put(key, shrinkArray(contexts.get(key), size));
		}
	}

	private static String[] shrinkArray(String[] src, int size) {
		String[] dest = new String[size];
		System.arraycopy(src, 0, dest, 0, size);
		return dest;
	}

	private static double[] shrinkArray(double[] src, int size) {
		double[] dest = new double[size];
		System.arraycopy(src, 0, dest, 0, size);
		return dest;
	}

	public boolean isFull() {
		return size == capacity;
	}

	public String[] getWhat() {
		return what;
	}

	public String[] getWhereFirstId() {
		return whereFirstId;
	}

	public String[] getWhereFirstType() {
		return whereFirstType;
	}

	public String[] getWhereSecondId() {
		return whereSecondId;
	}

	public String[] getWhereSecondType() {
		return whereSecondType;
	}

	public String[] getWhereProperty() {
		return whereProperty;
	}

	public String[] getWhoType() {
		return whoType;
	}

	public String[] getWhoId() {
		return whoId;
	}

	public double[] getValue() {
		return value;
	}

	public double[] getWhen() {
		return when;
	}

	public Map<String, String[]> getContexts() {
		return contexts;
	}

	public int getSize() {
		return size;
	}
	
	

}