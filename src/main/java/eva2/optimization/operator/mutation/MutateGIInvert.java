package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 */
@Description("The invert mutation inverts a segment of the int[].")
public class MutateGIInvert implements InterfaceMutation, java.io.Serializable {

    int maxLengthOfInvert = 2;

    public MutateGIInvert() {

    }

    public MutateGIInvert(MutateGIInvert mutator) {
        this.maxLengthOfInvert = mutator.maxLengthOfInvert;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGIInvert();
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGIInvert) {
            MutateGIInvert mut = (MutateGIInvert) mutator;
            return this.maxLengthOfInvert == mut.maxLengthOfInvert;
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
            int range, center, index = 0;
            //this.pintInt("Before ", x);
            range = RNG.randomInt(1, this.maxLengthOfInvert);
            if (2 * range >= x.length) {
                return;
            }
            center = RNG.randomInt(range, x.length - 1 - range);
            //System.out.println("Range: " + range + " Center: " + center);
            int[] tmp = new int[x.length];
            System.arraycopy(x, 0, tmp, 0, x.length);
            for (int i = center - range; i < center + range; i++) {
                x[center - 1 + range - index] = tmp[i];
                index++;
            }
            //this.pintInt("After ", tmp);
            //this.pintInt("After ", x);
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

    private void pintInt(String s, int[] x) {
        String tmp = "{" + x[0];
        for (int i = 1; i < x.length; i++) {
            tmp += ", " + x[i];
        }
        System.out.println(s + tmp + "}");
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GI invert mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GI invert mutation";
    }

    /**
     * This method allows you to set the max length of invert.
     *
     * @param n The max length of invert
     */
    public void setMaxLengthOfInvert(int n) {
        this.maxLengthOfInvert = n;
    }

    public int getMaxLengthOfInvert() {
        return this.maxLengthOfInvert;
    }

    public String maxLengthOfInvertTipText() {
        return "Gives half the maximum length of an inverted segment.";
    }
}