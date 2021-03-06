package eva2.optimization.individuals.codings.gp;


import eva2.problems.InterfaceProgramProblem;
import eva2.tools.math.Mathematics;

/**
 * A simple sum node with a single, possibly vectorial (array), argument.
 */
public class GPNodeSum extends AbstractGPNode implements java.io.Serializable {

    public GPNodeSum() {
    }

    public GPNodeSum(GPNodeSum node) {
        this.cloneMembers(node);
    }

    /**
     * This method will be used to identify the node in the GPAreaEditor
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Sum";
    }

    /**
     * This method allows you to clone the Nodes
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return new GPNodeSum(this);
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

        for (int i = 0; i < this.nodes.length; i++) {
            tmpObj = this.nodes[i].evaluate(environment);
            if (tmpObj instanceof double[]) {
                result += Mathematics.sum((double[]) tmpObj);
            } else if (tmpObj instanceof Double[]) {
                Double[] vals = (Double[]) tmpObj;
                for (int j = 0; j < vals.length; j++) {
                    result += vals[j];
                }
            } else if (tmpObj instanceof Double) {
                result = (Double) tmpObj;
            }
        }
        return result;
    }

    @Override
    public String getOpIdentifier() {
        return "sum";
    }
}
