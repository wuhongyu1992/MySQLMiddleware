import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MiddleServer extends MiddleSocket {
//	private Socket socket;
//	private String ipAddr;
//	private int portNum;
//	private DataOutputStream outData;
//	private DataInputStream inData;
	private boolean inTrax;
	
	public MiddleServer() {
		super();
//		socket = inSock;
		setInTrax(false);
	}
	
	public void startServer(Socket inSock) {
//		ServerSocket serverSock;
		try {
//			serverSock = new ServerSocket(portNum);
			
//			serverSock.setSoTimeout(5000);
			
//			System.out.println("server is waiting...");
//			socket = serverSock.accept();
			socket = inSock;

			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());
			//System.out.println("startServer");
		} catch (IOException ioe) {
			System.out.println("Error in starting server");
		}
	}

	public boolean isInTrax() {
		return inTrax;
	}

	public void setInTrax(boolean inTrax) {
		this.inTrax = inTrax;
	}

	public boolean hasInput() {
		try {
			return (inData.available() != 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

//	public boolean isConnected() {
//		// TODO Auto-generated method stub
//		return socket.isConnected();
//	}
	
//	
//	public void sendOutput(byte[] b, int len) {
//		try {
//			outData.write(b, 0, len);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.out.println("Error in output");
//		}
//	}
//	
//	public int getInput(byte[] byteArray) {
//		int len = 0;
//		try {
//
//			len = inData.read(byteArray);
//
//		} catch (IOException ioe) {
//			System.out.println("Error in receiving data stream");
//		}
//		System.out.println(len);
//		return len;
//	}
//	
	
}
