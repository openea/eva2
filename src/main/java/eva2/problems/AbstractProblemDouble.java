package eva2.problems;

import Jama.Matrix;
import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.TopoPlot;
import eva2.optimization.enums.PostProcessMethod;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.constraint.AbstractConstraint;
import eva2.optimization.operator.constraint.GenericConstraint;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.operator.terminators.FitnessConvergenceTerminator;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.ChangeTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.DirectionTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.StagnationTypeEnum;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Pair;
import eva2.tools.ToolBox;
import eva2.tools.diagram.ColorBarCalculator;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Parameter;

/**
 * For a double valued problem, there are two main methods to implement:
 * {@link #getProblemDimension()} must return the problem dimension, while
 * {@link #evaluate(double[])} is to evaluate a single double vector into the result
 * fitness vector.
 * <p>
 * To define the problem range, you may use the default range parameter
 * resulting in a symmetric double range [-defaultRange,defaulRange] in all
 * dimensions. Or you may implement {@link #getRangeLowerBound(int)} and
 * {@link #getRangeUpperBound(int)} to define an arbitrary problem range. In
 * that case, the default range parameter is not used.
 * <p>
 * Anything you want to do before any optimization is started on the problem
 * should go into {@link #initializeProblem()}, but remember to call the super-method
 * in your implementation. The individual template will be initialized to an
 * ESIndividualDoubleData by then.
 * <p>
 * For the GUI, it is also convenient to implement the {@link eva2.util.annotation.Description}
 * annotation and {@link #getName()} method to provide some distinctive information for the
 * user.
 */
public abstract class AbstractProblemDouble extends AbstractOptimizationProblem implements InterfaceProblemDouble, Interface2DBorderProblem {
    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -3904130243174390134L;
    private double defaultRange = 10;
    private double noise = 0;
    private boolean doRotation = false; // should really be false by default
    private Matrix rotation;

    private AbstractConstraint[] constraintArray = new AbstractConstraint[]{new GenericConstraint()};
    private boolean withConstraints = false;
    private transient boolean isShowing = false;
    private double rotAngle = 22.5; // for default rotation along every axis
    public static String rawFitKey = "UnconstrainedFitnessValue";

    public AbstractProblemDouble() {
        initializeTemplate();
    }

    public AbstractProblemDouble(AbstractProblemDouble o) {
        cloneObjects(o);
    }

    protected void initializeTemplate() {
        if (template == null) {
            template = new ESIndividualDoubleData();
        }
        if (getProblemDimension() > 0) { // avoid evil case setting dim to 0
            // during object initialize
            ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(getProblemDimension());
            ((InterfaceDataTypeDouble) this.template).setDoubleRange(makeRange());
        }
    }

    public void hideHideable() {
        setWithConstraints(isWithConstraints());
    }

    protected void cloneObjects(AbstractProblemDouble o) {
        this.defaultRange = o.defaultRange;
        this.noise = o.noise;
        this.setDefaultAccuracy(o.getDefaultAccuracy());
        if (o.template != null) {
            this.template = (AbstractEAIndividual) o.template.clone();
        }
        if (o.constraintArray != null) {
            this.constraintArray = o.constraintArray.clone();
            for (int i = 0; i < constraintArray.length; i++) {
                constraintArray[i] = (AbstractConstraint) o.constraintArray[i]
                        .clone();
            }
        }
        this.withConstraints = o.withConstraints;
        this.doRotation = o.doRotation;
        this.rotation = (o.rotation == null) ? null : (Matrix) o.rotation
                .clone();
        this.rotAngle = o.rotAngle;
    }

    /**
     * Retrieve and copy the double solution representation from an individual.
     * This may also perform a coding adaption. The result is stored as
     * phenotype within the evaluate method.
     *
     * @param individual
     * @return the double solution representation
     */
    protected double[] getEvalArray(AbstractEAIndividual individual) {
        double[] x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        return x;
    }

    /**
     * When implementing a double problem, inheriting classes should not
     * override this method (or only extend it) and do the fitness calculations
     * in the method evaluate(double[]).
     *
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[] x;
        double[] fitness;

        x = getEvalArray(individual);
        ((InterfaceDataTypeDouble) individual).setDoublePhenotype(x);
        // evaluate the vector
        fitness = this.evaluate(x);
        // if indicated, add Gaussian noise
        if (noise != 0) {
            RNG.addNoise(fitness, noise);
        }
        // set the fitness
        setEvalFitness(individual, x, fitness);
        if (isWithConstraints()) {
            individual.putData(rawFitKey, individual.getFitness().clone());
            addConstraints(individual, x);
        }
    }

    protected double[] rotateMaybe(double[] x) {
        if (isDoRotation()) {
            if (rotation == null) {
                initializeProblem();
            }
            x = Mathematics.rotate(x, rotation);
        }
        return x;
    }

    protected double[] inverseRotateMaybe(double[] x) {
        if (isDoRotation()) {
            if (rotation == null) {
                initializeProblem();
            }
            x = Mathematics.rotate(x, rotation.inverse());
        }
        return x;
    }

    /**
     * Add all constraint violations to the individual. Expect that the fitness
     * has already been set.
     *
     * @param individual
     * @param indyPos    may contain the decoded individual position
     */
    protected void addConstraints(AbstractEAIndividual individual, double[] indyPos) {
        AbstractConstraint[] constraints = getConstraints();
        for (AbstractConstraint constraint : constraints) {
            constraint.addViolation(individual, indyPos);
        }
    }

    /**
     * Write a fitness value back to an individual. May be overridden to add
     * constraints.
     *
     * @param individual
     * @param x
     * @param fit
     */
    protected void setEvalFitness(AbstractEAIndividual individual, double[] x, double[] fit) {
        individual.setFitness(fit);
    }

    /**
     * Evaluate a double vector, representing the target function. If you
     * implement this, you should take care of the offsets and rotation, e.g. by
     * using x=rotateMaybe(x) before further evaluation.
     *
     * @param x the vector to evaluate
     * @return the target function value
     */
    @Override
    public abstract double[] evaluate(double[] x);

    @Override
    public void initializePopulation(Population population) {
        initializeTemplate();
        AbstractOptimizationProblem.defaultInitializePopulation(population, template, this);
    }

    /**
     * Create a new range array by using the getRangeLowerBound and
     * getRangeUpperBound methods.
     *
     * @return a range array
     */
    @Override
    public double[][] makeRange() {
        double[][] range = new double[this.getProblemDimension()][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = getRangeLowerBound(i);
            range[i][1] = getRangeUpperBound(i);
        }
        return range;
    }

    /**
     * Get the lower bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. Use setDefaultRange for symmetric
     * ranges.
     *
     * @param dim
     * @return the lower bound of the double range in the given dimension
     * @see #makeRange()
     * @see #getRangeUpperBound(int dim)
     */
    @Override
    public double getRangeLowerBound(int dim) {
        return -getDefaultRange();
    }

    /**
     * Get the upper bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. User setDefaultRange for
     * symmetric ranges.
     *
     * @param dim
     * @return the upper bound of the double range in the given dimension
     * @see #makeRange()
     * @see #getRangeLowerBound(int dim)
     */
    @Override
    public double getRangeUpperBound(int dim) {
        return getDefaultRange();
    }

    @Override
    public void initializeProblem() {
        initializeTemplate();
        if (isDoRotation()) {
            rotation = initializeDefaultRotationMatrix(rotAngle, getProblemDimension());
        } else {
            rotation = null;
        }
    }

    /**
     * Initialize rotation matrix which rotates around the given angle in every
     * axis.
     *
     * @param rotAngle
     * @param dim
     * @return
     */
    public static Matrix initializeDefaultRotationMatrix(double rotAngle, int dim) {
        return Mathematics.getRotationMatrix((rotAngle * Math.PI / 180.), dim).transpose();
    }

    /**
     * This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more
     * difficult.
     *
     * @param noise The sigma for a gaussian random number.
     */
    @Parameter(description = "Gaussian noise level on the fitness value.")
    public void setNoise(double noise) {
        if (noise < 0) {
            noise = 0;
        }
        this.noise = noise;
    }

    /**
     * Get the current noise level.
     *
     * @return the current noise level
     */
    public double getNoise() {
        return this.noise;
    }

    /**
     * This method allows you to choose the EA individual used by the problem.
     *
     * @param indy The EAIndividual type
     */
    @Parameter(name = "individual", description = "Base individual type defining the data representation and mutation/crossover operators")
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.template = (AbstractEAIndividual) indy;
    }

    /**
     * Get the EA individual template currently used by the problem.
     *
     * @return the EA individual template currently used
     */
    @Override
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble) this.template;
    }

    /**
     * A (symmetric) absolute range limit.
     *
     * @return value of the absolute range limit
     */
    public double getDefaultRange() {
        return defaultRange;
    }

    /**
     * Set a (symmetric) absolute range limit.
     *
     * @param defaultRange
     */
    public void setDefaultRange(double defaultRange) {
        this.defaultRange = defaultRange;
        initializeTemplate();
    }

    public String defaultRangeTipText() {
        return "Absolute limit for the symmetric range in any dimension";
    }

    @Parameter(name = "rotate", description = "If marked, the function is rotated by 22.5 degrees along every axis.")
    public void setDoRotation(boolean doRotation) {
        this.doRotation = doRotation;
        if (!doRotation) {
            rotation = null;
        }
    }

    public boolean isDoRotation() {
        return doRotation;
    }

    /**
     * *******************************************************************************************************************
     * These are for InterfaceParamControllable
     */
    public Object[] getParamControl() {
        if (isWithConstraints()) {
            return constraintArray;
            // return constraintList.getObjects();
        } else {
            return null;
        }
    }

    /**
     * *******************************************************************************************************************
     * These are for Interface2DBorderProblem
     */
    @Override
    public double[][] get2DBorder() {
        return makeRange();
    }

    @Override
    public double[] project2DPoint(double[] point) {
        return Mathematics.expandVector(point, getProblemDimension(), 0.);
    }

    @Override
    public double functionValue(double[] point) {
        double[] x = project2DPoint(point);
        double v = evaluate(x)[0];
        return v;
    }

    /**
     * Add a position as a known optimum to a list of optima. This method
     * evaluates the fitness and applies inverse rotation if necessary.
     *
     * @param optimas
     * @param prob
     * @param pos
     */
    public static void addUnrotatedOptimum(Population optimas,
                                           AbstractProblemDouble prob, double[] pos) {
        InterfaceDataTypeDouble tmpIndy;
        tmpIndy = (InterfaceDataTypeDouble) prob.getIndividualTemplate()
                .clone();
        tmpIndy.setDoubleGenotype(pos);
        if (prob.isDoRotation()) {
            pos = prob.inverseRotateMaybe(pos); // theres an inverse rotation
            // required
            tmpIndy.setDoubleGenotype(pos);
        }
        ((AbstractEAIndividual) tmpIndy).setFitness(prob.evaluate(pos));
        if (!Mathematics.isInRange(pos, prob.makeRange())) {
            System.err.println("Warning, add optimum which is out of range!");
        }
        optimas.add((AbstractEAIndividual) tmpIndy);
    }

    /**
     * Refine a potential solution using Nelder-Mead-Simplex.
     *
     * @param prob
     * @param pos
     * @return
     */
    public static double[] refineSolutionNMS(AbstractProblemDouble prob,
                                             double[] pos) {
        Population pop = new Population();
        InterfaceDataTypeDouble tmpIndy;
        tmpIndy = (InterfaceDataTypeDouble) prob.getIndividualTemplate()
                .clone();
        tmpIndy.setDoubleGenotype(pos);
        ((AbstractEAIndividual) tmpIndy).setFitness(prob.evaluate(pos));
        pop.add((AbstractEAIndividual) tmpIndy);
        FitnessConvergenceTerminator convTerm = new FitnessConvergenceTerminator(
                1e-25, 10, StagnationTypeEnum.generationBased,
                ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease);
        int calls = PostProcess.processSingleCandidatesNMCMA(
                PostProcessMethod.nelderMead, pop, convTerm, 0.001, prob);
        return ((InterfaceDataTypeDouble) pop.getBestEAIndividual())
                .getDoubleData();
    }

    /**
     * Refine a candidate solution vector regarding rotations. Saves the new
     * solution vector in pos and returns the number of dimensions that had to
     * be modified after rotation due to range restrictions.
     * <p>
     * The given position is expected to be unrotated! The returned solution is
     * unrotated as well.
     *
     * @param pos
     * @param prob
     * @return
     */
    public static int refineWithRotation(double[] pos,
                                         AbstractProblemDouble prob) {
        double[] res = prob.inverseRotateMaybe(pos);
        int modifiedInPrjct = Mathematics.projectToRange(res, prob.makeRange());
        res = AbstractProblemDouble.refineSolutionNMS(prob, res);
        res = prob.rotateMaybe(res);
        System.arraycopy(res, 0, pos, 0, res.length);
        return modifiedInPrjct;
    }

    /**
     * This method allows the GUI to read the name to the current object.
     *
     * @return the name of the object
     */
    @Override
    public String getName() {
        return "AbstractProblemDouble";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("A double valued problem: ");
        sb.append(this.getName());
        sb.append("\n");
        sb.append("Dimension   : ");
        sb.append(this.getProblemDimension());
        sb.append("\nNoise level : ");
        sb.append(this.noise);
        return sb.toString();
    }

    public AbstractConstraint[] getConstraints() {
        return constraintArray;
    }

    @Parameter(description = "Add constraints to the problem.")
    public void setConstraints(AbstractConstraint[] constrArray) {
        this.constraintArray = constrArray;
    }

    public boolean isWithConstraints() {
        return withConstraints;
    }

    @Parameter(description = "(De-)Activate constraints for the problem.")
    public void setWithConstraints(boolean withConstraints) {
        this.withConstraints = withConstraints;
        GenericObjectEditor.setShowProperty(this.getClass(), "constraints",
                withConstraints);
    }

    @Override
    public String[] getAdditionalDataHeader() {
        String[] superHeader = super.getAdditionalDataHeader();
        if (isWithConstraints()) {
            return ToolBox.appendArrays(superHeader, new String[]{"rawFit","numViol", "sumViol"});
        } else {
            return superHeader;
        }
    }

    @Override
    public String[] getAdditionalDataInfo() {
        String[] superInfo = super.getAdditionalDataInfo();
        if (isWithConstraints()) {
            return ToolBox.appendArrays(superInfo,new String[]{
                "Raw fitness (unpenalized) of the current best individual",
                "The number of constraints violated by the current best individual",
                "The sum of constraint violations of the current best individual"}
            );
        } else {
            return superInfo;
        }
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] superVal = super.getAdditionalDataValue(pop);
        if (isWithConstraints()) {
            AbstractEAIndividual indy = (AbstractEAIndividual) pop
                    .getBestIndividual();
            Pair<Integer, Double> violation = getConstraintViolation(indy);
            return ToolBox.appendArrays(superVal,
                    new Object[]{indy.getData(rawFitKey), violation.head(),
                            violation.tail()});
            // return superVal + " \t" +
            // BeanInspector.toString(indy.getData(rawFitKey)) + " \t" +
            // violation.head() + " \t" + violation.tail();
        } else {
            return superVal;
        }
    }

    protected Pair<Integer, Double> getConstraintViolation(
            AbstractEAIndividual indy) {
        double sum = 0;
        int numViol = 0;
        for (AbstractConstraint constr : constraintArray) {
            double v = constr.getViolation(getEvalArray(indy));
            if (v > 0) {
                numViol++;
            }
            sum += v;
        }
        return new Pair<>(numViol, sum);
    }

    public boolean isShowPlot() {
        return isShowing;
    }

    @Parameter(description = "Produce an exemplary 2D plot of the function (dimensional cut at x_i=0 for n>1).")
    public void setShowPlot(boolean showP) {
        if (!isShowing && showP) {
            TopoPlot plot = new TopoPlot(getName(), "x1", "x2");
            plot.setParams(100, 100, ColorBarCalculator.BLUE_TO_RED);
            this.initializeProblem();
            plot.setTopology(this, makeRange(), true);
            if (this instanceof InterfaceMultimodalProblemKnown && ((InterfaceMultimodalProblemKnown) this).fullListAvailable()) {
                plot.drawPopulation("Opt", ((InterfaceMultimodalProblemKnown) this).getRealOptima());
            }
        }
        isShowing = showP;
    }
}
