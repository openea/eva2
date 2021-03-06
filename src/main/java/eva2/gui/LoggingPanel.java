package eva2.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Simple logging panel that shows logs produced by EvA2
 */
public class LoggingPanel extends JPanel {
    protected static Logger LOGGER = Logger.getLogger(LoggingPanel.class.getName());
    protected JTextArea loggingTextArea = new JTextArea(10, 20);
    protected Handler loggingHandler;

    /**
     *
     */
    public LoggingPanel() {
        loggingTextArea.setEditable(false);
        loggingTextArea.setLineWrap(true);
        loggingTextArea.setBorder(BorderFactory.createEmptyBorder());

        setLayout(new BorderLayout());

        add(new JLabel("Info"), BorderLayout.PAGE_START);

        this.loggingHandler = new LoggingHandler(this);

        // Create default logger at namespace root eva2
        Logger rootLogger = Logger.getLogger("eva2");
        rootLogger.addHandler(loggingHandler);

        final JScrollPane scrollpane = new JScrollPane(loggingTextArea);
        scrollpane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//		scrollpane.setAutoscrolls(false);
        add(scrollpane, BorderLayout.CENTER);
        scrollpane.getViewport().addChangeListener(new ChangeListener() {
            private int lastHeight;

            //
            @Override
            public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport) e.getSource();
                int height = viewport.getViewSize().height;
                if (height != lastHeight) {
                    lastHeight = height;
                    int x = height - viewport.getExtentSize().height;
                    viewport.setViewPosition(new Point(0, x));
                }
            }
        });
    }

    /**
     *
     */
    protected static String getTimestamp() {
        return (new SimpleDateFormat("HH:mm:ss:")).format(new Date());
    }

    /**
     *
     */
    public void logMessage(String message) {
        loggingTextArea.append(LoggingPanel.getTimestamp() + ' ' + message);
        loggingTextArea.append("\n");
    }
}

class LoggingHandler extends Handler {
    protected LoggingPanel loggingPanel;

    public LoggingHandler(LoggingPanel loggingPanel) {
        this.loggingPanel = loggingPanel;
    }

    @Override
    public void publish(LogRecord record) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("[");
        sBuilder.append(record.getLevel().toString());
        sBuilder.append("] ");
        MessageFormat messageFormat = new MessageFormat(record.getMessage());
        sBuilder.append(messageFormat.format(record.getParameters()));
        // Show message on LogPanel
        this.loggingPanel.logMessage(sBuilder.toString());
    }

    @Override
    public void flush() {
        /*
         * We do nothing here as we don't buffer the entries
		 */
    }

    @Override
    public void close() throws SecurityException {
        /*
		 * Nothing to close
		 */
    }
}