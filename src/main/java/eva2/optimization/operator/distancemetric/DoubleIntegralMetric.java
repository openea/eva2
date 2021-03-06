package eva2.optimization.operator.distancemetric;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.tools.EVAERROR;
import eva2.tools.math.Mathematics;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * Calculate the euclidean difference between discrete integrals, which is
 * d(x,y)=sqrt(sum_i=1^n(((sum_j=1^i(x_j))-(sum_j=1^i(y_j)))^2)).
 * <p>
 * This can be used with any individual type that implements getDoublePositionShallow.
 *
 * @see AbstractEAIndividual#getDoublePositionShallow(AbstractEAIndividual)
 */
@Description("A simple integral metric for real-valued types.")
public class DoubleIntegralMetric implements InterfaceDistanceMetric, Serializable {
    boolean oneNormed = true; // if true, the vectors are normed to unity sum before comparison.

    public DoubleIntegralMetric() {}

    public DoubleIntegralMetric(boolean normed) {
        oneNormed = normed;
    }

    @Override
    public Object clone() {
        return new DoubleIntegralMetric(oneNormed);
    }

    @Override
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double[] dIndy1 = null, dIndy2 = null;

        dIndy1 = AbstractEAIndividual.getDoublePositionShallow(indy1);
        dIndy2 = AbstractEAIndividual.getDoublePositionShallow(indy2);

        if (oneNormed) {
            double l1 = Mathematics.sum(dIndy1);
            double l2 = Mathematics.sum(dIndy2);
            if (l1 != 1. || l2 != 1.) { // norming really necessary
                if (l1 == 0 || l2 == 0) {
                    EVAERROR.errorMsgOnce("Warning, double vector with zero sum will yield infinite distances in " + this.getClass());
                }
                dIndy1 = Mathematics.svDiv(l1, dIndy1);
                dIndy2 = Mathematics.svDiv(l2, dIndy2);
            }
        }

        double sum1 = 0, sum2 = 0;
        double tmpDiff, sqrDiffSum = 0;

        for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
            sum1 += dIndy1[i];
            sum2 += dIndy2[i];
            tmpDiff = (sum1 - sum2);
            sqrDiffSum += (tmpDiff * tmpDiff);
        }
        return Math.sqrt(sqrDiffSum);
    }

    public String getName() {
        return "Real-valued integral metric";
    }

    public boolean isOneNormed() {
        return oneNormed;
    }

    @Parameter(description = "If true, both vectors are normed to unity sum before comparison.")
    public void setOneNormed(boolean normedByLength) {
        this.oneNormed = normedByLength;
    }
}
