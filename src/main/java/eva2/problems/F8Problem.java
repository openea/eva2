package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;

import java.util.Arrays;

/**
 * Ackley's function.
 */
@Description("Ackley's function.")
public class F8Problem extends AbstractProblemDoubleOffset
        implements InterfaceInterestingHistogram, InterfaceMultimodalProblem, //InterfaceFirstOrderDerivableProblem,
        InterfaceMultimodalProblemKnown, java.io.Serializable {

    transient protected Population listOfOptima = null;
    private static transient boolean stateInitializingOptima = false;
    private double a = 20;
    private double b = 0.2;
    private double c = 2 * Math.PI;
    final static double f8Range = 32.768;

    public F8Problem() {
        setDefaultRange(f8Range);
    }

    public F8Problem(F8Problem b) {
        super(b);
        this.a = b.a;
        this.b = b.b;
        this.c = b.c;
    }

    public F8Problem(int dim) {
        super(dim);
        setDefaultRange(f8Range);
    }

//    make this a multimodal problem known and add the best optima as in the niching ES papers!

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new F8Problem(this);
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
        double sum1 = 0, sum2 = 0, exp1, exp2;

        for (int i = 0; i < x.length; i++) {
            double xi = x[i] - xOffset;
            sum1 += (xi) * (xi);
            sum2 += Math.cos(c * (xi));
        }
        exp1 = -b * Math.sqrt(sum1 / (double) this.problemDimension);
        exp2 = sum2 / (double) this.problemDimension;
        result[0] = yOffset + a + Math.E - a * Math.exp(exp1) - Math.exp(exp2);

        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F8 Ackley's function.\n";
        result += "This problem is multimodal.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }


    @Override
    public void initializeProblem() {
        super.initializeProblem();
        initListOfOptima();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Ackley";
    }

    @Override
    public SolutionHistogram getHistogram() {
        if (getProblemDimension() < 15) {
            return new SolutionHistogram(-0.1, 7.9, 16);
        } else if (getProblemDimension() < 25) {
            return new SolutionHistogram(-0.5, 15.5, 16);
        } else {
            return new SolutionHistogram(0, 16, 16);
        }
    }

    @Override
    public boolean fullListAvailable() {
        return true;
    }

    @Override
    public double getMaximumPeakRatio(Population pop) {
        return AbstractMultiModalProblemKnown.getMaximumPeakRatioMinimization(listOfOptima, pop, getDefaultAccuracy(), 0, 5);
    }

    @Override
    public int getNumberOfFoundOptima(Population pop) {
        return AbstractMultiModalProblemKnown.getNoFoundOptimaOf(this, pop);
    }

    @Override
    public Population getRealOptima() {
        return listOfOptima;
    }

    @Override
    public String[] getAdditionalDataHeader() {
        String[] superHd = super.getAdditionalDataHeader();
        return ToolBox.appendArrays(new String[]{"numOptimaFound", "maxPeakRatio"}, superHd);
    }

    @Override
    public final String[] getAdditionalDataInfo() {
        String[] superHd = super.getAdditionalDataInfo();
        return ToolBox.appendArrays(new String[]{
                "The number of optima found",
                "Ratio of maximum peaks"
        }, superHd);
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] myRet = new Object[2];
        myRet[0] = this.getNumberOfFoundOptima((Population) pop);
        myRet[1] = this.getMaximumPeakRatio((Population) pop);
        return ToolBox.appendArrays(myRet, super.getAdditionalDataValue(pop));
    }

    /**
     * We identify a set of 2*n+1 optima, which are the global and the first level
     * of local optima.
     * Interestingly, the local optima in the first sphere (close to (1,0,0,0,...) vanish starting
     * with n=18 (checked with Matlab). The next sphere contains O(n^2) optima (I think 3*n*(n-1)).
     * This is unfortunately not mentioned in the papers by Shir
     * and Bäck who still seemed to be able to find 2*n+1...
     */
    @Override
    public void initListOfOptima() {
        if (listOfOptimaNeedsUpdate()) {
            stateInitializingOptima = true;
            listOfOptima = new Population();
            // ingeniously avoid recursive calls during refinement!
            double[] pos = new double[getProblemDimension()];
            Arrays.fill(pos, getXOffset());
            addOptimum(pos); // the global optimum
            double refinedX = 0;
            if (getProblemDimension() < 18) {
                for (int i = 0; i < getProblemDimension(); i++) {
                    // TODO what about dimensions higher than 18???
                    for (int k = -1; k <= 1; k += 2) {
                        Arrays.fill(pos, getXOffset());
                        if (refinedX == 0) { // we dont know the exact x-offset for the optima, so refine once
                            pos[i] = k + getXOffset();
                            double[] dir = pos.clone();
                            dir[i] -= 0.05;
                            pos = inverseRotateMaybe(pos);
//						ab dim 18/20 oder so finde ich plötzlich keine optima bei x[i]<1 mehr???
                            dir = inverseRotateMaybe(dir);
                            pos = refineSolution(this, pos, dir, 0.0005, 1e-20, 0);
                            if (EuclideanMetric.euclideanDistance(pos, listOfOptima.getEAIndividual(0).getDoublePosition()) < 0.5) {
                                System.err.println("Warning, possibly converged to a wrong optimum in F8Problem.initListOfOptima!");
                            }
                            pos = rotateMaybe(pos);
                            refinedX = Math.abs(pos[i] - getXOffset()); // store the refined position which is equal in any direction and dimension
                        } else {
                            pos[i] = (k * refinedX) + getXOffset();
                        }
                        addOptimum(pos);
                    }
                }
            }
            stateInitializingOptima = false;
        }
    }

    private double[] refineSolution(AbstractProblemDouble prob, double[] pos, double[] vect, double initStep, double thresh, int fitCrit) {
        // a line search along a vector
        double[] tmpP = pos.clone();
        double[] normedVect = Mathematics.normVect(vect);
        double dx = initStep;
        double tmpFit, oldFit = prob.evaluate(pos)[fitCrit];

        int dir = 1;
        while (dx > thresh) {
            // add a step to tmpP
            Mathematics.svvAddScaled(dx * dir, normedVect, pos, tmpP);
            // evaluate tmpP
            tmpFit = prob.evaluate(tmpP)[fitCrit];
            if (tmpFit < oldFit) {
                // if tmpP is better than pos continue at new pos
                double[] tmp = pos;
                pos = tmpP;
                tmpP = tmp;
                oldFit = tmpFit;
            } else {
                // otherwise invert direction, reduce step, continue
                dx *= 0.73;
                dir *= -1;
            }
        }
        return pos;
    }

    private boolean listOfOptimaNeedsUpdate() {
        if (stateInitializingOptima) {
            return false;
        } // avoid recursive call during refining with GDA
        if (listOfOptima == null || (listOfOptima.size() != (1 + 2 * getProblemDimension()))) {
            return true;
        } else { // the number of optima is corret - now check different offset or rotation by comparing one fitness value
            AbstractEAIndividual indy = listOfOptima.getEAIndividual(1);
            double[] curFit = evaluate(indy.getDoublePosition());
            return Math.abs(Mathematics.dist(curFit, indy.getFitness(), 2)) > 1e-10;
        }
    }

    private void addOptimum(double[] pos) {
        AbstractProblemDouble.addUnrotatedOptimum(listOfOptima, this, pos);
    }
}