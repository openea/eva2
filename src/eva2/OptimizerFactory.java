/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 */
package eva2;

import java.util.BitSet;
import java.util.Vector;

import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.crossover.CrossoverESDefault;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.operators.crossover.NoCrossover;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESCovarianceMartixAdaption;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.mutation.MutateESGlobal;
import eva2.server.go.operators.mutation.NoMutation;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.terminators.CombinedTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.operators.terminators.FitnessConvergenceTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.strategies.ClusterBasedNichingEA;
import eva2.server.go.strategies.ClusteringHillClimbing;
import eva2.server.go.strategies.DifferentialEvolution;
import eva2.server.go.strategies.EvolutionStrategies;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.GradientDescentAlgorithm;
import eva2.server.go.strategies.HillClimbing;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.MonteCarloSearch;
import eva2.server.go.strategies.ParticleSwarmOptimization;
import eva2.server.go.strategies.SimulatedAnnealing;
import eva2.server.go.strategies.Tribes;
import eva2.server.modules.GOParameters;


/**
 * <p>
 * The OptimizerFactory allows quickly creating some optimizers without thinking
 * much about parameters. You can access a runnable Optimization thread and
 * directly start it, or access its fully prepared GOParameter instance, change
 * some parameters, and start it then.
 * </p>
 * <p>
 * On the other hand this class provides an almost complete list of all
 * currently available optimization procedures in EvA2. The arguments passed
 * to the methods initialize the respective optimization procedure. To perform
 * an optimization one has to do the following: <code>
 * InterfaceOptimizer optimizer = OptimizerFactory.createCertainOptimizer(arguments);
 * EvaluationTerminator terminator = new EvaluationTerminator();
 * terminator.setFitnessCalls(numOfFitnessCalls);
 * while (!terminator.isTerminated(mc.getPopulation())) mc.optimize();
 * </code>
 * </p>
 *
 * @version 0.1
 * @since 2.0
 * @author mkron
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @date 17.04.2007
 */
public class OptimizerFactory {
	private static InterfaceTerminator	term	         = null;

	public final static int	           STD_ES	         = 1;

	public final static int	           CMA_ES	         = 2;

	public final static int	           STD_GA	         = 3;

	public final static int	           PSO	           = 4;

	public final static int	           DE	             = 5;

	public final static int	           TRIBES	         = 6;

	public final static int	           RANDOM	         = 7;

	public final static int	           HILLCL	         = 8;

	public final static int	           CBN_ES	         = 9;

	public final static int	           CL_HILLCL	     = 10;

	public final static int	           defaultFitCalls	= 10000;

	public final static int	           randSeed	       = 0;

	private static OptimizerRunnable	 lastRunnable	   = null;

	/**
	 * Add an InterfaceTerminator to any new optimizer in a boolean combination.
	 * The old and the given terminator will be combined as in (TOld && TNew) if
	 * bAnd is true, and as in (TOld || TNew) if bAnd is false.
	 *
	 * @param newTerm
	 *          a new InterfaceTerminator instance
	 * @param bAnd
	 *          indicate the boolean combination
	 */
	public static void addTerminator(InterfaceTerminator newTerm, boolean bAnd) {
		if (OptimizerFactory.term == null)
			OptimizerFactory.term = term;
		else setTerminator(new CombinedTerminator(OptimizerFactory.term, newTerm,
		    bAnd));
	}

	public static final GOParameters cbnES(AbstractOptimizationProblem problem) {
		ClusterBasedNichingEA cbn = new ClusterBasedNichingEA();
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);
		cbn.setOptimizer(es);
		ClusteringDensityBased clustering = new ClusteringDensityBased(0.1);
		cbn.setConvergenceCA((ClusteringDensityBased) clustering.clone());
		cbn.setDifferentationCA(clustering);
		cbn.setShowCycle(0); // don't do graphical output

		Population pop = new Population();
		pop.setPopulationSize(100);
		problem.initPopulation(pop);

		return makeParams(cbn, pop, problem, randSeed, defaultTerminator());
	}

	public static final GOParameters clusteringHillClimbing(
	    AbstractOptimizationProblem problem) {
		ClusteringHillClimbing chc = new ClusteringHillClimbing();
		chc.SetProblem(problem);
		Population pop = new Population();
		pop.setPopulationSize(100);
		problem.initPopulation(pop);
		chc.setHcEvalCycle(1000);
		chc.setInitialPopSize(100);
		chc.setStepSizeInitial(0.05);
		chc.setMinImprovement(0.000001);
		chc.setNotifyGuiEvery(0);
		chc.setStepSizeThreshold(0.000001);
		chc.setSigmaClust(0.05);
		return makeParams(chc, pop, problem, randSeed, defaultTerminator());
	}

	public static final GOParameters cmaES(AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);

		// TODO improve this by adding getEAIndividual to AbstractEAIndividual?
		AbstractEAIndividual indyTemplate = problem.getIndividualTemplate();
		if ((indyTemplate != null)
		    && (indyTemplate instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			AbstractEAIndividual indy = (AbstractEAIndividual) indyTemplate;
			MutateESCovarianceMartixAdaption cmaMut = new MutateESCovarianceMartixAdaption();
			cmaMut.setCheckConstraints(true);
			indy.setMutationOperator(cmaMut);
			indy.setCrossoverOperator(new CrossoverESDefault());
		} else {
			System.err
			    .println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
			return null;
		}

		Population pop = new Population();
		pop.setPopulationSize(es.getLambda());

		return makeParams(es, pop, problem, randSeed, defaultTerminator());
	}

	/**
	 * This method optimizes the given problem using differential evolution.
	 *
	 * @param problem
	 * @param popsize
	 * @param f
	 * @param k
	 * @param lambda
	 * @param listener
	 * @return An optimization algorithm that performs differential evolution.
	 */
	public static final DifferentialEvolution createDifferentialEvolution(
	    AbstractOptimizationProblem problem, int popsize, double f,
	    double lambda, double k, InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0.0);

		DifferentialEvolution de = new DifferentialEvolution();
		de.SetProblem(problem);
		de.getPopulation().setPopulationSize(popsize);
		de.getDEType().setSelectedTag(1);
		de.setF(f);
		de.setK(k);
		de.setLambda(lambda);
		de.addPopulationChangedEventListener(listener);
		de.init();

		listener.registerPopulationStateChanged(de.getPopulation(), "");

		return de;
	}

	/**
	 * This method performs the optimization using an Evolution strategy.
	 *
	 * @param mu
	 * @param lambda
	 * @param plus
	 * @param mutationoperator
	 * @param pm
	 * @param crossoveroperator
	 * @param pc
	 * @param selection
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employes an evolution strategy.
	 */
	public static final EvolutionStrategies createEvolutionStrategy(int mu,
	    int lambda, boolean plus, InterfaceMutation mutationoperator, double pm,
	    InterfaceCrossover crossoveroperator, double pc,
	    InterfaceSelection selection, AbstractOptimizationProblem problem,
	    InterfacePopulationChangedEventListener listener) {

		problem.initProblem();
		// RNG.setRandomSeed(100);

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setMutationOperator(mutationoperator);
		tmpIndi.setMutationProbability(pm);// */ // 1/tmpIndi.size()
		tmpIndi.setCrossoverOperator(crossoveroperator);
		tmpIndi.setCrossoverProbability(pc);// */ // 0.95

		EvolutionStrategies es = new EvolutionStrategies();
		es.addPopulationChangedEventListener(listener);
		es.setParentSelection(selection);
		es.setPartnerSelection(selection);
		es.setEnvironmentSelection(selection);
		es.setGenerationStrategy(mu, lambda, plus); // comma strategy
		es.SetProblem(problem);
		es.init();

		listener.registerPopulationStateChanged(es.getPopulation(), "");

		return es;
	}

	/**
	 * This method performs a Genetic Algorithm.
	 *
	 * @param mut
	 * @param pm
	 * @param cross
	 * @param pc
	 * @param select
	 * @param popsize
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employes an genetic algorithm.
	 */
	public static final GeneticAlgorithm createGeneticAlgorithm(
	    InterfaceMutation mut, double pm, InterfaceCrossover cross, double pc,
	    InterfaceSelection select, int popsize,
	    AbstractOptimizationProblem problem,
	    InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(cross);
		tmpIndi.setCrossoverProbability(pc);
		tmpIndi.setMutationOperator(mut);
		tmpIndi.setMutationProbability(pm);

		GeneticAlgorithm ga = new GeneticAlgorithm();
		ga.SetProblem(problem);
		ga.getPopulation().setPopulationSize(popsize);
		ga.setParentSelection(select);
		ga.setPartnerSelection(select);
		ga.addPopulationChangedEventListener(listener);
		ga.init();

		listener.registerPopulationStateChanged(ga.getPopulation(), "");

		return ga;
	}

	/**
	 * This starts a Gradient Descent.
	 *
	 * @param problem
	 * @return An optimization algorithm that performs gradient descent.
	 */
	public static final GradientDescentAlgorithm createGradientDescent(
	    AbstractOptimizationProblem problem) {

		System.err.println("Currently not implemented!");

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);

		GradientDescentAlgorithm gd = new GradientDescentAlgorithm();

		// TODO implement!

		return gd;
	}

	/**
	 * This method performs a Hill Climber algorithm.
	 *
	 * @param pop
	 *          The size of the population
	 * @param problem
	 *          The problem to be optimized
	 * @param listener
	 * @return An optimization procedure that performes hill climbing.
	 */
	public static final HillClimbing createHillClimber(int pop,
	    AbstractOptimizationProblem problem,
	    InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		MutateESFixedStepSize mutator = new MutateESFixedStepSize();
		mutator.setSigma(0.2); // mutations step size
		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setMutationOperator(mutator);
		tmpIndi.setMutationProbability(1.0);
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0);

		HillClimbing hc = new HillClimbing();
		hc.getPopulation().setPopulationSize(pop);
		hc.addPopulationChangedEventListener(listener);
		hc.SetProblem(problem);
		hc.init();

		listener.registerPopulationStateChanged(hc.getPopulation(), "");

		return hc;
	}

	/**
	 * This method performs a Monte Carlo Search with the given number of
	 * fitnesscalls.
	 *
	 * @param problem
	 * @param listener
	 * @return An optimization procedure that performes the random walk.
	 */
	public static final MonteCarloSearch createMonteCarlo(
	    AbstractOptimizationProblem problem,
	    InterfacePopulationChangedEventListener listener) {
		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0);
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0);

		MonteCarloSearch mc = new MonteCarloSearch();
		mc.addPopulationChangedEventListener(listener);
		mc.SetProblem(problem);
		mc.init();

		listener.registerPopulationStateChanged(mc.getPopulation(), "");

		return mc;
	}

	/**
	 * This method performs a particle swarm optimization.
	 *
	 * @param problem
	 * @param mut
	 * @param popsize
	 * @param phi1
	 * @param phi2
	 * @param k
	 * @param listener
	 * @param topology
	 * @return An optimization algorithm that performs particle swarm
	 *         optimization.
	 */
	public static final ParticleSwarmOptimization createParticleSwarmOptimization(
	    AbstractOptimizationProblem problem, int popsize, double phi1,
	    double phi2, double k, InterfacePopulationChangedEventListener listener,
	    int selectedTopology) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0.0);

		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
		pso.SetProblem(problem);
		pso.getPopulation().setPopulationSize(popsize);
		pso.setPhi1(phi1);
		pso.setPhi2(phi2);
		pso.setSpeedLimit(k);
		pso.getTopology().setSelectedTag(selectedTopology);
		pso.addPopulationChangedEventListener(listener);
		pso.init();

		listener.registerPopulationStateChanged(pso.getPopulation(), "");

		return pso;
	}

	/**
	 * This method performs a Simulated Annealing Optimization and prints the
	 * result as R output. It uses real valued individuals. The mutation
	 * probability is always 1.0.
	 *
	 * @param problem
	 * @param popsize
	 * @param alpha
	 *          The parameter for the linear cooling
	 * @param temperature
	 *          The initial temperature
	 * @param mut
	 * @param listener
	 * @return Returns an optimizer that performs simulated annealing.
	 */
	public static final SimulatedAnnealing createSimulatedAnnealing(
	    AbstractOptimizationProblem problem, int popsize, double alpha,
	    double temperature, InterfaceMutation mut,
	    InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(mut);
		tmpIndi.setMutationProbability(1.0);

		SimulatedAnnealing sa = new SimulatedAnnealing();
		sa.setAlpha(alpha);
		sa.setInitialTemperature(temperature);
		sa.SetProblem(problem);
		sa.getPopulation().setPopulationSize(popsize);
		sa.addPopulationChangedEventListener(listener);
		sa.init();

		listener.registerPopulationStateChanged(sa.getPopulation(), "");

		return sa;
	}

	// /////////////////////////// Termination criteria
	public static InterfaceTerminator defaultTerminator() {
		if (term == null) term = new EvaluationTerminator(defaultFitCalls);
		return term;
	}

	/**
	 * The default Terminator finishes after n fitness calls, the default n is
	 * returned here.
	 *
	 * @return the default number of fitness call done before termination
	 */
	public static final int getDefaultFitCalls() {
		return defaultFitCalls;
	}

	// /////////////////////////// constructing a default OptimizerRunnable

	public static GOParameters getParams(final int optType,
	    AbstractOptimizationProblem problem) {
		switch (optType) {
		case STD_ES:
			return standardES(problem);
		case CMA_ES:
			return cmaES(problem);
		case STD_GA:
			return standardGA(problem);
		case PSO:
			return standardPSO(problem);
		case DE:
			return standardDE(problem);
		case TRIBES:
			return tribes(problem);
		case RANDOM:
			return monteCarlo(problem);
		case HILLCL:
			return hillClimbing(problem);
		case CBN_ES:
			return cbnES(problem);
		case CL_HILLCL:
			return clusteringHillClimbing(problem);
		default:
			System.err.println("Error: optimizer type " + optType + " is unknown!");
			return null;
		}
	}

	public static OptimizerRunnable getOptRunnable(final int optType,
	    AbstractOptimizationProblem problem, int fitCalls, String outputFilePrefix) {
		OptimizerRunnable opt = null;
		GOParameters params = getParams(optType, problem);
		if (params != null) {
			opt = new OptimizerRunnable(params, outputFilePrefix);
			if (fitCalls != defaultFitCalls)
			  opt.getGOParams().setTerminator(new EvaluationTerminator(fitCalls));
		}
		return opt;
	}

	// /////////////////////////// constructing a default OptimizerRunnable
	public static OptimizerRunnable getOptRunnable(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		return getOptRunnable(optType, problem, defaultFitCalls, outputFilePrefix);
	}

	public static InterfaceTerminator getTerminator() {
		return OptimizerFactory.term;
	}

	public static final GOParameters hillClimbing(
	    AbstractOptimizationProblem problem) {
		HillClimbing hc = new HillClimbing();
		hc.SetProblem(problem);
		Population pop = new Population();
		pop.setPopulationSize(50);
		problem.initPopulation(pop);
		return makeParams(hc, pop, problem, randSeed, defaultTerminator());
	}

	public static int lastEvalsPerformed() {
		return (lastRunnable != null) ? lastRunnable.getProgress() : -1;
	}

	// /////////////////////// Creating default strategies
	public static GOParameters makeParams(InterfaceOptimizer opt, Population pop,
	    AbstractOptimizationProblem problem, long seed, InterfaceTerminator term) {
		GOParameters params = new GOParameters();
		params.setProblem(problem);
		opt.SetProblem(problem);
		opt.setPopulation(pop);
		params.setOptimizer(opt);
		params.setTerminator(term);
		params.setSeed(seed);
		return params;
	}

	public static final GOParameters monteCarlo(
	    AbstractOptimizationProblem problem) {
		MonteCarloSearch mc = new MonteCarloSearch();
		Population pop = new Population();
		pop.setPopulationSize(50);
		problem.initPopulation(pop);
		return makeParams(mc, pop, problem, randSeed, defaultTerminator());
	}

	// TODO hier weiter kommentieren
	public static OptimizerRunnable optimize(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		return optimize(getOptRunnable(optType, problem, outputFilePrefix));
	}

	public static OptimizerRunnable optimize(OptimizerRunnable runnable) {
		if (runnable == null) return null;
		new Thread(runnable).run();
		lastRunnable = runnable;
		return runnable;
	}

	/**
	 * Create a runnable optimization Runnable and directly start it in an own
	 * thread. The Runnable will notify waiting threads and set the isFinished
	 * flag when the optimization is complete. If the optType is invalid, null
	 * will be returned.
	 *
	 * @param optType
	 * @param problem
	 * @param outputFilePrefix
	 * @return
	 */
	public static OptimizerRunnable optimizeInThread(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = getOptRunnable(optType, problem,
		    outputFilePrefix);
		if (runnable != null) new Thread(runnable).start();
		return runnable;
	}

	// ///////////////////////////// Optimize a given parameter instance
	public static BitSet optimizeToBinary(GOParameters params,
	    String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
		    outputFilePrefix));
		return runnable.getBinarySolution();
	}

	// ///////////////////////////// Optimize using a default strategy
	public static BitSet optimizeToBinary(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem, outputFilePrefix);
		return (runnable != null) ? runnable.getBinarySolution() : null;
	}

	// ///////////////////////////// Optimize a given runnable
	public static BitSet optimizeToBinary(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getBinarySolution() : null;
	}

	public static double[] optimizeToDouble(GOParameters params,
	    String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
		    outputFilePrefix));
		return runnable.getDoubleSolution();
	}

	public static double[] optimizeToDouble(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem, outputFilePrefix);
		return (runnable != null) ? runnable.getDoubleSolution() : null;
	}

	public static double[] optimizeToDouble(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getDoubleSolution() : null;
	}

	public static IndividualInterface optimizeToInd(GOParameters params,
	    String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
		    outputFilePrefix));
		return runnable.getResult();
	}

	public static IndividualInterface optimizeToInd(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem, outputFilePrefix);
		return (runnable != null) ? runnable.getResult() : null;
	}

	public static IndividualInterface optimizeToInd(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getResult() : null;
	}

	public static Population optimizeToPop(GOParameters params,
	    String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
		    outputFilePrefix));
		return runnable.getSolutionSet();
	}

	public static Population optimizeToPop(final int optType,
	    AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem, outputFilePrefix);
		return (runnable != null) ? runnable.getSolutionSet() : null;
	}

	public static Population optimizeToPop(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getSolutionSet() : null;
	}

	public static Population postProcess(int steps, double sigma, int nBest) {
		return (lastRunnable == null) ? null : postProcess(lastRunnable,
		    new PostProcessParams(steps, sigma, nBest));
	}

	public static Population postProcess(InterfacePostProcessParams ppp) {
		return (lastRunnable == null) ? null : postProcess(lastRunnable, ppp);
	}

	public static Population postProcess(OptimizerRunnable runnable, int steps,
	    double sigma, int nBest) {
		PostProcessParams ppp = new PostProcessParams(steps, sigma, nBest);
		return postProcess(runnable, ppp);
	}

	public static Population postProcess(OptimizerRunnable runnable,
	    InterfacePostProcessParams ppp) {
		runnable.setDoRestart(true);
		runnable.setDoPostProcessOnly(true);
		runnable.setPostProcessingParams(ppp);
		runnable.run(); // this run will not set the lastRunnable - postProcessing
		// starts always anew
		return runnable.getSolutionSet();
	}

	public static Vector<BitSet> postProcessBinVec(int steps, double sigma,
	    int nBest) {
		return (lastRunnable != null) ? postProcessBinVec(lastRunnable,
		    new PostProcessParams(steps, sigma, nBest)) : null;
	}

	public static Vector<BitSet> postProcessBinVec(InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessBinVec(lastRunnable, ppp) : null;
	}

	public static Vector<BitSet> postProcessBinVec(OptimizerRunnable runnable,
	    int steps, double sigma, int nBest) {
		return postProcessBinVec(runnable, new PostProcessParams(steps, sigma,
		    nBest));
	}

	public static Vector<BitSet> postProcessBinVec(OptimizerRunnable runnable,
	    InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<BitSet> ret = new Vector<BitSet>(resPop.size());
		for (Object o : resPop) {
			if (o instanceof InterfaceDataTypeBinary) {
				InterfaceDataTypeBinary indy = (InterfaceDataTypeBinary) o;
				ret.add(indy.getBinaryData());
			}
		}
		return ret;
	}

	public static Vector<double[]> postProcessDblVec(int steps, double sigma,
	    int nBest) {
		return (lastRunnable == null) ? null : postProcessDblVec(lastRunnable,
		    new PostProcessParams(steps, sigma, nBest));
	}

	public static Vector<double[]> postProcessDblVec(
	    InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessDblVec(lastRunnable, ppp) : null;
	}

	public static Vector<double[]> postProcessDblVec(OptimizerRunnable runnable,
	    int steps, double sigma, int nBest) {
		return postProcessDblVec(runnable, new PostProcessParams(steps, sigma,
		    nBest));
	}

	public static Vector<double[]> postProcessDblVec(OptimizerRunnable runnable,
	    InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<double[]> ret = new Vector<double[]>(resPop.size());
		for (Object o : resPop) {
			if (o instanceof InterfaceDataTypeDouble) {
				InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble) o;
				ret.add(indy.getDoubleData());
			}
		}
		return ret;
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(int steps,
	    double sigma, int nBest) {
		return (lastRunnable != null) ? postProcessIndVec(lastRunnable,
		    new PostProcessParams(steps, sigma, nBest)) : null;
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(
	    InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessIndVec(lastRunnable, ppp) : null;
	}

	// /////////////////////////// post processing
	public static Vector<AbstractEAIndividual> postProcessIndVec(
	    OptimizerRunnable runnable, int steps, double sigma, int nBest) {
		return postProcessIndVec(runnable, new PostProcessParams(steps, sigma,
		    nBest));
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(
	    OptimizerRunnable runnable, InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<AbstractEAIndividual> ret = new Vector<AbstractEAIndividual>(resPop
		    .size());
		for (Object o : resPop) {
			if (o instanceof AbstractEAIndividual) {
				AbstractEAIndividual indy = (AbstractEAIndividual) o;
				ret.add(indy);
			}
		}
		return ret;
	}

	public static void setEvaluationTerminator(int maxEvals) {
		setTerminator(new EvaluationTerminator(maxEvals));
	}

	public static void setFitnessConvergenceTerminator(double fitThresh) {
		setTerminator(new FitnessConvergenceTerminator(fitThresh, 100, true, true));
	}

	public static void setTerminator(InterfaceTerminator term) {
		OptimizerFactory.term = term;
	}

	/**
	 * Return a simple String showing the accessible optimizers. For external
	 * access."
	 *
	 * @return a String listing the accessible optimizers
	 */
	public static String showOptimizers() {
		return "1: Standard ES \n2: CMA-ES \n3: GA \n4: PSO \n5: DE \n6: Tribes \n7: Random (Monte Carlo) "
		    + "\n8: Hill-Climbing \n9: Cluster-based niching ES \n10: Clustering Hill-Climbing";
	}

	public static final GOParameters standardDE(
	    AbstractOptimizationProblem problem) {
		DifferentialEvolution de = new DifferentialEvolution();
		Population pop = new Population();
		pop.setPopulationSize(50);
		de.setPopulation(pop);
		de.getDEType().setSelectedTag(1); // this sets current-to-best
		de.setF(0.8);
		de.setK(0.6);
		de.setLambda(0.6);
		de.setMt(0.05);

		return makeParams(de, pop, problem, randSeed, defaultTerminator());
	}

	public static final GOParameters standardES(
	    AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);

		AbstractEAIndividual indy = problem.getIndividualTemplate();

		if ((indy != null) && (indy instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			indy.setMutationOperator(new MutateESGlobal());
			indy.setCrossoverOperator(new CrossoverESDefault());
		} else {
			System.err
			    .println("Error, standard ES is implemented for ES individuals only (requires double data types)");
			return null;
		}

		Population pop = new Population();
		pop.setPopulationSize(es.getLambda());

		return makeParams(es, pop, problem, randSeed, defaultTerminator());
	}

	public static final GOParameters standardGA(
	    AbstractOptimizationProblem problem) {
		GeneticAlgorithm ga = new GeneticAlgorithm();
		Population pop = new Population();
		pop.setPopulationSize(100);
		ga.setPopulation(pop);
		ga.setElitism(true);

		return makeParams(ga, pop, problem, randSeed, defaultTerminator());
	}

	public static final GOParameters standardPSO(
	    AbstractOptimizationProblem problem) {
		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
		Population pop = new Population();
		pop.setPopulationSize(30);
		pso.setPopulation(pop);
		pso.setPhiValues(2.05, 2.05);
		pso.getTopology().setSelectedTag("Grid");
		return makeParams(pso, pop, problem, randSeed, defaultTerminator());
	}

	public static String terminatedBecause() {
		return (lastRunnable != null) ? lastRunnable.terminatedBecause() : null;
	}

	public static final GOParameters tribes(AbstractOptimizationProblem problem) {
		Tribes tr = new Tribes();
		Population pop = new Population();
		pop.setPopulationSize(1); // only for init
		problem.initPopulation(pop);
		return makeParams(tr, pop, problem, randSeed, defaultTerminator());
	}
}