package eva2.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.GradientDescentAlgorithm;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Generalized Rastrigin's function.
 */
@Description("Generalized Rastrigin's function.")
public class F6Problem extends AbstractProblemDoubleOffset
        implements InterfaceMultimodalProblem, InterfaceFirstOrderDerivableProblem, InterfaceLocalSearchable, Serializable, InterfaceInterestingHistogram {
    private double a = 10;
    private double omega = 2 * Math.PI;
    private transient GradientDescentAlgorithm localSearchOptimizer = null;

    public F6Problem() {
        this.template = new ESIndividualDoubleData();
    }

    public F6Problem(F6Problem b) {
        super(b);
        this.a = b.a;
        this.omega = b.omega;
    }

    public F6Problem(int dim) {
        super(dim);
    }

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F6Problem(this);
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
        result[0] = x.length * this.a + yOffset;
        for (int i = 0; i < x.length; i++) {
            double xi = x[i] - xOffset;
            result[0] += Math.pow(xi, 2) - this.a * Math.cos(this.omega * xi);
        }
        return result;
    }

    @Override
    public double[] getFirstOrderGradients(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[x.length];
        for (int j = 0; j < x.length; j++) {
            result[j] = 0;
            double xj = x[j] - xOffset;
            result[j] += 2 * xj + this.omega * this.a * Math.sin(this.omega * xj);
        }
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F6 Generalized Rastrigin's Function:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Generalized Rastrigin";
    }

    /**
     * This method allows you to set/get an offset for decision variables.
     *
     * @param a The offset for the decision variables.
     */
    public void setA(double a) {
        this.a = a;
    }

    public double getA() {
        return this.a;
    }

    public String aTipText() {
        return "Choose a value for A.";
    }

    /**
     * This method allows you to set/get the offset for the
     * objective value.
     *
     * @param Omega The offset for the objective value.
     */
    public void setOmega(double Omega) {
        this.omega = Omega;
    }

    public double getOmega() {
        return this.omega;
    }

    public String omegaTipText() {
        return "Choose Omega.";
    }

    @Override
    public void setDefaultAccuracy(double acc) {
        super.setDefaultAccuracy(acc);
    }

    @Override
    public SolutionHistogram getHistogram() {
        if (getProblemDimension() < 15) {
            return new SolutionHistogram(-0.5, 15.5, 16);
        } else if (getProblemDimension() < 25) {
            return new SolutionHistogram(-0.5, 39.5, 16);
        } else {
            return new SolutionHistogram(0, 80, 16);
        }
    }

    @Override
    public void doLocalSearch(Population pop) {
        if (localSearchOptimizer == null) {
            initLS();
        }
        localSearchOptimizer.setPopulation(pop);
        localSearchOptimizer.optimize();
    }

    private void initLS() {
        localSearchOptimizer = new GradientDescentAlgorithm();
        localSearchOptimizer.setProblem(this);
        localSearchOptimizer.initialize();
    }

    @Override
    public double getLocalSearchStepFunctionCallEquivalent() {
        double cost = 1;
        if (this.localSearchOptimizer instanceof GradientDescentAlgorithm) {
            cost = localSearchOptimizer.getIterations();
        }
        return cost;
    }
}