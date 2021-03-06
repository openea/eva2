package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.enums.PostProcessMethod;
import eva2.optimization.operator.mutation.MutateESFixedStepSize;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.population.*;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * The clustering hill climber is similar to a multi-start hill climber. In
 * addition so optimizing a set of individuals in parallel using a (1+1)
 * strategy, the population is clustered in regular intervals. If several
 * individuals have gathered together in the sense that they are interpreted as
 * a cluster, only a subset of representatives of the cluster is taken over to
 * the next HC step while the rest is discarded. This means that the population
 * size may be reduced.
 * <p>
 * As soon as the improvement by HC lies below a threshold, the mutation step
 * size is decreased. If the step size is decreased below a certain threshold,
 * the current population is stored to an archive and reinitialized. Thus, the
 * number of optima that may be found and returned by getAllSolutions is higher
 * than the population size.
 *
 */
@Description("Similar to multi-start HC, but clusters the population during optimization to remove redundant individuals for efficiency."
        + "If the local search step does not achieve a minimum improvement, the population may be reinitialized.")
public class ClusteringHillClimbing extends AbstractOptimizer implements InterfacePopulationChangedEventListener, Serializable, InterfaceAdditionalPopulationInformer {

    private transient Population archive = new Population();
    private int hcEvalCycle = 1000;
    private int initialPopSize = 100;
    private int notifyGuiEvery = 50;
    private double sigmaClust = 0.01;
    private double minImprovement = 0.000001;
    private double stepSizeThreshold = 0.000001;
    private double initialStepSize = 0.1;
    // reduce the step size when there is hardy improvement. 
    private double reduceFactor = 0.2;
    private MutateESFixedStepSize mutator = new MutateESFixedStepSize(0.1);
    private PostProcessMethod localSearchMethod = PostProcessMethod.nelderMead;
    private boolean doReinitialization = true;

    public ClusteringHillClimbing() {
        hideHideable();
    }

    public ClusteringHillClimbing(int initialPopSize, PostProcessMethod lsMethod) {
        this();
        setInitialPopSize(initialPopSize);
        setLocalSearchMethod(lsMethod);
    }

    public ClusteringHillClimbing(ClusteringHillClimbing other) {
        hideHideable();
        population = (Population) other.population.clone();
        optimizationProblem = (InterfaceOptimizationProblem) other.optimizationProblem.clone();

        hcEvalCycle = other.hcEvalCycle;
        initialPopSize = other.initialPopSize;
        notifyGuiEvery = other.notifyGuiEvery;
        sigmaClust = other.sigmaClust;
        minImprovement = other.minImprovement;
        stepSizeThreshold = other.stepSizeThreshold;
        initialStepSize = other.initialStepSize;
        reduceFactor = other.reduceFactor;
        mutator = (MutateESFixedStepSize) other.mutator.clone();
    }

    @Override
    public Object clone() {
        return new ClusteringHillClimbing(this);
    }

    /**
     * Hide the population.
     */
    public void hideHideable() {
        GenericObjectEditor.setHideProperty(getClass(), "population", true);
        setDoReinitialization(isDoReinitialization());
        setLocalSearchMethod(getLocalSearchMethod());
    }

    @Override
    public void initialize() {
        mutator = new MutateESFixedStepSize(initialStepSize);
        archive = new Population();
        hideHideable();
        population.setTargetSize(initialPopSize);
        this.optimizationProblem.initializePopulation(this.population);
        population.addPopulationChangedEventListener(null); // noone will be notified directly on pop changes
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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
        population.addPopulationChangedEventListener(null);
        if (reset) {
            this.population.initialize();
            this.optimizationProblem.evaluate(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    @Override
    public void optimize() {
        double improvement;

        population.addPopulationChangedEventListener(this);
        population.setNotifyEvalInterval(notifyGuiEvery);
        Pair<Population, Double> popD;
        int funCallsBefore = population.getFunctionCalls();
        int evalsNow, lastOverhead = (population.getFunctionCalls() % hcEvalCycle);
        if (lastOverhead > 0) {
            evalsNow = (2 * hcEvalCycle - (population.getFunctionCalls() % hcEvalCycle));
        } else {
            evalsNow = hcEvalCycle;
        }
        do {
            popD = PostProcess.clusterLocalSearch(localSearchMethod, population, (AbstractOptimizationProblem) optimizationProblem, sigmaClust, evalsNow, 0.5, mutator);
            //		(population, (AbstractOptimizationProblem)problem, sigmaClust, hcEvalCycle - (population.getFunctionCalls() % hcEvalCycle), 0.5);
            if (popD.head().getFunctionCalls() == funCallsBefore) {
                System.err.println("Bad case, increasing allowed evaluations!");
                evalsNow = Math.max(evalsNow++, (int) (evalsNow * 1.2));
            }
        } while (popD.head().getFunctionCalls() == funCallsBefore);
        improvement = popD.tail();
        population = popD.head();

        popD.head().setGeneration(population.getGeneration() + 1);

        if (doReinitialization && (improvement < minImprovement)) {
            if ((localSearchMethod != PostProcessMethod.hillClimber) || (mutator.getSigma() < stepSizeThreshold)) { // reinit!
                // is performed for nm and cma, and if hc has too low sigma

                if (localSearchMethod == PostProcessMethod.hillClimber) {
                    mutator.setSigma(initialStepSize);
                }

                // store results
                archive.setFunctionCalls(population.getFunctionCalls());
                archive.addPopulation(population);

                Population tmpPop = new Population();
                tmpPop.addPopulationChangedEventListener(null);
                tmpPop.setTargetSize(initialPopSize);
                this.optimizationProblem.initializePopulation(tmpPop);
                tmpPop.setSameParams(population);
                tmpPop.setTargetSize(initialPopSize);
                this.optimizationProblem.evaluate(tmpPop);

                // reset population while keeping function calls etc.
                population.clear();
                population.addPopulation(tmpPop);
                population.incrFunctionCallsBy(tmpPop.size());

            } else {  // decrease step size for hc
                if (localSearchMethod != PostProcessMethod.hillClimber) {
                    System.err.println("Invalid case in ClusteringHillClimbing!");
                }
                mutator.setSigma(mutator.getSigma() * reduceFactor);
            }
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);

    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.FUN_CALL_INTERVAL_REACHED) == 0) {
            // set funcalls to real value
            population.setFunctionCalls(((Population) source).getFunctionCalls());
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population tmp = new Population();
        tmp.addPopulation(archive);
        tmp.addPopulation(population);
        tmp.setFunctionCalls(population.getFunctionCalls());
        tmp.setGeneration(population.getGeneration());
        return new SolutionSet(population, tmp);
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        StringBuilder sbuf = new StringBuilder("Clustering Hill Climbing");
        sbuf.append(", initial pop size: ");
        sbuf.append(getPopulation().getTargetSize());
        sbuf.append("Optimization Problem: ");
        sbuf.append(this.optimizationProblem.getStringRepresentationForProblem(this));
        sbuf.append(this.population.getStringRepresentation());
        return sbuf.toString();
    }

    @Override
    public String getName() {
        return "ClustHC-" + initialPopSize + "-" + localSearchMethod;
    }

    /**
     * @return the hcEvalCycle
     */
    public int getEvalCycle() {
        return hcEvalCycle;
    }

    /**
     * @param hcEvalCycle the hcEvalCycle to set
     */
    @Parameter(description = "The number of evaluations between two clustering/adaption steps.")
    public void setEvalCycle(int hcEvalCycle) {
        this.hcEvalCycle = hcEvalCycle;
    }

    /**
     * @return the initialPopSize
     */
    public int getInitialPopSize() {
        return initialPopSize;
    }

    /**
     * @param initialPopSize the initialPopSize to set
     */
    @Parameter(description = "Population size at the start and at reinitialization times.")
    public void setInitialPopSize(int initialPopSize) {
        this.initialPopSize = initialPopSize;
    }

    /**
     * @return the sigma
     */
    public double getSigmaClust() {
        return sigmaClust;
    }

    /**
     * @param sigma the sigma to set
     */
    @Parameter(description = "Defines the sigma distance parameter for density based clustering.")
    public void setSigmaClust(double sigma) {
        this.sigmaClust = sigma;
    }

    /**
     * @return the notifyGuiEvery
     */
    public int getNotifyGuiEvery() {
        return notifyGuiEvery;
    }

    /**
     * @param notifyGuiEvery the notifyGuiEvery to set
     */
    @Parameter(description = "How often to notify the GUI to plot the fitness etc.")
    public void setNotifyGuiEvery(int notifyGuiEvery) {
        this.notifyGuiEvery = notifyGuiEvery;
    }

    /**
     * @return the minImprovement
     */
    public double getMinImprovement() {
        return minImprovement;
    }

    /**
     * @param minImprovement the minImprovement to set
     */
    @Parameter(description = "Improvement threshold below which the mutation step size is reduced or the population reinitialized.")
    public void setMinImprovement(double minImprovement) {
        this.minImprovement = minImprovement;
    }

    /**
     * @return the reinitForStepSize
     */
    public double getStepSizeThreshold() {
        return stepSizeThreshold;
    }

    /**
     * @param reinitForStepSize the reinitForStepSize to set
     */
    @Parameter(description = "Threshold for the mutation step size below which the population is seen as converged and reinitialized.")
    public void setStepSizeThreshold(double reinitForStepSize) {
        this.stepSizeThreshold = reinitForStepSize;
    }

    /**
     * @return the initialStepSize
     */
    public double getStepSizeInitial() {
        return initialStepSize;
    }

    /**
     * @param initialStepSize the initialStepSize to set
     */
    @Parameter(description = "Initial mutation step size for hill climbing, relative to the problem range.")
    public void setStepSizeInitial(double initialStepSize) {
        this.initialStepSize = initialStepSize;
    }

    public PostProcessMethod getLocalSearchMethod() {
        return localSearchMethod;
    }

    @Parameter(description = "Set the method to be used for the hill climbing as local search")
    public void setLocalSearchMethod(PostProcessMethod localSearchMethod) {
        this.localSearchMethod = localSearchMethod;
        GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeInitial", localSearchMethod == PostProcessMethod.hillClimber);
        GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeThreshold", localSearchMethod == PostProcessMethod.hillClimber);
    }

    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"numIndies", "sigma", "numArchived", "archivedMeanDist"};
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{"The current population size", "Current step size in case of stochastic HC", "Number of archived solutions", "Mean distance of archived solutions"};
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{population.size(), mutator.getSigma(), archive.size(), archive.getPopulationMeasures()[0]};
    }

    public boolean isDoReinitialization() {
        return doReinitialization;
    }

    @Parameter(description = "Activate reinitialization if no improvement was achieved.")
    public void setDoReinitialization(boolean doReinitialization) {
        this.doReinitialization = doReinitialization;
        GenericObjectEditor.setShowProperty(this.getClass(), "minImprovement", doReinitialization);
    }
}
