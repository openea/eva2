package eva2.optimization.operator.selection;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.ObjectiveSpaceMetric;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * An experimental implementation for mating restriction.
 * Possibly defunct.
 */
@Description("This selection will select n mates from all individuals within the mating distance (extends Tournament Selection)." +
        "This is a single objective selecting method, it will select in respect to a random criterion.")
public class SelectHomologousMate extends SelectTournament implements java.io.Serializable {

    private double matingRadius = 0.1;
    private InterfaceDistanceMetric metric = new ObjectiveSpaceMetric();

    public SelectHomologousMate() {
    }

    public SelectHomologousMate(SelectHomologousMate a) {
        this.matingRadius = a.matingRadius;
        this.metric = (InterfaceDistanceMetric) a.metric.clone();
    }

    @Override
    public Object clone() {
        return new SelectHomologousMate(this);
    }

    /**
     * This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     *
     * @param population The population that is to be processed.
     */
    @Override
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad               The already seleceted parent
     * @param availablePartners The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        Population possibleMates = new Population();

        // first select all possible partners for daddy
        // to be honest daddy himself is not omitted....
        for (int i = 0; i < availablePartners.size(); i++) {
            if (this.metric.distance(dad, availablePartners.get(i)) < this.matingRadius) {
                possibleMates.add(availablePartners.get(i));
            }
        }
        //System.out.println("Partners Size: " + possibleMates.size());
        if (possibleMates.size() <= 1) {
            return this.selectFrom(availablePartners, size);
        } else {
            return this.selectFrom(possibleMates, size);
        }
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Homologous Mating Selection";
    }

    /**
     * This method allows you to set/get the mating radius.
     *
     * @return The current optimizing method
     */
    public double getMatingRadius() {
        return this.matingRadius;
    }

    public void setMatingRadius(double b) {
        this.matingRadius = b;
    }

    public String matingRadiusTipText() {
        return "Choose the mating radius.";
    }

    /**
     * These methods allows you to set/get the type of Distance Metric.
     *
     * @param Metric
     */
    public void setMetric(InterfaceDistanceMetric Metric) {
        this.metric = Metric;
    }

    public InterfaceDistanceMetric getMetric() {
        return this.metric;
    }

    public String metricTipText() {
        return "The distance metric used. Note: This depends on the type of EAIndividual used!";
    }
}