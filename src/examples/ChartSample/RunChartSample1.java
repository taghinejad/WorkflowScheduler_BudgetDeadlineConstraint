package examples.ChartSample;

import charts.ChartMakerCost;
import executionClasses.executionClass;
import executionClasses.executionSenario;
import utility.ChartBarMaker;
import utility.Utility;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class RunChartSample1 {
	public static void main(String arg[]) {

		int bandwidth = 20;
		bandwidth *= 1000000;
		// interval is based on Second;

		// res is resources type which is Amazon EC2 2020
		int res = 2020;
		int interval = 3600;
		// tasksSeries is TaskSize 1:25 2:50 3:100 4: 1000;
		int TaskSeries = 4;

		executionSenario.SeriesMontage(TaskSeries, bandwidth, res, interval,executionSenario.TightRange.Tight);
//		executionSenario.SeriesSipht(TaskSeries, bandwidth, res, interval,executionSenario.TightRange.Tight);
//		executionSenario.SeriesEpigenomics(TaskSeries, bandwidth, res, interval,executionSenario.TightRange.Tight);
//		executionSenario.SeriesCyberShake(TaskSeries, bandwidth, res, interval,executionSenario.TightRange.Tight);
//		executionSenario.SeriesInspiral(TaskSeries, bandwidth, res, interval,executionSenario.TightRange.Tight);
		
		Boolean QosRatio = true, costBarChart = false, timeBarChart = false, saveCharts = false, xyChartPrint = false,
				xyChartSave = false,successRate=true,timeRatioCostRatio=true;
		utility.Utility.LogResults(QosRatio, costBarChart, timeBarChart, saveCharts, xyChartPrint, xyChartSave,successRate,timeRatioCostRatio);
	}
}
