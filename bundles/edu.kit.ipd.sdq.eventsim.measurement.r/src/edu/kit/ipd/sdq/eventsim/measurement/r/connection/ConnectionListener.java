package edu.kit.ipd.sdq.eventsim.measurement.r.connection;

public interface ConnectionListener {

    void connectionAdded(RserveConnection connection);
    
    void connectionRemoved(RserveConnection connection);

}
