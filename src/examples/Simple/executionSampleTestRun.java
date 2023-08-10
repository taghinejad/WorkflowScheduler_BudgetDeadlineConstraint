package examples.Simple;

import executionClasses.executionClass;
import utility.Utility;

public class executionSampleTestRun {
	public static void main(String arg[]) {
		// bandwidth is Transfer Time based on Megabytes per second;
		// interval is based on Second;
		// res is resources type which is Amazon EC2 2020
		// tasksSeries is TaskSize 1:25 2:50 3:100 4: 1000;
		int bandwidth = 20, res = 2020, interval = 3600;
		bandwidth *= 1000000;

		Boolean instancePrint = false;
		int taskSeries = 1;

		String WfFile = Utility.returnDaxMontage(taskSeries);
		int deadline = 167;
		float costConstraint = 22;
		executionClass.RunDBCS_HARABNEJAD(WfFile, deadline, costConstraint, instancePrint, res, interval, bandwidth);
		executionClass.RunBDDCtaghinezhad(WfFile, deadline, costConstraint, instancePrint, res, interval, bandwidth);

	}
}
