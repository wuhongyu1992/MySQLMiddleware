import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddleServerSocket extends Thread {
	private SharedData sharedData;
	private ServerSocket serverSock;

	MiddleServerSocket(SharedData s) {
		sharedData = s;
		try {
			serverSock = new ServerSocket(s.getMiddlePortNum());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!sharedData.isEndOfProgram()) {
			Socket socket = null;
			try {
				socket = serverSock.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}

			MiddlewareUnit newUnit = new MiddlewareUnit(sharedData);
			if (newUnit.setUp(socket)) {
				newUnit.start();
			}
		}
	}
}
