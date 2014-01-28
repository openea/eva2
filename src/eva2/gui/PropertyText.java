package eva2.gui;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;

/**
 * A text property editor view. Updates the editor on key release and lost focus
 * events.
 */
public class PropertyText extends JTextField {

    private PropertyEditor propertyEditor;

    /**
     *
     */
    public PropertyText(PropertyEditor pe) {
        super(pe.getAsText());
        this.setBorder(BorderFactory.createEmptyBorder());
        propertyEditor = pe;
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                //if (e.getKeyCode() == KeyEvent.VK_ENTER)
                updateEditor();
            }
        });
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                updateEditor();
            }
        });
    }

    /**
     *
     */
    protected void updateEditor() {
        try {
            String x = getText();
            if (!propertyEditor.getAsText().equals(x)) {
                propertyEditor.setAsText(x);
//				setText(editor.getAsText());
            }
        } catch (IllegalArgumentException ex) {
//			System.err.println("Warning: Couldnt set value (PropertyText)");
        }
    }

    public boolean checkConsistency() {
        String x = getText();
        return x.equals(propertyEditor.getAsText());
    }

    public void updateFromEditor() {
        setText(propertyEditor.getAsText());
    }
}
