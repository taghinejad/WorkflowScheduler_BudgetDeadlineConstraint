package examples.ChartSample;

import org.jfree.ui.RefineryUtilities;

import Broker.WorkflowBroker;
import charts.instanceChart;
import executionClasses.executionClass;
import utility.Utility;

public class RunChartInstanceGantt {
	public static void main(String arg[]) {
		// bandwidth is Transfer Time based on Megabytes per second;
		// interval is based on Second;
		// res is resources type which is Amazon EC2 2020
		// tasksSeries is TaskSize 1:25 2:50 3:100 4: 1000;
		int bandwidth = 20, res = 2020, interval = 300;
		bandwidth *= 1000000;

		Boolean instancePrint = true;
		int taskSeries = 3;
		
		String WfFile = Utility.returnDaxCyberShake(taskSeries);
		int deadline = 2000;
		float costConstraint = 100;
		executionClass.RunMyPCP(WfFile, deadline, instancePrint, false, false, false, res, interval, bandwidth);
			


	}
}
