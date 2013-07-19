package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.gp.InterfaceProgram;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/** This individual combines a real-valued phenotype with a tree-based phenotype.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.04.2003
 * Time: 10:04:44
 * To change this template use Options | File Templates.
 */
public class GAPIndividualProgramData extends AbstractEAIndividual implements InterfaceDataTypeProgram, InterfaceDataTypeDouble, java.io.Serializable {
    
    private InterfaceDataTypeDouble     m_Numbers   = new ESIndividualDoubleData();
    private InterfaceDataTypeProgram    m_Program   = new GPIndividualProgramData();

    public GAPIndividualProgramData() {
        this.mutationProbability = 1.0;
        this.crossoverProbability = 1.0;
        this.m_Numbers      = new GAIndividualDoubleData();
        this.m_Program      = new GPIndividualProgramData();
    }

    public GAPIndividualProgramData(GAPIndividualProgramData individual) {
        this.m_Numbers      = (InterfaceDataTypeDouble)((AbstractEAIndividual)individual.getNumbers()).clone();
        this.m_Program      = (InterfaceDataTypeProgram)((AbstractEAIndividual)individual.getProgramRepresentation()).clone();

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation)individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        for (int i = 0; i < this.selectionProbability.length; i++) {
            this.selectionProbability[i] = individual.selectionProbability[i];
        }
        this.fitness = new double[individual.fitness.length];
        for (int i = 0; i < this.fitness.length; i++) {
            this.fitness[i] = individual.fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    @Override
    public Object clone() {
        return (Object) new GAPIndividualProgramData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAPIndividualProgramData) {
            GAPIndividualProgramData indy = (GAPIndividualProgramData)individual;
            if (!((AbstractEAIndividual)this.m_Numbers).equalGenotypes((AbstractEAIndividual)indy.m_Numbers)) {
                return false;
            }
            if (!((AbstractEAIndividual)this.m_Program).equalGenotypes((AbstractEAIndividual)indy.m_Program)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    @Override
    public void init(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual)this.m_Numbers).init(opt);
        ((AbstractEAIndividual)this.m_Program).init(opt);
    }
    
    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        ((AbstractEAIndividual)this.m_Numbers).defaultInit(prob);
        ((AbstractEAIndividual)this.m_Program).defaultInit(prob);   	
    }
    
    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof Object[]) {
            if (((Object[])obj)[0] instanceof double[]) {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[0], opt);
                ((AbstractEAIndividual)this.m_Program).initByValue(((Object[])obj)[1], opt);
            } else {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[1], opt);
                ((AbstractEAIndividual)this.m_Program).initByValue(((Object[])obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual)this.m_Numbers).init(opt);
            ((AbstractEAIndividual)this.m_Program).init(opt);
            System.out.println("Initial value for GAPIndividualDoubleData is not suitable!");
        }
    }

    /** This method will mutate the individual randomly
     */
    @Override
    public void mutate() {
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual)this.m_Numbers).mutate();
        }
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual)this.m_Program).mutate();
        }
    }

    @Override
    public void defaultMutate() {
        ((AbstractEAIndividual)this.m_Numbers).defaultMutate();
        ((AbstractEAIndividual)this.m_Program).defaultMutate();  	
    }
    
    /** This method will mate the Individual with given other individuals
     * of the same type.
     * @param partners  The possible partners
     * @return offsprings
     */
    @Override
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RNG.flipCoin(this.crossoverProbability)) {
            AbstractEAIndividual[]  resNum, resBin;
            AbstractEAIndividual    numTmp, binTmp;
            Population              numPop, binPop;

            numTmp = (AbstractEAIndividual)this.getNumbers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                numPop.add(((GAPIndividualProgramData)partners.get(i)).getNumbers());
            }
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual)this.getProgramRepresentation();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                binPop.add(((GAPIndividualProgramData)partners.get(i)).getProgramRepresentation());
            }
            resBin = binTmp.mateWith(binPop);

            result = new GAPIndividualProgramData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GAPIndividualProgramData(this);
                ((GAPIndividualProgramData)result[i]).setNumbers((InterfaceDataTypeDouble)resNum[i]);
                ((GAPIndividualProgramData)result[i]).setProgramRepresentation((InterfaceDataTypeProgram)resBin[i]);
            }
        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() +1];
            result[0] = (AbstractEAIndividual)this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i].giveNewName();
        }
        return result;         
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "This is a hybrid Individual:\n";
        result += "The Numbers Part:\n"+((AbstractEAIndividual)this.m_Numbers).getStringRepresentation();
        result += "\nThe Binarys Part:\n"+((AbstractEAIndividual)this.m_Program).getStringRepresentation();
        return result;
    }

/**********************************************************************************************************************
 * These are for InterfaceDataTypeDouble

    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    @Override
    public void setDoubleDataLength (int length) {
        this.m_Numbers.setDoubleDataLength(length);
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.m_Numbers.size();
    }

    /** This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     * @param range     The new range for the double data.
     */
    @Override
    public void SetDoubleRange(double[][] range) {
        this.m_Numbers.SetDoubleRange(range);
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.m_Numbers.getDoubleRange();
    }

    /** This method allows you to read the double data
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        return this.m_Numbers.getDoubleData();
    }

    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        return this.m_Numbers.getDoubleDataWithoutUpdate();
    }

    /** This method allows you to set the phenotype data. To change the genotype, use
     * SetDoubleDataLamarckian().
     * @param doubleData    The new double data.
     */
    @Override
    public void SetDoublePhenotype(double[] doubleData) {
        this.m_Numbers.SetDoublePhenotype(doubleData);
    }

    /** This method allows you to set the genotype data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    @Override
    public void SetDoubleGenotype(double[] doubleData) {
        this.m_Numbers.SetDoubleGenotype(doubleData);
    }

/************************************************************************************
 * InterfaceDataTypeProgram methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    @Override
    public void setProgramDataLength (int length) {
        this.m_Program.setProgramDataLength(length);
    }

    /** This method allows you to read the program stored as Koza style node tree
     * @return AbstractGPNode representing the binary data.
     */
    @Override
    public InterfaceProgram[] getProgramData() {
        return this.m_Program.getProgramData();
    }

    /** This method allows you to read the Program data without
     * an update from the genotype
     * @return InterfaceProgram[] representing the Program.
     */
    @Override
    public InterfaceProgram[] getProgramDataWithoutUpdate() {
        return this.m_Program.getProgramDataWithoutUpdate();
    }

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    @Override
    public void SetProgramPhenotype(InterfaceProgram[] program) {
        this.m_Program.SetProgramPhenotype(program);
    }

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    @Override
    public void SetProgramGenotype(InterfaceProgram[] program) {
        this.m_Program.SetProgramGenotype(program);
    }

    /** This method allows you to set the function area
     * @param area  The area contains functions and terminals
     */
    @Override
    public void SetFunctionArea(Object[] area) {
        this.m_Program.SetFunctionArea(area);
    }

    /** This method allows you to set the function area
     * @return The function area
     */
    @Override
    public Object[] getFunctionArea() {
        return this.m_Program.getFunctionArea();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "GAP individual";
    }

    /** This method will allow you to set the inner constants
     * @param Numbers     The new representation for the inner constants.
      */
    public void setNumbers(InterfaceDataTypeDouble Numbers) {
        this.m_Numbers = Numbers;
    }
    public InterfaceDataTypeDouble getNumbers() {
        return this.m_Numbers;
    }
    public String numbersTipText() {
        return "Choose the type of inner constants to use.";
    }
    /** This method will allow you to set the inner constants
     * @param program     The new representation for the program.
      */
    public void setProgramRepresentation(InterfaceDataTypeProgram program) {
        this.m_Program = program;
    }
    public InterfaceDataTypeProgram getProgramRepresentation() {
        return this.m_Program;
    }
    public String programRepresentationTipText() {
        return "Choose the type of inner constants to use.";
    }
}

