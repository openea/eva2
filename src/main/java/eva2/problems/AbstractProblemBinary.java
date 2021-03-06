package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;

import java.util.BitSet;

/**
 * An abstract problem based on binary data.
 */
public abstract class AbstractProblemBinary extends AbstractOptimizationProblem {

    public AbstractProblemBinary() {
        initTemplate();
    }

    protected void initTemplate() {
        if (template == null) {
            this.template = new GAIndividualBinaryData();
        }
        if (((InterfaceGAIndividual) this.template).getGenotypeLength() != this.getProblemDimension()) {
            ((InterfaceDataTypeBinary) this.template).setBinaryDataLength(this.getProblemDimension());
        }
    }

    public void cloneObjects(AbstractProblemBinary o) {
        if (o.template != null) {
            template = (AbstractEAIndividual) o.template.clone();
        }
    }

    @Override
    public void evaluate(AbstractEAIndividual individual) {
        BitSet tmpBitSet;
        double[] result;

        tmpBitSet = ((InterfaceDataTypeBinary) individual).getBinaryData();
        // evaluate the fitness
        result = evaluate(tmpBitSet);
        // set the fitness
        individual.setFitness(result);
    }

    /**
     * Evaluate a BitSet representing a possible solution. This is the target
     * function implementation.
     *
     * @param bs a BitSet representing a possible solution
     * @return Fitness
     */
    public abstract double[] evaluate(BitSet bs);

    @Override
    public void initializePopulation(Population population) {
        ((InterfaceDataTypeBinary) this.template).setBinaryDataLength(this.getProblemDimension());
        AbstractOptimizationProblem.defaultInitializePopulation(population, template, this);
    }

    @Override
    public void initializeProblem() {
        initTemplate();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "AbstractProblemBinary";
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("A binary valued problem:\n");
        sb.append("Dimension   : ");
        sb.append(this.getProblemDimension());
        return sb.toString();
    }
}