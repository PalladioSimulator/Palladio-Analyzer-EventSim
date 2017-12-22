package edu.kit.ipd.sdq.eventsim.measurement.r.connection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionRegistry {

    private static ConnectionRegistry instance = new ConnectionRegistry();

    private RserveConnection connection;

    private List<ConnectionListener> connectionListener;

    private ConnectionRegistry() {
        connectionListener = new CopyOnWriteArrayList<>();
    }

    public static ConnectionRegistry instance() {
        return instance;
    }

    public void setConnection(RserveConnection connection) {
        this.connection = connection;
        connectionListener.forEach(l -> l.connectionAdded(connection));
    }
    
    // TODO remove connection method

    public RserveConnection getConnection() {
        return connection;
    }

    public void addListener(ConnectionListener listener) {
        this.connectionListener.add(listener);
    }

    public void removeListener(ConnectionListener listener) {
        this.connectionListener.remove(listener);
    }

}
