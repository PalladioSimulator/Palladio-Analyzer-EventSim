package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Philipp Merkle
 *
 * @param <E>
 *            the measuring point's type (i.e. the type of the probed element)
 */
public class Measurement<E> {

    private Object what;

    private MeasuringPoint<E> where;

    private Object who;

    private double value;

    private double when;

    private Metadata[] metadata;

    /**
     * Constructs a new measurement.
     * 
     * @param what
     *            the measured metric or property (e.g. response time)
     * @param where
     *            the measuring point (e.g. a reference to the method whose response time is to be
     *            measured)
     * @param who
     *            the trigger, i.e. the element that caused this measurement (e.g. a specific
     *            process/thread/request)
     * @param value
     *            the measured value (unit could be stored as metadata, if desired)
     * @param when
     *            the point in time this measurement refers to
     * @param metadata
     *            optional data to characterize this measurement
     */
    public Measurement(Object what, MeasuringPoint<E> where, Object who, double value, double when,
            Metadata... metadata) {
        this.what = what;
        this.where = where;
        this.who = who;
        this.value = value;
        this.when = when;
        this.metadata = metadata;
    }

    public Object getWhat() {
        return what;
    }

    public MeasuringPoint<E> getWhere() {
        return where;
    }

    public Object getWho() {
        return who;
    }

    public double getWhen() {
        return when;
    }

    public double getValue() {
        return value;
    }

    public Metadata[] getMetadata() {
        return metadata;
    }

    public void addMetadata(Metadata... globalMetadata) {
        metadata = MetadataHelper.mergeMetadata(metadata, globalMetadata);
    }

    public void addMetadata(List<Metadata> globalMetadata) {
        metadata = MetadataHelper.mergeMetadata(metadata, globalMetadata);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Measurement [what=").append(what).append(", where=").append(where).append(", who=").append(who)
                .append(", value=").append(value).append(", when=").append(when).append(", metadata=")
                .append(Arrays.toString(metadata)).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((what == null) ? 0 : what.hashCode());
        temp = Double.doubleToLongBits(when);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((where == null) ? 0 : where.hashCode());
        result = prime * result + ((who == null) ? 0 : who.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        Measurement other = (Measurement) obj;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
            return false;
        if (what != other.what)
            return false;
        if (Double.doubleToLongBits(when) != Double.doubleToLongBits(other.when))
            return false;
        if (where == null) {
            if (other.where != null)
                return false;
        } else if (!where.equals(other.where))
            return false;
        if (who == null) {
            if (other.who != null)
                return false;
        } else if (!who.equals(other.who))
            return false;
        return true;
    }

}
