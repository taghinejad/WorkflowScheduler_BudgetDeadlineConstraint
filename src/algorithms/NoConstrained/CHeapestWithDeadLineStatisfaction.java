package algorithms.NoConstrained;

import java.util.PriorityQueue;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.WorkflowPolicy.UpRankComparator;
import algorithms.NoConstrained.HEFTAlgorithm.result;

public class CHeapestWithDeadLineStatisfaction extends WorkflowPolicy {

	public CHeapestWithDeadLineStatisfaction(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float schedule(int startTime, int deadline) {
		float cost;

		setRuntimes();
		computeESTandEFT(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);

		planning();

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	private void planning() {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.UpRankComparator());
		result r;
		long bestFinish = Integer.MAX_VALUE;
		long finishTime = -1;
		computeUpRank();
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();
			int bestInst = -1;
			float bestCost = Float.MAX_VALUE;

			bestFinish = Integer.MAX_VALUE;
			for (int curInst = 0; curInst < instances.getSize(); curInst++) {
				// r = checkInstance(curNode, instances.getInstance(curInst));

				finishTime = checkInstance(curNode, instances.getInstance(curInst));
				if (finishTime < bestFinish) {
					bestFinish = finishTime;
					bestInst = curInst;
				}

			}
			// int maxResourceId = -1;
			// int maxMips = 0;
			// calculates maximum resources
			// for (int curRes = 0; curRes < resources.getSize(); curRes++) {
			// Instance inst = new Instance(instances.getSize(),
			// resources.getResource(curRes));
			// if (inst.getType().getMIPS() > maxMips) {
			// maxMips = inst.getType().getMIPS();
			// maxResourceId = curRes;
			// }
			// }

			// because the cheapest one is the last
			
			Instance Initinst = new Instance(instances.getSize(), resources.getMyMaxResource());
			finishTime = checkInstance(curNode, Initinst);
			if (finishTime < bestFinish) {
				bestFinish = finishTime;
				bestInst = 10000 + resources.getMyMaxResourceIndex();
			}

			if (bestInst < 10000)
				setInstance(curNode, instances.getInstance(bestInst));
			else {
				bestInst -= 10000;
				Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
				instances.addInstance(inst);
				setInstance(curNode, inst);
			}
		}
	}

	private long checkInstance(WorkflowNode curNode, Instance curInst) {
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
		// r.finishTime = curFinish;
		// if ((finishTime!=0 && curStart > curIntervalFinish) || curFinish >
		// curNode.getLFT()) //difference with PCPD2
		// r.cost = Float.MAX_VALUE;
		// else
		// r.cost = (float)(Math.ceil((double)(curFinish - startTime)/(double)interval)
		// * curInst.getType().getCost() - curCost);

		// return (r);
		return curFinish;
	}

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
		endNode.setEST(endTime);
		endNode.setEFT(endTime);
	}

	public class result {
		float cost;
		int finishTime;
	}
}
