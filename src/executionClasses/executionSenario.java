package executionClasses;

import utility.ConsoleColors;

import utility.ResultDB;
import utility.Utility;
import examples.*;

import java.util.ArrayList;
import java.util.List;

import Broker.File;
import Broker.Log;
import Broker.ScheduleType;
import Broker.WorkflowBroker.ResourceProvision;
import algorithms.BDHEFTalgorithm;

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

public class executionSenario {
	public enum SenarioBase {
		HamidArabnejad, Sun_BDSD, Mine, VahidArabnejad_BDAS,PCP
	}

	public enum Algorithm {
		BDDC, BDC, BDSDsun, DBCS_hArab, BDAS_vArab, BDHEFT
	}

	public enum Workflow {
		ALL, Montage, Sipht, Epigenomics, Inspiral, Cybershake
	}

	public enum Tsize {
		VerySmall, Small, Mediom, Large
	}

	// public enum ResourceProvision {
	// List, EC2, EC2v2, Sample1, EC2ArabNejad, EC2020
	// }
	public static void SenarioCDRangePro(Workflow workflow, Tsize tasksize, SenarioBase sb, 
			ResourceProvision resource,int bandwidth, int interval, float cStart, float cEnd, float cInc, float dStart, float dEnd,
			float dInc) {
		int res;
		int taskSeries = 0;
		String wf = "";

		if (tasksize == Tsize.VerySmall)
			taskSeries = 1;
		else if (tasksize == Tsize.Small)
			taskSeries = 2;
		else if (tasksize == Tsize.Mediom)
			taskSeries = 3;
		else if (tasksize == Tsize.Large)
			taskSeries = 4;

		if (resource == ResourceProvision.ListAbrishami)
			res = 0;
		else if (resource == ResourceProvision.EC2)
			res = 1;
		else if (resource == ResourceProvision.EC2v2)
			res = 2;
		else if (resource == ResourceProvision.Sample1)
			res = 3;
		else if (resource == ResourceProvision.EC2ArabNejad)
			res = 4;
		else if (resource == ResourceProvision.EC2020)
			res = 2020;
		else
			res = 2020;

		// Schedules a Single Workflow or AllWorkflows
		if (workflow != Workflow.ALL) {
			if (workflow == Workflow.Montage)
				wf = Utility.returnDaxMontage(taskSeries);
			else if (workflow == Workflow.Sipht)
				wf = Utility.returnDaxSipht(taskSeries);
			else if (workflow == Workflow.Epigenomics)
				wf = Utility.returnDaxEpigenomics(taskSeries);
			else if (workflow == Workflow.Cybershake)
				wf = Utility.returnDaxCyberShake(taskSeries);
			else if (workflow == Workflow.Inspiral)
				wf = Utility.returnDaxInspiral(taskSeries);

			//
			executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
			
			executionSenario.SenarioCostDeadlineRangeHamidArab(wf, bandwidth, res, interval, cStart, cEnd, cInc,
						dStart, dEnd, dInc);

		}
		// Schedules AllWorkflows

		else if (workflow == Workflow.ALL) {
			List<String> wfs = new ArrayList();
			wfs.add(Utility.returnDaxMontage(taskSeries));
			wfs.add(Utility.returnDaxSipht(taskSeries));
			wfs.add(Utility.returnDaxCyberShake(taskSeries));
			wfs.add(Utility.returnDaxEpigenomics(taskSeries));
			wfs.add(Utility.returnDaxInspiral(taskSeries));

			if (sb == SenarioBase.VahidArabnejad_BDAS) {
				//starts from 1-20
				for (String wf1 : wfs) {
					
					System.out.println("***********" + wf1);
					executionClass.scheduleWorkflowInitial(wf1, res, interval, bandwidth);
					executionSenario.SenarioCostDeadlineRangeByV_Arabnejad(wf1, bandwidth, res, interval, cStart, cEnd,
							cInc, dStart, dEnd, dInc);
				}
			} 
			else if (sb == SenarioBase.Sun_BDSD) {
				//starts from 0.1 to 2.0
				for (String wf1 : wfs) {
					System.out.println("***********" + wf1);
					executionClass.scheduleWorkflowInitial(wf1, res, interval, bandwidth);
					executionSenario.SenarioCostDeadlineRangeSun(wf1, bandwidth, res, interval, cStart, cEnd,
							cInc, dStart, dEnd, dInc);
				}
				
			}
			else if (sb == SenarioBase.HamidArabnejad) {
				for (String wf1 : wfs) {
					System.out.println("***********" + wf1);
					executionClass.scheduleWorkflowInitial(wf1, res, interval, bandwidth);
					executionSenario.SenarioCostDeadlineRangeHamidArab(wf1, bandwidth, res, interval, cStart, cEnd,
							cInc, dStart, dEnd, dInc);
				}
				
			}
			else if (sb == SenarioBase.Mine) {
				for (String wf1 : wfs) {
					
					System.err.println("***********" + wf1);
					executionClass.scheduleWorkflowInitial(wf1, res, interval, bandwidth);
					executionSenario.SenarioCostDeadlineRangeMine(wf1, bandwidth, res, interval, cStart, cEnd,
							cInc, dStart, dEnd, dInc);
				}
				
			}
			else if (sb == SenarioBase.PCP) {
				for (String wf1 : wfs) {
					System.out.println("***********" + wf1);
					executionClass.scheduleWorkflowInitial(wf1, res, interval, bandwidth);
					executionSenario.SenarioCostDeadlineRangePCP(wf1, bandwidth, res, interval, cStart, cEnd,
							cInc, dStart, dEnd, dInc);
				}
				
			}
		}
	}

	public static void Series1(int TaskSeries, int bandwidth, int res, int interval) {
		SeriesMontage(TaskSeries, bandwidth, res, interval);
		SeriesSipht(TaskSeries, bandwidth, res, interval);
		SeriesEpigenomics(TaskSeries, bandwidth, res, interval);
		SeriesCyberShake(TaskSeries, bandwidth, res, interval);
		SeriesInspiral(TaskSeries, bandwidth, res, interval);
		// utility.Utility.LogResults();
	}

	public static void SeriesBasedTightness(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SeriesMontage(TaskSeries, bandwidth, res, interval, Tightness);
		SeriesSipht(TaskSeries, bandwidth, res, interval, Tightness);
		SeriesEpigenomics(TaskSeries, bandwidth, res, interval, Tightness);
		SeriesCyberShake(TaskSeries, bandwidth, res, interval, Tightness);
		SeriesInspiral(TaskSeries, bandwidth, res, interval, Tightness);
		// utility.Utility.LogResults();
	}

	public static void SeriesX(float startX, float endX, float xInt, int TaskSeries, int bandwidth, int res,
			int interval) {
		SenarioExe(startX, endX, xInt, Utility.returnDaxMontage(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		SenarioExe(startX, endX, xInt, Utility.returnDaxSipht(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		SenarioExe(startX, endX, xInt, Utility.returnDaxEpigenomics(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		SenarioExe(startX, endX, xInt, Utility.returnDaxCyberShake(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		SenarioExe(startX, endX, xInt, Utility.returnDaxInspiral(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");

	}

	public static void SeriesMontage(int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxMontage(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesSipht(int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxSipht(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesEpigenomics(int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxEpigenomics(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesCyberShake(int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxCyberShake(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesInspiral(int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxInspiral(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesMontage(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SenarioExe(Utility.returnDaxMontage(TaskSeries), bandwidth, res, interval, Tightness);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesSipht(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SenarioExe(Utility.returnDaxSipht(TaskSeries), bandwidth, res, interval, Tightness);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesEpigenomics(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SenarioExe(Utility.returnDaxEpigenomics(TaskSeries), bandwidth, res, interval, Tightness);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesCyberShake(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SenarioExe(Utility.returnDaxCyberShake(TaskSeries), bandwidth, res, interval, Tightness);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesInspiral(int TaskSeries, int bandwidth, int res, int interval,
			executionSenario.TightRange Tightness) {
		SenarioExe(Utility.returnDaxInspiral(TaskSeries), bandwidth, res, interval, Tightness);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SenarioTight(String wf, int bandwidth, int res, int interval) {
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) 0.1; i <= 0.3; i += 0.1)
			for (float j = (float) 0.1; j <= 0.3; j += 0.1) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime + Math.round((float) j * (2 * executionClass.HeftTime)));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("Tight-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioCostDeadlineRange(String wf, int bandwidth, int res, int interval, float cStart,
			float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.FastestCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime + Math.round((float) j * (2 * executionClass.HeftTime)));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("Tight-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval,
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioCostDeadlineRangeHamidArab(String wf, int bandwidth, int res, int interval, float cStart,
			float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		System.out.println("***** Hamid Arabnejad DBCS Senario is Running");
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost);
				deadline = (executionClass.FastestCPTime)
						+ Math.round(j * ((float) executionClass.CheapestCpTime-executionClass.FastestCPTime));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("-----  cX" + i + " --dX:" + j + " ---<<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);

				System.out.println(
						"============================================================================================");
			}
	}
	public static void SenarioCostDeadlineRangeMine(String wf, int bandwidth, int res, int interval, float cStart,
			float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		System.out.println("***** Taghinezhad Senario is Running");
		int deadline = 0;
		float cost;
		float pcpcost;

		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {

				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost)/(float)1.8;
				deadline = (executionClass.FastestCPTime)
						+ Math.round(((float) j * (executionClass.CheapestCpTime - executionClass.FastestCPTime))/(float)2.2);
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				System.out.println("~~~~~ #"+strId);
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.err.println("-----  cX" + i + " --dX:" + j + " ---<<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
			//	executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
			//	executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}
	public static void SenarioCostDeadlineRangePCP(String wf, int bandwidth, int res, int interval, float cStart,
			float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		System.out.println("***** Taghinezhad Senario is Running");
		int deadline = 0;
		float cost;
		float pcpcost;
		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				
//				cost = executionClass.CheapestIndCost
//						+ (float) i * Math.abs(executionClass.FastestCost- executionClass.CheapestCost);
//				deadline = (executionClass.FastestCPTime)
//						+ Math.round(j * ((float) executionClass.CheapestCpTime-executionClass.FastestCPTime));
//				cost = executionClass.CheapestCost
//						+ (float) i * Math.abs((executionClass.HeftCost)- executionClass.CheapestCost);
			
				deadline = (executionClass.FastestCPTime)
						+ Math.round(j * ( 4*executionClass.FastestCPTime));
				pcpcost=executionClass.RunMyPCP(wf, deadline, false, false, false, false, res, interval, bandwidth);
				cost = (pcpcost /2)
						+ (float) i * (pcpcost /2);
//				deadline = (executionClass.FastestCPTime)
//						+ Math.round(j * (executionClass.CheapestCpTime-  executionClass.FastestCPTime));
								
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("-----  cX" + i + " --dX:" + j + " ---<<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				
			//	executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
//				 executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval, bandwidth);
//				 executionClass.RunFDAS3(wf, deadline, cost, false, res, interval, bandwidth);
//				 executionClass.RunFDAS4(wf, deadline, cost, false, res, interval, bandwidth);
//				 executionClass.RunFDASHef2(wf, deadline, cost, false, res, interval, bandwidth);
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}
	public static void SenarioCostDeadlineRangeVahidArabnejad_BDAS(String wf, int bandwidth, int res, int interval,
			float cStart, float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		System.out.println("***** Vahid Arabnejad BDAS Senario is Running");

		// Usually variance of i and j is between 1 to 20
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				cost = (float) executionClass.CheapestCost + i;
				deadline = Math.round((float) executionClass.FastestCPTime * (float) j);

				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("-----  cX" + i + " --dX:" + j + " ---<<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval,
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioCostDeadlineRangeSun(String wf, int bandwidth, int res, int interval, float cStart,
			float cEnd, float cInc, float dStart, float dEnd, float dInc) {
		System.out.println("***** Sun BDSD Senario is Running");
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.FastestCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime) + Math.round(j * ((float) 2 * executionClass.HeftTime));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println(wf+"  cX" + i + " --dX:" + j + " ---<<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval,
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioCostDeadlineRangeByV_Arabnejad(String wf, int bandwidth, int res, int interval,
			float cStart1, float cEnd1, float cInc1, float dStart1, float dEnd1, float dInc1) {
		int cStart=Math.round(cStart1) ; int cEnd=Math.round(cEnd1); int cInc=Math.round(cInc1); int dStart=Math.round(dStart1); int dEnd=Math.round(dEnd1); int dInc=Math.round(dInc1);
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;

		for (float i = (float) cStart; i <= cEnd; i += cInc)
			for (float j = (float) dStart; j <= dEnd; j += dInc) {
				cost = (float) executionClass.CheapestCost + i;
				deadline = Math.round((float) executionClass.FastestCPTime * (float) j);
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("Tight-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");

				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval,
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioTight2(String wf, int bandwidth, int res, int interval) {
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) 0.3; i <= 0.5; i += 0.1)
			for (float j = (float) 0.3; j <= 0.5; j += 0.1) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime + Math.round((float) j * (2 * executionClass.HeftTime)));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("Mediocre-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioTight3(String wf, int bandwidth, int res, int interval) {
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cost;
		// float i = (float) 0.1;
		for (float i = (float) 0.6; i <= 0.9; i += 0.1)
			for (float j = (float) 0.6; j <= 0.9; j += 0.1) {
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime + Math.round((float) j * (2 * executionClass.HeftTime)));
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("Relaxed-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:"
						+ deadline + ">>-------------------");
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				System.out.println(
						"============================================================================================");
			}
	}

	public static void SenarioExe(String wf, int bandwidth, int res, int interval) {
		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);
		SenarioTight(wf, bandwidth, res, interval);
		// SenarioTight2(wf, bandwidth, res, interval);
		// SenarioTight3(wf, bandwidth, res, interval);
	}

	public enum TightRange {
		Tight, Mediocre, Relax, TightBudget, TightDeadline, TightBudgetDeadline,
	}

	public static void SenarioExe(String wf, int bandwidth, int res, int interval, TightRange tr) {
		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);
		if (tr == TightRange.Tight)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.1, (float) 0.3, (float) 0.1, (float) 0.1,
					(float) 0.4, (float) 0.05);
		else if (tr == TightRange.Mediocre)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.3, (float) 0.55, (float) 0.1, (float) 0.3,
					(float) 0.55, (float) 0.1);
		else if (tr == TightRange.Relax)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.5, (float) 0.85, (float) 0.1, (float) 0.5,
					(float) 0.85, (float) 0.1);
		else if (tr == TightRange.TightBudget)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.1, (float) 0.3, (float) 0.1, (float) 0.3,
					(float) 0.6, (float) 0.1);
		else if (tr == TightRange.TightDeadline)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.3, (float) 0.6, (float) 0.1, (float) 0.1,
					(float) 0.35, (float) 0.1);
		else if (tr == TightRange.TightBudgetDeadline)
			SenarioCostDeadlineRange(wf, bandwidth, res, interval, (float) 0.1, (float) 0.3, (float) 0.1, (float) 0.1,
					(float) 0.3, (float) 0.1);
	}

	public static void SenarioExeOld(String wf, int bandwidth, int res, int interval) {
		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		// float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false,
		// false, false, res, interval,
		// bandwidth);

		float cost;

		for (float i = (float) 0.05; i <= 0.9; i += 0.1) {
			for (float j = (float) 0.05; j <= 0.9; j += 0.1) {
				// cost = cheepest * (float) i;
				// deadline = Math.round(HeftTime * (float) j);
				// cost = executionClass.CheapestCpCost
				// + (float) i * (executionClass.HeftCost - executionClass.CheapestCpCost);
				// deadline = (executionClass.HeftTime
				// + Math.round((float) j * (executionClass.CheapestCpTime -
				// executionClass.HeftTime)));

				// based on sun
				cost = executionClass.CheapestCost
						+ (float) i * (executionClass.HeftCost - executionClass.CheapestCost);
				deadline = (executionClass.HeftTime + Math.round((float) j * (2 * executionClass.HeftTime)));

				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, (float) j, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
						+ ">>-------------------");

				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDCtaghinezhad(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASHef2(wf, deadline, cost, false, res, interval,
				// bandwidth);
				// executionClass.RunBDASSHef(wf, deadline, cost, false, res, interval,
				// bandwidth);
				// executionClass.RunDBDASSHef2(wf, deadline, cost, false, res, interval,
				// bandwidth);
				// executionClass.RunFDASrealHeft(wf, deadline, cost, false, res, interval,
				// bandwidth);
				// executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false,
				// res, interval, bandwidth);
				// executionClass.RunMyPCP(wf, deadline, false, false, false, false, res,
				// interval, bandwidth);
				// executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunDBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunDBDAS2(wf, deadline, cost, false, res, interval,
				// bandwidth);
				// executionClass.RunFDAS(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDAS2(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDAS3(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDAS4(wf, deadline, cost, false, res, interval, bandwidth);
				// executionClass.RunFDASHef(wf, deadline, cost, false, res, interval,
				// bandwidth);
				System.out.println(
						"============================================================================================");
			}
		}
	}

	public static void SenarioExePro(String wf, int bandwidth, int res, int interval) {

		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		// float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false,
		// false, false, res, interval,
		// bandwidth);

		float cost;
		float cheepest = executionClass.CheapestCost;
		for (float i = (float) 1; i <= 2; i += 0.5) {
			for (float j = (float) 1; j <= 3; j += 0.5) {
				deadline = Math.round(HeftTime * (float) i);
				cost = cheepest * (float) j;
				String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
				ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) j, (float) i, wf);
				utility.Utility.dbs.add(rdb);
				System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
						+ ">>-------------------");
				// executionClass.RunMyPCP(wf, deadline, false, false, false, false, res,
				// interval, bandwidth);
				executionClass.RunBDDCtaghinezhad(wf, deadline, cost, null, res, interval, bandwidth);
				// executionClass.RunDBDAS(wf, deadline, cost, false, res, interval, bandwidth);

				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				// executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false,
				// res, interval, bandwidth);
				// executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval,
				// bandwidth);

				System.out.println(
						"============================================================================================");
			}
		}
	}

	public static void SenarioExe2(String wf, int bandwidth, int res, int interval) {

		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		// float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false,
		// false, false, res, interval,
		// bandwidth);

		float cost;
		float cheepest = executionClass.CheapestCost;

		for (float i = (float) 1; i < 4; i += 0.5) {
			deadline = Math.round(HeftTime * (float) i);
			cost = cheepest * (float) i;
			String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
			ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, wf);
			utility.Utility.dbs.add(rdb);
			System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
					+ ">>-------------------");
			// executionClass.RunMyPCP(wf, deadline, false, false, false, false, res,
			// interval, bandwidth);
			executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
			
			// executionClass.RunBDASSHef(wf, deadline, cost, false, res, interval,
			// bandwidth);
			// executionClass.RunDBDASSHef2(wf, deadline, cost, false, res, interval,
			// bandwidth);
			executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
		
			executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
			// executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false,
			// res, interval, bandwidth);
			// executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval,
			// bandwidth);

			System.out.println(
					"============================================================================================");
		}
	}

	public static void SenarioExe(float startX, float EndX, float xInt, String wf, int bandwidth, int res,
			int interval) {

		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false, false, false, res, interval,
				bandwidth);
		;
		float cost = cheepest;

		for (float i = startX; i <= EndX; i += xInt) {
			deadline = Math.round(HeftTime * (float) i);
			cost = cheepest * (float) i;
			String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
			ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, wf);
			utility.Utility.dbs.add(rdb);
			System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
					+ ">>-------------------");
			executionClass.RunMyPCP(wf, deadline, false, false, false, false, res, interval, bandwidth);
			executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);

			executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
			
			executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);

			// executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false,
			// res, interval, bandwidth);
			executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
			System.out.println(
					"============================================================================================");
		}

	}

	public static void SenarioAlgirthm(String wf, ScheduleType Algorithm, Boolean instancePrint, int bandwidth, int res,
			int interval) {
		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false, false, false, res, interval,
				bandwidth);
		float cost = cheepest;
		for (float i = (float) 1; i < 2.5; i += 0.5) {
			deadline = Math.round(HeftTime * (float) i);
			cost = cheepest * (float) i;
			String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
			ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, wf);
			utility.Utility.dbs.add(rdb);
			System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
					+ ">>-------------------");
			switch (Algorithm) {
			case BDHEFT:
				executionClass.RunBDHEFT(wf, deadline, cost, instancePrint, false, false, false, res, interval,
						bandwidth);
				break;
			case MYBDHEFT:
				executionClass.RunMyBDHEFT(wf, deadline, cost, instancePrint, false, false, false, res, interval,
						bandwidth);
				break;
			case BDAS:
				executionClass.RunBDAS(wf, deadline, cost, instancePrint, res, interval, bandwidth);
				break;
			
			case DBCS_HARAB:
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, instancePrint, res, interval, bandwidth);
				break;
			case BDSDson:
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				break;
			}
		}
	}

	public static void SenarioGrid(String wf, int bandwidth, int res, int interval) {

		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + " s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);
		int HeftTime = executionClass.HeftTime;
		int deadline = 0;

		float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false, false, false, res, interval,
				bandwidth);
		for (float i = (float) 1; i < 5; i = (float) (i + 0.5)) {
			deadline = Math.round(HeftTime * i);
			float cost = cheepest * i;
			System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
					+ ">>-------------------");
			executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
			executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
			executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
			System.out.println(
					"============================================================================================");
		}

	}

	public static void SenarioAlgirthm(float startX, float EndX, float xInt, String wf, ScheduleType Algorithm,
			int bandwidth, int res, int interval) {
		Log.print("bandwidth:" + bandwidth + " Bytes ,Interval:" + interval + "s");
		executionClass.scheduleWorkflowInitial(wf, res, interval, bandwidth);
		Utility.readConsideration = true;
		executionClass.RunHeft(wf, false, res, interval, bandwidth);

		int HeftTime = executionClass.HeftTime;
		int deadline = 0;
		float cheepest = executionClass.RunMyPCP(wf, HeftTime * 2, false, false, false, false, res, interval,
				bandwidth);
		float cost = cheepest;
		for (float i = startX; i <= EndX; i += xInt) {
			deadline = Math.round(HeftTime * (float) i);
			cost = cheepest * (float) i;
			String strId = deadline +"-"+ cost +"-"+ interval +"-"+ wf;
			ResultDB rdb = new ResultDB(strId, deadline, cost, interval, (int) bandwidth, (float) i, wf);
			utility.Utility.dbs.add(rdb);
			System.out.println("-----------  X" + i + " <<Constrained on Cost:" + cost + " Deadline:" + deadline
					+ ">>-------------------");
			switch (Algorithm) {
			case BDHEFT:
				executionClass.RunBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				break;
			case MYBDHEFT:
				executionClass.RunMyBDHEFT(wf, deadline, cost, false, false, false, false, res, interval, bandwidth);
				break;
			case BDAS:
				executionClass.RunBDAS(wf, deadline, cost, false, res, interval, bandwidth);
				break;
			case DBCS_HARAB:
				executionClass.RunDBCS_HARABNEJAD(wf, deadline, cost, false, res, interval, bandwidth);
				break;
			case BDSDson:
				executionClass.RunBDSDson(wf, deadline, cost, false, res, interval, bandwidth);
				break;
			}
		}
	}

	public static void SeriesMontage(float startX, float endX, int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxMontage(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesSipht(float startX, float endX, int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxSipht(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesEpigenomics(float startX, float endX, int TaskSeries, int bandwidth, int res,
			int interval) {
		SenarioExe(Utility.returnDaxEpigenomics(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesCyberShake(float startX, float endX, int TaskSeries, int bandwidth, int res,
			int interval) {
		SenarioExe(Utility.returnDaxCyberShake(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}

	public static void SeriesInspiral(float startX, float endX, int TaskSeries, int bandwidth, int res, int interval) {
		SenarioExe(Utility.returnDaxInspiral(TaskSeries), bandwidth, res, interval);
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// utility.Utility.LogResults();
	}
}
