package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import Broker.WorkflowPolicy.result;
import utility.PriorityNodeList;

public class BDCtaghinezhad extends WorkflowPolicy {
	boolean backCheck = true;
	// my algorithm: takes into account start time of resources;
	float factorTime=(float)0.5;
	float factorBudget=(float)0.5;
	public BDCtaghinezhad(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	List<levelNodes> lvlList = new ArrayList<levelNodes>();
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
		calculateLevelLists(cost);
		computeECT_SubDeadline();
		// computeECT_SubBudget();
		Boolean r = planning();
		if (!r)
			return -1;
		setEndNodeEST();
		cost = super.computeFinalCost();

		return (cost);
	}

	protected void computeECT_SubDeadline() {
		Map<String, WorkflowNode> nodes = graph.getNodes();

		float alpha = 0;
		long sum = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				sum += wn.getRunTimeWithData(bandwidth);
			}
		}
		alpha = (float) TotalDeadline / sum;
		float[] DR = new float[lvlList.size()];
		long levelWeight = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			levelWeight = 0;
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				levelWeight += wn.getRunTimeWithData(bandwidth);
			}
			DR[i] = levelWeight * alpha;
			if (i > 0)
				DR[i] += DR[i - 1];
		}
		for (int i = 0; i < lvlList.size(); i++) {
			lvlList.get(i).setSubDeadline(Math.round(DR[i]));
		}

	}

	protected void computeECT_SubBudget() {

		// complexity Analysis: N+N:Nodes O(N)
		Map<String, WorkflowNode> nodes = graph.getNodes();
		int interval = resources.getInterval();
		float alphaBudget = TotalCost;
		// float alphaBudget= TotalCost*interval;
		float alpha = 0;
		long sum = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				sum += wn.getRunTimeWithData(bandwidth);

			}
		}
		alpha = (float) alphaBudget / sum;
		float[] DR = new float[lvlList.size()];
		long levelWeight = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			levelWeight = 0;
			for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
				levelWeight += wn.getRunTimeWithData(bandwidth);
			}
			DR[i] = (levelWeight * alpha);
			// DR[i] = (levelWeight * alpha)/interval;
			// if (i > 0)
			// DR[i] += DR[i - 1];
		}
		for (int i = 0; i < lvlList.size(); i++) {
			lvlList.get(i).setSubBudget(DR[i]);
		}

	}

	public float schedule(int startTime, int deadline) {
		return 0;
	}

	// inserts nodes to level;
	public void insertLevel(int level, WorkflowNode wn) {

		for (int i = 0; i < lvlList.size(); i++) {
			if (level == lvlList.get(i).levelid) {
				lvlList.get(i).lvlNodes.add(wn);
				return;
			}
		}
		levelNodes ln = new levelNodes(level, wn);
		lvlList.add(ln);
	}

	private void calculateSubDeadlines() {

		for (int i = 0; i < lvlList.size(); i++) {
			{
				int max = -1;
				for (WorkflowNode wn : lvlList.get(i).lvlNodes) {
					if (wn.getEFT() > max)
						max = wn.getEFT();
				}
				lvlList.get(i).setSubDeadline(max);

			}
		}
	}

	private levelNodes getLevelNode(int levelID) {
		for (int i = 0; i < lvlList.size(); i++) {
			{
				if (lvlList.get(i).getLevelid() == levelID)
					return lvlList.get(i);
			}
		}
		return null;
	}

	private void calculateLevelLists(float cost) {
		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.DownLevelComparator());
		result r;
		int bestFinish = Integer.MAX_VALUE;

		computeDownRank();
		// addes nodes to graph
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				queue.add(node);

		while (!queue.isEmpty()) {
			WorkflowNode curNode = queue.remove();
			insertLevel(curNode.getLevelBottem(), curNode);
		}
		lvlList.get(0).setSubBudget(cost);

	}

	private float getConsumedCost() {
		return super.computeFinalCost();
	}

	private float CalculateCTTF(WorkflowNode curNode) {

		
		result r, rFast, rCheap;
		int bestInst = -1;
		// float bestCost = Float.MAX_VALUE;
		int bestFinish = Integer.MAX_VALUE;
		float Time;
		long subDl;
		double Cost, subBudget, CTTF, BestCTTF = -1;
		Instance Fastinst = new Instance(instances.getSize(), resources.getResource(resources.getMaxId()));
		Instance LowCostinst = new Instance(instances.getSize(), resources.getResource(resources.getMinId()));
		float BestCost = 0;
		float BestTime = 0;


		subDl = getLevelNode(curNode.getLevelBottem()).getSubDeadline();
		subBudget = remaingCost;
		rFast = checkInstance(curNode, getFastestInstance(curNode));
		rCheap = checkInstance(curNode, getCheapInstanceNoDeadline(curNode));
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			// check divider error not to be zero

			Time = (float) (subDl - (r.finishTime - rFast.finishTime)) / subDl;
			Cost = (float) (subBudget - (r.cost - rCheap.cost)) / (subBudget);

			CTTF = (factorTime* Time) + (factorBudget*Cost);

			if (CTTF > BestCTTF ) {
				BestCTTF = CTTF;
				bestInst = curInst;
				BestTime = r.finishTime;
				BestCost = r.cost;
			}
			

		}

		for (int curRes = 0; curRes < resources.getSize() - 1; curRes++) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			Time = (float) (subDl - (r.finishTime - rFast.finishTime)) / subDl;
			Cost = (float) (subBudget - (r.cost - rCheap.cost)) / (subBudget);

			// Time = (float) (subDl - r.finishTime) / (subDl - rFast.finishTime);
			// Cost = (float) (subBudget - r.cost) / (subBudget - rCheap.cost);
			CTTF = (factorTime* Time) + (factorBudget*Cost);
			if (CTTF > BestCTTF ) {

				BestCTTF = CTTF;
				bestInst = 10000 + curRes;

				BestTime = r.finishTime;
				BestCost = r.cost;
			}
		}

		if (bestInst == -1) {
			System.out.print("----------------Can't Continue with this DeadLine ,BDAS algorithm");
			System.out.print("---  Get Error on Node: " + curNode.getId());
			return Float.MAX_VALUE;
			// return;
			// bestInst=0;
		}

		if (bestInst < 10000)
			setInstance2(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			instances.addInstance(inst);
			setInstance2(curNode, inst);
		}
		return BestCost;

	}

	private Boolean planning() {
		int bestFinish = Integer.MAX_VALUE;

		PriorityQueue<WorkflowNode> qu = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.EarliestStartTime());
		PriorityNodeList olist = new PriorityNodeList();
		float cost = 0;
		for (int i = 0; i < lvlList.size(); i++) {
			int len = lvlList.get(i).getLvlNodes().size();
			float TotalCost = 0;
			for (int j = 0; j < len; j++) {
				if (lvlList.get(i).getLvlNodes().get(j).isScheduled() == false) {
					qu.add(lvlList.get(i).getLvlNodes().get(j));
					olist.Add(lvlList.get(i).getLvlNodes().get(j));
				}
			}
		}
			WorkflowNode cNode, cNode2;
			// for (int j = 0; j < len; j++) {
			while (!olist.isEmpty()) {
				cNode2 = qu.remove();
				cNode = olist.pullbyEST();
				// Send To Run
				cost = CalculateCTTF(cNode);
				remaingCost -= cost;
				// checks for capability of nodes children to see that if they can be processed.
				for (int k = 0; k < cNode.getChildren().size(); k++) {
					WorkflowNode child = graph.nodes.get(cNode.getChildren().get(k).id);
					if (!qu.contains(child) && !child.getId().equals(graph.getStartId())
							&& !child.getId().equals(graph.getEndId())) {
						Boolean cap = true;
						for (int q = 0; q < child.getParents().size(); q++) {
							WorkflowNode parentNode = graph.nodes.get(child.getParents().get(q).id);
							if (parentNode.isScheduled() == false)
								cap = false;
						}
						if (cap == true) {
							qu.add(child);
							olist.AddNoDuplicate(child);
						}
					}
				}
				// end

				// TotalCost += cost;
				if (cost == Float.MAX_VALUE)
					return false;
				getLevelNode(cNode.getLevelBottem())
						.setSubBudget(getLevelNode(cNode.getLevelBottem()).getSubBudget() - cost);
				// lvlList.get(i).setSubBudget(lvlList.get(i).getSubBudget() - cost);
			}
		
		return true;

	}
	// backup old
	// private Boolean planning() {
	// int bestFinish = Integer.MAX_VALUE;
	//
	// PriorityQueue<WorkflowNode> qu = new
	// PriorityQueue<WorkflowNode>(graph.nodes.size(),
	// new WorkflowPolicy.EarliestStartTime());
	// float cost = 0;
	// for (int i = 0; i < lvlList.size(); i++) {
	// int len = lvlList.get(i).getLvlNodes().size();
	// float TotalCost = 0;
	// for (int j = 0; j < len; j++) {
	// if (lvlList.get(i).getLvlNodes().get(j).isScheduled() == false)
	// qu.add(lvlList.get(i).getLvlNodes().get(j));
	// }
	//
	// WorkflowNode cNode;
	// for (int j = 0; j < len; j++) {
	// cNode = qu.remove();
	// // Send To Run
	// cost = CalculateCTTF(cNode);
	// remaingCost -= cost;
	// // checks for capability of nodes children to see that if they can be
	// processed.
	// for (int k = 0; k < cNode.getChildren().size(); k++) {
	// WorkflowNode child = graph.nodes.get(cNode.getChildren().get(k).id);
	// if ( !qu.contains(child)&& !child.getId().equals(graph.getStartId()) &&
	// !child.getId().equals(graph.getEndId())) {
	// Boolean cap = true;
	// for (int q = 0; q < child.getParents().size(); q++) {
	// WorkflowNode parentNode = graph.nodes.get(child.getParents().get(q).id);
	// if (parentNode.isScheduled() == false)
	// cap = false;
	// }
	// if (cap == true)
	// qu.add(child);
	// }
	// }
	// // end
	//
	// // TotalCost += cost;
	// if (cost == Float.MAX_VALUE)
	// return false;
	// getLevelNode(cNode.getLevelBottem())
	// .setSubBudget(getLevelNode(cNode.getLevelBottem()).getSubBudget() - cost);
	// // lvlList.get(i).setSubBudget(lvlList.get(i).getSubBudget() - cost);
	// }
	// if (i < lvlList.size() - 1)
	// lvlList.get(i + 1).setSubBudget(lvlList.get(i + 1).getSubBudget() +
	// lvlList.get(i).getSubBudget());
	// }
	// return true;
	//
	// }
	//

private result checkInstance(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;
		
		if (utility.Utility.readConsideration == true) {
			curStart = (int)super.GetMyFileTransferTime(curStart, curNode, curInst);
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

	private void setInstance(WorkflowNode curNode, Instance curInst) {
		int start, curStart = (int) curInst.getFinishTime(), curFinish;
		// checks Latest start Time in Instances
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());

			start = parentNode.getEFT();
			if (parentNode.getSelectedResource() != curInst.getId())
				start += Math.round((float) parent.getDataSize() / bandwidth);
			if (start > curStart)
				curStart = start;
		}

		curFinish = curStart + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
		curNode.setEST(curStart);
		curNode.setEFT(curFinish);
		curNode.setSelectedResource(curInst.getId());
		curNode.setScheduled();

		if (curInst.getFinishTime() == 0) {
			curInst.setStartTime(curStart);
			curInst.setFirstTask(curNode.getId());
		}
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
if (finishtime>endTime)
	endTime=finishtime;
		endNode.setEST(endTime);
		endNode.setEFT(endTime);
	}

	private class levelNodes {
		int levelid;
		List<WorkflowNode> lvlNodes = new ArrayList<>();
		private float subBudget;
		private long subDeadline;

		public levelNodes(int levelid, WorkflowNode lvlNodes) {
			super();
			this.levelid = levelid;
			this.lvlNodes.add(lvlNodes);
			this.subBudget = 0;
			this.subDeadline = 0;
		}

		public int getNodeLevelId(String nodeID) {
			for (int i = 0; i < lvlList.size(); i++) {
				for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
					if (wn.getId().contains(nodeID))
						return i;
				}
			}
			return -1;
		}

		public int getLevelid() {
			return levelid;
		}

		public void setLevelid(int levelid) {
			this.levelid = levelid;
		}

		public List<WorkflowNode> getLvlNodes() {
			return lvlNodes;
		}

		public void setLvlNodes(List<WorkflowNode> lvlNodes) {
			this.lvlNodes = lvlNodes;
		}

		public float getSubBudget() {
			return subBudget;
		}

		public void setSubBudget(float subBudget) {
			this.subBudget = subBudget;
		}

		public long getSubDeadline() {
			return subDeadline;
		}

		public void setSubDeadline(long subDeadline) {
			this.subDeadline = subDeadline;
		}

	}

}
