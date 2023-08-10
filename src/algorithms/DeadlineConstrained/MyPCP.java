package algorithms.DeadlineConstrained;

import java.util.ArrayList;
import java.util.List;

import Broker.Instance;
import Broker.Link;
import Broker.ResourceSet;
import Broker.WorkflowGraph;
import Broker.WorkflowNode;
import Broker.WorkflowPolicy;
import utility.SimulationResults;

public class MyPCP extends WorkflowPolicy {
	boolean backCheck = true;

	public MyPCP(WorkflowGraph g, ResourceSet rs, long bw) {
		super(g, rs, bw);

	}

	public float schedule(int startTime, int deadline) {
		float cost;

		setRuntimes();
		computeESTandEFT2(startTime);
		computeLSTandLFT(deadline);
		initializeStartEndNodes(startTime, deadline);

		assignParents(graph.getNodes().get(graph.getEndId()));

		setEndNodeEST();
		cost = super.computeFinalCost();
		return (cost);
	}

	private void assignParents(WorkflowNode curNode) {
		List<WorkflowNode> criticalPath;

		criticalPath = findPartialCriticalPath(curNode);

		if (criticalPath.isEmpty())
			return;
		// i add the critical paths results
		SimulationResults.criticalPaths.add(criticalPath);

		assignPath(criticalPath);
		for (int i = 0; i < criticalPath.size(); i++) {
			updateChildrenEST(criticalPath.get(i));
			updateParentsLFT(criticalPath.get(i));
		}
		for (int i = 0; i < criticalPath.size(); i++)
			assignParents(criticalPath.get(i));

		assignParents(curNode);
	}

	private void assignPath(List<WorkflowNode> path) {
		SimulationResults.criticalPath = path;
		float cost, bestCost = Float.MAX_VALUE;
		int bestInst = -1;
		WorkflowNode last = path.get(path.size() - 1), first = path.get(0);
		// checks if the child of last task on the path is scheduled on a resource or
		// not, if yes then it checks that is there a way that
		// we can put the path nodes before of the last nodes's child on the resource
		// Check last node's children
		for (Link child : last.getChildren()) {
			WorkflowNode childNode = graph.getNodes().get(child.getId());
			if (childNode.isScheduled() && !childNode.getId().equals(graph.getEndId()))
				if (checkChildInstance(path, childNode)) {
					setChildInstance(path, childNode);
					return;
				}
		}

		// checks that if it can put the path after father of the first node parent;
		// Check first node's parents
		// in this part we could make a
		for (Link parent : first.getParents()) {
			WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			if (parentNode.isScheduled() && !parentNode.getId().equals(graph.getStartId())) {
				List<WorkflowNode> tasks = instances.getInstance(parentNode.getSelectedResource()).getTasks();

				int place;
				for (place = 0; !tasks.get(place).getId().equals(parentNode.getId()); place++)
					;

				if (place < tasks.size() - 1) {
					WorkflowNode childNode = tasks.get(place + 1);
					if (checkChildInstance(path, childNode)) {
						setChildInstance(path, childNode);
						return;
					}
				}
			}
		}

		// if we could not assign this path on to one of the resources either its last
		// task childs is on it neither its first task father on it, we will check other
		// resource instances that we have already allocated
		for (int curInst = 0; curInst < instances.getSize(); curInst++) {
			cost = checkInstance(path, instances.getInstance(curInst));
			if (cost < bestCost) {
				bestCost = cost;
				bestInst = curInst;
			}
		}
		if (bestInst == -1) {
			Instance inst;
			int bestRes = 0;
			for (int curRes = resources.getSize() - 1; curRes >= 0; curRes--) { // because the cheapest one is the last
				inst = new Instance(-1, resources.getResource(curRes));
				cost = checkInstance(path, inst);
				if (cost < bestCost) {
					bestCost = cost;
					bestRes = curRes;
				}
			}
			inst = new Instance(instances.getSize(), resources.getResource(bestRes));
			instances.addInstance(inst);
			bestInst = inst.getId();
		}

		setInstance(path, instances.getInstance(bestInst));
	}

	private float checkInstance(List<WorkflowNode> path, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int interval = resources.getInterval();
		double curCost = Math.ceil((double) (finishTime - startTime) / (double) interval) * curInst.getType().getCost();
		double newCost;
		int[] newESTs = new int[path.size()], newLFTs = new int[path.size()];
		boolean success = true;
		long curTime = 0;

		// check after finish time
		long curIntervalFinish = startTime
				+ (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		newESTs = computeNewESTs(path, curInst, finishTime);
		newLFTs = computeNewLFTs(path, curInst, path.get(path.size() - 1).getLFT());
		if (newESTs[0] > curIntervalFinish && finishTime != 0)
			success = false;
		if (finishTime == 0)
			startTime = newESTs[0];
		for (int i = 0; i < path.size() && success; i++) {
			WorkflowNode curNode = path.get(i);
			curTime = newESTs[i] + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			if (curTime > newLFTs[i])
				success = false;
		}
		if (success) {
			newCost = Math.ceil((double) (curTime - startTime) / (double) resources.getInterval())
					* curInst.getType().getCost();
			newCost -= curCost;
			return ((float) newCost);
		}

		// check before start time
		if (!backCheck)
			return (Float.MAX_VALUE);
		long curIntervalStart = finishTime
				- (long) Math.ceil((double) (finishTime - startTime) / (double) interval) * interval;
		newLFTs = computeNewLFTs(path, curInst, startTime);
		newESTs = computeNewESTs(path, curInst, path.get(0).getEST());
		if (newLFTs[path.size() - 1] > curIntervalStart)
			success = true;
		else
			success = false;
		for (int i = path.size() - 1; i >= 0 && success; i--) {
			WorkflowNode curNode = path.get(i);
			curTime = newLFTs[i] - Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			if (curTime < newESTs[i])
				success = false;
		}
		if (success) {
			newCost = Math.ceil((double) (finishTime - curTime) / (double) resources.getInterval())
					* curInst.getType().getCost();
			newCost -= curCost;
			return ((float) newCost);
		}

		return (Float.MAX_VALUE);
	}

	private void setInstance(List<WorkflowNode> path, Instance curInst) {
		long finishTime = curInst.getFinishTime();
		long startTime = curInst.getStartTime();
		int[] newESTs = new int[path.size()], newLFTs = new int[path.size()], newDTT = new int[path.size()];
		boolean success = true;
		long curTime = 0, curRuntime;
		int firstStart = path.get(0).getEST(), lastFinish = path.get(path.size() - 1).getLFT();

		// check after finish time
		newESTs = computeNewESTs(path, curInst, finishTime);
		newLFTs = computeNewLFTs(path, curInst, lastFinish);

		// my edit
		newDTT = computeNewDataTransferTime(path, curInst, 0);

		for (int i = 0; i < path.size() && success; i++) {
			WorkflowNode curNode = path.get(i);
			curRuntime = Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			curTime = newESTs[i] + curRuntime;
			if (curTime > newLFTs[i])
				success = false;

			curNode.setEST(newESTs[i]);
			curNode.setEFT((int) curTime);

			curNode.setLFT(newLFTs[i]);
			curNode.setLST((int) (newLFTs[i] - curRuntime));

			// curNode.setLFT(curNode.getEFT());
			// curNode.setLST(curNode.getEST());

			// my edit ahmad
			curNode.setNeedTransferTime(newDTT[i]);

			curNode.setRunTime((int) curRuntime);
			curNode.setSelectedResource(curInst.getId());
			curNode.setScheduled();
			curInst.addExe(curNode.getId(), curNode.getEST(), curNode.getEFT(), 0, 0);
		}
		if (success) {
			if (curInst.getFinishTime() == 0) {
				curInst.setStartTime(path.get(0).getEST());
				curInst.setFirstTask(path.get(0).getId());
			}
			// *************************************************************
			// curInst.setFinishTime(path.get(path.size()-1).getLFT());
			curInst.setFinishTime(path.get(path.size() - 1).getEFT());
			curInst.setLastTask(path.get(path.size() - 1).getId());

			curInst.getTasks().addAll(curInst.getTasks().size(), path);

			return;
		} else
			for (int i = 0; i < path.size(); i++)
				path.get(i).setUnscheduled();

		// check before start time
		if (!backCheck)
			return;
		newLFTs = computeNewLFTs(path, curInst, startTime);
		newESTs = computeNewESTs(path, curInst, firstStart);
		// my edit ahmad
		newDTT = computeNewDataTransferTime(path, curInst, 0);

		success = true;
		for (int i = path.size() - 1; i >= 0 && success; i--) {
			WorkflowNode curNode = path.get(i);
			curRuntime = Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			curTime = newLFTs[i] - curRuntime;
			if (curTime < newESTs[i])
				success = false;

			curNode.setLFT(newLFTs[i]);
			curNode.setLST((int) curTime);

			// curNode.setEST(curNode.getLST());
			// curNode.setEFT(curNode.getLFT());
			curNode.setEST(newESTs[i]);
			curNode.setEFT((int) (newESTs[i] + curRuntime));

			// my edit ahmad
			curNode.setNeedTransferTime(newDTT[i]);

			curNode.setRunTime((int) curRuntime);
			curNode.setSelectedResource(curInst.getId());
			curNode.setScheduled();
			
			curInst.addExe(curNode.getId(), curNode.getEST(), curNode.getEFT(), 0, 0);

		}
		if (curInst.getFinishTime() == 0) {
			// *************************************************************
			// curInst.setFinishTime(path.get(path.size()-1).getLFT());
			curInst.setFinishTime(path.get(path.size() - 1).getEFT());
			curInst.setLastTask(path.get(path.size() - 1).getId());
		}
		curInst.setStartTime(path.get(0).getEST());
		curInst.setFirstTask(path.get(0).getId());

		curInst.getTasks().addAll(0, path);
	}

	private int[] computeNewESTs(List<Broker.WorkflowNode> path, Instance inst, long startTime) {
		int[] newESTs = new int[path.size()];
		int[] tempESTs = new int[path.size()];
		long start, selfTransfer, max, temp, curTime = startTime;
		temp = startTime;
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			selfTransfer = 0;

			Broker.WorkflowNode curNode = path.get(i);
			temp = GetMyFileTransferTime(temp, curNode, inst);

//			for (Link parent : curNode.getParents()) {
//				IaaSCloudWorkflowScheduler.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
//				if (parentNode.isScheduled()) {
//					start = parentNode.getEFT();
//
//					if (parentNode.getSelectedResource() != inst.getId()) {
//						// max=super.GetMyFileTransferTime(curStart, curNode, in)
//						max += Math.round((float) parent.getDataSize() / bandwidth);
//						selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
//						if (selfTransfer > max)
//							max = selfTransfer;
//						start += max;
//					} else
//						start += Math.round((float) curNode.getReadFileSize() / bandwidth);
//				} else {
//					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
//						start = curTime + Math.round((float) curNode.getReadFileSize() / bandwidth);
//					else
//						start = parentNode.getEFT() + Math.round((float) curNode.getReadFileSize() / bandwidth)
//								+ Math.round((float) parent.getDataSize() / bandwidth);
//				}
//				if (start > curTime)
//					curTime = start;
//			}
			
		//	newESTs[i] = (int) curTime;
			tempESTs[i] = (int) temp;
			temp += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
		//	curTime += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
		}

		return (tempESTs);
	}

	private int[] computeNewDataTransferTime(List<Broker.WorkflowNode> path, Instance inst, long startTime) {
		int[] newESTs = new int[path.size()];
		int[] tempESTs = new int[path.size()];
		long start, selfTransfer, max, temp, curTime = startTime;
		temp = startTime;
		for (int i = 0; i < path.size(); i++) {
			max = 0;
			selfTransfer = 0;

			Broker.WorkflowNode curNode = path.get(i);
			temp = GetMyFileTransferTime(0, curNode, inst);

			for (Link parent : curNode.getParents()) {
				Broker.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
				if (parentNode.isScheduled()) {
					start = parentNode.getEFT();

					if (parentNode.getSelectedResource() != inst.getId()) {
						// max=super.GetMyFileTransferTime(curStart, curNode, in)
						max += Math.round((float) parent.getDataSize() / bandwidth);
						selfTransfer = Math.round((float) curNode.getReadFileSize() / bandwidth);
						if (selfTransfer > max)
							max = selfTransfer;
						start += max;
					} else
						start += Math.round((float) curNode.getReadFileSize() / bandwidth);
				} else {
					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
						start = curTime + Math.round((float) curNode.getReadFileSize() / bandwidth);
					else
						start = parentNode.getEFT() + Math.round((float) curNode.getReadFileSize() / bandwidth)
								+ Math.round((float) parent.getDataSize() / bandwidth);
				}
				if (start > curTime)
					curTime = start;
			}
			
			newESTs[i] = (int) curTime;
			tempESTs[i] = (int) temp;
			temp += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
			curTime += Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
		}

		return (tempESTs);
	}

	
//	private int[] computeNewDataTransferTime2(List<IaaSCloudWorkflowScheduler.WorkflowNode> path, Instance inst,
//			long startTime) {
//		int[] newDTTs = new int[path.size()];
//		long start = 0, curTime = startTime;
//		if (path.get(0).getId().contains("ID00010"))
//			System.out.print("");
//		for (int i = 0; i < path.size(); i++) {
//			IaaSCloudWorkflowScheduler.WorkflowNode curNode = path.get(i);
//			for (Link parent : curNode.getParents()) {
//				IaaSCloudWorkflowScheduler.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
//				if (parentNode.isScheduled()) {
//					// start = parentNode.getEFT();
//					if (parentNode.getSelectedResource() != inst.getId())
//						start = Math.round((float) parent.getDataSize() / bandwidth);
//				} else {
//					if (i > 0 && parentNode.getId().equals(path.get(i - 1).getId()))
//						start = 0;
//					else
//						start = Math.round((float) parent.getDataSize() / bandwidth);
//					// edit start = parentNode.getEFT() + Math.round((float) parent.getDataSize() /
//					// bandwidth);
//				}
//				if (start > curTime)
//					curTime = start;
//			}
//
//			newDTTs[i] = (int) curTime;
//			// curTime += Math.round((float) curNode.getInstructionSize() /
//			// inst.getType().getMIPS());
//		}
//
//		return (newDTTs);
//	}
//
	private int[] computeNewLFTs(List<Broker.WorkflowNode> path, Instance inst, long finishTime) {
		int[] newLFTs = new int[path.size()];
		long finish, curTime = finishTime;

		for (int i = path.size() - 1; i >= 0; i--) {
			Broker.WorkflowNode curNode = path.get(i);
			for (Link child : curNode.getChildren()) {
				Broker.WorkflowNode childNode = graph.getNodes().get(child.getId());
				if (childNode.isScheduled()) {
					finish = childNode.getLST();
					if (childNode.getSelectedResource() != inst.getId())
						finish -= Math.round((float) child.getDataSize() / bandwidth);
				} else {
					if (i < path.size() - 1 && childNode.getId().equals(path.get(i + 1).getId()))
						finish = curTime;
					else
						finish = childNode.getLST() - Math.round((float) child.getDataSize() / bandwidth);
				}
				if (finish < curTime)
					curTime = finish;
			}

			newLFTs[i] = (int) curTime;
			curTime -= Math.round((float) curNode.getInstructionSize() / inst.getType().getMIPS());
		}

		return (newLFTs);
	}

	private boolean checkChildInstance(List<WorkflowNode> path, WorkflowNode child) {
		Instance curInst = instances.getInstance(child.getSelectedResource());
		int[] newESTs = new int[path.size()], newLFTs = new int[path.size()], newDTT = new int[path.size()];
		boolean success = true;
		int curTime = 0;

		// find the place of child in the task list of its instance
		int place;
		for (place = 0; !curInst.getTasks().get(place).getId().equals(child.getId()); place++)
			;

		newESTs = computeNewESTs(path, curInst, curInst.getTasks().get(place - 1).getEFT());
		newLFTs = computeNewLFTs(path, curInst, path.get(path.size() - 1).getLFT());

		for (int i = 0; i < path.size() && success; i++) {
			WorkflowNode curNode = path.get(i);
			curTime = newESTs[i] + Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			if (curTime > newLFTs[i])
				success = false;
		}
		if (!success)
			return (false);

		for (int i = place; i < curInst.getTasks().size() && success; i++) {
			WorkflowNode curNode = curInst.getTasks().get(i);
			curTime += curNode.getRunTime();
			if (curTime > curNode.getLFT())
				success = false;
		}

		return (success);
	}

	private void setChildInstance(List<WorkflowNode> path, WorkflowNode child) {
		Instance curInst = instances.getInstance(child.getSelectedResource());
		int[] newESTs = new int[path.size()], newLFTs = new int[path.size()], newDTT = new int[path.size()];
		int curTime = 0, curRuntime;

		// find the place of child in the task list of its instance
		int place;
		for (place = 0; !curInst.getTasks().get(place).getId().equals(child.getId()); place++)
			;

		newESTs = computeNewESTs(path, curInst, curInst.getTasks().get(place - 1).getEFT());
		newLFTs = computeNewLFTs(path, curInst, path.get(path.size() - 1).getLFT());
		// my edit ahmad
		newDTT = computeNewDataTransferTime(path, curInst, 0);

		for (int i = 0; i < path.size(); i++) {
			WorkflowNode curNode = path.get(i);
			curRuntime = Math.round((float) curNode.getInstructionSize() / curInst.getType().getMIPS());
			curTime = newESTs[i] + curRuntime;

			curNode.setEST(newESTs[i]);
			curNode.setEFT((int) curTime);

			curNode.setLFT(newLFTs[i]);
			curNode.setLST((int) (newLFTs[i] - curRuntime));
			// curNode.setLFT(curNode.getEFT());
			// curNode.setLST(curNode.getEST());
			curNode.setNeedTransferTime(newDTT[i]);

			curNode.setRunTime((int) curRuntime);
			curNode.setSelectedResource(curInst.getId());
			curNode.setScheduled();
		}

		for (int i = place; i < curInst.getTasks().size(); i++) {
			WorkflowNode curNode = curInst.getTasks().get(i);

			if (curTime > curNode.getEST()) {
				curNode.setEST(curTime);
				curTime += curNode.getRunTime();
				curNode.setEFT(curTime);
			}
		}

		curInst.getTasks().addAll(place, path);
		curInst.setStartTime(curInst.getTasks().get(0).getEST());
		curInst.setFinishTime(curInst.getTasks().get(curInst.getTasks().size() - 1).getEFT());
	}

	protected void updateChildrenEST(Broker.WorkflowNode parentNode) {
		for (Link child : parentNode.getChildren()) {
			WorkflowNode childNode = graph.getNodes().get(child.getId());
			int newEST;
			int newDataTransferTime = 0;

			if (parentNode.isScheduled()) {
				newEST = parentNode.getEFT();
				if (!childNode.isScheduled() || parentNode.getSelectedResource() != childNode.getSelectedResource()) {
					newEST += Math.round((float) child.getDataSize() / bandwidth);
					newDataTransferTime = Math.round((float) child.getDataSize() / bandwidth);
				}
			} else {
				newEST = parentNode.getEFT() + Math.round((float) child.getDataSize() / bandwidth);
				newDataTransferTime = Math.round((float) child.getDataSize() / bandwidth);
			}
			if (childNode.getEST() < newEST) {
				childNode.setEST(newEST);
				childNode.setEFT(newEST + childNode.getRunTime());
				childNode.setNeedTransferTime(newDataTransferTime);
				// *************************************************************
				if (childNode.isScheduled()
						&& instances.getInstance(childNode.getSelectedResource()).getFinishTime() < childNode.getEFT())
					instances.getInstance(childNode.getSelectedResource()).setFinishTime(childNode.getEFT());

				updateChildrenEST(childNode);
			}
		}
	}

	protected void updateParentsLFT(Broker.WorkflowNode childNode) {
		for (Link parent : childNode.getParents()) {
			Broker.WorkflowNode parentNode = graph.getNodes().get(parent.getId());
			int newLFT;

			if (childNode.isScheduled()) {
				newLFT = childNode.getLST();
				if (!parentNode.isScheduled() || parentNode.getSelectedResource() != childNode.getSelectedResource())
					newLFT -= Math.round((float) parent.getDataSize() / bandwidth);
			} else
				newLFT = childNode.getLST() - Math.round((float) parent.getDataSize() / bandwidth);
			if (parentNode.getLFT() > newLFT) {
				parentNode.setLFT(newLFT);
				parentNode.setLST(newLFT - parentNode.getRunTime());

//				if (parentNode.getLFT() < parentNode.getEFT())
//					System.out.println("LFT Setting: Id=" + parentNode.getId() + " EFT=" + parentNode.getEFT() + " LFT="
//							+ parentNode.getLFT());

				updateParentsLFT(parentNode);
			}
		}
	}

	protected WorkflowNode findCriticalParent(Broker.WorkflowNode child) {
		Broker.WorkflowNode criticalPar = null;
		int criticalParStart = -1, curStart;

		for (Link parentLink : child.getParents()) {
			Broker.WorkflowNode parentNode = graph.getNodes().get(parentLink.getId());
			if (parentNode.isScheduled())
				continue;

			curStart = parentNode.getEFT() + Math.round((float) parentLink.getDataSize() / bandwidth);
			if (curStart > criticalParStart) {
				criticalParStart = curStart;
				criticalPar = parentNode;
			}
		}
		// if (criticalPar != null)
		// if (graph.nodes.get(child.getId()).getCriticalParent() == null)
		// graph.nodes.get(child.getId()).setCriticalParent(criticalPar);
		return (criticalPar);
	}

	protected WorkflowNode SetCriticalParent(Broker.WorkflowNode child) {
		Broker.WorkflowNode criticalPar = null;
		int criticalParStart = -1, curStart;

		for (Link parentLink : child.getParents()) {
			Broker.WorkflowNode parentNode = graph.getNodes().get(parentLink.getId());
			if (parentNode.isScheduled())
				continue;

			curStart = parentNode.getEFT() + Math.round((float) parentLink.getDataSize() / bandwidth);
			if (curStart > criticalParStart) {
				criticalParStart = curStart;
				criticalPar = parentNode;
			}
		}
		if (criticalPar != null)
			if (graph.nodes.get(child.getId()).getCriticalParent() == null)
				graph.nodes.get(child.getId()).setCriticalParent(criticalPar);
		return (criticalPar);
	}

	protected List<WorkflowNode> findPartialCriticalPath(Broker.WorkflowNode curNode) {
		List<WorkflowNode> criticalPath = new ArrayList<WorkflowNode>();

		do {
			SetCriticalParent(curNode);
			curNode = findCriticalParent(curNode);
			if (curNode != null) {
				criticalPath.add(0, curNode);

			}
		} while (curNode != null);
		return (criticalPath);
	}

	protected void setEndNodeEST() {
		int endTime = -1;
		Broker.WorkflowNode endNode = graph.getNodes().get(graph.getEndId());

		for (Link parent : endNode.getParents()) {
			int curEndTime = graph.getNodes().get(parent.getId()).getEFT();
			if (endTime < curEndTime)
				endTime = curEndTime;
		}
		endNode.setEST(endTime);
		endNode.setEFT(endTime);
		endNode.setNeedTransferTime(0);
	}

	@Override
	public float schedule(int startTime, int deadline, float cost) {
		// TODO Auto-generated method stub
		return 0;
	}

}
