import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SharedData {
	private int maxSize;
	private int numClient;
	private String serverIpAddr;
	private int serverPortNum;
	private int middlePortNum;
	private boolean endOfProgram;
	private boolean outputToFile;
	private String filePathName;
	private File file;
	private FileOutputStream fileOutputStream;
	private PrintWriter printWriter;
	private int fileBufferSize;

	SharedData() {
		maxSize = 1024;
		numClient = 0;
		endOfProgram = false;
		outputToFile = false;
		filePathName = null;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getNumClient() {
		return numClient;
	}

	public void addClient() {
		++numClient;
	}

	public void subClient() {
		--numClient;
	}

	public int getMiddlePortNum() {
		return middlePortNum;
	}

	public void setMiddlePortNum(int middlePortNum) {
		this.middlePortNum = middlePortNum;
	}

	public int getServerPortNum() {
		return serverPortNum;
	}

	public void setServerPortNum(int serverPortNum) {
		this.serverPortNum = serverPortNum;
	}

	public String getServerIpAddr() {
		return serverIpAddr;
	}

	public void setServerIpAddr(String serverIpAddr) {
		this.serverIpAddr = serverIpAddr;
	}

	public boolean isEndOfProgram() {
		return endOfProgram;
	}

	public void setEndOfProgram(boolean endOfProgram) {
		this.endOfProgram = endOfProgram;
	}

	public boolean isOutputToFile() {
		return outputToFile;
	}

	public void setOutputToFile(boolean outputToFile) {
		this.outputToFile = outputToFile;
	}

	public String getFilePathName() {
		return filePathName;
	}

	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}

	public int getFileBufferSize() {
		return fileBufferSize;
	}

	public void setFileBufferSize(int fileBufferSize) {
		this.fileBufferSize = fileBufferSize;
	}

}
