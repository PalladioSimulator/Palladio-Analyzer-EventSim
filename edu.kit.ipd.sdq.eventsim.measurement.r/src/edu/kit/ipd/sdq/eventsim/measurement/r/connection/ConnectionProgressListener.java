package edu.kit.ipd.sdq.eventsim.measurement.r.connection;

public interface ConnectionProgressListener {

    void failed();
    
    void connecting(int attempt);
    
    void connected();

    void cancelled();
    
    void disconnected();
    
}
