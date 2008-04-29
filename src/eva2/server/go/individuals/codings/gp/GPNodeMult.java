package eva2.server.go.individuals.codings.gp;

import eva2.server.go.problems.InterfaceProgramProblem;


/** A multiplicator node taking to arguments.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:00:16
 * To change this template use Options | File Templates.
 */
public class GPNodeMult extends AbstractGPNode implements java.io.Serializable {

    public GPNodeMult() {
    }
    public GPNodeMult(GPNodeMult node) {
        this.m_Depth    = node.m_Depth;
        this.m_Parent   = node.m_Parent;
        this.m_Nodes    = new AbstractGPNode[node.m_Nodes.length];
        for (int i = 0; i < node.m_Nodes.length; i++) this.m_Nodes[i] = (AbstractGPNode) node.m_Nodes[i].clone();
    }

    /** This method allows you to determine wehter or not two subtrees
     * are actually the same.
     * @param obj   The other subtree.
     * @return boolean if equal true else false.
     */
    public boolean equals(Object obj) {
        if (obj instanceof GPNodeMult) {
            GPNodeMult node = (GPNodeMult)obj;
            if (this.m_Nodes.length != node.m_Nodes.length) return false;
            for (int i = 0; i < this.m_Nodes.length; i++) {
                if (!this.m_Nodes[i].equals(node.m_Nodes[i])) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method will be used to identify the node in the GPAreaEditor
     * @return The name.
     */
    public String getName() {
        return "Mult";
    }

    /** This method allows you to clone the Nodes
     * @return the clone
     */
    public Object clone() {
        return (Object) new GPNodeMult(this);
    }

    /** This method will return the current arity
     * @return Arity.
     */
    public int getArity() {
        return 2;
    }

    /** This method will evaluate a given node
     * @param environment
     */
    public Object evaluate(InterfaceProgramProblem environment) {
        Object tmpObj;
        double result = 1;

        for (int i = 0; i < this.m_Nodes.length; i++) {
            tmpObj = this.m_Nodes[i].evaluate(environment);
            if (tmpObj instanceof Double) result = result * ((Double)tmpObj).doubleValue();
        }
        return new Double(result);
    }

    /** This method returns a string representation
     * @return string
     */
    public String getStringRepresentation() {
        String result = "*( ";
        for (int i = 0; i < this.m_Nodes.length; i++) result += this.m_Nodes[i].getStringRepresentation() +" ";
        result += ")";
        return result;
    }
}