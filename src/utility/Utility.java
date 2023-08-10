package utility;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RefineryUtilities;

import Broker.InstanceSet;
import Broker.Log;
import charts.ChartDataMaker;
import charts.ChartMakerCost;
import charts.ChartMakerTime;


public class Utility {
	//if it is true files are considered but if it is false just data;
	public static Boolean readConsideration = false;

	// dbs stores results of execution of different Scenarios;
	public static List<ResultDB> dbs = new ArrayList<ResultDB>();
	public static float[] succRate;
	public static List<PSRclass> psrlst = new ArrayList<PSRclass>();;

	public static String returnDax(String name) {
		System.out.println("Workflow:" + name);
		return "dax/" + name + ".xml";
	}

	public static int getIndexOfIdDbs(String id) {
		for (int i = 0; i < dbs.size(); i++) {
			if (dbs.get(i).id.contains(id))
				return i;
		}
		return -1;
	}

	public static void saveAlgorithmResult(String id, String algorithmName, long makespan, float cost,
			float normalizedCost, float normalizedTime, InstanceSet instances) {
		if (cost == -1)
			makespan = -1;
		int index = utility.Utility.getIndexOfIdDbs(id);
		if (index != -1) {
			AlgorithmResult ar = new AlgorithmResult(algorithmName, makespan, cost, normalizedCost, normalizedTime,
					instances);
			dbs.get(index).getAlgorithms().add(ar);

		}
	}

	public static void saveAlgorithmResult(String id, String algorithmName, long makespan, float cost,
			float normalizedCost, float normalizedTime, InstanceSet instances, int deadlineC, float costC) {
		if (cost == -1)
			makespan = -1;
		int index = utility.Utility.getIndexOfIdDbs(id);
		if (index != -1) {
			AlgorithmResult ar = new AlgorithmResult(algorithmName, makespan, cost, normalizedCost, normalizedTime,
					instances, deadlineC, costC);
			dbs.get(index).getAlgorithms().add(ar);

		}
	}

	public static void saveAlgorithmResult(String id, String algorithmName, long makespan, float cost,
			float normalizedCost, float normalizedTime, InstanceSet instances, int deadlineC, float costC,
			int utilization) {
		if (cost == -1)
			makespan = -1;
		int index = utility.Utility.getIndexOfIdDbs(id);
		if (index != -1) {
			AlgorithmResult ar = new AlgorithmResult(algorithmName, makespan, cost, normalizedCost, normalizedTime,
					instances, deadlineC, costC, utilization);
			dbs.get(index).getAlgorithms().add(ar);

		}
	}

	public static void PSRAlgorithm() {
		System.out.println("Total PSR of Algorithms"
				+ ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
		System.out.println(
				",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
		float[] PSRAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		float[] TimeBadgetRationAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		float[] TimeRationAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		float[] BadgetRationAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		int count, succount = 0, scheduled = 0;
		float TimeRatio = 0, BudgetRatio = 0;
		int utilization = 0, utilizationRatio = 0;
		succRate = new float[dbs.get(0).getAlgorithms().size()];
		for (int i = 0; i < dbs.get(0).getAlgorithms().size(); i++) {
			count = 0;
			succount = 0;
			utilization=0;
			AlgorithmResult as = dbs.get(0).algorithms.get(i);
			TimeRatio = 0;
			BudgetRatio = 0;
			scheduled = 0;
			for (int j = 0; j < dbs.size(); j++) {
				as = dbs.get(j).algorithms.get(i);
				if (dbs.get(j).getCostConstrained() >= as.cost && dbs.get(j).getTimeConstrained() >= as.makespan
						&& as.cost > 0)
					succount++;
				count++;
				utilization += as.Utilization;
				if (as.cost > 0) {
					TimeRatio += as.TimeRatio;
					BudgetRatio += as.BudgetRatio;
					scheduled++;
				}

			}
			PSRAlgroithms[i] = (float) (succount * 100) / count;
			TimeRationAlgroithms[i] = (float) TimeRatio / scheduled;
			BadgetRationAlgroithms[i] = (float) BudgetRatio / scheduled;
			TimeBadgetRationAlgroithms[i] = (float) TimeRationAlgroithms[i] / BadgetRationAlgroithms[i];
			utilizationRatio = Math.round((float) utilization / count);
			succRate[i] = PSRAlgroithms[i];
			System.out.println(as.name + " (Success_Rate) Total PSR: " + PSRAlgroithms[i] + " TimeBudgetRatio:"
					+ TimeBadgetRationAlgroithms[i] + " TimeRatio:" + TimeRationAlgroithms[i] + " BudgetRatio:"
					+ BadgetRationAlgroithms[i] + " Total Run:" + count + " totalUnscheduled:" + (count - scheduled)+ " Utrate:"+utilizationRatio);
		}

	}

	private static Boolean isContainWorkflowPSRclass(String workflow) {
		for (int i = 0; i < psrlst.size(); i++) {
			if (psrlst.get(i).workflow.contains(workflow))
				return true;
		}
		return false;
	}

	public static void PSRAlgorithmWorkflowBased() {
		psrlst.clear();
		System.out.println(
				"PSR WorkflowBased,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
		System.out.println(
				",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
		float[] PSRAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		int count, succount = 0;
		float sucRate = 0;
		int utilization = 0, utilizationRatio = 0;
		List<String> wflows = new ArrayList<String>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
		}

		String curWf;

		for (int j = 0; j < wflows.size(); j++) {
			curWf = wflows.get(j);
			PSRclass psr = new PSRclass(curWf);

			for (int i = 0; i < dbs.get(0).getAlgorithms().size(); i++) {
				count = 0;
				succount = 0;
				utilization = 0;
				for (ResultDB rs : utility.Utility.dbs) {
					if (rs.getWorkflow().contains(curWf)) {
						AlgorithmResult as = rs.algorithms.get(i);
						if (rs.getCostConstrained() >= as.cost && rs.getTimeConstrained() >= as.makespan && as.cost > 0)
							succount++;
						count++;
						utilization += as.Utilization;
					}
				}
				sucRate = (float) (succount * 100) / count;
				utilizationRatio = Math.round((float) utilization / count);
				psr.algorithms.add(new algorithm(dbs.get(0).algorithms.get(i).name, sucRate,utilizationRatio));
			}

			psrlst.add(psr);
		}

		// print Results
		System.out.println("*** PSR based on Workflows");
		for (int j = 0; j < psrlst.size(); j++) {
			System.out.println(psrlst.get(j).workflow);
			for (int i = 0; i < psrlst.get(j).algorithms.size(); i++) {
				System.out.println(
						"  " + psrlst.get(j).algorithms.get(i).name + " PSR: " + psrlst.get(j).algorithms.get(i).PSR+ " UTrate:" + psrlst.get(j).algorithms.get(i).Utilization);
			}
		}
	}

	public static void PSRAlgorithmWorkflowIntervalBased() {
		psrlst.clear();
		System.out.println(
				"PSR WorkflowIntervalBased,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
		System.out.println(
				",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

		int count, succount = 0;
		float sucRate = 0;
		int utilization = 0, utilizationRatio = 0,sucUtilization=0;
		List<String> wflows = new ArrayList<String>();
		List<Integer> intervals = new ArrayList<Integer>();
		for (ResultDB rs : utility.Utility.dbs) {
			if (!wflows.contains(rs.getWorkflow().trim()))
				wflows.add(rs.getWorkflow());
			if (!intervals.contains(rs.getInterval()))
				intervals.add(rs.getInterval());
		}

		String curWf;
		Integer curInt;
		for (int j = 0; j < wflows.size(); j++) {
			curWf = wflows.get(j);
			for (int q = 0; q < intervals.size(); q++) {
				curInt = intervals.get(q);
				PSRclass psr = new PSRclass(curWf, curInt);
				for (int i = 0; i < dbs.get(0).getAlgorithms().size(); i++) {
					count = 0;
					succount = 0;
					utilization = 0;
					sucUtilization=0;
					for (ResultDB rs : utility.Utility.dbs) {
						if (rs.getWorkflow().contains(curWf) && rs.getInterval() == curInt) {
							AlgorithmResult as = rs.algorithms.get(i);
							if (rs.getCostConstrained() >= as.cost && rs.getTimeConstrained() >= as.makespan
									&& as.cost > 0) {
								succount++;
								sucUtilization+=as.Utilization;
							}
							count++;
							utilization += as.Utilization;
						}
					}
					sucRate = (float) (succount * 100) / count;
					utilizationRatio = Math.round((float) utilization / count);
					sucUtilization=Math.round((float) sucUtilization / succount);
					psr.algorithms.add(new algorithm(dbs.get(0).algorithms.get(i).name, sucRate, utilizationRatio,sucUtilization));
				}
				psrlst.add(psr);
			}

		}

		// print Results
		System.out.println("*** PSR based on Workflows");
		for (int j = 0; j < psrlst.size(); j++) {
			System.out.println(psrlst.get(j).workflow + "Interval:" + psrlst.get(j).interval);
			for (int i = 0; i < psrlst.get(j).algorithms.size(); i++) {
				System.out.println("  " + psrlst.get(j).algorithms.get(i).name + " PSR: "
						+ psrlst.get(j).algorithms.get(i).PSR + " UT:" + psrlst.get(j).algorithms.get(i).Utilization+ " sucUT:" + psrlst.get(j).algorithms.get(i).SuccessUtilization);
			}
		}

	}

	public static void LogResults() {

		for (ResultDB rs : dbs) {
			System.out.println("__________________________________" + rs.id);
			System.out.println(
					" --- Interval: " + rs.interval + "  Bandwidth:" + rs.bandwidth + " Workflow:" + rs.Workflow);
			System.out.println(" X:" + rs.x + " Cost Constrained :" + rs.costConstrained + " Deadline Constrained:"
					+ rs.timeConstrained);
			for (AlgorithmResult as : rs.algorithms) {
				System.out.println(as.name + "   Cost:" + as.cost + "    Time: " + as.makespan + "     Ncost:"
						+ as.NormalizedCost + " Ntime:" + as.NormalizedTime);
			}
		}
		PSRAlgorithm();
		PSRAlgorithmWorkflowBased();
		PSRAlgorithmWorkflowIntervalBased();

	}

	public static void LogResults(Boolean CostChart, Boolean TimeChart, Boolean SaveCharts) {
		for (ResultDB rs : dbs) {
			System.out.println("__________________________________" + rs.id);
			System.out.println(
					" --- Interval: " + rs.interval + "  Bandwidth:" + rs.bandwidth + " Workflow:" + rs.Workflow);
			System.out.println(" X:" + rs.x + " Cost Constrained :" + rs.costConstrained + " Deadline Constrained:"
					+ rs.timeConstrained);
			for (AlgorithmResult as : rs.algorithms) {
				System.out.println(as.name + "   Cost:" + as.cost + "    Time: " + as.makespan + "     Ncost:"
						+ as.NormalizedCost + " Ntime:" + as.NormalizedTime);
			}
		}
		if (CostChart || TimeChart) {
			charts.ChartDataMaker.printCostTimeBarCharts(CostChart, TimeChart);
			if (SaveCharts)
				charts.ChartDataMaker.SaveBarCostTimeCharts(CostChart, TimeChart);
		}
		PSRAlgorithm();
	}

	public static void LogResults(Boolean CostBarChart, Boolean TimeBarChart, Boolean SaveCharts, Boolean XYChartPrint,
			Boolean XYChartSave) {
		for (ResultDB rs : dbs) {
			System.out.println("__________________________________" + rs.id);
			System.out.println(
					" --- Interval: " + rs.interval + "  Bandwidth:" + rs.bandwidth + " Workflow:" + rs.Workflow);
			System.out.println(" X:" + rs.x + " Cost Constrained :" + rs.costConstrained + " Deadline Constrained:"
					+ rs.timeConstrained);
			for (AlgorithmResult as : rs.algorithms) {
				System.out.println(as.name + "   Cost:" + as.cost + "    Time: " + as.makespan + " CostRatio:"
						+ as.BudgetRatio + " TimeRatio:" + as.TimeRatio + " Utilization:" + as.Utilization + " NC:"
						+ as.NormalizedCost + " NT:" + as.NormalizedTime);
			}
		}
		if (CostBarChart || TimeBarChart) {
			charts.ChartDataMaker.printCostTimeBarCharts(CostBarChart, TimeBarChart);
			if (SaveCharts)
				charts.ChartDataMaker.SaveBarCostTimeCharts(CostBarChart, TimeBarChart);
		}
		if (XYChartPrint || XYChartSave) {
			charts.ChartDataMaker.printXYCharts();
			if (XYChartSave)
				charts.ChartDataMaker.saveCostTimeXYCharts();
		}
		PSRAlgorithm();
		PSRAlgorithmWorkflowBased();
		PSRAlgorithmWorkflowIntervalBased();
	}
	public static void LogResults(Boolean QosRatioChart, Boolean CostBarChart, Boolean TimeBarChart, Boolean SaveCharts, Boolean XYChartPrint,
			Boolean XYChartSave,Boolean SuccessRate,Boolean TimeRatioCostRatio) {
		for (ResultDB rs : dbs) {
			System.out.println("\n                  " + rs.id);
			System.err.println(
					" --- Interval: " + rs.interval + "  Bandwidth:" + rs.bandwidth + " Workflow:" + rs.Workflow);
			System.out.println(" X:" + rs.x + " Cost Constrained :" + rs.costConstrained + " Deadline Constrained:"
					+ rs.timeConstrained);
			for (AlgorithmResult as : rs.algorithms) {
				if (as.cost>rs.costConstrained || as.makespan>rs.timeConstrained)
					System.err.println(as.name + "   Cost:" + as.cost + "    Time: " + as.makespan + " CostRatio:"
							+ as.BudgetRatio + " TimeRatio:" + as.TimeRatio + " Utilization:" + as.Utilization + " NC:"
							+ as.NormalizedCost + " NT:" + as.NormalizedTime);
				else
				System.out.println(as.name + "   Cost:" + as.cost + "    Time: " + as.makespan + " CostRatio:"
						+ as.BudgetRatio + " TimeRatio:" + as.TimeRatio + " Utilization:" + as.Utilization + " NC:"
						+ as.NormalizedCost + " NT:" + as.NormalizedTime);
			}
		}
		if (QosRatioChart)
			charts.ChartDataMaker.printQosRatioCharts();
		if (CostBarChart || TimeBarChart) {
			charts.ChartDataMaker.printCostTimeBarCharts(CostBarChart, TimeBarChart);
			if (SaveCharts)
				charts.ChartDataMaker.SaveBarCostTimeCharts(CostBarChart, TimeBarChart);
		}
		if (XYChartPrint || XYChartSave) {
			charts.ChartDataMaker.printXYCharts();
			if (XYChartSave)
				charts.ChartDataMaker.saveCostTimeXYCharts();
		}
		if (SuccessRate)
			charts.ChartDataMaker.printSuccessRateCharts();
		if (TimeRatioCostRatio)
			charts.ChartDataMaker.printTimeCostRatioCharts();
		PSRAlgorithm();
		PSRAlgorithmWorkflowBased();
		PSRAlgorithmWorkflowIntervalBased();
	}
	

	public static String returnDaxFloodplain() {

		return returnDax("others/mfloodplain");

	}

	public static String returnDaxFloodplain(int taskSeries) {

		switch (taskSeries) {
		case 1:
			return returnDax("others/mfloodplain");
		default:
			return returnDax("others/mfloodplain");
		}

	}

	public static String returnDaxHEFT_paper() {

		return returnDax("HEFT_paper");

	}

	public static String returnDaxHEFT_paper(int taskSeries) {

		switch (taskSeries) {
		case 1:
			returnDax("HEFT_paper");
		default:
			return returnDax("HEFT_paper");
		}
	}

	public static String returnDaxSipht(int taskSeries) {
		switch (taskSeries) {
		case 1:
			return returnDax("Sipht_30");
		case 2:
			return returnDax("Sipht_60");
		case 3:
			return returnDax("Sipht_100");
		case 4:
			return returnDax("Sipht_1000");
		default:
			return returnDax("Sipht_30");
		}
	}

	public static String returnDaxMontage(int taskSeries) {
		switch (taskSeries) {
		case 1:
			return returnDax("Montage_25");
		case 2:
			return returnDax("Montage_50");
		case 3:
			return returnDax("Montage_100");
		case 4:
			return returnDax("Montage_1000");
		default:
			return returnDax("Montage_25");
		}
	}

	public static String returnDaxCyberShake(int taskSeries) {
		switch (taskSeries) {
		case 1:
			return returnDax("CyberShake_30");
		case 2:
			return returnDax("CyberShake_50");
		case 3:
			return returnDax("CyberShake_100");
		case 4:
			return returnDax("CyberShake_1000");
		default:
			return returnDax("CyberShake_30");
		}
	}

	public static String returnDaxInspiral(int taskSeries) {
		switch (taskSeries) {
		case 1:
			return returnDax("Inspiral_30");
		case 2:
			return returnDax("Inspiral_50");
		case 3:
			return returnDax("Inspiral_100");
		case 4:
			return returnDax("Inspiral_1000");
		default:
			return returnDax("Inspiral_30");
		}
	}

	public static String returnDaxEpigenomics(int taskSeries) {
		switch (taskSeries) {
		case 1:
			return returnDax("Epigenomics_24");
		case 2:
			return returnDax("Epigenomics_46");
		case 3:
			return returnDax("Epigenomics_100");
		case 4:
			return returnDax("Epigenomics_997");
		default:
			return returnDax("Epigenomics_24");
		}
	}

	private static class PSRclass {
		String workflow;
		int interval;
		// float[] PSRAlgroithms = new float[dbs.get(0).getAlgorithms().size()];
		List<algorithm> algorithms = new ArrayList<algorithm>();

		PSRclass(String wf) {
			this.workflow = wf;
		}

		PSRclass(String wf, int interval) {
			this.workflow = wf;
			this.interval = interval;
		}

	}

	private static class algorithm {
		public String name;
		// PSR: Success Ratio;
		public float PSR = 0;
		public int Utilization = 0;
		public int SuccessUtilization = 0;
		public algorithm(String name, float pSR) {
			super();
			this.name = name;
			this.PSR = pSR;
		}

		public algorithm(String name, float PSR, int utilization) {
			super();
			this.name = name;
			this.PSR = PSR;
			this.Utilization = utilization;
		}
		public algorithm(String name, float PSR, int utilization,int SucUtilization) {
			super();
			this.name = name;
			this.PSR = PSR;
			this.Utilization = utilization;
			this.SuccessUtilization=SucUtilization;
		}
	}
}
