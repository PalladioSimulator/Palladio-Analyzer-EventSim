package edu.kit.ipd.sdq.eventsim.measurement.r.connection.ui;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.sdq.eventsim.measurement.r.Activator;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class ConnectionView {

    private static final String DISCONNECTED_ICON_FILE = "bullet_black.png";
    private static final String CONNECTED_ICON_FILE = "bullet_green.png";
    private static final String CONNECTING_ICON_FILE = "bullet_orange.png";
    private static final String CONNECTION_FAILED_ICON_FILE = "bullet_red.png";

    private ConnectionViewListener viewListener;

    private Text txtServer;
    private Text txtPort;
    private StackLayout sl_compositeConnection;
    private Composite compositeConnection;
    private Composite compositeConnecting;
    private Composite compositeConnected;
    private Composite compositeDisconnected;
    private Button btnConnect;
    private Button btnDefault;
    private Button btnCancel;
    private Button btnDisconnect;
    private Label lblConnecting;
    private Label lblConnectionHint1;
    private Label lblConnectionHint2;
    private Text txtConnectionHint;
    private Composite compositeFailed;
    private Label lblConnectionFailed;

    private static Image getImage(String file) {
        return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/" + file).createImage();
    }

    public ConnectionView() {
    }

    @PostConstruct
    public void createConrols(Composite parent) {
        viewListener = new ConnectionViewController(this);

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.VERTICAL));

        Group grpStatus = new Group(container, SWT.NONE);
        grpStatus.setLayout(new FillLayout(SWT.HORIZONTAL));
        grpStatus.setText("Connection Status");

        compositeConnection = new Composite(grpStatus, SWT.NONE);
        sl_compositeConnection = new StackLayout();
        sl_compositeConnection.marginHeight = 5;
        compositeConnection.setLayout(sl_compositeConnection);

        compositeConnected = new Composite(compositeConnection, SWT.NONE);
        GridLayout gl_compositeConnected = new GridLayout(3, false);
        gl_compositeConnected.verticalSpacing = 2;
        compositeConnected.setLayout(gl_compositeConnected);

        Canvas canvasConnectedIcon = new Canvas(compositeConnected, SWT.NONE);
        GridData gd_canvasConnectedIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_canvasConnectedIcon.widthHint = 16;
        gd_canvasConnectedIcon.heightHint = 16;
        canvasConnectedIcon.setLayoutData(gd_canvasConnectedIcon);

        canvasConnectedIcon.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Image image = getImage(CONNECTED_ICON_FILE);
                e.gc.drawImage(image, 0, 0);
                image.dispose();
            }
        });

        Label lblConnected = new Label(compositeConnected, SWT.NONE);
        lblConnected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblConnected.setText("Connected");

        compositeConnecting = new Composite(compositeConnection, SWT.NONE);
        GridLayout gl_compositeConnecting = new GridLayout(3, false);
        gl_compositeConnecting.verticalSpacing = 2;
        compositeConnecting.setLayout(gl_compositeConnecting);

        Canvas canvasConnectingIcon = new Canvas(compositeConnecting, SWT.NONE);
        GridData gd_canvasConnectingIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_canvasConnectingIcon.widthHint = 16;
        gd_canvasConnectingIcon.heightHint = 16;
        canvasConnectingIcon.setLayoutData(gd_canvasConnectingIcon);

        canvasConnectingIcon.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Image image = getImage(CONNECTING_ICON_FILE);
                e.gc.drawImage(image, 0, 0);
                image.dispose();
            }
        });

        lblConnecting = new Label(compositeConnecting, SWT.NONE);
        lblConnecting.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblConnecting.setText("Connecting...");

        btnCancel = new Button(compositeConnecting, SWT.NONE);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewListener.cancelButtonSelected();
            }
        });
        btnCancel.setText("Cancel");

        btnDisconnect = new Button(compositeConnected, SWT.NONE);
        btnDisconnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewListener.disconnectButtonSelected();
            }
        });
        btnDisconnect.setText("Disconnect");
        
        compositeFailed = new Composite(compositeConnection, SWT.NONE);
        compositeFailed.setLayout(new GridLayout(2, false));
        
        Canvas canvasConnectionFailedIcon = new Canvas(compositeFailed, SWT.NONE);
        GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_canvas.widthHint = 16;
        gd_canvas.heightHint = 16;
        canvasConnectionFailedIcon.setLayoutData(gd_canvas);
        
        lblConnectionFailed = new Label(compositeFailed, SWT.NONE);
        lblConnectionFailed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblConnectionFailed.setText("Connection failed. Make sure Rserve is listening on the specfied host and port.");

        canvasConnectionFailedIcon.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Image image = getImage(CONNECTION_FAILED_ICON_FILE);
                e.gc.drawImage(image, 0, 0);
                image.dispose();
            }
        });
        
        compositeDisconnected = new Composite(compositeConnection, SWT.NONE);
        compositeDisconnected.setLayout(new GridLayout(2, false));

        Canvas canvasDisconnectedIcon = new Canvas(compositeDisconnected, SWT.NONE);
        GridData gd_canvasDisconnectedIcon = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_canvasDisconnectedIcon.heightHint = 16;
        gd_canvasDisconnectedIcon.widthHint = 16;
        canvasDisconnectedIcon.setLayoutData(gd_canvasDisconnectedIcon);

        Label lblDisconnected = new Label(compositeDisconnected, SWT.NONE);
        lblDisconnected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblDisconnected.setText("Disconnected");

        canvasDisconnectedIcon.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Image image = getImage(DISCONNECTED_ICON_FILE);
                e.gc.drawImage(image, 0, 0);
                image.dispose();
            }
        });

        Group grpConnect = new Group(container, SWT.NONE);
        grpConnect.setText("Connect to R via Rserve");
        grpConnect.setLayout(new GridLayout(6, false));

        Label lblServer = new Label(grpConnect, SWT.NONE);
        lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblServer.setText("Server:");

        txtServer = new Text(grpConnect, SWT.BORDER);
        txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPort = new Label(grpConnect, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPort.setText("Port:");

        txtPort = new Text(grpConnect, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnConnect = new Button(grpConnect, SWT.NONE);
        btnConnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewListener.connectButtonSelected();
            }
        });
        btnConnect.setText("Connect");

        btnDefault = new Button(grpConnect, SWT.NONE);
        btnDefault.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setDefaultServerAndPort();
            }
        });
        btnDefault.setText("Default");
        new Label(grpConnect, SWT.NONE);

        lblConnectionHint1 = new Label(grpConnect, SWT.NONE);
        lblConnectionHint1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));
        lblConnectionHint1.setText("Before connecting, ensure there is an Rserve instance running.");
        new Label(grpConnect, SWT.NONE);

        lblConnectionHint2 = new Label(grpConnect, SWT.NONE);
        lblConnectionHint2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));
        lblConnectionHint2.setText("To start Rserve, execute the following command in R:");
        new Label(grpConnect, SWT.NONE);

        txtConnectionHint = new Text(grpConnect, SWT.BORDER);
        txtConnectionHint.setEditable(false);
        txtConnectionHint.setText("library(Rserve); Rserve()");
        txtConnectionHint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

        setDisconnected();
        setDefaultServerAndPort();
    }

    public void setDisconnected() {
        compositeConnection.getDisplay().asyncExec(() -> {
            sl_compositeConnection.topControl = compositeDisconnected;
            compositeConnection.layout();
            enableConnectionGroup(true);
        });
    }

    public void setConnecting() {
        compositeConnection.getDisplay().asyncExec(() -> {
            sl_compositeConnection.topControl = compositeConnecting;
            compositeConnection.layout();
            enableConnectionGroup(false);
            lblConnecting.setText("Connecting...");
        });
    }

    public void setConnecting(int retriesLeft) {
        compositeConnection.getDisplay().asyncExec(() -> {
            lblConnecting.setText("Connecting... (" + retriesLeft + " retries left)");
        });
    }

    public void setConnected() {
        compositeConnection.getDisplay().asyncExec(() -> {
            sl_compositeConnection.topControl = compositeConnected;
            compositeConnection.layout();
            enableConnectionGroup(false);
        });
    }

    public void setFailed() {
        compositeConnection.getDisplay().asyncExec(() -> {
            sl_compositeConnection.topControl = compositeFailed;
            compositeConnection.layout();
            enableConnectionGroup(true);
        });
    }

    public void setDefaultServerAndPort() {
        txtServer.getDisplay().asyncExec(() -> {
            txtServer.setText(RserveConnection.DEFAULT_HOST);
            txtPort.setText(Integer.toString(RserveConnection.DEFAULT_PORT));
        });
    }

    private void enableConnectionGroup(boolean enabled) {
        txtServer.getDisplay().asyncExec(() -> {
            txtServer.setEnabled(enabled);
            txtPort.setEnabled(enabled);
            btnConnect.setEnabled(enabled);
            btnDefault.setEnabled(enabled);
        });
    }

    public String getPort() {
        return txtPort.getText();
    }

    public String getServer() {
        return txtServer.getText();
    }

}
