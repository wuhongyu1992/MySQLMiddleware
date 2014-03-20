import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class MiddleClient extends MiddleSocket {
	
	public MiddleClient(String ip, int port) {
		super(ip, port);
	}
	
	public void startClient() {
		try {
			socket = new Socket(ipAddr, portNum);
			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());
		}
		catch (IOException ioe) {
			System.out.println("Error in connecting to server");
			System.exit(10);
		}
	}
	
//	public void reconnect() {
//		try {
//			socket.connect(socket.getRemoteSocketAddress());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
//	public int getInput(byte[] byteArray) {
//		int len = 0;
//		try {
//			
//			len = inData.read(byteArray);
//		} catch (IOException ioe) {
//			System.out.println("Error in receiving data stream");
//		}
//		System.out.println(len);
//		return len;
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
	
	
	
}
