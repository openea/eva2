package eva2.optimization.operator.crossover;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;


/**
 *
 */
@Description("This meta-mutation operator allows you to combine multiple alternative mutation operators.")
public class CrossoverEAMixer implements InterfaceCrossover, InterfaceEvaluatingCrossoverOperator, java.io.Serializable {
    public static final String CROSSOVER_EA_MIXER_OPERATOR_KEY = "CrossoverEAMixerOperatorKey";

    protected PropertyCrossoverMixer crossoverMixer;
    protected boolean useSelfAdaption = false;
    protected double tau1 = 0.15;
    protected double lowerLimitChance = 0.05;
    protected int lastOperatorIndex = -1;

    public CrossoverEAMixer() {
        InterfaceCrossover[] tmpList;
        ArrayList<String> crossers = GenericObjectEditor.getClassesFromProperties(InterfaceCrossover.class.getCanonicalName(), null);
        tmpList = new InterfaceCrossover[crossers.size()];
        for (int i = 0; i < crossers.size(); i++) {
            Class clz = null;
            try {
                clz = (Class) Class.forName((String) crossers.get(i));
            } catch (ClassNotFoundException e1) {
                continue;
            }
            if (clz.isAssignableFrom(this.getClass())) {
                // Do not instanciate this class or its subclasses or die of an infinite loop
//				System.out.println("Skipping " + clz.getClass().getName());
                continue;
            } else {
//				System.out.println("Taking " + clz.getClass().getName());
            }
            try {
                tmpList[i] = (InterfaceCrossover) Class.forName((String) crossers.get(i)).newInstance();
            } catch (java.lang.ClassNotFoundException e) {
                System.out.println("Could not find class for " + crossers.get(i));
            } catch (java.lang.InstantiationException k) {
                System.out.println("Instantiation exception for " + crossers.get(i));
            } catch (java.lang.IllegalAccessException a) {
                System.out.println("Illegal access exception for " + crossers.get(i));
            }
        }
        this.crossoverMixer = new PropertyCrossoverMixer(tmpList);
        tmpList = new InterfaceCrossover[2];
        tmpList[0] = new CrossoverESArithmetical();
        tmpList[1] = new CrossoverESSBX();
        this.crossoverMixer.setSelectedCrossers(tmpList);
        this.crossoverMixer.normalizeWeights();
        this.crossoverMixer.setDescriptiveString("Combining alternative mutation operators, please norm the weights!");
        this.crossoverMixer.setWeightsLabel("Weights");
    }

    public CrossoverEAMixer(CrossoverEAMixer mutator) {
        this.crossoverMixer = (PropertyCrossoverMixer) mutator.crossoverMixer.clone();
        this.useSelfAdaption = mutator.useSelfAdaption;
        this.tau1 = mutator.tau1;
        this.lowerLimitChance = mutator.lowerLimitChance;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverEAMixer(this);
    }

    /**
     * This method allows you to evaluate whether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof CrossoverEAMixer) {
            CrossoverEAMixer mut = (CrossoverEAMixer) mutator;

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to initialize the crossover operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        InterfaceCrossover[] crossers = this.crossoverMixer.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
            crossers[i].init(individual, opt);
        }
    }

    /**
     * This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     *
     * @param indy1    The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        this.crossoverMixer.normalizeWeights();
        double[] probs = this.crossoverMixer.getWeights();
        if (this.useSelfAdaption) {
            for (int i = 0; i < probs.length; i++) {
                probs[i] *= Math.exp(this.tau1 * RNG.gaussianDouble(1));
                if (probs[i] <= this.lowerLimitChance) {
                    probs[i] = this.lowerLimitChance;
                }
                if (probs[i] >= 1) {
                    probs[i] = 1;
                }
            }
            this.crossoverMixer.normalizeWeights();
        }

        InterfaceCrossover[] crossover = this.crossoverMixer.getSelectedCrossers();
        double pointer = RNG.randomFloat(0, 1);
        double dum = probs[0];
        lastOperatorIndex = 0;
        while ((pointer > dum) && (lastOperatorIndex < probs.length - 1)) {
            lastOperatorIndex++;
            dum += probs[lastOperatorIndex];
        }
        if (lastOperatorIndex == probs.length) {
            lastOperatorIndex = RNG.randomInt(0, probs.length - 1);
        }
//        System.out.println("Using : " + mutators[index].getStringRepresentation());
//        for (int i = 0; i < probs.length; i++) {
//            System.out.println(""+mutators[i].getStringRepresentation()+" : "+ probs[i]);
//        }
//        System.out.println("");

        indy1.putData(CROSSOVER_EA_MIXER_OPERATOR_KEY, lastOperatorIndex);
        for (int i = 0; i < partners.size(); i++) {
            partners.getEAIndividual(i).putData(CROSSOVER_EA_MIXER_OPERATOR_KEY, lastOperatorIndex);
        }
        AbstractEAIndividual[] indies = crossover[lastOperatorIndex].mate(indy1, partners);

        maybeAdaptWeights(indies);
        return indies;
    }

    protected void maybeAdaptWeights(AbstractEAIndividual[] indies) {
    }

    public int getLastOperatorIndex() {
        return lastOperatorIndex;
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "EA mutation mixer";
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
        return "EA mutation mixer";
    }

    /**
     * Choose the set of crossers.
     *
     * @param d The crossover operators.
     */
    public void setCrossovers(PropertyCrossoverMixer d) {
        this.crossoverMixer = d;
    }

    public PropertyCrossoverMixer getCrossovers() {
        return this.crossoverMixer;
    }

    public String CrossoversTipText() {
        return "Choose the set of crossover operators.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setUseSelfAdaption(boolean d) {
        this.useSelfAdaption = d;
    }

    public boolean getUseSelfAdaption() {
        return this.useSelfAdaption;
    }

    public String useSelfAdaptionTipText() {
        return "Use my implementation of self-adaption for the mutation mixer.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setLowerLimitChance(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitChance = d;
    }

    public double getLowerLimitChance() {
        return this.lowerLimitChance;
    }

    public String lowerLimitChanceTipText() {
        return "Set the lower limit for the mutation chance.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau1 = d;
    }

    public double getTau1() {
        return this.tau1;
    }

    public String tau1TipText() {
        return "Set the value for tau1.";
    }

    @Override
    public int getEvaluations() {
        int numEvals = 0;
        InterfaceCrossover[] crossers = this.crossoverMixer.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
            if (crossers[i] instanceof InterfaceEvaluatingCrossoverOperator) {
                numEvals += ((InterfaceEvaluatingCrossoverOperator) crossers[i]).getEvaluations();
            }
        }
        return numEvals;
    }

    @Override
    public void resetEvaluations() {
        InterfaceCrossover[] crossers = this.crossoverMixer.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
            if (crossers[i] instanceof InterfaceEvaluatingCrossoverOperator) {
                ((InterfaceEvaluatingCrossoverOperator) crossers[i]).resetEvaluations();
            }
        }
    }
}