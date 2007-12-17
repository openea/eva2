package javaeva.gui;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.09.2005
 * Time: 10:16:18
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceDPointWithContent {
    public void setEAIndividual(AbstractEAIndividual indy);
    public AbstractEAIndividual getEAIndividual();

    /** This method allows you to set the according optimization problem
     * @param problem  InterfaceOptimizationProblem
     */
    public void setProblem(InterfaceOptimizationProblem problem);
    public InterfaceOptimizationProblem getProblem();

    /** This method allows you to draw additional data of the individual
     */
    public void showIndividual();
}