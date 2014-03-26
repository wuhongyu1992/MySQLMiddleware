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

	public void addSQLToTrax() {
		String s = new String();

		for (int i = 5; i < clientDataArray.size(); ++i) {
			if (clientDataArray.get(i) < (byte) 32) {
				s += '.';

			} else {
				s += (char) clientDataArray.get(i).byteValue();
			}

		}
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

	public boolean traxBegin() {
		byte[] temp = new byte[clientDataArray.size() - 5];
		for (int i = 5; i < clientDataArray.size(); ++i) {
			temp[i - 5] = clientDataArray.get(i).byteValue();
		}
		if (clientDataArray.size() == 10) {
			if (temp[0] != 'b' && temp[0] != 'B')
				return false;
			if (temp[1] != 'e' && temp[0] != 'E')
				return false;
			if (temp[2] != 'g' && temp[0] != 'G')
				return false;
			if (temp[3] != 'i' && temp[0] != 'I')
				return false;
			if (temp[4] != 'n' && temp[0] != 'N')
				return false;

			return true;
		}
		return false;
	}
//	public void addToClientData(byte[] clientData, int clientDataLen) {
//		
//	}

	public boolean traxEnd() {
		byte[] temp = new byte[clientDataArray.size() - 5];
		for (int i = 5; i < clientDataArray.size(); ++i) {
			temp[i - 5] = clientDataArray.get(i).byteValue();
		}
		if (clientDataArray.size() == 11) {
			if (temp[0] != 'c' && temp[0] != 'C')
				return false;
			if (temp[1] != 'o' && temp[0] != 'O')
				return false;
			if (temp[2] != 'm' && temp[0] != 'M')
				return false;
			if (temp[3] != 'm' && temp[0] != 'M')
				return false;
			if (temp[4] != 'i' && temp[0] != 'I')
				return false;
			if (temp[5] != 't' && temp[0] != 'T')
				return false;

			return true;
		}
		if (clientDataArray.size() == 13) {
			if (temp[0] != 'r' && temp[0] != 'R')
				return false;
			if (temp[1] != 'o' && temp[0] != 'O')
				return false;
			if (temp[2] != 'l' && temp[0] != 'L')
				return false;
			if (temp[3] != 'l' && temp[0] != 'L')
				return false;
			if (temp[4] != 'b' && temp[0] != 'B')
				return false;
			if (temp[5] != 'a' && temp[0] != 'A')
				return false;
			if (temp[6] != 'c' && temp[0] != 'C')
				return false;
			if (temp[7] != 'k' && temp[0] != 'K')
				return false;

			return true;
		}
		return false;
	}

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
