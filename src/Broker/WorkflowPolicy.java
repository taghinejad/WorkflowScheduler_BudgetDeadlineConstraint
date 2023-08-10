package Broker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import Broker.WorkflowPolicy.result;

public abstract class WorkflowPolicy {
	protected WorkflowGraph graph;
	public ResourceSet resources;
	public InstanceSet instances;
	protected long bandwidth;
	protected final long MB = 1000000;
	protected final double pricePerMB = 0;

	public WorkflowPolicy(WorkflowGraph g, ResourceSet rs, long bw) {
		graph = g;
		resources = rs;
		instances = new InstanceSet(resources);
		bandwidth = bw;
	}

	abstract public float schedule(int startTime, int deadline);

	abstract public float schedule(int startTime, int deadline, float cost);

	protected void setRuntimes() {
		float maxMIPS = resources.getMaxMIPS();
		for (WorkflowNode node : graph.getNodes().values())
			node.setRunTime((int) Math.ceil((float) node.getInstructionSize() / maxMIPS));
	}

	public int GetFileTransferTimeInInstance(List<File> Filesets, Instance in) {
		long transferSize = in.getNotExistedFileSizes(Filesets);
		int tTime = (int) Math.round((double) transferSize / bandwidth);
		return tTime;
	}

	public long GetMyFileTransferTime(long curStart, WorkflowNode curNode, Instance in) {
		try {
			if (curNode.getId().contains("start") || curNode.getId().contains("end"))
				return 0;

			int start = 0;
			int max = -1;
			List<File> nodeFiles = new ArrayList<File>();
			nodeFiles.addAll(curNode.getFileSet());
			List<File> ParentsFiles = new ArrayList<File>();
			for (Link parent : curNode.getParents()) {
				WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
				ParentsFiles.addAll(ParentFiles);
				// calculate file transfer Time from nodes parents if them does not exist in the
				// instance;
				int parentData = GetFileTransferTimeInInstance(ParentFiles, in);
				start = parentData + parentNode.getEFT();
				if (max < start)
					max = start;
			}

			// calculates SelfReadDataTransferTime;
			nodeFiles.removeAll(ParentsFiles);
			nodeFiles.removeAll(curNode.getOutputFileSet());
			long SelfInputFileSize = 0;
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
			// SelfData=0;
			// end

			if ((SelfData) > max)
				max = SelfData;

			if (max > curStart)
				curStart = max;
			return curStart;
		} catch (Exception e) {
			// System.out.println(e.getMessage());
			return 0;
		}
	}

	protected void MysetInstance(WorkflowNode curNode, Instance curInst) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();

		if (utility.Utility.readConsideration == true) {
			curStart = GetMyFileTransferTime(curStart, curNode, curInst);
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
		InsertFilesToInstance(curNode, curInst);
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

	// for BDHEFT
	protected float meanCost(WorkflowNode wn) {
		int interval = resources.getInterval();
		float NodeCost = 0;
		long nodeFinish = 0;

		for (int curInst = 0; curInst < instances.getSize(); curInst++) {

			nodeFinish = instances.getInstance(curInst).getFinishTime()
					+ Math.round((float) wn.getInstructionSize() / instances.getInstance(curInst).getType().getMIPS());
			NodeCost += checkInstanceFinish(nodeFinish, instances.getInstance(curInst)).cost;

		}

		// calculated mean Cost of nodes that is not scheduled by TotalCost variable;
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) {

			nodeFinish = Math.round((float) wn.getInstructionSize() / resources.getResource(curRes).getMIPS());
			NodeCost += Math.ceil((double) (nodeFinish - 0) / (double) interval)
					* resources.getResource(curRes).getCost();

		}
		NodeCost = NodeCost / (resources.getSize() + instances.getSize());
		return NodeCost;
	}

	protected float meanRunTime(WorkflowNode curNode) {
		float cost = 0;
		result r;
		float finishTime = 0;

		for (int curRes = 0; curRes < resources.getSize(); curRes++) { // because the cheapest one is // the last
			// Instance inst = new Instance(instances.getSize(),
			// resources.getResource(curRes));
			// r = checkInstanceRun(curNode, inst);
			int readTime= curNode.getReadTime(bandwidth);
			finishTime +=readTime+ Math.round((float) curNode.getInstructionSize() / resources.getResource(curRes).getMIPS());
		}
		finishTime = finishTime / resources.getSize();
		return finishTime;
	}

	protected result checkInstanceFinish(long curFinish, Instance curInst) {

		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();

		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		long start, curStart = (int) finishTime;

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
		if (curFinish < startTime)
			return r;
		// curFinish = curStart + Math.round((float) curNode.getInstructionSize() /
		// curInst.getType().getMIPS());
		r.finishTime = (int) curFinish;

		r.cost = (float) (Math.ceil((double) (curFinish - startTime) / (double) interval) * curInst.getType().getCost()
				- curCost);
		return r;
	}

	public WorkflowNode GetCriticalParent(WorkflowNode curNode, Instance in) {

		WorkflowNode CP = null;

		long curStart;
		int start = 0;
		int max = -1;
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			int parentData = GetFileTransferTimeInInstance(ParentFiles, in);
			start = parentData + parentNode.getEFT();
			if (max < start) {
				max = start;
				CP = parentNode;
			}
		}

		return CP;

	}

	public long GetMyStartTimeNoParentFile(long curStart, WorkflowNode curNode, Instance in) {
		int start = 0;
		int max = -1;
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			// int parentData = CalculateFileTransferTimeInInstance(ParentFiles, in);
			start = 0 + parentNode.getEFT();
			if (max < start)
				max = start;
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		long SelfInputFileSize = 0;
		// calculate file transfer Time from nodes parents if them does not exist in the
		// instance;
		int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
		// SelfData=0;
		// end

		if ((SelfData) > max)
			max = SelfData;

		if (max > curStart)
			curStart = max;
		return curStart;
	}

	public List<File> getSelfReadFileSet(WorkflowNode curNode) {
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		return nodeFiles;
	}

	public long GetMyStartTimeForPath(long curStart, int NodeIndexOnPath, Instance in, List<Broker.WorkflowNode> path,
			int[] newESTs) {
		int start = 0;
		int max = -1;
		Broker.WorkflowNode curNode = path.get(NodeIndexOnPath);
		List<File> nodeFiles = new ArrayList<File>();
		nodeFiles.addAll(curNode.getFileSet());
		List<File> ParentsFiles = new ArrayList<File>();
		for (Link parent : curNode.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			List<File> ParentFiles = filesFromParentNode(curNode, parentNode);
			ParentsFiles.addAll(ParentFiles);
			// calculate file transfer Time from nodes parents if them does not exist in the
			// instance;
			// int parentData = CalculateFileTransferTimeInInstance(ParentFiles, in);
			start = 0 + parentNode.getEFT();
			if (max < start)
				max = start;
		}

		// calculates SelfReadDataTransferTime;
		nodeFiles.removeAll(ParentsFiles);
		nodeFiles.removeAll(curNode.getOutputFileSet());
		long SelfInputFileSize = 0;
		// calculate file transfer Time from nodes parents if them does not exist in the
		// instance;
		int SelfData = GetFileTransferTimeInInstance(nodeFiles, in);
		// SelfData=0;
		// end

		if ((SelfData) > max)
			max = SelfData;

		if (max > curStart)
			curStart = max;
		return curStart;
	}

	// for BDAS AND DBDAS algorithm
	protected Instance getFastestInstance(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = Long.MAX_VALUE;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.finishTime < bestFinish) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.finishTime < bestFinish) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}

	// for BDAS AND DBDAS algorithm
	protected Instance getCheapInstance(WorkflowNode curNode, float deadline) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = 0;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.cost < bestCost && deadline >= r.finishTime) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost < bestCost && deadline >= r.finishTime) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
			}
		}
		if (bestInst == -1) {
			// if it could not find any resources to satisfy the deadline
			bestFinish = Long.MAX_VALUE;
			Instance inst = new Instance(instances.getSize(), resources.getResource(resources.getSize() - 1));
			r = checkInstance(curNode, inst);
			if (r.finishTime < bestFinish) {
				bestFinish = r.finishTime;
				bestInst = 10000 + resources.getSize() - 1;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}

	protected Instance getCheapInstanceNoDeadline(WorkflowNode curNode) {
		result r;
		int bestInst = -1;
		float bestCost = Float.MAX_VALUE;
		long bestFinish = 0;
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			r = checkInstance(curNode, instances.getInstance(curInst));
			if (r.cost < bestCost) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = curInst;
			}
		}
		for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
			Instance inst = new Instance(instances.getSize(), resources.getResource(curRes));
			r = checkInstance(curNode, inst);
			if (r.cost < bestCost) {
				bestCost = r.cost;
				bestFinish = r.finishTime;
				bestInst = 10000 + curRes;
			}
		}
		if (bestInst == -1) {
			// if it could not find any resources to satisfy the deadline
			bestFinish = Long.MAX_VALUE;
			Instance inst = new Instance(instances.getSize(), resources.getResource(resources.getSize() - 1));
			r = checkInstance(curNode, inst);
			if (r.finishTime < bestFinish) {
				bestFinish = r.finishTime;
				bestInst = 10000 + resources.getSize() - 1;
			}
		}
		if (bestInst < 10000)
			return instances.getInstance(bestInst);
		// setInstance(curNode, instances.getInstance(bestInst));
		else {
			bestInst -= 10000;
			Instance inst = new Instance(instances.getSize(), resources.getResource(bestInst));
			return inst;
			// instances.addInstance(inst);
			// setInstance(curNode, inst);
		}

	}
	protected void setInstanceFull(WorkflowNode curNode, Instance curInst) {
		long start, curStart = curInst.getFinishTime(), curFinish, readStart;
		// checks Latest start Time in Instances
		readStart = curStart;

		int interval = resources.getInterval();
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();

		if (utility.Utility.readConsideration == true) {
			curStart = GetMyFileTransferTime(curStart, curNode, curInst);
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
		InsertFilesToInstance(curNode, curInst);
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
	private result checkInstance(WorkflowNode curNode, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		int start, curStart = (int) finishTime, curFinish;
		curStart = (int) GetMyFileTransferTime(curStart, curNode, curInst);
		// for (Link parent : curNode.getParents()) {
		// WorkflowNode parentNode = graph.getNodes().get(parent.getId());
		//
		// start = parentNode.getEFT();
		// if (parentNode.getSelectedResource() != curInst.getId())
		// start += Math.round((float) parent.getDataSize() / bandwidth);
		// if (start > curStart)
		// curStart = start;
		// }

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

	public List<File> getFileNotExistedInParent(WorkflowNode curNode, WorkflowNode parent) {
		List<File> newFiles = new ArrayList<File>();

		List<File> f1 = curNode.getFileSet();
		List<File> f2 = parent.getFileSet();
		if (f1 == null)
			return null;
		if (f2 == null)
			return f1;

		for (File file : f1) {
			if (!f2.contains(file))
				newFiles.add(file);
		}
		return newFiles;
	}

	public List<File> filesFromParentNode(WorkflowNode curNode, WorkflowNode parent) {
		List<File> newFiles = new ArrayList<File>();

		List<File> f1 = curNode.getFileSet();
		List<File> f2 = parent.getFileSet();
		if (f1 == null || f2 == null)
			return newFiles;

		for (File f : f1) {
			for (File ff : f2) {
				if (f.fileName.contains(ff.fileName)) {
					newFiles.add(f);
					continue;
				}
			}
		}
		return newFiles;
	}

	public void InsertFilesToInstance(WorkflowNode wn, Instance in) {
		List<File> Filesets = wn.getFileSet();
		in.addFiles(Filesets);
		// long transferSize =in.getNotExistedFileSizes(Filesets);
		// int tTime= (int) Math.round((double)transferSize/bandwidth);
		// return tTime;
	}

	protected void computeLSTandLFT(int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime();
				thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime < minTime)
					minTime = thisTime;
			}
			curNode.setLFT(minTime);
			curNode.setLST(minTime - curNode.getRunTime());
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

	protected void computeMyLSTandLFT(int deadline) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getEndId());
		curNode.setLFT(deadline);
		curNode.setLST(deadline);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, minTime;
			curNode = nodes.get(candidateNodes.remove());
			minTime = Integer.MAX_VALUE;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				int startTime = (int) GetMyFileTransferTime(0, curNode, curInst);
				thisTime = childNode.getLFT() - childNode.getRunTime();
				// thisTime -= Math.round((float) child.getDataSize() / bandwidth);
				thisTime -= startTime;
				if (thisTime < minTime)
					minTime = thisTime;
			}
			curNode.setLFT(minTime);
			curNode.setLST(minTime - curNode.getRunTime());
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

	protected void computeESTandEFT(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			float datatransfertime = 0;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);

				thisTime = (int) (parentNode.getEFT() + datatransfertime);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeESTandEFT2(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			float datatransfertime = 0, selfTransfer = 0;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
				selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
				if (selfTransfer > datatransfertime)
					datatransfertime = selfTransfer;

				thisTime = (int) (parentNode.getEFT() + datatransfertime);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMyESTandEFT(int startTime) {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;
		Instance curInst = new Instance(0, resources.getMyMaxResource());
		curNode = nodes.get(graph.getStartId());
		curNode.setEST(startTime);
		curNode.setEFT(startTime);
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			float datatransfertime = 0;
			maxTime = (int) GetMyFileTransferTime(0, curNode, curInst);
			// for (Link parent : curNode.getParents()) {
			// parentNode = nodes.get(parent.getId());
			// datatransfertime = Math.round((float) parent.getDataSize() / bandwidth);
			// thisTime = (int) (parentNode.getEFT() + datatransfertime);
			// if (thisTime > maxTime)
			// maxTime = thisTime;
			// }

			curNode.setEST(maxTime);
			curNode.setEFT(maxTime + curNode.getRunTime());
			curNode.setScheduled();

			for (Link child : curNode.getChildren()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}
		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setUpRank(0);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;

			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				thisTime = childNode.getUpRank() + Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setUpRank(maxTime);
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

	protected void computeDownRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setDownRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime = parentNode.getDownRank() + Math.round((float) parent.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setDownRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeSigmaUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getStartId());
		curNode.setUpRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime = 0, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getParents()) {
				parentNode = nodes.get(parent.getId());
				thisTime += parentNode.getUpRank() + Math.round((float) parent.getDataSize() / bandwidth);

			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setUpRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeSigmaDownRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setDownRank((0));
		curNode.setScheduled();
		for (Link child : curNode.getChildren())
			candidateNodes.add(child.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime = 0, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link parent : curNode.getChildren()) {
				parentNode = nodes.get(parent.getId());
				thisTime += parentNode.getDownRank() + Math.round((float) parent.getDataSize() / bandwidth);

			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS);
			curNode.setDownRank(maxTime);
			curNode.setScheduled();

			for (Link child : curNode.getParents()) {
				boolean isCandidate = true;
				childNode = nodes.get(child.getId());
				for (Link parent : childNode.getParents())
					if (!nodes.get(parent.getId()).isScheduled())
						isCandidate = false;
				if (isCandidate)
					candidateNodes.add(child.getId());
			}
		}

		for (WorkflowNode node : nodes.values())
			node.setUnscheduled();
	}

	protected void computeMyUpRank() {
		Queue<String> candidateNodes = new LinkedList<String>();
		Map<String, WorkflowNode> nodes = graph.getNodes();
		WorkflowNode curNode, parentNode, childNode;

		curNode = nodes.get(graph.getEndId());
		curNode.setUpRank(0);
		curNode.setScheduled();
		for (Link parent : curNode.getParents())
			candidateNodes.add(parent.getId());

		while (!candidateNodes.isEmpty()) {
			int thisTime, maxTime;
			float maxMIPS;
			curNode = nodes.get(candidateNodes.remove());
			maxTime = -1;
			for (Link child : curNode.getChildren()) {
				childNode = nodes.get(child.getId());
				thisTime = childNode.getUpRank() + Math.round((float) child.getDataSize() / bandwidth);
				if (thisTime > maxTime)
					maxTime = thisTime;
			}
			maxMIPS = resources.getMeanMIPS();
			maxTime += Math.round((float) curNode.getInstructionSize() / maxMIPS)
					+ Math.round((float) curNode.getReadFileSize() / bandwidth);
			curNode.setUpRank(maxTime);
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

	protected long getDataSize(WorkflowNode parent, WorkflowNode child) {
		long size = 0;
		for (Link link : parent.getChildren())
			if (link.getId().equals(child.getId())) {
				size = link.getDataSize();
				break;
			}
		return (size);
	}

	protected void setEndNodeAST() {
		int endTime = -1;
		WorkflowNode endNode = graph.getNodes().get(graph.getEndId());

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getAFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}
		endNode.setAST(endTime);
		endNode.setAFT(endTime);
	}

	protected void initializeStartEndNodes(int startTime, int deadline) {
		Map<String, WorkflowNode> nodes = graph.getNodes();

		nodes.get(graph.getStartId()).setScheduled();
		nodes.get(graph.getEndId()).setScheduled();
	}

	public float computeFinalCost() {
		float totalCost = 0, curCost;

		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);

			if (inst.getFinishTime() == 0)
				break;
			// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
			// inst.getStartTime()) / (double)resources.getInterval())
			// * inst.getType().getCost() ;
			WorkflowNode first = graph.getNodes().get(inst.getFirstTask()),
					last = graph.getNodes().get(inst.getLastTask());
			curCost = (float) Math.ceil((double) (last.getEFT() - first.getEST()) / (double) resources.getInterval())
					* inst.getType().getCost();
			totalCost += curCost;
		}

		return (totalCost);
	}

	public int computeFinalTime() {
		float totalCost = 0, curCost;
		int ft = 0;
		for (int instId = 0; instId < instances.getSize(); instId++) {
			Instance inst = instances.getInstance(instId);

			if (inst.getFinishTime() == 0)
				break;
			// curCost = (float)Math.ceil((double)(inst.getFinishTime() -
			// inst.getStartTime()) / (double)resources.getInterval())
			// * inst.getType().getCost() ;
			WorkflowNode first = graph.getNodes().get(inst.getFirstTask()),
					last = graph.getNodes().get(inst.getLastTask());
			if (last.getEFT() > ft)
				ft = last.getEFT();

		}

		return (ft);
	}

	public static class UpRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getUpRank() < node2.getUpRank())
				return (1);
			else if (node1.getUpRank() > node2.getUpRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class DownRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getDownRank() < node2.getDownRank())
				return (1);
			else if (node1.getDownRank() > node2.getDownRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class DownLevelComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getLevelBottem() < node2.getLevelBottem())
				return (1);
			else if (node1.getLevelBottem() > node2.getLevelBottem())
				return (-1);
			else
				return (0);
		}
	}

	public static class subDeadlineAscending implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getDeadline() < node2.getDeadline())
				return (-1);
			else if (node1.getDeadline() > node2.getDeadline())
				return (1);
			else
				return (0);
		}
	}

	public static class EarliestStartTime implements Comparator<WorkflowNode> {
		
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getEST() >= node2.getEST())
				return (1);
			else if (node1.getEST() < node2.getEST())
				return (-1);
			else
				return (0);
		}
	}

	public static class SumRankComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getSumRank() < node2.getSumRank())
				return (1);
			else if (node1.getSumRank() > node2.getSumRank())
				return (-1);
			else
				return (0);
		}
	}

	public static class ASTComparator implements Comparator<WorkflowNode> {
		public int compare(WorkflowNode node1, WorkflowNode node2) {
			if (node1.getAST() < node2.getAST())
				return (-1);
			else if (node1.getAST() > node2.getAST())
				return (1);
			else
				return (0);
		}
	}

	public class result {
		public float cost;
		public int finishTime;
	}

}
