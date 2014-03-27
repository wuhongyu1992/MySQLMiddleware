import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MiddleServerSocket extends Thread {
	private SharedData sharedData;
	private ServerSocket serverSock;

	MiddleServerSocket(SharedData s) {
		sharedData = s;
		try {
			serverSock = new ServerSocket(s.getMiddlePortNum());
			serverSock.setSoTimeout(10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!sharedData.isEndOfProgram()) {
			Socket socket = null;
			try {
				socket = serverSock.accept();
			} catch (SocketTimeoutException e1) {
				
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			if (socket != null) {
				MiddlewareUnit newUnit = new MiddlewareUnit(sharedData);
				if (newUnit.setUp(socket)) {
					newUnit.start();
				}
			}
		}

		System.out.println("server socket end");
	}
}
