package eva2.examples;

import eva2.OptimizerFactory;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.MutateESCovarianceMatrixAdaption;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.strategies.EvolutionStrategies;
import eva2.problems.FM0Problem;

public class TestingPlusCmaEs {

    public static void main(String[] args) {
        // a simple bimodal target function, two optima near (1.7,0) and (-1.44/0)
        FM0Problem fm0 = new FM0Problem();
        AbstractEAIndividual bestIndy;
        // create standard ES parameters                
        OptimizationParameters esParams = OptimizerFactory.standardES(fm0);
        esParams.setTerminator(new EvaluationTerminator(2000));
        // set a random seed based on system time               
        esParams.setRandomSeed(0);

        // set evolutionary operators and probabilities                 
        AbstractEAIndividual.setOperators(
                fm0.getIndividualTemplate(),
                new MutateESCovarianceMatrixAdaption(true), 0.9,
                new CrossoverESDefault(), 0.1);

        // access the ES                
        EvolutionStrategies es = (EvolutionStrategies) esParams.getOptimizer();
        // set a (1+5) selection strategy               
        es.setMu(1);
        es.setLambda(5);
        es.setPlusStrategy(true);

        // run optimization and retrieve winner individual              
        bestIndy = (AbstractEAIndividual) OptimizerFactory.optimizeToInd(esParams, null);
        System.out.println(esParams.getTerminator().lastTerminationMessage() + "\nFound solution: "
                + AbstractEAIndividual.getDefaultDataString(bestIndy));
    }

}