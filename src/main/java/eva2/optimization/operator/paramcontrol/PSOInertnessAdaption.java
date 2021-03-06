package eva2.optimization.operator.paramcontrol;

import eva2.gui.editor.GenericObjectEditor;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Adapt PSO inertness linearly by time, from given start to end value.
 * This only works if iterations are known. The new variant allows exponential adaption,
 * where the second parameter (endV) is interpreted as halfing time in percent of the
 * full run.
 */
@Description("Adapt the inertnessOrChi value of PSO.")
public class PSOInertnessAdaption extends LinearParamAdaption implements Serializable {

    public PSOInertnessAdaption() {
        super("inertnessOrChi", 0.7, 0.2);
    }

    public void hideHideable() {
        GenericObjectEditor.setHideProperty(this.getClass(), "controlledParam", true);
    }

    @Override
    public String startVTipText() {
        return "Start value for the inertness";
    }

    @Override
    public String endVTipText() {
        return "End value for the inertness";
    }
}
