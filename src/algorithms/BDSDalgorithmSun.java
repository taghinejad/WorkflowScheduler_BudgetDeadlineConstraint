package algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import Broker.File;
import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
//T. Sun, C. Xiao, and X. Xu, “A scheduling algorithm using sub-deadline for workflow applications under budget and deadline constrained,” Cluster Comput., 2018.
import Broker.WorkflowPolicy.result;

public class BDSDalgorithmSun extends WorkflowPolicy {
	boolean backCheck = true;
	final int priority = 1;
	// priority for chooseing 1-fastest 2-cheapest
	// my algorithm: takes into account start time of resources;

	public BDSDalgorithmSun(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	float alphaDeadline = 0;
	double DeadlineFactor = 0;
	int TotalDeadline;
	float TotalCost;
	float remaingCost = 0;

	public float schedule(int startTime, int deadline, float cost) {
		this.TotalDeadline = deadline;
		this.TotalCost = cost;
		this.remaingCost = cost;
		setRuntimes();
		computeESTandEFT2(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);
		computeSubDL(TotalDeadline);
		planning();

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	public float schedule(int startTime, int deadline) {
		return 0;
	}

	// protected void computeSubDL(int totalDeadline) {
	// Queue<String> candidateNodes = new LinkedList<String>();
	// Map<String, WorkflowNode> nodes = graph.getNodes();
	// WorkflowNode curNode, parentNode, childNode;
	//
	// curNode = nodes.get(graph.getEndId());
	// curNode.setDeadline(totalDeadline);
	// curNode.setScheduled();
	// for (Link parent : curNode.getParents())
	// candidateNodes.add(parent.getId());
	//
	// while (!candidateNodes.isEmpty()) {
	// float thisTime, minTime;
	// float maxMIPS;
	//
	// curNode = nodes.get(candidateNodes.remove());
	// minTime = Integer.MAX_VALUE;
	// maxMIPS = resources.getMaxMIPS();
	// for (Link child : curNode.getChildren()) {
	// childNode = nodes.get(child.getId());
	// thisTime = childNode.getDeadline() -((float) childNode.getInstructionSize() /
	// maxMIPS)
	// + Math.round((float) child.getDataSize() / bandwidth);
	// if (thisTime < minTime)
	// minTime = thisTime;
	// }
	//
	// curNode.setDeadline(minTime);
	// curNode.setScheduled();
	//
	// for (Link parent : curNode.getParents()) {
	// boolean isCandidate = true;
	// parentNode = nodes.get(parent.getId());
	// for (Link child : parentNode.getChildren())
	// if (!nodes.get(child.getId()).isScheduled())
	// isCandidate = false;
	// if (isCandidate)
	// candidateNodes.add(parent.getId());
	// }
	// }
	//
	// for (WorkflowNode node : nodes.values())
	// node.setUnscheduled();
	// }
	protected void computeSubDL(int totalDeadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setDeadline(totalDeadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			float thisTime, minTime;
			float maxMIPS;

			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			maxMIPS = resources.getMaxMIPS();
			float transferTime = 0;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				List<File> ParentFiles = filesFromParentNode(childNode, curNode);
				transferTime = 0;
				for (File fl : ParentFiles) {
					transferTime += (float) fl.getFileSize() / bandwidth;
				}
				thisTime = childNode.getDeadline() - ((float) childNode.getInstructionSize() / maxMIPS);
				thisTime -= transferTime;
				if (thisTime < minTime)
					minTime = thisTime;
			}
			if (minTime > 0)
				curNode.setDeadline(minTime);
			else
				curNode.setDeadline(0);
			curNode.setScheduled();

			for (Link parent : curNode.getParents()) {
				boolean isCandidate = true;
				parentNode = nodes.get(parent.getId());
				for (Link child : parentNode.getChildren())
					if (!nodes.get(child.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(parent.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	private int unScheduledTasksNumber() {
		int count = 0;
		for (WorkflowNode node : graph.getNodes().values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId())
					&& !node.isScheduled())
				count++;
		return count;

	}

	private void planning() {

		remaingCost = this.TotalCost;
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.subDeadlineAscending());
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();
			// erc is expended reasonableCost
			int unScheduledTasks = unScheduledTasksNumber();
			double erc = (float) remaingCost / unScheduledTasks;

			result r, rFast, rCheap;
			int bestInst = -1;
			float ResourceCost = Float.MAX_VALUE;
			int bestFinish = Integer.MAX_VALUE;
			float Time;
			long subDl;
			double Cost, subBudget, CTTF, BestCTTF = Double.MIN_VALUE * -1;
			float bestcost = Float.MAX_VALUE;

			for (int curInst = 0; curInst < instances.getSize(); curInst++) {
				r = checkInstance(curNode, instances.getInstance(curInst));
				// 1 for best finish time 2 for chosseing cheapset resource
				if (priority == 1) {
					if (r.cost < erc && r.finishTime < bestFinish) {
						bestInst = curInst;
						bestFinish = r.finishTime;
						ResourceCost = r.cost;
					}
				} else if (priority==0) {
					if (r.cost < erc && r.cost < bestcost) {
						bestInst = curInst;
						bestcost = r.cost;
						ResourceCost = r.cost;
					}
				}
			}
			for (int curRes = 0; curRes < resources.getSize() - 1; curRes++) { // because the cheapest one is the last
				Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
				r = checkInstance(curNode, inst);
				if (priority == 1) {
					if (r.cost < erc && r.finishTime < bestFinish) {
						bestInst = 10000 + curRes;
						bestFinish = r.finishTime;
						ResourceCost = r.cost;
					}
				} else if (priority==0) {
					if (r.cost < erc && r.cost < bestcost) {
						bestInst = 10000 + curRes;
						bestcost = r.cost;
						ResourceCost = r.cost;
					}
				}
			}

			if (bestInst == -1) {
				bestFinish = Integer.MAX_VALUE;
				bestcost=Float.MAX_VALUE;
				for (int curInst = 0; curInst < instances.getSize(); curInst++) {
					r = checkInstance(curNode, instances.getInstance(curInst));
					if (priority == 1) {
						if (r.finishTime < bestFinish) {
							bestInst = curInst;
							bestFinish = r.finishTime;
							ResourceCost = r.cost;
						}
					} else if (priority==0) {
						if (r.cost < erc && r.cost < bestcost) {
							bestInst = curInst;
							bestcost = r.cost;
							ResourceCost = r.cost;
						}
					}
				}

				for (int curRes = 0; curRes < resources.getSize() - 1; curRes++) {
					Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
					r = checkInstance(curNode, inst);
					if (priority == 1) {
						if (r.finishTime < bestFinish) {
							bestInst = 10000 + curRes;
							bestFinish = r.finishTime;
							ResourceCost = r.cost;
						}
					} else if (priority==0) {
						if (r.cost < erc && r.cost < bestcost) {
							bestInst = 10000 + curRes;
							bestcost = r.cost;
							ResourceCost = r.cost;
						}
					}

				}

			}

			if (bestInst < 10000)
				setInstance2(curNode, instances.getInstance(bestInst));
			else {
				bestInst -= 10000;
				Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
				instances.addInstance(inst);
				setInstance2(curNode, inst);
			}
			remaingCost -= ResourceCost;
		}

	}

	private result checkInstance(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;

		if (utility.Utility.readConsideration == true) {
			curStart = (int) super.GetMyFileTransferTime(curStart, curNode, curInst);
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
		r.finishTime = curFinish;
		// if ((finishTime != 0 && curStart > curIntervalFinish) || curFinish >
		// curNode.getLFT()) // difference with PCPD2
		// r.cost = Float.MAX_VALUE;
		// else
		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);

		return (r);
	}

	private void setInstance2(WorkflowNode curNode, Instance curInst) {
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

		//
		if (utility.Utility.readConsideration == true) 
			super.InsertFilesToInstance(curNode, curInst);
		curNode.setAST((int) curStart);
		curNode.setAFT((int) curFinish);
		curNode.setEST((int) curStart);
		curNode.setEFT((int) curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setStartReading((int) readStart);
		curNode.setScheduled();

		if (curInst.getFinishTime() == 0) {
			// curInst.setStartTime(curStart);
			// sets start of the instances to the time that it reads files from storage or
			// parent of a node;
			curInst.setStartTime(readStart);
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
		finishtime = computeFinalTime();
		endNode.setEST(endTime);
		endNode.setEFT(endTime);
	}

}
