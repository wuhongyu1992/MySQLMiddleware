import java.util.ArrayList;

public class MySQLMiddleware {

	final static int maxSize = 8192;

	public static void main(String[] args) {
		ArrayList<Byte> clientDataArray = new ArrayList<Byte>();
		ArrayList<Byte> serverDataArray = new ArrayList<Byte>();

		byte[] clientData = new byte[maxSize];
		int clientDataLen = 0;

		byte[] serverData = new byte[maxSize];
		int serverDataLen = 0;
		
		MiddleClient midClient = new MiddleClient("127.0.0.1", 3320);
		midClient.startClient();

		serverDataLen = midClient.getInput(serverData);
		showData(serverData, serverDataLen);
		addToList(serverDataArray, serverData, serverDataLen);

//		midClient.reconnect();
//
//		serverDataLen = midClient.getInput(serverData);
//		showData(serverData, serverDataLen);
//		addToList(serverDataArray, serverData, serverDataLen);
//		
//		if (1 == 1 ) return;
		
		MiddleServer midServer = new MiddleServer("127.0.0.1", 3306);
		midServer.startServer();

		midServer.sendOutput(serverDataArray);

		clientDataLen = midServer.getInput(clientData);
		showData(clientData, clientDataLen);
		addToList(clientDataArray, clientData, clientDataLen);

		System.out.println("send client info");
		midClient.sendOutput(clientDataArray);

		serverDataLen = midClient.getInput(serverData);
		showData(serverData, serverDataLen);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);

		System.out.println("get server empty packet");

		midServer.sendOutput(serverDataArray);

		clientDataLen = midServer.getInput(clientData);
		clientDataArray.clear();
		addToList(clientDataArray, clientData, clientDataLen);
		showData(clientData, clientDataLen);

		System.out.println("111111111111111111");

		midClient.sendOutput(clientDataArray);

		serverDataLen = midClient.getInput(serverData);
		serverDataArray.clear();
		addToList(serverDataArray, serverData, serverDataLen);
		showData(serverData, serverDataLen);

		System.out.println("2222222222222222222222222");

		midServer.sendOutput(serverDataArray);

		long sendTime, recTime;
		boolean inTrax = false;
		while (true) {

			clientDataArray.clear();
			do {
				clientDataLen = midServer.getInput(clientData);
				addToList(clientDataArray, clientData, clientDataLen);
			} while (clientDataLen == maxSize);
			if (clientDataArray.size() == 0)
				break;

			if (traxBegin(clientDataArray)) {
				inTrax = true;
				System.out.println("Transaction begins.");
			}
			
			if (inTrax) {
				System.out.print("Client: ");
				showClientData(clientDataArray);
			}
			
			if (inTrax && traxEnd(clientDataArray)) {
				inTrax = false;
				System.out.println("Transaction ends.");
			}
			
			recTime = 0;
			serverDataArray.clear();
			midClient.sendOutput(clientDataArray);
			sendTime = System.currentTimeMillis();

			do {
				serverDataLen = midClient.getInput(serverData);
				if (recTime == 0)
					recTime = System.currentTimeMillis();

				addToList(serverDataArray, serverData, serverDataLen);

			} while (serverDataLen == maxSize);

			if (serverDataArray.size() == 0)
				break;
			
			if (inTrax) {
				System.out.print("Latency: ");
				System.out.print(recTime - sendTime);
				System.out.println(" ms");				
			}

//			System.out.print("Server: ");
//			showData(serverData, serverDataLen);

			midServer.sendOutput(serverDataArray);

		}

		System.out.println("END");

	}

	private static boolean traxEnd(ArrayList<Byte> array) {
		byte[] temp = new byte[array.size() - 5];
		for (int i = 5; i < array.size(); ++i) {
			temp[i-5] = array.get(i).byteValue();
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
			temp[i-5] = array.get(i).byteValue();
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
