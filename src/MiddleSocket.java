import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
	
	public void setStream() {

		try {
			outData = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			inData = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//
//	public void sendOutput(ArrayList<Byte> array) {
//		int len = array.size();
//		byte[] b = new byte[array.size()];
//
//		for (int i = 0; i < array.size(); ++i) {
//			b[i] = array.get(i).byteValue();
//		}
//		try {
//			outData.write(b, 0, len);
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Error in output");
//		}
//	}
	
	public void sendOutput(byte[] b, int len) {
		try {
			outData.write(b, 0, len);
			outData.flush();
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
			inData.close();
			outData.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasInput() {
		try {
			int i = inData.available();
			// if (i != 0)
			// System.out.println("available " + i);
			return (i != 0);
			// return (inData.available() != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
