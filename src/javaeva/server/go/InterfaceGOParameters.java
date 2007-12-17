package javaeva.server.go;

import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.InterfaceOptimizer;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.05.2003
 * Time: 13:14:06
 * To change this template use Options | File Templates.
 */
public interface InterfaceGOParameters {

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo();

    /** This method allows you to serialize the current parameters into a *.ser file
     */
    public void saveInstance();

    /** This method returns the name
     * @return string
     */
    public String getName();

    /** This methods allow you to set and get the Seed for the Random Number Generator.
     * @param x     Long seed.
     */
    public void setSeed(long x);
    public long getSeed();
    public String seedTipText();

    /** This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     * @param term  The new terminator
     */
    public void setTerminator(TerminatorInterface term);
    public TerminatorInterface getTerminator();
    public String terminatorTipText();

    /** This method allows you to set the current optimizing algorithm
     * @param optimizer The new optimizing algorithm
     */
    public void setOptimizer(InterfaceOptimizer optimizer);
    public InterfaceOptimizer getOptimizer();
//    public String optimizerTipText();

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void setProblem (InterfaceOptimizationProblem problem);
    public InterfaceOptimizationProblem getProblem ();
    public String problemTipText();

    /** This method will set the output filename
     * @param name
     */
    public void setOutputFileName (String name);
    public String getOutputFileName ();
    public String outputFileNameTipText();
}