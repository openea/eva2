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
 * Time: 19:28:33
 * To change this template use File | Settings | File Templates.
 */
public class F4Problem extends F1Problem implements java.io.Serializable {

    public F4Problem() {
        this.m_Template         = new ESIndividualDoubleData();
    }
    public F4Problem(F4Problem b) {
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
            range[i][0] = -1.28;
            range[i][1] = 1.28;
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

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone() {
        return (Object) new F4Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double[] result = new double[1];
        result[0]     = 0;
        for (int i = 0; i < x.length-1; i++) {
            result[0]  += (i+1)*Math.pow(x[i], 4);
        }
        return result;
    }

    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F4 Quadratic Function with noise:\n";
        result += "This problem is noisey.\n";
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
        return "F4 Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Quadratic Function with noise.";
    }
}