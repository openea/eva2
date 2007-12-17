package javaeva.server.stat;

/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 284 $
 *            $Date: 2007-11-27 14:37:05 +0100 (Tue, 27 Nov 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.Serializable;
import javaeva.tools.Serializer;
import javaeva.tools.Tag;
import javaeva.tools.SelectedTag;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class StatisticsParameterImpl implements StatisticsParameter, Serializable {
  public final static int PLOT_BEST = 0;
  public final static int PLOT_WORST = 1;
  public final static int PLOT_BEST_AND_WORST = 2;
  public final static Tag[] TAGS_PLOT_FITNESS = {
                                                new Tag(PLOT_BEST, "plot best of population"),
                                                new Tag(PLOT_WORST, "plot worst of population"),
                                                new Tag(PLOT_BEST_AND_WORST, "plot best and worst of population")
  };
  private int m_PlotFitness = PLOT_BEST;

  private int m_Textoutput = 0;
  private int m_Plotoutput = 1;
  private int m_MultiRuns = 1;
  private String m_ResultFileName = "none";
  protected String m_Name = "not defined";
  protected String m_InfoString = "";
  private boolean m_useStatPlot = true;
  private double m_ConvergenceRateThreshold=0.001;
  /**
   *
   */
  public static StatisticsParameterImpl getInstance() {
    StatisticsParameterImpl Instance = (StatisticsParameterImpl) Serializer.loadObject("Statistics.ser");
    if (Instance == null)
      Instance = new StatisticsParameterImpl();
    return Instance;
  }

  /**
   *
   */
  public StatisticsParameterImpl() {
    m_Name = "Statistics";
  }

  /**
   *
   */
  public String toString() {
    String ret = "\r\nStatParameter:\r\nm_MultiRuns=" + m_MultiRuns +
                 "\r\nm_Textoutput=" + m_Textoutput +
                 "\r\nm_Plotoutput=" + m_Plotoutput;
    return ret;
  }

  /**
   *
   */
  public void saveInstance() {
    Serializer.storeObject("Statistics.ser", this);
  }

  /**
   *
   */
  private StatisticsParameterImpl(StatisticsParameterImpl Source) {
    m_ConvergenceRateThreshold = Source.m_ConvergenceRateThreshold;
    m_useStatPlot = Source.m_useStatPlot;
    m_Textoutput = Source.m_Textoutput;
    m_Plotoutput = Source.m_Plotoutput;
    m_PlotFitness = Source.m_PlotFitness;
    m_MultiRuns = Source.m_MultiRuns;
    m_ResultFileName = Source.m_ResultFileName;
  }

  /**
   *
   */
  public Object getClone() {
    return new StatisticsParameterImpl(this);
  }

  /**
   *
   */
  public String getName() {
    return m_Name;
  }

  public String globalInfo() {
    return "Set of parameter describing the statistics which logs the state of the optimization.";
  }

  /**
   *
   */
  public void setTextoutput(int i) {
    if (i >= 0)
      m_Textoutput = i;
  }

  /**
   *
   */
  public int GetTextoutput() {
    return m_Textoutput;
  }

  /**
   *
   */
  public String textoutputTipText() {
    return "Describes how often information is printed in the textoutput frame. textoutput=1 -> there is a output every generation. textoutput<0 -> there is no text output";
  }

  /**
   *
   */
  public void setPlotoutput(int i) {
    m_Plotoutput = i;
  }

  /**
   *
   */
  public int GetPlotoutput() {
    return m_Plotoutput;
  }

  /**
   *
   */
  public String plotFrequencyTipText() {
    return "Frequency how often the fitness plot gets an update. plotoutput=1 -> there is a output every generation. plotoutput<0 -> there is no plot output";
  }

  /**
   *
   */
  public String printMeanTipText() {
    return "Prints the mean of the fitness plot. Makes only sense when multiRuns > 1;";
  }

  /**
   *
   */
  public void setMultiRuns(int x) {
    m_MultiRuns = x;
  }

  /**
   *
   */
  public int getMultiRuns() {
    return m_MultiRuns;
  }

  /**
   *
   */
  public String multiRunsTipText() {
    return "Number of independent optimization runs to evaluate.";
  }

  /**
   *
   */
  public void setResultFileName(String x) {
    m_ResultFileName = x;
  }

  /**
   *
   */
  public String GetInfoString() {
    return m_InfoString;
  }

  /**
   *
   */
  public void setInfoString(String s) {
    m_InfoString = s;
  }

  /**
   *
   */
  public String infoStringTipText() {
    return "Infostring displayed on fitness graph by prssing the right mouse button.";
  }

  /**
   *
   */
  public boolean GetuseStatPlot() {
    return m_useStatPlot;
  }

  /**
   *
   */
  public void setuseStatPlot(boolean x) {
    m_useStatPlot = x;
  }

  /**
   *
   */
  public String useStatPlotTipText() {
    return "Plotting each fitness graph separate if multiruns > 1.";
  }

  /**
   *
   */
  public SelectedTag getPlotFitness() {
    return new SelectedTag(m_PlotFitness, TAGS_PLOT_FITNESS);
  }

  /**
   *
   */
  public void setPlotFitness(SelectedTag newMethod) {
    m_PlotFitness = newMethod.getSelectedTag().getID();
  }

  /**
   *
   */
  public String plotFitnessTipText() {
    return "The individual of which the fitness is plotted.";
  }

  /**
   *
   */
  public String plotObjectivesTipText() {
    return "The individual of which the objectives are plotted.";
  }

  /**
   *
   */
  public String getResultFileName() {
    return m_ResultFileName;
  }

  /**
   *
   */
  public String resultFileNameTipText() {
    return "File name for the result file, if 'none' no output file will be created.";
  }

  /**
   *
   * @param x
   */
  public void setConvergenceRateThreshold(double x) {
    m_ConvergenceRateThreshold = x;
  }

  /**
   *
   * @param x
   */
  public double getConvergenceRateThreshold() {
    return m_ConvergenceRateThreshold;
  }
}