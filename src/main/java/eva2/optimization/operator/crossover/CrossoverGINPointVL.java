package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 *
 */
@Description("This is a variable length n-point crossover between m individuals.")
public class CrossoverGINPointVL implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;
    private int numberOfCrossovers = 3;

    public CrossoverGINPointVL() {

    }

    public CrossoverGINPointVL(CrossoverGINPointVL mutator) {
        this.optimizationProblem = mutator.optimizationProblem;
        this.numberOfCrossovers = mutator.numberOfCrossovers;
    }

    /**
     * This method will enable you to clone a given crossover operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverGINPointVL(this);
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     *
     * @param indy1    The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size() + 1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i + 1] = (AbstractEAIndividual) partners.get(i).clone();
        }
        if (partners.size() == 0) {
            return result;
        }
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
        if ((indy1 instanceof InterfaceGIIndividual) && (partners.get(0) instanceof InterfaceGIIndividual)) {
            int[] length = new int[partners.size() + 1];
            int mixer = RNG.randomInt(0, partners.size());
            int[][] crossoverPoints = new int[length.length][this.numberOfCrossovers];
            int[][] parents = new int[partners.size() + 1][];
            int[][] offsprings = new int[partners.size() + 1][];

            length[0] = ((InterfaceGIIndividual) indy1).getGenotypeLength();
            parents[0] = ((InterfaceGIIndividual) indy1).getIGenotype();
            for (int i = 0; i < partners.size(); i++) {
                length[i + 1] = ((InterfaceGIIndividual) partners.get(i)).getGenotypeLength();
                parents[i + 1] = ((InterfaceGIIndividual) partners.get(i)).getIGenotype();
            }

            int[][][] fragments = this.getNFragmentsFrom(parents);
            int index, tmpLen;
            for (int i = 0; i < offsprings.length; i++) {
                index = 0;
                tmpLen = 0;
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    tmpLen += fragments[(i + j) % fragments.length][index].length;
                    index++;
                }
                offsprings[i] = new int[tmpLen];
                index = 0;
                tmpLen = 0;
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    System.arraycopy(fragments[(i + j) % fragments.length][index], 0, offsprings[i], tmpLen, fragments[(i + j) % fragments.length][index].length);
                    tmpLen += fragments[(i + j) % fragments.length][index].length;
                    index++;
                }
            }


            for (int i = 0; i < result.length; i++) {
                ((InterfaceGIIndividual) result[i]).setIntegerDataLength(offsprings[i].length);
                ((InterfaceGIIndividual) result[i]).setIGenotype(offsprings[i]);
            }
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) {
            result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        }
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /**
     * This method will return n fragments from the int arrays
     */
    private int[][][] getNFragmentsFrom(int[][] parents) {
        int[][][] result = new int[parents.length][this.numberOfCrossovers][];

        for (int i = 0; i < parents.length; i++) {
            // for each parents get n fragments
            if (this.numberOfCrossovers + 2 > parents[i].length) {
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    result[i][j] = new int[parents[i].length];
                    System.arraycopy(parents[i], 0, result[i][j], 0, parents[i].length);
                }
            } else {
                int[] crossoverpoints = this.getCrossoverPoints(parents[i].length);
                int lastPoint = 0;
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    result[i][j] = new int[crossoverpoints[j] - lastPoint];
                    System.arraycopy(parents[i], lastPoint, result[i][j], 0, crossoverpoints[j] - lastPoint);
                    lastPoint = crossoverpoints[j];
                }
            }
        }

        return result;
    }

    /**
     * This method will return a number of n sorted crossover points
     *
     * @param length The length of the int array
     * @return int[] the list of crossover points
     */
    private int[] getCrossoverPoints(int length) {
        int[] result = new int[this.numberOfCrossovers];
        BitSet bitset = new BitSet(length);

        while (bitset.cardinality() < this.numberOfCrossovers) {
            bitset.set(RNG.randomInt(1, length - 2));
        }
        int index = 0;
        for (int i = 0; i < length; i++) {
            if (bitset.get(i)) {
                result[index] = i;
                index++;
            }
        }

        return result;
    }

    /**
     * This method allows you to evaluate wether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverGINPointVL) {
            CrossoverGINPointVL cross = (CrossoverGINPointVL) crossover;
            return this.numberOfCrossovers == cross.numberOfCrossovers;
        } else {
            return false;
        }
    }

    /**
     * This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.optimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GI var. length N-Point Crossover";
    }

    /**
     * This method allows you to set the number of crossovers that occur in the
     * genotype.
     *
     * @param crossovers The number of crossovers.
     */
    public void setNumberOfCrossovers(int crossovers) {
        if (crossovers < 0) {
            crossovers = 0;
        }
        this.numberOfCrossovers = crossovers;
    }

    public int getNumberOfCrossovers() {
        return this.numberOfCrossovers;
    }

    public String numberOfCrossoversTipText() {
        return "The number of crossoverpoints.";
    }
}