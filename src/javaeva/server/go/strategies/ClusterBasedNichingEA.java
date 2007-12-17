package javaeva.server.go.strategies;

import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.operators.cluster.ClusteringDensityBased;
import javaeva.server.go.operators.cluster.InterfaceClustering;
import javaeva.server.go.operators.mutation.InterfaceMutation;
import javaeva.server.go.operators.mutation.MutateESGlobal;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.Interface2DBorderProblem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.problems.TF1Problem;
import javaeva.server.go.tools.RandomNumberGenerator;
import javaeva.gui.*;
import java.util.ArrayList;
import wsi.ra.chart2d.DPointSet;
import wsi.ra.chart2d.DPoint;
import wsi.ra.chart2d.DPointIcon;

/** The infamuos clustering based niching EA, still under construction.
 * It should be able to identify and track multiple global/local optima
 * at the same time, but currently i'm not sure what size the subpopulations
 * is. It is straightforward with a GA but in case of an ES i've changed the
 * interpretation of the population size and i guess that the mu/lambda ratio
 * is currently lost.. i'll have to fix that some day.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 322 $
 *            $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007) $
 *            $Author: mkron $
 */

public class ClusterBasedNichingEA implements InterfacePopulationChangedEventListener, InterfaceOptimizer, java.io.Serializable {

    private Population                      m_Population                    = new Population();
    public ArrayList<Population>			m_Species                       = new ArrayList<Population>();
    public Population                       m_Undifferentiated              = new Population();
    private InterfaceOptimizationProblem    m_Problem                       = new B1Problem();
    private InterfaceOptimizer              m_Optimizer                     = new GeneticAlgorithm();
    private InterfaceClustering             m_CAForSpeciesDifferentation    = new ClusteringDensityBased();
    private InterfaceClustering             m_CAForSpeciesConvergence       = new ClusteringDensityBased();

    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    private int                             m_SpeciesCycle                  = 1;
    // from which size on is a species considered active 
    private int 							m_actSpecSize					= 2;
    private int 							m_minGroupSize					= 3;
    private boolean                         m_UseClearing                   = false;
    private boolean                         m_UseSpeciesDifferentation      = true;
    private boolean                         m_UseSpeciesConvergence         = true;
    private boolean                         m_UseHaltingWindow              = true;
    private int                             m_PopulationSize                = 50;

    private boolean                         m_Debug     = true;
    private int                             m_ShowCycle = 2;
    private TopoPlot                        m_Topology;
    private int                 			haltingWindow         			 = 15;


    public ClusterBasedNichingEA() {
        this.m_CAForSpeciesConvergence = new ClusteringDensityBased();
        ((ClusteringDensityBased)this.m_CAForSpeciesConvergence).setMinimumGroupSize(m_minGroupSize);
    }

    public ClusterBasedNichingEA(ClusterBasedNichingEA a) {    
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Optimizer                    = (InterfaceOptimizer)a.m_Optimizer.clone();
        this.m_Species                      = (ArrayList)(a.m_Species.clone());
        this.m_Undifferentiated             = (Population)a.m_Undifferentiated.clone();
        this.m_CAForSpeciesConvergence      = (InterfaceClustering)this.m_CAForSpeciesConvergence.clone();
        this.m_CAForSpeciesDifferentation   = (InterfaceClustering)this.m_CAForSpeciesDifferentation.clone();
        this.m_SpeciesCycle                 = a.m_SpeciesCycle;
        this.m_actSpecSize					= a.m_actSpecSize;
        this.m_minGroupSize					= a.m_minGroupSize;
        this.m_UseClearing                  = a.m_UseClearing;
        this.m_UseSpeciesDifferentation     = a.m_UseSpeciesDifferentation;
        this.m_UseSpeciesConvergence        = a.m_UseSpeciesConvergence;
        this.m_UseHaltingWindow             = a.m_UseHaltingWindow;
        this.m_PopulationSize               = a.m_PopulationSize;
    }

    public Object clone() {
        return (Object) new ClusterBasedNichingEA(this);
    }

    public void init() {
        this.m_Optimizer.addPopulationChangedEventListener(this);
        this.m_Undifferentiated = new Population();
        this.m_Undifferentiated.setPopulationSize(this.m_PopulationSize);
        this.m_Species = new ArrayList<Population>();
        this.m_Problem.initPopulation(this.m_Undifferentiated);
        this.evaluatePopulation(this.m_Undifferentiated);
        this.firePropertyChangedEvent("FirstGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Optimizer.addPopulationChangedEventListener(this);
        this.m_Undifferentiated = (Population)pop.clone();
        if (reset) this.m_Undifferentiated.init();
        this.m_Undifferentiated.setPopulationSize(this.m_PopulationSize);
        this.m_Species = new ArrayList<Population>();
        this.evaluatePopulation(this.m_Undifferentiated);
        this.firePropertyChangedEvent("FirstGenerationPerformed");
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

    private void plot() {
        double[]   a = new double[2];
        a[0] = 0.0;
        a[1] = 0.0;
        if (this.m_Problem instanceof TF1Problem) {
            // now i need to plot the pareto fronts
            Plot plot = new Plot("TF3Problem", "y1", "y2", a, a);
            plot.setUnconnectedPoint(0,0,0);
            plot.setUnconnectedPoint(1,5,0);
            GraphPointSet   mySet = new GraphPointSet(10, plot.getFunctionArea());
            DPoint          point;
            mySet.setConnectedMode(false);
            for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                AbstractEAIndividual indy = (AbstractEAIndividual)this.m_Undifferentiated.get(i);
                double [] d = indy.getFitness();
                point = new DPoint(d[0], d[1]);
                point.setIcon(new Chart2DDPointIconCircle());
                mySet.addDPoint(point);
            }
            for (int i = 0; i < this.m_Species.size(); i++) {
                mySet = new GraphPointSet(10+i, plot.getFunctionArea());
                mySet.setConnectedMode(false);
                Population pop = (Population)this.m_Species.get(i);
//                ArchivingAllDomiating arch = new ArchivingAllDomiating();
//                arch.plotParetoFront(pop, plot);
                for (int j = 0; j < pop.size(); j++) {
                    AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(j);
                    double [] d = indy.getFitness();
                    point = new DPoint(d[0], d[1]);
                    point.setIcon(new Chart2DDPointIconText("P"+j));
                    mySet.addDPoint(point);
                }

            }

        }
        if (this.m_Problem instanceof Interface2DBorderProblem) {
            DPointSet               popRep  = new DPointSet();
            InterfaceDataTypeDouble tmpIndy1, best;
            Population              pop;

            this.m_Topology          = new TopoPlot("CBN-Species","x","y",a,a);
            this.m_Topology.gridx = 60;
            this.m_Topology.gridy = 60;
            this.m_Topology.setTopology((Interface2DBorderProblem)this.m_Problem);
            //draw the undifferentiated
            for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                tmpIndy1 = (InterfaceDataTypeDouble)this.m_Undifferentiated.get(i);
                popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
            }
            this.m_Topology.getFunctionArea().addDElement(popRep);
            //draw the species
            for (int i = 0; i < this.m_Species.size(); i++) {
                pop = (Population)this.m_Species.get(i);
                best = (InterfaceDataTypeDouble)pop.getBestIndividual();
                if (isActive(pop)) {
                    for (int j = 0; j < pop.size(); j++) {
                        popRep = new DPointSet();
                        tmpIndy1 = (InterfaceDataTypeDouble)pop.get(j);
                        popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                        this.m_Topology.getFunctionArea().addDElement(popRep);

                        popRep      = new DPointSet();
                        popRep.setConnected(true);
                        popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                        popRep.addDPoint(new DPoint(best.getDoubleData()[0], best.getDoubleData()[1]));
                        this.m_Topology.getFunctionArea().addDElement(popRep);
                    }
//                    popRep = new DPointSet();
//                    tmpIndy1 = (InterfaceDataTypeDouble)pop.getBestEAIndividual();
//                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
//                    double d = Math.round(100*((AbstractEAIndividual)tmpIndy1).getFitness(0))/(double)100;
//                    DPointIcon icon = new Chart2DDPointIconText(""+d);
//                    ((Chart2DDPointIconText)icon).setIcon(new Chart2DDPointIconCircle());
//                    popRep.setIcon(icon);
//                    this.m_Topology.m_PlotArea.addDElement(popRep);
                } else {
                    // this is an inactive species
                    popRep = new DPointSet();
                    tmpIndy1 = (InterfaceDataTypeDouble)pop.get(0);
                    popRep.addDPoint(new DPoint(tmpIndy1.getDoubleData()[0], tmpIndy1.getDoubleData()[1]));
                    double d = Math.round(100*((AbstractEAIndividual)tmpIndy1).getFitness(0))/(double)100;
                    DPointIcon icon = new Chart2DDPointIconText(""+d);
                    ((Chart2DDPointIconText)icon).setIcon(new Chart2DDPointIconCircle());
                    popRep.setIcon(icon);
                    this.m_Topology.getFunctionArea().addDElement(popRep);
                }
            }
        }
    }

    /** 
     * This method is used to cap the mutation rate.
     * For the global ES mutation, set the mutation rate to not more than cap. 
     * 
     * @param pop   The population
     * @param cap   The maximum mutation rate
     */
    private void capMutationRate(Population pop, double cap) {
        AbstractEAIndividual    indy;
        InterfaceMutation       mutator;

        if (cap <= 0) return;
        for (int i = 0; i < pop.size(); i++) {
            indy = (AbstractEAIndividual) pop.get(i);
            mutator = indy.getMutationOperator();
            if (mutator instanceof MutateESGlobal) {
                ((MutateESGlobal)mutator).setMutationStepSize(Math.min(cap, ((MutateESGlobal)mutator).getMutationStepSize()));
            }
        }
    }

    /** This method is called to generate n freshly initialized individuals
     * @param n     Number of new individuals
     * @return A population of new individuals
     */
    private Population initializeIndividuals(int n) {
        Population result = new Population();
        result.setPopulationSize(n);
        //@todo: crossover between species is to be impelemented
        this.m_Problem.initPopulation(result);
        this.m_Problem.evaluate(result);
        this.capMutationRate(result, RandomNumberGenerator.randomDouble(0.001, 0.1));
        return result;
    }

    /** 
     * This method checks whether a species is converged. 
     * @param pop   The species to test
     * @return True if converged.
     */
    private boolean testSpeciesForConvergence(Population pop) {
        ArrayList tmpA = pop.m_History;
        int     length = pop.m_History.size();

        if (length <= haltingWindow) return false;
        else {
            AbstractEAIndividual historic = ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow));
            for (int i = 1; i < haltingWindow; i++) {
                if (historic.getFitness(0) > ((AbstractEAIndividual)pop.m_History.get(length-haltingWindow+i)).getFitness(0)) {
                    //System.out.println("( " + historic.getFitness(0) + "/" + ((AbstractEAIndividual)pop.m_History.get(length-hw+i)).getFitness(0));
                    return false;
                }
            }
        }
        if (this.m_Debug) {
            double[] a = new double[2];
            a[0] = 0; a[1] = 0;
            Plot plot = new Plot("HaltingWindow", "History", "Fitness", a, a);
            for (int i = 0; i < tmpA.size(); i++) {
                a = ((AbstractEAIndividual)tmpA.get(i)).getFitness();
                plot.setUnconnectedPoint(i, a[0], 0);
            }
        }
        return true;
    }

    public void optimize() {
    	// plot the populations
    	if (this.m_ShowCycle > 0) {
            if ((this.m_Undifferentiated.getGeneration() == 0) || (this.m_Undifferentiated.getGeneration() == 1) || (this.m_Undifferentiated.getGeneration() == 2)) {
                this.plot();
            } else {
                if (this.m_Undifferentiated.getGeneration()%this.m_ShowCycle == 0) this.plot();
            }
        }

        // species evolution phase
        if (this.m_Debug) {
            System.out.println("");
            System.out.println("Optimizing Generation " + this.m_Undifferentiated.getGeneration());
        }

        // optimize D_0
        this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.size());
        if (isActive(m_Undifferentiated)) {
            this.capMutationRate(this.m_Undifferentiated, 0); // MK TODO this sets mutation rate to 0! why?
            this.m_Optimizer.setPopulation(this.m_Undifferentiated);
            this.m_Optimizer.optimize();
            this.m_Undifferentiated = this.m_Optimizer.getPopulation();
        } else {
            this.m_Undifferentiated.incrGeneration();
        }

        // now the population is of max size 
        if (this.m_Debug) {
            System.out.println("-Undiff. size: " + this.m_Undifferentiated.size());
            System.out.println("-Number of Demes: " + this.m_Species.size());
        }

        int reinitCount;
        Population curSpecies;
        // optimize the clustered species
        for (int i = 0; i < this.m_Species.size(); i++) {
            if (this.m_Debug) System.out.println("-Deme " + i + " size: " + ((Population)this.m_Species.get(i)).size());
            curSpecies = ((Population)this.m_Species.get(i));
            curSpecies.SetFunctionCalls(0);
            curSpecies.setPopulationSize(curSpecies.size());
            reinitCount = curSpecies.size()-1;
            if (isActive(curSpecies)) {
                if ((this.m_UseHaltingWindow) && (this.testSpeciesForConvergence(curSpecies))) {
///////////////////////////////////////////// Halting Window /////////////////////////////////////////////////
                    if (this.m_Debug) System.out.println("--Converged");
//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
                    // memorize the best one....
                    AbstractEAIndividual best = (AbstractEAIndividual)curSpecies.getBestEAIndividual().getClone();
                    // now reset the converged species to inactivity size = 1
                    curSpecies.setPopulationSize(1);
                    curSpecies.clear();
                    curSpecies.add(best);
                    // reinit the surplus individuals and add these new individuals to undifferentiated
                    this.m_Undifferentiated.addPopulation(this.initializeIndividuals(reinitCount));
                    this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()+reinitCount);
//                    if (this.m_Debug) {
//                        System.out.println("Undiff.Size: " + this.m_Undifferentiated.size() +"/"+this.m_Undifferentiated.getPopulationSize());
//                        System.out.println("Diff.Size  : " + ((Population)this.m_Species.get(i)).size() +"/"+((Population)this.m_Species.get(i)).getPopulationSize());
//                    }
//                    if (this.m_Debug) System.out.println("--------------------------End converged");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
                } else {
                	// actually optimize D_i
                    this.capMutationRate(curSpecies, 0.05);
                    this.m_Optimizer.setPopulation(curSpecies);
                    this.m_Optimizer.optimize();
                    this.m_Species.set(i, this.m_Optimizer.getPopulation());
                    curSpecies = ((Population)this.m_Species.get(i)); // reset to expected population, just to be sure
                }
            } else {
                // a single individual species, this element is inactive
            }
            // This is necessary to keep track to the function calls needed
            this.m_Undifferentiated.SetFunctionCalls(this.m_Undifferentiated.getFunctionCalls() + curSpecies.getFunctionCalls());
        }
        if (this.m_Debug) System.out.println("-Number of Demes: " + this.m_Species.size());

        // possible species differentation and convergence
        if (this.m_Undifferentiated.getGeneration()%this.m_SpeciesCycle == 0) {
            if (this.m_Debug) System.out.println("Species cycle:");
            
            if (this.m_UseSpeciesDifferentation) {
                // species differentation phase
                if (this.m_Debug) System.out.println("-Sepecies Differentation:");
                Population[]    ClusterResult;
                ArrayList<Population>       newSpecies = new ArrayList<Population>();
                //cluster the undifferentiated population
                ClusterResult   = this.m_CAForSpeciesDifferentation.cluster(this.m_Undifferentiated);
                this.m_Undifferentiated = ClusterResult[0];
                for (int j = 1; j < ClusterResult.length; j++) {
                    ClusterResult[j].m_History = new ArrayList();
                    newSpecies.add(ClusterResult[j]);
                }
                for (int i = 0; i < this.m_Species.size(); i++) {
                    if (isActive(this.m_Species.get(i))) {
                        // only active populations are clustered
                        ClusterResult = this.m_CAForSpeciesDifferentation.cluster((Population)this.m_Species.get(i));
                        this.m_Undifferentiated.addPopulation(ClusterResult[0]);
                        this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize() + ClusterResult[0].size());
                        for (int j = 1; j < ClusterResult.length; j++) {
                            ClusterResult[j].setPopulationSize(ClusterResult[j].size());
                            if (ClusterResult.length > 2) ClusterResult[j].m_History = new ArrayList();
                            newSpecies.add(ClusterResult[j]);
                        }
                    } else {
                        // inactive populations are added directly
                        newSpecies.add(this.m_Species.get(i));
                    }
                }
                this.m_Species = newSpecies;
                if (this.m_Debug) {
                    System.out.println("--Number of species: " + this.m_Species.size());
                    System.out.println("---Undiff size: " + this.m_Undifferentiated.size());
                    for (int i = 0; i < this.m_Species.size(); i++) {
                        System.out.println("---Deme " + i + " size: " + ((Population)this.m_Species.get(i)).size());
                    }
                }
                //if (this.m_Show) this.plot();
            }

            if (this.m_UseSpeciesConvergence) {
                //species convergence phase
                if (this.m_Debug) System.out.println("-Species convergence:");
                
                // first test if loners belong to any species
                boolean found = false;
                for (int i = 0; i < this.m_Undifferentiated.size(); i++) {
                	int j=0;
                	while (!found && j<m_Species.size()) {
                    //for (int j = 0; j < this.m_Species.size(); j++) {
                    	curSpecies = (Population)this.m_Species.get(j);
                        AbstractEAIndividual tmpIndy = (AbstractEAIndividual)this.m_Undifferentiated.get(i);
                        if (this.m_CAForSpeciesConvergence.belongsToSpecies(tmpIndy, curSpecies)) {
                            if (this.m_Debug) System.out.println("--Adding loner to species "+j);
                            found = true;
                            this.m_Undifferentiated.remove(i);
                            if (isActive(curSpecies)) {
                                curSpecies.add(tmpIndy);
                                this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()-1);
                                i--; // needs to be reduced because D0 size has decreased
                            } else {
                                // the species is inactive, reinitialize the individual (MK: why?)
                                this.m_Undifferentiated.add(i, this.initializeIndividuals(1).get(0));
                            }
                        }
                        j++;
                    }
                }
                // Now test if species converge
                Population spec1, spec2;
                for (int i = 0; i < this.m_Species.size(); i++) {
                    spec1 = (Population)this.m_Species.get(i);
                    for (int j = i+1; j < this.m_Species.size(); j++) {
                        spec2 = (Population)this.m_Species.get(j);
                        if (this.m_CAForSpeciesConvergence.convergingSpecies(spec1, spec2)) {
                            if (this.m_Debug) System.out.println("--------------------Merging species (" + i +", " +j +") ["+spec1.size()+"/"+spec2.size()+"]");
                            if (isActive(spec1) && isActive(spec2)) {
                                if (this.m_Debug) System.out.println("---Active merge");
                                
                                spec1.addPopulation(spec2);
                                if (spec2.m_History.size() > spec1.m_History.size()) spec1.m_History = spec2.m_History;
                                this.m_Species.remove(j);
                                j--;
                            } else {
                                if (this.m_Debug) System.out.println("---Inactive merge");
                                // save best in singular species and reinit the rest of the individuals
                                spec1.addPopulation(spec2);
                                this.m_Species.remove(j);
                                j--;
                                AbstractEAIndividual best = (AbstractEAIndividual)spec1.getBestEAIndividual().getClone();
                                reinitCount = spec1.size()-1;
                                // now reset the converged species to inactivity size = 0
                                spec1.setPopulationSize(1);
                                spec1.clear();
                                spec1.add(best);
                                // reinitialized individuals and add them to undifferentiated
                                this.m_Undifferentiated.addPopulation(this.initializeIndividuals(reinitCount));
                                this.m_Undifferentiated.setPopulationSize(this.m_Undifferentiated.getPopulationSize()+reinitCount);
                            }
                        }
                    }
                }
                if (this.m_Debug) System.out.println("--Number of species: " + this.m_Species.size());
            }

//            if (this.m_UseClearing) {
//                //@todo
//                if (this.m_Debug) System.out.println("-Clearing applied:");
//                for (int i = 0; i < this.m_Species.size(); i++) {
//                    this.m_Undifferentiated.add(((Population)this.m_Species.get(i)).getBestEAIndividual());
//                }
//                this.m_Species      = new ArrayList();
//                Population tmpPop   = new Population();
//                tmpPop.setPopulationSize(this.m_Undifferentiated.getPopulationSize() - this.m_Undifferentiated.size());
//                this.m_Problem.initPopulation(tmpPop);
//                this.evaluatePopulation(tmpPop);
//                this.m_Undifferentiated.addPopulation(tmpPop);
//            }
        }
        // output the result
        if (this.m_Debug) System.out.println("-Number of species: " + this.m_Species.size());
        this.m_Population = new Population();
        this.m_Population = (Population)this.m_Undifferentiated.clone();
        if (this.m_Debug) System.out.println("initing with " + this.m_Undifferentiated.size());
        for (int i = 0; i < this.m_Species.size(); i++) {
            if (this.m_Debug) System.out.println("Adding deme " + i + " with size " + ((Population)this.m_Species.get(i)).size());
            this.m_Population.addPopulation((Population)this.m_Species.get(i));
        }
        if (this.m_Debug) System.out.println("Population size: " + this.m_Population.size());
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /**
     * Return true if the given population is considered active.
     *  
     * @param pop	a population
     * @return true, if pop is considered an active population, else false
     */
    protected boolean isActive(Population pop) {
    	return (pop.size() >= m_actSpecSize);
    }
    
    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name) {
        //Population population = ((InterfaceOptimizer)source).getPopulation();
    }
    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
        this.m_Optimizer.SetProblem(this.m_Problem);
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "Genetic Algorithm:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }

    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {

    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a versatible species based niching EA method.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "CBN-EA";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        this.m_Population = (Population)this.m_Undifferentiated.clone();
        for (int i = 0; i < this.m_Species.size(); i++) this.m_Population.addPopulation((Population)this.m_Species.get(i));
        return this.m_Population;
    }
    public void setPopulation(Population pop){
        this.m_Undifferentiated = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** This method allows you to set/get the switch that toggels the use
     * of species differentation.
     * @return The current status of this flag
     */
    public boolean getApplyDifferentation() {
        return this.m_UseSpeciesDifferentation;
    }
    public void setApplyDifferentation(boolean b){
        this.m_UseSpeciesDifferentation = b;
    }
    public String applyDifferentationTipText() {
        return "Toggle the species differentation mechanism.";
    }

//    /** Clearing removes all but the best individuals from an identified species.
//     * @return The current status of this flag
//     */
//    public boolean getApplyClearing() {
//        return this.m_UseClearing;
//    }
//    public void setApplyClearing(boolean b){
//        this.m_UseClearing = b;
//    }
//    public String applyClearingTipText() {
//        return "Clearing removes all but the best individuals from an identified species.";
//    }

    /** This method allows you to toggle the use of the halting window.
     * @return The current status of this flag
     */
    public boolean getUseHaltingWindow() {
        return this.m_UseHaltingWindow;
    }
    public void setUseHaltingWindow(boolean b){
        this.m_UseHaltingWindow = b;
    }
    public String useHaltingWindowTipText() {
        return "With a halting window converged species are freezed.";
    }

    /** This method allows you to set/get the switch that toggels the use
     * of species convergence.
     * @return The current status of this flag
     */
    public boolean getApplyConvergence() {
        return this.m_UseSpeciesConvergence;
    }
    public void setApplyConvergence(boolean b){
        this.m_UseSpeciesConvergence = b;
    }
    public String applyConvergenceTipText() {
        return "Toggle the species convergence mechanism.";
    }

    /** Choose a population based optimizing technique to use
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }
    public void setOptimizer(InterfaceOptimizer b){
        this.m_Optimizer = b;
    }
    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** The cluster algorithm on which the species differentation is based
     * @return The current clustering method
     */
    public InterfaceClustering getDifferentationCA() {
        return this.m_CAForSpeciesDifferentation;
    }
    public void setDifferentationCA(InterfaceClustering b){
        this.m_CAForSpeciesDifferentation = b;
    }
    public String differentationCATipText() {
        return "The cluster algorithm on which the species differentation is based.";
    }

    /** The Cluster Algorithm on which the species convergence is based.
     * @return The current clustering method
     */
    public InterfaceClustering getConvergenceCA() {
        return this.m_CAForSpeciesConvergence;
    }
    public void setConvergenceCA(InterfaceClustering b){
        this.m_CAForSpeciesConvergence = b;
    }
    public String convergenceCATipText() {
        return "The cluster algorithm on which the species convergence is based.";
    }

    /** Determines how often species differentation/convergence is performed.
     * @return This number gives the generations when specification is performed.
     */
    public int getSpeciesCycle() {
        return this.m_SpeciesCycle;
    }
    public void setSpeciesCycle(int b){
        this.m_SpeciesCycle = b;
    }
    public String speciesCycleTipText() {
        return "Determines how often species differentation/convergence is performed.";
    }

    /** TDetermines how often show is performed.
     * @return This number gives the generations when specification is performed.
     */
    public int getShowCycle() {
        return this.m_ShowCycle;
    }
    public void setShowCycle(int b){
        this.m_ShowCycle = b;
    }
    public String showCycleTipText() {
        return "Determines how often show is performed.";
    }
    /** Determines the size of the initial population.
     * @return This number gives initial population size.
     */
    public int getPopulationSize() {
        return this.m_PopulationSize;
    }
    public void setPopulationSize(int b){
        this.m_PopulationSize = b;
    }
    public String populationSizeTipText() {
        return "Determines the size of the initial population.";
    }
}