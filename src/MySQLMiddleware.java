import java.util.Scanner;

public class MySQLMiddleware {

	public static void main(String[] args) {
		SharedData sharedData = new SharedData();
		int fileBufferSize = 1000000;

		sharedData.setMaxSize(1024);
		sharedData.setServerIpAddr("127.0.0.1");
		sharedData.setServerPortNum(3320);
		sharedData.setMiddlePortNum(3306);
		sharedData.setFilePathName(".");
		sharedData.setOutputToFile(true);
		sharedData.setFileOutputStream();

		MiddleServerSocket middleServerSock = new MiddleServerSocket(sharedData);
		middleServerSock.start();

		Scanner scanner = new Scanner(System.in);
		String s = "";

		System.out.println("Start");

		while (!sharedData.isEndOfProgram()) {
			s = scanner.nextLine();
			if (s.contentEquals("q")) {
				sharedData.setEndOfProgram(true);
			}
			if (s.contentEquals("o")) {
				sharedData.setOutputToFile(true);
			}
			if (s.contentEquals("c")) {
				sharedData.setOutputToFile(false);
			}
			if (s.contentEquals("f")) {
				sharedData.setOutputFlag(false);
			}
			if (s.contentEquals("t")) {
				sharedData.setOutputFlag(true);
			}

			if (s.contentEquals("p")) {
				sharedData.setClearClients(true);
			}
			// if (s.contentEquals("k")) {
			// sharedData.killAllUnits();
			// }

			if (s.charAt(0) == 's') {
				s = s.replace('s', ' ');
				s = s.trim();
				try {
					fileBufferSize = Integer.parseInt(s);
					System.out.println("Set file buffer size to "
							+ fileBufferSize);
				} catch (Exception e) {
					System.out.println("invalid input");
				}
			}

			if (sharedData.getFileBufferSize() >= fileBufferSize) {
				sharedData.flushOutput();
			}
		}

		sharedData.flushOutput();
		System.out.println("main end");

		return;
	}

}
