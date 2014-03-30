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

			if (sharedData.isOutputToFile() && !inTrax && traxBegin()) {
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

			if (outputFlag)
				showClientData(clientDataArray);
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
				if (file == null) {
					setOutputFileStream();
				}
				printTrax();

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
			serverDataArray.clear();
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
			printFailConnection();
			return false;
		}
		//
		// clientDataLen = server.getInput(clientData);
		// clientDataArray.clear();
		// addToList(clientDataArray, clientData, clientDataLen);
		//
		// if (outputFlag) {
		// System.out.println("c");
		// showData(clientData, clientDataLen);
		// // System.out.println("111111111111111111");
		// }
		//
		// client.sendOutput(clientDataArray);
		//
		// serverDataLen = client.getInput(serverData);
		// serverDataArray.clear();
		// addToList(serverDataArray, serverData, serverDataLen);
		//
		// if (outputFlag) {
		// System.out.println("s");
		// showData(serverData, serverDataLen);
		// }
		//
		// // System.out.println("2222222222222222222222222");
		//
		// server.sendOutput(serverDataArray);
		//
		// if (isErrorPacket(serverDataArray)) {
		// printFailConnection();
		// return false;
		// }

		sharedData.addClient();
		clientPortNum = server.getClientPort();
		System.out.println("Client(" + clientPortNum + ") login");

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
		file = new File(sharedData.getFilePathName() + "/Transactions/C"
				+ clientPortNum + ".txt");
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
		String s = new String(temp);
		s = s.toLowerCase();
		s = s.replaceAll("\\s", "");
		// System.out.println(s);
		if (s.contentEquals("begin") || s.contentEquals("starttransaction"))
			return true;
		else
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
		String s = new String(temp);
		s = s.toLowerCase();
		s = s.replaceAll("\\s", "");
		if (s.contentEquals("commit") || s.contentEquals("rollback"))
			return true;
		else
			return false;
	}

	private void printFailConnection() {
		System.out.println("client(" + clientPortNum + ") fails connection.");
	}

	private static void showClientData(ArrayList<Byte> array) {

		switch (array.get(4)) {
		case 1:
			System.out.print("client quit");
			break;
		case 2:
			System.out.print("select database: ");
			break;
		case 3:
			System.out.print("");
			break;
		case 4:
			System.out.print("list field: ");
			break;
		case 5:
			System.out.print("create database: ");
			break;
		case 6:
			System.out.print("drop database: ");
			break;
		default:
			break;

		}

		for (int i = 5; i < array.size(); ++i) {
			if (array.get(i) < (byte) 32) {
				// System.out.print(new String ("'"));
				// System.out.print((byte) b[i]);
				// System.out.print(new String ("'"));
				System.out.print(".");

			} else if (array.get(i) >= (byte) 32 && array.get(i) < (byte) 127)
				System.out.print((char) array.get(i).byteValue());
			else
				System.out.printf(" %02x ", array.get(i));
		}
		System.out.println();

	}

}
