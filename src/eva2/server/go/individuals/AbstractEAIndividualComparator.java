package eva2.server.go.individuals;

import java.util.Comparator;

public class AbstractEAIndividualComparator implements Comparator<Object> {

	// flag whether a data field should be used.
	String indyDataKey = null;
	
	/**
	 * Comparator implementation which compares two individuals based on their fitness.
	 * The default version calls isDominatingDebConstraints() of the AbstractEAIndividual
	 * class and assigns -1 if first is dominant, 1 if second is dominant, 0 if the two ind.s
	 * are not comparable.
	 *
	 */
	public AbstractEAIndividualComparator() {
		indyDataKey = null;
	}
	
	/**
	 * Constructor with data key. A data field of the individuals may be used to retrieve
	 * the double array used for comparison. Both individuals must have a data field with
	 * the given key and return a double array of the same dimension. No constraints are 
	 * regarded for this comparison.
	 * If indyDataKey is null, the default comparison is used.
	 * 
	 * @param indyDataKey
	 */
	public AbstractEAIndividualComparator(String indyDataKey) {
		this.indyDataKey = indyDataKey;		
	}
	
	/**
	 * Compare two individuals, return -1 if first is dominant, 1 if second is dominant, 0 if they
	 * are not comparable.
	 */
	public int compare(Object o1, Object o2) {
		boolean o1domO2, o2domO1;
		
		if (indyDataKey != null) {
			double[] fit1 = (double[])((AbstractEAIndividual)o1).getData(indyDataKey);
			double[] fit2 = (double[])((AbstractEAIndividual)o2).getData(indyDataKey);
			o1domO2 = AbstractEAIndividual.isDominatingFitness(fit1, fit2);
			o2domO1 = AbstractEAIndividual.isDominatingFitness(fit2, fit1);
		} else {
			o1domO2 = ((AbstractEAIndividual) o1).isDominatingDebConstraints((AbstractEAIndividual) o2);
			o2domO1 = ((AbstractEAIndividual) o2).isDominatingDebConstraints((AbstractEAIndividual) o1);
		}
		if (o1domO2 ^ o2domO1) return (o1domO2 ? -1 : 1);
		else return 0; // these are not comparable
	}
}