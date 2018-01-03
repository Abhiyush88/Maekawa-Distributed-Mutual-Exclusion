import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class server extends Thread{
	private int port;
	client clientObj = new client();
	ServerSocket serverSocket= null;
	Socket clientSocket = null;
	static ArrayList<Integer> count = new ArrayList<Integer>();
	static Boolean csPermissionGranted = false;
	int csPermissionGrantedTo[][]  = new int[1][2];
	static boolean failedMsg = false;
	//List<Message> waitingList = new ArrayList<Message>();
	static ArrayList<Integer> failedList = new ArrayList<Integer>();

	PriorityQueue<Message> localQueue = new PriorityQueue<Message>(client.numberofNodes, new Comparator<Message>(){
		@Override
		public int compare(Message a, Message b)
		{
			
			 if(a.getClock() > b.getClock())
			return 1;
			if(a.getClock() < b.getClock())
				return -1;
			if(a.getClock() == b.getClock())
				{
					if(a.getSenderId() > b.getSenderId())
					{
						return 1;
					}
					
					else return -1; 
				}
			 return 0;
		}	
	});
	
	public server(int port)
	{
		this.port = port;
		//clientObj = new client();
	}
	
	public void run()
	{
		try {

			serverSocket = new ServerSocket(port);
			

		} catch (IOException e1) {

		}	
		 while(true) {
			synchronized (clientObj) {
				{
					try {
						clientSocket = serverSocket.accept();
						ObjectInputStream objectInput = new ObjectInputStream(clientSocket.getInputStream());
						//Object object = (Message) objectInput.readObject();
						Message objRec = (Message) objectInput.readObject();

						if (objRec.getMessageType() == 1) {
							client.clock = Math.max(objRec.getClock(), client.clock) + 1;
							if (csPermissionGranted == false && localQueue.isEmpty()) {
								csPermissionGranted = true;
								csPermissionGrantedTo[0][0] = objRec.getSenderId();
								csPermissionGrantedTo[0][1] = objRec.getClock();

								grantPermission(objRec.getSenderId());
							} else {

								if (objRec.clock > csPermissionGrantedTo[0][1]) {
									// send failed message
									sendFailed(objRec.getSenderId());
									//add to queue
									localQueue.add(objRec);
								} else if (objRec.clock == csPermissionGrantedTo[0][1]) {
									if (objRec.getSenderId() > csPermissionGrantedTo[0][0]) {
										// send failed message
										sendFailed(objRec.getSenderId());
										//add to queue
										localQueue.add(objRec);
									} else {
										//send yield/inquire message
										localQueue.add(objRec);
										sendInquire(csPermissionGrantedTo[0][0]);
									}
								} else {
									//send yield/inquire message
									localQueue.add(objRec);
									sendInquire(csPermissionGrantedTo[0][0]);
								}

							}
						} else if (objRec.getMessageType() == 2) {
							count.add(objRec.getSenderId());
							for (int i = 0; i < failedList.size(); i++) {
								if (failedList.get(i) == objRec.getSenderId()) {
									failedList.remove(i);
								}
							}
							if (failedList.isEmpty()) {
								failedMsg = false;
							}

						} else if (objRec.getMessageType() == 3) {
							failedMsg = true;
							failedList.add(objRec.getSenderId());
						} else if (objRec.getMessageType() == 4) {
							if (failedMsg) {
								for (int i = 0; i < count.size(); i++) {
									if (count.get(i) == objRec.getSenderId()) {
										count.remove(i);
									}
								}

								sendRelinquish(objRec.getSenderId());
							}
						} else if (objRec.getMessageType() == 5) {

							Message input = new Message();
							input.setSenderId(csPermissionGrantedTo[0][0]);
							input.setClock(csPermissionGrantedTo[0][1]);
							input.setMessageType(1);
							localQueue.add(input);
							//grant permission to first element in queue
							Message msg = localQueue.poll();
							int node = msg.getSenderId();
							//grant permission to node
							csPermissionGrantedTo[0][0] = node;
							csPermissionGrantedTo[0][1] = msg.getClock();
							grantPermission(node);

						} else if (objRec.getMessageType() == 6) {
							csPermissionGranted = false;
							if (!localQueue.isEmpty()) {
								csPermissionGranted = true;
								Message msg = localQueue.poll();
								int node = msg.getSenderId();
								//grant permission to node
								csPermissionGrantedTo[0][0] = node;
								csPermissionGrantedTo[0][1] = msg.getClock();
								grantPermission(node);

							}
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public void grantPermission(int id) throws IOException
	{
		Message grant = new Message();
		grant.setSenderId(client.currentNode);
		grant.setMessageType(2);
		int port = client.info.get(id).getPort();
		String hostname = client.info.get(id).hostName;
		Socket socket1 = new Socket(hostname, port);
		ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
		objectOutput.writeObject(grant);
		socket1.close();
	}
	
	public void sendFailed(int id) throws IOException
	{
		Message grant = new Message();
		grant.setSenderId(client.currentNode);
		grant.setMessageType(3);
		int port = client.info.get(id).getPort();
		String hostname = client.info.get(id).hostName;
		Socket socket1 = new Socket(hostname, port);
		ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
		objectOutput.writeObject(grant);
		socket1.close();
	}
	
	public void sendInquire(int id) throws IOException
	{
		Message grant = new Message();
		grant.setSenderId(client.currentNode);
		grant.setMessageType(4);
		int port = client.info.get(id).getPort();
		String hostname = client.info.get(id).hostName;
		Socket socket1 = new Socket(hostname, port);
		ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
		//System.out.println("14");
		objectOutput.writeObject(grant);
		socket1.close();
	}
	
	public void sendRelinquish(int id) throws IOException
	{
		Message grant = new Message();
		grant.setSenderId(client.currentNode);
		grant.setMessageType(5);
		int port = client.info.get(id).getPort();
		String hostname = client.info.get(id).hostName;
		Socket socket1 = new Socket(hostname, port);
		ObjectOutputStream objectOutput = new ObjectOutputStream(socket1.getOutputStream());
		objectOutput.writeObject(grant);
		socket1.close();
	}

}
