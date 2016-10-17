package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.List;

import edu.kit.ipd.sdq.eventsim.measurement.Metadata;

public class MetadataHelper {

    private MetadataHelper() {
    }

    public static Metadata[] mergeMetadata(Metadata[] metadata, List<Metadata> global) {
        return mergeMetadata(metadata, global.toArray(new Metadata[global.size()]));
    }

    public static Metadata[] mergeMetadata(Metadata[] metadata, Metadata[] global) {
        Metadata[] merged = new Metadata[metadata.length + global.length];
        System.arraycopy(metadata, 0, merged, 0, metadata.length);
        System.arraycopy(global, 0, merged, metadata.length, global.length);
        return merged;
    }

}
