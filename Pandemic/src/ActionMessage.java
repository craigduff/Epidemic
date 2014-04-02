import java.io.Serializable;

public class ActionMessage implements Serializable {

	protected static final long serialVersionUID = 1L;

	private String msgType;
	private String message;
	
	// constructor
	ActionMessage(String message) {
		this.message = message;
		msgType = "CHAT";
	}
	
	ActionMessage(String message, String type) {
		this.message = message;
		this.msgType = type;
	}
	
	String getMessage() {
		return message;
	}
	
	String getType() {
		return msgType;
	}
}