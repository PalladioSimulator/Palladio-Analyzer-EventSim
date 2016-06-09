package edu.kit.ipd.sdq.eventsim.measurement.r.connection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;

public class RserveConnection {

    private static final Logger log = Logger.getLogger(RMeasurementStore.class);

    public static final String DEFAULT_HOST = "127.0.0.1";

    public static final int DEFAULT_PORT = 6311;

    public static final int CONNECTION_RETRIES_MAX = 60;

    private static final int MILLISECONDS_BETWEEN_CONNECTION_RETRIES = 1000;

    /** used to synchronize access to the underlying R connection */
    private Object connectionMonitor = new Object();

    private RConnection connection;

    private boolean shouldCancel = false;

    private Thread connectorThread;

    private List<ConnectionProgressListener> statusListener;

    public RserveConnection() {
        this.statusListener = new CopyOnWriteArrayList<>();
    }

    public void connect(String host, int port) {
        synchronized (connectionMonitor) {
            ConnectorRunnable connector = new ConnectorRunnable(host, port);
            connectorThread = new Thread(connector);
            connectorThread.start();
            try {
                connectorThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void connect() {
        connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void connectAsync(String host, int port) {
        synchronized (connectionMonitor) {
            ConnectorRunnable connector = new ConnectorRunnable(host, port);
            connectorThread = new Thread(connector);
            connectorThread.start();
        }
    }

    public void stopConnecting() {
        synchronized (connectionMonitor) {
            if (connectorThread == null) {
                return;
            }
            shouldCancel = true;
            try {
                connectorThread.join(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // still alive?
            if (connectorThread.isAlive()) {
                connectorThread.stop();
            }
            connectorThread = null;
            shouldCancel = false;
        }
    }

    public void disconnect() {
        synchronized (connectionMonitor) {
            statusListener.forEach(l -> l.disconnected());
            if (connection != null && connection.isConnected()) {
                connection.close();
            } else {
                log.warn("Tried to disconnect, but there is no open connection");
            }
        }
    }

    public RConnection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void addListener(ConnectionProgressListener listener) {
        statusListener.add(listener);
    }

    public void removeListener(ConnectionProgressListener listener) {
        statusListener.remove(listener);
    }

    private class ConnectorRunnable implements Runnable {

        private String host;
        private int port;

        public ConnectorRunnable(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            if (connection != null && connection.isConnected()) {
                log.warn("Tried to connect, but there is already an open connection");
                statusListener.forEach(l -> l.failed());
                return;
            }
            // try connecting to R
            log.info("Establishing R connection to " + host + ":" + port + "...");
            for (int retries = 0; retries < CONNECTION_RETRIES_MAX; retries++) {
                if (shouldCancel) {
                    statusListener.forEach(l -> l.cancelled());
                    return;
                }
                try {
                    connection = new RConnection(host, port);
                } catch (RserveException e) {
                    // handled in the following
                }

                if (connection != null && connection.isConnected()) {
                    // successfully connected => leave for
                    statusListener.forEach(l -> l.connected());
                    break;
                } else {
                    if (retries == 0) {
                        log.error("Could not establish Rserve connection to R. "
                                + "Make sure to run Rserve, e.g. by calling \"library(Rserve); Rserve(port=" + port
                                + ")\" in R. ");
                    }
                    if (retries % 20 == 0) {
                        log.error("Waiting for Rserve connection to " + host + ":" + port + "...");
                    }
                    // wait some time before retrying again
                    try {
                        for (ConnectionProgressListener l : statusListener) {
                            l.connecting(retries);
                        }
                        Thread.sleep(MILLISECONDS_BETWEEN_CONNECTION_RETRIES);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }

            // still not yet connected? give up.
            if (connection == null || !connection.isConnected()) {
                statusListener.forEach(l -> l.failed());
                log.error("Could not establish Rserve connection to R within " + CONNECTION_RETRIES_MAX
                        + " attempts. Giving up now.");
                return;
            }

            // TODO move to another class
            // try loading "data.table" library
            try {
                connection.voidEval("library(data.table)");
            } catch (RserveException e) {
                throw new RuntimeException("R could not load library \"data.table\". "
                        + "Please run \"install.packages('data.table')\" in R.");
            }

        }

    }

}
