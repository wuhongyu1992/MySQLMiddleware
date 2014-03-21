import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class MySQLMiddleware {

	final static int maxSize = 8192;

	static ArrayList<MiddleServer> servers = new ArrayList<MiddleServer>();
	static ArrayList<MiddleClient> clients = new ArrayList<MiddleClient>();

	static ArrayList<Byte> clientDataArray = new ArrayList<Byte>();
	static ArrayList<Byte> serverDataArray = new ArrayList<Byte>();

	static byte[] clientData = new byte[maxSize];
	static int clientDataLen = 0;

	static byte[] serverData = new byte[maxSize];
	static int serverDataLen = 0;

	static long sendTime, recTime;

	public static void main(String[] args) {

		ServerSocket serverSock = null;
		try {
			serverSock = new ServerSocket(3306);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Socket firstSock = null;
		try {
			firstSock = serverSock.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		addConnection(firstSock);

		try {
			serverSock.setSoTimeout(10);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		while (servers.size() > 0) {

			Socket newSock = null;

			try {
				newSock = serverSock.accept();
			} catch (SocketTimeoutException e1) {
				// e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			if (newSock != null) {
				addConnection(newSock);

				// System.out.println(servers.size());
			}

			for (int i = 0; i < servers.size(); ++i) {
				if (!servers.get(i).isConnected()) {
					removeConnection(i);
					--i;
					continue;
				}

				if (servers.get(i).hasInput()) {
					try {
						run(i);
					} catch (inputException e) {
						removeConnection(i);
						// System.out.println(servers.size());
						--i;
					}
				}
			}

		}

		System.out.println("END");

	}

	private static void removeConnection(int i) {
		servers.get(i).close();
		clients.get(i).close();

		servers.remove(i);
		clients.remove(i);
	}

	@SuppressWarnings("serial")
	private static class inputException extends Exception {
		public inputException() {

		}
	}

	private static void run(int i) throws inputException {

		clientDataArray.clear();

		do {
			clientDataLen = servers.get(i).getInput(clientData);
			addToList(clientDataArray, clientData, clientDataLen);
		} while (clientDataLen == maxSize);

		// if (clientDataArray.size() == 0) {
		// inputException e = null;
		// throw e;
		// }

		if (traxBegin(clientDataArray)) {
			servers.get(i).setInTrax(true);
			// System.out.println("Transaction begins.");

			servers.get(i).setLatency(0);

			servers.get(i).clearTrax();

		}

		if (servers.get(i).isInTrax()) {
			// System.out.print("Client: ");
			// showClientData(clientDataArray);
			String s = getSQL(clientDataArray);
			servers.get(i).addToTrax(s);
		}

		recTime = 0;
		serverDataArray.clear();
		clients.get(i).sendOutput(clientDataArray);
		sendTime = System.currentTimeMillis();

		do {
			serverDataLen = clients.get(i).getInput(serverData);
			if (recTime == 0)
				recTime = System.currentTimeMillis();

			addToList(serverDataArray, serverData, serverDataLen);

		} while (serverDataLen == maxSize);

		if (serverDataArray.size() == 0) {
			inputException e = new inputException();
			throw e;
		}

		if (servers.get(i).isInTrax()) {
			// System.out.print("Latency: ");
			// System.out.print(recTime - sendTime);
			// System.out.println(" ms");
			servers.get(i).addLatency(recTime - sendTime);
		}

		if (servers.get(i).isInTrax() && traxEnd(clientDataArray)) {
			servers.get(i).setInTrax(false);
			// System.out.println("Transaction ends.");

			servers.get(i).printTrax();
		}

		// System.out.print("Server: ");
		// showData(serverData, serverDataLen);

		servers.get(i).sendOutput(serverDataArray);

	}

	private static String getSQL(ArrayList<Byte> array) {
		String s = new String();

		for (int i = 5; i < array.size(); ++i) {
			if (array.get(i) < (byte) 32) {
				s += '.';

			} else {
				s += (char) array.get(i).byteValue();
			}

		}

		return s;
	}

	private static void addConnection(Socket socket) {

		MiddleServer server = new MiddleServer();
		server.startServer(socket);

		MiddleClient client = new MiddleClient("127.0.0.1", 3320);
		client.startClient();

		serverDataLen = client.getInput(serverData);

		// System.out.println("s");

		serverDataArray.clear();

		// showData(serverData, serverDataLen);
		addToList(serverDataArray, serverData, serverDataLen);
		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);

		// System.out.println("c");

		clientDataArray.clear();

		// showData(clientData, clientDataLen);
		addToList(clientDataArray, clientData, clientDataLen);

		// System.out.println("send client info");
		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);

		// System.out.println("s");

		// showData(serverData, serverDataLen);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);

		// System.out.println("get server empty packet");

		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);
		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);

		// System.out.println("c");

		// showData(clientData, clientDataLen);

		// System.out.println("111111111111111111");

		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);

		// System.out.println("s");

		// showData(serverData, serverDataLen);

		// System.out.println("2222222222222222222222222");

		server.sendOutput(serverDataArray);

		servers.add(server);
		clients.add(client);

	}

	private static boolean traxEnd(ArrayList<Byte> array) {
		byte[] temp = new byte[array.size() - 5];
		for (int i = 5; i < array.size(); ++i) {
			temp[i - 5] = array.get(i).byteValue();
		}
		if (array.size() == 11) {
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
		if (array.size() == 13) {
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

	private static boolean traxBegin(ArrayList<Byte> array) {
		byte[] temp = new byte[array.size() - 5];
		for (int i = 5; i < array.size(); ++i) {
			temp[i - 5] = array.get(i).byteValue();
		}
		if (array.size() == 10) {
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

	private static void addToList(ArrayList<Byte> dataArray, byte[] data,
			int len) {
		for (int i = 0; i < len; ++i) {
			dataArray.add(data[i]);
		}
	}

	public static void showData(byte[] b, int len) {
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

	public static void showClientData(ArrayList<Byte> array) {

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
