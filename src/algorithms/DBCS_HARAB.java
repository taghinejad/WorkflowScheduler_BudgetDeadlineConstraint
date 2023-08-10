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
import Broker.Instance.execution;
import Broker.WorkflowPolicy.result;
import utility.PriorityNodeList;

//Low-time complexity budget–deadline constrained workflow scheduling on heterogeneous resources; Hamid Arabnejad a, Jorge G. Barbosa a,∗, Radu Prodanb 2016
//Check Fine
public class DBCS_HARAB extends WorkflowPolicy {
	boolean backCheck = true;

	public DBCS_HARAB(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	List<levelNodes> lvlList = new ArrayList<levelNodes>();
	float alphaDeadline = 0;
	double DeadlineFactor = 0;
	int TotalDeadline;
	float TotalCost;
	float tetaCost = 0;
	float ConsumedBudget = (float) 0.0000001;

	public float schedule(int startTime, int deadline, float cost) {
		this.TotalDeadline = deadline;
		this.TotalCost = cost;
		setRuntimes();
		computeESTandEFT2(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);
		// calculateLevelLists(cost);
		computeSubDL(TotalDeadline);
		// computeECT_SubDeadline();
		// computeECT_SubBudget();

		if (!planning())
			return -1;

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	protected long computeSubDeadlineOf(WorkflowNode curNode) {
		int levelId = getLevelNode(curNode.getLevelBottem()).levelid;
		levelId = lvlList.size() - levelId;
		long levelWeight = 0;
		long teta = TotalDeadline;
		if (levelId + 1 < lvlList.size()) {
			for (int i = levelId; i < lvlList.size() - 1; i++) {
				levelWeight = 0;
				for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
					if (wn.getRunTime() > levelWeight)
						levelWeight = wn.getRunTime();
				}
				teta -= levelWeight;

			}
		}
		return teta;
	}

	protected float computeSubDeadlineCp(WorkflowNode GoalNode) {
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode;
		float subDl = 0, curTime = 0, minTime = 0;
		minTime = Integer.MAX_VALUE;

		for (Link child : GoalNode.getChildren()) {
			curNode = nodes.get(child.getId());
			curTime = (curNode.getDeadline() - curNode.getRunTime()
					- Math.round(((float) curNode.getInputFileSize() / bandwidth)));
			if (curTime < minTime) {
				minTime = curTime;

			}
		}
		return minTime;
	}
	// protected long computeSubDeadlineCp(WorkflowNode GoalNode) {
	// Map<String, WorkflowNode> nodes = graph.getNodes();
	// WorkflowNode curNode;
	// List<String> candidates = new ArrayList<String>();
	//
	// int subDl = 0,curTime=0,minTime=0;
	//
	// WorkflowNode minNode = GoalNode;
	// candidates.add(GoalNode.getId());
	//
	// while (!candidates.isEmpty()) {
	// candidates.clear();
	// for (Link child : minNode.getChildren()) {
	// candidates.add(child.getId());
	// }
	// minTime = Integer.MAX_VALUE;
	//
	//
	// for (int i = 0; i < candidates.size(); i++) {
	// curNode = nodes.get(candidates.get(i));
	// curTime = (curNode.getDeadline() - curNode.getRunTime()
	// - Math.round(((float) curNode.getInputFileSize() / bandwidth)));
	// if (curTime < minTime) {
	// minTime = curTime;
	// minNode = curNode;
	// }
	// }
	// subDl += minTime;
	//
	// }
	// return subDl;
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

	// protected long computeSubDeadlineOf2(WorkflowNode curNode) {
	// int levelId = getLevelNode(curNode.getLevelBottem()).levelid;
	// levelId=lvlList.size()-levelId;
	// long levelWeight = 0;
	// long teta = TotalDeadline;
	// if (levelId + 1 < lvlList.size()) {
	// for (int i = levelId ; i < lvlList.size()-1; i++) {
	// levelWeight = 0;
	// for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
	// for (Link child : wn.getChildren()) {
	// WorkflowNode childNode = graph.getNodes().get(parent.getId());
	// if (childNode.getRunTime() > levelWeight)
	// levelWeight = wn.getRunTime();
	//
	// }
	// }
	// teta -= levelWeight;
	// }
	// }
	// return teta;
	//
	// }

	// protected void computeECT_SubBudget() {
	// // interval is considerd in distribution
	// Map<String, WorkflowNode> nodes = graph.getNodes();
	// int interval = resources.getInterval();
	// float alphaBudget = TotalCost;
	// // float alphaBudget= TotalCost*interval;
	// float alpha = 0;
	// long sum = 0;
	// long[] Weight = new long[lvlList.size()];
	// long lvlWeight = 0;
	// long balfa = interval;
	// for (int i = 0; i < lvlList.size(); i++) {
	// for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
	// sum += wn.getRunTime();
	// }
	// }
	// Weight[0] = sum;
	// for (int i = 0; i < lvlList.size() - 1; i++) {
	// lvlWeight = 0;
	// for (WorkflowNode wn : lvlList.get(i).getLvlNodes()) {
	// sum += wn.getRunTime();
	// lvlWeight += wn.getRunTime();
	// }
	// Weight[i + 1] = Weight[i] - lvlWeight;
	// }
	// // if (Weight[0] * 2 < balfa) {
	// // balfa = Weight[0];
	// // }
	//
	// alpha = 0;
	// float[] DR = new float[lvlList.size()];
	// for (int i = 0; i < lvlList.size() - 1; i++) {
	// DR[i] = (float) Weight[i] / balfa;
	// alpha += DR[i];
	// }
	// for (int i = 0; i < lvlList.size() - 1; i++) {
	// DR[i] = (float) (DR[i] * TotalCost) / alpha;
	// }
	//
	// for (int i = 0; i < lvlList.size(); i++) {
	// lvlList.get(i).setSubBudget(DR[i]);
	// }
	//
	// }
	//
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

	private levelNodes getLevelNode(int levelID) {
		for (int i = 0; i < lvlList.size(); i++) {
			{
				if (lvlList.get(i).getLevelid() == levelID)
					return lvlList.get(i);
			}
		}
		return null;
	}

	private float getConsumedCost() {
		return super.computeFinalCost();
	}

	private float cheapestUnscheduledRun() {
		// Spare Workflow Deadline
		float costs = 0;
		long curFinish, curStart;
		Instance LowCostinst = new Instance(instances.getSize(), resources.getResource(resources.getMinId()));
		for (WorkflowNode node : graph.nodes.values()) {
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				if (!node.isScheduled()) {
					Instance curInst = new Instance(instances.getSize(), resources.getMinResource());
					result rCheap = checkInstance(node, curInst);
					costs += rCheap.cost;
				}
		}
		return costs;

	}

	private float CalculateQuality(WorkflowNode curNode) {
		result r, rFast, rCheap;
		int bestInst = -1;
		// float bestCost = Float.MAX_VALUE;
		int bestFinish = Integer.MAX_VALUE;
		float Time = 0;
		float subDl;
		double Cost = 0, subBudget, CTTF, BestCTTF = Float.MAX_VALUE * -1;
		Instance Fastinst = new Instance(instances.getSize(), resources.getResource(resources.getMaxId()));
		Instance LowCostinst = new Instance(instances.getSize(), resources.getResource(resources.getMinId()));
		float BestCost = 0;
		float BestTime = 0;
		float CL = 0;
		// 1- first subDL is recursively calculated;
		// subDl = computeSubDeadlineOf(curNode);

		subDl = computeSubDeadlineCp(curNode);

		rFast = checkInstance(curNode, getFastestInstance(curNode));
		// Instance cheapInst = new Instance(instances.getSize(),
		// resources.getMinResource());
		// rCheap = checkInstance(curNode, cheapInst);
		rCheap = checkInstance(curNode, getCheapInstanceNoDeadline(curNode));
		CL = rCheap.cost + this.tetaCost;
		// consider total cheap
		int delta = 0;
		float cheapestUnscheduled = cheapestUnscheduledRun();
		float budgetFraction = (float) cheapestUnscheduled / (TotalCost - ConsumedBudget);
		// 2- Quality is calculated useing CTTF
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.cost < CL) {
				// check divider error not to be zero
				delta = 0;
				if (r.finishTime < subDl)
					delta = 1;

				if ((rCheap.finishTime - rFast.finishTime) != 0)
					Time = (float) ((float) (subDl * delta - r.finishTime) / (rCheap.finishTime - rFast.finishTime));

				if ((rFast.cost - rCheap.cost) != 0)
					Cost = (float) (rFast.cost - r.cost) / (float) (rFast.cost - rCheap.cost) * delta;
				CTTF = Time + (Cost * budgetFraction);
				if (CTTF > BestCTTF) {
					BestCTTF = CTTF;
					bestInst = curInst;
					BestTime = r.finishTime;
					BestCost = r.cost;
				}
			}
		}

		for (int curRes = 0; curRes < resources.getSize() - 1; curRes++) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost < CL) {
				delta = 0;
				if (r.finishTime < subDl)
					delta = 1;

				if ((rCheap.finishTime - rFast.finishTime) != 0)
					Time = (float) ((float) (subDl * delta - r.finishTime) / (rCheap.finishTime - rFast.finishTime));

				if ((rFast.cost - rCheap.cost) != 0)
					Cost = (float) (rFast.cost - r.cost) / (float) (rFast.cost - rCheap.cost) * delta;

				CTTF = Time + (Cost * budgetFraction);

				if (CTTF > BestCTTF) {

					BestCTTF = CTTF;
					bestInst = 10000 + curRes;

					BestTime = r.finishTime;
					BestCost = r.cost;
				}
			}
		}
		// 3- After Chooseing the highest quality("CTTF") we set the instance;
		if (bestInst == -1) {
			// System.out.print("----------------Can't Continue with this DeadLine ,BDAS
			// algorithm");
			// System.out.print("--- Get Error on Node: " + curNode.getId());
			// return Float.MAX_VALUE;
			// return;
			// bestInst=0;
			// if it ignore CL
			for (int curInst = 0; curInst < instances.getSize(); curInst++) {
				r = checkInstance(curNode, instances.getInstance(curInst));

				// check divider error not to be zero
				delta = 0;
				if (r.finishTime < subDl)
					delta = 1;

				if ((rCheap.finishTime - rFast.finishTime) != 0)
					Time = (float) ((float) (subDl * delta - r.finishTime) / (rCheap.finishTime - rFast.finishTime));

				if ((rFast.cost - rCheap.cost) != 0)
					Cost = (float) (rFast.cost - r.cost) / (float) (rFast.cost - rCheap.cost) * delta;
				CTTF = Time + (Cost * budgetFraction);
				if (CTTF > BestCTTF) {
					BestCTTF = CTTF;
					bestInst = curInst;
					BestTime = r.finishTime;
					BestCost = r.cost;
				}

			}

			for (int curRes = 0; curRes < resources.getSize() - 1; curRes++) { // because the cheapest one is the last
				Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
				r = checkInstance(curNode, inst);

				delta = 0;
				if (r.finishTime < subDl)
					delta = 1;

				if ((rCheap.finishTime - rFast.finishTime) != 0)
					Time = (float) ((float) (subDl * delta - r.finishTime) / (rCheap.finishTime - rFast.finishTime));

				if ((rFast.cost - rCheap.cost) != 0)
					Cost = (float) (rFast.cost - r.cost) / (float) (rFast.cost - rCheap.cost) * delta;

				CTTF = Time + (Cost * budgetFraction);

				if (CTTF > BestCTTF) {

					BestCTTF = CTTF;
					bestInst = 10000 + curRes;

					BestTime = r.finishTime;
					BestCost = r.cost;
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

		// 4- updateing tetaCost
		this.tetaCost = tetaCost - (BestCost - rCheap.cost);
		return BestCost;

	}

	private float InitializeTetaCost() {

		float costs = 0;

		for (WorkflowNode node : graph.nodes.values()) {
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId()))
				if (!node.isScheduled()) {
					result rCheap = checkInstance(node, getCheapInstanceNoDeadline(node));
					costs += rCheap.cost;
				}
		}

		return (this.TotalCost - costs);
	}

	// private float CalculateSubDeadline(WorkflowNode wn) {
	// WorkflowNode lastNode = graph.getNodes().get(graph.getEndId());
	//
	// WorkflowNode node = lastNode;
	// int longRun = -1;
	// Boolean cond = true;
	// while (cond) {
	// for (int i = 0; i < node.getParents().size(); i++) {
	// Link parent = node.getParents().get(i);
	// WorkflowNode parentNode = graph.getNodes().get(parent.getId());
	// if (parentNode.getRunTime() > longRun)
	// longRun = parentNode.getRunTime();
	//
	// }
	//
	// }
	//
	//
	// return 0;
	// }
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

	private Boolean planning() {
		// this algorithm consider all of resources to be cost with one second.

		// sends tasks to queue based on upRank heft ranking;
		PriorityQueue<WorkflowNode> qu = new PriorityQueue<WorkflowNode>(graph.nodes.size(),
				new WorkflowPolicy.UpRankComparator());
		PriorityNodeList olist = new PriorityNodeList();
		result r;
		int bestFinish = Integer.MAX_VALUE;

		// first Step computing upRank
		computeUpRank();
		// addes nodes to graph
		for (WorkflowNode node : graph.nodes.values())
			if (!node.getId().equals(graph.getStartId()) && !node.getId().equals(graph.getEndId())) {
				qu.add(node);
				olist.Add(node);
			}

		// second Step is computing tetaCost ▲Cost;
		this.tetaCost = InitializeTetaCost();

		WorkflowNode cNode;
		// computeSubDL(TotalDeadline);
		while (!qu.isEmpty()) {
			cNode = qu.remove();
			// cNode = olist.pullbyUpWard();

			// Send To Run
			float cost = CalculateQuality(cNode);
			if (cost == Float.MAX_VALUE)
				return false;

			this.ConsumedBudget += cost;

		}
		return true;
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
