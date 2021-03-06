package eva2.optimization.operator.nichepso.deactivation;

import eva2.optimization.strategies.ParticleSubSwarmOptimization;

/**
 * dummy strategy does not deactivate anything
 */
public class DummyDeactivationStrategy implements InterfaceDeactivationStrategy, java.io.Serializable {

    @Override
    public Object clone() {
        return new DummyDeactivationStrategy();
    }

    @Override
    public int[] deactivateSubswarm(ParticleSubSwarmOptimization subswarm,
                                    ParticleSubSwarmOptimization mainswarm) {
        return null;
    }

    @Override
    public boolean shouldDeactivateSubswarm(
            ParticleSubSwarmOptimization subswarm) {
        return false;
    }

}
