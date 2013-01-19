import java.io.*;
import java.util.*;


public class finalsimdesi
{
	public static void main(String args[])throws Exception
	{
		Tool hd = new Tool();
		hd.Toool();
		hd.simulate();
		hd.printresults();
		Performance p = new Performance();


		/* What is left to do ??
		   1. Call feasibility function - Time window concept - giv the option to the user to delay his request.....
		   2. Fleet size constraint -Done... but the req wich are not served are left alone
		   3. Add heuristic 2
		   4. Input matrix generation (Input call characterization)
		   5. Request generator
	   */
	}

}

class Tool
{
	static int si_time,local_time,step=1,num_rejected_fleet;
	static int dist_time_factor=3;
	static String req_name,adj_name,des_name;
	static int[][] ADJMATRIX;
	static int[][] TRAVELTIMEMATRIX;
	static int[][] REQMATRIX;
	static int[][] CARMATRIX;
	static int[][] SPATH ;
	static int[][] SPATHD ;
	static float[][] DESIRABILTY;
	static int[][] car_sel_req;
	static ArrayList unatt_req = new ArrayList();
	static ArrayList inserv_req = new ArrayList();
	static ArrayList notserv_req = new ArrayList();
	static ArrayList move_list = new ArrayList();

	static int fl_size,numnodes,numreq,req_no,req_origin,req_dest,req_pt,req_ct,reqtmp;
	static int service_compl_time=10000,service_compl_time_temp,service_compl_index,temp_reqno;

	public void Toool() throws Exception
	{
		System.out.println("");
		System.out.println("");
		DataInputStream dis=new DataInputStream(System.in);
		System.out.print("Enter Fleetsize (Number of cars): ");
		fl_size=Integer.parseInt(dis.readLine());
		System.out.print("Enter Simulation time in minutes: ");
		si_time=Integer.parseInt(dis.readLine());
		System.out.println("Enter the Request Data Filename ");
		req_name=dis.readLine();
		System.out.print("Enter Number of Requests in the request file: ");
		numreq=Integer.parseInt(dis.readLine());
		System.out.println("Enter the Desirablility Data Filename ");
		des_name=dis.readLine();
		//System.out.println("Adjacency Matrix Filename by default is adj.txt");
		initialize();

	}

	public static void initialize() throws Exception
	{
		ADJMATRIX = new int[60][60];
		TRAVELTIMEMATRIX = new int[60][60];
		SPATH = new int[60][60];
		SPATHD = new int[60][60];
		DESIRABILTY = new float[60][60];
		adj_name="finalmapint.txt";
		ADJMATRIX = readMatrix(adj_name);
		numnodes=ADJMATRIX.length;
		//req_name="Output.txt";
		REQMATRIX = readMatrix(req_name);
		DESIRABILTY = readMatrixFloat(des_name);
		CARMATRIX = new int[fl_size][3];


//		Setting Current Car Index, Current Node position, Current Car Status; 0==Free & 1==Busy
		for( int i=0; i<fl_size; i++ )
		{
				CARMATRIX[i][0]=i;
				CARMATRIX[i][1]=0;
				CARMATRIX[i][2]=0;
		}

//		Initialising cars to the requests

		car_sel_req = new int[numreq][12];

		for( int i=0; i<numreq; i++ )
		{
			car_sel_req[i][0]=i;
			car_sel_req[i][1]=10000;
			car_sel_req[i][2]=0;
			car_sel_req[i][3]=0;
			car_sel_req[i][4]=0;
			car_sel_req[i][5]=0;
			car_sel_req[i][6]=0;
			car_sel_req[i][7]=REQMATRIX[i][1];
			car_sel_req[i][8]=REQMATRIX[i][2];
			car_sel_req[i][9]=0;
			car_sel_req[i][10]=REQMATRIX[i][2]; // Move node
			//car_sel_req[i][11]=0; // Move completition time

		}

// 		Initialising the Unattended & the Inservice Request arrrays


		for( int i=0; i<numreq; i++ )
		{
			unatt_req.add(i,new Integer(i+1));
		}

		/*for( int i=0; i<unatt_req.size(); i++ )
		{
			System.out.println("Element: "+ i+ "="+ unatt_req.get(i));
		}*/

		for( int i=0; i<ADJMATRIX.length; i++ )
		{
			for( int j=0; j<ADJMATRIX.length; j++ )
			{
				TRAVELTIMEMATRIX[i][j]=ADJMATRIX[i][j]*dist_time_factor;
			}
		}
	}


// The Simulation Model

	public static void simulate()throws Exception
	{
		System.out.println("Simulate");
		floydWarshall(numnodes,SPATH,TRAVELTIMEMATRIX);
		floydWarshall(numnodes,SPATHD,ADJMATRIX);
		for(local_time=0;local_time<=si_time;local_time=local_time+step)
		{
			System.out.println("Local Time:" + local_time);
			System.out.println("--------------");

// Car Position and status print

	/*	for (int i=0;i<fl_size;i++)
		{
			System.out.print("Car index:" +CARMATRIX[i][0]+"	" );
			System.out.print("Car" + CARMATRIX[i][0] + "@ Node:" + CARMATRIX[i][1]+"	" );
			if(CARMATRIX[i][2]==0)
			{
				System.out.println("Car Status:" + "Idle" );
			}
			else
			{
				System.out.println("Car Status:" + "Busy" );
			}
		} */
		System.out.println("");

// Request Allocation Check

			if(unatt_req.isEmpty())
			{
				System.out.println("All the requests have been attended to at this time... ");
				System.out.println("");
			}
			else{
			if(inserv_req.size()<fl_size && local_time>=(REQMATRIX[((Integer)unatt_req.get(0)).intValue()-1][3]-45))
			{
			/*	       1.Get the request details of the one which is goin to be served
					   2.Move Un-Attended to Inservice - perform push operation; then pop out the UnAtt
					   3.Check the nearest available free taxi from CARMATRIX
					   4.Set THIS PARTICULAR car status as busy==1
					   5.update car_sel_req 	*/

// Getting the request details of the request which is goin to be serverd.

				getrequest(((Integer)unatt_req.get(0)).intValue());
// Doing 2.

				inserv_req.add(inserv_req.size(),unatt_req.get(0));
				inserv_req.trimToSize();
				unatt_req.remove(0);


// Doing 3.
				int temp=0,tempindex=0,min=100000,car_selected=0;
				for(int i=0; i<fl_size; i++)
				{
					if(CARMATRIX[i][2]==0)
					{
						temp=SPATH[CARMATRIX[i][1]][req_origin];
						if(temp<min)
						{
							min=temp;
							car_selected=CARMATRIX[i][0];
							tempindex=i;
						}
					}
				}

// Doing 4,5.
				//System.out.println("Carselected: " + car_selected);
				CARMATRIX[tempindex][2]=1;
//				System.out.println("Request num: "+ req_no);
				car_sel_req[req_no-1][1]=car_selected;
				car_sel_req[req_no-1][2]=local_time;
				car_sel_req[req_no-1][3]=car_sel_req[req_no-1][2]+SPATH[CARMATRIX[tempindex][1]][REQMATRIX[req_no-1][1]];
				car_sel_req[req_no-1][4]=REQMATRIX[req_no-1][3];
				car_sel_req[req_no-1][6]=CARMATRIX[tempindex][1];
				System.out.println("There is a request served at this time: ");
				System.out.print("Carselected: " + car_selected+"	");
				System.out.print("To serve the request: " + req_no);
				System.out.println("");
			}

			else if(inserv_req.size()>=fl_size && local_time>=(REQMATRIX[((Integer)unatt_req.get(0)).intValue()-1][3]-45))
			{
				notserv_req.add(notserv_req.size(),unatt_req.get(0));
				notserv_req.trimToSize();
				unatt_req.remove(0);

				System.out.println("All cars are busy ...your request cannot be satisfied currently");
				num_rejected_fleet+=1;
				System.out.println("");
			}
		}


// Node change @ Pick up

		if(inserv_req.isEmpty())
			{

			}
	else{
			for (int i=0;i<inserv_req.size();i++)
			{
				if(local_time>=car_sel_req[i][3])
				{
					CARMATRIX[car_sel_req[i][1]][1]=car_sel_req[i][7];
				}
			}

		}



//Request Completion Check
			service_compl_time=10000;
			int inserv_token=0;

			if(inserv_req.isEmpty())
			{
				System.out.println("All requests have been successfully completed");
				System.out.println("");
			}
	else{
			for (int i=0;i<inserv_req.size();i++)
			{
					temp_reqno=((Integer)inserv_req.get(i)).intValue();
					getrequest(temp_reqno);
					service_compl_time_temp=REQMATRIX[temp_reqno-1][3]+SPATH[req_origin][req_dest];
				//	System.out.println("service completition time temp: "+service_compl_time_temp);
					if(service_compl_time_temp<service_compl_time)
					{
						service_compl_time=service_compl_time_temp;
						service_compl_index=temp_reqno;
						inserv_token=i;

					}
			}
		}
			//System.out.println("service completition time: "+service_compl_time);
			//System.out.println("service completition request index: "+service_compl_index);
			if(local_time>service_compl_time)
			{
				/*
				   1.Update CARMATRIX for THIS PARTICULAR CAR to idle==0
				   2.Car node change in CARMATRIX
				   3.Perform push operation on Inservice array if not empty
				*/
				System.out.println("Request no:: " +service_compl_index + " :: has being successfully completed at this time.....");
				System.out.println("");

// Doing 1 & 2
				int car_sel_index;
				if(service_compl_index>0)
				{
			//	System.out.println("Service completiton index @ crucial "+service_compl_index);
					car_sel_index=car_sel_req[service_compl_index-1][1];
			//	System.out.println("Car sel index "+car_sel_index);
					CARMATRIX[car_sel_index][2]=0;
					CARMATRIX[car_sel_index][1]=REQMATRIX[service_compl_index-1][2];
					car_sel_req[service_compl_index-1][5]=car_sel_req[service_compl_index-1][4]+SPATH[car_sel_req[service_compl_index-1][7]][car_sel_req[service_compl_index-1][8]];
					//service_compl_index=0;
				}
// Doing 3
				//System.out.println("service completition");
				if(inserv_req.size()>0)
				{
					inserv_req.remove(inserv_token);
				}
				car_sel_req[service_compl_index-1][10]=car_sel_req[service_compl_index-1][8];

				movedesirable();

				service_compl_index=0;
			}

// Move Completition check
			if(move_list.isEmpty())
			{
				System.out.println("No moves in the list");
				System.out.println("");
			}
	else{

			int mov_compl_index=((Integer)move_list.get(0)).intValue(), mov_compl_time=10000, tmp, token_array=0;
			for(int i=0;i<move_list.size();i++)
			{
				tmp=car_sel_req[((Integer)move_list.get(i)).intValue()][11];
				if (mov_compl_time>tmp)
				{
					mov_compl_time = tmp	;
					mov_compl_index = ((Integer)move_list.get(i)).intValue();
					token_array=i;
					//System.out.println("mov_compl_index : "+ ((Integer)move_list.get(i)).intValue());
				}
			}

				/*
				   1.Update CARMATRIX for THIS PARTICULAR CAR to idle==0
				   2.Car node change in CARMATRIX
				   3.Perform push operation on Move_list array if not empty
				*/

				if (local_time>=mov_compl_time)
				{
					CARMATRIX[car_sel_req[mov_compl_index][1]][2]=0;
					CARMATRIX[car_sel_req[mov_compl_index][1]][1]=car_sel_req[mov_compl_index][10];
					move_list.remove(token_array);
					move_list.trimToSize();
				}

		}

	}
	}

// Moving to the desirable node

	public static void movedesirable()
	{
		double desirability=0,temp_desirability=0; int movenode=car_sel_req[service_compl_index-1][8];
		car_sel_req[service_compl_index-1][10]=car_sel_req[service_compl_index-1][8];
		for(int i=0; i<numnodes ;i++)
		{
			//System.out.println("SCI:" + service_compl_index);
			//System.out.println("DeM:" + DESIRABILTY[0][0]);
			if((DESIRABILTY[car_sel_req[service_compl_index-1][8]][i]-DESIRABILTY[car_sel_req[service_compl_index-1][8]][car_sel_req[service_compl_index-1][8]])>0.25)
			{
				temp_desirability=DESIRABILTY[car_sel_req[service_compl_index-1][8]][i];
				if(temp_desirability>desirability)
				{
					desirability=temp_desirability;
					movenode=i;
				}
			}
		}

		System.out.println("desirablity: "+ desirability);
		System.out.println("Node to be moved to: "+ movenode);
		if(movenode!=car_sel_req[service_compl_index-1][8])
		{
			/*1.Make the car status busy
			2.Update the move node and the 	move completition time
			3.add - Array list the moves
			4.Call another function wich verifies with the local time and completes the move
			  from this list */

			  CARMATRIX[car_sel_req[service_compl_index-1][1]][2]=1;
			  car_sel_req[service_compl_index-1][10]=movenode;
			  car_sel_req[service_compl_index-1][11]= local_time+SPATH[car_sel_req[service_compl_index-1][8]][car_sel_req[service_compl_index-1][10]];
			  move_list.add(move_list.size(),new Integer (car_sel_req[service_compl_index-1][0]));
			  //move_list.trimToSize();
			  System.out.println("Move added.....");
			  //System.out.println("req no " + car_sel_req[service_compl_index-1][0]);
			  //System.out.println("Move Completition time : " + car_sel_req[service_compl_index-1][11]);
		}

		else if (movenode==car_sel_req[service_compl_index-1][8])
		{
			/*1.Update the move node and move completiton time	*/
			CARMATRIX[car_sel_req[service_compl_index-1][1]][2]=0;
			car_sel_req[service_compl_index-1][10]=movenode;
			car_sel_req[service_compl_index-1][11]= car_sel_req[service_compl_index-1][5];
			System.out.println("Service completition time: "+car_sel_req[service_compl_index-1][11]);
			System.out.println("Move retained.....");
		}
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

// Reads the FLOAT matrices from the test files into the arrays...

	public static float[][] readMatrixFloat(String filename)
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
			float[][] matrix = new float[st.countTokens()][];

			for(int i = 0 ; st.hasMoreTokens() ; i++)
			{
				StringTokenizer st1 = new StringTokenizer(st.nextToken());
				matrix[i] = new float[st1.countTokens()];
				for(int j = 0 ; st1.hasMoreTokens() ; j++)
				matrix[i][j] = Float.parseFloat(st1.nextToken());
			}
			return matrix;
	}

/*	The Floyd warshall algorithm to find All-Pairs Shortest path
	d contains the final shortest distances and w contains the edge weights
*/

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

// Gettting the Request Details
	public static void getrequest(int reqtmp)
	{
				req_no=REQMATRIX[reqtmp-1][0];
				req_origin=REQMATRIX[reqtmp-1][1];
				req_dest=REQMATRIX[reqtmp-1][2];
				req_pt=REQMATRIX[reqtmp-1][3];
				req_ct=REQMATRIX[reqtmp-1][4];
	}

	public static void printresults()
	{
		int  rev_tot=0,rev_cost=12,nonrev_cost=4,tot_dead_mileage=0;
		int tot_nonrev_path=0,tot_car_wait_time=0,div_factor=0;
		System.out.println("Printing how the request have been handled.......");
		System.out.println("-------------------------------------------------");
		System.out.print("");
		for(int i=0; i<numreq;i++)
		{
			int rev_req=0,rev_path=0,nonrev_path=0,dead_mileage=0;


			System.out.print("Request Number: ");
			System.out.println(car_sel_req[i][0]+1);
			if (car_sel_req[i][1]!=10000)
			{
				div_factor=div_factor+1;
				System.out.print("Car Selected:	");
				System.out.println(car_sel_req[i][1]);

				System.out.print("Origin: ");
				System.out.println(car_sel_req[i][7]);
				System.out.print("Destination: ");
				System.out.println(car_sel_req[i][8]);
				System.out.print("Start Node:	");
				System.out.println(car_sel_req[i][6]);


				System.out.print("Start Time: ");
				System.out.println(car_sel_req[i][2]);
				System.out.print("Pick up point reach time: ");
				System.out.println(car_sel_req[i][3]);
				System.out.print("Actual pick up time: ");
				System.out.println(car_sel_req[i][4]);
				System.out.print("Driver waiting time: ");
				System.out.println(car_sel_req[i][4]-car_sel_req[i][3]);
				tot_car_wait_time=tot_car_wait_time +car_sel_req[i][4]-car_sel_req[i][3];
				System.out.print("Drop off time: ");



				car_sel_req[i][5]=car_sel_req[i][4]+SPATH[car_sel_req[i][7]][car_sel_req[i][8]];
				if(car_sel_req[i][11]!=0)
				{
					System.out.println(car_sel_req[i][5]);

					
					nonrev_path=SPATHD[car_sel_req[i][6]][car_sel_req[i][7]]+ SPATHD[car_sel_req[i][7]][car_sel_req[i][8]]+SPATHD[car_sel_req[i][8]][car_sel_req[i][10]];
					tot_nonrev_path+=nonrev_path;
					System.out.println("Non rev path..: "+ tot_nonrev_path);
					dead_mileage=SPATHD[car_sel_req[i][6]][car_sel_req[i][7]];
					tot_dead_mileage+=dead_mileage;
					rev_path=SPATHD[car_sel_req[i][7]][car_sel_req[i][8]];
					System.out.println("Rev path..: "+ rev_path);

					/*System.out.print(dead_mileage);
					System.out.print("Revenue path..: ");
					System.out.print(rev_path);*/

					rev_req=(rev_path*rev_cost)-(nonrev_path*nonrev_cost);
					System.out.println("Non Rev cost..: "+ nonrev_path*nonrev_cost);
					System.out.println("Rev cost..: "+ rev_path*rev_cost);
					
					car_sel_req[i][9]=rev_req;
					System.out.print("REVENUE GENERATED IN THIS REQUEST is Rs..: ");
					System.out.println(car_sel_req[i][9]);
					rev_tot=rev_tot+rev_req;//car_sel_req[i][9];
					System.out.println("Dead_mileage in this request : "+dead_mileage);
					System.out.print("Desirable Node: ");
					System.out.println(car_sel_req[i][10]);
					System.out.print("Move completition time: ");
					System.out.println(car_sel_req[i][11]);
				}
				else
				{
					System.out.println(car_sel_req[i][5]);
					System.out.println("Request still in progress..Move to be completed...");
				}
				System.out.println();

			}

		    else if(car_sel_req[i][1]==10000)
			{
				System.out.println("Request still pending.....");
				System.out.println("");
			}
		}
				System.out.print("TOTAL REVENUE GENERATED till now is :	");
				System.out.println(rev_tot);
				System.out.print("AVERAGE CAR WAITING TIME till now is : ");
				System.out.println(tot_car_wait_time/div_factor);
				System.out.print("AVERAGE DEAD MILEAGE till now is : ");
				System.out.println(tot_dead_mileage/div_factor);
				System.out.println("");
				/*for(int i=0;i<numreq;i++)
				{
					System.out.println("Desi Node: "+car_sel_req[i][10]);
				}*/
	}

}

class Performance extends Tool
{
	static int num_rejected_callthreshold;
	//static int threshold=45;

	public Performance() throws Exception
	{
		//rejected_callgap();
		rejected_fleet();
		late_serviced();
	}

	public static void rejected_callgap()
	{
		//System.out.println("Performance");
		for(int threshold=45; threshold>=15;threshold=threshold-5)
		{
			for(int i=0;i<REQMATRIX.length;i++)
			{
				if((REQMATRIX[i][3]-REQMATRIX[i][4])<threshold)
				{
					num_rejected_callthreshold+=1;
				}
			}

			System.out.print("Number of Calls Rejected with threshold "+threshold+" is : "+num_rejected_callthreshold);
			System.out.println("  % of Calls Rejected  is : "+((num_rejected_callthreshold*100)/REQMATRIX.length));
			num_rejected_callthreshold=0;
		}
	}

	public static void rejected_fleet()
	{
		System.out.println("Number of Calls Rejected due to fleet constraint with fleet size "+fl_size+" is : "+num_rejected_fleet);
     }
	
    public static void late_serviced()
	{
		int late_count=0;
		for(int i=0; i<numreq;i++)
		{
			if((car_sel_req[i][4]-car_sel_req[i][3])<0)
			{
				late_count+=1;	
			}
		}
		System.out.println("Number of Calls late serviced "+fl_size+" is : "+late_count);		
     }
}