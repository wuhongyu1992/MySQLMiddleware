import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class MiddleServer extends MiddleSocket {
//	private Socket socket;
//	private String ipAddr;
//	private int portNum;
//	private DataOutputStream outData;
//	private DataInputStream inData;
	
	public MiddleServer(String ip, int port) {
		super(ip, port);
	}
	
	public void startServer() {
		ServerSocket serverSock;
		try {
			serverSock = new ServerSocket(portNum);
			System.out.println("server is waiting...");
			socket = serverSock.accept();

			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());
			//System.out.println("startServer");
		} catch (IOException ioe) {
			System.out.println("Error in starting server");
		}
	}

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
