package eva2.problems.regression;

import eva2.util.annotation.Description;

/**
 */
@Description("This target function f(x)=x^4+x^3+x^2+x as given in Koza GP I chapter 7.3.")
public class RFKoza_GPI_7_3 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_7_3() {

    }

    public RFKoza_GPI_7_3(RFKoza_GPI_7_3 b) {

    }

    @Override
    public Object clone() {
        return new RFKoza_GPI_7_3(this);
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
            result += Math.pow(x[i], 4) + Math.pow(x[i], 3) + Math.pow(x[i], 2) + x[i];
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
        return "Koza GP I 7.3";
    }

}
