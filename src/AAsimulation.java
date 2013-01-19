import java.io.*;
import java.util.*;
import lpsolve.*;


public class AAsimulation
{
	public static void main(String args[])throws Exception
	{
		Tool hd = new Tool();
		hd.createNw();
		hd.createArcs();
		hd.createLp();
		hd.calc_dead_mileage();
		
		
		/*
			 What is left to do ??
				1. Build the network - Done
					1.1. Define lower and upper bounds matrices - Done
				2. Apply LP 
					2.1 Convert adjacency to LP format - Arc flows....- Done
					2.2 Add Constraints - Solve
						2.2.1 Conservation constraint - Done
						2.2.2 Lower and Upper bound constraints - Done
						2.2.3 Objective Function - Done
				3. Evaluate performance measures - Revenue - Done
		 */
	}
	
}

class Tool
{
	static int si_time,local_time,step=1,count;
	static int BIG = 10000;
	static int oper_cost = 4, revenue_cost = 8, dist_time_factor=3;
	static String req_name,adj_name;
	static int[][] ADJMATRIX;
	static int[][] VIRTUALMATRIX;
	static int[][] LBMATRIX;
	static int[][] UBMATRIX;
	static int[][] REQMATRIX;
	static int[][] SPATH ;
	static int[][] SPATHD ;
	static double[][] ARCFLOWS;
	static double[] SOLNFLOWS; 
	static int numreq,numnodes,fl_req;
	
	
	public Tool() throws Exception
	{
		System.out.println("");
		System.out.println("");
		DataInputStream dis=new DataInputStream(System.in);
		System.out.println("Enter Request Data Filename :");
		req_name=dis.readLine();
		System.out.print("Enter Number of Requests in the request file: ");
		numreq=Integer.parseInt(dis.readLine());
		System.out.println("Adjacency Matrix Filename by default is finalmapint.txt");
		ADJMATRIX = new int[60][60];
		adj_name="finalmapint.txt";
		ADJMATRIX = readMatrix(adj_name);
		numnodes=ADJMATRIX.length;
		SPATH = new int[numnodes][numnodes];
		floydWarshall(numnodes,SPATH,ADJMATRIX);
		SPATHD = new int[numnodes][numnodes];
		floydWarshall(numnodes,SPATHD,ADJMATRIX);
		timeconverter();
		//req_name="req.txt";
		REQMATRIX = readMatrix(req_name);
		VIRTUALMATRIX = new int[2*numreq+2][2*numreq+2];
		LBMATRIX = new int[2*numreq+2][2*numreq+2];
		UBMATRIX = new int[2*numreq+2][2*numreq+2];
	}

	public static void timeconverter()
	{
		for(int i=0;i<numnodes;i++)
		{
			for(int j=0;j<numnodes;j++)
			{
				SPATH[i][j]=SPATHD[i][j]*dist_time_factor;
			}	
		}
			
	}
	
	
	// Creating the Virtual network

	public static void createNw()
	{
		System.out.println();
		System.out.println("Creating Vitual Network ...");
		System.out.println("---------------------------");
	
		
		for(int i=0;i<2*numreq+2;i++)
		{
			for(int j=0;j<2*numreq+2;j++)
			{
				VIRTUALMATRIX[i][j]=-1;
				LBMATRIX[i][j]=0;
				UBMATRIX[i][j]=BIG;
			}
		}
		
		VIRTUALMATRIX[0][0]=-1;
		VIRTUALMATRIX[0][1]=-1;
		VIRTUALMATRIX[1][0]=0;
		VIRTUALMATRIX[1][1]=-1;
		
		
		for(int j=0;j<numreq;j++)
		{
			int i=j+1;
			VIRTUALMATRIX[0][2*i]=SPATH[0][REQMATRIX[j][1]];
			VIRTUALMATRIX[2*i+1][1]=SPATH[REQMATRIX[j][2]][0];
			VIRTUALMATRIX[2*i][2*i]=-1;
			VIRTUALMATRIX[2*i+1][2*i+1]=-1;
			VIRTUALMATRIX[2*i][2*i+1]=SPATH[REQMATRIX[j][1]][REQMATRIX[j][2]];
			LBMATRIX[2*i][2*i]=0;
			LBMATRIX[2*i+1][2*i+1]=0;
			LBMATRIX[2*i][2*i+1]=1;
			
			UBMATRIX[2*i][2*i]=BIG;
			UBMATRIX[2*i+1][2*i+1]=BIG;
			UBMATRIX[2*i][2*i+1]=1;
		}

// Connection Maker
 		
		for(int i=0;i<numreq;i++)
		{
			for(int j=0;j<numreq;j++)
			{
				if(REQMATRIX[i][3]+SPATH[REQMATRIX[i][1]][REQMATRIX[i][2]]+SPATH[REQMATRIX[i][2]][REQMATRIX[j][1]]<=REQMATRIX[j][3])
				{
					
					//System.out.println("Make Connection ");
					//System.out.println("i - "+i+"j - "+j);
					//System.out.println("REQMATRIX[j][3]"+REQMATRIX[j][3]);
					VIRTUALMATRIX[2*(i+1)+1][2*(j+1)]=SPATH[REQMATRIX[i][2]][REQMATRIX[j][1]];;
					
				}
			}
		}
		
		/*for(int i=0;i<2*numreq+2;i++)
		{
			System.out.println();
			for(int j=0;j<2*numreq+2;j++)
			{
				//System.out.print("Vitual matrix " + i + " " + j+" --- ");
				System.out.print(VIRTUALMATRIX[i][j]);
				System.out.print("	");
			}
		}
		System.out.println("");
		System.out.println("");
		System.out.println("Lower Bound Matrix");
		System.out.println("------------------");
		
		for(int i=0;i<2*numreq+2;i++)
		{
			System.out.println();
			for(int j=0;j<2*numreq+2;j++)
			{
				//System.out.print("Vitual matrix " + i + " " + j+" --- ");
				System.out.print(LBMATRIX[i][j]);
				System.out.print("	");
			}
		}
		
		System.out.println("");
		System.out.println("");
		System.out.println("Upper Bound Matrix");
		System.out.println("------------------");
		
		for(int i=0;i<2*numreq+2;i++)
		{
			System.out.println();
			for(int j=0;j<2*numreq+2;j++)
			{
				//System.out.print("Vitual matrix " + i + " " + j+" --- ");
				System.out.print(UBMATRIX[i][j]);
				System.out.print("	");
			}
		}
		System.out.println("");*/
	}
	
// Creating arcs flows...Lp Format

	public static void createArcs() throws Exception
	{
		
		for(int i=0;i<2*numreq+2;i++)
		{
			for(int j=0;j<2*numreq+2;j++)
			{
				if (VIRTUALMATRIX[i][j]>=0)
				{
					count= count+1;
				}
			}
		}    
		
		System.out.println("Number of arc flows: "+ count);
		System.out.println("");
		
		ARCFLOWS = new double [count][6];
		SOLNFLOWS = new double[count];
		int temp_count=0;
		for(int i=0;i<2*numreq+2;i++)
		{
			for(int j=0;j<2*numreq+2;j++)
			{
				if (VIRTUALMATRIX[i][j]>=0)
				{
					ARCFLOWS[temp_count][0]=temp_count;
					ARCFLOWS[temp_count][1]=i	;
					ARCFLOWS[temp_count][2]=j	;
					ARCFLOWS[temp_count][3]=LBMATRIX[i][j]	;
					ARCFLOWS[temp_count][4]=UBMATRIX[i][j]	;
					ARCFLOWS[temp_count][5]=(VIRTUALMATRIX[i][j]/3)	;
					temp_count= temp_count+1;
				}
			}
		}
		
		/*System.out.println("Arc Flows");
		System.out.println("---------");
		for(int i=0;i<count;i++)
		{
			System.out.println();
			
			for(int j=0;j<=5;j++)
			{
				System.out.print(ARCFLOWS[i][j]);
				System.out.print("	");
			}
		}
		System.out.println("");
		System.out.println("");	*/
	}

// Creating the Linear Programming Model
		
	public static void createLp() throws Exception
	{
		
	
		 LpSolve problem = LpSolve.makeLp(0,count);
		 double[] temp_arc_cost = new double[count+1];
		 temp_arc_cost[0]=0;
		 for (int i=1;i<=count;i++)
		 {
			if (ARCFLOWS[i-1][3]==1 && ARCFLOWS[i-1][4]==1)
			{
		 		temp_arc_cost[i]=(ARCFLOWS[i-1][5])*revenue_cost;
		 		//System.out.println("arc costs "+temp_arc_cost[i] );
	 		}
	 		else  
			{
		 		temp_arc_cost[i]=(-1*ARCFLOWS[i-1][5])*oper_cost;
		 		//System.out.println("arc costs "+temp_arc_cost[i] );
	 		}
		 }
		 
		 
		 for (int i=0;i<count;i++)
		 {
			double[] temp = new double [count+1]; 
			for (int j=0;j<=count; j++) 
			{
				temp[j]=0;	
			}
				temp[i+1]=1;
		  	problem.addConstraint(temp, LpSolve.GE, ARCFLOWS[i][3]);	    //Lower Bound constraints
		  	problem.addConstraint(temp ,LpSolve.LE, ARCFLOWS[i][4]);	    //Upper Bound constraints
		}	
		  	//Conservation constraint
		  	int numnodes_virtual = VIRTUALMATRIX.length;
		  	double[] temp_conser = new double [count+1]; 
		  	for (int i=1 ; i<numnodes_virtual; i++)
		  	{	
			  	for (int j=0;j<=count; j++) 
				{
					temp_conser[j]=0;	
				}
				for (int k=0 ; k<count; k++)
		  		{
					if( ARCFLOWS [k][1]== i)
					{
						temp_conser[(int)(ARCFLOWS [k][0])+1] = 1;	
					}
					if( ARCFLOWS [k][2]== i)
					{
						temp_conser[(int)(ARCFLOWS [k][0])+1] = -1;	
					}
				}			  	
			  	problem.addConstraint(temp_conser ,LpSolve.EQ, 0);
			}
		 
		 double[] obj_temp = new double [count+1];
		 problem.setMaxim();
		 problem.setObjFn(temp_arc_cost);
		 problem.solve();
		 //problem.setDebug(true);
		// problem.printLp();
		//problem.setOutputfile("log.txt");
		//problem.printSolution(2);
		
		//problem.setOutputfile(NULL);
		 System.out.println();
		 System.out.println();
		 
		 System.out.println("Value of objective function: " + problem.getObjective());
		 SOLNFLOWS = problem.getPtrVariables();
		 double max_flow=-1000;
		 for(int p=0;p<SOLNFLOWS.length;p++)
		 {
			 
			 if(SOLNFLOWS[p]>max_flow)
			 {max_flow=SOLNFLOWS[p];}
		 		
	 	 }
	 	 	fl_req=(int)max_flow;
		 	System.out.println("Optimal fleet size required is: "+fl_req);
		 
		 //problem.deleteLp();
		// calc_dead_mileage();
		
	}
	
	
	
// Reads the matrices from the test files into the arrays...
	public static int[][] readMatrix(String filename)
	{
		String s;
		try{
			File f = new File(filename);
			FileInputStream fs = new FileInputStream(f);
			int i , c;
			StringBuffer sb = new StringBuffer();
			while((c = fs.read()) != -1) sb.append((char) c);
			s = sb.toString();
			}
		catch(IOException fne){
			System.err.println("File " + filename + " does not exist. Exiting....");
			return null;
			}

			StringTokenizer st = new StringTokenizer(s , "\n");
			int[][] matrix = new int[st.countTokens()][];

			for(int i = 0 ; st.hasMoreTokens() ; i++)
			{
				StringTokenizer st1 = new StringTokenizer(st.nextToken());
				matrix[i] = new int[st1.countTokens()];
				for(int j = 0 ; st1.hasMoreTokens() ; j++)
				matrix[i][j] = Integer.parseInt(st1.nextToken());
			}
			return matrix;
	}

public static void floydWarshall( int n, int[][] d, int[][] w )throws Exception
	{
//      Initialization
		for (int i=0; i<n; i++)
		{
			for (int j=0; j<n; j++)
			{
				d[i][j] = w[i][j];
			}
		}
		
		for (int i=0; i<n; i++)
			d[i][i] = 0;
		
//	    Now for the Actual Algoritm
		for (int k=0;k<n;k++) /* k -> is the intermediate point */
		{
			for (int i=0;i<n;i++) /* start from i */
			{
				for (int j=0;j<n;j++) /* reaching j */
				{	/* if i-->k + k-->j is smaller than the original i-->j */
					if( d[i][k] > 0 && d[k][j] > 0 && ( d[i][k] + d[k][j] < d[i][j] || d[i][j] < 0 ))
					{
						/* then reduce i-->j distance to the smaller one i->k->j */
						d[i][j] = d[i][k]+d[k][j];
					}
				}
			}
		}
	}
	
	public static void calc_dead_mileage()
	{
		double dead_mileage=0;int d_m_round;
		for(int i=0; i<SOLNFLOWS.length;i++)	
		{
			if (SOLNFLOWS[i]==1 && ARCFLOWS[i][3]!=1 && ARCFLOWS[i][4]!=1)	
			{
				dead_mileage=dead_mileage+ ARCFLOWS[i][5];
				//System.out.println("Dead mileage per trip is: " + dead_mileage);		
			}
		}
		dead_mileage=dead_mileage/numreq;
		d_m_round=(int)dead_mileage;
		System.out.println("Dead mileage per trip is: " + d_m_round);
	}
	
	
}
	
