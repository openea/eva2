package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.selection.replacement.ReplacementCrowding;
import eva2.optimization.operator.selection.replacement.ReplacementNondominatedSortingDistanceCrowding;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.problems.AbstractOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.util.Vector;

/**
 * Differential evolution implementing DE1 and DE2 following the paper of Storm
 * and Price and the Trigonometric DE published recently. Please note that DE
 * will only work on real-valued genotypes and will ignore all mutation and
 * crossover operators selected. Added aging mechanism to provide for
 * dynamically changing problems. If an individual reaches the age limit, it is
 * doomed and replaced by the next challenge vector, even if its worse.
 */
@Description(value = "Differential Evolution using a steady-state population scheme.")
public class DifferentialEvolution extends AbstractOptimizer implements java.io.Serializable {
    protected transient Population children = null;

    private eva2.optimization.enums.DEType DEType;

    private double differentialWeight = 0.8;

    private double crossoverRate = 0.6;

    private double lambda = 0.6;

    private double mt = 0.05;
    private int maximumAge = -1;
    private boolean reEvaluate = false;
    // to log the parents of a newly created indy.
    public boolean doLogParents = false; // deactivate for better performance
    private transient Vector<AbstractEAIndividual> parents = null;
    private boolean randomizeFKLambda = false;
    private boolean generational = true;
    private String identifier = "";
    private boolean forceRange = true;
    private boolean cyclePop = false; // if true, individuals are used as parents in a cyclic sequence - otherwise randomly 
    private boolean compareToParent = true;  // if true, the challenge indy is compared to its parent, otherwise to a random individual

    /**
     * A constructor.
     */
    public DifferentialEvolution() {
        // sets DE2 as default
        DEType = eva2.optimization.enums.DEType.RandToBest;
    }

    public DifferentialEvolution(int popSize, eva2.optimization.enums.DEType type, double f, double cr, double lambda, double mt) {
        population = new Population(popSize);
        DEType = type;
        differentialWeight = f;
        crossoverRate = cr;
        this.lambda = lambda;
        this.mt = mt;
    }

    /**
     * The copy constructor.
     *
     * @param a
     */
    public DifferentialEvolution(DifferentialEvolution a) {
        this.DEType = a.DEType;
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (AbstractOptimizationProblem) a.optimizationProblem.clone();
        this.identifier = a.identifier;
        this.differentialWeight = a.differentialWeight;
        this.crossoverRate = a.crossoverRate;
        this.lambda = a.lambda;
        this.mt = a.mt;

        this.maximumAge = a.maximumAge;
        this.randomizeFKLambda = a.randomizeFKLambda;
        this.forceRange = a.forceRange;
        this.cyclePop = a.cyclePop;
        this.compareToParent = a.compareToParent;
    }

    @Override
    public Object clone() {
        return new DifferentialEvolution(this);
    }

    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.evaluatePopulation(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    public void hideHideable() {
        setDEType(getDEType());
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.initialize();
            this.evaluatePopulation(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
    }

    /**
     * This method returns a difference vector between two random individuals
     * from the population. This method should make sure that delta is not zero.
     *
     * @param pop The population to choose from
     * @return The delta vector
     */
    private double[] fetchDeltaRandom(Population pop) {
        double[] x1, x2;
        double[] result;
        boolean isEmpty;
        int iterations = 0;

        AbstractEAIndividual x1Indy = getRandomIndy(pop);
        x1 = getGenotype(x1Indy);

        if (parents != null) {
            parents.add(x1Indy);
        }

        result = new double[x1.length];
        isEmpty = true;
        AbstractEAIndividual x2Indy = null;
        while (isEmpty && (iterations < pop.size())) {
            x2Indy = getRandomIndy(pop);
            x2 = getGenotype(x2Indy);

            for (int i = 0; i < x1.length; i++) {
                result[i] = x1[i] - x2[i];
                isEmpty = (isEmpty && (result[i] == 0));
            }
            iterations++;
        }
        if (!isEmpty && (parents != null)) {
            parents.add(x2Indy);
        }

        while (isEmpty) {
            // for n (popSize) iterations there were only zero vectors found
            // so now the hard way: construct a random vector
            for (int i = 0; i < x1.length; i++) {
                if (RNG.flipCoin(1 / (double) x1.length)) {
                    result[i] = 0.01 * RNG.gaussianDouble(0.1);
                } else {
                    result[i] = 0;
                }
                isEmpty = (isEmpty && (result[i] == 0));
            }
            // single parent! don't add another one
        }

        return result;
    }

    /**
     * This method returns a difference vector between two random individuals
     * from the population. This method should make sure that delta is not zero.
     *
     * @param pop The population to choose from
     * @return The delta vector
     */
    private double[] fetchDeltaCurrentRandom(Population pop, InterfaceDataTypeDouble indy) {
        double[] x1, x2;
        double[] result;
        boolean isEmpty;
        int iterations = 0;


        x1 = indy.getDoubleData();


        if (parents != null) {
            parents.add((AbstractEAIndividual) indy);
        }

        result = new double[x1.length];
        isEmpty = true;
        AbstractEAIndividual x2Indy = null;
        while (isEmpty && (iterations < pop.size())) {
            x2Indy = getRandomIndy(pop);
            x2 = getGenotype(x2Indy);

            for (int i = 0; i < x1.length; i++) {
                result[i] = x1[i] - x2[i];
                isEmpty = (isEmpty && (result[i] == 0));
            }
            iterations++;
        }
        if (!isEmpty && (parents != null)) {
            parents.add(x2Indy);
        }

        while (isEmpty) {
            // for n (popSize) iterations there were only zero vectors found
            // so now the hard way: construct a random vector
            for (int i = 0; i < x1.length; i++) {
                if (RNG.flipCoin(1 / (double) x1.length)) {
                    result[i] = 0.01 * RNG.gaussianDouble(0.1);
                } else {
                    result[i] = 0;
                }
                isEmpty = (isEmpty && (result[i] == 0));
            }
            // single parent! don't add another one
        }

        return result;
    }

    /**
     * This method will return the delta vector to the best individual
     *
     * @param pop  The population to choose the best from
     * @param indy The current individual
     * @return the delta vector
     */
    private double[] fetchDeltaBest(Population pop, InterfaceDataTypeDouble indy) {
        double[] x1, result;
        AbstractEAIndividual xbIndy;

        x1 = indy.getDoubleData();
        result = new double[x1.length];
        if (optimizationProblem instanceof AbstractMultiObjectiveOptimizationProblem) {
            // implements MODE for the multi-objective case: a dominating individual is selected for difference building
            Population domSet = pop.getDominatingSet((AbstractEAIndividual) indy);
            if (domSet.size() > 0) {
                xbIndy = getRandomIndy(domSet);
            } else {
                return result; // just return a zero vector. this will happen automatically if domSet contains only the individual itself
            }
        } else {
            xbIndy = getBestIndy(pop);
        }

        double[] xb = getGenotype(xbIndy);
        if (parents != null) {
            parents.add(xbIndy);
        } // given indy argument is already listed

        for (int i = 0; i < x1.length; i++) {
            result[i] = xb[i] - x1[i];
        }

        return result;
    }

    /**
     * This method will generate one new individual from the given population
     *
     * @param population    The current population
     * @param parentIndex
     * @return AbstractEAIndividual
     */
    public AbstractEAIndividual generateNewIndividual(Population population, int parentIndex) {
        AbstractEAIndividual indy;
        InterfaceDataTypeDouble esIndy;

        if (doLogParents) {
            parents = new Vector<>();
        } else {
            parents = null;
        }
        try {
            // select one random indy as starting individual. It's a parent in any case.
            if (parentIndex < 0) {
                parentIndex = RNG.randomInt(0, population.size() - 1);
            }
            indy = (AbstractEAIndividual) (population.getEAIndividual(parentIndex)).getClone();
            esIndy = (InterfaceDataTypeDouble) indy;
        } catch (java.lang.ClassCastException e) {
            throw new RuntimeException("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
        }
        double[] nX, vX, oX;
        oX = esIndy.getDoubleData();
        vX = oX.clone();
        nX = new double[oX.length];
        switch (this.DEType) {
            case RandOne: {
                // this is DE1 or DE/rand/1
                double[] delta = this.fetchDeltaRandom(population);
                if (parents != null) {
                    parents.add(population.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * delta[i];
                }
                break;
            }
            case CurrentToRand: {
                // this is DE/current-to-rand/1
                double[] rndDelta = this.fetchDeltaRandom(population);
                double[] bestDelta = this.fetchDeltaCurrentRandom(population, esIndy);
                if (parents != null) {
                    parents.add(population.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentLambda() * bestDelta[i] + this.getCurrentF() * rndDelta[i];
                }
                break;
            }
            case RandTwo: {
                // this is DE/current-to-rand/1
                double[] rndDelta = this.fetchDeltaRandom(population);
                double[] rndDelta2 = this.fetchDeltaRandom(population);
                if (parents != null) {
                    parents.add(population.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * rndDelta[i] + this.getCurrentF() * rndDelta2[i];
                }
                break;
            }
            case RandToBest: {
                // this is DE2 or DE/rand-to-best/1
                double[] rndDelta = this.fetchDeltaRandom(population);
                double[] bestDelta = this.fetchDeltaBest(population, esIndy);
                if (parents != null) {
                    parents.add(population.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentLambda() * bestDelta[i] + this.getCurrentF() * rndDelta[i];
                }
                break;
            }
            case BestOne: {
                // DE/best/1
                AbstractEAIndividual bestIndy = getBestIndy(population);
                oX = getGenotype(bestIndy);
                if (parents != null) {
                    parents.add(bestIndy);
                }  // Add best instead of preselected
                double[] delta1 = this.fetchDeltaRandom(population);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * delta1[i];
                }
                break;
            }
            case BestTwo: {
                // DE/best/2
                AbstractEAIndividual bestIndy = getBestIndy(population);
                oX = getGenotype(bestIndy);
                if (parents != null) {
                    parents.add(bestIndy);
                }  // Add best instead of preselected
                double[] delta1 = this.fetchDeltaRandom(population);
                double[] delta2 = this.fetchDeltaRandom(population);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * delta1[i] + this.getCurrentF() * delta2[i];
                }
                break;
            }
            case Trigonometric: {
                // this is trigonometric mutation
                if (parents != null) {
                    parents.add(population.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                if (RNG.flipCoin(this.mt)) {
                    double[] xk, xl;
                    double p, pj, pk, pl;
                    InterfaceDataTypeDouble indy1 = null, indy2 = null;
                    try {
                        // and i got indy!
                        indy1 = (InterfaceDataTypeDouble) population.get(RNG.randomInt(0, population.size() - 1));
                        indy2 = (InterfaceDataTypeDouble) population.get(RNG.randomInt(0, population.size() - 1));
                        if (parents != null) {
                            parents.add((AbstractEAIndividual) indy1);
                            parents.add((AbstractEAIndividual) indy2);
                        }
                    } catch (java.lang.ClassCastException e) {
                        EVAERROR.errorMsgOnce("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
                    }
                    xk = indy1.getDoubleData();
                    xl = indy2.getDoubleData();
                    p = Math.abs(((AbstractEAIndividual) esIndy).getFitness(0)) + Math.abs(((AbstractEAIndividual) indy1).getFitness(0)) + Math.abs(((AbstractEAIndividual) indy2).getFitness(0));
                    pj = Math.abs(((AbstractEAIndividual) esIndy).getFitness(0)) / p;
                    pk = Math.abs(((AbstractEAIndividual) indy1).getFitness(0)) / p;
                    pl = Math.abs(((AbstractEAIndividual) indy2).getFitness(0)) / p;
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = (oX[i] + xk[i] + xl[i]) / 3.0 + ((pk - pj) * (oX[i] - xk[i])) + ((pl - pk) * (xk[i] - xl[i])) + ((pj - pl) * (xl[i] - oX[i]));
                    }
                } else {
                    // this is DE1
                    double[] delta = this.fetchDeltaRandom(population);
                    if (parents != null) {
                        parents.add(population.getEAIndividual(parentIndex));
                    }  // Add wherever oX is used directly
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = oX[i] + this.getCurrentF() * delta[i];
                    }
                }
                break;
            }
        }
        int k = RNG.randomInt(oX.length); // at least one position is changed
        for (int i = 0; i < oX.length; i++) {
            if ((i == k) || RNG.flipCoin(this.getCurrentK())) {
                // it is altered
                nX[i] = vX[i];
            } else {
                // it remains the same
                nX[i] = oX[i];
            }
        }
        // setting the new genotype and fitness
        if (forceRange) {
            Mathematics.projectToRange(nX, esIndy.getDoubleRange());
        } // why did this never happen before?
        esIndy.setDoubleGenotype(nX);
        indy.setAge(0);
        indy.resetConstraintViolation();
        double[] fit = new double[1];
        fit[0] = 0;
        indy.setFitness(fit);
        if (parents != null) {
            indy.setParents(parents);
        }
        return indy;
    }

    private double getCurrentK() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(crossoverRate * 0.8, crossoverRate * 1.2);
        } else {
            return crossoverRate;
        }
    }

    private double getCurrentLambda() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(lambda * 0.8, lambda * 1.2);
        } else {
            return lambda;
        }
    }

    private double getCurrentF() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(differentialWeight * 0.8, differentialWeight * 1.2);
        } else {
            return differentialWeight;
        }
    }

    private AbstractEAIndividual getBestIndy(Population pop) {
        return (AbstractEAIndividual) pop.getBestIndividual();
    }

    private AbstractEAIndividual getRandomIndy(Population pop) {
        if (pop.size() < 1) {
            System.err.println("Error: invalid pop size in DE!");
            System.err.println("DE: \n" + BeanInspector.toString(this) + "\nPop: \n" + BeanInspector.toString(pop));
        }

        int randIndex = RNG.randomInt(0, pop.size() - 1);
        return pop.getEAIndividual(randIndex);
    }

    private double[] getGenotype(AbstractEAIndividual indy) {
        try {
            return ((InterfaceDataTypeDouble) indy).getDoubleData();
        } catch (java.lang.ClassCastException e) {
            EVAERROR.errorMsgOnce("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
            return null;
        }
    }

    @Override
    public void optimize() {
        if (generational) {
            optimizeGenerational();
        } else {
            optimizeSteadyState();
        }
    }

    /**
     * This generational DE variant calls the method
     * AbstractOptimizationProblem.evaluate(Population). Its performance may be
     * slightly worse for schemes that rely on current best individuals, because
     * improvements are not immediately incorporated as in the steady state DE.
     * However it may be easier to parallelize.
     */
    public void optimizeGenerational() {
        int parentIndex;

        // Initialize or clear child population
        if (children == null) {
            children = new Population(population.size());
        } else {
            children.clear();
        }

        // Create a new population based on the old one
        for (int i = 0; i < this.population.size(); i++) {
            if (cyclePop) {
                parentIndex = i;
            } else {
                parentIndex = RNG.randomInt(0, this.population.size() - 1);
            }
            AbstractEAIndividual indy = generateNewIndividual(population, parentIndex);
            children.add(indy);
        }

        children.setGeneration(population.getGeneration());
        optimizationProblem.evaluate(children);

        /**
         * Re-evalutation mechanism for dynamically changing problems
         */
        if (isReEvaluate()) {
            for (int i = 0; i < this.population.size(); i++) {

                if (population.get(i).getAge() >= maximumAge) {
                    this.optimizationProblem.evaluate(population.get(i));
                    population.get(i).setAge(0);
                    population.incrFunctionCalls();
                }
            }
        }

        int nextDoomed = getNextDoomed(population, 0);
        for (int i = 0; i < this.population.size(); i++) {
            AbstractEAIndividual indy = children.getEAIndividual(i);
            if (cyclePop) {
                parentIndex = i;
            } else {
                parentIndex = RNG.randomInt(0, this.population.size() - 1);
            }
            if (nextDoomed >= 0) {    // this one is lucky, may replace an 'old' one
                population.replaceIndividualAt(nextDoomed, indy);
                nextDoomed = getNextDoomed(population, nextDoomed + 1);
            } else {
                if (optimizationProblem instanceof AbstractMultiObjectiveOptimizationProblem & indy.getFitness().length > 1) {
                    ReplacementCrowding repl = new ReplacementCrowding();
                    repl.insertIndividual(indy, population, null);
                } else {
                    if (!compareToParent) {
                        parentIndex = RNG.randomInt(0, this.population.size() - 1);
                    }
                    AbstractEAIndividual orig = this.population.get(parentIndex);
                    if (indy.isDominatingDebConstraints(orig)) {
                        this.population.replaceIndividualAt(parentIndex, indy);
                    }
                }
            }
        }
        this.population.incrFunctionCallsBy(children.size());
        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    public void optimizeSteadyState() {
        AbstractEAIndividual indy, orig;
        int index;

        int nextDoomed = getNextDoomed(population, 0);

        // required for dynamic problems especially
        ((AbstractOptimizationProblem) optimizationProblem).evaluatePopulationStart(population);

        /**
         * Reevalutation mechanism for dynamically changing problems
         */
        if (isReEvaluate()) {
            nextDoomed = -1;
            for (int i = 0; i < this.population.size(); i++) {

                if (population.get(i).getAge() >= maximumAge) {
                    this.optimizationProblem.evaluate(population.get(i));
                    population.get(i).setAge(0);
                    population.incrFunctionCalls();
                }
            }
        }


        for (int i = 0; i < this.population.size(); i++) {
            if (cyclePop) {
                index = i;
            } else {
                index = RNG.randomInt(0, this.population.size() - 1);
            }
            indy = generateNewIndividual(population, index);
            this.optimizationProblem.evaluate(indy);
            this.population.incrFunctionCalls();
            if (nextDoomed >= 0) {    // this one is lucky, may replace an 'old' one
                population.replaceIndividualAt(nextDoomed, indy);
                nextDoomed = getNextDoomed(population, nextDoomed + 1);
            } else {
                if (optimizationProblem instanceof AbstractMultiObjectiveOptimizationProblem) {

                    if (indy.isDominatingDebConstraints(population.getEAIndividual(index))) { //child dominates the parent replace the parent
                        population.replaceIndividualAt(index, indy);
                    } else if (!(population.getEAIndividual(index).isDominatingDebConstraints(indy))) { //do nothing if parent dominates the child use crowding if neither one dominates the other one
                        ReplacementNondominatedSortingDistanceCrowding repl = new ReplacementNondominatedSortingDistanceCrowding();
                        repl.insertIndividual(indy, population, null);
                    }
                } else {
                    if (!compareToParent) {
                        index = RNG.randomInt(0, this.population.size() - 1);
                    }
                    orig = this.population.get(index);
                    if (indy.isDominatingDebConstraints(orig)) {
                        this.population.replaceIndividualAt(index, indy);
                    }
                }
            }
        }

        ((AbstractOptimizationProblem) optimizationProblem).evaluatePopulationEnd(population);
        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * Search for the first individual which is older than the age limit and
     * return its index. If there is no age limit or all individuals are
     * younger, -1 is returned. The start index of the search may be provided to
     * make iterative search efficient.
     *
     * @param pop        Population to search
     * @param startIndex index to start the search from
     * @return index of an over aged individual or -1
     */
    protected int getNextDoomed(Population pop, int startIndex) {
        if (maximumAge > 0) {
            for (int i = startIndex; i < pop.size(); i++) {
                if (pop.get(i).getAge() >= maximumAge) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "Differential Evolution:\n";
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "Differential Evolution";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(this.population);
    }

    /**
     * F is a real and constant factor which controls the amplification of the
     * differential variation
     *
     * @param f
     */
    @Parameter(name = "F", description = "F is a real and constant factor which controls the amplification of the differential variation.")
    public void setDifferentialWeight(double f) {
        this.differentialWeight = f;
    }

    public double getDifferentialWeight() {
        return this.differentialWeight;
    }

    /**
     * Probability of alteration through DE (something like a discrete uniform
     * crossover is performed here)
     *
     * @param k
     */
    @Parameter(name = "CR", description = "Probability of alteration through DE (a.k.a. CR, similar to discrete uniform crossover).")
    public void setCrossoverRate(double k) {
        if (k < 0) {
            k = 0;
        }
        if (k > 1) {
            k = 1;
        }
        this.crossoverRate = k;
    }

    public double getCrossoverRate() {
        return this.crossoverRate;
    }

    /**
     * Enhance greediness through amplification of the differential vector to
     * the best individual for DE2
     *
     * @param l
     */
    @Parameter(description = "Enhance greediness through amplification of the differential vector to the best individual for DE2.")
    public void setLambda(double l) {
        this.lambda = l;
    }

    public double getLambda() {
        return this.lambda;
    }

    /**
     * In case of trig. mutation DE, the TMO is applied wit probability Mt
     *
     * @param l
     */
    @Parameter(description = "In case of trigonometric mutation DE, the TMO is applied with probability Mt.")
    public void setMt(double l) {
        this.mt = l;
        if (this.mt < 0) {
            this.mt = 0;
        }
        if (this.mt > 1) {
            this.mt = 1;
        }
    }

    public double getMt() {
        return this.mt;
    }

    /**
     * This method allows you to choose the type of Differential Evolution.
     *
     * @param s The type.
     */
    @Parameter(name = "type", description = "Choose the type of Differential Evolution.")
    public void setDEType(eva2.optimization.enums.DEType s) {
        this.DEType = s;
        // show mt for trig. DE only
        GenericObjectEditor.setShowProperty(this.getClass(), "lambda", s == eva2.optimization.enums.DEType.RandToBest);
        GenericObjectEditor.setShowProperty(this.getClass(), "mt", s == eva2.optimization.enums.DEType.Trigonometric);
    }

    public eva2.optimization.enums.DEType getDEType() {
        return this.DEType;
    }

    /**
     * @return the maximumAge
     */
    public int getMaximumAge() {
        return maximumAge;
    }

    /**
     * @param maximumAge the maximumAge to set
     */
    @Parameter(description = "The maximum age of individuals, older ones are discarded. Set to -1 (or 0) to deactivate")
    public void setMaximumAge(int maximumAge) {
        this.maximumAge = maximumAge;
    }

    /**
     * Check whether the problem range will be enforced.
     *
     * @return the forceRange
     */
    public boolean isCheckRange() {
        return forceRange;
    }

    /**
     * @param forceRange the forceRange to set
     */
    @Parameter(description = "Set whether to enforce the problem range.")
    public void setCheckRange(boolean forceRange) {
        this.forceRange = forceRange;
    }

    public boolean isRandomizeFKLambda() {
        return randomizeFKLambda;
    }

    @Parameter(description = "If true, values for k, f, lambda are randomly sampled around +/- 20% of the given values.")
    public void setRandomizeFKLambda(boolean randomizeFK) {
        this.randomizeFKLambda = randomizeFK;
    }

    public boolean isCompareToParent() {
        return compareToParent;
    }

    @Parameter(description = "Compare a challenge individual to its original parent instead of a random one.")
    public void setCompareToParent(boolean compareToParent) {
        this.compareToParent = compareToParent;
    }

    public boolean isGenerational() {
        return generational;
    }

    @Parameter(description = "Switch to generational DE as opposed to standard steady-state DE")
    public void setGenerational(boolean generational) {
        this.generational = generational;
    }

    public boolean isCyclePop() {
        return cyclePop;
    }

    @Parameter(description = "if true, individuals are used as parents in a cyclic sequence - otherwise randomly ")
    public void setCyclePop(boolean cycle) {
        this.cyclePop = cycle;
    }

    /**
     * @return the maximumAge
     */
    public boolean isReEvaluate() {
        return reEvaluate;
    }

    /**
     */
    @Parameter(description = "Re-evaluates individuals which are older than maximum age instead of discarding them")
    public void setReEvaluate(boolean reEvaluate) {
        this.reEvaluate = reEvaluate;
    }
}