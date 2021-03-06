package eva2.optimization.operator.paramcontrol;

import eva2.optimization.population.Population;

/**
 * Interface for dynamic changes to object parameters.
 * When an optimizer has the getter method getParamControl retrieving an instance of this interface,
 * an update is triggered after every optimize call. This allows parameters of the object to be
 * adapted dynamically, such as linearly decreasing weights or control parameters. In case of PSO,
 * this may be the linearly decreasing inertia weight or the activation feedback control mechanism.
 * <p>
 * Notice how Processor handles parameter control. For now, a controlled object must implement
 * the method getParamControl() which is to return either an InterfaceParameterControl or an array
 * of Objects which should either be instances of InterfaceParameterControl or
 * themselves should implement getParamControl(). This allows recursive
 * parameter adaption hierarchies.
 *
 * @author mkron
 */
public interface InterfaceParameterControl {

    /**
     * Make a deep copy of the object.
     *
     * @return Deep copy
     */
    Object clone();

    /**
     * Initialize the parameter control instance before a run.
     *
     * @param obj The controlled object.
     */
    void init(Object obj, Population initialPop);

    /**
     * After an optimization run, finalizing stuff may be done.
     *
     * @param obj The controlled object.
     */
    void finish(Object obj, Population finalPop);

    /**
     * For a given runtime (maxIteration) and current iteration, update the parameters of the object.
     *
     * @param obj Object
     * @param pop Population
     * @param iteration Iteration
     * @param maxIteration Maximum Iteration
     */
    void updateParameters(Object obj, Population pop, int iteration, int maxIteration);

    /**
     * If no runtime in terms of iterations can be specified, the parameter control may try to infer
     * the state from the object itself.
     *
     * @param obj Object
     */
    void updateParameters(Object obj);
}
