package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This crossover-Method performs an \"intersection\" of the selected Individuals
 * It only mates 2 Individuals, not more
 */
@Description("This is a Crossover Method for Binary Individuals which just forms the \"intersection\" of the individuals")
public class CM2 implements InterfaceCrossover, java.io.Serializable {

    private InterfaceOptimizationProblem optimizationProblem;

    public CM2() {

    }

    public CM2(CM2 c) {
        this.optimizationProblem = c.optimizationProblem;
    }

    @Override
    public Object clone() {
        return new CM2(this);
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
                                       Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[1];
        if (indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary) {
            BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            BitSet data2 = ((InterfaceDataTypeBinary) partners.getIndividual(0)).getBinaryData();
            for (int j = 0; j < data2.length(); j++) {
                if (data2.get(j)) {
                    data.set(j, true);
                }
            }
            for (int i = 0; i < data.size(); i++) {
                if (RNG.flipCoin(0.5)) {
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

    public String getName() {
        return "Combination Method 2";
    }
}
