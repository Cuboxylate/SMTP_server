/**
 * Main method loops infinitely listening for a 
 * socket connection. When one is detected, forks
 * a new process to deal with it.
 *
 * Sebastian Dunn 2013
 */

import java.net.*;
import java.io.*;

public class MySMTPServer {
	static int messageNum = 0;
	
	public static void main (String[] args) {
		try {
			int serverPort = 6013;
			ServerSocket sSocket = new ServerSocket(serverPort);
			
			while (true) {
				
				//blocks here until a connection is incoming
				Socket clientSocket = sSocket.accept();
				
				//Create new MessageBank object to handle client and pass it to a new thread
				SMTPParser m = new SMTPParser(clientSocket, messageNum);
				new Thread(m).start();
				messageNum++;
			}
			
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}
}
