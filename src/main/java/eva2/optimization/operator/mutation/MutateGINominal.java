package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 */
@Description("The nominal mutation alters n element of the int attributes completely at random.")
public class MutateGINominal implements InterfaceMutation, java.io.Serializable {

    int numberOfMutations = 2;

    public MutateGINominal() {

    }

    public MutateGINominal(MutateGINominal mutator) {
        this.numberOfMutations = mutator.numberOfMutations;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGINominal();
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGINominal) {
            MutateGINominal mut = (MutateGINominal) mutator;
            return this.numberOfMutations == mut.numberOfMutations;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void initialize(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[] x = ((InterfaceGIIndividual) individual).getIGenotype();
            int[][] range = ((InterfaceGIIndividual) individual).getIntRange();
            int mutInd = 0;
            for (int k = 0; k < this.numberOfMutations; k++) {
                try {
                    mutInd = RNG.randomInt(0, x.length - 1);
                } catch (java.lang.ArithmeticException e) {
                    System.out.println("x.length " + x.length);
                }
                x[mutInd] = RNG.randomInt(range[mutInd][0], range[mutInd][1]);
            }
            ((InterfaceGIIndividual) individual).setIGenotype(x);
        }
    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GI nominal mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GI nominal mutation";
    }

    /**
     * This method allows you to set the number of mutations to occur.
     *
     * @param n The number of mutations
     */
    public void setNumberOfMutations(int n) {
        this.numberOfMutations = n;
    }

    public int getNumberOfMutations() {
        return this.numberOfMutations;
    }

    public String numberOfMutationsTipText() {
        return "Gives the number of mutations.";
    }
}