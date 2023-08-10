package Broker;

import Broker.WorkflowPolicy.result;
import DAG.*;
import algorithms.BDCtaghinezhad;
import algorithms.BDDCtaghinezhad;
import algorithms.BDHEFTalgorithm;
import algorithms.BDSDalgorithmSun;

import algorithms.DBCS_HARAB;

import algorithms.MyBDHEFT;


import algorithms.DeadlineConstrained.MyPCP;

import algorithms.NoConstrained.CHeapestWithDeadLineStatisfaction;
import algorithms.NoConstrained.CheapestCP;
import algorithms.NoConstrained.CheapestPolicy;
import algorithms.NoConstrained.FastestCP;
import algorithms.NoConstrained.FastestPolicy;
import algorithms.NoConstrained.HEFTAlgorithm;
import algorithms.NoConstrained.MyCheapestPolicy;
import algorithms.NoConstrained.MyFast;

public class WorkflowBroker {
	public WorkflowGraph graph;
	public ResourceSet resources;
	public WorkflowPolicy policy;
	public static int interval = 3600;
	static long bandwidth = 20000000;

	String SchedulerName;
	int time = 0;
	float cost = 0;

	public ResourceSet getResources() {
		return resources;
	}

	public void setResources(ResourceSet resources) {
		this.resources = resources;
	}

	public enum ResourceProvision {
		ListAbrishami, EC2, EC2v2, Sample1, EC2ArabNejad, EC2020
	}

	public WorkflowBroker(String wfDescFile, ScheduleType type, int interval, long bandwidth, int resGen)
			throws Exception {
		DAG.Adag dag = null;
		this.interval = interval;
		this.bandwidth = bandwidth;
		try {
			dag = DagUtils.readWorkflowDescription(wfDescFile);
		} catch (Throwable e) {
			System.out.println("Error reading Workflow File " + e);
		}
		graph = new WorkflowGraph();
		graph.convertDagToWorkflowGraph(dag);
		// createResourceList()
		//
		switch (resGen) {
		case 0:
			createResourceList();
			break;
		case 1:
			createResourceListEC2();
			break;
		case 2:
			createResourceListEC2v2();
			break;
		case 3:
			createResourceListSample1();
			break;
		case 4:
			createResourceListEC2ArabNejad();
			break;
		case 2020:
			createResourceListEC2020();
			break;

		}

		if (type == ScheduleType.Fastest)
			policy = new FastestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.Cheapest)
			policy = new CheapestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.FastestCP)
			policy = new FastestCP(graph, resources, bandwidth);

		else if (type == ScheduleType.My_CHEAPEST)
			policy = new MyCheapestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.MY_FAST)
			policy = new MyFast(graph, resources, bandwidth);
		
		else if (type == ScheduleType.HEFT)
			policy = new HEFTAlgorithm(graph, resources, bandwidth);
		else if (type == ScheduleType.BDHEFT)
			policy = new BDHEFTalgorithm(graph, resources, bandwidth);
		else if (type == ScheduleType.MYBDHEFT)
			policy = new MyBDHEFT(graph, resources, bandwidth);


		else if (type == ScheduleType.BDSDson)
			policy = new BDSDalgorithmSun(graph, resources, bandwidth);
		else if (type == ScheduleType.MYPCP)
			policy = new MyPCP(graph, resources, bandwidth);
		else if (type == ScheduleType.BDDCtaghinezhad)
			policy = new BDDCtaghinezhad(graph, resources, bandwidth);
		else if (type == ScheduleType.BDCtaghinezhad)
			policy = new BDCtaghinezhad(graph, resources, bandwidth);

	
		else if (type == ScheduleType.DBCS_HARAB)
			policy = new DBCS_HARAB(graph, resources, bandwidth);
		else if (type == ScheduleType.CheapestDeadline)
			policy = new CHeapestWithDeadLineStatisfaction(graph, resources, bandwidth);
		

	}

	public float schedule(int startTime, int deadline) {
		return (policy.schedule(startTime, deadline));
	}

	public float schedule(int startTime, int deadline, float cost) {
		return (policy.schedule(startTime, deadline, cost));
	}

	private void createResourceList() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(0, 5, 100));
		resources.addResource(new Resource(1, (float) 4.5, 90));
		resources.addResource(new Resource(2, (float) 4, 80));
		resources.addResource(new Resource(3, (float) 3.5, 70));
		resources.addResource(new Resource(4, (float) 3, 60));
		resources.addResource(new Resource(5, (float) 2.5, 50));
		resources.addResource(new Resource(6, (float) 2, 40));
		resources.addResource(new Resource(7, (float) 1.5, 30));
		resources.addResource(new Resource(8, (float) 1.25, 25));
		resources.addResource(new Resource(9, (float) 1, 20));

		// resources.addResource(new Resource(0, 20, 100));
		// resources.addResource(new Resource(1, (float) 16.2, 90));
		// resources.addResource(new Resource(2, (float) 12.8, 80));
		// resources.addResource(new Resource(3, (float) 9.8, 70));
		// resources.addResource(new Resource(4, (float) 7.2, 60));
		// resources.addResource(new Resource(5, 5, 50));
		// resources.addResource(new Resource(6, (float) 3.2, 40));
		// resources.addResource(new Resource(7, (float) 1.8, 30));
		// resources.addResource(new Resource(8, (float) 1.25, 25));
		// resources.addResource(new Resource(9, (float) 0.8, 20));
		resources.sort();
	}

	private void createResourceListEC2() {
		//based on S. Sadat, M. Nik, M. Naghibzadeh, and Y. Sedaghat, “with deadline and reliability constraints,” Computing, no. 0123456789, 2019.
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 1.0, 26));
		resources.addResource(new Resource(4, (float) 0.50, 13));
		resources.addResource(new Resource(3, (float) 0.48, 8));
		resources.addResource(new Resource(2, (float) 0.24, 4));
		resources.addResource(new Resource(1, (float) 0.12, 2));
		resources.addResource(new Resource(0, (float) 0.06, 1));
		resources.sort();
	}

	private void createResourceListEC2020() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		// https://aws.amazon.com/ec2/pricing/on-demand/
		// name ECU Memory (GiB) Instance Storage (GB) Linux/UNIX Usage
		// m5.large 10 8-GiB EBS Only $0.096 per Hour
		// m5.xlarge 16 16-GiB EBS Only $0.192 per Hour
		// m5.2xlarge 37 32-GiB EBS Only $0.384 per Hour
		// m5.4xlarge 70 64-GiB EBS Only $0.768 per Hour
		// m5.8xlarge 128 128-GiB EBS Only $1.536 per Hour
		// m5.12xlarge 168 192-GiB EBS Only $2.304 per Hour

		resources.addResource(new Resource(5, (float) 5.64, 168));
		resources.addResource(new Resource(4, (float) 3.76, 128));
		resources.addResource(new Resource(3, (float) 1.88, 70));
		resources.addResource(new Resource(2, (float) 0.94, 37));
		resources.addResource(new Resource(1, (float) 0.47, 16));
		resources.addResource(new Resource(0, (float) 0.235, 10));
		resources.sort();
	}

	private void createResourceListEC2ArabNejad() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 2.520, (float) 124.5));
		resources.addResource(new Resource(4, (float) 1.008, (float) 53.5));
		resources.addResource(new Resource(3, (float) 0.504, 26));
		resources.addResource(new Resource(2, (float) 0.266, 13));
		resources.addResource(new Resource(1, (float) 0.126, (float) 6.5));
		resources.addResource(new Resource(0, (float) 0.067, 3));
		resources.sort();
	}

	private void createResourceListEC2v2() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		resources.addResource(new Resource(5, (float) 1.0, 26000));
		resources.addResource(new Resource(4, (float) 0.50, 13000));
		resources.addResource(new Resource(3, (float) 0.48, 8000));
		resources.addResource(new Resource(2, (float) 0.24, 4000));
		resources.addResource(new Resource(1, (float) 0.12, 2000));
		resources.addResource(new Resource(0, (float) 0.06, 1000));
		resources.sort();
	}

	private void createResourceListSample1() {
		resources = new ResourceSet(interval);
		// Resource(Id, Cost, MIPS)
		// resources.addResource(new Resource(2, (float) 3, 30));

		resources.addResource(new Resource(3, (float) 0.4, 40));
		resources.addResource(new Resource(2, (float) 0.3, 30));
		resources.addResource(new Resource(1, (float) 0.2, 20));
		resources.addResource(new Resource(0, (float) 0.1, 10));
		resources.sort();
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}

	public WorkflowBroker(String wfDescFile, ScheduleType type, int interval, long bandwidth) throws Exception {
		DAG.Adag dag = null;
		this.interval = interval;
		this.bandwidth = bandwidth;
		try {
			dag = DagUtils.readWorkflowDescription(wfDescFile);
		} catch (Throwable e) {
			System.out.println("Error reading Workflow File " + e);
		}
		graph = new WorkflowGraph();
		graph.convertDagToWorkflowGraph(dag);
		// createResourceList()
		//

		createResourceListEC2();

		if (type == ScheduleType.Fastest)
			policy = new FastestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.BDDCtaghinezhad)
			policy = new BDDCtaghinezhad(graph, resources, bandwidth);
		else if (type == ScheduleType.Cheapest)
			policy = new CheapestPolicy(graph, resources, bandwidth);

		else if (type == ScheduleType.My_CHEAPEST)
			policy = new MyCheapestPolicy(graph, resources, bandwidth);
		else if (type == ScheduleType.MY_FAST)
			policy = new MyFast(graph, resources, bandwidth);
		
		else if (type == ScheduleType.HEFT)
			policy = new HEFTAlgorithm(graph, resources, bandwidth);
		else if (type == ScheduleType.MYBDHEFT)
			policy = new MyBDHEFT(graph, resources, bandwidth);
		else if (type == ScheduleType.BDHEFT)
			policy = new BDHEFTalgorithm(graph, resources, bandwidth);
		
		else if (type == ScheduleType.MYPCP)
			policy = new MyPCP(graph, resources, bandwidth);

	}

}
