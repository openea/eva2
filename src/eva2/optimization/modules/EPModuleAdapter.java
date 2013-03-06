package eva2.optimization.modules;

/**
 * This the EP module adapter necessary to access this implementation from the EvA top level.
 */
public class EPModuleAdapter extends GenericModuleAdapter implements ModuleAdapter {

    private static final String moduleName = "Evolutionary_Programming";

    public EPModuleAdapter(String adapterName) {
        super(adapterName, "EP.html", EPParameters.getInstance(), true);
    }

    /**
     * This method returns the name of the ModulAdapter
     *
     * @return The name
     */
    public static String getName() {
        return moduleName;
    }
}