package eva2.server.go.operators.crossover;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 12:45:06
 * To change this template use Options | File Templates.
 */
public class CrossoverGANPoint implements InterfaceCrossover, java.io.Serializable {
    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private int                             m_NumberOfCrossovers = 3;

    public CrossoverGANPoint() {

    }
    public CrossoverGANPoint(CrossoverGANPoint mutator) {
        this.m_OptimizationProblem    = mutator.m_OptimizationProblem;
        this.m_NumberOfCrossovers     = mutator.m_NumberOfCrossovers;
    }

    /** This method will enable you to clone a given crossover operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverGANPoint(this);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size()+1];
        result[0] = (AbstractEAIndividual) (indy1).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }
        if (partners.size() == 0) return result;
        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
        if ((indy1 instanceof InterfaceGAIndividual) && (partners.get(0) instanceof InterfaceGAIndividual)) {
            int         length          =  ((InterfaceGAIndividual)indy1).getGenotypeLength();
            int         mixer           = RNG.randomInt(0, partners.size());
            int[]       crossoverPoints = new int[this.m_NumberOfCrossovers];
            BitSet[][]  tmpBitSet       = new BitSet[2][partners.size()+1];

            tmpBitSet[0][0]     = ((InterfaceGAIndividual)indy1).getBGenotype();
            tmpBitSet[1][0]     = ((InterfaceGAIndividual)result[0]).getBGenotype();
            for (int i = 0; i < partners.size(); i++) {
                tmpBitSet[0][i+1] = ((InterfaceGAIndividual)partners.get(i)).getBGenotype();
                tmpBitSet[1][i+1] = ((InterfaceGAIndividual)result[i+1]).getBGenotype();
                length = Math.max(length, ((InterfaceGAIndividual)partners.get(i)).getGenotypeLength());
            }

            for (int i = 0; i < this.m_NumberOfCrossovers; i++) {
                crossoverPoints[i] = RNG.randomInt(0, length-1);
                //System.out.println("crpoint: "+crossoverPoints[i]);
            }
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < this.m_NumberOfCrossovers; j++) {
                    if (i == crossoverPoints[j]) mixer++;
                }
                for (int j = 0; j < tmpBitSet[0].length; j++) {
                    //if ((mixer % tmpBitSet[0].length) != 0) {
                        //System.out.println(""+((j + mixer) % tmpBitSet[0].length)+ " - " + (j + mixer) +" - "+(tmpBitSet[0].length));
                        if (tmpBitSet[0][(j + mixer) % tmpBitSet[0].length].get(i)) tmpBitSet[1][j].set(i);
                        else tmpBitSet[1][j].clear(i);
                    //}
                }
            }

            for (int i = 0; i < result.length; i++) ((InterfaceGAIndividual)result[i]).SetBGenotype(tmpBitSet[1][i]);
        }
        //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
        return result;
    }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverGANPoint) {
            CrossoverGANPoint cross = (CrossoverGANPoint)crossover;
            if (this.m_NumberOfCrossovers != cross.m_NumberOfCrossovers) return false;
            return true;
        } else return false;
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        this.m_OptimizationProblem = opt;
    }

    public String getStringRepresentation() {
        return this.getName();
    }    

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA N-Point Crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a n-point crossover between m individuals.";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param crossovers   The number of crossovers.
     */
    public void setNumberOfCrossovers(int crossovers) {
        if (crossovers < 0) crossovers = 0;
        this.m_NumberOfCrossovers = crossovers;
    }
    public int getNumberOfCrossovers() {
        return this.m_NumberOfCrossovers;
    }
    public String numberOfCrossoversTipText() {
        return "The number of crossoverpoints.";
    }
}