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

	private MiddleServer middleServer;
	private MiddleClient middleClient;
	private SharedData sharedData;
	private int maxSize;
	private int clientPortNum;

	private byte[] clientData;
	private int clientDataLen;

	private byte[] serverData;
	private int serverDataLen;

	// private ArrayList<Byte> clientDataArray;
	// private ArrayList<Byte> serverDataArray;

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

		middleServer = new MiddleServer();
		middleClient = new MiddleClient(sharedData.getServerIpAddr(),
				sharedData.getServerPortNum());

		clientData = new byte[maxSize];
		clientDataLen = 0;

		serverData = new byte[maxSize];
		serverDataLen = 0;

		// clientDataArray = new ArrayList<Byte>();
		// serverDataArray = new ArrayList<Byte>();

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
			if (!middleServer.isConnected()) {
				break;
			}

			// System.out.println("ttttt");
			if (middleServer.hasInput()) {

				// System.out.println("ttttt");
				// clientDataArray.clear();

				// System.out.println("before read");
//				getClientData();
				clientDataLen = middleServer.getInput(clientData);
				//
				// if (sharedData.isEndOfProgram()) {
				// break;
				// }

				checkAutoCommit();
				if (sharedData.isOutputToFile() && !inTrax && traxBegin()) {
					inTrax = true;
					traxStart = System.currentTimeMillis();
					// System.out.println("Transaction begins.");

				}

				recTime = 0;

				// if (sharedData.isOutputFlag())
				// showClientData(clientDataArray);

				// middleClient.sendOutput(clientDataArray);
				middleClient.sendOutput(clientData, clientDataLen);
				sendTime = System.currentTimeMillis();
				if (clientQuit())
					break;
			}

			if (middleClient.hasInput()) {
				// serverDataArray.clear();
				// getServerData();
				//
				// if (serverDataArray.size() == 0) {
				// break;
				// }

				if (inTrax) {
					// System.out.print("middleClient: ");
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

				serverDataLen = middleClient.getInput(serverData);
				middleServer.sendOutput(serverData, serverDataLen);

				// middleServer.sendOutput(serverDataArray);
			}

		}
		middleServer.close();
		middleClient.close();

		if (printWriter != null) {
			printWriter.close();
		}

		if (printWriter != null)
			printWriter.flush();
		System.out.println("client(" + clientPortNum + ") quit");

	}

	private boolean clientQuit() {
		if (clientDataLen < 5)
			return false;
		if (clientData[4] == (byte) 1)
			return true;
		return false;
	}

	//
	// private void getServerData() {
	// do {
	// serverDataLen = middleClient.getInput(serverData);
	// if (recTime == 0)
	// recTime = System.currentTimeMillis();
	//
	// addToList(serverDataArray, serverData, serverDataLen);
	//
	// // System.out.println("middleServer " + serverDataLen);
	// if (sharedData.isOutputFlag()) {
	// System.out.print("server: ");
	// showData(serverData, serverDataLen);
	// }
	// } while (middleClient.hasInput());
	//
	// }
	//
	// private void getClientData() {
	// do {
	// clientDataLen = middleServer.getInput(clientData);
	// addToList(clientDataArray, clientData, clientDataLen);
	//
	// // System.out.println("middleClient " + clientDataLen);
	// // System.out.print("middleClient: ");
	// // showData(clientData, clientDataLen);
	// } while (middleServer.hasInput());
	// }

	synchronized public boolean setUp(Socket socket) {
		middleServer.startServer(socket);
		middleClient.startClient();

		serverDataLen = middleClient.getInput(serverData);
		if (sharedData.isOutputFlag()) {
			System.out.println("s");
			showData(serverData, serverDataLen);
		}
		middleServer.sendOutput(serverData, serverDataLen);

		clientDataLen = middleServer.getInput(clientData);

		if (sharedData.isOutputFlag()) {
			System.out.println("c");
			showData(clientData, clientDataLen);
			System.out.println("send client info");
		}

		middleClient.sendOutput(clientData, clientDataLen);

		serverDataLen = middleClient.getInput(serverData);

		if (sharedData.isOutputFlag()) {
			System.out.println("s");
			showData(serverData, serverDataLen);
		}

		if (sharedData.isOutputFlag())
			System.out.println("get server OK packet");

		middleServer.sendOutput(serverData, serverDataLen);

		if (isErrorPacket(serverData, serverDataLen)) {
			printFailConnection();
			return false;
		}

		sharedData.addClient();
		clientPortNum = middleServer.getClientPort();
		clientID = sharedData.getNumClient();
		try {
			setFileOutputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("client(" + clientPortNum + ") login");

		return true;
	}

	// private void addToList(ArrayList<Byte> dataArray, byte[] data,
	// int len) {
	// for (int i = 0; i < len; ++i) {
	// dataArray.add(data[i]);
	// }
	// }

	private boolean isErrorPacket(byte[] b, int len) {
		if (len < 5)
			return false;
		if (b[4] == (byte) 255)
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
		StringBuilder sb = new StringBuilder();

		sb.append("Statement ID: ");
		sb.append(trax.size() / 2 + 1);
		sb.append("   Start: ");
		sb.append(getTimeString(sendTime));
		sb.append("   End: ");
		sb.append(getTimeString(recTime));
		trax.add(sb.toString());

		sb = new StringBuilder(clientDataLen - 5);
		for (int i = 5; i < clientDataLen; ++i) {
			if (clientData[i] < (byte) 32) {
				sb.append('.');

			} else {
				sb.append((char) clientData[i]);
			}

		}
		trax.add(sb.toString());
	}

	private String getTimeString(long t) {
		date.setTime(t);
		cal.setTime(date);
		String s = "";

		s += cal.get(Calendar.YEAR) + '-' + cal.get(Calendar.MONTH) + 1 + '-'
				+ cal.get(Calendar.DATE) + ' ' + cal.get(Calendar.HOUR) + ':'
				+ cal.get(Calendar.MINUTE) + ':' + cal.get(Calendar.SECOND)
				+ ',' + cal.get(Calendar.MILLISECOND);

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

	private void checkAutoCommit() {
		if (clientDataLen < 6)
			return;

		String s = new String(clientData, 5, clientDataLen - 5);
		s = s.toLowerCase();
		s = s.replaceAll("\\s", "");
		if (s.contentEquals("setautocommit=0"))
			autoCommit = false;
		if (s.contentEquals("setautocommit=1"))
			autoCommit = true;

	}

	private boolean traxBegin() {
		if (!autoCommit)
			return true;
		if (clientDataLen < 6)
			return false;

		String s = new String(clientData, 5, clientDataLen - 5);
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
		if (clientDataLen < 6)
			return false;
		String s = new String(clientData, 5, clientDataLen - 5);

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

//	private void showClientData(ArrayList<Byte> array) {
//
//		switch (array.get(4)) {
//		case 1:
//			System.out.print("client quit");
//			break;
//		case 2:
//			System.out.print("select database: ");
//			break;
//		case 3:
//			System.out.print("");
//			break;
//		case 4:
//			System.out.print("list field: ");
//			break;
//		case 5:
//			System.out.print("create database: ");
//			break;
//		case 6:
//			System.out.print("drop database: ");
//			break;
//		default:
//			break;
//
//		}
//
//		for (int i = 5; i < array.size(); ++i) {
//			if (array.get(i) < (byte) 32) {
//				// System.out.print(new String ("'"));
//				// System.out.print((byte) b[i]);
//				// System.out.print(new String ("'"));
//				System.out.print(".");
//
//			} else if (array.get(i) >= (byte) 32 && array.get(i) < (byte) 127)
//				System.out.print((char) array.get(i).byteValue());
//			else
//				System.out.printf(" %02x ", array.get(i));
//		}
//		System.out.println();
//
//	}

}
