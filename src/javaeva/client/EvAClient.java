package javaeva.client;

/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.Properties;

import javaeva.gui.ExtAction;
import javaeva.gui.JEFrame;
import javaeva.gui.JEFrameRegister;
import javaeva.gui.JExtMenu;
import javaeva.gui.JTabbedModuleFrame;
import javaeva.gui.LogPanel;
import javaeva.server.EvAServer;
import javaeva.server.modules.ModuleAdapter;
import javaeva.tools.EVAERROR;
import javaeva.tools.EVAHELP;
import javaeva.tools.Serializer;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import wsi.ra.jproxy.RemoteStateListener;
import wsi.ra.tool.BasicResourceLoader;

/**
/////////////////////////////////
// -Xrunhprof:cpu=samples
/////////////////////////////////////////////////
 /*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class EvAClient implements RemoteStateListener, Serializable {
	public static final String EVA_PROPERTY_FILE = "resources/JavaEvA.props";
	private static Properties EVA_PROPERTIES;
	public static final String iconLocation = "resources/images/JavaEvAIcon_3.gif";

	public static boolean TRACE = false;
	private static String m_ProductName = "JavaEvA";
//	private int PREFERRED_WIDTH = 680;
//	private int PREFERRED_HEIGHT = 550;
	private JWindow m_splashScreen;
	public JEFrame m_Frame;

	private EvAComAdapter m_ComAdapter;
//	private JExtDesktopPane m_Desktop;
	private transient JMenuBar 		m_barMenu;
	private transient JExtMenu 		m_mnuAbout;
	private transient JExtMenu 		m_mnuSelHosts;
	private transient JExtMenu 		m_mnuModule;
	private transient JExtMenu 		m_mnuWindow;
	private transient JExtMenu 		m_mnuOptions;
	private transient JProgressBar	m_ProgressBar;

//	public ArrayList m_ModulGUIContainer = new ArrayList();
	// LogPanel
	private LogPanel m_LogPanel;

	// Module:
	private ExtAction m_actModuleLoad;
	// GUI:

	// Hosts:
	private ExtAction m_actHost;
	private ExtAction m_actAvailableHost;
	private ExtAction m_actKillHost;
	private ExtAction m_actKillAllHosts;
//	private ArrayList m_ModuleAdapterList = new ArrayList();
	// About:
	private ExtAction m_actAbout;

//	private JPanel m_panelTool;
//	private FrameCloseListener m_frameCloseListener;
//	private JFileChooser m_FileChooser;

//	if not null, the module is loaded automatically and no other can be selected
	private String useDefaultModule = null;//"Genetic_Optimization";
	private boolean showLoadModules = false;
	private boolean localMode = false;
	// This variable says whether, if running locally, a local server should be addressed by RMI.
	// False should be preferred here to avoid overhead
	private boolean useLocalRMI = false;
	// measuring optimization runtime
	private long startTime = 0;
	// remember the module in use
	private transient String currentModule = null;

	public static String getProperty(String key) {
		String myVal = EVA_PROPERTIES.getProperty(key);
		return myVal;
	}

	public static Properties getProperties() {
		return EVA_PROPERTIES;
	}
	
	public static void setProperty(String key, String value) {
		EVA_PROPERTIES.setProperty(key, value);
	}

	/**
	 * Statically loading Properties.
	 */
	static {
		try {
			EVA_PROPERTIES = BasicResourceLoader.readProperties(EVA_PROPERTY_FILE);
		} catch (Exception ex) {
			System.err.println("Could not read the configuration file "+ EVA_PROPERTY_FILE);
			ex.printStackTrace();
		}
	}

	/**
	 * Constructor of GUI of JavaEva.
	 * Works as client for the JavaEva server.
	 *
	 */
	public EvAClient(String hostName) {
		createSplashScreen();
		currentModule = null;
		
		m_ComAdapter = EvAComAdapter.getInstance();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (m_splashScreen != null) m_splashScreen.setVisible(true);
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//init();
				try {
					Thread.sleep(6000);
				} catch (Exception e) {
					System.out.println("error" + e.getMessage());
				}
				if (m_splashScreen != null) m_splashScreen.setVisible(false);
				m_splashScreen = null;
			}
		});
		init(hostName);

	}

	/**
	 *
	 */
	private void init(String hostName) {
		//EVA_EDITOR_PROPERTIES
		useDefaultModule = getProperty("DefaultModule");
		
		if (useDefaultModule != null) {
			useDefaultModule = useDefaultModule.trim();
			if (useDefaultModule.length() < 1) useDefaultModule = null;
		}
		

		m_Frame = new JEFrame();
		BasicResourceLoader loader = BasicResourceLoader.instance();
		byte[] bytes = loader.getBytesFromResourceLocation(iconLocation);
		try {
			m_Frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
		} catch (java.lang.NullPointerException e) {
			System.out.println("Could not find JavaEvA icon, please move rescoure folder to working directory!");
		}
		m_Frame.setTitle("JavaEvA workbench");

		try {
			Thread.sleep(200);
		} catch (Exception e) {
			System.out.println("Error" + e.getMessage());
		}

		m_Frame.getContentPane().setLayout(new BorderLayout());
		m_LogPanel = new LogPanel();
		m_Frame.getContentPane().add(m_LogPanel, BorderLayout.CENTER);
		m_ProgressBar = new JProgressBar();
		m_Frame.getContentPane().add(m_ProgressBar, BorderLayout.SOUTH);

		if (getProperty("ShowModules") != null) showLoadModules = true;
		else showLoadModules = false; // may be set to true again if default module couldnt be loaded
		
		createActions();

		if (useDefaultModule != null) {
			loadModuleFromServer(useDefaultModule);//loadSpecificModule
		}
		
		buildMenu();

		m_Frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing JavaEvA Client. Bye!");
				System.exit(1);
			}
		});

		if (m_ComAdapter != null) {
			if (hostName != null) selectHost(hostName);
			m_ComAdapter.setLogPanel(m_LogPanel);
			logMessage("Selected Host: " + m_ComAdapter.getHostName());
		}
//		m_mnuModule.setText("Select module");
//		m_mnuModule.repaint();
		
		m_LogPanel.logMessage("Class path is: " + System.getProperty("java.class.path","."));

		if (!(m_Frame.isVisible())) {
			m_Frame.pack();
			m_Frame.setVisible(true);
		}
	}

	/**
	 *  Create the JavaEvA splash screen.
	 */
	public void createSplashScreen() {
		BasicResourceLoader loader = BasicResourceLoader.instance();
		byte[] bytes = loader.getBytesFromResourceLocation("resources/images/JavaEvaSplashScreen.png");
		try {
			ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
			JLabel splashLabel = new JLabel(ii);
			m_splashScreen = new JWindow();
			m_splashScreen.getContentPane().add(splashLabel);
			m_splashScreen.pack();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//splashScreen.setSize(screenSize);
			m_splashScreen.setLocation(screenSize.width / 2 - m_splashScreen.getSize().width / 2, screenSize.height / 2 - m_splashScreen.getSize().height / 2);
		} catch (java.lang.NullPointerException e) {
			System.err.println("Could not find JavaEvA splash screen, please move rescoure folder to working directory!");
		}

	}

	/**
	 * The one and only main of the client programm.
	 *
	 * @param args command line parameters
	 */
	public static void main(String[] args) {
		if (TRACE) {
			System.out.println(EVAHELP.getSystemPropertyString());
		}
		EvAClient Client = new EvAClient((args.length == 1) ? args[0] : null);
	}

//	/**
//	*
//	*/
//	public void addInternalFrame(JInternalFrame newFrame) {
//	m_Desktop.add(newFrame);
//	newFrame.toFront();
//	}

	/**
	 *
	 */
	private void createActions() {
		//////////////////////////////////////////////////////////////
		// Module:
		/////////////////////////////////////////////////////////////
		m_actModuleLoad = new ExtAction("&Load", "Load Module",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				loadModuleFromServer(null);
			}
		};

		m_actAbout = new ExtAction("&About...", "Product Information",
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showAboutDialog();
			}
		};
		m_actHost = new ExtAction("&List of all servers", "All servers in list",
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				selectAvailableHost(m_ComAdapter.getHostNameList());
			}
		};
		m_actAvailableHost = new ExtAction("Available &Server", "Available server",
				KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAvailableHost(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		m_actKillHost = new ExtAction("&Kill server", "Kill server process on selected host",
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAvailableHostToKill(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		m_actKillAllHosts = new ExtAction("Kill &all servers", "Kill all servers",
				KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK)) {
			public void actionPerformed(ActionEvent e) {
				logMessage(e.getActionCommand());
				showPleaseWaitDialog();
				Thread xx = new Thread() {
					public void run() {
						selectAllAvailableHostToKill(m_ComAdapter.getAvailableHostNameList());
					}
				};
				xx.start();
			}
		};
		/* m_actStartServerManager = new ExtAction("Start &Server Manager", "Start &Server Manager",
             KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK)){
             public void actionPerformed(ActionEvent e){
               m_LogPanel.logMessage(e.getActionCommand());
               ServerStartFrame sm = new ServerStartFrame(m_ComAdapter.getHostNameList());
             }
           };
		 */
	}

	/**
	 *
	 */
	private void buildMenu() {
		m_barMenu = new JMenuBar();
		m_Frame.setJMenuBar(m_barMenu);
		////////////////////////////////////////////////////////////////////////////
		JExtMenu mnuLookAndFeel = new JExtMenu("&Look and Feel");
		ButtonGroup grpLookAndFeel = new ButtonGroup();
		UIManager.LookAndFeelInfo laf[] = UIManager.getInstalledLookAndFeels();
//		if (TRACE) {
//		for (int i=0;i<3;i++)
//		System.out.println(laf[i].getName());
//		System.out.println ("->"+UIManager.getLookAndFeel().getClass().getName());
//		}

		String LAF = Serializer.loadString("LookAndFeel.ser");

		boolean lafSelected = false;
		for (int i = 0; i < laf.length; i++) {
			JRadioButtonMenuItem mnuItem = new JRadioButtonMenuItem(laf[i].getName());
			mnuItem.setActionCommand(laf[i].getClassName());
			if (!lafSelected && laf[i].getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
//			if (!lafSelected && laf[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
//				if (LAF==null) {// do this only if no older selection one could be loaded
//					LAF = laf[i].getClassName(); // set for later selection
//				} // this causes problems with my gnome!
				if (LAF == null) {
					lafSelected = true;
					mnuItem.setSelected(true);
				}
			}
			mnuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						UIManager.setLookAndFeel(e.getActionCommand());
						SwingUtilities.updateComponentTreeUI(m_Frame);
						// hier noch reinhacken dass alle frame geupdated werden.
						m_Frame.pack();
//						m_Frame.setSize(new Dimension(900, 700));
//						m_Frame.setVisible(true);
						Serializer.storeString("LookAndFeel.ser", e.getActionCommand());
					} catch (ClassNotFoundException exc) {} catch (InstantiationException exc) {} catch (UnsupportedLookAndFeelException exc) {} catch (
							IllegalAccessException exc) {}
				}
			});
			mnuLookAndFeel.add(mnuItem);
			grpLookAndFeel.add(mnuItem);
		}
		if (LAF != null) {
			try {
				UIManager.setLookAndFeel(LAF);
				SwingUtilities.updateComponentTreeUI(m_Frame);
				m_Frame.pack();
//				m_Frame.setSize(new Dimension(900, 700));
//				m_Frame.setVisible(true);
			} catch (ClassNotFoundException exc) {} catch (InstantiationException exc) {} catch (UnsupportedLookAndFeelException exc) {} catch (
					IllegalAccessException exc) {}
		}
		m_mnuModule = new JExtMenu("Select &module");
		m_mnuModule.add(m_actModuleLoad);

		////////////////////////////////////////////////////////////////

		m_mnuWindow = new JExtMenu("&Window");
		m_mnuWindow.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
//				System.out.println("Selected");
				m_mnuWindow.removeAll();
				Object[] framelist = JEFrameRegister.getFrameList();
				for (int i = 0; i < framelist.length; i++) {
					JMenuItem act = new JMenuItem((i + 1) + ". " + ((JEFrame) framelist[i]).getTitle());
					final JFrame x = ((JEFrame) framelist[i]);
					act.addActionListener((new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							x.setExtendedState(JFrame.NORMAL);
							x.toFront();
						}
					}
					)
					);
					m_mnuWindow.add(act);
				}

			}

			public void menuCanceled(MenuEvent e) {

			}

			public void menuDeselected(MenuEvent e) {

			}
		}
		);

		////////////////////////////////////////////////////////////////
		m_mnuSelHosts = new JExtMenu("&Select Hosts");
		m_mnuSelHosts.setToolTipText("Select a host for the server application");
		//if (EvAClient.LITE_VERSION == false)
		m_mnuSelHosts.add(m_actHost);
		m_mnuSelHosts.add(m_actAvailableHost);
		m_mnuSelHosts.addSeparator();
		m_mnuSelHosts.add(m_actKillHost);
		m_mnuSelHosts.add(m_actKillAllHosts);
//		m_mnuOptions.add(m_actStartServerManager);
		////////////////////////////////////////////////////////////////
		m_mnuAbout = new JExtMenu("&About");
		m_mnuAbout.add(m_actAbout);
		//////////////////////////////////////////////////////////////
		// m_barMenu.add(m_Desktop.getWindowMenu());
		
		m_mnuOptions = new JExtMenu("&Options");
		m_mnuOptions.add(mnuLookAndFeel);
		m_mnuOptions.add(m_mnuSelHosts);
		//m_barMenu.add(m_mnuSelHosts);
		// this is accessible if no default module is given
		if (showLoadModules) {
			m_barMenu.add(m_mnuModule);
		}

		m_barMenu.add(m_mnuOptions);
		m_barMenu.add(m_mnuWindow);
		m_barMenu.add(m_mnuAbout);

	}
	
	protected void logMessage(String msg) {
		if (TRACE || m_LogPanel == null) System.out.println(msg);
		if (m_LogPanel != null) m_LogPanel.logMessage(msg);
	}

	/**
	 *
	 */
	private void loadModuleFromServer(String selectedModule) {
		if (m_ComAdapter.getHostName() == null) {
			System.err.println("error in loadModuleFromServer!");
			return;
		}
		if (m_ComAdapter.getHostName().equals("localhost")) {
			localMode = true;
			if (useLocalRMI) {
				EvAServer Server = new EvAServer(true, false);
				m_ComAdapter.setLocalRMIServer(Server.getRMIServer());
				logMessage("Local EvAServer started");
				m_ComAdapter.setRunLocally(false); // this is not quite true but should have the desired effect
			} else {
				logMessage("Working locally");
				m_ComAdapter.setLocalRMIServer(null);
				m_ComAdapter.setRunLocally(true);
			}
		} else {
			localMode = false;
			if (TRACE) logMessage("Using RMI on m_ComAdapter.getHostName()");
			m_ComAdapter.setRunLocally(false);
		}
		if (selectedModule == null) { // show a dialog and ask for a module
			String[] ModuleNameList = m_ComAdapter.getModuleNameList();
			if (ModuleNameList == null) {
				JOptionPane.showMessageDialog(m_Frame.getContentPane(), "No modules available on " + m_ComAdapter.getHostName(), "JavaEvA Information", 1);
			} else {
				String LastModuleName = Serializer.loadString("lastmodule.ser");
				if (LastModuleName == null) LastModuleName = ModuleNameList[0];
				selectedModule = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
						"Which module do you want \n to load on host: " + m_ComAdapter.getHostName() + " ?", "Load optimization module on host",
						JOptionPane.QUESTION_MESSAGE,
						null,
						ModuleNameList,
						LastModuleName);
			}
		}
		if (selectedModule == null) {
			System.err.println("not loading any module");
		} else {
			Serializer.storeString("lastmodule.ser", selectedModule);

			loadSpecificModule(selectedModule);

			m_actHost.setEnabled(true);
			m_actAvailableHost.setEnabled(true);
			logMessage("Selected Module: " + selectedModule);
//			m_LogPanel.statusMessage("Selected Module: " + selectedModule);
		}
	}

	private void loadSpecificModule(String selectedModule) {
		ModuleAdapter newModuleAdapter = null;
		//
		try {
			newModuleAdapter = m_ComAdapter.getModuleAdapter(selectedModule);
		} catch (Exception e) {
			logMessage("Error while m_ComAdapter.GetModuleAdapter Host: " + e.getMessage());
			e.printStackTrace();
			EVAERROR.EXIT("Error while m_ComAdapter.GetModuleAdapter Host: " + e.getMessage());
		}
		if (newModuleAdapter == null) showLoadModules = true;
		else {
			newModuleAdapter.setConnection(!localMode);
			if (m_ComAdapter.isRunLocally()) {
				// TODO in rmi-mode this doesnt work yet!
				newModuleAdapter.addRemoteStateListener((RemoteStateListener)this);
			}
			try {
				// this (or rather: JModuleGeneralPanel) is where the start button etc come from!
				JTabbedModuleFrame Temp = newModuleAdapter.getModuleFrame();
//				newModuleAdapter.setLogPanel(m_LogPanel);

				JPanel moduleContainer = Temp.createContentPane(); // MK the frame is actually painted in here
				//			m_Frame.setLayout(new BorderLayout());
				m_Frame.setVisible(false);
				m_Frame.getContentPane().removeAll();
				
				// nested info-panel so that we can stay with simple borderlayouts
				JPanel infoPanel = new JPanel();
				infoPanel.setLayout(new BorderLayout());
				infoPanel.add(m_ProgressBar, BorderLayout.SOUTH);
				infoPanel.add(m_LogPanel, BorderLayout.NORTH);
				
				m_Frame.add(Temp.getToolBar(), BorderLayout.NORTH);
				m_Frame.add(moduleContainer, BorderLayout.CENTER);
				//m_Frame.add(m_ProgressBar, BorderLayout.CENTER);
				//m_Frame.add(m_LogPanel, BorderLayout.SOUTH);
				m_Frame.add(infoPanel, BorderLayout.SOUTH);

				m_Frame.pack();
				m_Frame.setVisible(true);

				currentModule = selectedModule;
				//			m_ModulGUIContainer.add(Temp);
			} catch (Exception e) {
				currentModule = null;
				e.printStackTrace();
				logMessage("Error while newModulAdapter.getModulFrame(): " + e.getMessage());
				EVAERROR.EXIT("Error while newModulAdapter.getModulFrame(): " + e.getMessage());
			}
//			try { TODO whats this?
//				newModuleAdapter.setConnection(true);
//			} catch (Exception e) {
//				e.printStackTrace();
//				m_LogPanel.logMessage("Error while m_ComAdapter.AddRMIPlotListener Host: " + e.getMessage());
//				EVAERROR.EXIT("Error while m_ComAdapter.AddRMIPlotListener: " + e.getMessage());
//			}
			// set mode (rmi or not)

			// ModuladapterListe adden
//			m_ModuleAdapterList.add(newModuleAdapter);
		}
	}

	/**
	 *
	 */
	private void selectAvailableHost(String[] HostNames) {
		if (TRACE) System.out.println("SelectAvailableHost");
		if (HostNames == null || HostNames.length == 0) {
			showNoHostFoundDialog();
		} else {
			String HostName = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
					"Which active host do you want to connect to?", "Host", JOptionPane.QUESTION_MESSAGE, null,
					HostNames, m_ComAdapter.getHostName());
			if (HostName != null) selectHost(HostName);
		}
	}
	
	private void selectHost(String hostName) {
		m_ComAdapter.setHostName(hostName);
		logMessage("Selected Host: " + hostName);
		if (currentModule != null) {
			logMessage("Reloading module from server...");
			loadModuleFromServer(currentModule);
		}
		
//		m_mnuModule.setText("Select module");
//		m_mnuModule.repaint();
	//			System.out.println(HostName + " selected");
	}
	
	private void showPleaseWaitDialog() {
		JOptionPane.showMessageDialog(m_Frame.getContentPane(), "Please wait one moment.", "JavaEvA Information", 1);
	}
	
	private void showAboutDialog() {
		JOptionPane.showMessageDialog
		(m_Frame,
				m_ProductName +
				"\n University of Tuebingen\n Computer Architecture\n Holger Ulmer & Felix Streichert & Hannes Planatscher \n Prof. Dr. Andreas Zell \n (c) 2007 \n Version " +
				EvAServer.Version + " \n http://www-ra.informatik.uni-tuebingen.de/software/JavaEvA/", "JavaEvA Information", 1);
	}
	
	private void showNoHostFoundDialog() {
		JOptionPane.showMessageDialog(m_Frame.getContentPane(), "No host with running EVASERVER found. Please start one or \nadd the correct address to the properties list.", "JavaEvA Information", 1);
	}

	/**
	 *
	 */
	private void selectAvailableHostToKill(String[] HostNames) {
		if (TRACE) System.out.println("SelectAvailableHostToKill");
		if (HostNames == null || HostNames.length == 0) {
			showNoHostFoundDialog();
			return;
		}
		String HostName = (String) JOptionPane.showInputDialog(m_Frame.getContentPane(),
				"Which server do you want to be killed ?", "Host", JOptionPane.QUESTION_MESSAGE, null,
				HostNames, m_ComAdapter.getHostName());
		if (HostName == null)
			return;
		logMessage("Kill host process on = " + HostName);
		m_ComAdapter.killServer(HostName);
//		m_LogPanel.statusMessage("");
	}

	/**
	 *
	 */
	private void selectAllAvailableHostToKill(String[] HostNames) {
		System.out.println("SelectAllAvailableHostToKill");
		if (HostNames == null || HostNames.length == 0) {
			System.out.println("no host is running");
			return;
		}
		m_ComAdapter.killAllServers();
	}

	public void performedRestart(String infoString) {
		logMessage("Restarted " + infoString);
		startTime = System.currentTimeMillis();
	}

	public void performedStart(String infoString) {
		logMessage("Started " + infoString);
		startTime = System.currentTimeMillis();
	}

	public void performedStop() {
		long t = (System.currentTimeMillis() - startTime);
		logMessage(String.format("Stopped after %1$d.%2$tL s", (t / 1000), (t % 1000)));
	}
	
    /**
     * When the worker needs to update the GUI we do so by queuing
     * a Runnable for the event dispatching thread with
     * SwingUtilities.invokeLater().  In this case we're just
     * changing the progress bars value.
     */
	public void updateProgress(final int percent) {
        if (this.m_ProgressBar != null) {
            Runnable doSetProgressBarValue = new Runnable() {
                public void run() {
                    m_ProgressBar.setValue(percent);
                }
            };
            SwingUtilities.invokeLater(doSetProgressBarValue);
        }
    }
}