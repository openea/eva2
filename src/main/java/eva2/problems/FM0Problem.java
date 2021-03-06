package eva2.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * ToDo: Document
 */
@Description("M0(x) = sin(2*x - 0.5*PI) + 1 + 2*cos(y) + 0.5*x is to be maximized, two optima.")
public class FM0Problem extends AbstractMultiModalProblemKnown implements InterfaceOptimizationProblem, Interface2DBorderProblem, InterfaceMultimodalProblemKnown, Serializable {

    public FM0Problem() {
        this.problemDimension = 2;
        this.template = new ESIndividualDoubleData();
    }

    @Override
    public double getRangeUpperBound(int dim) {
        if (dim == 0) {
            return 2.0;
        } else {
            return 2.8;
        }
    }

    @Override
    public double getRangeLowerBound(int dim) {
        return -1 * getRangeUpperBound(dim);
    }

    public FM0Problem(FM0Problem b) {
        cloneObjects(b);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new FM0Problem(this);
    }

    /**
     * This method returns the unnormalized function value for an maximization problem
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] evalUnnormalized(double[] x) {
        double[] result = new double[1];
        result[0] = Math.sin(2 * x[0] - 0.5 * Math.PI) + 1 + 2 * Math.cos(x[1]) + 0.5 * x[0];
        return result;
    }

    /**
     * This method will prepare the problem to return a list of all optima
     * if possible and to return quality measures like NumberOfOptimaFound and
     * the MaximumPeakRatio. This method should be called by the user.
     */
    @Override
    public void initListOfOptima() {
        //this.add2DOptimum((Math.PI - (Math.PI - Math.acos(-1/4.0)) + Math.PI/2.0)/2.0, 0);
        //this.add2DOptimum((-Math.PI - (Math.PI - Math.acos(-1/4.0)) + Math.PI/2.0)/2.0, 0);

        // These are Matlab fminserach results with tol = 0.00000000001
        this.add2DOptimum(1.69713645852390, -0.00000000896995);
        this.add2DOptimum(-1.44445618316078, 0.00000000700284);
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "M0 Problem";
    }

}
