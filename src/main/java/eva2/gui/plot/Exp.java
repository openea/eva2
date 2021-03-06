package eva2.gui.plot;

import eva2.tools.chart2d.DFunction;

/**
 * Represents an exponential scaling function.
 * MK: added a guard against undefined values: the smallest positive position can
 * be stored and used instead of invalid points.
 */
public class Exp extends DFunction {
    private double minValue = 1e-10; // think of a minimal value we want to show in case invalid (<=0) values are requested

    public void setMinValue(double v) {
        if (v > 0) {
            minValue = v;
        } else {
            System.err.println("Error, minimal value for Exp must be positive!");
        }
    }

    /*
     * (non-Javadoc)
     * @see eva2.tools.chart2d.DFunction#isDefinedAt(double)
     */
    @Override
    public boolean isDefinedAt(double source) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see eva2.tools.chart2d.DFunction#isInvertibleAt(double)
     */
    @Override
    public boolean isInvertibleAt(double image) {
        return image > 0;
    }

    /*
     * (non-Javadoc)
     * @see eva2.tools.chart2d.DFunction#getImageOf(double)
     */
    @Override
    public double getImageOf(double source) {
        return Math.exp(source);
    }

    /*
     * (non-Javadoc)
     * @see eva2.tools.chart2d.DFunction#getSourceOf(double)
     */
    @Override
    public double getSourceOf(double target) {
        if (target <= 0) {
            return Math.log(minValue); // think of a minimal value we want to show in case invalid values are requested
        }
        return Math.log(target);
    }

    public void updateMinValue(double y) {
        if (y < minValue && (y > 0)) {
            minValue = y;
        }
    }
}
