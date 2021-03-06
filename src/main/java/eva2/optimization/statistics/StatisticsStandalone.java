package eva2.optimization.statistics;

import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceAdditionalPopulationInformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This simple statistics implementation can collect all Object data available during runs.
 * Be aware that the memory requirements can be excessive depending on the data produced by
 * the additional informers, and depending on the selected fields to be collected.
 * Therefore, the default is not to log the data but just print it using the super class.
 *
 * @see InterfaceAdditionalPopulationInformer
 * @see AbstractStatistics
 */
public class StatisticsStandalone extends AbstractStatistics implements InterfaceStatistics, Serializable {
    private ArrayList<ArrayList<Object[]>> resultData = null;
    private ArrayList<String> resultHeaderStrings = null;
    private boolean collectData = false;


    public StatisticsStandalone(InterfaceStatisticsParameters statParams) {
        super();
        statisticsParameter = statParams;
    }

    public StatisticsStandalone(String resultFileName) {
        this(resultFileName, 1, resultFileName == null ? InterfaceStatisticsParameters.OutputVerbosity.NONE : InterfaceStatisticsParameters.OutputVerbosity.FINAL, false);
    }

    public StatisticsStandalone(String resultFileName, int multiRuns, InterfaceStatisticsParameters.OutputVerbosity verbosity, boolean outputAllFieldsAsText) {
        this(StatisticsParameters.getInstance(false));
        statisticsParameter.setMultiRuns(multiRuns);
        statisticsParameter.setOutputVerbosity(verbosity);
        statisticsParameter.setResultFilePrefix(resultFileName);
        statisticsParameter.setOutputAllFieldsAsText(outputAllFieldsAsText);
        if (resultFileName == null) {
            statisticsParameter.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);
        } else {
            statisticsParameter.setOutputTo(InterfaceStatisticsParameters.OutputTo.FILE);
        }
    }

    public StatisticsStandalone() {
        this(new StatisticsParameters());
    }

    @Override
    protected void initializePlots(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (collectData) {
            resultData = new ArrayList<>(statisticsParameter.getMultiRuns());
            List<String> description = getOutputHeaderFieldNames(informerList);
            resultHeaderStrings = new ArrayList<>();
            resultHeaderStrings.addAll(description.stream().collect(Collectors.toList()));
            for (int i = 0; i < statisticsParameter.getMultiRuns(); i++) {
                resultData.add(new ArrayList<>());
            }
        } else {
            resultData = null;
            resultHeaderStrings = null;
        }
    }

    @Override
    protected void plotCurrentResults() {
        if (collectData && (resultData != null)) {
            resultData.get(optRunsPerformed).add(currentStatObjectData);
        }
    }

    @Override
    public void plotSpecificData(PopulationInterface pop, List<InterfaceAdditionalPopulationInformer> informerList) {
        double[] specificData = pop.getSpecificData();
        if (specificData != null) {
            for (int i = 0; i < specificData.length; i++) {
                resultData.get(optRunsPerformed).add(new Object[]{(double) functionCalls, specificData});
            }
        }
    }

    /**
     * Check whether data collection is activated, which stores an Object[] for every iteration and
     * every multi-run.
     *
     * @return
     */
    public boolean isCollectData() {
        return collectData;
    }

    /**
     * Set state of full data collection, which stores an Object[] for every iteration and
     * every multi-run.
     *
     * @param collectFullData
     */
    public void setCollectData(boolean collectFullData) {
        this.collectData = collectFullData;
    }

    public ArrayList<ArrayList<Object[]>> getCollectedData() {
        return resultData;
    }

    public ArrayList<Object[]> getCollectedRunData(int runIndex) {
        return resultData.get(runIndex);
    }

    public ArrayList<String> getCollectedDataHeaders() {
        return resultHeaderStrings;
    }
}