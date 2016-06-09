package edu.kit.ipd.sdq.eventsim.measurement.r.connection.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionProgressListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class ConnectionStatusProvider extends AbstractSourceProvider
        implements ConnectionListener, ConnectionProgressListener {

    private static final String ID = "edu.kit.ipd.sdq.eventsim.measurement.r.connection.status";

    private RserveConnection connection;

    public ConnectionStatusProvider() {
        ConnectionRegistry.instance().addListener(this);
    }

    @Override
    public void dispose() {
        if (connection != null) {
            connection.removeListener(this);
        }
        ConnectionRegistry.instance().removeListener(this);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map getCurrentState() {
        Map map = new HashMap<>(1);
        map.put(ID, isConnected());
        return map;
    }

    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { ID };
    }

    @Override
    public void connectionAdded(RserveConnection connection) {
        connection.addListener(this);
        this.connection = connection;
    }

    @Override
    public void connectionRemoved(RserveConnection connection) {
        connection.removeListener(this);
        this.connection = null;
    }

    @Override
    public void failed() {
        // nothing to do
    }

    @Override
    public void connecting(int attempt) {
        // nothing to do
    }

    @Override
    public void connected() {
        fireSourceChanged(ISources.WORKBENCH, ID, true);
    }

    @Override
    public void cancelled() {
        // nothing to do
    }

    @Override
    public void disconnected() {
        fireSourceChanged(ISources.WORKBENCH, ID, false);
    }

}
