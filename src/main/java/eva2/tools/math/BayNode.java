package eva2.tools.math;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class BayNode {

    private int id;
    private int numberOfParents = 0;
    private double[] pTable = {0.5};
    private boolean calculated = false;
    private List<Integer> parents = new LinkedList<>();
    private List<Integer> children = new LinkedList<>();

    public BayNode(int id) {
        this.id = id;
    }

    public BayNode(BayNode b) {
        this.id = b.id;
        this.numberOfParents = b.numberOfParents;
        this.pTable = b.pTable.clone();
        this.parents = new LinkedList<>();
        this.children = new LinkedList<>();
        this.parents.addAll(b.parents.stream().collect(Collectors.toList()));
        this.children.addAll(b.children.stream().collect(Collectors.toList()));
        this.calculated = b.calculated;
    }

    @Override
    public Object clone() {
        return new BayNode(this);
    }

    public double getProbability(int i) {
        return pTable[i];
    }

    public void generateNewPTable() {
        this.pTable = new double[(int) Math.pow(2, this.numberOfParents)];
        for (int i = 0; i < this.pTable.length; i++) {
            this.pTable[i] = 0.50001;
        }
    }

    public List<Integer> getParents() {
        return this.parents;
    }

    public void addParent(Integer b) {
        if (!this.parents.contains(b)) {
            this.parents.add(b);
        }
    }

    public void removeParent(Integer b) {
        this.parents.remove(b);
    }

    public List<Integer> getChildren() {
        return this.children;
    }

    public void addChild(Integer b) {
        if (!this.children.contains(b)) {
            this.children.add(b);
        }
    }

    public void removeChild(Integer b) {
        this.children.remove(b);
    }

    public void setPTable(double[] table) {
        this.pTable = table;
    }

    public void setPTable(int i, double v) {
        this.pTable[i] = v;
    }

    public void incrNumberOfParents() {
        this.numberOfParents++;
    }

    public void decrNumberOfParents() {
        this.numberOfParents--;
    }

    public int getNumberOfParents() {
        return this.numberOfParents;
    }

    public int getId() {
        return this.id;
    }

    public double[] getPTable() {
        return this.pTable;
    }

    public boolean getCalculated() {
        return this.calculated;
    }

    public void setCalculated(boolean b) {
        this.calculated = b;
    }
}