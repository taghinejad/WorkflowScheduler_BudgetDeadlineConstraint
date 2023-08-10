package algorithms;

import java.util.PriorityQueue;

import Broker.Log;
import Broker.ScheduleType;
import Broker.WorkflowBroker;

import java.io.PrintWriter;
import java.io.FileWriter;

import DAG.DagUtils;
public class Test {
	public static void main(String[] args) {
		scheduleWorkflow();
		//createResources();
		//temp();
	}
	//float cost, CC = 1, CH;
	private static void scheduleWorkflow() {
		//String WfFile = "WfDescFiles\\Mine_35.xml" , resultFile = "results\\Sipht_30_new.txt";
	//	String WfFile = "D:/JavaApps/WfDescFiles/motif_medium.xml" ;
		String WfFile = "dax/Montage_25.xml" ;
		//String WfFile = "WfDescFiles\\Sipht_1000.xml" ;
		//D:/EclipseWorkset/WorkflowSim-1.0-master/config/dax/Montage_100.xml
		int startTime = 0, deadline = 100, finishTime, MH, MC;
		float cost, CC = 1, CH;
		WorkflowBroker wb=null;
		PrintWriter out=null;
		long realStartTime, realFinishTime;
		int interval = 3600;
	    long bandwidth=20000000;
		/*try {
    		out = new PrintWriter(new FileWriter(resultFile));
        } catch (Exception e) {}
        */
		
		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.Fastest,interval,bandwidth) ;
		}catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!"+e.getLocalizedMessage());
		}
		//CH is Cost MH is time
		CH=wb.schedule(startTime, deadline);
		MH=wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		System.out.println("Fastest: cost="+CH+" time="+MH  + " Mips: "+ wb.resources.getMaxMIPS()+ " Cost:"+wb.resources.getMaxCost());

		try {
			wb = new WorkflowBroker(WfFile, ScheduleType.Cheapest,interval,bandwidth) ;
		}catch (Exception e) {
			System.out.println("Error in creating workflow broker!!!"+e.getLocalizedMessage());
		}
		//CC is CHepest Resource Cost MH is Chepest Resource time
		CC=wb.schedule(startTime, deadline);
		MC=wb.graph.getNodes().get(wb.graph.getEndId()).getAST();
		//System.out.println("Cheapest: cost="+CC+" time="+MC);
		System.out.println("Cheapest: cost="+CC+" time="+MC  + " Mips: "+ wb.resources.getMinMIPS()+ " Cost:"+wb.resources.getMinCost());

        //deadline = Math.round((float)3*finishTime);
        //for (deadline=28000; deadline<=28000; deadline+=1800) {
        for (float alpha=(float)1.5; alpha<=5; alpha+=0.5) {
        	//if (alpha == 1.5)
        		//alpha = (float)1.6;
	        deadline = Math.round(alpha*500);
	        System.out.println("deadline="+deadline +"{");
	        
	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.IC_PCP,interval,bandwidth) ;
			}catch (Exception e) {}
			realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			realFinishTime = System.currentTimeMillis();
			realFinishTime -= realStartTime;
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("\n PCP: cost="+cost+" NC="+ cost/CC+"  time="+finishTime+"  Instances="+wb.policy.instances.getSize());
	        //System.out.println("Real Time="+realFinishTime);
	       // Log.printInstances(wb.policy.instances,wb.graph);
	    //    printWorkflow(wb.graph);

	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.IC_PCP2,interval,bandwidth) ;
			}catch (Exception e) {}
			realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			realFinishTime = System.currentTimeMillis();
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("PCP2: cost="+cost+" NC="+ cost/CC+" time="+finishTime+"  Instances="+wb.policy.instances.getSize());
	     //   Log.printInstances(wb.policy.instances,wb.graph);
	       // Log.printWorkflow(wb.graph);
	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.List,interval,bandwidth) ;
			}catch (Exception e) {}
			//realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			//realFinishTime = System.currentTimeMillis();
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("List: cost="+cost+" NC="+ cost/CC+" time="+finishTime+"  Instances="+wb.policy.instances.getSize());
	        //Log.printInstances(wb);	
	        //Log.printWorkflow(wb);
	        
	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.List2,interval,bandwidth) ;
			}catch (Exception e) {}
			//realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			//realFinishTime = System.currentTimeMillis();
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("List2: cost="+cost+" NC="+ cost/CC+" time="+finishTime+"  Instances="+wb.policy.instances.getSize());

	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.IC_Loss,interval,bandwidth) ;
			}catch (Exception e) {}
			//realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			//realFinishTime = System.currentTimeMillis();
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("IC-Loss: cost="+cost+" NC="+ cost/CC+" time="+finishTime+"  Instances="+wb.policy.instances.getSize());
	        
	       /* try {
				wb = new WorkflowBroker(WfFile, ScheduleType.IC_PCPD2) ;
			}catch (Exception e) {}
			realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			realFinishTime = System.currentTimeMillis();
			realFinishTime -= realStartTime;
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("PCPD2: cost="+cost+" NC="+ cost/CC+" time="+finishTime);
	        System.out.println("Time="+realFinishTime);
	        //Log.printInstances(wb.policy.instances,wb.graph);	
	        //printWorkflow(wb.graph);

	        try {
				wb = new WorkflowBroker(WfFile, ScheduleType.IC_PCPD2_2) ;
			}catch (Exception e) {}
			//realStartTime = System.currentTimeMillis();
			cost=wb.schedule(startTime, deadline);
			//realFinishTime = System.currentTimeMillis();
			finishTime = wb.graph.getNodes().get(wb.graph.getEndId()).getEST();
	        System.out.println("PCPD2_2: cost="+cost+" NC="+ cost/CC+" time="+finishTime);
	        //Log.printInstances(wb.policy.instances,wb.graph);	
	        //Log.printWorkflow(wb.graph);


	        
	        */
	      //  System.out.println("");
	        System.out.println("---------deadline="+deadline +"} \n");
        }
	}
	
//	private static void printWorkflow(WorkflowGraph g) {
//		PriorityQueue<WorkflowNode> queue = new PriorityQueue<WorkflowNode>(g.nodes.size(), new WorkflowPolicy.ASTComparator()) ;
//		
//		for (WorkflowNode node : g.nodes.values())  
//			if (!node.getId().equals(g.getStartId()) && !node.getId().equals(g.getEndId())) 
//				queue.add(node) ;
//		
//		
//		while (!queue.isEmpty()) {
//			WorkflowNode n = queue.remove() ;
//			if (n.getRunTime() > 0)
//				System.out.println("Id="+n.getId()+" RT="+n.getRunTime()+" SR="+n.getSelectedResource()+
//						" AST="+n.getEST()+" AFT="+n.getEFT()+" Deadline="+n.getDeadline());
//		}
//	}
//	
//	private static void printInstances(InstanceSet instances, WorkflowGraph g) {
//		System.out.println("Instances = "+instances.getSize());
//		for (int i=0; i<instances.getSize(); i++) {
//			Instance cur = instances.getInstance(i);
//			System.out.println("id="+cur.getId()+" type="+cur.getType().getId()+" start="+g.nodes.get(cur.getFirstTask()).getEST()+" end= "+g.nodes.get(cur.getLastTask()).getEFT()+" task count="+cur.getTasks().size());
//		}
//	}
	
	static void temp() {
		int[][] times={{5,8,12},{8,12,20},{6,12,15}};
		int[][] costs={{8,5,3},{12,6,4},{8,5,3}};
		int totalTime, totalCost;
		
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				for (int k=0; k<3; k++) {
					totalTime = times[0][i] + times[1][j]+times[2][k];
					totalCost = costs[0][i] + costs[1][j]+costs[2][k];
					System.out.println("time= "+totalTime+" cost="+totalCost);
				}
			}
		}
	}
}
