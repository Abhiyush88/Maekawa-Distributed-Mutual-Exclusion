import java.util.ArrayList;

public class node {
int id;


String hostName;
int port;
ArrayList<Integer> quorums = new ArrayList<Integer>();

public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getHostName() {
	return hostName;
}
public void setHostName(String hostName) {
	this.hostName = hostName;
}
public int getPort() {
	return port;
}
public void setPort(int port) {
	this.port = port;
}
public ArrayList<Integer> getQuorums() {
	return quorums;
}
public void setQuorums(ArrayList<Integer> quorum) {
	this.quorums = quorum;
}
}
