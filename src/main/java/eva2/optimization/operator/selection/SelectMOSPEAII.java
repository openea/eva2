package eva2.optimization.operator.selection;

import eva2.gui.plot.GraphPointSet;
import eva2.gui.plot.Plot;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.archiving.ArchivingSPEAII;
import eva2.optimization.population.Population;
import eva2.tools.chart2d.DPoint;
import eva2.tools.chart2d.DPointIconCircle;
import eva2.tools.chart2d.DPointIconText;
import eva2.util.annotation.Description;

/**
 * The SPEA II selection criteria using strength and raw fitness to determine
 * good individuals.
 */
@Description("This selection method calucates the strength and selects using the strength.")
public class SelectMOSPEAII implements InterfaceSelection, java.io.Serializable {

    private InterfaceSelection environmentSelection = new SelectTournament();
    private ArchivingSPEAII SPEAII = new ArchivingSPEAII();
    private double[] SPEAFitness;
    private boolean obeyDebsConstViolationPrinciple = true;

    public SelectMOSPEAII() {
    }

    public SelectMOSPEAII(SelectMOSPEAII a) {
        this.SPEAII = new ArchivingSPEAII();
        this.environmentSelection = (InterfaceSelection) a.environmentSelection.clone();
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new SelectMOSPEAII(this);
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
        SPEAFitness = this.SPEAII.calculateSPEA(population);
    }

    /**
     * This method will select one Individual from the given
     * Population in respect to the selection propability of the
     * individual.
     *
     * @param population The source population where to select from
     * @param size       The number of Individuals to select
     * @return The selected population.
     */
    @Override
    public Population selectFrom(Population population, int size) {
        // first replace the fitness with the SPEA strength
        double[][] orgFit = new double[population.size()][];
        double[] newFit = new double[1];
        for (int i = 0; i < population.size(); i++) {
            orgFit[i] = population.get(i).getFitness();
            newFit = new double[1];
            newFit[0] = this.SPEAFitness[i];
            population.get(i).setFitness(newFit);
        }

        // then select
        Population result = this.environmentSelection.selectFrom(population, size);

        // finally replace the fitness with the original
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setFitness(orgFit[i]);
        }

        if (false) {
            // debug
            double[] tmpD = new double[2];
            tmpD[0] = 0;
            tmpD[1] = 0;
            Plot plot = new Plot("Debug SPEAIISelect", "Y1", "Y2", tmpD, tmpD);
            plot.setUnconnectedPoint(0, 0, 11);
            plot.setUnconnectedPoint(1.2, 10, 11);
            GraphPointSet mySet = new GraphPointSet(10, plot.getFunctionArea());
            DPoint myPoint;
            double tmp1, tmp2;
            DPointIconText tmp;

            mySet.setConnectedMode(false);
            for (int i = 0; i < orgFit.length; i++) {
                myPoint = new DPoint(orgFit[i][0], orgFit[i][1]);
                tmp1 = Math.round(SPEAFitness[i] * 100) / (double) 100;
                tmp = new DPointIconText("" + tmp1);
                tmp.setIcon(new DPointIconCircle());
                myPoint.setIcon(tmp);
                mySet.addDPoint(myPoint);
            }

            // Now plot the selection
            for (int i = 0; i < result.size(); i++) {
                tmpD = result.get(i).getFitness();
                plot.setUnconnectedPoint(tmpD[0], tmpD[1], 11);
            }
        }

        return result;
    }

    /**
     * This method allows you to select partners for a given Individual
     *
     * @param dad              The already seleceted parent
     * @param availablePartners The mating pool.
     * @param size             The number of partners needed.
     * @return The selected partners.
     */
    @Override
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
        return this.selectFrom(availablePartners, size);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "MO SPEAII selection";
    }

    /**
     * This method will set the selection method that is to be used
     *
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        this.environmentSelection = selection;
    }

    public InterfaceSelection getEnvironmentSelection() {
        return this.environmentSelection;
    }

    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }

    /**
     * Toggle the use of obeying the constraint violation principle
     * of Deb
     *
     * @param b The new state
     */
    @Override
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.obeyDebsConstViolationPrinciple = b;
    }

    public boolean getObeyDebsConstViolationPrinciple() {
        return this.obeyDebsConstViolationPrinciple;
    }

    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
}