package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 *
 */
@Description("This is an n-point crossover between m individuals.")
public class CrossoverGINPoint implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;
    private int numberOfCrossovers = 3;

    public CrossoverGINPoint() {

    }

    public CrossoverGINPoint(CrossoverGINPoint mutator) {
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
        return new CrossoverGINPoint(this);
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
            int length = ((InterfaceGIIndividual) indy1).getGenotypeLength();
            int mixer = RNG.randomInt(0, partners.size());
            int[] crossoverPoints = new int[this.numberOfCrossovers];
            int[][][] tmpInts = new int[2][partners.size() + 1][];

            tmpInts[0][0] = ((InterfaceGIIndividual) indy1).getIGenotype();
            tmpInts[1][0] = ((InterfaceGIIndividual) result[0]).getIGenotype();
            for (int i = 0; i < partners.size(); i++) {
                length = Math.max(length, ((InterfaceGIIndividual) partners.get(i)).getGenotypeLength());
                tmpInts[0][i + 1] = ((InterfaceGIIndividual) partners.get(i)).getIGenotype();
                tmpInts[1][i + 1] = ((InterfaceGIIndividual) result[i + 1]).getIGenotype();
                length = Math.max(length, ((InterfaceGIIndividual) partners.get(i)).getGenotypeLength());
            }

            for (int i = 0; i < this.numberOfCrossovers; i++) {
                crossoverPoints[i] = RNG.randomInt(0, length - 1);
                //System.out.println("crpoint: "+crossoverPoints[i]);
            }
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < this.numberOfCrossovers; j++) {
                    if (i == crossoverPoints[j]) {
                        mixer++;
                    }
                }
                for (int j = 0; j < tmpInts[0].length; j++) {
                    if ((tmpInts[0][(j + mixer) % tmpInts[0].length].length > i) &&
                            (tmpInts[1][j].length > i)) {
                        tmpInts[1][j][i] = tmpInts[0][(j + mixer) % tmpInts[0].length][i];
                    }
                }
            }

            for (int i = 0; i < result.length; i++) {
                ((InterfaceGIIndividual) result[i]).setIGenotype(tmpInts[1][i]);
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
     * This method allows you to evaluate wether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverGINPoint) {
            CrossoverGINPoint cross = (CrossoverGINPoint) crossover;
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
        return "GI N-Point Crossover";
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