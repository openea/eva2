package eva2.optimization.operator.selection.probability;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * Scale the fitness of a maximization problem by inverting it by the maximum fitness,
 * then normalize by fitness sum. This way, maximally bad individuals will receive a
 * selection probability of zero.
 *
 */
@Description("This is a standard normation method inverted by maximum fitness.")
public class SelectionProbabilityInvertByMax extends AbstractSelectionProbability {

    private double maxFit = 1.;

    public SelectionProbabilityInvertByMax() {
    }

    public SelectionProbabilityInvertByMax(double mF) {
        maxFit = mF;
    }

    public SelectionProbabilityInvertByMax(SelectionProbabilityInvertByMax a) {
        this.maxFit = a.maxFit;
    }

    @Override
    public Object clone() {
        return new SelectionProbabilityInvertByMax(this);
    }

    /**
     * This method computes the selection probability for each individual
     * in the population. Note: Summed over the complete population the selection
     * probability sums up to one.
     *
     * @param population The population to compute.
     * @param data       The input data as double[][].
     */
    @Override
    public void computeSelectionProbability(Population population, double[][] data, boolean obeyConst) {
        double sum = 0;
        double[] result = new double[data.length];
        boolean isFeasible = false;

        if (obeyConst) {
            // first check if anyone holds the constraints
            int k = 0;
            while ((k < population.size()) && !isFeasible) {
                if (!(population.getEAIndividual(k)).violatesConstraint()) {
                    isFeasible = true;
                }
                k++;
            }
        }
        if (isFeasible || !obeyConst) {
            // at least one is feasible
            // iterating over the fitness cases
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                // invert fitness
                for (int i = 0; i < data.length; i++) {
                    if (population.getEAIndividual(i).violatesConstraint()) {
                        result[i] = 0;
                    } else {
                        result[i] = maxFit - data[i][x];
                    }
                    sum += result[i];
                }

                for (int i = 0; i < population.size(); i++) {
                    population.get(i).setSelectionProbability(x, result[i] / sum);
                }
            }
        } else {
            // not one is feasible therefore select the best regarding feasibility
            System.err.println("warning, using standard probability for selection");
            sum = 0;
            // iterating over the individuals
            for (int i = 0; i < data.length; i++) {
                result[i] = Math.exp(-population.get(i).getConstraintViolation());
            }
            for (int i = 0; i < data.length; i++) {
                sum += result[i];
            }
            for (int i = 0; i < population.size(); i++) {
                double[] tmpD = new double[1];
                tmpD[0] = result[i] / sum;
                population.get(i).setSelectionProbability(tmpD);
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Inverted Maximum Fitness Normation";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public double getMaxFit() {
        return this.maxFit;
    }

    public void setMaxFit(double b) {
        this.maxFit = Math.abs(b);
    }

    public String maxFitTipText() {
        return "The maximum fitness value by which to invert";
    }
}
