package eva2.optimization.mocco;

import eva2.gui.MOCCOStandalone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public abstract class MOCCOPhase implements InterfaceProcessElement {

    public MOCCOStandalone mocco;
    public volatile boolean hasFinished = false;

    /**
     * This method will call the initialize method and will go to stall
     */
    @Override
    public abstract void initProcessElementParametrization();

    /**
     * This method will wait for the parametrisation result
     *
     * @return boolean  Result
     */
    @Override
    public boolean isFinished() {
        return this.hasFinished;
    }

    /**
     * Save the stuff to *.ser file for offline optimization
     */
    ActionListener saveState2FileForOfflineOptimization = event -> {
        // @todo
    };

    /**
     * This method makes a helptext element similar to that used in EvA
     *
     * @param help The text to display
     * @return the helptext component
     */
    public JComponent makeHelpText(String help) {
        return this.makeInformationText("Info", help);
    }

    /**
     * This method makes a helptext element similar to that used in EvA
     *
     * @param title The title of the help text
     * @param help  The text to display
     * @return the helptext component
     */
    public JComponent makeInformationText(String title, String help) {
        JPanel result = new JPanel();
        JTextArea jt = new JTextArea();
        jt.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jt.setEditable(false);
        jt.setLineWrap(true);
        jt.setWrapStyleWord(true);
        jt.setText(help);
        jt.setBackground(result.getBackground());
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        result.setLayout(new BorderLayout());
        result.add(jt, BorderLayout.CENTER);
        return result;
    }
}
