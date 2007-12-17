package javaeva.server.go.problems;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.ESIndividualDoubleData;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.populations.Population;
import javaeva.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:40:28
 * To change this template use File | Settings | File Templates.
 */
public class F8Problem extends F1Problem implements java.io.Serializable {

    private double      a = 20;
    private double      b = 0.2;
    private double      c = 2*Math.PI;

    public F8Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F8Problem(F8Problem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //F1Problem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_Noise            = b.m_Noise;
        this.m_XOffSet          = b.m_XOffSet;
        this.m_YOffSet          = b.m_YOffSet;
        this.m_UseTestConstraint = b.m_UseTestConstraint;        
    }

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F8Problem(this);
    }

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initPopulation(Population population) {
        AbstractEAIndividual tmpIndy;

        this.m_OverallBest = null;

        population.clear();

        ((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        double[][] range = new double[this.m_ProblemDimension][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = -32.768;
            range[i][1] = 32.768;
        }
        ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(range);

        for (int i = 0; i < population.getPopulationSize(); i++) {
            tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
            tmpIndy.init(this);
            population.add(tmpIndy);
        }
        // population init must be last
        // it set's fitcalls and generation to zero
        population.init();
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[]        result = new double[1];
        double          sum1 = 0, sum2 = 0, exp1, exp2;

        for (int i = 0; i < x.length; i++) {
        	sum1 += (x[i])*(x[i]);
        	sum2 += Math.cos(c * (x[i]));
        }
        exp1    = -b*Math.sqrt(sum1/(double)this.m_ProblemDimension);
        exp2    = sum2/(double)this.m_ProblemDimension;
        result[0] = a +  Math.E  - a * Math.exp(exp1)- Math.exp(exp2);

        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F8 Ackley's function.\n";
        result += "This problem is multimodal.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.m_ProblemDimension +"\n";
        result += "Noise level : " + this.m_Noise + "\n";
        result += "Solution representation:\n";
        //result += this.m_Template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "F8-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Ackley's function.";
    }
}