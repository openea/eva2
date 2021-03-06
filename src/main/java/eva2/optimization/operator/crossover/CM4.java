package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This crossover-Method performs an \"intersection\" of the selected Individuals and then tries to improve it through score (like in CM3)
 * It only mates 2 Individuals, not more
 */
@Description("Intersection with weight driven improvement")
public class CM4 implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem optimizationProblem;

    public CM4() {

    }

    public CM4(CM4 c) {
        this.optimizationProblem = c.optimizationProblem;
    }

    @Override
    public Object clone() {
        return new CM4(this);
    }

    private int convertBoolean(boolean b) {
        if (b) {
            return 1;
        } else {
            return 0;
        }
    }

    private double weight(double valX, boolean xi, double valY, boolean yi) {
        double result = valX * convertBoolean(xi) + valY * convertBoolean(yi);
        result /= (valX + valY);
        return result;
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
                                       Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[1];
        if (indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary) {
            BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            BitSet data2 = ((InterfaceDataTypeBinary) partners.getEAIndividual(0)).getBinaryData();
            for (int i = 0; i < data.size(); i++) {
                boolean setBit = data.get(i);
                setBit = setBit && data2.get(i);
                double val = partners.getEAIndividual(0).getFitness(0);
                double w = weight(indy1.getFitness(0), data.get(i), val, setBit);
                data.set(i, setBit);
                if (!setBit && RNG.flipCoin(w)) {
                    data.set(i, true);
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
        return this.getName();
    }

    public String getName() {
        return "Combination Method 4";
    }
}
