package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypePermutation;
import eva2.optimization.individuals.InterfaceOBGAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;


/**
 *          <p>
 *          Mutates a permutation by inversion a part of the permutation.
 *          <br><br>
 *          <b>Example: </b>
 *          <pre>
 *                                                                1 2 | 3 4 5 | 6 7 8 9 -&gt;
 *                                                                1 2 | 5 4 3 | 6 7 8 9
 *                                                       </pre>
 */
@Description("This mutation operators inverts a certain section of the OBGA.")
public class MutateOBGAInversion implements java.io.Serializable, InterfaceMutation {

    public MutateOBGAInversion() {
    }

    @Override
    public Object clone() {
        return this;
    }

    /**
     * This method allows you to evaluate whether two mutation operators
     * are actually the same.
     *
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        return mutator instanceof MutateOBGAInversion;
    }

    @Override
    public void initialize(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        //nothing to initialize
    }

    @Override
    public void mutate(AbstractEAIndividual individual) {
        int[][] permnew = ((InterfaceOBGAIndividual) individual).getOBGenotype().clone();
        int[][] perm = ((InterfaceDataTypePermutation) individual).getPermutationData();
        for (int p = 0; p < perm.length; p++) {
            int p1 = RNG.randomInt(0, perm[p].length - 1);
            int p2 = RNG.randomInt(p1, perm[p].length - 1);
            for (int i = 0; i <= (p2 - p1); i++) {
                permnew[p][p1 + i] = perm[p][p2 - i];
            }
        }
        ((InterfaceOBGAIndividual) individual).setOBGenotype(permnew);
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "OBGA inversion mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "OBGA inversion mutation";
    }

}
