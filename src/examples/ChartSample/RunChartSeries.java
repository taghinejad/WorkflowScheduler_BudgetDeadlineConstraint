package examples.ChartSample;

import executionClasses.executionSenario;

public class RunChartSeries {
	public static void main(String[] args) {
		
		runSeries1(3600);
//		runSeries1(300);
//		runSeries1(60);
		
		Boolean QosRatio = true, costBarChart = false, timeBarChart = false, saveCharts = false, xyChartPrint = false,
				xyChartSave = false,successRate=true,timeRatioCostRatio=true;
		utility.Utility.LogResults(QosRatio, costBarChart, timeBarChart, saveCharts, xyChartPrint, xyChartSave,successRate,timeRatioCostRatio);
	}
	
	public static void runSeries1(int interval)
	{
		//bandwidth is Transfer Time based on Megabytes per second;
				int bandwidth = 200;
				bandwidth *= 1000000;
				// interval is based on Second;
				
				// res is resources type which is Amazon EC2 2020
				int res = 2020;
				
				//tasksSeries is TaskSize 1:25 2:50 3:100 4: 1000;
				int taskSeries=3;
				
				executionSenario.Series1(taskSeries, bandwidth, res, interval);
	}
}
