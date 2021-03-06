package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectEPTournaments;
import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.util.ArrayList;
import java.util.List;

/**
 * Evolutionary programming by Fogel. Works fine but is actually a quite greedy
 * local search strategy solely based on mutation. To prevent any confusion, the
 * mutation rate is temporaily set to 1.0. Potential citation: the PhD thesis of
 * David B. Fogel (1992).
 */
@Description("This is a basic Evolutionary Programming scheme.")
public class EvolutionaryProgramming extends AbstractOptimizer implements java.io.Serializable {

    private int populationSize = 0;
    private InterfaceSelection environmentSelection = new SelectEPTournaments();
    private String identifier = "";
    transient private List<InterfacePopulationChangedEventListener> populationChangedEventListeners = new ArrayList<>();

    public EvolutionaryProgramming() {
    }

    public EvolutionaryProgramming(EvolutionaryProgramming a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.identifier = a.identifier;
        this.environmentSelection = (InterfaceSelection)a.environmentSelection.clone();
    }

    @Override
    public Object clone() {
        return new EvolutionaryProgramming(this);
    }

    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.evaluatePopulation(this.population);
        this.populationSize = this.population.size();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
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
     * This method will generate the offspring population from the given
     * population of evaluated individuals.
     */
    private Population generateChildren() {
        Population result = this.population.cloneWithoutInds();
        AbstractEAIndividual mutant;

        result.clear();
        for (int i = 0; i < this.population.size(); i++) {
            mutant = (AbstractEAIndividual) this.population.get(i).clone();
            double tmpD = mutant.getMutationProbability();
            mutant.setMutationProbability(1.0);
            mutant.mutate();
            mutant.setMutationProbability(tmpD);
            result.add(mutant);
        }
        return result;
    }

    @Override
    public void optimize() {
        Population nextGeneration, parents;

        this.environmentSelection.prepareSelection(this.population);
        parents = this.environmentSelection.selectFrom(this.population, this.populationSize);
        this.population.clear();
        this.population.addPopulation(parents);
        nextGeneration = this.generateChildren();
        this.evaluatePopulation(nextGeneration);
        nextGeneration.addPopulation(this.population);
        this.population = nextGeneration;

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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
        result += "Evolutionary Programming:\n";
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
        return "EP";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * Choose a method for selecting the reduced population.
     *
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        this.environmentSelection = selection;
    }

    public InterfaceSelection getEnvironmentSelection() {
        return this.environmentSelection;
    }

    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }
}
