package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceMultimodalProblemKnown;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This terminator counts the number of found optima for a problem that
 * implements InterfaceMultimodalProblemKnown. A population is regarded as terminated
 * if the preset number of optima is identified.
 * For any other problem types, this terminator will not make sense, so take care.
 */
@Description("Terminate if a given number of optima has been found. Works for problems implementing InterfaceMultimodalProblemKnown, e.g. FM0.")
public class KnownOptimaFoundTerminator implements InterfaceTerminator, Serializable {
    private static final Logger LOGGER = Logger.getLogger(KnownOptimaFoundTerminator.class.getName());
    private InterfaceMultimodalProblemKnown mProblem = null;
    private int reqOptima = 1;
    private String msg = "";

    public KnownOptimaFoundTerminator() {}

    @Override
    public void initialize(InterfaceOptimizationProblem prob) throws IllegalArgumentException {
        if (prob != null) {
            if (prob instanceof InterfaceMultimodalProblemKnown) {
                mProblem = (InterfaceMultimodalProblemKnown) prob;
            } else {
                throw new IllegalArgumentException("KnownOptimaFoundTerminator only works with InterfaceMultimodalProblemKnown instances!");
            }
        } else {
            LOGGER.log(Level.WARNING, "KnownOptimaFoundTerminator wont work with null problem!");
        }
        msg = "Not terminated.";
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerm(solSet.getSolutions());
    }

    @Override
    public boolean isTerminated(PopulationInterface pop) {
        EVAERROR.errorMsgOnce("Warning, the KnownOptimaFoundTerminator is supposed to work on a final population.");
        return isTerm((Population) pop);
    }

    private boolean isTerm(Population pop) {
        int found = mProblem.getNumberOfFoundOptima(pop);
        if (found >= reqOptima) {
            msg = "There were " + reqOptima + " optima found.";
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    /**
     * @return the reqOptima
     */
    public int getRequiredOptima() {
        return reqOptima;
    }

    /**
     * @param reqOptima the reqOptima to set
     */
    @Parameter(description = "The number of optima that need to be found to terminate the optimization.")
    public void setRequiredOptima(int reqOptima) {
        this.reqOptima = reqOptima;
    }

    @Override
    public String toString() {
        return "KnownOptimaFoundTerminator requiring " + reqOptima + " optima.";
    }
}
