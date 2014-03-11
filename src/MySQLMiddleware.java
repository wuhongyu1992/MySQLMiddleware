
public class MySQLMiddleware {

	final static int maxSize = 8192;

	public static void main(String[] args) {

		MiddleClient midClient = new MiddleClient("127.0.0.1", 3320);
		midClient.startClient();
		byte[] clientData = new byte[maxSize];
		int clientDataLen = 0;

		byte[] serverData = new byte[maxSize];
		int serverDataLen = 0;

		serverDataLen = midClient.getInput(serverData);
		showData(serverData, serverDataLen);

		MiddleServer midServer = new MiddleServer("127.0.0.1", 3306);
		midServer.startServer();

		midServer.sendOutput(serverData, serverDataLen);

		clientDataLen = midServer.getInput(clientData);
		showData(clientData, clientDataLen);

		System.out.println("send client info");
		midClient.sendOutput(clientData, clientDataLen);

		serverDataLen = midClient.getInput(serverData);
		showData(serverData, serverDataLen);
		System.out.println("get server empty packet");

		midServer.sendOutput(serverData, serverDataLen);

		clientDataLen = midServer.getInput(clientData);

		midClient.sendOutput(clientData, clientDataLen);

		serverDataLen = midClient.getInput(serverData);

		midServer.sendOutput(serverData, serverDataLen);

		while (true) {
			clientDataLen = midServer.getInput(clientData);
			System.out.print("Client: ");
			if (clientDataLen < 0)
				break;
			showClientData(clientData, clientDataLen);
			
			midClient.sendOutput(clientData, clientDataLen);
			long sendTime = System.currentTimeMillis();
			
			serverDataLen = midClient.getInput(serverData);
			long recTime = System.currentTimeMillis();
			
			if (serverDataLen < 0)
				break;
			System.out.print("Latency: ");
			System.out.print(recTime - sendTime);
			System.out.println(" ms");

			System.out.print("Server: ");
			showData(serverData, serverDataLen);
			
			midServer.sendOutput(serverData, serverDataLen);

		}

		 System.out.println("bye!");
		
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
		for (int i = 5; i < len; ++ i) {
			if (b[i] < (byte) 32) {
//				System.out.print(new String ("'"));
//				System.out.print((byte) b[i]);
//				System.out.print(new String ("'"));
				System.out.print(".");
				
			}
			else if (b[i] >= (byte) 32 && b[i] < (byte) 127)
				System.out.print((char) b[i]);
			else
				System.out.printf(" %02x ", b[i]);
		}
		System.out.println();
 
	}


	public static void showClientData(byte[] b, int len) {
		
		switch (b[4]) {
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
				return;
				
		}

		
		for (int i = 5; i < len; ++ i) {
			if (b[i] < (byte) 32) {
//				System.out.print(new String ("'"));
//				System.out.print((byte) b[i]);
//				System.out.print(new String ("'"));
				System.out.print(".");
				
			}
			else if (b[i] >= (byte) 32 && b[i] < (byte) 127)
				System.out.print((char) b[i]);
			else
				System.out.printf(" %02x ", b[i]);
		}
		System.out.println();
 
	}
	
}
