package eva2.optimization.stat;


import eva2.gui.plot.DataViewer;
import eva2.gui.plot.DataViewerInterface;
import eva2.gui.plot.Graph;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 *
 */
public class GenericStatistics implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GenericStatistics.class.getName());
    private int test;
    private String[] propertyNames;
    private boolean[] states;
    private transient Field[] fields;
    private DataViewerInterface viewer;
    private Graph graph;

    /**
     *
     */
    public GenericStatistics getClone() {
        return new GenericStatistics(this);
    }

    /**
     *
     */
    private GenericStatistics(GenericStatistics Source) {
        test = Source.test;
        propertyNames = Source.propertyNames;
        states = Source.states;
        fields = Source.fields;
        viewer = Source.viewer;
        graph = Source.graph;

    }

    /**
     *
     */
    public GenericStatistics(Object target) {
        try {
            fields = getDeclaredFields(target);
            propertyNames = new String[fields.length];
            states = new boolean[fields.length];
            for (int i = 0; i < fields.length; i++) {
                String desc = fields[i].toString(); //System.out.println("desc "+desc);
                int istransient = desc.indexOf("transient");
                Object FieldValue = null;
                if (istransient == -1 || fields[i].getName().equals("elementData")) {  // the elementdatahack
                    fields[i].setAccessible(true);
                    FieldValue = fields[i].get(target);
                }
                propertyNames[i] = fields[i].getName();
            }
        } catch (Exception ex) {
            System.out.println("ERROR in GenericStatistics:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    public void setTest(int Test) {
        test = Test;
    }

    /**
     *
     */
    public int getTest() {
        return test;
    }

    /**
     *
     */
    public String[] getPropertyNames() {
        return propertyNames;
    }

    /**
     *
     */
    public boolean[] getState() {
        return states;
    }

    /**
     *
     */
    public void setState(boolean[] x) {
        System.out.println("in statistics setState !!!!!!!!!!!!!!!!!!");
        states = x;
    }

    /**
     *
     */
    public void initViewer() {
        viewer = DataViewer.getInstance("test");
        graph = viewer.getNewGraph("test");
    }

    /**
     *
     */
    public Field[] getDeclaredFields(Object target) {
        Field[] ret_1 = target.getClass().getSuperclass().getDeclaredFields();
        Field[] ret_2 = target.getClass().getDeclaredFields();
        Field[] ret = new Field[ret_1.length + ret_2.length];
        int index = 0;
        for (int i = 0; i < ret_1.length; i++) {
            ret[index] = ret_1[i];
            index++;
        }
        for (int i = 0; i < ret_2.length; i++) {
            ret[index] = ret_2[i];
            index++;
        }
        return ret;
    }

    /**
     *
     */
    public void statechanged(Object target) {
        int len = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == true) {
                len++;
            }
        }
        if (len == 0) {
            return;
        }
        if (viewer == null) {
            initViewer();
        }
        double[] data = new double[len];
        try {
            fields = getDeclaredFields(target);
        } catch (Exception ex) {
            System.out.println("ERROR in GenericStatistics:" + ex.getMessage());
            ex.printStackTrace();
        }
        int index = 0;
        for (int i = 0; i < fields.length; i++) {
            for (int n = 0; n < propertyNames.length; n++) {
                if (this.states[n] == false) {
                    continue;
                }
                if (fields[i].getName().equals(propertyNames[n])) {
                    String desc = fields[i].toString(); //System.out.println("desc "+desc);
                    int istransient = desc.indexOf("transient");

                    Object FieldValue = null;
                    if (istransient == -1 || fields[i].getName().equals("elementData")) {  // the elementdatahack
                        fields[i].setAccessible(true);
                        try {
                            FieldValue = fields[i].get(target);
                            if (FieldValue instanceof Double) {
                                data[index] = ((Double) FieldValue).doubleValue();
                            }
                            if (FieldValue instanceof Integer) {
                                data[index] = ((Integer) FieldValue).doubleValue();
                            }
                            index++;
                        } catch (Exception ex) {
                            System.out.println("ERROR in GenericStatistics:" + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }
        graph.setConnectedPoint(data[1], data[0]);
    }
}