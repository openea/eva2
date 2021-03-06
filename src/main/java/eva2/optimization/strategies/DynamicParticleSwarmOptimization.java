package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.Population;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;
import eva2.util.annotation.Parameter;

/**
 * This extends our particle swarm implementation to dynamic optimization problems.
 */
@Description("Particle Swarm Optimization tuned for tracking a dynamic target")
public class DynamicParticleSwarmOptimization extends ParticleSwarmOptimization {

    public enum ChangeDetectionStrategy { RandomAnchor, AssumeChange, AssumeNoChange;

        @Override
        public String toString() {
            switch (this) {
                case RandomAnchor: return "Random Anchor";
                case AssumeChange: return "Assume Change";
                case AssumeNoChange: return "Assume no change";
                default: return name();
            }
        }
    }

    private boolean envHasChanged = false;
    /**
     * switch for the speed adaptation mechanism
     */
    protected boolean doSpeedAdaptation = false;
    private double phi0 = 0.005;
    private double phi3 = 0.0;
    private double highEnergyRaise = 2.;
    private double highEnergyRatio = .2;
    private double quantumRatio = 0.1;
    private double quantumCloudDia = 0.2;
    // the detectAnchor may be an Individual ID which will be ommitted by the update to check for env. changes.
    private int detectAnchor = 0;
    private double[] detectFit = null;
    /**
     * the change detection strategy
     */
    protected ChangeDetectionStrategy changeDetectStrategy;

    private double maxSpeedLimit = 0.1;
    private double minSpeedLimit = .003;

    private boolean plotBestOnly = false;
    private transient double[] lastBestPlot = null;

    /**
     * constant indication quantum particles
     */
    public static final int quantumType = 1;

    /**
     * A standard constructor.
     */
    public DynamicParticleSwarmOptimization() {
        super();
        this.changeDetectStrategy = ChangeDetectionStrategy.RandomAnchor;
    }

    /**
     * The copy constructor.
     *
     * @param a another DynPSO object
     */
    public DynamicParticleSwarmOptimization(DynamicParticleSwarmOptimization a) {
        super(a);
        envHasChanged = a.envHasChanged;
        doSpeedAdaptation = a.doSpeedAdaptation;
        phi0 = a.phi0;
        highEnergyRaise = a.highEnergyRaise;
        highEnergyRatio = a.highEnergyRatio;
        quantumRatio = a.quantumRatio;
        quantumCloudDia = a.quantumCloudDia;
        detectAnchor = a.detectAnchor;
        detectFit = a.detectFit;
        maxSpeedLimit = a.maxSpeedLimit;
        minSpeedLimit = a.minSpeedLimit;
        changeDetectStrategy = a.changeDetectStrategy;
    }

    @Override
    public Object clone() {
        return new DynamicParticleSwarmOptimization(this);
    }

    /**
     * Call all methods that may hide anything, cf. PSO
     */
    @Override
    public void hideHideable() {
        super.hideHideable();
        setQuantumRatio(quantumRatio);
        setDoSpeedAdaptation(doSpeedAdaptation);
        setHighEnergyRatio(highEnergyRatio);
    }

    /**
     * Adapts the swarm speed limit to give room to the favourite tracking speed. This is currently
     * done by approaching a speed limit value at two times the EMA speed.
     *
     * @param range
     */
    public void adaptTrackingSpeed(double[][] range) {
        double incFact = 1.1;
        double decFact = .97;
        double upperChgLim = .6;
        double lowerChgLim = .4;
        double normEmaSpd = getRelativeEMASpeed(range);
        double spdLim = getSpeedLimit();

        if (normEmaSpd > (upperChgLim * spdLim)) {
            setSpeedLimit(Math.min(maxSpeedLimit, incFact * spdLim));
        } else if (normEmaSpd < (lowerChgLim * spdLim)) {
            setSpeedLimit(Math.max(minSpeedLimit, decFact * spdLim));
        }
    }

    /**
     * Returns the favourite speed-limit for the current average speed, which currently is
     * twice the EMA speed. Range is required to calculate the speed relative to the playground size.
     *
     * @param range range values for the optimization problem
     * @return the favourite speed limit.
     */
    public double getFavTrackingSpeed(double[][] range) {
        return 2 * Mathematics.getRelativeLength(getEMASpeed(), range);
    }

    /**
     * This method will update a quantum individual.
     *
     * @param index The individual to update.
     * @param pop   The current population.
     * @param indy  The best individual found so far.
     */
    private void updateQuantumIndividual(int index, AbstractEAIndividual indy, Population pop) {
        InterfaceDataTypeDouble endy = (InterfaceDataTypeDouble) indy;
        // search for the local best position

        double[] localBestPosition;
        double[] position = endy.getDoubleData();

        localBestPosition = findNeighbourhoodOptimum(index, pop);

        double[] newPos = new double[position.length];
        double[][] range = endy.getDoubleRange();

        System.arraycopy(localBestPosition, 0, newPos, 0, newPos.length);

        double[] rand = getNormalRandVect(position.length, range, quantumCloudDia);
        //double[] rand = getUniformRandVect(position.length, range);

        Mathematics.vvAdd(newPos, rand, newPos);
        if (checkRange) {
            Mathematics.projectToRange(newPos, range);
        }

        if (indy instanceof InterfaceDataTypeDouble) {
            ((InterfaceDataTypeDouble) indy).setDoubleGenotype(newPos);
        } else {
            endy.setDoubleGenotype(newPos);
        }

        resetFitness(indy);

        plotIndy(position, null, (Integer) (indy.getData(indexKey)));
//            if (this.show) {
//                this.plot.setUnconnectedPoint(position[0], position[1], (Integer)(indy.getData(indexKey)));
        //this.plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index+1);
//        }
        //System.out.println("quantum particle " + index + " set to " + newPos[0] + "/" + newPos[1]);
    }

    private void plotBestIndy() {
        if (plot != null) {
            double[] curPosition = ((InterfaceDataTypeDouble) population.getBestEAIndividual()).getDoubleData();

            if (lastBestPlot != null) {
                this.plot.setConnectedPoint(lastBestPlot[0], lastBestPlot[1], 0);
            }
            this.plot.setConnectedPoint(curPosition[0], curPosition[1], 0);
            lastBestPlot = curPosition.clone();
        }
    }

    @Override
    protected void plotIndy(double[] curPosition, double[] curVelocity, int index) {
        if (this.show) {
            if (plotBestOnly) {
                if (index != ((Integer) (population.getBestEAIndividual().getData(indexKey)))) {
                    return;
                } else {
//					if (lastBestPlot != null) this.plot.setConnectedPoint(lastBestPlot[0], lastBestPlot[1], index);
//					this.plot.setConnectedPoint(curPosition[0], curPosition[1], index);
                    this.plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
                    lastBestPlot = curPosition.clone();
                }
            } else {
                if (curVelocity == null) {

                    this.plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
                } else {
                    this.plot.setConnectedPoint(curPosition[0], curPosition[1], index);
                    this.plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index);
                }
            }
        }
    }

    /**
     * Return a uniformly distributed position vector within a sphere of radius r in relation to the given range.
     *
     * @param vlen  vector length
     * @param range range of the playground in any axis
     * @param r     radius of the sphere relative to the range
     * @return a uniformly distributed vector within a sphere of radius r in relation to the given range
     */
    protected double[] getUniformRandVect(int vlen, double[][] range, double r) {
        double normfact = 0.;
        double[] rand = new double[vlen];
        for (int i = 0; i < rand.length; i++) {
            rand[i] = RNG.gaussianDouble(1);
            normfact += (rand[i] * rand[i]);
        }
        normfact = Math.sqrt(normfact); // for normalization

        // normalize and scale with range
        for (int i = 0; i < rand.length; i++) {
            // leaving out the sqrt(rand) part would result in a point on the sphere (muller, marsaglia)
            rand[i] = rand[i] / normfact * Math.sqrt(RNG.randomDouble()) * r * (range[i][1] - range[i][0]);
        }
        return rand;
    }

    /**
     * Return a normally distributed position vector around zero with the given standard deviation in relation to the given range.
     *
     * @param vlen   vector length
     * @param range  range of the playground in any axis
     * @param stddev standard deviation for the random vector
     * @return a normally distributed position vector around zero with the given standard deviation in relation to the given range.
     */
    protected double[] getNormalRandVect(int vlen, double[][] range, double stddev) {
        double[] rand = new double[vlen];
        for (int i = 0; i < rand.length; i++) {
            rand[i] = RNG.gaussianDouble(stddev * (range[i][1] - range[i][0]));
        }
        return rand;
    }

    @Override
    protected double[] updateVelocity(int index, double[] lastVelocity, double[] bestPosition, double[] curPosition, double[] localBestPos, double[][] range) {
        if (envHasChanged) {
            double chi;
            double[] curVelocity = new double[lastVelocity.length];
            for (int i = 0; i < lastVelocity.length; i++) {
                // the component from the old velocity
                curVelocity[i] = this.inertnessOrChi * lastVelocity[i];
                if (algType == PSOType.Constriction) {
                    chi = inertnessOrChi;
                } else {
                    chi = 1.;
                }
                // random perturbation
                //curVelocity[i]  += (this.phi0 * chi * RNG.randomDouble(-1., 1.) * (range[i][1] - range[i][0]));
                curVelocity[i] += (this.phi0 * chi * getSpeedLimit(index) * RNG.randomDouble(-1., 1.) * (range[i][1] - range[i][0]));
                // a problem specific attractor
                curVelocity[i] += getProblemSpecificAttraction(i, chi);
                // the component from the social model
                curVelocity[i] += (getIndySocialModifier(index, chi) * (localBestPos[i] - curPosition[i]));
            }
            return curVelocity;
        } else {
            return super.updateVelocity(index, lastVelocity, bestPosition, curPosition, localBestPos, range);
        }
    }

    protected double getProblemSpecificAttraction(int i, double chi) {
//		if (problem instanceof DynLocalizationProblem) {
//			// TODO test this!
//			//hier weiter
//			double[] att = ((DynLocalizationProblem)problem).getProblemSpecificAttractor();
//			return (this.phi3 * chi * RNG.randomDouble(0, 1.))*att[i];
//		} else 
        return 0;
    }

    /**
     * Get a modifier for the velocity update, social component. A random double in (0,1) is standard.
     *
     * @param index  index of the concerned individual
     * @param chiVal current chi value
     * @return a double value
     */
    protected double getIndySocialModifier(int index, double chiVal) {
        return (this.phi2 * chiVal * RNG.randomDouble(0, 1));
//		if (index < 50) return (this.phi2 * chi * RNG.randomDouble(0,1));
//		else return (this.phi2 * chi * (-1.) * RNG.randomDouble(0,1));
    }

    /**
     * Get the speed limit of an individual (respecting highEnergyRatio, so some individuals may be accelerated).
     *
     * @param index the individuals index
     * @return the speed limit of the individual
     */
    @Override
    protected double getSpeedLimit(int index) {
        if (index >= population.size() * highEnergyRatio) {
            return speedLimit;
        } else {
            if (highEnergyRaise == 0.) {
                return maxSpeedLimit;
            } else {
                return speedLimit * highEnergyRaise;
            }
        }
    }

    /////////////////////////////////////////// these are called from the optimize loop
    @Override
    protected void startOptimize() {
        super.startOptimize();
        if (detectAnchor >= 0) {    // set the new detection anchor individual
            detectAnchor = RNG.randomInt(0, population.size() - 1);
            if (detectFit == null) {
                detectFit = (population.getIndividual(detectAnchor).getFitness()).clone();
            } else {
                System.arraycopy(population.getIndividual(detectAnchor).getFitness(), 0, detectFit, 0, detectFit.length);
            }
        }
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        super.initializeByPopulation(pop, reset);
        double quantumCount = 0.;
        // do what the usual function does plus announce quantum particles
        if (quantumRatio > 0.) {
            for (int i = 0; i < this.population.size(); i++) {
                AbstractEAIndividual indy = population.get(i);
                if (i >= quantumCount) {
                    indy.putData(partTypeKey, quantumType);
                    quantumCount += 1. / quantumRatio;
                }
            }
        }
    }

    /**
     * This method will update a given individual
     * according to the PSO method
     *
     * @param index The individual to update.
     * @param pop   The current population.
     * @param indy  The best individual found so far.
     */
    @Override
    protected void updateIndividual(int index, AbstractEAIndividual indy, Population pop) {
        if (index != detectAnchor) { // only for non anchor individuals (its -1 if other detect strategy is used)
            if (indy instanceof InterfaceDataTypeDouble) {
                int type = (Integer) indy.getData(partTypeKey);
                switch (type) {
                    case quantumType:
                        updateQuantumIndividual(index, indy, pop);
                        break;
                    default:    // handles the standard way of updating a particle
                        super.updateIndividual(index, indy, pop);
                        break;
                }
            } else {
                throw new RuntimeException("Could not perform PSO update, because individual is not instance of InterfaceESIndividual!");
            }
        }
    }

    /**
     * This method will evaluate the current population using the
     * given problem.
     *
     * @param population The population that is to be evaluated
     */
    @Override
    protected void evaluatePopulation(Population population) {
        envHasChanged = false;
        super.evaluatePopulation(population);
        if (show && plotBestOnly) {
            plotBestIndy();
        }

        envHasChanged = detectChange(this.population);

        if (doSpeedAdaptation) {
            adaptTrackingSpeed(((InterfaceDataTypeDouble) population.get(0)).getDoubleRange());
        }
    }

    @Override
    protected boolean isIndividualToUpdate(AbstractEAIndividual indy) {
        return (envHasChanged || super.isIndividualToUpdate(indy));
    }

    @Override
    protected void logBestIndividual() {
        // log the best individual of the population
        if (envHasChanged || (this.population.getBestEAIndividual().isDominatingDebConstraints(this.bestIndividual))) {
            this.bestIndividual = (AbstractEAIndividual) this.population.getBestEAIndividual().clone();
            this.bestIndividual.putData(partBestFitKey, this.bestIndividual.getFitness());
            this.bestIndividual.putData(partBestPosKey, ((InterfaceDataTypeDouble) this.bestIndividual).getDoubleData());
            //System.out.println("-- best ind set to " + ((InterfaceDataTypeDouble)this.bestIndividual).getDoubleData()[0] + "/" + ((InterfaceDataTypeDouble)this.bestIndividual).getDoubleData()[1]);
        }
    }

    /**
     * Checks for env change depending on the detection strategy.
     * For anchor detection, if detectAnchor is a valid ID, it returns true if the anchor individuals fitness has changed.
     * For assume change, true is returned, for assume no change, false is returned.
     *
     * @param population the population to check for a change
     * @return true if the population has changed as to the detect strategy, else false
     */
    protected boolean detectChange(Population population) {
        switch (changeDetectStrategy) {
            case RandomAnchor:
                if (detectAnchor >= 0) {
                    return !(java.util.Arrays.equals(detectFit, this.population.getIndividual(detectAnchor).getFitness()));
                } else {
                    System.err.println("warning, inconsistency in detectChange");
                }
                break;
            case AssumeChange:
                return true;
            case AssumeNoChange:
                return false;
        }
        System.err.println("warning, inconsistency in detectChange");
        return false;
    }

    @Override
    public void setPopulation(Population pop) {
        super.setPopulation(pop);
        if (detectAnchor >= pop.size()) {
            detectAnchor = 0;
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        setEmaPeriods(15);
        if (doSpeedAdaptation) {
            setSpeedLimit(2 * getInitialVelocity());
        }
    }


    @Override
    @Hidden
    public void setProblem(InterfaceOptimizationProblem problem) {
        super.setProblem(problem);
        if (problem instanceof AbstractOptimizationProblem) {
            ((AbstractOptimizationProblem) problem).informAboutOptimizer(this);
        }
    }

    /**
     * This method will return a string describing all properties of the optimizer
     * and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("Dynamic Particle Swarm Optimization:\nOptimization Problem: ");
        strB.append(this.optimizationProblem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.population.getStringRepresentation());
        return strB.toString();
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "DynPSO";
    }

    /**
     * @return the quantumCloudDia
     */
    public double getQuantumCloudDia() {
        return quantumCloudDia;
    }

    /**
     * @param quantumCloudDia the quantumCloudDia to set
     */
    public void setQuantumCloudDia(double quantumCloudDia) {
        this.quantumCloudDia = quantumCloudDia;
    }

    /**
     * @return the quantumRatio
     */
    public double getQuantumRatio() {
        return quantumRatio;
    }

    /**
     * @param quantumRatio the quantumRatio to set
     */
    public void setQuantumRatio(double quantumRatio) {
        this.quantumRatio = quantumRatio;
        if (quantumRatio == 0.) {
            GenericObjectEditor.setHideProperty(this.getClass(), "quantumCloudDia", true);
        } else {
            GenericObjectEditor.setHideProperty(this.getClass(), "quantumCloudDia", false);
        }
    }

    /**
     * @return the highEnergyRaise
     */
    public double getHighEnergyRaise() {
        return highEnergyRaise;
    }

    /**
     * @param highEnergyRaise the highEnergyRaise to set
     */
    public void setHighEnergyRaise(double highEnergyRaise) {
        this.highEnergyRaise = highEnergyRaise;
    }

    /**
     * @return the highEnergyRatio
     */
    public double getHighEnergyRatio() {
        return highEnergyRatio;
    }

    /**
     * @param highEnergyRatio the highEnergyRatio to set
     */
    public void setHighEnergyRatio(double highEnergyRatio) {
        this.highEnergyRatio = highEnergyRatio;
        if (highEnergyRatio == 0.) {
            GenericObjectEditor.setHideProperty(this.getClass(), "highEnergyRaise", true);
        } else {
            GenericObjectEditor.setHideProperty(this.getClass(), "highEnergyRaise", false);
        }
    }

    /**
     * @return the maxSpeedLimit
     */
    public double getMaxSpeedLimit() {
        return maxSpeedLimit;
    }

    /**
     * @param maxSpeedLimit the maxSpeedLimit to set
     */
    public void setMaxSpeedLimit(double maxSpeedLimit) {
        this.maxSpeedLimit = maxSpeedLimit;
    }

    /**
     * @return the minSpeedLimit
     */
    public double getMinSpeedLimit() {
        return minSpeedLimit;
    }

    /**
     * @param minSpeedLimit the minSpeedLimit to set
     */
    public void setMinSpeedLimit(double minSpeedLimit) {
        this.minSpeedLimit = minSpeedLimit;
    }

    /**
     * @return the doSpeedAdaptation
     */
    public boolean isDoSpeedAdaptation() {
        return doSpeedAdaptation;
    }

    /**
     * @param doSpeedAdaptation the doSpeedAdaptation to set
     */
    public void setDoSpeedAdaptation(boolean doSpeedAdaptation) {
        this.doSpeedAdaptation = doSpeedAdaptation;
        if (doSpeedAdaptation && getEmaPeriods() < 1) {
            int newEmaP = 15;
            System.err.println("warning: EMA periods at " + getEmaPeriods() + " and speed adaption set to true... setting it to " + newEmaP);
            setEmaPeriods(newEmaP);
        }
        GenericObjectEditor.setHideProperty(getClass(), "minSpeedLimit", !doSpeedAdaptation);
        GenericObjectEditor.setHideProperty(getClass(), "maxSpeedLimit", !doSpeedAdaptation);
    }

    /**
     * @return the changeDetectStrategy
     */
    public ChangeDetectionStrategy getChangeDetectStrategy() {
        return changeDetectStrategy;
    }

    /**
     * @param changeDetectStrategy the changeDetectStrategy to set
     */
    public void setChangeDetectStrategy(ChangeDetectionStrategy changeDetectStrategy) {
        this.changeDetectStrategy = changeDetectStrategy;
        if (changeDetectStrategy == ChangeDetectionStrategy.RandomAnchor) { // random anchor
            detectAnchor = 0; // this will be set to a random individual
        } else {
            detectAnchor = -1;
        }
    }

    /**
     * @return the phi0 value
     */
    public double getPhi0() {
        return phi0;
    }

    /**
     * @param phi0 the phi0 to set
     */
    @Parameter(description = "The random perturbation factor in relation to the problem range")
    public void setPhi0(double phi0) {
        this.phi0 = phi0;
    }

    /**
     * @return the phi3
     */
    public double getPhi3() {
        return phi3;
    }

    /**
     * @param phi3 the phi3 to set
     */
    @Parameter(description = "Acceleration of the problem specific attractor")
    public void setPhi3(double phi3) {
        this.phi3 = phi3;
    }

    public Plot getPlot() {
        return plot;
    }

    /**
     * @return the plotBestOnly
     */
    public boolean isPlotBestOnly() {
        return plotBestOnly;
    }

    /**
     * @param plotBestOnly the plotBestOnly to set
     */
    public void setPlotBestOnly(boolean plotBestOnly) {
        this.plotBestOnly = plotBestOnly;
    }
}