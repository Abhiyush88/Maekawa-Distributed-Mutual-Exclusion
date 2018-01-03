import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class client {
	static int numberofNodes;
	static int meanInterReqDelay;
	static int meanExecTime;
	static int numberofRequests;
	static int clock=0;
	static int localClock=0;
	public static HashMap<Integer, node> info = new HashMap<Integer, node>();
	public static HashMap<String, Integer> convert = new HashMap<String, Integer>();
	private static int port;
	private static String host = null;
	static int currentNode;
	
	public static void readConfig(String path) throws IOException {
		int countofnodes = 0;
		int i = 0;
		BufferedReader br = null;
		String currLine;
		br = new BufferedReader(new FileReader(path));
		while ((currLine = br.readLine()) != null) {
			if (!currLine.startsWith("#")) {
				String trimmed = currLine.trim();
				if (!trimmed.equals("")) {
					//trimmed.replace("\\s+", " ");
					String line[] = trimmed.split("\\s+");
					if (i == 0) {
						i++;
						numberofNodes = Integer.parseInt(line[0].trim());
						meanInterReqDelay = Integer.parseInt(line[1].trim());
						meanExecTime = Integer.parseInt(line[2].trim());
						numberofRequests = Integer.parseInt(line[3].trim());

					} else if (i <= numberofNodes) {
						node nodes = new node();
						nodes.setId(Integer.parseInt(line[0].trim()));
						nodes.setHostName(line[1].trim());
						nodes.setPort(Integer.parseInt(line[2].trim()));
						convert.put(line[1], Integer.parseInt(line[0].trim()));
						info.put(Integer.parseInt(line[0].trim()), nodes);
						i++;

					} else {

						ArrayList<Integer> quorums = new ArrayList<Integer>();
						for (int k = 0; k < line.length; k++) {
							if(line[k].trim().startsWith("#")){
								break;
							}
							quorums.add(Integer.parseInt(line[k].trim()));
						}
						node nodes = info.get(countofnodes);
						nodes.setQuorums(quorums);
						info.put(countofnodes, nodes);
						countofnodes++;
					}

				}
			}
		}
		br.close();

	}

	public static int generateRandomNumber(int mean)
	{
		double random = Math.random();
		double rate = (double)1/(double)mean;
		random = (Math.log(1-random))/(- rate);
		//random= random* 1000;
		int convert = (int)random;
		return convert;

	}
	
	public static void csEnter()
	{
		localClock= clock;
		try {
            
			Message broad = new Message();
			broad.setSenderId(currentNode);
			broad.setMessageType(1);
			broad.setClock(clock);
			ArrayList<Integer> listofquorums = info.get(currentNode).getQuorums();
			for (int neighid : listofquorums) 
			{
				node node1 = info.get(neighid);
				int neighPort = node1.port;
				String hosttemp = node1.hostName;
				
				Socket socket1 = new Socket(hosttemp, neighPort);
				ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
				objectOutput.writeObject(broad);
				socket1.close();
				
			}
			
			clock = clock +1;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void csExec() throws IOException, InterruptedException
	{
		int randomNo = generateRandomNumber(meanExecTime);
		// write to file
		/*File file = new File("sharedResource");
		BufferedWriter bfWriter = new BufferedWriter(new FileWriter(file,true));
		bfWriter.write(currentNode + ":" + localClock + System.lineSeparator());
		bfWriter.close();
		
		
		
		String name= "Node-"+currentNode + ".txt";
		File file1 = new File(name);
		BufferedWriter bfWriter1 = new BufferedWriter(new FileWriter(file1,true));
		bfWriter1.write(currentNode + ":" + localClock + System.lineSeparator());
		bfWriter1.close();
		
		Thread.sleep(randomNo);*/
		
		File file1 = new File("output.txt");
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(file1,true));
		File file = new File("sharedResource.txt");
                RandomAccessFile rf = new RandomAccessFile(file, "rw");
                FileChannel fc = rf.getChannel();
                FileLock lock = fc.tryLock();
                if(lock!=null)
                {
                	String out = "Node "+currentNode + ":" + "Clock count "+localClock;
                    bfWriter.write(out + System.lineSeparator());
                    bfWriter.close();
                    Thread.sleep(randomNo);
					lock.release();
					
                }
                else
                {
                	System.out.println("Multiple CS execution");
                }
                fc.close();
                rf.close();
	}
	
	public static void csLeave()
	{
		try {

			Message broad = new Message();
			broad.setSenderId(currentNode);
			broad.setMessageType(6);
			ArrayList<Integer> listofquorums = info.get(currentNode).getQuorums();
			for (int neighid : listofquorums) 
			{
				node node1 = info.get(neighid);
				int neighPort = node1.port;
				String hosttemp = node1.hostName;
				
				Socket socket1 = new Socket(hosttemp, neighPort);
				ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
				objectOutput.writeObject(broad);
				socket1.close();
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String pathtofile = args[1];
		//System.out.println();
		readConfig(pathtofile);
		
		
		
		int id = 0;
		host = InetAddress.getLocalHost().getHostName().substring(0, 4);
		for (Entry<String, Integer> trav : convert.entrySet()) {
			if (trav.getKey().equalsIgnoreCase(host)) {
				id = trav.getValue();
				currentNode = id;

			}
		}
		
		/*String name= "Node-"+currentNode + ".txt";
		File file = new File(name);
		file.createNewFile();*/
		
		// server initiated
		
		node temp = info.get(id);
		port = temp.port;
		server sp = new server(port);
		sp.start();
		
		//
		Thread.sleep(1000);
		
		// client initiates
		
		for(int i=0; i< numberofRequests; i++)
		{
			int randomNo = generateRandomNumber(meanInterReqDelay);
			Thread.sleep(randomNo);
			csEnter();
			
			while(true)
			{
				Thread.sleep(5);
				synchronized (sp) {
					//System.out.println(server.count.size() + "==>" + info.get(currentNode).getQuorums().size());
					if (server.count.size() == info.get(currentNode).getQuorums().size()) {
						csExec();
						server.count.clear();
						/*for(int j =0; j<info.get(currentNode).getQuorums().size(); j++)
						{
							server.count.remove(j);
						}
						*/ csLeave();
						break;
					}
				}
			}
			
		}
		
		System.out.println("End of node execution "+ currentNode);
		
	}

}
