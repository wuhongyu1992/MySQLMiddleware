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
		// TODO Auto-generated constructor stub
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
			// TODO Auto-generated catch block
			System.out.println("Error in output");
		}
	}

	public int getInput(byte[] byteArray) {
		int len = 0;
		try {

			len = inData.read(byteArray);

		} catch (IOException ioe) {
			System.out.println("Error in receiving data stream");
		}
		// System.out.println(len);
		return len;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return socket.isConnected();
	}

	public void close() {
		// TODO Auto-generated method stub
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
