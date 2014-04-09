import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MiddleServerSocket extends Thread {
	private SharedData sharedData;
	private ServerSocket serverSock;
	private File dir;

	MiddleServerSocket(SharedData s) {
		sharedData = s;
		try {
			serverSock = new ServerSocket(s.getMiddlePortNum());
			serverSock.setSoTimeout(10);
		} catch (IOException e) {
			e.printStackTrace();
		}
		dir = new File(sharedData.getFilePathName() + "/Transactions");
		if (!dir.exists()) {
			dir.mkdirs();
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
				sharedData.setClearClients(false);
				MiddlewareUnit newUnit = new MiddlewareUnit(sharedData);
				if (newUnit.setUp(socket)) {
					newUnit.start();
					// sharedData.addUnit(newUnit);
				}
			}

			if (sharedData.getFileBufferSize() >= sharedData.getOutputSize()) {
				sharedData.flushOutput();
			}
		}

		System.out.println("server socket end");
	}
}
