package eva2.problems.regression;

import eva2.util.annotation.Description;

/**
 */
@Description("This target function is given in Koza GP I chapter 10.2.")
public class RFKoza_GPI_10_2 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_10_2() {

    }

    public RFKoza_GPI_10_2(RFKoza_GPI_10_2 b) {

    }

    @Override
    public Object clone() {
        return new RFKoza_GPI_10_2(this);
    }

    /**
     * This method will return the y value for a given x vector
     *
     * @param x Input vector.
     * @return y the function result.
     */
    @Override
    public double evaluateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += 3.1416 * x[i] + 2.718 * Math.pow(x[i], 2);
        }
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Koza GP I 10.2";
    }

}
