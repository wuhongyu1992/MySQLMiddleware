import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MiddleServer extends MiddleSocket {

	public MiddleServer() {
		super();
	}

	public void startServer(Socket inSock) {

		socket = inSock;
		setStream();
		
		// System.out.println("startServer");

		// System.out.println(clientPortNum);
	}

	public int getClientPort() {
		return socket.getPort();
	}

}
