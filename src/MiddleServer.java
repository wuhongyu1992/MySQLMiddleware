import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class MiddleServer extends MiddleSocket {
	
	private boolean inTrax;
	private int clientPortNum;
	private ArrayList<String> trax;
	private long latency;
	private long sendTime;
	private long recTime;
	public ArrayList<Byte> clientDataArray;
	public ArrayList<Byte> serverDataArray;

	public MiddleServer() {
		super();

		inTrax = false;
		trax = new ArrayList<String>();
		latency = 0;
		setSendTime(0);
		setRecTime(0);
		clientDataArray = new ArrayList<Byte>();
		serverDataArray = new ArrayList<Byte>();
		
	}

	public void startServer(Socket inSock) {

		socket = inSock;

		try {
			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("startServer");

		clientPortNum = socket.getPort();
		// System.out.println(clientPortNum);
	}

	public void printTrax() {
		System.out.println("client(" + clientPortNum + ") transaction:");
		for (int i = 0; i < trax.size(); ++i) {
			System.out.println(trax.get(i));
		}
		System.out.println("Latency: " + latency + " ms");
		System.out.println();

	}
	
	public void printFailConnection() {
		System.out.println("client(" + clientPortNum + ") fails connection.");
	}

	public void addLatency() {
		latency += (recTime - sendTime);
	}

	public void addToTrax(String s) {
		trax.add(s);
	}

	public boolean isInTrax() {
		return inTrax;
	}

	public void setInTrax(boolean inTrax) {
		this.inTrax = inTrax;
	}
//
//	public boolean hasInput() {
//		try {
//			int i = inData.available();
//			System.out.println("available" + i);
//			return (i != 0);
////			return (inData.available() != 0);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	public void clearTrax() {
		trax.clear();
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public void setRecTime(long recTime) {
		this.recTime = recTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public long getRecTime() {
		return recTime;
	}

	public boolean clientQuit() {
		if (clientDataArray.get(4).byteValue() == (byte) 1)
			return true;
		
		return false;
	}

//	public void addToClientData(byte[] clientData, int clientDataLen) {
//		
//	}

	//
	// public void sendOutput(byte[] b, int len) {
	// try {
	// outData.write(b, 0, len);
	// } catch (IOException e) {
	// System.out.println("Error in output");
	// }
	// }
	//
	// public int getInput(byte[] byteArray) {
	// int len = 0;
	// try {
	//
	// len = inData.read(byteArray);
	//
	// } catch (IOException ioe) {
	// System.out.println("Error in receiving data stream");
	// }
	// System.out.println(len);
	// return len;
	// }
	//

}
