import java.util.regex.*;
import java.io.*;
import java.util.*;

public class Refiner
{
	
	static int[][] triptable= new int[60][60];
	static int timeInt,temp;
	static int[][] output ; 
	static int[][] ADJMATRIX = new int[60][60];
	static int[][] SPATHD = new int[60][60] ;
	static int num_requests;
	static float[][] desirabilty= new float[60][60];
	
	public static void main(String args[])
	{
		String fileName;
		LinkedList input = new LinkedList();
		LinkedList index = new LinkedList();
		DataInputStream dis= new DataInputStream(System.in);
		
		try
		{
			
			
			// Get the file name from the user..
			System.out.println("Enter the Raw Data File");
			fileName = dis.readLine();
	//		System.out.println("Enter the Time slot (2,4,6)");
	//		timeInt = Integer.parseInt(dis.readLine());
			System.out.println("Enter the number of requests in the Raw Data File");
			num_requests = Integer.parseInt(dis.readLine());
			output = new int[num_requests][5];
	//		ADJMATRIX = readMatrix("finalmapint.txt");
			System.out.println("Processing......");
	//		createFiles();
			readFromFile(fileName,input);
			readFromIndex("index1.txt",index);
			processInput(input, index);
			//printInput(input);
			//printIndex(index);
	//		findDesirabilty();
	//		printToFile("Triptable.txt",triptable);
	//		printToFileFloat("Desirabilty2.txt", desirabilty);
			printOutput("Jan3-ID.txt", output);
	//		System.out.println("The trip table is generated : Triptable.txt");
			System.out.println("The new processed input is generated : RefineData.txt");
			//System.out.println("The Desirablity Matrix is generated : DesirablityMatrix.txt");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void createFiles() throws IOException
	{
		
		int num = 24/timeInt;
		int[][] slotTable = new int[num][num+1];
		
		for(int i = 0; i < 60; i++)
		{
			for(int j = 0; j < 60; j++)
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter("c:/simulation2/Data_automation/data/" + i + "_" + j +".txt"));
				for(int row = 0; row < num; row++)
				{
					for(int col = 0; col < num + 1; col++)
					{
						writer.write(slotTable[row][col] + "\t");
					}
					writer.newLine();
				}
				writer.close();
			}
		}
	}
	
	
	public static void readFromFile(String fileName, LinkedList input) throws IOException
	{
		int counter;
		String line;
		BufferedReader reader;
		String[] result;
		Row row;
		
		reader = new BufferedReader(new FileReader(fileName));
		
		counter = 0;
		while( (line = reader.readLine()) != null)
		{
			if(counter >= 0)
			{
				line = line.trim();
				result = line.split("\\t");
				row = new Row(result[0],result[1],result[2],result[3]);
				input.add(row);
			}
			counter++;
		}
	}
	
	public static void readFromIndex(String fileName, LinkedList index) throws IOException
	{
		int counter;
		String line;
		BufferedReader reader;
		String[] result;
		RowIndex rowindex;
		
		reader = new BufferedReader(new FileReader(fileName));
		
		counter = 0;
		while( (line = reader.readLine()) != null)
		{
			if(counter >= 0)
			{
				line = line.trim();
				result = line.split("\\t");
				rowindex = new RowIndex(Integer.parseInt(result[0]),result[1]);
				index.add(rowindex);
			}
			counter++;
		}
		
	}
	
	public static void processInput(LinkedList input, LinkedList index) throws IOException
	{
		Row row;
		RowIndex rowindex;
		String line;
		String[] entry,c_time,p_time;
		String cTime,pTime;
		//int num = 24/timeInt;
		//int[][] slotTable = new int[num][num + 1];
		BufferedReader reader;
		BufferedWriter writer;
		int node_origin=-1, node_dest=-1;
		int ct1=0, ct2=0;
		for(int i=0; i<input.size(); i++)
		{
			node_origin=-1; node_dest=-1;
			ct1=0; ct2=0;
			row = (Row)(input.get(i));
			cTime=row.cTime;
			pTime=row.pTime;
			for(int j=0; j<index.size(); j++)
			{
				ct1=0; ct2=0;
				rowindex = (RowIndex)(index.get(j));
				if(row.origin.equals(rowindex.area))
				{
					node_origin=rowindex.node_no;
					
					ct1=1;
				}
				if(row.dest.equals(rowindex.area))
				{
					node_dest=rowindex.node_no;
					
					ct2=1;
				}
				
			}
			if(node_origin==-1){node_origin=0;}
			if(node_dest==-1){node_dest=0;}
			//System.out.println("node_origin:  "+node_origin);
			//System.out.println("node_dest:  "+node_dest);
		//	triptable[node_origin][node_dest]=triptable[node_origin][node_dest]+1;
			
			output[i][0]=i+1;
			output[i][1]=node_origin;
			output[i][2]=node_dest;
			
			
		/*	// Read from the corresponding file
			reader = new BufferedReader(new FileReader("c:/simulation2/Data_automation/data/" + node_origin + "_" + node_dest + ".txt"));
			int k = 0;
			while( (line = reader.readLine()) != null)
			{
				entry = (line.split("\\t"));
				for(int l = 0; l < num + 1; l++)
				{
					slotTable[k][l] = Integer.parseInt(entry[l]);
				}
				k++;
			}
			reader.close();*/
			
			// Increment corresponding slot
			c_time = cTime.split(":");
			p_time = pTime.split(":");
			
			
			if(Integer.parseInt(c_time[0]) > Integer.parseInt(p_time[0]))
				{
					//slotTable[Integer.parseInt(p_time[0])/timeInt][num] += 1;
				output[i][4]=0 ;
				output[i][3]=Integer.parseInt(p_time[0])*60+Integer.parseInt(p_time[1]) ;}
			else
				{
					//slotTable[Integer.parseInt(p_time[0])/timeInt][Integer.parseInt(c_time[0])/timeInt] += 1;
				output[i][4]=Integer.parseInt(c_time[0])*60+Integer.parseInt(c_time[1]) ;
				output[i][3]=Integer.parseInt(p_time[0])*60+Integer.parseInt(p_time[1]) ;}
			
			
			/*// Write everything back to the file
			writer = new BufferedWriter(new FileWriter("c:/simulation2/Data_automation/data/" + node_origin + "_" + node_dest +".txt"));
			for(int x = 0; x < num; x++)
			{
				for(int y = 0; y < num + 1; y++)
				{
					writer.write(slotTable[x][y] + "\t");
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();*/
		}
		//System.out.println("Triptable:  "+triptable[0][1]);*
	}
	
	
	
	public static void printInput(LinkedList input)
	{
		
		Row row;
		
		for(int i = 0; i < input.size(); i++)
		{
			row = (Row)(input.get(i));
			System.out.print(row.origin + "\t" + row.dest /*+ "\t" + row.cTime + "\t" + row.pTime*/);
			System.out.println();
		}
	}
	
	
	public static void findDesirabilty() throws Exception
	{
		
		int trips_node=0;
		int pertrip_oppcost=60, commuting_charge=40;
		
		floydWarshall(60,SPATHD,ADJMATRIX);
		
		for(int i=0;i<60;i++)
		{
			for(int j=0;j<60;j++)
			{
				trips_node=0;
				for(int k=0;k<60;k++)
				{
					trips_node+=triptable[j][k];
					//System.out.print(k+"	");
				}
				desirabilty[i][j]= (trips_node*pertrip_oppcost)-(SPATHD[i][j]*commuting_charge);
			}	
		}
		
		float maxvalue=0, minvalue=10000;
		for(int i=0;i<60;i++)
		{
			for(int j=0;j<60;j++)
			{
				if (maxvalue<desirabilty[i][j])
				maxvalue=desirabilty[i][j];
				if (minvalue>desirabilty[i][j])
				minvalue=desirabilty[i][j];
			}	
		}
		System.out.println("Max value: "+maxvalue);
		for(int i=0;i<60;i++)
		{
			for(int j=0;j<60;j++)
			{
				desirabilty[i][j]=((desirabilty[i][j]+Math.abs(minvalue))/(maxvalue+Math.abs(minvalue)));
			}	
		}
		
	}
	
	public static void printIndex(LinkedList index)
	{
		
		RowIndex rowindex;
		
		for(int i = 0; i < index.size(); i++)
		{
			rowindex = (RowIndex)(index.get(i));
			System.out.print(rowindex.node_no + "\t" + rowindex.area);
			System.out.println();
		}
	}
	public static void printToFile(String fileName, int[][] triptable) throws IOException
	{
		BufferedWriter writer;
		
		writer = new BufferedWriter(new FileWriter(fileName));
		for(int i = 0; i < triptable.length; i++)
		{
			for(int j = 0; j < triptable[i].length; j++)
			{
				writer.write(triptable[i][j] + "\t");
			}
			writer.newLine();
		}
		
		writer.flush();
		writer.close();
		
	}
	
	public static void printToFileFloat(String fileName, float[][] desirabilty) throws IOException
	{
		BufferedWriter writer;
		
		writer = new BufferedWriter(new FileWriter(fileName));
		for(int i = 0; i < desirabilty.length; i++)
		{
			for(int j = 0; j < desirabilty[i].length; j++)
			{
				writer.write(desirabilty[i][j] + "\t");
			}
			writer.newLine();
		}
		
		writer.flush();
		writer.close();
		
	}
	public static void printOutput(String fileName, int[][] output) throws IOException
	{
		BufferedWriter writer;
		
		writer = new BufferedWriter(new FileWriter(fileName));
		for(int i = 0; i < output.length; i++)
		{
			for(int j = 0; j < 5; j++)
			{
				writer.write(output[i][j] + "\t");
			}
			writer.newLine();
		}
		
		writer.flush();
		writer.close();
		
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
	
}
