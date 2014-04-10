import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MiddlewareUnit extends Thread {

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
	private int traxNum;
	private long traxStart;
	private long traxEnd;
	private boolean inTrax;
	private boolean autoCommit;

	// private int latency;
	private long sendTime;
	private long recTime;
	private Calendar cal;
	private Date date;

	private File file;
	private FileOutputStream fileOutputStream;
	private PrintWriter printWriter;

	private int clientID;

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
		traxNum = 0;
		inTrax = false;
		autoCommit = true;

		sendTime = 0;
		recTime = 0;
		cal = new GregorianCalendar();
		date = new Date();

		file = null;
		fileOutputStream = null;
		printWriter = null;
	}

	public void run() {

		while (!sharedData.isEndOfProgram() && !sharedData.isClearClients()) {
			if (!server.isConnected()) {
				break;
			}

			// System.out.println("ttttt");
			if (server.hasInput()) {

				// System.out.println("ttttt");
				clientDataArray.clear();

				// System.out.println("before read");
				getClientData();

				if (sharedData.isEndOfProgram()) {
					break;
				}

				checkAutoCommit();
				if (sharedData.isOutputToFile() && !inTrax && traxBegin()) {
					inTrax = true;
					traxStart = System.currentTimeMillis();
					// System.out.println("Transaction begins.");

				}

				recTime = 0;
				serverDataArray.clear();

				if (sharedData.isOutputFlag())
					showClientData(clientDataArray);

				client.sendOutput(clientDataArray);
				sendTime = System.currentTimeMillis();
			}

			if (clientQuit())
				break;

			if (client.hasInput()) {
				getServerData();

				if (serverDataArray.size() == 0) {
					break;
				}

				if (inTrax) {
					// System.out.print("Client: ");
					// showClientData(clientDataArray);
					addSQLToTrax();
					if (traxEnd()) {
						inTrax = false;
						traxEnd = System.currentTimeMillis();
						// System.out.println("Transaction ends.");

						printTrax();
						trax.clear();
					}
				}

				// System.out.print("Server: ");
				// showData(serverData, serverDataLen);

				server.sendOutput(serverDataArray);
			}

		}
		server.close();
		client.close();

		if (printWriter != null) {
			printWriter.close();
		}

		if (printWriter != null)
			printWriter.flush();
		System.out.println("Client(" + clientPortNum + ") quit");

	}

	private boolean clientQuit() {
		if (clientDataArray.size() < 5)
			return false;
		if (clientDataArray.get(4).byteValue() == (byte) 1)
			return true;
		return false;
	}

	private void checkAutoCommit() {
		if (clientDataArray.size() < 6)
			return;
		byte[] temp = new byte[clientDataArray.size() - 5];
		for (int i = 5; i < clientDataArray.size(); ++i) {
			temp[i - 5] = clientDataArray.get(i).byteValue();
		}
		String s = new String(temp);
		s = s.toLowerCase();
		s = s.replaceAll("\\s", "");
		if (s.contentEquals("setautocommit=0"))
			autoCommit = false;
		if (s.contentEquals("setautocommit=1"))
			autoCommit = true;

	}

	private void getServerData() {
		do {
			serverDataLen = client.getInput(serverData);
			if (recTime == 0)
				recTime = System.currentTimeMillis();

			addToList(serverDataArray, serverData, serverDataLen);

			// System.out.println("server " + serverDataLen);
			// System.out.print("server: ");
			// showData(serverData, serverDataLen);
		} while (client.hasInput());

	}

	private void getClientData() {
		do {
			clientDataLen = server.getInput(clientData);
			addToList(clientDataArray, clientData, clientDataLen);

			// System.out.println("client " + clientDataLen);
			// System.out.print("client: ");
			// showData(clientData, clientDataLen);
		} while (server.hasInput());
	}

	synchronized public boolean setUp(Socket socket) {
		server.startServer(socket);
		client.startClient();

		serverDataLen = client.getInput(serverData);
		if (sharedData.isOutputFlag()) {
			System.out.println("s");
			serverDataArray.clear();
			showData(serverData, serverDataLen);
		}
		addToList(serverDataArray, serverData, serverDataLen);
		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);

		if (sharedData.isOutputFlag()) {
			System.out.println("c");
			showData(clientData, clientDataLen);
			System.out.println("send client info");
		}

		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);
		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);

		if (sharedData.isOutputFlag()) {
			System.out.println("s");
			showData(serverData, serverDataLen);
		}

		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);
		if (sharedData.isOutputFlag())
			System.out.println("get server OK packet");

		server.sendOutput(serverDataArray);

		if (isErrorPacket(serverDataArray)) {
			printFailConnection();
			return false;
		}

		sharedData.addClient();
		clientPortNum = server.getClientPort();
		clientID = sharedData.getNumClient();
		try {
			setFileOutputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
		if (array.size() < 5)
			return false;
		if (array.get(4).byteValue() == (byte) 255)
			return true;

		return false;
	}

	private static void showData(byte[] b, int len) {
		System.out.print(len + " ");
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
		String s = "";
		s += "Statement ID: ";
		s += trax.size() / 2 + 1;
		s += "   Start: ";
		s += getTimeString(sendTime);
		s += "   End: ";
		s += getTimeString(recTime);
		trax.add(s);
		s = "";

		for (int i = 5; i < clientDataArray.size(); ++i) {
			if (clientDataArray.get(i) < (byte) 32) {
				s += '.';

			} else {
				s += (char) clientDataArray.get(i).byteValue();
			}

		}
		trax.add(s);
	}

	private String getTimeString(long t) {
		date.setTime(t);
		cal.setTime(date);
		String s = "";

		s += cal.get(Calendar.YEAR);
		s += '-';
		s += cal.get(Calendar.MONTH) + 1;
		s += '-';
		s += cal.get(Calendar.DATE);
		s += ' ';
		s += cal.get(Calendar.HOUR);
		s += ':';
		s += cal.get(Calendar.MINUTE);
		s += ':';
		s += cal.get(Calendar.SECOND);
		s += ':';
		s += cal.get(Calendar.MILLISECOND);

		return s;
	}

	private void setFileOutputStream() throws FileNotFoundException {
		file = new File(sharedData.getFilePathName() + "/Transactions/C"
				+ clientPortNum + ".txt");
		try {
			fileOutputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		printWriter = new PrintWriter(fileOutputStream, false);
	}

	private void printTrax() {
		++traxNum;
		String s = "Client ID: " + clientID + "   Transaction ID: " + traxNum
				+ "   Start: " + getTimeString(traxStart) + "   End: "
				+ getTimeString(traxEnd) + "   Latency: "
				+ (traxEnd - traxStart) + " ms";
		printWriter.println(s);
		sharedData.printTrax(s);
		for (int i = 0; i < trax.size(); ++i) {
			printWriter.println(trax.get(i));
			sharedData.printTrax(trax.get(i));
			// System.out.println(trax.get(i));
		}
		printWriter.println();
		sharedData.printTrax("");
		sharedData.addFileBufferSize(trax.size() / 2);

	}

	private boolean traxBegin() {
		if (!autoCommit)
			return true;
		if (clientDataArray.size() < 6)
			return false;
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
		if (clientDataArray.size() < 6)
			return false;
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
