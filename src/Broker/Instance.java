package Broker;

import java.util.ArrayList;
import java.util.List;

import DAG.LinkageType;

public class Instance {

	private int id;
	private Resource type;
	private long startTime, finishTime;
	private String firstTaskId, lastTaskId;
	private List<WorkflowNode> tasks;
	private long RunTime, IdleTime, RealRunTime;
	private double utilization;
	private List<File> FileSet;
	private List<execution> exeList;

	public Instance(int newId, Resource t) {

		id = newId;
		type = t;
		startTime = 0;
		finishTime = 0;
		FileSet = new ArrayList<File>();
		tasks = new ArrayList<WorkflowNode>();
	}

	public Instance(int newId, Resource t, long st, long ft) {
		id = newId;
		type = t;
		startTime = st;
		finishTime = ft;
		FileSet = new ArrayList<File>();
		tasks = new ArrayList<WorkflowNode>();
	}

	public void addFiles(List<File> files) {
		if (files == null)
			return;
		for (File file : files) {
			if (!this.FileSet.contains(file))
				this.FileSet.add(file);
		}
	}

	public List<File> getNotExistedFiles(List<File> files) {

		List<File> ff = new ArrayList<File>();
		int index = -1;
		for (File f1 : files) {
			index = -1;
			index = SearchIndexFile(f1);
			if (index == -1)
				ff.add(f1);
		}
		return ff;
	}

	public int SearchIndexFile(File f1) {

		File file = null;
		for (int i = 0; i < FileSet.size(); i++) {
			file = FileSet.get(i);
			if (file.fileName.contains(f1.getFileName()))
				return i;
		}
		return -1;

	}

	public long getNotExistedFileSizes(List<File> files) {
		if (files == null)
			return 0;
		List<File> ff = getNotExistedFiles(files);
		long size = 0;
		for (File file : ff) {
			if (file.getLink() == LinkageType.INPUT)
				size += file.getFileSize();
		}
		return size;
	}

	public void addExe(String id, long start, long finish, long readStart,float cost) {
		if (exeList == null)
			exeList = new ArrayList<execution>();
		execution ex = new execution(id, start, finish, readStart, cost);
		exeList.add(ex);
	}

	public List<execution> getExeList() {
		return exeList;
	}

	public long getRealRunTime() {
		return RealRunTime;
	}

	public double getUtilization() {
		return utilization;
	}

	public void setUtilization(double utilization) {
		this.utilization = utilization;
	}

	public void setRealRunTime(long realRunTime) {
		RealRunTime = realRunTime;
	}

	public void CalculateTasksTimeInInstances() {
		long InsRunTime = 0;
		for (execution ts : getExeList()) {
			InsRunTime += ts.getFinish()- ts.getStart();
		}
		long runtime = getFinishTime() - getStartTime();
		long ceil = (long) Math.ceil((double) runtime / (double) WorkflowBroker.interval);
		long idleTime = ((ceil * WorkflowBroker.interval) - InsRunTime);
		double utlization = (double) (runtime) * (100) / (double) (ceil * WorkflowBroker.interval);
		this.setRealRunTime(InsRunTime);
		this.setRunTime(runtime);
		this.setIdleTime(idleTime);
		this.setUtilization(utlization);
	}

	public long getRunTime() {
		return RunTime;
	}

	public void setRunTime(long runTime) {
		RunTime = runTime;
	}

	public long getIdleTime() {
		return IdleTime;
	}

	public void setIdleTime(long idleTime) {
		IdleTime = idleTime;
	}

	public void setStartTime(long st) {
		if (st >= 0)
			startTime = st;
	}

	public void setFinishTime(long ft) {
		if (ft >= 0)
			finishTime = ft;
	}

	public long getStartTime() {
		return (startTime);
	}

	public long getFinishTime() {
		return (finishTime);
	}

	public int getId() {
		return (id);
	}

	public Resource getType() {
		return (type);
	}

	public void setFirstTask(String id) {
		firstTaskId = id;
	}

	public String getFirstTask() {
		return (firstTaskId);
	}

	public void setLastTask(String id) {
		lastTaskId = id;
	}

	public String getLastTask() {
		return (lastTaskId);
	}

	public List<WorkflowNode> getTasks() {
		return (tasks);
	}

	public void addTask(WorkflowNode task) {
		tasks.add(task);
	}

	public execution getExectionTask(String id) {

		for (execution ex : exeList) {
			if (ex.getId().contains(id)) {
				return ex;
			}
		}
		return null;
	}

	public class execution {
		public String id;
		public long start;
		public long finish;
		public long readStart;
		public float Cost;

		execution(String id, long start, long finish, long readStart) {
			this.id = id;
			this.start = start;
			this.finish = finish;
			this.readStart = readStart;
		}

		execution(String id, long start, long finish, long readStart, float cost) {
			this.id = id;
			this.start = start;
			this.finish = finish;
			this.readStart = readStart;
			this.Cost = cost;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getFinish() {
			return finish;
		}

		public void setFinish(long finish) {
			this.finish = finish;
		}

		public long getReadStart() {
			return readStart;
		}

		public void setReadStart(long readStart) {
			this.readStart = readStart;
		}

		public float getCost() {
			return Cost;
		}

		public void setCost(float cost) {
			Cost = cost;
		}
		

	}

}
