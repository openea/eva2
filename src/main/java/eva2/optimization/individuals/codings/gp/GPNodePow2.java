package eva2.optimization.individuals.codings.gp;

import eva2.problems.InterfaceProgramProblem;

/**
 * This node puts the argument to the power of two.
 */
public class GPNodePow2 extends AbstractGPNode implements java.io.Serializable {

    public GPNodePow2() {
    }

    public GPNodePow2(GPNodePow2 node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Pow2";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodePow2(this);
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
        double result = 1;

        tmpObj = this.nodes[0].evaluate(environment);
        if (tmpObj instanceof Double) {
            result = Math.pow((Double) tmpObj, 2);
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "pow2";
    }
//    /** This method returns a string representation
//     * @return string
//     */
//    public String getStringRepresentation() {
//    	String result = "";
//        for (int i = 0; i < this.nodes.length; i++) result += this.nodes[i].getStringRepresentation() +" ";
//        return "("+result+")^2";
//    }
}
