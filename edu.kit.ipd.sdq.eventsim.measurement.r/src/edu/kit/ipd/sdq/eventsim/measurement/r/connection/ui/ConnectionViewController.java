package edu.kit.ipd.sdq.eventsim.measurement.r.connection.ui;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionStatusListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class ConnectionViewController implements ConnectionViewListener, ConnectionStatusListener {

    private ConnectionView view;

    private RserveConnection connection;

    public ConnectionViewController(ConnectionView view) {
        this.view = view;
        
        this.connection = new RserveConnection();
        connection.addListener(this);
        ConnectionRegistry.instance().setConnection(connection);
    }

    @Override
    public void disconnectButtonSelected() {
        connection.disconnect();
        view.setDisconnected();
    }

    @Override
    public void cancelButtonSelected() {
        connection.stopConnecting();
        view.setDisconnected();
    }

    @Override
    public void connectButtonSelected() {
        view.setConnecting();

        String server = view.getServer();
        int port = Integer.parseInt(view.getPort());
        connection.connectAsync(server, port);
    }

    @Override
    public void failed() {
        view.setFailed();
    }

    @Override
    public void connecting(int retries) {
        view.setConnecting(RserveConnection.CONNECTION_RETRIES_MAX - retries);
    }

    @Override
    public void connected() {
        view.setConnected();
    }

    @Override
    public void cancelled() {
        view.setDisconnected();
    }

    @Override
    public void disconnected() {
        view.setDisconnected();
    }

}
