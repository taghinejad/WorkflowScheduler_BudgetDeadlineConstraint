package examples.Simple;

import executionClasses.executionClass;
import utility.Utility;

public class executionSample {
	public static void main(String[] args) {

		// bandwidth is Transfer Time based on Megabytes per second;
		// interval is based on Second;
		// res is resources type which is Amazon EC2 2020
		// tasksSeries is TaskSize 1:25 2:50 3:100 4: 1000;
		int bandwidth = 200, res = 2020, interval = 3600;
		bandwidth *= 1000000;
		int taskSeries = 2;
		String WfFile = Utility.returnDaxMontage(taskSeries);
		int deadline = 200;
		float costConstraint;
		Boolean instancePrint = true, graphChartPrint = false, CriticalPathPrint = false, ResourceUsedPrint = false;
		
		
		// this function returns the costConstrained by execution of IC-PCP algorithm;
		costConstraint = executionClass.RunMyPCP(WfFile, deadline, instancePrint, graphChartPrint, CriticalPathPrint,
				ResourceUsedPrint, res, interval, bandwidth);
		
		//Algorithm in which runs
		executionClass.RunBDCtaghinezhad(WfFile, deadline, costConstraint, instancePrint, res, interval, bandwidth);
		executionClass.RunBDDCtaghinezhad(WfFile, deadline, costConstraint, instancePrint, res, interval, bandwidth);
	}
}
