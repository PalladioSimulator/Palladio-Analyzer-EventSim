package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPointPair;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.PropertyExtractor;

/**
 * Buffers multiple measurements waiting to be transferred to R later on.
 * 
 * @author Philipp Merkle
 *
 */
public class Buffer {

    private Column<String> what;
    private BufferPart whereFirst;
    private BufferPart whereSecond;
    private Column<String> whereProperty;
    private BufferPart who;
    private double[] value;
    private double[] when;

    private Map<String, BufferPart> contexts;
    private Map<String, Column<String>> metadata;

    private PropertyExtractor idExtractors;
    private PropertyExtractor nameExtractors;
    private PropertyExtractor typeExtractors;

    /**
     * the number of elements effectively contained in this buffer. Once this number equals the
     * buffer capacity, the buffer is considered full
     */
    private AtomicInteger size = new AtomicInteger(0);

    /**
     * the maximum size of this buffer
     */
    private final int capacity;

    public Buffer(int capacity, PropertyExtractor idExtractors, PropertyExtractor nameExtractors,
            PropertyExtractor typeExtractors) {
        this.capacity = capacity;

        this.idExtractors = idExtractors;
        this.nameExtractors = nameExtractors;
        this.typeExtractors = typeExtractors;

        what = new Column<>(String.class, "what", capacity, true);
        whereFirst = new BufferPart("where.first", capacity);
        whereSecond = new BufferPart("where.second", capacity);
        whereProperty = new Column<>(String.class, "where.property", capacity, true);
        who = new BufferPart("who", capacity);
        value = new double[capacity];
        when = new double[capacity];

        contexts = new HashMap<>();
        metadata = new HashMap<>();
    }

    /**
     * Adds a measurement to this buffer.
     * 
     * @param m
     *            the measurement to be added
     * @return @code{true}, if the measurement has been added successfully; @code{false}, if the
     *         measurement could not be added because this buffer is full already. The caller should
     *         then retry to call this method on the new buffer.
     */
    public <E> boolean put(Measurement<?> m) {
        int position = size.getAndIncrement();
        if (position >= capacity) {
            return false; // buffer is full
        }

        value[position] = m.getValue();
        when[position] = m.getWhen();
        what.set(position, m.getWhat().toString());
        whereProperty.set(position, m.getWhere().getProperty());
        if (m.getWho() != null) {
            who.getType().set(position, typeExtractors.extractFrom(m.getWho()));
            who.getId().set(position, idExtractors.extractFrom(m.getWho()));
            who.getName().set(position, nameExtractors.extractFrom(m.getWho()));
        }

        putMeasuringPoint(m, position);
        putContexts(m, position);
        putMetadata(m, position);

        return true;
    }

    private void putMeasuringPoint(Measurement<?> m, int position) {
        if (m.getWhere() instanceof MeasuringPointPair<?, ?>) {
            MeasuringPointPair<?, ?> mpp = (MeasuringPointPair<?, ?>) m.getWhere();
            Object first = mpp.getElement().getFirst();
            Object second = mpp.getElement().getSecond();
            whereFirst.getId().set(position, idExtractors.extractFrom(first));
            whereFirst.getType().set(position, typeExtractors.extractFrom(first));
            whereFirst.getName().set(position, nameExtractors.extractFrom(first));
            whereSecond.getId().set(position, idExtractors.extractFrom(second));
            whereSecond.getType().set(position, typeExtractors.extractFrom(second));
            whereSecond.getName().set(position, nameExtractors.extractFrom(second));
        } else {
            whereFirst.getId().set(position, idExtractors.extractFrom(m.getWhere().getElement()));
            whereFirst.getType().set(position, typeExtractors.extractFrom(m.getWhere().getElement()));
            whereFirst.getName().set(position, nameExtractors.extractFrom(m.getWhere().getElement()));
        }
    }

    private void putContexts(Measurement<?> m, int position) {
        for (Object o : m.getWhere().getContexts()) {
            String key = typeExtractors.extractFrom(o).toLowerCase();
            if (!contexts.containsKey(key)) {
                contexts.put(key, new BufferPart(key, capacity));
            }
            contexts.get(key).getId().set(position, idExtractors.extractFrom(o));
            contexts.get(key).getType().set(position, typeExtractors.extractFrom(o));
            contexts.get(key).getName().set(position, nameExtractors.extractFrom(o));
        }
    }

    private void putMetadata(Measurement<?> m, int position) {
        for (Metadata md : m.getMetadata()) {
            String key = md.getName();
            if (!metadata.containsKey(key)) {
                metadata.put(key, new Column<>(String.class, md.getName(), capacity, md.isFactorial()));
            }
            metadata.get(key).set(position, md.getValue().toString()); // TODO use "value
                                                                       // extractor"?
        }
    }

    public void shrinkToSize() {
        int position = size.get();
        what.shrink(position);
        whereFirst.shrink(position);
        whereSecond.shrink(position);
        whereProperty.shrink(position);
        who.shrink(position);
        value = shrinkArray(value, position);
        when = shrinkArray(when, position);

        for (BufferPart p : contexts.values()) {
            p.shrink(position);
        }

        for (Column<?> c : metadata.values()) {
            c.shrink(position);
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
        return size.get() >= capacity;
    }

    public Column<String> getWhat() {
        return what;
    }

    public BufferPart getWhereFirst() {
        return whereFirst;
    }

    public BufferPart getWhereSecond() {
        return whereSecond;
    }

    public Column<String> getWhereProperty() {
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

    public int getSize() {
        return size.get();
    }

    public Collection<Column<?>> getColumns() {
        List<Column<?>> columns = new ArrayList<>();
        columns.add(what);
        columns.add(whereFirst.id);
        columns.add(whereFirst.name);
        columns.add(whereFirst.type);
        columns.add(whereSecond.id);
        columns.add(whereSecond.name);
        columns.add(whereSecond.type);
        columns.add(whereProperty);
        columns.add(who.id);
        columns.add(who.name);
        columns.add(who.type);
        for (BufferPart p : contexts.values()) {
            columns.add(p.id);
            columns.add(p.name);
            columns.add(p.type);
        }
        for (Column<?> md : metadata.values()) {
            columns.add(md);
        }
        return columns;
    }

}