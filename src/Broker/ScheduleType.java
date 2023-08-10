package Broker;

public enum ScheduleType {
	Fastest("Fastest"), Cheapest("Cheapest"), CheapestCP("CheapestCP"), IC_PCP("IC-PCP"), IC_PCPD2("IC-PCPD2"), List("List"), IC_PCP2("IC-PCP2"), IC_PCPD2_2("IC-PCPD2-2"), List2("List2"), IC_Loss("IC-Loss"), My_CHEAPEST("My_CHEAPEST"), MY_FAST("MY_FAST"), CbCR("CbCR"), HEFT("HEFT"), BDHEFT("BDHEFT"), MYPCP("MYPCP"), BDAS("BDAS"), MYBDHEFT("MYBDHEFT"), BDSDson("BDSDson"),FastestCP("FastestCP"), DBCS_HARAB("DBCS_HARAB"),FDASrealHeftBudget("FDASrealHeftBudget"),BDDCtaghinezhad("BDDCtaghinezhad"),BDCtaghinezhad("BDCtaghinezhad"), CheapestDeadline("CheapestDeadline");
	private final String value;

	private ScheduleType(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}

	public static ScheduleType convert(String val) {
		for (ScheduleType inst : values()) {
			if (inst.toString().equals(val)) {
				return inst;
			}
		}
		return null;
	}
}
