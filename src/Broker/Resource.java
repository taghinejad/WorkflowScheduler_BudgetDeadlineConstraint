package Broker;

import java.io.Serializable;

public class Resource implements Serializable {
	private int id;
	private float costPerInterval;
	private float MIPS;

	public Resource(int newId) {
		id = newId;
		costPerInterval = 0;
		MIPS = 0;
	}

	public Resource(int newId, float cost, float mips) {
		id = newId;
		costPerInterval = cost;
		MIPS = mips ;
	}
	
	public Resource(Resource r) {
		id = r.id;
		costPerInterval = r.costPerInterval;
		MIPS = r.MIPS;
	}
	
	public int getId() {
		return(id);
	}
	
	public float getCost() {
		return (costPerInterval);
	}

	public float getMIPS() {
		return (MIPS);
	}
	
	public void setCost(float newCost) {
		if (newCost >= 0)
			costPerInterval = newCost ;
	}
}
