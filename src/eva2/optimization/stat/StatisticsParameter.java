package eva2.optimization.stat;

import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfaceNotifyOnInformers;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.tools.EVAERROR;
import eva2.tools.SelectedTag;
import eva2.tools.Serializer;
import eva2.tools.StringSelection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of parameters for statistics in EvA2. Several data entries are provided by the AbstractStatistics class,
 * others by the additional informers. This class allows customization of entries and frequency of data output.
 * Data entries can be selected using a StringSelection instance.
 * There is a switch called "output full data as text" which will be interpreted by AbstractStatistics showing
 * all or only the selected entities.
 * 
 * @see AbstractStatistics
 * @author mkron
 */
public class StatisticsParameter implements InterfaceStatisticsParameter, InterfaceNotifyOnInformers, Serializable {
    private static final long serialVersionUID = -8681061379203108390L;
    private static final Logger LOGGER = Logger.getLogger(StatisticsParameter.class.getName());
    public final static int VERBOSITY_NONE = 0;
    public final static int VERBOSITY_FINAL = 1;
    public final static int VERBOSITY_KTH_IT = 2;
    public final static int VERBOSITY_ALL = 3;
    SelectedTag outputVerbosity = new SelectedTag("No output", "Final results", "K-th iterations", "All iterations");
    public final static int OUTPUT_FILE = 0;
    public final static int OUTPUT_WINDOW = 1;
    public final static int OUTPUT_FILE_WINDOW = 2;
    SelectedTag outputTo = new SelectedTag("File (current dir.)", "Text-window", "Both file and text-window");
    private int verboK = 10;
    private int m_Textoutput = 0;
    private int m_MultiRuns = 1;
    private String m_ResultFilePrefix = "EvA2";
    protected String m_Name = "not defined";
    private boolean m_useStatPlot = true;
    private boolean showAdditionalProblemInfo = false;
    private double m_ConvergenceRateThreshold = 0.001;
    private StringSelection graphSel = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings());

    /**
     *
     */
    public static StatisticsParameter getInstance(boolean loadDefaultSerFile) {
        if (loadDefaultSerFile) {
            return getInstance("Statistics.ser");
        } else {
            return new StatisticsParameter();
        }
    }

    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static StatisticsParameter getInstance(String serFileName) {
        StatisticsParameter instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(serFileName);
            instance = (StatisticsParameter) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }

        if (instance == null) {
            instance = new StatisticsParameter();
        }
        return instance;
    }

    /**
     *
     */
    public StatisticsParameter() {
        m_Name = "Statistics";
        outputVerbosity.setSelectedTag(VERBOSITY_KTH_IT);
        outputTo.setSelectedTag(1);
    }

    /**
     *
     */
    @Override
    public String toString() {
        String ret = "\r\nStatisticsParameter (" + super.toString() + "):\r\nm_MultiRuns=" + m_MultiRuns
                + ", m_Textoutput=" + m_Textoutput
                + //		", m_Plotoutput=" + m_Plotoutput +
                ", verbosity= " + outputVerbosity.getSelectedString()
                + "\nTo " + outputTo.getSelectedString()
                + ", " + BeanInspector.toString(graphSel.getStrings());
        return ret;
    }

    /**
     *
     */
    @Override
    public void saveInstance() {
        try {
            FileOutputStream fileStream = new FileOutputStream("Statistics.ser");
            Serializer.storeObject(fileStream, this);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not store instance object.", ex);
        }
    }

    /**
     *
     */
    private StatisticsParameter(StatisticsParameter Source) {
        m_ConvergenceRateThreshold = Source.m_ConvergenceRateThreshold;
        m_useStatPlot = Source.m_useStatPlot;
        m_Textoutput = Source.m_Textoutput;
//		m_Plotoutput = Source.m_Plotoutput;
//		m_PlotFitness = Source.m_PlotFitness;
        m_MultiRuns = Source.m_MultiRuns;
        m_ResultFilePrefix = Source.m_ResultFilePrefix;
        verboK = Source.verboK;
    }

    /**
     *
     */
    public Object getClone() {
        return new StatisticsParameter(this);
    }

    /**
     *
     */
    @Override
    public String getName() {
        return m_Name;
    }

    public static String globalInfo() {
        return "Configure statistics and output of the optimization run. Changes to the data selection state will not take effect during a run.";
    }
    
    /**
     *
     */
    @Override
    public void setMultiRuns(int x) {
        m_MultiRuns = x;
    }

    /**
     *
     */
    @Override
    public int getMultiRuns() {
        return m_MultiRuns;
    }

    /**
     *
     */
    @Override
    public String multiRunsTipText() {
        return "Number of independent optimization runs to evaluate.";
    }
    /**
     *
     */
    public String infoStringTipText() {
        return "Infostring displayed on fitness graph by prssing the right mouse button.";
    }

    /**
     * Use averaged graph for multi-run plots or not
     */
    @Override
    public boolean getUseStatPlot() {
        return m_useStatPlot;
    }

    /**
     * Activate or deactivate averaged graph for multi-run plots
     */
    @Override
    public void setUseStatPlot(boolean x) {
        m_useStatPlot = x;
    }

    public String useStatPlotTipText() {
        return "Plotting each fitness graph separately if multiruns > 1.";
    }

    /**
     *
     */
    @Override
    public void setResultFilePrefix(String x) {
        if (x == null) {
            m_ResultFilePrefix = "";
        } else {
            m_ResultFilePrefix = x;
        }
    }

    /**
     *
     */
    @Override
    public String getResultFilePrefix() {
        return m_ResultFilePrefix;
    }

    @Override
    public void setShowTextOutput(boolean show) {
        // activate if not activated
        if (show && outputTo.getSelectedTagID() == 0) {
            outputTo.setSelectedTag(2);
        } // deactivate if activated
        else if (!show && outputTo.getSelectedTagID() > 0) {
            outputTo.setSelectedTag(0);
        }
    }

    @Override
    public boolean isShowTextOutput() {
        return outputTo.getSelectedTagID() > 0;
    }

//	/**
//	*
//	*/
//	public String resultFileNameTipText() {
//	return "File name for the result file. If empty or 'none', no output file will be created.";
//	}
    public String convergenceRateThresholdTipText() {
        return "Provided the optimal fitness is at zero, give the threshold below which it is considered as 'reached'";
    }

    /**
     *
     * @param x
     */
    @Override
    public void setConvergenceRateThreshold(double x) {
        m_ConvergenceRateThreshold = x;
    }

    /**
     *
     */
    @Override
    public double getConvergenceRateThreshold() {
        return m_ConvergenceRateThreshold;
    }

    @Override
    public boolean isOutputAllFieldsAsText() {
        return showAdditionalProblemInfo;
    }

    @Override
    public void setOutputAllFieldsAsText(boolean bShowFullText) {
        showAdditionalProblemInfo = bShowFullText;
    }

    public String outputAllFieldsAsTextTipText() {
        return "Output all available data fields or only the selected entries as text.";
    }

    public void hideHideable() {
        setOutputVerbosity(getOutputVerbosity());
    }

    @Override
    public void setOutputVerbosity(SelectedTag sTag) {
        outputVerbosity = sTag;
        GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", sTag.getSelectedTagID() != VERBOSITY_KTH_IT);
    }

    public void setOutputVerbosity(int i) {
        outputVerbosity.setSelectedTag(i);
        GenericObjectEditor.setHideProperty(this.getClass(), "outputVerbosityK", outputVerbosity.getSelectedTagID() != VERBOSITY_KTH_IT);
    }

    @Override
    public SelectedTag getOutputVerbosity() {
        return outputVerbosity;
    }

    public String outputVerbosityTipText() {
        return "Set the data output level.";
    }

    @Override
    public int getOutputVerbosityK() {
        return verboK;
    }

    @Override
    public void setOutputVerbosityK(int k) {
        verboK = k;
    }

    public String outputVerbosityKTipText() {
        return "Set the interval of data output for intermediate verbosity (in generations).";
    }

    @Override
    public SelectedTag getOutputTo() {
        return outputTo;
    }

    @Override
    public void setOutputTo(SelectedTag tag) {
        outputTo = tag;
    }

    public void setOutputTo(int i) {
        outputTo.setSelectedTag(i);
    }

    public String outputToTipText() {
        return "Set the output destination; to deactivate output, set verbosity to none.";
    }

    @Override
    public StringSelection getFieldSelection() {
        return graphSel;
    }

    @Override
    public void setFieldSelection(StringSelection v) {
        graphSel = v;
    }

    public String fieldSelectionTipText() {
        return "Select the data fields to be collected and plotted. Note that only simple types can be plotted to the GUI.";
    }

    /**
     * May be called to dynamically alter the set of graphs that can be
     * selected, using a list of informer instances, which usually are the
     * problem and the optimizer instance.
     *
     * @see InterfaceAdditionalPopulationInformer
     */
    @Override
    public void setInformers(List<InterfaceAdditionalPopulationInformer> informers) {
        ArrayList<String> headerFields = new ArrayList<String>();
        ArrayList<String> infoFields = new ArrayList<String>();
        // parse list of header elements, show additional Strings according to names.
        for (InterfaceAdditionalPopulationInformer inf : informers) {
            String[] dataHeader = inf.getAdditionalDataHeader();
            headerFields.addAll(Arrays.asList(dataHeader));
            if (infoFields.size() < headerFields.size()) { // add info strings for tool tips - fill up with null if none have been returned.
                String[] infos = inf.getAdditionalDataInfo();
                if (infos != null) {
                    if (infos.length != dataHeader.length) {
                        System.out.println(BeanInspector.toString(infos));
                        System.out.println(BeanInspector.toString(dataHeader));
                        EVAERROR.errorMsgOnce("Warning, mismatching number of headers and additional data fields for " + inf.getClass() + " (" + dataHeader.length + " vs. " + infos.length + ").");
                    }
                    infoFields.addAll(Arrays.asList(infos));
                }
                while (infoFields.size() < headerFields.size()) {
                    infoFields.add(null);
                }
            }
//			header += inf.getAdditionalDataHeader(null); // lets hope this works with a null 
        }
        // create additional fields to be selectable by the user, defined by the informer headers
//		StringSelection ss = new StringSelection(GraphSelectionEnum.getAndAppendArray(headerFields.toArray(new String[headerFields.size()])));
        StringSelection ss = new StringSelection(GraphSelectionEnum.currentBest, GraphSelectionEnum.getInfoStrings(),
                headerFields, infoFields.toArray(new String[infoFields.size()]));
        ss.takeOverSelection(graphSel);
//		System.out.println("In " + this + ": setting new informers: " + BeanInspector.toString(ss.getStrings()));
        // This works!
        setFieldSelection(ss);
//		System.out.println("After: " + this);
    }
}