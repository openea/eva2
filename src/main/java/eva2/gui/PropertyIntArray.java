package eva2.gui;

/**
 *
 */
public class PropertyIntArray implements java.io.Serializable {
    public int[] intArray;

    public PropertyIntArray(int[] d) {
        this.intArray = d;
    }

    public PropertyIntArray(PropertyIntArray d) {
        this.intArray = new int[d.intArray.length];
        System.arraycopy(d.intArray, 0, this.intArray, 0, this.intArray.length);
    }

    @Override
    public Object clone() {
        return new PropertyIntArray(this);
    }

    /**
     * This method will allow you to set the value of the double array
     *
     * @param d The int[]
     */
    public void setIntArray(int[] d) {
        this.intArray = d;
    }

    /**
     * This method will return the int array
     *
     * @return The int array
     */
    public int[] getIntArray() {
        return this.intArray;
    }
}