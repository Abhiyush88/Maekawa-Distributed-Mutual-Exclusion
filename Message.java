/* Message Formats:
 * Type 1: Request for Critical Section
 * Type 2: Grant Critical Section
 * Type 3: Failed Request
 * Type 4: yield/inquire Critical Section
 * Type 5: Give back access to Critical Section
 * Type 6: Release Critical Section
 */

import java.io.Serializable;

public class Message implements Serializable {
private static final long serialVersionUID = 3884398797680355305L;
int messageType;
int senderId;
int clock;

public int getClock() {
	return clock;
}
public void setClock(int clock) {
	this.clock = clock;
}
public int getMessageType() {
	return messageType;
}
public void setMessageType(int messageType) {
	this.messageType = messageType;
}
public int getSenderId() {
	return senderId;
}
public void setSenderId(int senderId) {
	this.senderId = senderId;
}


}
