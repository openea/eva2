package eva2.optimization.enums;

public enum PSOTopology {
    /**
     *
     */
    linear,
    /**
     *
     */
    grid,
    /**
     *
     */
    star,
    /**
     *
     */
    multiSwarm,
    /**
     *
     */
    tree,
    /**
     *
     */
    hpso,
    /**
     *
     */
    random,
    dms;

    /**
     * A method to translate the "old" integer tags into the enum type.
     *
     * @param oldID
     * @return
     */
    public static PSOTopology getFromId(int oldID) {
        switch (oldID) {
            case 0:
                return linear;
            case 1:
                return grid;
            case 2:
                return star;
            case 3:
                return multiSwarm;
            case 4:
                return tree;
            case 5:
                return hpso;
            case 6:
                return random;
            case 7:
                return dms;
            default:
                System.err.println("Error: invalid old topology ID in PSOTopologyEnum getFromId! Returning grid.");
                return grid;
        }
    }
}