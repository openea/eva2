package eva2.optimization.individuals;

import eva2.tools.EVAERROR;
import eva2.util.annotation.Parameter;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare two individuals based on a linear combination of the fitness values.
 *
 * @author mkron
 */
public class IndividualWeightedFitnessComparator implements Comparator<AbstractEAIndividual>, Serializable {
    /**
     * Generated serial version identifier
     */
    private static final long serialVersionUID = 3182129129041083881L;
    /**
     *
     */
    private double[] fitWeights = null;

    /**
     * @param weights
     */
    public IndividualWeightedFitnessComparator(double[] weights) {
        setFitWeights(weights);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IndividualWeightedFitnessComparator) {
            IndividualWeightedFitnessComparator o = (IndividualWeightedFitnessComparator) obj;
            if (fitWeights == null && (o.fitWeights == null)) {
                return true;
            }
            if (fitWeights == null || o.fitWeights == null) {
                return false;
            }
            // now both are non null:
            if (fitWeights.length == o.fitWeights.length) {
                for (int i = 0; i < fitWeights.length; i++) {
                    if (fitWeights[i] != o.fitWeights[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (fitWeights == null) {
            return super.hashCode();
        }
        int code = 0;
        for (int i = 0; i < fitWeights.length; i++) {
            code += (int) (fitWeights[i] * 10000) % (10000 * (i + 1));
        }
        return code;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(AbstractEAIndividual o1, AbstractEAIndividual o2) {
        double[] f1 = o1.getFitness();
        double[] f2 = o2.getFitness();

        double score1 = calculateScore(f1);
        double score2 = calculateScore(f2);

        if (score1 < score2) {
            return -1;
        } else if (score1 > score2) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * @param f
     * @return
     */
    private double calculateScore(double[] f) {
        if (f == null || fitWeights == null) {
            throw new RuntimeException("Error, missing information in " + this.getClass());
        }
        if (f.length != fitWeights.length) {
            if (f.length < fitWeights.length) {
                EVAERROR.errorMsgOnce("Warning, fitness vector has less dimensions than the weights... some weights are ignored, in " + this.getClass());
            } else {
                EVAERROR.errorMsgOnce("Warning, fitness vector has more dimensions than the weights... some fitness values are ignored, in " + this.getClass());
            }
        }
        double s = 0;
        for (int i = 0; i < Math.min(f.length, fitWeights.length); i++) {
            s += f[i] * fitWeights[i];
        }
        return s;
    }

    /**
     * @param indy
     * @return
     */
    public double calculateScore(AbstractEAIndividual indy) {
        double[] f = indy.getFitness();
        return calculateScore(f);
    }

    /**
     * @param dim
     * @param v
     */
    public void setAllWeights(int dim, double v) {
        fitWeights = new double[dim];
        for (int i = 0; i < fitWeights.length; i++) {
            fitWeights[i] = v;
        }
    }

    @Parameter(description = "Weights of the fitness values in the linear combination")
    public void setFitWeights(double[] fitWeights) {
        this.fitWeights = fitWeights;
    }

    public double[] getFitWeights() {
        return fitWeights;
    }
}
