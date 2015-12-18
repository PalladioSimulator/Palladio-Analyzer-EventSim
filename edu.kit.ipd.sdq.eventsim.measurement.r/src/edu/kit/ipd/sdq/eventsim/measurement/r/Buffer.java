package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.PropertyExtractor;

/**
 * Buffers multiple measurements waiting to be transferred to R later on.
 * 
 * @author Philipp Merkle
 *
 */
public class Buffer {

	private String[] what;
	private BufferPart whereFirst;
	private BufferPart whereSecond;
	private String[] whereProperty;
	private BufferPart who;
	private double[] value;
	private double[] when;

	private Map<String, BufferPart> contexts;

	private PropertyExtractor idExtractors;
	private PropertyExtractor nameExtractors;

	/**
	 * the number of elements effectively contained in this buffer. Once this number equals the buffer size, the buffer
	 * is considered full
	 */
	private int size = 0;

	private final int capacity;

	public Buffer(int capacity, PropertyExtractor idExtractors, PropertyExtractor nameExtractors) {
		this.capacity = capacity;
		this.idExtractors = idExtractors;
		this.nameExtractors = nameExtractors;

		what = new String[capacity];
		whereFirst = new BufferPart(capacity);
		whereSecond = new BufferPart(capacity);
		whereProperty = new String[capacity];
		who = new BufferPart(capacity);
		value = new double[capacity];
		when = new double[capacity];

		contexts = new HashMap<>();
	}

	public <F extends Entity, S extends Entity, T> void putPair(Measurement<Pair<F, S>, T> m) {
		F first = m.getWhere().getElement().getFirst();
		S second = m.getWhere().getElement().getSecond();
		whereFirst.id[size] = idExtractors.extractFrom(first);
		whereFirst.type[size] = toTypeString(first);
		whereFirst.name[size] = nameExtractors.extractFrom(first);
		whereSecond.id[size] = idExtractors.extractFrom(second);
		whereSecond.type[size] = toTypeString(second);
		whereSecond.name[size] = nameExtractors.extractFrom(second);

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
		whereFirst.id[size] = idExtractors.extractFrom(m.getWhere().getElement());
		whereFirst.type[size] = toTypeString(m.getWhere().getElement());
		whereFirst.name[size] = nameExtractors.extractFrom(m.getWhere().getElement());
		whereSecond.id[size] = null;
		whereSecond.type[size] = null;
		whereSecond.name[size] = null;
		whereProperty[size] = m.getWhere().getProperty();

		putCommonProperties(m);
		size++;
	}

	private <E> void putCommonProperties(Measurement<E, ?> m) {
		what[size] = m.getWhat().toString();

		for (Object o : m.getWhere().getContexts()) {
			String key = toTypeString(o).toLowerCase();
			if (!contexts.containsKey(key)) {
				contexts.put(key, new BufferPart(capacity));
			}
			contexts.get(key).id[size] = idExtractors.extractFrom(o);
			contexts.get(key).type[size] = toTypeString(o);
			contexts.get(key).name[size] = nameExtractors.extractFrom(o);
		}

		if (m.getWho() != null) {
			who.type[size] = toTypeString(m.getWho());
			who.id[size] = idExtractors.extractFrom(m.getWho());
			who.name[size] = nameExtractors.extractFrom(m.getWho());
		} else {
			who.type[size] = null;
			who.id[size] = null;
			who.name[size] = null;
		}
		value[size] = m.getValue();
		when[size] = m.getWhen();
	}

	public void shrinkToSize() {
		what = shrinkArray(what, size);
		whereFirst.shrink(size);
		whereSecond.shrink(size);
		whereProperty = shrinkArray(whereProperty, size);
		who.shrink(size);
		value = shrinkArray(value, size);
		when = shrinkArray(when, size);

		for (BufferPart p : contexts.values()) {
			p.shrink(size);
		}
	}

	protected static String[] shrinkArray(String[] src, int size) {
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

	public BufferPart getWhereFirst() {
		return whereFirst;
	}

	public BufferPart getWhereSecond() {
		return whereSecond;
	}

	public String[] getWhereProperty() {
		return whereProperty;
	}

	public BufferPart getWho() {
		return who;
	}

	public double[] getValue() {
		return value;
	}

	public double[] getWhen() {
		return when;
	}

	public Map<String, BufferPart> getContexts() {
		return contexts;
	}

	public int getSize() {
		return size;
	}

}