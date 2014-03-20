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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Socket firstSock = null;
		try {
			firstSock = serverSock.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addConnection(firstSock);

		// MiddleClient midClient = new MiddleClient("127.0.0.1", 3320);
		// midClient.startClient();

		// serverDataLen = midClient.getInput(serverData);
		// showData(serverData, serverDataLen);
		// addToList(serverDataArray, serverData, serverDataLen);

		// midClient.reconnect();
		//
		// serverDataLen = midClient.getInput(serverData);
		// showData(serverData, serverDataLen);
		// addToList(serverDataArray, serverData, serverDataLen);
		//
		// if (1 == 1 ) return;

		// MiddleServer midServer = new MiddleServer("127.0.0.1", 3306);
		// midServer.startServer();

		// midServer.sendOutput(serverDataArray);
		//
		// clientDataLen = midServer.getInput(clientData);
		// showData(clientData, clientDataLen);
		// addToList(clientDataArray, clientData, clientDataLen);
		//
		// System.out.println("send client info");
		// midClient.sendOutput(clientDataArray);
		//
		// serverDataLen = midClient.getInput(serverData);
		// showData(serverData, serverDataLen);
		// serverDataArray.clear();
		// addToList(serverDataArray, serverData, serverDataLen);
		//
		// System.out.println("get server empty packet");
		//
		// midServer.sendOutput(serverDataArray);
		//
		// clientDataLen = midServer.getInput(clientData);
		// clientDataArray.clear();
		// addToList(clientDataArray, clientData, clientDataLen);
		// showData(clientData, clientDataLen);
		//
		// System.out.println("111111111111111111");
		//
		// midClient.sendOutput(clientDataArray);
		//
		// serverDataLen = midClient.getInput(serverData);
		// serverDataArray.clear();
		// addToList(serverDataArray, serverData, serverDataLen);
		// showData(serverData, serverDataLen);
		//
		// System.out.println("2222222222222222222222222");
		//
		// midServer.sendOutput(serverDataArray);
		//

		try {
			serverSock.setSoTimeout(10);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while (servers.size() > 0) {

			Socket newSock = null;
			
			try {
				newSock = serverSock.accept();
			} catch (SocketTimeoutException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			if (newSock != null) {
				addConnection(newSock);
			}
			
			for (int i = 0; i < servers.size(); ++i) {
				while (servers.get(i).hasInput()) {
					try {
						run(i);
					} catch (inputException e) {
						System.out.println("catch inpout exception");
						System.out.println("remove");
						removeConnection(i);
						--i;

					}
				}
			}

			// clientDataArray.clear();
			// do {
			// clientDataLen = servers.get(0).getInput(clientData);
			// addToList(clientDataArray, clientData, clientDataLen);
			// } while (clientDataLen == maxSize);
			// if (clientDataArray.size() == 0)
			// break;
			//
			// if (traxBegin(clientDataArray)) {
			// inTrax = true;
			// System.out.println("Transaction begins.");
			// }
			//
			// if (inTrax) {
			// System.out.print("Client: ");
			// showClientData(clientDataArray);
			// }
			//
			// if (inTrax && traxEnd(clientDataArray)) {
			// inTrax = false;
			// System.out.println("Transaction ends.");
			// }
			//
			// recTime = 0;
			// serverDataArray.clear();
			// clients.get(0).sendOutput(clientDataArray);
			// sendTime = System.currentTimeMillis();
			//
			// do {
			// serverDataLen = clients.get(0).getInput(serverData);
			// if (recTime == 0)
			// recTime = System.currentTimeMillis();
			//
			// addToList(serverDataArray, serverData, serverDataLen);
			//
			// } while (serverDataLen == maxSize);
			//
			// if (serverDataArray.size() == 0)
			// break;
			//
			// if (inTrax) {
			// System.out.print("Latency: ");
			// System.out.print(recTime - sendTime);
			// System.out.println(" ms");
			// }

			// System.out.print("Server: ");
			// showData(serverData, serverDataLen);
			//
			// servers.get(0).sendOutput(serverDataArray);

		}

		System.out.println("END");

	}

	private static void removeConnection(int i) {
		// TODO Auto-generated method stub
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

	@SuppressWarnings("null")
	private static void run(int i) throws inputException {
		// TODO Auto-generated method stub
		clientDataArray.clear();
		do {
			clientDataLen = servers.get(i).getInput(clientData);
			addToList(clientDataArray, clientData, clientDataLen);
		} while (clientDataLen == maxSize);
		if (clientDataArray.size() == 0) {
			inputException e = null;
			throw e;
		}

		if (traxBegin(clientDataArray)) {
			servers.get(i).setInTrax(true);
			System.out.println("Transaction begins.");
		}

		if (servers.get(i).isInTrax()) {
			System.out.print("Client: ");
			showClientData(clientDataArray);
		}

		if (servers.get(i).isInTrax() && traxEnd(clientDataArray)) {
			servers.get(i).setInTrax(false);
			System.out.println("Transaction ends.");
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
			System.out.print("Latency: ");
			System.out.print(recTime - sendTime);
			System.out.println(" ms");
		}

		// System.out.print("Server: ");
		// showData(serverData, serverDataLen);

		servers.get(i).sendOutput(serverDataArray);

	}

	private static void addConnection(Socket socket) {
		// TODO Auto-generated method stub

		MiddleServer server = new MiddleServer();
		server.startServer(socket);

		MiddleClient client = new MiddleClient("127.0.0.1", 3320);
		client.startClient();

		serverDataLen = client.getInput(serverData);
		showData(serverData, serverDataLen);
		addToList(serverDataArray, serverData, serverDataLen);
		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);
		showData(clientData, clientDataLen);
		addToList(clientDataArray, clientData, clientDataLen);

		System.out.println("send client info");
		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);
		showData(serverData, serverDataLen);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);

		System.out.println("get server empty packet");

		server.sendOutput(serverDataArray);

		clientDataLen = server.getInput(clientData);
		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);
		showData(clientData, clientDataLen);

		System.out.println("111111111111111111");

		client.sendOutput(clientDataArray);

		serverDataLen = client.getInput(serverData);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);
		showData(serverData, serverDataLen);

		System.out.println("2222222222222222222222222");

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
