import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class MiddleSocket {
	protected Socket socket;
	protected String ipAddr;
	protected int portNum;
	protected DataOutputStream outData;
	protected DataInputStream inData;

	public MiddleSocket(String ip, int port) {
		ipAddr = ip;
		portNum = port;
		socket = null;
		inData = null;
		outData = null;
	}

	public MiddleSocket() {
		socket = null;
		inData = null;
		outData = null;
	}

	public void sendOutput(ArrayList<Byte> array) {
		int len = array.size();
		byte[] b = new byte[array.size()];
		
//		for (int i = 0; i < array.size(); ++i) {
//			if (b[i] < (byte) 32) {
//
//				System.out.print(".");
//
//			} else if (b[i] >= (byte) 32 && b[i] < (byte) 127)
//				System.out.print((char) b[i]);
//			else
//				System.out.printf(" %02x ", b[i]);
//		}
//		System.out.println();
		
		for (int i = 0; i < array.size(); ++i) {
			b[i] = array.get(i).byteValue();
		}
		try {
			outData.write(b, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in output");
		}
	}

	public int getInput(byte[] byteArray) {
		int len = 0;
		try {

			len = inData.read(byteArray);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Error in receiving data stream");
		}
		// System.out.println(len);
		return len;
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public boolean hasInput() {
		try {
			int i = inData.available();
//			System.out.println("available" + i);
			return (i != 0);
//			return (inData.available() != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
