import java.net.Socket;
import java.util.ArrayList;

public class MiddlewareUnit extends Thread {
	private MiddleServer server;
	private MiddleClient client;
	private SharedData sharedData;
	private int maxSize;
	static byte[] clientData;
	static int clientDataLen;

	static byte[] serverData;
	static int serverDataLen;

	MiddlewareUnit(SharedData s) {
		sharedData = s;
		maxSize = sharedData.getMaxSize();
		
		server = new MiddleServer();
		client = new MiddleClient(sharedData.getServerIpAddr(), sharedData.getServerPortNum());

		clientData = new byte[maxSize];
		clientDataLen = 0;

		serverData = new byte[maxSize];
		serverDataLen = 0;

	}

	public void run() {

		while (!sharedData.isEndOfProgram()) {
			if (!server.isConnected()) {
				break;
			}
			// System.out.println(i);

			server.clientDataArray.clear();

			// System.out.println("before read");
			getClientData();
			// do {
			// clientDataLen = server.getInput(clientData);
			// addToList(server.clientDataArray, clientData, clientDataLen);
			// } while (clientDataLen == maxSize);

			// if (server.clientDataArray.size() == 0) {
			// }
			// System.out.println("after read");

			if (server.traxBegin()) {
				server.setInTrax(true);
				// System.out.println("Transaction begins.");

				server.setLatency(0);

				server.clearTrax();

			}

			if (server.isInTrax()) {
				// System.out.print("Client: ");
				// showClientData(clientDataArray);
				server.addSQLToTrax();
			}

			server.setRecTime(0);
			server.serverDataArray.clear();

			client.sendOutput(server.clientDataArray);
			server.setSendTime(System.currentTimeMillis());

			getServerData();
			// do {
			// serverDataLen = client.getInput(serverData);
			// if (server.getRecTime() == 0)
			// server.setRecTime(System.currentTimeMillis());
			//
			// addToList(server.serverDataArray, serverData, serverDataLen);
			//
			// } while (serverDataLen == maxSize);

			if (server.serverDataArray.size() == 0) {
				break;
			}

			if (server.isInTrax()) {
				server.addLatency();
			}

			if (server.isInTrax() && server.traxEnd()) {
				server.setInTrax(false);
				// System.out.println("Transaction ends.");

				server.printTrax();
			}

			// System.out.print("Server: ");
			// showData(serverData, serverDataLen);

			server.sendOutput(server.serverDataArray);

			if (server.clientQuit()) {
				break;
			}

		}
		server.close();
		client.close();

	}

	synchronized private void getServerData() {
		do {
			serverDataLen = client.getInput(serverData);
			if (server.getRecTime() == 0)
				server.setRecTime(System.currentTimeMillis());

			addToList(server.serverDataArray, serverData, serverDataLen);

		} while (serverDataLen == maxSize);

	}

	synchronized private void getClientData() {
		do {
			clientDataLen = server.getInput(clientData);
			addToList(server.clientDataArray, clientData, clientDataLen);
		} while (clientDataLen == maxSize);
	}

	public boolean setUp(Socket socket) {
		server.startServer(socket);
		client.startClient();

		serverDataLen = client.getInput(serverData);

		// System.out.println("s");

		server.serverDataArray.clear();

		// showData(serverData, serverDataLen);
		addToList(server.serverDataArray, serverData, serverDataLen);
		server.sendOutput(server.serverDataArray);

		clientDataLen = server.getInput(clientData);

		// System.out.println("c");

		server.clientDataArray.clear();

		// showData(clientData, clientDataLen);
		addToList(server.clientDataArray, clientData, clientDataLen);

		// System.out.println("send client info");
		client.sendOutput(server.clientDataArray);

		serverDataLen = client.getInput(serverData);

		// System.out.println("s");

		// showData(serverData, serverDataLen);
		server.serverDataArray.clear();
		addToList(server.serverDataArray, serverData, serverDataLen);

		// System.out.println("get server OK packet");

		server.sendOutput(server.serverDataArray);

		if (isErrorPacket(server.serverDataArray)) {
			server.printFailConnection();
			return false;
		}

		clientDataLen = server.getInput(clientData);
		server.clientDataArray.clear();
		addToList(server.clientDataArray, clientData, clientDataLen);

		// System.out.println("c");

		// showData(clientData, clientDataLen);

		// System.out.println("111111111111111111");

		client.sendOutput(server.clientDataArray);

		serverDataLen = client.getInput(serverData);
		server.serverDataArray.clear();
		addToList(server.serverDataArray, serverData, serverDataLen);

		// System.out.println("s");

		// showData(serverData, serverDataLen);

		// System.out.println("2222222222222222222222222");

		server.sendOutput(server.serverDataArray);

		if (isErrorPacket(server.serverDataArray)) {
			server.printFailConnection();
			return false;
		}
		
		sharedData.addClient();
		
		return true;
	}

	private static void addToList(ArrayList<Byte> dataArray, byte[] data,
			int len) {
		for (int i = 0; i < len; ++i) {
			dataArray.add(data[i]);
		}
	}

	private static boolean isErrorPacket(ArrayList<Byte> array) {
		if (array.get(4).byteValue() == (byte) 255)
			return true;

		return false;
	}

}
