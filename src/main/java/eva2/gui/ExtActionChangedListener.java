package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 */
public abstract class ExtActionChangedListener implements PropertyChangeListener {
    protected JComponent component;

    /**
     *
     */
    ExtActionChangedListener(JComponent c) {
        super();
        setTarget(c);
    }

    /**
     *
     */
    @Override
    public abstract void propertyChange(PropertyChangeEvent e);

    /**
     *
     */
    public void setTarget(JComponent c) {
        component = c;
    }
}
