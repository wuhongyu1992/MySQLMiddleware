import java.util.Scanner;

public class MySQLMiddleware {

	public static void main(String[] args) {
		SharedData sharedData = new SharedData();

		sharedData.setMaxSize(1024);
		sharedData.setServerIpAddr("127.0.0.1");
		sharedData.setServerPortNum(3320);
		sharedData.setMiddlePortNum(3306);
		sharedData.setFilePathName(".");
		sharedData.setOutputToFile(true);

		MiddleServerSocket middleServerSock = new MiddleServerSocket(sharedData);
		middleServerSock.start();

		Scanner scanner = new Scanner(System.in);
		String s = "";
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
		}

		System.out.println("main end");

		return;
	}

}
