package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.Instance.execution;
import algorithms.NoConstrained.HEFTAlgorithm.result;

//A. Verma and S. Kaushal, “Cost-Time Efficient Scheduling Plan for Executing Workflows in the Cloud,” J. Grid Comput., 2015.
public class BDHEFTalgorithm extends WorkflowPolicy {
	boolean backCheck = true;
	float userCost = 0;
	float userDeadline = 0;
float factorTime=(float)0.48;
float factorBudget=(float)0.52;
	// this is updated BDHEFT to cloud system. it orginal one use grid system
	// mechanism.
	public BDHEFTalgorithm(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	public float schedule(int startTime, int deadline) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float schedule(int startTime, int deadline, float cost) {

		setRuntimes();
		computeESTandEFT(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);

		planning(cost, deadline);

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	private float calcSWB(float cost) {
		// Spare Workflow Budget
		float c = 0;
		float selectCost = 0;
		float NotselectCost = 0;

		result r;
		float finishTime = 0;
		int interval = resources.getInterval();
		double curCost = 0;
		double TotalCost = 0;
		double SelectedCost = 0;
		double eachCost = 0;
		double meanEachCost = 0;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			finishTime = instances.getInstance(curInst).getFinishTime();
			for (WorkflowNode node : graph.nodes.values()) {
				if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
					if (!node.isScheduled()) {
						finishTime += Math.round(
								(float) node.getInstructionSize() / instances.getInstance(curInst).getType().getMIPS());
						eachCost += checkInstanceFinish(Math.round(finishTime), instances.getInstance(curInst)).cost;
					}
			}
			// curCost = Math.ceil((double) (finishTime - 0) / (double) interval)
			// * resources.getResource(curRes).getCost();
			curCost = checkInstanceFinish(Math.round(finishTime), instances.getInstance(curInst)).cost;

			TotalCost += curCost;
		}

		// calculated mean Cost of nodes that is not scheduled by TotalCost variable;
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) {
			finishTime = 0;
			for (WorkflowNode node : graph.nodes.values()) {
				if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
					if (!node.isScheduled()) {
						finishTime += Math
								.round((float) node.getInstructionSize() / resources.getResource(curRes).getMIPS());
						eachCost += Math.ceil((double) (finishTime) / (double) interval)
								* resources.getResource(curRes).getCost();
					}
			}
			curCost = Math.ceil((double) (finishTime) / (double) interval) * resources.getResource(curRes).getCost();
			TotalCost += curCost;
		}
		TotalCost = TotalCost / (resources.getSize() + instances.getSize());
		meanEachCost = eachCost / (resources.getSize() + instances.getSize());
		for (WorkflowNode node : graph.nodes.values()) {
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				if (node.isScheduled()) {
					SelectedCost = instances.getInstance(node.getSelectedResource()).getExectionTask(node.getId()).Cost;
				}
		}

		c = (float) (userCost - TotalCost - selectCost);
		return c;
	}

	private float calcSWD(float deadline) {
		// Spare Workflow Deadline
		float c = deadline;
		float selectTime = 0;
		float NotselectTime = 0;
		execution ex;
		for (WorkflowNode node : graph.nodes.values()) {
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				if (node.isScheduled()) {
					ex = instances.getInstance(node.getSelectedResource()).getExectionTask(node.getId());
					selectTime += ex.getFinish() - ex.getStart();
				} else {
					NotselectTime += meanRunTime(node);
				}
		}
		c = userDeadline - selectTime - NotselectTime;

		return c;
	}

	// private float meanCost(WorkflowNode wn) {
	// int interval = resources.getInterval();
	// float NodeCost = 0;
	// long nodeFinish = 0;
	// for (int curInst = 0; curInst < instances.getSize(); curInst++) {
	//
	// nodeFinish = instances.getInstance(curInst).getFinishTime()
	// + Math.round((float) wn.getInstructionSize() /
	// instances.getInstance(curInst).getType().getMIPS());
	// NodeCost += checkInstanceFinish(nodeFinish,
	// instances.getInstance(curInst)).cost;
	//
	// }
	//
	// // calculated mean Cost of nodes that is not scheduled by TotalCost variable;
	// for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) {
	//
	// nodeFinish = Math.round((float) wn.getInstructionSize() /
	// resources.getResource(curRes).getMIPS());
	// NodeCost += Math.ceil((double) (nodeFinish - 0) / (double) interval)
	// * resources.getResource(curRes).getCost();
	//
	// }
	// NodeCost = NodeCost / (resources.getSize() + instances.getSize());
	// return NodeCost;
	// }

	private float calcBAF(WorkflowNode wn, float SWB) {
		// Budget Adjustment Factor (
		if (SWB < 0)
			return 0;

		float finishTime = 0;
		int interval = resources.getInterval();
		double curCost = 0;
		double TotalCost = 0;
		double SelectedCost = 0;

		// for (int curInst = 0; curInst < instances.getSize(); curInst++) {
		// finishTime = instances.getInstance(curInst).getFinishTime();
		// for (WorkflowNode node : graph.nodes.values()) {
		// if (!node.getId().equals(graph.getStartId()) &&
		// !node.getId().equals(graph.getEndId())
		// && !node.getId().contains(wn.getId()))
		// finishTime += Math.round(
		// (float) node.getInstructionSize() /
		// instances.getInstance(curInst).getType().getMIPS());
		//
		// }
		//
		// // curCost = Math.ceil((double) (finishTime - 0) / (double) interval)
		// // * resources.getResource(curRes).getCost();
		// curCost += checkInstanceFinish(Math.round(finishTime),
		// instances.getInstance(curInst)).cost;
		// TotalCost += curCost;
		// }

		// calculated mean Cost of nodes that is not scheduled by TotalCost variable;
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) {
			finishTime = 0;
			for (WorkflowNode node : graph.nodes.values()) {
				if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId())
						&& !node.getId().contains(wn.getId()))
					finishTime +=node.getReadTime(bandwidth)+ Math
							.round((float) node.getInstructionSize() / resources.getResource(curRes).getMIPS());
			}
			curCost = Math.ceil((double) (finishTime - 0) / (double) interval)
					* resources.getResource(curRes).getCost();
			TotalCost += curCost;

		}
		// TotalCost = TotalCost / (resources.getSize() + instances.getSize());
		TotalCost = TotalCost / (resources.getSize());

		float NodeCost = meanCost(wn);
		return (float) (NodeCost / TotalCost);
	}

	private float calcDAF(WorkflowNode wn, float SWD, List<WorkflowNode> lst) {
		// Deadline Adjustment Factor (
		if (SWD < 0)
			return 0;

		float curNode = meanRunTime(wn);

		float NotselectCost = curNode;
		for (WorkflowNode node : graph.nodes.values()) {
			if (!node.getId().equals(graph.getStartId()) && node.getId() != wn.getId()
					&& node.getId().equals(graph.getEndId()) && !lst.contains(node))
				NotselectCost += meanRunTime(node);
		}
		curNode = curNode / NotselectCost;
		return curNode;
	}

	private void planning(float cost, float deadline) {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.UpRankComparator());
		result r;
		List<WorkflowNode> lst = new ArrayList<WorkflowNode>();
		long bestFinish = Integer.MAX_VALUE;
		long finishTime = -1;
		float bstFinish = Float.MAX_VALUE;
		float bstCost = Float.MAX_VALUE;
		userCost = cost;
		userDeadline = deadline;
		float balance = 0;
		float bestbalance = Float.MAX_VALUE;
		computeUpRank();
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();
			lst.add(curNode);
			int bestInst = -1;
			int bstCurInstance = -1;
			float bestCost = Float.MAX_VALUE;
			float SWB = cost;
			float SWD = deadline;
			bestFinish = Integer.MAX_VALUE;

			SWB = calcSWB(cost);
			SWD = calcSWD(deadline);
			float BAF = calcBAF(curNode, SWB);
			float DAF = calcDAF(curNode, SWD, lst);
			float CTB = meanCost(curNode) + SWB * BAF;
			float CTD = meanRunTime(curNode) + SWD * DAF;
			bestbalance = Float.MAX_VALUE;
			// constructing Diffrenet Resources;
			for (int curInst = 0; curInst < instances.getSize(); curInst++) {
				r = checkInstance(curNode, instances.getInstance(curInst));
				if (r.finishTime <= CTD && r.cost <= CTB) {
					// float normFinish,normCost;
					// normFinish= (r.finishTime- 1) / (deadline-1);
					// normCost=(r.cost- 0) /(cost-0);
					// balance = (float) (0.5 * normFinish + (0.5 * normCost));
					balance = (float) ((factorTime * r.finishTime) + (factorBudget * r.cost));
					// balance = (float) (0.5 * r.finishTime + (0.5 * r.cost));
					if (bestbalance > balance) {
						bestbalance = balance;
						bestInst = curInst;
						bstCurInstance = curInst;
					}
				}
			}
			for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
				Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
				r = checkInstance(curNode, inst);
				if (r.finishTime <= CTD && r.cost <= CTB) {
					// we should use normaliziation between cost and finishTime to get a good
					// results
					// float normFinish,normCost;
					// normFinish= (r.finishTime- 1) / (deadline-1);
					// normCost=(r.cost- 0) /(cost-0);
					// balance = (float) (0.5 * normFinish + (0.5 * normCost));
					balance = (float) ((factorTime * r.finishTime) + (factorBudget * r.cost));
					// balance = (float) (0.5 * r.finishTime + (0.5 *10* r.cost));
					if (bestbalance > balance) {
						bestbalance = balance;
						bestInst = 10000 + curRes;
					}
				}
			}
			if (bestInst == -1) {
				// if SWB >= 0 chose a resources which have low balance;
				if (SWB >= 0) {
					for (int curInst = 0; curInst < instances.getSize(); curInst++) {
						r = checkInstance(curNode, instances.getInstance(curInst));
						float normFinish, normCost;
						// normFinish= (r.finishTime- 1) / (deadline-1);
						// normCost=(r.cost- 0) /(cost-0);
						// balance = (float) (0.5 * normFinish + (0.5 * normCost));
						balance = (float) (factorTime * r.finishTime + (factorBudget * 10 * r.cost));
						if (bestbalance > balance) {
							bestbalance = balance;
							bestInst = curInst;
							bstCurInstance = curInst;
						}

					}
					for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is
																						// // the last
						Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
						r = checkInstance(curNode, inst);
						// float normFinish,normCost;
						// normFinish= (r.finishTime- 1) / (deadline-1);
						// normCost=(r.cost- 0) /(cost-0);
						// balance = (float) (0.5 * normFinish + (0.5 * normCost));
						balance = (float) ((factorTime * r.finishTime) + (factorBudget * r.cost));
						// balance = (float) (0.5 * r.finishTime + (0.5*10 * r.cost));
						if (bestbalance > balance) {
							bestbalance = balance;
							bestInst = 10000 + curRes;
						}
					}
				}
				// if SWB < 0 and SWD<0 chose a resources which have cheap resource;
				else {
					float cheapest = Float.MAX_VALUE;
					for (int curInst = 0; curInst < instances.getSize(); curInst++) {
						r = checkInstance(curNode, instances.getInstance(curInst));

						if (r.cost < cheapest) {
							cheapest = r.cost;
							bestInst = curInst;
						}

					}
					for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is
																						// // the last
						Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
						r = checkInstance(curNode, inst);
						if (r.cost < cheapest) {
							cheapest = r.cost;
							bestInst = 10000 + curRes;
						}
					}
				}

				// System.out.print("#%^$%&@!#$ Can't Continue with this DeadLine
				// ,ListPolicy2");
				// return;
				// bestInst=0;
			}
			if (bestInst < 10000)
				setInstance(curNode, instances.getInstance(bestInst));
			else {
				bestInst -= 10000;
				Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
				instances.addInstance(inst);
				setInstance(curNode, inst);
			}
			//
		}
	}

	// private float meanRunTime(WorkflowNode curNode) {
	// float cost = 0;
	// result r;
	// float finishTime = 0;
	//
	// for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { //
	// because the cheapest one is // the last
	// // Instance inst = new Instance(instances.getSize(),
	// // resources.getResource(curRes));
	// // r = checkInstanceRun(curNode, inst);
	// finishTime += Math.round((float) curNode.getInstructionSize() /
	// resources.getResource(curRes).getMIPS());
	// }
	// finishTime = finishTime / resources.getSize();
	// return finishTime;
	// }

	// private float meanCost(WorkflowNode curNode) {
	// float cost = 0;
	// result r;
	// for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { //
	// because the cheapest one is // the last
	// Instance inst = new Instance(instances.getSize(),
	// resources.getResource(curRes));
	// r = checkInstance(curNode, inst);
	// cost += r.cost;
	// }
	// int bestInst = -1;
	//
	// for (int curInst = 0; curInst < instances.getSize(); curInst++) {
	// r = checkInstanceRun(curNode, instances.getInstance(curInst));
	// if (r.cost < bestCost) {
	// bestCost = r.cost;
	// bestFinish = r.finishTime;
	// bestInst = curInst;
	// } else if (bestCost < Float.MAX_VALUE && r.cost == bestCost && r.finishTime <
	// bestFinish) {
	// bestFinish = r.finishTime;
	// bestInst = curInst;
	// }
	// }
	// for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { //
	// because the cheapest one is the last
	// Instance inst = new Instance(instances.getSize(),
	// resources.getResource(curRes));
	// r = checkInstance(curNode, inst);
	// if (r.cost < bestCost) {
	// bestCost = r.cost;
	// bestFinish = r.finishTime;
	// bestInst = 10000 + curRes;
	// } else if (bestCost < Float.MAX_VALUE && r.cost == bestCost && r.finishTime <
	// bestFinish) {
	// bestFinish = r.finishTime;
	// bestInst = 10000 + curRes;
	// }
	// }
	// if (bestInst == -1) {
	// System.out.print("#%^$%&@!#$ Can't Continue with this DeadLine
	// ,ListPolicy2");
	// return;
	// // bestInst=0;
	// }
	// if (bestInst < 10000)
	// setInstance(curNode, instances.getInstance(bestInst));
	// else {
	// bestInst -= 10000;
	// Instance inst = new Instance(instances.getSize(),
	// resources.getResource(bestInst));
	// instances.addInstance(inst);
	// setInstance(curNode, inst);
	// }
	//
	// cost = cost / resources.getSize();
	// return cost;
	// }
	//
	private result checkInstance(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = (int) finishTime, curFinish;

		if (utility.Utility.readConsideration == true) {
			curStart = super.GetMyFileTransferTime(curStart, curNode, curInst);
		} else {
			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				start = parentNode.getEFT();
				if (parentNode.getSelectedResource() != curInst.getId())
					start += Math.round((float) parent.getDataSize() / bandwidth);
				if (start > curStart)
					curStart = start;
			}
		}

		if (finishTime == 0)
			startTime = curStart;

		result r = new result();
		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		r.finishTime = (int) curFinish;

		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		return r;
	}

	private result checkInstanceRun(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = (int) finishTime, curFinish;

		// if (utility.Utility.readConsideration == true) {
		// curStart = super.GetMyFileTransferTime(curStart, curNode, curInst);
		// } else {
		// for (Link parent : curNode.getParents()) {
		// WorkflowNode parentNode = graph.getNodes().get(parent.getId());
		// start = parentNode.getEFT();
		// if (parentNode.getSelectedResource() != curInst.getId())
		// start += Math.round((float) parent.getDataSize() / bandwidth);
		// if (start > curStart)
		// curStart = start;
		// }
		// }

		if (finishTime == 0)
			startTime = curStart;

		result r = new result();
		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		r.finishTime = (int) curFinish;

		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		return r;
	}

	// private result checkInstanceFinish(long curFinish, Instance curInst) {
	//
	// long finishTime = curInst.getFinishTime();
	// long startTime = curInst.getStartTime();
	//
	// int interval = resources.getInterval();
	// double curCost = Math.ceil((double) (finishTime - startTime) / (double)
	// interval) * curInst.getType().getCost();
	// long curIntervalFinish = startTime
	// + (long) Math.ceil((double) (finishTime - startTime) / (double) interval) *
	// interval;
	// long start, curStart = (int) finishTime;
	//
	// // if (utility.Utility.readConsideration == true) {
	// // curStart = super.GetMyFileTransferTime(curStart, curNode, curInst);
	// // } else {
	// // for (Link parent : curNode.getParents()) {
	// // WorkflowNode parentNode = graph.getNodes().get(parent.getId());
	// // start = parentNode.getEFT();
	// // if (parentNode.getSelectedResource() != curInst.getId())
	// // start += Math.round((float) parent.getDataSize() / bandwidth);
	// // if (start > curStart)
	// // curStart = start;
	// // }
	// // }
	//
	// if (finishTime == 0)
	// startTime = curStart;
	//
	// result r = new result();
	// if (curFinish < startTime)
	// return r;
	// // curFinish = curStart + Math.round((float) curNode.getInstructionSize() /
	// // curInst.getType().getMIPS());
	// r.finishTime = (int) curFinish;
	//
	// r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double)
	// interval) * curInst.getType().getCost()
	// - curCost);
	// return r;
	// }

	private void setInstance(WorkflowNode curNode, Instance curInst) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();

		if (utility.Utility.readConsideration == true) {
			curStart = super.GetMyFileTransferTime(curStart, curNode, curInst);
		} else {
			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());

				start = parentNode.getEFT();
				if (parentNode.getSelectedResource() != curInst.getId())
					start += Math.round((float) parent.getDataSize() / bandwidth);
				if (start > curStart)
					curStart = start;
			}
		}

		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		float Cost = (float) (Math.ceil((double) (curFinish - curStart) / (double) interval)
				* curInst.getType().getCost() - curCost);

		super.InsertFilesToInstance(curNode, curInst);
		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curInst.setFirstTask(curNode.getId());
		}
		curInst.addExe(curNode.getId(), (int) curStart, curFinish, readStart, Cost);
		curInst.setFinishTime(curFinish);
		curInst.setLastTask(curNode.getId());
	}

	protected void setEndNodeEST() {
		int endTime = -1;
		WorkflowNode endNode = graph.getNodes().get(graph.getEndId());

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getEFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}
		int finishtime = -1;
		finishtime =computeFinalTime();
		endNode.setEST(endTime);
		endNode.setEFT(endTime);
		
	}

	private class result {
		float cost;
		int finishTime;
	}
}
