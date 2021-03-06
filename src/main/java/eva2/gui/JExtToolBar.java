package eva2.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 */
public class JExtToolBar extends JToolBar {
    @Override
    public JButton add(Action a) {
        JButton button = super.add(a);
        button.setText(null);
        button.setMargin(new Insets(0, 0, 0, 0));

        Object o;
        o = a.getValue(ExtAction.TOOLTIP);
        String toolTip = o != null ? (String) o : "";

        o = a.getValue(ExtAction.KEYSTROKE);
        button.setToolTipText(toolTip + getKeyText((KeyStroke) o));

        return button;
    }

    private String getKeyText(KeyStroke k) {
        StringBuilder result = new StringBuilder();

        if (k != null) {
            int modifiers = k.getModifiers();
            if (modifiers > 0) {
                result.append(KeyEvent.getKeyModifiersText(modifiers)).append("+");
            }
            result.append(KeyEvent.getKeyText(k.getKeyCode()));
        }
        if (result.length() > 0) {
            result.insert(0, " [");
            result.append("]");
        }

        return result.toString();
    }

    @Override
    protected PropertyChangeListener createActionChangeListener(JButton b) {
        return new ExtActionChangedListener(b) {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                JButton button = (JButton) component;

                String propertyName = e.getPropertyName();
                if (propertyName.equals(Action.NAME)) {
                    /* Nichts tun! */
                } else if (propertyName.equals("enabled")) {
                    button.setEnabled((Boolean) e.getNewValue());
                    button.repaint();
                } else if (e.getPropertyName().equals(Action.SMALL_ICON)) {
                    button.setIcon((Icon) e.getNewValue());
                    button.invalidate();
                    button.repaint();
                } else if (propertyName.equals(ExtAction.TOOLTIP) || propertyName.equals(ExtAction.KEYSTROKE)) {
                    Action source = (Action) e.getSource();

                    Object o = source.getValue(ExtAction.TOOLTIP);
                    String toolTip = o != null ? (String) o : "";
                    o = source.getValue(ExtAction.KEYSTROKE);
                    button.setToolTipText(toolTip + getKeyText((KeyStroke) o));
                }
            }
        };
    }
}
