import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MiddleServer extends MiddleSocket {

	public MiddleServer() {
		super();		
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

		// System.out.println(clientPortNum);
	}



	public int getClientPort() {
		return socket.getPort();
	}



}
