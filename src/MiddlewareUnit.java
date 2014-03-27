import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class MiddlewareUnit extends Thread {

	final boolean outputFlag = false;

	private MiddleServer server;
	private MiddleClient client;
	private SharedData sharedData;
	private int maxSize;
	private int clientPortNum;


	private byte[] clientData;
	private int clientDataLen;

	private byte[] serverData;
	private int serverDataLen;

	private ArrayList<Byte> clientDataArray;
	private ArrayList<Byte> serverDataArray;

	private ArrayList<String> trax;
	private boolean inTrax;

	private int latency;
	private long sendTime;
	private long recTime;

	private File file;
	private PrintStream printStream;

	MiddlewareUnit(SharedData s) {
		sharedData = s;
		maxSize = sharedData.getMaxSize();

		server = new MiddleServer();
		client = new MiddleClient(sharedData.getServerIpAddr(),
				sharedData.getServerPortNum());

		clientData = new byte[maxSize];
		clientDataLen = 0;

		serverData = new byte[maxSize];
		serverDataLen = 0;

		clientDataArray = new ArrayList<Byte>();
		serverDataArray = new ArrayList<Byte>();

		trax = new ArrayList<String>();
		inTrax = false;

		latency = 0;
		sendTime = 0;
		recTime = 0;

		file = null;
		printStream = null;
	}

	public void run() {

		while (!sharedData.isEndOfProgram()) {
			if (!server.isConnected()) {
				break;
			}
			// System.out.println(i);

			clientDataArray.clear();

			// System.out.println("before read");
			getClientData();
			// do {
			// clientDataLen = server.getInput(clientData);
			// addToList(server.clientDataArray, clientData, clientDataLen);
			// } while (clientDataLen == maxSize);

			// if (server.clientDataArray.size() == 0) {
			// }
			// System.out.println("after read");

			if (sharedData.isEndOfProgram()) {
				break;
			}

			if (traxBegin()) {
				inTrax = true;
				// System.out.println("Transaction begins.");
				latency = 0;
				trax.clear();
			}

			if (inTrax) {
				// System.out.print("Client: ");
				// showClientData(clientDataArray);
				addSQLToTrax();
			}

			recTime = 0;
			serverDataArray.clear();

			// showClientData(server.clientDataArray);
			client.sendOutput(clientDataArray);
			sendTime = System.currentTimeMillis();

			getServerData();
			// do {
			// serverDataLen = client.getInput(serverData);
			// if (server.getRecTime() == 0)
			// server.setRecTime(System.currentTimeMillis());
			//
			// addToList(server.serverDataArray, serverData, serverDataLen);
			//
			// } while (serverDataLen == maxSize);

			if (serverDataArray.size() == 0) {
				break;
			}

			if (inTrax) {
				latency += recTime - sendTime;
			}

			if (inTrax && traxEnd()) {
				inTrax = false;
				// System.out.println("Transaction ends.");
				if (sharedData.isOutputToFile()) {
					if (file == null){
						setOutputFileStream();
					}
					printTrax();
				}
			}

			// System.out.print("Server: ");
			// showData(serverData, serverDataLen);

			server.sendOutput(serverDataArray);

			// if (server.clientQuit()) {
			// break;
			// }

		}
		server.close();
		client.close();
		
		if (printStream != null) {
			printStream.close();
		}

		System.out.println("Client(" + clientPortNum + ") quit");

	}

	synchronized private void getServerData() {
		do {
			serverDataLen = client.getInput(serverData);
			if (recTime == 0)
				recTime = System.currentTimeMillis();

			addToList(serverDataArray, serverData, serverDataLen);

			// System.out.print("server: ");
			// showData(serverData, serverDataLen);
		} while (serverDataLen == maxSize);

	}

	synchronized private void getClientData() {
		do {
			clientDataLen = server.getInput(clientData);
			addToList(clientDataArray, clientData, clientDataLen);

			// System.out.print("client: ");
			// showData(clientData, clientDataLen);
		} while (clientDataLen == maxSize);
	}

	synchronized public boolean setUp(Socket socket) {
		server.startServer(socket);
		client.startClient();

		serverDataLen = client.getInput(serverData);
		if (outputFlag) {
			System.out.println("s");
			server.serverDataArray.clear();
		}
		if (outputFlag)
			showData(serverData, serverDataLen);
		addToList(serverDataArray, serverData, serverDataLen);
		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);

		if (outputFlag) {
			System.out.println("c");
			showData(clientData, clientDataLen);
			System.out.println("send client info");
		}

		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);
		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);

		if (outputFlag) {
			System.out.println("s");
			showData(serverData, serverDataLen);
		}

		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);
		if (outputFlag)
			System.out.println("get server OK packet");

		server.sendOutput(serverDataArray);

		if (isErrorPacket(serverDataArray)) {
			server.printFailConnection();
			return false;
		}

		clientDataLen = server.getInput(clientData);
		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);

		if (outputFlag) {
			System.out.println("c");
			showData(clientData, clientDataLen);
			// System.out.println("111111111111111111");
		}

		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);

		if (outputFlag) {
			System.out.println("s");
			showData(serverData, serverDataLen);
		}

		// System.out.println("2222222222222222222222222");

		server.sendOutput(serverDataArray);

		if (isErrorPacket(serverDataArray)) {
			server.printFailConnection();
			return false;
		}

		sharedData.addClient();
		clientPortNum = server.getClientPort();

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

	private static void showData(byte[] b, int len) {
		System.out.printf("%02x", b[0]);
		System.out.print(" ");
		System.out.printf("%02x", b[1]);
		System.out.print(" ");
		System.out.printf("%02x", b[2]);
		System.out.print(" ");
		System.out.printf("%02x", b[3]);
		System.out.print(" ");
		System.out.printf("%02x", b[4]);
		System.out.print(" ");
		for (int i = 5; i < len; ++i) {
			if (b[i] < (byte) 32) {
				// System.out.print(new String ("'"));
				// System.out.print((byte) b[i]);
				// System.out.print(new String ("'"));
				System.out.print(".");

			} else if (b[i] >= (byte) 32 && b[i] < (byte) 127)
				System.out.print((char) b[i]);
			else
				System.out.printf(" %02x ", b[i]);
		}
		System.out.println();

	}

	private void addSQLToTrax() {
		String s = new String();

		for (int i = 5; i < clientDataArray.size(); ++i) {
			if (clientDataArray.get(i) < (byte) 32) {
				s += '.';

			} else {
				s += (char) clientDataArray.get(i).byteValue();
			}

		}
		trax.add(s);
	}

	private void setOutputFileStream() {
		file = new File(sharedData.getFilePathName() + "/C" + clientPortNum
				+ ".txt");
		try {
			printStream = new PrintStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void printTrax() {
		for (int i = 0; i < trax.size(); ++i) {
			printStream.println(trax.get(i));
		}
		printStream.println("# Latency: " + latency + " ms\n");

	}

	private boolean traxBegin() {
		byte[] temp = new byte[clientDataArray.size() - 5];
		for (int i = 5; i < clientDataArray.size(); ++i) {
			temp[i - 5] = clientDataArray.get(i).byteValue();
		}
		if (clientDataArray.size() == 10) {
			if (temp[0] != 'b' && temp[0] != 'B')
				return false;
			if (temp[1] != 'e' && temp[0] != 'E')
				return false;
			if (temp[2] != 'g' && temp[0] != 'G')
				return false;
			if (temp[3] != 'i' && temp[0] != 'I')
				return false;
			if (temp[4] != 'n' && temp[0] != 'N')
				return false;

			return true;
		}
		return false;
	}

	// public void addToClientData(byte[] clientData, int clientDataLen) {
	//
	// }

	private boolean traxEnd() {
		byte[] temp = new byte[clientDataArray.size() - 5];
		for (int i = 5; i < clientDataArray.size(); ++i) {
			temp[i - 5] = clientDataArray.get(i).byteValue();
		}
		if (clientDataArray.size() == 11) {
			if (temp[0] != 'c' && temp[0] != 'C')
				return false;
			if (temp[1] != 'o' && temp[0] != 'O')
				return false;
			if (temp[2] != 'm' && temp[0] != 'M')
				return false;
			if (temp[3] != 'm' && temp[0] != 'M')
				return false;
			if (temp[4] != 'i' && temp[0] != 'I')
				return false;
			if (temp[5] != 't' && temp[0] != 'T')
				return false;

			return true;
		}
		if (clientDataArray.size() == 13) {
			if (temp[0] != 'r' && temp[0] != 'R')
				return false;
			if (temp[1] != 'o' && temp[0] != 'O')
				return false;
			if (temp[2] != 'l' && temp[0] != 'L')
				return false;
			if (temp[3] != 'l' && temp[0] != 'L')
				return false;
			if (temp[4] != 'b' && temp[0] != 'B')
				return false;
			if (temp[5] != 'a' && temp[0] != 'A')
				return false;
			if (temp[6] != 'c' && temp[0] != 'C')
				return false;
			if (temp[7] != 'k' && temp[0] != 'K')
				return false;

			return true;
		}
		return false;
	}

}
