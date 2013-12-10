package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.06.2005
 * Time: 14:59:23
 * To change this template use File | Settings | File Templates.
 */
public class F11Problem extends AbstractProblemDoubleOffset implements InterfaceMultimodalProblem, java.io.Serializable {

    private double m_D = 4000;

    public F11Problem() {
        this.problemDimension = 10;
        this.template = new ESIndividualDoubleData();
        setDefaultRange(600);
    }

    public F11Problem(F11Problem b) {
        super(b);
        this.m_D = b.m_D;
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F11Problem(this);
    }

    /**
     * Ths method allows you to evaluate a double[] to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evaluate(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        double tmpProd = 1;
        for (int i = 0; i < x.length; i++) {
            double xi = x[i] - xOffset;
            result[0] += Math.pow(xi, 2);
            tmpProd *= Math.cos((xi) / Math.sqrt(i + 1));
        }
        result[0] = ((result[0] / this.m_D) - tmpProd + 1) + yOffset;
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F11 Griewank Function:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
//        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "F11-Problem";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Griewank Function";
    }

    /**
     * This method allows you to set/get d.
     *
     * @param d The d.
     */
    public void setD(double d) {
//        if (d < 1) d = 1;// how can this be limited to [1,2] if 4000 is default?
//        if (d > 2) d = 2;// MK FIXED: this obviously was a copy-paste error from F10
        this.m_D = d;
    }

    public double getD() {
        return this.m_D;
    }

    public String dTipText() {
        return "Set D (=4000).";
    }
}