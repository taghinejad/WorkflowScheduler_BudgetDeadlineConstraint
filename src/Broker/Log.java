package Broker;

import java.util.List;
import java.util.PriorityQueue;

import com.sun.crypto.provider.RSACipher;
import com.sun.jmx.snmp.tasks.Task;

import Broker.Instance.execution;
import Broker.WorkflowPolicy.ASTComparator;
import utility.SimulationResults;

public class Log {

	public static void GraphSummery() {
		System.out.println("Graph Details:=" + "Real Computation:" + SimulationResults.computation + " DataTransfer:"
				+ SimulationResults.datatransfer + " RunTime:" + SimulationResults.runTime + " CompRate:"
				+ SimulationResults.ComputationRate2data + " DataRate:" + SimulationResults.DataRate2Comp + " Tasks:"
				+ SimulationResults.Tasks);
	}
	public static void print(String str)
	{
		System.out.println(str);
	}
	public static void printWorkflow(WorkflowGraph g) {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(g.nodes.size(),
				new WorkflowPolicy.ASTComparator());

		for (WorkflowNode node : g.nodes.values())
			if (!node.getId().equals(g.getStartId()) && !node.getId().equals(g.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode n = queue.remove();
			if (n.getRunTime() > 0)
				System.out.println("Id=" + n.getId() + " RT=" + n.getRunTime() + " SR=" + n.getSelectedResource()
						+ " AST=" + n.getEST() + " AFT=" + n.getEFT() + " Deadline=" + n.getDeadline());
		}
	}

	public static void printFiles() {
//Do not distinguish between file types
		System.out.print("\n Files Ignoreing Link");
		for (int i = 0; i < FileSets.Size(); i++) {
			System.out.print("\n File"+i+" = " + FileSets.get(i).getFileName() + " size = " + FileSets.get(i).getFileSize()
					+ " _NodesSize:"+FileSets.get(i).nodes.size()+" :");
			for (String str : FileSets.get(i).nodes) {
				System.out.print("[" + str + "]");
			}
		}
	}

	public static void printFilesTypeSensitive() {
// print a file as an input ann also with output
		System.out.print("\n Files with Links");
		for (int i = 0; i < FileSets.SizeByType(); i++) {
			System.out.print("\n File"+i+" = " + FileSets.getByType(i).getFileName() + " size = " + FileSets.getByType(i).getFileSize()
					+ " type = " + FileSets.getByType(i).getLink().toString()+ " _NodesSize:"+FileSets.getByType(i).nodes.size()+" :");
			for (String str : FileSets.getByType(i).nodes) {
				System.out.print("[" + str + "]");
			}
		}
	}

	public static void printWorkflowDetailed(WorkflowGraph g) {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(g.nodes.size(),
				new WorkflowPolicy.ASTComparator());

		for (WorkflowNode node : g.nodes.values())
			if (!node.getId().equals(g.getStartId()) && !node.getId().equals(g.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode n = queue.remove();
			if (n.getRunTime() > 0)
				System.out.println("Id=" + n.getId() + " RT=" + n.getRunTime() + " SR=" + n.getSelectedResource()
						+ " AST=" + n.getEST() + " AFT=" + n.getEFT() + " Deadline=" + n.getDeadline() + " InstSize: "
						+ n.getInstructionSize() + " input:" + n.getInputFileSize() + " output:" + n.getOutputFileSize()
						+ " input:" + n.getMET() + " Toplevel: " + n.getLevelTop() + " BotLevel: " + n.getLevelBottem()
						+ " children:" + n.getChildrenIDString());
		}

	}

	public static void printWorkflowChart(WorkflowGraph g) {

		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(g.nodes.size(),
				new WorkflowPolicy.ASTComparator());
		for (WorkflowNode node : g.nodes.values())
			if (!node.getId().equals(g.getStartId()) && !node.getId().equals(g.getEndId()))
				queue.add(node);

		int levelCount = g.nodes.get(g.getEndId()).getLevelTop();
		// String[] levelNodes = new String[levelCount];
		System.out.println("\n Start the Workflow Chart---------------");
		for (int i = 1; i < levelCount; i++) {
			for (WorkflowNode node : g.nodes.values()) {

				int lvl = node.getLevelTop();
				if (lvl == i) {
					System.out.print(node.getId() + "    ");
				}
			}
			System.out.println("");
		}
		System.out.println("end the Workflow Chart---------------");
	}

//	public static void printCritialPathes(WorkflowBroker wb) {
//		int i = 0;
//		InstanceSet instances = wb.policy.instances;
//		Instance ins;
//		System.out.println("--------\nCriticalPathses size:" + SimulationResults.criticalPaths.size());
//		for (List<WorkflowNode> criticalPath : SimulationResults.criticalPaths) {
//			ins = instances.getInstance(criticalPath.get(0).getSelectedResource());
//			System.out.print("\n      P" + (i + 1) + ": ");
//			long comp = 0;
//			long trans = 0;
//			long SumComp = 0, SumTrans = 0;
//			Boolean condition = false;
//			for (WorkflowNode wf : criticalPath) {
//				comp = Math.round(
//						wf.getInstructionSize() / instances.getInstance(wf.getSelectedResource()).getType().getMIPS());
//				trans = wf.getNeedTransferTime();
//				SumTrans += trans;
//				SumComp += comp;
//				if (condition)
//					System.out.print("->[#" + wf.getId() + " cm:" + comp + " dt:" + trans + "]");
//				else
//					System.out.print("[#" + wf.getId() + " cm:" + comp + " dt:" + trans + "]");
//				condition = true;
//			}
//			i++;
//			System.out.print("   :{totalComp:" + SumComp + " TotalData:" + SumTrans + "  | Inst=>"
//					+ criticalPath.get(0).getSelectedResource() + " Typ=>" + ins.getType().getId() + "} ");
//		}
//		System.out.print("\n--------");
//	}
//
	public static void printCritialPathes(WorkflowBroker wb) {
		int i = 0;
		InstanceSet instances = wb.policy.instances;
		Instance ins;
		System.out.println("--------\nCriticalPathses size:" + SimulationResults.criticalPaths.size());
		for (List<WorkflowNode> criticalPath : SimulationResults.criticalPaths) {
			ins = instances.getInstance(criticalPath.get(0).getSelectedResource());
			System.out.print("\n      P" + (i + 1) + ": ");
			long comp = 0;
			long trans = 0;
			long SumComp = 0, SumTrans = 0;
			Boolean condition = false;
			for (WorkflowNode wf : criticalPath) {
				comp = Math.round(
						wf.getInstructionSize() / instances.getInstance(wf.getSelectedResource()).getType().getMIPS());
				trans = wf.getNeedTransferTime();
				SumTrans += trans;
				SumComp += comp;
				//condition defines that if the task is start of the path or not;
				if (condition)
					System.out.print("->[#" + wf.getId() + " EST:" + wf.getEST() + " EFT:" + wf.getEFT() + " LST:" + wf.getLST() + " LFT:" + wf.getLFT()+ "]");
				else
					System.out.print("[#" + wf.getId() + " EST:" + wf.getEST() + " EFT:" + wf.getEFT() + " LST:" + wf.getLST() + " LFT:" + wf.getLFT()+ "]");
				condition = true;
			}
			i++;
			System.out.print("   :{totalComp:" + SumComp + " TotalData:" + SumTrans + "  | Inst=>"
					+ criticalPath.get(0).getSelectedResource() + " Typ=>" + ins.getType().getId() + "} ");
		}
		System.out.print("\n--------");
	}


	public static void printInstances(WorkflowBroker wb) {
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;

		System.out.println("Instances = " + instances.getSize());
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
			// int UsedTime=g.nodes.get(cur.getLastTask()).getEFT()-
			// g.nodes.get(cur.getFirstTask()).getEST();
			cur.CalculateTasksTimeInInstances();
			float cost = ((float) Math
					.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / WorkflowBroker.interval))
					* cur.getType().getCost();
			System.out.println("id=" + cur.getId() + " {type=" + cur.getType().getId() + "} start="
					+ g.nodes.get(cur.getFirstTask()).getEST() + " end= " + g.nodes.get(cur.getLastTask()).getEFT()
					+ " cost:" + cur.getType().getCost() + " TotalCost:" + cost + " runTime=" + cur.getRunTime()
					+ " IdleTime=" + cur.getIdleTime() + " TaskSize=" + cur.getTasks().size() + " Utilization="
					+ cur.getUtilization());
		}
	}
	public static int getUtilization(WorkflowBroker wb) {
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;
		int util=0;
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
			// int UsedTime=g.nodes.get(cur.getLastTask()).getEFT()-
			// g.nodes.get(cur.getFirstTask()).getEST();
			cur.CalculateTasksTimeInInstances();
//			float cost = ((float) Math
//					.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / WorkflowBroker.interval))
//					* cur.getType().getCost();
//			System.out.println("id=" + cur.getId() + " {type=" + cur.getType().getId() + "} start="
//					+ g.nodes.get(cur.getFirstTask()).getEST() + " end= " + g.nodes.get(cur.getLastTask()).getEFT()
//					+ " cost:" + cur.getType().getCost() + " TotalCost:" + cost + " runTime=" + cur.getRunTime()
//					+ " IdleTime=" + cur.getIdleTime() + " TaskSize=" + cur.getTasks().size() + " Utilization="
//					+ cur.getUtilization());
			util+= Math.round(cur.getUtilization());
			
		}
		util= Math.round((float)util / instances.getSize());
		return util;
	}
	public static int getUtilizationByInstances(InstanceSet instances) {
		int util=0;
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
			// int UsedTime=g.nodes.get(cur.getLastTask()).getEFT()-
			// g.nodes.get(cur.getFirstTask()).getEST();
			cur.CalculateTasksTimeInInstances();
//			float cost = ((float) Math
//					.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / WorkflowBroker.interval))
//					* cur.getType().getCost();
//			System.out.println("id=" + cur.getId() + " {type=" + cur.getType().getId() + "} start="
//					+ g.nodes.get(cur.getFirstTask()).getEST() + " end= " + g.nodes.get(cur.getLastTask()).getEFT()
//					+ " cost:" + cur.getType().getCost() + " TotalCost:" + cost + " runTime=" + cur.getRunTime()
//					+ " IdleTime=" + cur.getIdleTime() + " TaskSize=" + cur.getTasks().size() + " Utilization="
//					+ cur.getUtilization());
			util+= Math.round(cur.getUtilization());
			
		}
		util= Math.round((float)util / instances.getSize());
		return util;
	}
	public static void printInstancesFull(WorkflowBroker wb) {
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;

		System.out.println("Instances = " + instances.getSize());
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);
			// int UsedTime=g.nodes.get(cur.getLastTask()).getEFT()-
			// g.nodes.get(cur.getFirstTask()).getEST();
			cur.CalculateTasksTimeInInstances();
			float cost = ((float) Math
					.ceil((double) (cur.getFinishTime() - cur.getStartTime()) / WorkflowBroker.interval))
					* cur.getType().getCost();
			System.out.println("\n ++rsid=" + cur.getId() + " {type=" + cur.getType().getId()+" startRead:"+cur.getStartTime() + "} startComp="
					+ g.nodes.get(cur.getFirstTask()).getEST() + " end= " + g.nodes.get(cur.getLastTask()).getEFT()
					+ " cost:" + cur.getType().getCost() + " TotalCost:" + cost + " runTime=" + cur.getRunTime()
					+ " IdleTime=" + cur.getIdleTime() + " TaskSize=" + cur.getTasks().size() + " Utilization="
					+ cur.getUtilization());
			for (int j = 0; j < cur.getExeList().size(); j++) {
				execution ex=cur.getExeList().get(j);
				System.out.print("[#"+ex.id + " s:" +ex.start+ " f:" + ex.finish + "]");
			}
		}
	}

	public static void printAvailableResources(WorkflowBroker wb) {

		ResourceSet resources = wb.policy.resources;
		Resource rs;
		System.out.println("Available Resources:");
		for (int i = 0; i < resources.getSize(); i++) {
			rs = resources.getResource(i);
			System.out.println("Resource id=" + rs.getId() + " mips:" + rs.getMIPS() + " cost:" + rs.getCost());
		}
	}

	public static int ComputeNumberOfResourceTypeInInstances(InstanceSet instances, Resource r) {
		// Returns count of a resouce on Instance Set
		int count = 0;
		for (int i = 0; i < instances.getSize(); i++) {
			if (instances.getInstance(i).getType().getId() == r.getId())
				count++;
		}
		return count;
	}

	public static void printResourcesUsed(WorkflowBroker wb) {
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;
		ResourceSet rs = new ResourceSet(wb.getInterval());
		rs.addResource(instances.getInstance(0).getType());

		if (instances.getSize() > 1) {
			for (int i = 1; i < instances.getSize(); i++) {
				Instance cur = instances.getInstance(i);
				if (rs.getResourceByID(cur.getType().getId()) == null)
					rs.addResource(cur.getType());
			}
		}
		System.out.println("\n----\nDifferent Resources Used = " + rs.getSize());
		for (int i = 0; i < rs.getSize(); i++) {
			System.out.println("R" + rs.getResource(i).getId() + " MIPS:" + rs.getResource(i).getMIPS() + " Cost:"
					+ rs.getResource(i).getCost() + " CountOfInst:"
					+ ComputeNumberOfResourceTypeInInstances(wb.policy.instances, rs.getResource(i)));
		}
		System.out.println("------");
	}

	// public static void CalculateTasksTimeInInstances(InstanceSet instances,
	// WorkflowGraph g) {
	// System.out.println("Instances = " + instances.getSize());
	// for (int i = 0; i < instances.getSize(); i++) {
	// Instance cur = instances.getInstance(i);
	// long InsRunTime=0;
	// for (WorkflowNode ts : cur.getTasks()) {
	// InsRunTime+= ts.getEFT()-ts.getEST();
	// }
	//
	// int UsedTime=g.nodes.get(cur.getLastTask()).getEFT()-
	// g.nodes.get(cur.getFirstTask()).getEST();
	//
	// System.out.println("id=" + cur.getId() + " type=" + cur.getType().getId() + "
	// start="
	// + g.nodes.get(cur.getFirstTask()).getEST() + " end= " +
	// g.nodes.get(cur.getLastTask()).getEFT()
	// + " task count=" + cur.getTasks().size());
	// }
	// }

	public static void printInstancesWithTasks(WorkflowBroker wb) {
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;
		int interval = wb.getInterval();

		System.out.println("Instances = " + instances.getSize());
		for (int i = 0; i < instances.getSize(); i++) {
			Instance cur = instances.getInstance(i);

			float cost = (float) Math.ceil((cur.getFinishTime() - cur.getStartTime()) / (double) interval)
					* cur.getType().getCost();
			System.out.println("\n *ins=" + cur.getId() + " type=" + cur.getType().getId() + " start="
					+ g.nodes.get(cur.getFirstTask()).getEST() + " end= " + g.nodes.get(cur.getLastTask()).getEFT()
					+ " task count=" + cur.getTasks().size() + " total cost=" + cost + " cost:"
					+ cur.getType().getCost() + " mips:" + cur.getType().getMIPS());
			for (WorkflowNode ts : cur.getTasks()) {
				System.out.print(" t: " + ts.getId() + " ,");
			}
			System.out.print("\n");
		}
	}

	public static Instance FindInstanceContainTask(WorkflowNode task, InstanceSet instances) {
		Instance ins = null;
		for (int j = 0; j < instances.getSize(); j++) {
			Instance cur = instances.getInstance(j);
			for (WorkflowNode ts : cur.getTasks()) {
				if (ts.getId() == task.getId())
					return cur;
			}
		}
		return ins;

	}

	public static void printWorkflowChartDetailed(WorkflowBroker wb, Boolean ComputationTime, Boolean DTransfer,
			Boolean ChildrenData, Boolean ParentsData) {
		ResourceSet resources = wb.getResources();
		InstanceSet instances = wb.policy.instances;
		WorkflowGraph g = wb.graph;

		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(g.nodes.size(),
				new WorkflowPolicy.ASTComparator());
		for (WorkflowNode node : g.nodes.values())
			if (!node.getId().equals(g.getStartId()) && !node.getId().equals(g.getEndId()))
				queue.add(node);

		int levelCount = g.nodes.get(g.getEndId()).getLevelTop();
		// String[] levelNodes = new String[levelCount];
		System.out.println("\n Workflow Chart By TopLevel");
		for (int i = 1; i < levelCount; i++) {
			System.out.print("\n*level:" + i + " {");
			for (WorkflowNode node : g.nodes.values()) {

				int lvl = node.getLevelTop();
				if (lvl == i) {
					System.out.print(" [" + node.getId());
					System.out.print(" rn:" + node.getRunTime());
					System.out.print(" read:" + node.getReadFileSize());
					System.out.print(" Ndt:" + node.getNeedTransferTime());
					System.out.print(" Cores:" + node.getCores());
					if (node.getCriticalParent() != null)
						System.out.print(" CrParent:" + node.getCriticalParent().getId());
					WorkflowNode wn;

					// System.out.print(" @Ins:" + node.getSelectedResource());

					// calculate Computation time of Task
					/*
					 * 
					 * @author Ahmad.t72
					 */
					if (ComputationTime) {
						Resource rs = instances.getInstance(node.getSelectedResource()).getType();
						System.out.print(" Compute:" + Math.round(node.getInstructionSize() / rs.getMIPS()));
					}
					if (DTransfer) {
						// calculates DataTransfer time from all of the tasks fathers
						long dtmax = -1, dt = 0;
						for (Link lnk : node.getParents()) {
							dt = node.getParentDataSize(lnk.getId());
							if (dt > dtmax)
								dtmax = dt;
						}
						System.out.print(" Dt:" + (double) (dtmax / wb.getBandwidth()) + "]");
					}

					if (ChildrenData) {
						// calculates data size of every node's children;
						long dsize = 0;
						if (!node.getChildren().isEmpty())
							System.out.print("{ ->");
						for (Link lnk : node.getChildren()) {

							dsize = node.getChildDataSize(lnk.getId());
							System.out.print(", " + lnk.getId() + ":" + Math.floor(dsize / wb.getBandwidth()));
						}

						System.out.print(" } - ");
					}

					if (ParentsData) {
						long dsize = 0;

						if (node.getParents().size() == 1 && node.getParents().get(0).getId().contains("start"))
							continue;
						if (!node.getParents().isEmpty())
							System.out.print("{ <-");

						for (Link lnk : node.getParents()) {
							if (!lnk.getId().contains("start")) {
								dsize = node.getParentDataSize(lnk.getId());
								System.out.print(", " + lnk.getId() + ":" + Math.floor(dsize / wb.getBandwidth()));
							}
						}
						System.out.print(" } - ");
					}
				}
			}
			System.out.print("}");
		}
		System.out.print("\n end the Workflow Chart---------------");
	}

}
