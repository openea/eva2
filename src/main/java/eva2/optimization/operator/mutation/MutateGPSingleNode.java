package eva2.optimization.operator.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGPIndividual;
import eva2.optimization.individuals.codings.gp.AbstractGPNode;
import eva2.optimization.individuals.codings.gp.GPArea;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 */
@Description("The node mutation replaces a random node but keeps the descendants.")
public class MutateGPSingleNode implements InterfaceMutation, java.io.Serializable {

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGPSingleNode();
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        return mutator instanceof MutateGPSingleNode;
    }

    /**
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void initialize(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
//        System.out.println("Before Mutate: " +((InterfaceGPIndividual)individual).getPGenotype()[0].getStringRepresentation());
//        System.out.println("Length:        " +((InterfaceGPIndividual)individual).getPGenotype()[0].getNumberOfNodes());
        if (individual instanceof InterfaceGPIndividual) {
            ArrayList allNodes = new ArrayList();
            AbstractGPNode[] programs = ((InterfaceGPIndividual) individual).getPGenotype();
            GPArea[] areas = (GPArea[]) ((InterfaceGPIndividual) individual).getFunctionArea();
            for (int i = 0; i < programs.length; i++) {
                programs[i].addNodesTo(allNodes);
                AbstractGPNode nodeToMutate = (AbstractGPNode) allNodes.get(RNG.randomInt(0, allNodes.size() - 1));
                int orgArity = nodeToMutate.getArity();
                AbstractGPNode newNode = (AbstractGPNode) areas[i].getRandomNodeWithArity(orgArity).clone();
//                System.out.println("OldNode "+ nodeToMutate.getName() + ":"+nodeToMutate.getArity() + " / NewNode "+newNode.getName() + ":"+newNode.getArity());
                AbstractGPNode parent = nodeToMutate.getParent();
                if (parent != null) {
                    newNode.setParent(parent);
                } else {
                    ((InterfaceGPIndividual) individual).setPGenotype(newNode, i);
                }
                // now reconnect the children
                newNode.initNodeArray();
                for (int j = 0; j < nodeToMutate.getArity(); j++) {
                    if (nodeToMutate.getNode(j) == null) {
                        System.out.println("j" + j);
                    }
                    newNode.setNode(nodeToMutate.getNode(j), j);
                }
            }
        }
//        System.out.println("After Mutate:  " +((InterfaceGPIndividual)individual).getPGenotype()[0].getStringRepresentation());
//        System.out.println("Length:        " +((InterfaceGPIndividual)individual).getPGenotype()[0].getNumberOfNodes());
    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GP node mutation";
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GP node mutation";
    }
}