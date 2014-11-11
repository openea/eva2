package eva2.optimization.statistics;

import eva2.optimization.enums.StatisticsOnSingleDataSet;
import eva2.optimization.enums.StatisticsOnTwoSampledData;
import eva2.tools.StringSelection;

import javax.swing.*;
import java.io.Serializable;
import java.util.List;

@eva2.util.annotation.Description(value = "Select statistical values to be calculated and tests to be performed.")
public class StatisticalEvaluationParameters implements Serializable {

    private StringSelection singleStats = new StringSelection(StatisticsOnSingleDataSet.mean, StatisticsOnSingleDataSet.getInfoStrings());
    private StringSelection twoSampledStats = new StringSelection(StatisticsOnTwoSampledData.tTestUneqLenEqVar, StatisticsOnTwoSampledData.getInfoStrings());
    private List<JButton> additionalButtons = null;

    public void setGenericAdditionalButtons(List<JButton> buts) {
        this.additionalButtons = buts;
    }

    public StringSelection getTwoSampledStats() {
        return twoSampledStats;
    }

    public void setTwoSampledStats(StringSelection twoSStats) {
        this.twoSampledStats = twoSStats;
    }

    public String twoSampledStatsTipText() {
        return "Statistical tests on two-sampled data";
    }

    public StringSelection getOneSampledStats() {
        return singleStats;
    }

    public void setOneSampledStats(StringSelection singleStats) {
        this.singleStats = singleStats;
    }

    public String oneSampledStatsTipText() {
        return "Statistical tests on one-sampled data";
    }

    public String getName() {
        return "Statistical evaluation parameters";
    }

    public List<JButton> getAdditionalButtons() {
        return additionalButtons;
    }

    public boolean withGenericOkButton() {
        return false;
    }

    public boolean withGenericLoadSafeButtons() {
        return false;
    }
}