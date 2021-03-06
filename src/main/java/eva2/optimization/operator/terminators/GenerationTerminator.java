package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 *
 */
@Description(value = "Terminate after the given number of generations")
public class GenerationTerminator implements InterfaceTerminator, Serializable {

    /**
     * Number of fitness calls on the problem which is optimized
     */
    protected int maxGenerations = 100;
    private String msg = "";

    @Override
    public void initialize(InterfaceOptimizationProblem prob) {
        msg = "Not terminated.";
    }

    public GenerationTerminator() {}

    public GenerationTerminator(int gens) {
        maxGenerations = gens;
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerminated(solSet.getCurrentPopulation());
    }

    @Override
    public boolean isTerminated(PopulationInterface pop) {
        if (maxGenerations < pop.getGeneration()) {
            msg = maxGenerations + " generations reached.";
            return true;
        }
        return false;
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "Generations calls=" + maxGenerations;
    }

    @Parameter(description = "Number of generations to evaluate.")
    public void setGenerations(int x) {
        maxGenerations = x;
    }

    public int getGenerations() {
        return maxGenerations;
    }
}