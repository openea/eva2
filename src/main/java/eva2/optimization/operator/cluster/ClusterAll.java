package eva2.optimization.operator.cluster;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Dummy class which assigns all individuals to a single cluster only.
 */
@Description("A dummy clustering implementation which assigns all elements to a single cluster.")
public class ClusterAll implements InterfaceClustering, Serializable {

    private boolean assignLoners = false; // should loners be assigned?

    @Override
    public Object clone() {
        return new ClusterAll();
    }

    /**
     * Try to associate a set of loners with a given set of species. Return a list
     * of indices assigning loner i with species j for all loners. If no species can
     * be associated, -1 is returned as individual entry.
     * Note that the last cluster threshold is used which may have depended on the last
     * generation.
     * If the clustering depends on population measures, a reference set may be given
     * which is the reference population to consider the measures of. This is for cases
     * where, e.g., subsets of a Population are to be clustered using measures of the
     * original population.
     *
     * @param loners
     * @param species
     * @param referenceSet a reference population for dynamic measures
     * @return associative list matching loners to species.
     */
    @Override
    public int[] associateLoners(Population loners, Population[] species,
                                 Population referenceSet) {
        if (loners != null && (loners.size() > 0)) {
            int[] indices = new int[loners.size()];
            if (assignLoners) {
                Arrays.fill(indices, 0);
            } else {
                Arrays.fill(indices, -1);
            }
            return indices;
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.operators.cluster.InterfaceClustering#cluster(eva2.optimization.populations.Population, eva2.optimization.populations.Population)
     */
    @Override
    public Population[] cluster(Population pop, Population referenceSet) {
        // first pop is empty (there are no loners), second pop is complete
        return new Population[]{pop.cloneWithoutInds(), pop.cloneShallowInds()};
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.operators.cluster.InterfaceClustering#initClustering(eva2.optimization.populations.Population)
     */
    @Override
    public String initClustering(Population pop) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see eva2.optimization.operators.cluster.InterfaceClustering#mergingSpecies(eva2.optimization.populations.Population, eva2.optimization.populations.Population, eva2.optimization.populations.Population)
     */
    @Override
    public boolean mergingSpecies(Population species1, Population species2,
                                  Population referenceSet) {
        return true;
    }

    public String getName() {
        return "Cluster-all";
    }

}
