package executionClasses;

import Broker.Log;
import Broker.ScheduleType;
import Broker.WorkflowBroker;
import Broker.WorkflowPolicy;
import charts.instanceChart;
import utility.SimulationResults;

public class executionClass {
	static WorkflowBroker wb;
	// static int deadline = 0;
	// static int bandwidth = 200000000;

	static int FactestTime = 1, FastestCPTime = 0, CheapestTime = 1, HeftTime = 1, CheapestCpTime, CheapestIndTime;
	static float FastestCost, FastestCPCost = 0, CheapestCost, CheapestCpCost, CheapestIndCost, HeftCost, PCPcost;

		public static void scheduleWorkflowInitial(String WfFile, int res, int interval, int bandwidth) {
		scheduleWorkflowFastest(WfFile, res, interval, bandwidth);
		RunCheapestCP(WfFile, res, interval, bandwidth);
		RunCheapestPolicy(WfFile, res, interval, bandwidth);
		RunFastestCP(WfFile, res, interval, bandwidth);
		RunCheapestPolicyIndependent(WfFile, res, interval, bandwidth);
		RunHeft(WfFile, false, res, interval, bandwidth);
		
	}

	public static void scheduleWorkflowFastest(String WfFile, int res, int interval, int bandwidth) {
		int startTime = 0;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MY_FAST, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		// CH is Cost MH is time fastest Run
		FastestCost = wb.schedule(startTime, 0);
		FactestTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// setting Deadline
		// deadline = MH * 10;
		System.out.println(" Fastest Cost=" + FastestCost + "  Time=" + FactestTime + " ResourceDetails[Mips"
				+ wb.resources.getMaxMIPS() + " CostPerInterval:" + wb.resources.getMaxCost() + "]");

	}

	public static void RunCheapestPolicy(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.Cheapest, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		CheapestCost = wb.schedule(startTime, 0);
		CheapestTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("Cheapest: cost=" + CheapestCost + " time=" + CheapestTime + " ResourceDetails[Mips: "
				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunCheapestPolicyIndependent(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.My_CHEAPEST, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		CheapestIndCost = wb.schedule(startTime, 0);
		CheapestIndTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out
				.println("CheapestInd: cost=" + CheapestIndCost + " time=" + CheapestIndTime + " ResourceDetails[Mips: "
						+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunCheapestCP(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.CheapestCP, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}

		CheapestCpCost = wb.schedule(startTime, 0);
		CheapestCpTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("CheapestCp: cost=" + CheapestCpCost + " time=" + CheapestCpTime + " ResourceDetails[Mips: "
				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunFastestCP(String WfFile, int res, int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.FastestCP, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}

		FastestCPCost = wb.schedule(startTime, 0);
		FastestCPTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		HeftTime = FastestCPTime;
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("FastestCP: cost=" + FastestCPCost + " time=" + FastestCPTime + " ResourceDetails[Mips: "
				+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunCheapestWithDeadlineStatisfaction(String WfFile, int deadline, int res, int interval,
			int bandwidth) {
		// deadline = dl;
		// System.out.println("\n Cheapest Deadline=" + deadline);
		int startTime = 0;
		float cost;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.CheapestDeadline, interval, bandwidth, res);
		} catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!" + e.getLocalizedMessage());
		}
		CheapestCost = wb.schedule(startTime, deadline);
		CheapestTime = wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		// System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out
				.println("CheapestDeadline: cost=" + CheapestCost + " time=" + CheapestTime + " ResourceDetails[Mips: "
						+ wb.resources.getMinMIPS() + " CostPerInterval:" + wb.resources.getMinCost() + "]");

	}

	public static void RunPCP(String WfFile, int dl, Boolean instancePrint, Boolean graphChartPrint,
			Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval, int bandwidth) {
		// deadline = dl;
		System.out.println("-IC_PCP Schedule Deadline=" + dl);

		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.IC_PCP, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl);

		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		int util = Log.getUtilization(wb);
		System.out.println("IC_PCP: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost + " Instances="
				+ wb.policy.instances.getSize() + " UT:" + util);
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb,
					WfFile + " IC_PCP cost=" + cost + " time=" + finishTime + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);

		System.out.println("");
		SimulationResults.criticalPaths.clear();
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
	}

	public static float RunMyPCP(String WfFile, int dl, Boolean instancePrint, Boolean graphChartPrint,
			Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval, int bandwidth) {

		System.out.println("-MyIC_PCP Schedule Deadline=" + dl);

		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MYPCP, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl);
		int util = Log.getUtilization(wb);
		PCPcost = cost;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		System.out.println("MyIC_PCP: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
				+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);

			instanceChart.PrintInstanceGantt(wb,
					WfFile + " MY IC_PCP cost=" + cost + " time=" + finishTime + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);

		System.out.println("");
		SimulationResults.criticalPaths.clear();

		return cost;
	}

	public static float RunMyPCPwithCostConstrain(String WfFile, int dl, float CostConstrain, Boolean instancePrint,
			Boolean graphChartPrint, Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval,
			int bandwidth) {

		System.out.println("-MyIC_PCP Schedule Deadline=" + dl);

		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MYPCP, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl);
		int util = Log.getUtilization(wb);
		PCPcost = cost;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		System.out.println("MyIC_PCP: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
				+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);

			instanceChart.PrintInstanceGantt(wb,
					WfFile + " MY IC_PCP cost=" + cost + " time=" + finishTime + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);

		System.out.println("");
		SimulationResults.criticalPaths.clear();
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		utility.Utility.saveAlgorithmResult(dl + CostConstrain + interval + WfFile, "PCP", finishTime, cost, NC, NT,
				wb.policy.instances, dl, CostConstrain, util);

		return cost;
	}

	public static void RunListPolicy(String WfFile, int dl, Boolean instancePrint, int res, int interval,
			int bandwidth) {
		// deadline = dl;
		System.out.println("-ListPolicy Deadline=" + dl);

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.List, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();

		System.out.println("ListPolicy: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
				+ " Instances=" + wb.policy.instances.getSize());

		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " ListPolicy cost=" + cost + " time=" + finishTime);
		}
		System.out.println("");
	}

	public static void RunHeft(String WfFile, Boolean instancePrint, int res, int interval, int bandwidth) {
		System.out.println("-HEFT Algorithm ");

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.HEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, 0);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();

		HeftTime = finishTime;
		HeftCost = cost;
		System.out.println("HEFT: cost=" + cost + "$ time=" + finishTime + " NC=" + cost / CheapestCost + " Instances="
				+ wb.policy.instances.getSize());

		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " HEFT cost=" + cost + " time=" + finishTime);
		}
		System.out.println("");
	}

	public static void RunListPolicy2(String WfFile, int dl, Boolean instancePrint, int res, int interval,
			int bandwidth) {
		// deadline = dl;
		System.out.println("-ListPolicy2 Deadline=" + dl);

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.List2, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		int util = Log.getUtilization(wb);
		System.out.println("ListPolicy2: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
				+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " ListPolicy2 cost=" + cost + " time=" + finishTime);
		}
		System.out.println("");
	}

	public static void RunBDAS(String WfFile, int dl, float costConstrain, Boolean instancePrint, int res, int interval,
			int bandwidth) {
		// deadline = dl;
		// System.out.println("-BDAS COST constrain:" + costConstrain + " Deadline=" +
		// dl);

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDAS, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		int util = Log.getUtilization(wb);
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print("BDAS: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost + " Instances="
					+ wb.policy.instances.getSize() + " UT:" + util);
		else
		System.out.println("BDAS: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost + " Instances="
				+ wb.policy.instances.getSize() + " UT:" + util);
		
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDAS cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		System.out.println("");
	
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids, "BDAS", finishTime, cost, NC, NT, wb.policy.instances, dl,
				costConstrain, util);
	}

	public static void RunBDDCtaghinezhad(String WfFile, int dl, float costConstrain, Boolean instancePrint, int res,
			int interval, int bandwidth) {

		// System.out.println("-FDAS4 Deadline=" + deadline + " COST constrain:" +
		// costConstrain);
		// System.out.println(" *******Run BDDCtaghinezhad COST constrain:" +
		// costConstrain + " Deadline=" + dl);
		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDDCtaghinezhad, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		long tB = (long) (dl - finishTime);
		long cB = (long) (costConstrain - cost);
		int util = Log.getUtilization(wb);
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" BDDCtaghinezhad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);
		else
			System.out.print(" BDDCtaghinezhad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);

		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDDCtaghinezhad cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
		
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids, "BDDC", finishTime, cost, NC, NT,
				wb.policy.instances, dl, costConstrain, util);

	}

	public static void RunBDCtaghinezhad(String WfFile, int dl, float costConstrain, Boolean instancePrint, int res,
			int interval, int bandwidth) {

		// System.out.println("-FDAS4 Deadline=" + deadline + " COST constrain:" +
		// costConstrain);
		// System.out.println(" *******Run BDCtaghinezhad COST constrain:" +
		// costConstrain + " Deadline=" + dl);
		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDCtaghinezhad, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		long tB = (long) (dl - finishTime);
		long cB = (long) (costConstrain - cost);
		int util = Log.getUtilization(wb);
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" BDCtaghinezhad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);
		else
			System.out.print(" BDCtaghinezhad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);
		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDCtaghinezhad cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids, "BDC", finishTime, cost, NC, NT,
				wb.policy.instances, dl, costConstrain, util);

	}

	public static void RunBDSDson(String WfFile, int dl, float costConstrain, Boolean instancePrint, int res,
			int interval, int bandwidth) {
		// deadline = dl;
		// System.out.println("-BDSDson COST constrain:" + costConstrain + " Deadline="
		// + dl);

		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDSDson, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		int util = Log.getUtilization(wb);
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print("BDSDson: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		else
			System.out.print("BDSDson: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDSDson cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		System.out.println("");
		
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		utility.Utility.saveAlgorithmResult(ids, "BDSD", finishTime, cost, NC, NT, wb.policy.instances, dl,
				costConstrain, util);
	}

	public static void RunDBCS_HARABNEJAD(String WfFile, int dl, float costConstrain, Boolean instancePrint, int res,
			int interval, int bandwidth) {

		// System.out.println("-FDAS4 Deadline=" + deadline + " COST constrain:" +
		// costConstrain);
		// System.out.println(" *******DBCS H.Arabnejad COST constrain:" + costConstrain
		// + " Deadline=" + dl);
		int startTime = 0, finishTime, MH, MC;
		long realStartTime = 0, realFinishTime = 0;
		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.DBCS_HARAB, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		realFinishTime = System.currentTimeMillis();
		realFinishTime -= realStartTime;
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		long tB = (long) (dl - finishTime);
		long cB = (long) (costConstrain - cost);
		int util = Log.getUtilization(wb);
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" DBCS H.Arabnejad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);
		else
			System.out.print(" DBCS H.Arabnejad: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " balance:" + Math.abs(tB - cB) + " UT:" + util);

		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);
		// Log.printInstances(wb);
		// Log.printWorkflowChartDetailed(wb, true, true, false, false);
		// Log.printCritialPathes(wb);
		// Log.printResourcesUsed(wb);
		// Log.printAvailableResources(wb);
		if (instancePrint) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " DBCS H.ArabNejad cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		System.out.println("");
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids, "DBCS", finishTime, cost, NC, NT,
				wb.policy.instances, dl, costConstrain, util);
	}


	public static void RunBDHEFT(String WfFile, int dl, float costConstrain, Boolean instancePrint,
			Boolean graphChartPrint, Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval,
			int bandwidth) {

		// System.out.println("-BDHEFT Schedule Deadline=" + deadline + " Cost
		// Constrained:"+ costConstrained);
		// System.out.println(" *******BDHEFT COST constrain:" + costConstrain + "
		// Deadline=" + dl);
		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.BDHEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		int util = Log.getUtilization(wb);
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		else
			System.out.print(" BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " BDHEFT cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);

		System.out.println("");
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		SimulationResults.criticalPaths.clear();
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids, "BDHEFT", finishTime, cost, NC, NT,
				wb.policy.instances, dl, costConstrain, util);
	}

	public static void RunMyBDHEFT(String WfFile, int dl, float costConstrain, Boolean instancePrint,
			Boolean graphChartPrint, Boolean CriticalPathPrint, Boolean ResourceUsedPrint, int res, int interval,
			int bandwidth) {

		// System.out.println("-BDHEFT Schedule Deadline=" + deadline + " Cost
		// Constrained:"+ costConstrained);
		// System.out.println(" *******My BDHEFT COST constrain:" + costConstrain + "
		// Deadline=" + dl);
		int startTime = 0, finishTime;

		float cost, CC;
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.MYBDHEFT, interval, bandwidth, res);
		} catch (Exception e) {
		}
		// realStartTime = System.currentTimeMillis();
		cost = wb.schedule(startTime, dl, costConstrain);
		int util = Log.getUtilization(wb);
		finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
		if ((costConstrain < cost) || (dl < finishTime))
			System.err.print(" My BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);
		else
			System.out.print(" My BDHEFT: cost=" + cost + " time=" + finishTime + " NC=" + cost / CheapestCost
					+ " Instances=" + wb.policy.instances.getSize() + " UT:" + util);

		if (costConstrain < cost)
			System.out.print(" *{Failed Cost}");
		if (dl < finishTime)
			System.out.print(" *{Failed Time}");
		// System.out.println("System Time=" + realFinishTime + " \n");
		// Log.printInstances(wb.policy.instances, wb.graph);

		if (instancePrint && (cost > 0)) {
			// Log.printInstancesFull(wb);
			instanceChart.PrintInstanceGantt(wb, WfFile + " MY BDHEFT cost=" + cost + " time=" + finishTime
					+ " COST constrain:" + costConstrain + " Deadline=" + dl);
		}
		if (graphChartPrint)
			Log.printWorkflowChartDetailed(wb, true, true, false, false);
		if (CriticalPathPrint)
			Log.printCritialPathes(wb);
		if (ResourceUsedPrint)
			Log.printResourcesUsed(wb);
		float NC = (float) cost / CheapestCost;
		float NT = (float) finishTime / HeftTime;
		System.out.println("");
		SimulationResults.criticalPaths.clear();
		String ids = dl +"-"+ costConstrain +"-"+ interval +"-"+ WfFile;
		utility.Utility.saveAlgorithmResult(ids,"MyBDHEFT", finishTime, cost, NC,
				NT, wb.policy.instances, dl, costConstrain, util);

	}

}
