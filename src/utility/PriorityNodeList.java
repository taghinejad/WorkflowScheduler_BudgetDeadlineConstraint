package utility;

import java.util.ArrayList;
import java.util.List;

import Broker.WorkflowNode;
import sun.text.normalizer.IntTrie;

public class PriorityNodeList {

	List<WorkflowNode> list;

	enum ordering {
		ASC, DESC
	};

	public PriorityNodeList() {
		list = new ArrayList();
	}

	public void Add(WorkflowNode wn) {
		list.add(wn);
	}
	public boolean isEmpty()
	{
		if (list.size()<1) return true;
		else return false;
	}

	public void AddNoDuplicate(WorkflowNode wn) {
		boolean notExist = true;
		for (WorkflowNode item : list) {
			if (item.getId().contains(wn.getId())) {
				notExist = false;
				break;
			}
		}
		if (notExist)
			list.add(wn);
	}

	public void removeaNode(WorkflowNode wn) {

		for (WorkflowNode item : list) {
			if (item.getId().contains(wn.getId())) {
				list.remove(item);
			}
		}
	}

	public WorkflowNode pullbyEST() {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getEST() < est) {
				temp = item;
				est = item.getEST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyWeight() {
		int est = Integer.MAX_VALUE;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getRunTime() < est) {
				temp = item;
				est = item.getEST();
			}
		}
		list.remove(temp);
		return temp;
	}
	public WorkflowNode pullbyUpWard() {
		int est = -1;
		WorkflowNode temp = list.get(0);
		for (WorkflowNode item : list) {
			if (item.getUpRank() > est) {
				temp = item;
				est = item.getUpRank();
			}
		}
		list.remove(temp);
		return temp;
	}
	public PriorityNodeList(List<WorkflowNode> list) {
		super();
		this.list = list;
	}

	public List<WorkflowNode> getList() {
		return list;
	}

	public void setList(List<WorkflowNode> list) {
		this.list = list;
	}

}
