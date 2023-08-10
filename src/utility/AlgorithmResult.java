package utility;

import Broker.InstanceSet;

public class AlgorithmResult {
	public String name;
	public long makespan;
	public float cost;
	public float NormalizedCost,NormalizedTime;
	public InstanceSet instances;
	public float BudgetRatio;
	public float TimeRatio;
	public int Utilization;
	public AlgorithmResult(String name, long makespan, float cost, float normalizedCost, float normalizedTime,
			InstanceSet instances) {
		super();
		this.name = name;
		this.makespan = makespan;
		this.cost = cost;
		NormalizedCost = normalizedCost;
		NormalizedTime = normalizedTime;
		this.instances = instances;
	}
	public AlgorithmResult(String name, long makespan, float cost, float normalizedCost, float normalizedTime,
			InstanceSet instances,int deadlineC,float budgetC) {
		super();
		this.name = name;
		this.makespan = makespan;
		this.cost = cost;
		NormalizedCost = normalizedCost;
		NormalizedTime = normalizedTime;
		this.instances = instances;
		if (cost<=0) cost=budgetC*2;
		if (makespan<=0) makespan=deadlineC*2;
		this.BudgetRatio=(float) cost/budgetC;
		this.TimeRatio=(float)makespan/deadlineC;
	}
	public AlgorithmResult(String name, long makespan, float cost, float normalizedCost, float normalizedTime,
			InstanceSet instances,int deadlineC,float budgetC,int utilization) {
		super();
		this.name = name;
		this.makespan = makespan;
		this.cost = cost;
		NormalizedCost = normalizedCost;
		NormalizedTime = normalizedTime;
		this.instances = instances;
		if (cost<=0) cost=budgetC*2;
		if (makespan<=0) makespan=deadlineC*2;
		this.BudgetRatio=(float) cost/budgetC;
		this.TimeRatio=(float)makespan/deadlineC;
		this.Utilization=utilization;
	}
	
}
