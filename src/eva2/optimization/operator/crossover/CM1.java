package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.BinaryScatterSearch;
import eva2.tools.math.RNG;

import java.util.BitSet;

/**
 * This crossover-Method performs a \"union\" of the selected Individuals
 * It only mates 2 Individuals, not more
 *
 * @author Alex
 */
public class CM1 implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;

    public CM1() {
    }

    public CM1(CM1 c) {
        this.optimizationProblem = c.optimizationProblem;
    }

    @Override
    public Object clone() {
        return new CM1(this);
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
                                       Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[1];
        if (indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary) {
            BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            BitSet data2 = ((InterfaceDataTypeBinary) partners.getIndividual(0)).getBinaryData();
            for (int j = 0; j < data2.size(); j++) {
                if (data2.get(j)) {
                    data.set(j, true);
                }
            }
            partners.remove(0);
            for (int i = 0; i < data.size(); i++) {
                if (RNG.flipCoin(Math.max(0, 1 - BinaryScatterSearch.score(i, partners))) && data.get(i)) {
                    data.set(i, false);
                }
            }
            ((InterfaceDataTypeBinary) indy1).setBinaryGenotype(data);
        }
        result[0] = indy1;
        return result;
    }

    @Override
    public void init(AbstractEAIndividual individual,
                     InterfaceOptimizationProblem opt) {
        this.optimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return getName();
    }

    /**
     * **************************************************
     * GUI
     */

    public String getName() {
        return "Combination Method 1";
    }

    public static String globalInfo() {
        return "This is a Crossover Method for Binary Individuals which just forms the \"union\" of the individuals";
    }

}
