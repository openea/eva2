package eva2.optimization.operator.selection.probability;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * A simple sum with a scaling factor.
 */
@Description("This is a standard normation method with scaling.")
public class SelectionProbabilityStandardScaling extends AbstractSelectionProbability implements java.io.Serializable {

    private double Q = 0;

    public SelectionProbabilityStandardScaling() {
    }

    public SelectionProbabilityStandardScaling(double q) {
        Q = q;
    }

    public SelectionProbabilityStandardScaling(SelectionProbabilityStandardScaling a) {
        this.Q = a.Q;
    }

    @Override
    public Object clone() {
        return new SelectionProbabilityStandardScaling(this);
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
        double sum = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, delta;
        double[] result = new double[data.length];

        if (obeyConst) {
            // first check if anyone holds the constraints
            boolean isFeasible = false;
            int k = 0;
            while ((k < population.size()) && !isFeasible) {
                if (!population.get(k).violatesConstraint()) {
                    isFeasible = true;
                }
                k++;
            }
            if (isFeasible) {
                // at least one is feasible
                // iterating over the fitness cases
                for (int x = 0; x < data[0].length; x++) {
                    sum = 0;
                    min = Double.POSITIVE_INFINITY;
                    // first find the worst, to be able to default
                    double worst = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i][x] > worst) {
                            worst = data[i][x];
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (!population.get(i).violatesConstraint()) {
                            result[i] = -data[i][x];
                        } else {
                            result[i] = -worst;
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (result[i] < min) {
                            min = result[i];
                        }
                        if (result[i] > max) {
                            max = result[i];
                        }
                    }
                    if (max != min) {
                        delta = max - min;
                    } else {
                        delta = 1;
                    }

                    for (int i = 0; i < data.length; i++) {
                        result[i] = ((result[i] - min) / delta) + this.Q;
                        sum += result[i];
                    }

                    for (int i = 0; i < population.size(); i++) {
                        population.get(i).setSelectionProbability(x, result[i] / sum);
                    }
                }
            } else {
                // not one is feasible therefore select the best regarding feasibility
                sum = 0;
                min = Double.POSITIVE_INFINITY;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -population.get(i).getConstraintViolation();
                }
                for (int i = 0; i < data.length; i++) {
                    if (result[i] < min) {
                        min = result[i];
                    }
                    if (result[i] > max) {
                        max = result[i];
                    }
                }
                if (max != min) {
                    delta = max - min;
                } else {
                    delta = 1;
                }

                for (int i = 0; i < data.length; i++) {
                    result[i] = ((result[i] - min) / delta) + this.Q;
                    sum += result[i];
                }
                for (int i = 0; i < population.size(); i++) {
                    double[] tmpD = new double[1];
                    tmpD[0] = result[i] / sum;
                    population.get(i).setSelectionProbability(tmpD);
                }
            }
        } else {
            for (int x = 0; x < data[0].length; x++) {
                sum = 0;
                min = Double.POSITIVE_INFINITY;
                for (int i = 0; i < data.length; i++) {
                    result[i] = -data[i][x];
                }
                for (int i = 0; i < data.length; i++) {
                    if (result[i] < min) {
                        min = result[i];
                    }
                    if (result[i] > max) {
                        max = result[i];
                    }
                }
                if (max != min) {
                    delta = max - min;
                } else {
                    delta = 1;
                }

                for (int i = 0; i < data.length; i++) {
                    result[i] = ((result[i] - min) / delta) + this.Q;
                    sum += result[i];
                }

                for (int i = 0; i < population.size(); i++) {
                    population.get(i).setSelectionProbability(x, result[i] / sum);
                }
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Scaled Normation";
    }

    /**
     * This method will allow you to set and get the Q Parameter
     *
     * @return The new selection pressure q.
     */
    public double getQ() {
        return this.Q;
    }

    public void setQ(double b) {
        this.Q = Math.abs(b);
    }

    public String qTipText() {
        return "The selection pressure. The bigger q, the higher the selection pressure.";
    }
}