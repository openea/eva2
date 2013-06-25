package eva2.optimization.problems;

import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operators.postprocess.SolutionHistogram;
import eva2.optimization.populations.Population;
import eva2.optimization.strategies.GradientDescentAlgorithm;
import java.io.Serializable;

/**
 * Generalized Rastrigin's function.
 * 
 */
public class F6Problem extends AbstractProblemDoubleOffset
implements InterfaceMultimodalProblem, InterfaceFirstOrderDerivableProblem, InterfaceLocalSearchable, Serializable, InterfaceInterestingHistogram {
    private double          m_A     = 10;
    private double          m_Omega = 2*Math.PI;
	private transient GradientDescentAlgorithm localSearchOptimizer=null;

    public F6Problem() {
        this.template = new ESIndividualDoubleData();
    }
    public F6Problem(F6Problem b) {
        super(b);
        this.m_A                = b.m_A;
        this.m_Omega            = b.m_Omega;
    }

    public F6Problem(int dim) {
		super(dim);
	}
    
    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    @Override
    public Object clone() {
        return (Object) new F6Problem(this);
    }

    /** Ths method allows you to evaluate a double[] to determine the fitness
     * @param x     The n-dimensional input vector
     * @return  The m-dimensional output vector.
     */
    @Override
    public double[] eval(double[] x) {
    	x = rotateMaybe(x);
        double[] result = new double[1];
        result[0]     = x.length * this.m_A + yOffset;
        for (int i = 0; i < x.length; i++) {
        	double xi = x[i]- xOffset;
            result[0]  += Math.pow(xi, 2) - this.m_A * Math.cos(this.m_Omega*xi);
        }
        return result;
    }
    
    @Override
	public double[] getFirstOrderGradients(double[] x) {
		x = rotateMaybe(x);
        double[] result = new double[x.length];        
        for (int j=0; j<x.length; j++) {
        	result[j]=0;
        	double xj = x[j]- xOffset;
        	result[j]  += 2*xj + this.m_Omega * this.m_A * Math.sin(this.m_Omega*xj);
        }
        return result;
	}
	
    /** This method returns a string describing the optimization problem.
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F6 Generalized Rastrigin's Function:\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension +"\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "F6-Problem";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Generalized Rastrigins's function.";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param a     The offset for the decision variables.
     */
    public void setA(double a) {
        this.m_A = a;
    }
    public double getA() {
        return this.m_A;
    }
    public String aTipText() {
        return "Choose a value for A.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param Omega     The offset for the objective value.
     */
    public void setOmega(double Omega) {
        this.m_Omega = Omega;
    }
    public double getOmega() {
        return this.m_Omega;
    }
    public String omegaTipText() {
        return "Choose Omega.";
    }
    
    @Override
    public void setDefaultAccuracy(double acc) {
    	super.SetDefaultAccuracy(acc);
    }

    @Override
	public SolutionHistogram getHistogram() {
		if (getProblemDimension() < 15) {
                return new SolutionHistogram(-0.5, 15.5, 16);
            }
		else if (getProblemDimension() < 25) {
                return new SolutionHistogram(-0.5, 39.5, 16);
            }
		else {
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
	    localSearchOptimizer.init();
    }

    @Override
    public double getLocalSearchStepFunctionCallEquivalent() {
    	double cost = 1;
    	if (this.localSearchOptimizer instanceof GradientDescentAlgorithm) {
    		cost = ((GradientDescentAlgorithm) localSearchOptimizer).getIterations();
    	}
    	return cost;
    }
}