package eva2.optimization.operator.moso;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 */
@Description("This method calculates the MOGA rank of each individual and uses the rank as fitness [Fonseca93Genetic].")
public class MOSOMOGARankBased implements InterfaceMOSOConverter, java.io.Serializable {

    public MOSOMOGARankBased() {
    }

    public MOSOMOGARankBased(MOSOMOGARankBased b) {
    }

    @Override
    public Object clone() {
        return new MOSOMOGARankBased(this);
    }

    /**
     * This method takes a population of individuals with an array of
     * fitness values and calculates a single fitness value to replace
     * the former fitness array. Please note: The orignal fitness values
     * are lost this way, so please use the individual.setData() method
     * if you still want to access the original fitness values.
     *
     * @param pop The population to process.
     */
    @Override
    public void convertMultiObjective2SingleObjective(Population pop) {
        int[] MOGARank = new int[pop.size()];
        for (int i = 0; i < MOGARank.length; i++) {
            MOGARank[i] = 1;
        }
        for (int i = 0; i < pop.size() - 1; i++) {
            for (int j = 0; j < pop.size(); j++) {
                if (i != j) {
                    if (pop.get(j).isDominatingDebConstraints(pop.get(i))) {
                        MOGARank[i] += 1;
                    }
                }
            }
        }
        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).putData("MOGARank", MOGARank[i]);
        }
        for (int i = 0; i < pop.size(); i++) {
            this.convertSingleIndividual(pop.get(i));
        }
    }

    /**
     * This method processes a single individual
     *
     * @param indy The individual to process.
     */
    @Override
    public void convertSingleIndividual(AbstractEAIndividual indy) {
        double[] resultFit = new double[1];
        double[] tmpFit;

        tmpFit = indy.getFitness();
        indy.putData("MOFitness", tmpFit);
        resultFit[0] = ((Integer) indy.getData("MOGARank")).doubleValue();
        indy.setFitness(resultFit);
    }

    /**
     * This method allows the problem to set the current output size of
     * the optimization problem. Additional weights will be set to a default
     * value of one
     *
     * @param dim Outputdimension of the problem
     */
    @Override
    public void setOutputDimension(int dim) {

    }

    /**
     * This method returns a description of the objective
     *
     * @return A String
     */
    @Override
    public String getStringRepresentation() {
        return this.getName() + "\n";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "MOGA Rank Based";
    }
}