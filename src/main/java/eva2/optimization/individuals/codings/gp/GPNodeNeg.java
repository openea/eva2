package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;


/**
 * A substraction node using two arguments.
 */
public class GPNodeNeg extends AbstractGPNode implements java.io.Serializable {

    public GPNodeNeg() {
    }

    public GPNodeNeg(GPNodeNeg node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Neg";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeNeg(this);
    }

    /**
     * This method will return the current arity
     *
     * @return Arity.
     */
    @Override
    public int getArity() {
        return 1;
    }

    /**
     * This method will evaluate a given node
     *
     * @param environment
     */
    @Override
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 0;

        tmpObj = this.nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) {
            result += (Double) tmpObj;
        }
        for (int i = 1; i < this.nodes.length; i++) {
            tmpObj = this.nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) {
                result -= (Double) tmpObj;
            }
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "neg";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//    	return AbstractGPNode.makeStringRepresentation(nodes, "-");
//    }
}
