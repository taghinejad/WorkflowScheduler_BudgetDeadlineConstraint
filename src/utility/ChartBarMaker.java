package utility;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import utility.Utility.*;

public class ChartBarMaker extends ApplicationFrame {

	public ChartBarMaker(String applicationTitle, String chartTitle) {
		super(applicationTitle);
		// ChartFactory.createBoxAndWhiskerChart(title, timeAxisLabel, valueAxisLabel,
		// dataset, legend)
//		JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Category", "Score", createDataset(),
//				PlotOrientation.VERTICAL, true, true, false);
		JFreeChart barChart = ChartFactory.createBarChart(chartTitle, "Category", "Score", createDatasetDB(0),
				PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel(barChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);
		
		
	}

	private CategoryDataset createDataset() {
		final String fiat = "FIAT";
		final String audi = "AUDI";
		final String ford = "FORD";
		final String speed = "Speed";
		final String millage = "Millage";
		final String userrating = "User Rating";
		final String safety = "safety";
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(-1.0, fiat, speed);
		dataset.addValue(3.0, fiat, userrating);
		dataset.addValue(5.0, fiat, millage);
		dataset.addValue(5.0, fiat, safety);

		dataset.addValue(5.0, audi, speed);
		dataset.addValue(6.0, audi, userrating);
		dataset.addValue(10.0, audi, millage);
		dataset.addValue(4.0, audi, safety);

		dataset.addValue(4.0, ford, speed);
		dataset.addValue(2.0, ford, userrating);
		dataset.addValue(3.0, ford, millage);
		dataset.addValue(6.0, ford, safety);

		dataset.addValue(4.0, "benz", speed);
		dataset.addValue(2.0, "benz", userrating);
		dataset.addValue(3.0, "benz", millage);
		dataset.addValue(6.0, "benz", safety);

		return dataset;
	}

	private CategoryDataset createDatasetDB(int index) {
		final DefaultCategoryDataset ds = new DefaultCategoryDataset();
		ResultDB rs = Utility.dbs.get(index);
		for (AlgorithmResult as : rs.algorithms) {
			ds.addValue(as.cost, as.name, "Cost");
			ds.addValue(as.makespan, as.name, "Time");
		}
		return ds;
	}
	private CategoryDataset createDatasetCost(int index) {
		final DefaultCategoryDataset ds = new DefaultCategoryDataset();
		ResultDB rs = Utility.dbs.get(index);
		for (AlgorithmResult as : rs.algorithms) {
			ds.addValue(as.cost, as.name, "Cost");
		}
		return ds;
	}
	private void setDataset()
	{
		for (ResultDB rs: Utility.dbs)
		{
			System.out.println("__________________________________"+rs.id); 
			System.out.println(" --- Interval: "+ rs.interval+ "  Bandwidth:"+rs.bandwidth + " Workflow:"+rs.Workflow );
			System.out.println(" X:"+rs.x+" Cost Constrained :"+rs.costConstrained+" Deadline Constrained:"+rs.timeConstrained );
			for (AlgorithmResult as: rs.algorithms)
			{
				System.out.println(as.name+ "   Cost:" + as.cost+ "    Time: "+ as.makespan+ "     Ncost:"+ as.NormalizedCost+ " Ntime:"+ as.NormalizedTime);
			}
		}
	}

	public static void main(String[] args) {
		ChartBarMaker chart = new ChartBarMaker("Car Usage Statistics", "Which car do you like?");
		ChartBarMaker chart2 = new ChartBarMaker("auto Usage Statistics", "Which auto do you like?");
		chart.pack();
		chart2.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		// RefineryUtilities.centerFrameOnScreen( chart2 );
		chart.setVisible(true);
		chart2.setVisible(true);
	}
}