package eva2.tools.jproxy;
/**
 * Title:        The JProxy Framework
 * Description:  API for distributed and parallel computing.
 * Copyright:    Copyright (c) 2004
 * Company:      University of Tuebingen
 * @version:  $Revision: 1.1 $
 *            $Date: 2004/04/15 09:12:31 $
 *            $Author: ulmerh $
 */

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RMIServer {
    /*
     * Version string of the server application.
     */

    protected static RMIServer instance;
    /*
     * Name of host on which the server is running.
     */
    private String myHostName = "undefined";
    /*
     * IP of host on which the server is running.
     */
    private String myHostIP = "undefined";
    /*
     * MainAdapterImp object. This is need for the first connection between the server and the
     * client program.
     */
    protected MainAdapter mainRemoteObject;
    /*
     * String describing the properties of the enviroment.
     */
//  private ComAdapter m_ComAdapter;
    protected static String userName;
    protected static int numberOfVM = 0;
    private Registry myRegistry = null;
    protected static final Logger LOGGER = Logger.getLogger(RMIServer.class.getName());

    /**
     *
     */
    public static RMIServer getInstance() {
        if (instance == null) {
            instance = new RMIServer();
        }
        return instance;
    }

    /**
     * Constructor of EvAServer. Calls RMIConnection().
     */
    protected RMIServer() {
        userName = System.getProperty("user.name");
        initConnection();
    }

    /**
     * Main method of this class. Is the starting point of the server application.
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Start RMIServer !");
        RMIServer application = RMIServer.getInstance();
    }

    /**
     * Launches the RMIRegistry and makes the registration of the MainAdapterImpl class at the
     * rmiregistry.
     *
     * @param
     */
    private void initConnection() {
        String mainAdapterName = userName + MainAdapterImpl.MAIN_ADAPTER_NAME;
        System.setProperty("java.security.policy", "server.policy");
        launchRMIRegistry();
        try {
            myHostIP = InetAddress.getLocalHost().getHostAddress();
            myHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting HostName " + e.getMessage(), e);
        }
        LOGGER.log(Level.INFO, "Start of EvA RMI-Server on host " + myHostName + " = " + myHostIP);

        try {
            String[] list = Naming.list("rmi://localhost:" + MainAdapterImpl.PORT);
            numberOfVM = getNumberOfVM(list);
        } catch (RemoteException e) {
            LOGGER.log(Level.WARNING, "No RMI registry available yet");
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "MalformedURLException: Error while looking up " + ex.getMessage(), ex);
        }
        createMainRemoteObject(mainAdapterName);

        LOGGER.log(Level.INFO, "End of RMI-Server Initialisation");
        LOGGER.log(Level.INFO, "Host: " + myHostName + " = " + myHostIP + ", adapter name is " + mainAdapterName);
        LOGGER.log(Level.INFO, "Waiting for a client.");
    }

    protected void createMainRemoteObject(String mainAdapterName) {
        try {
            mainRemoteObject = new MainAdapterImpl();
            mainRemoteObject =
                    (MainAdapter) RMIProxyLocal.newInstance(mainRemoteObject, mainAdapterName + "_" + numberOfVM);
            mainRemoteObject.setRemoteThis(mainRemoteObject);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not create main remote object!", ex);
        }
    }

    /**
     *
     */
    public MainAdapter getMainRemoteObject() {
        return mainRemoteObject;
    }

    /**
     * Install RMIregistry on default port !!
     */
    private void launchRMIRegistry() {
        try {
            myRegistry = java.rmi.registry.LocateRegistry.createRegistry(MainAdapterImpl.PORT);
        } catch (Exception ex) {
            myRegistry = null;
        }
        if (myRegistry == null) {
            LOGGER.log(Level.INFO, "Try to get registry with getRegistry on port " + MainAdapterImpl.PORT);
            try {
                myRegistry = java.rmi.registry.LocateRegistry.getRegistry(MainAdapterImpl.PORT);
            } catch (RemoteException e) {
                myRegistry = null;
            }
        }
        if (myRegistry == null) {
            LOGGER.log(Level.WARNING, "Got no RMIREGISTRY");
        }
    }

    /**
     *
     */
    private int getNumberOfVM(String[] list) {
        int ret = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].indexOf(MainAdapterImpl.MAIN_ADAPTER_NAME) != -1) {
                ret++;
            }
        }
        return ret;
    }
}
