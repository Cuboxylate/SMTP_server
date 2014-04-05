/**
 * Class to deal with SMTP commands. When the user
 * wants to start an email, creates a MIMEParser 
 * object to parse the email itself.
 * Uses Simple Mail Transfer Protocol.
 *
 * Sebastian Dunn 2013
 */

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class SMTPParser implements Runnable {
	int messageNum;
	Socket clientSocket;
	BufferedReader incoming;
	DataOutputStream outgoing;

	String sender;
	ArrayList<String> recipients = new ArrayList<String>();

	/**
	 * Stores client socket and messageNum, sets up input and output streams.
	 * 
	 * @param clientSocket
	 * @param messageNum
	 */
	public SMTPParser(Socket clientSocket, int messageNum) {
		this.messageNum = messageNum;
		this.clientSocket = clientSocket;
		try {
			incoming = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			outgoing = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error setting up input or output stream for "
					+ messageNum + ".");
		}
	}

	/**
	 * Parses messages from input stream and calls the appropriate functions
	 */
	@Override
	public void run() {
		String data;

		try {
			/*
			 * Sends 220 to show connection is established. Also sends timestamp
			 * for good measure.
			 */
			outgoing.writeChars("220 Welcome! New connection initiated on "
					+ new java.util.Date().toString()
					+ ".\n\rWould you like to play a game?\n\r");

			/*
			 * Loops forever until a QUIT command is received
			 */
			while (true) {

				/*
				 * Read each line of data and call the appropriate function. If
				 * the command is not recognised/implemented, sends a 500
				 * message back.
				 */
				data = incoming.readLine();
				
				if (data.toLowerCase().startsWith("helo")) {
					receiveHelo(data);
				} else if (data.toLowerCase().startsWith("mail from:")) {
					receiveMailFrom(data);
				} else if (data.toLowerCase().startsWith("rcpt to:")) {
					receiveRcptTo(data);
				} else if (data.toLowerCase().startsWith("data")) {

				    /* When the user starts an email, check first that there is a sender and
				     * at least one recipient.
				     */
					if (sender == null) {
						try {
							outgoing.writeChars("503 Please give me a sender first! I can't read your mind O.o\n\r");
						} catch (IOException e) {
							System.out.println("IO error in stream " + messageNum
									+ " on Data (reject: no sender).");
						}
					}

					else if (recipients.size() == 0) {
						try {
							outgoing.writeChars("503 Come on, really, how can you give me a message without any recipients?\n\r");
						} catch (IOException e) {
							System.out.println("IO error in stream " + messageNum
									+ " on Data (reject: no recipients).");
						}
					}

                    /* Start the email by creating a MIMEParser object and handing it the
                     * incoming and outgoing data streams, then calling parse().
                     */
					else {
						try {
							outgoing.writeChars("354 Oh go on then, start the mail input. Just end it with a single '.' on a line by itself\n\r");
							MIMEParser email = new MIMEParser(incoming, outgoing, messageNum);
							email.parse();
                        }
                        catch (IOException e) {
                            System.out.println("IO error in MIME parser: " + e.getMessage());
				break;
                        }
                    }

				} else if (data.toLowerCase().startsWith("quit")) {
					receiveQuit();
					
					//Send error if command is unrecognised
				} else {
					outgoing.writeChars("500 I'm sorry, could you repeat that? I'm a little hard of hearing.\n\r");
				}

		}
		} catch (IOException e) {
			System.out.println("IO error in stream " + messageNum + ".");
		}
	}

	/**
	 * Function to handle HELO commands. Simply replies identifying the remote
	 * IP and the local IP.
	 * 
	 * @param data
	 */
	private void receiveHelo(String data) {
		try {
			outgoing.writeChars("250 Hello "
					+ clientSocket.getInetAddress().toString() + ", this is "
					+ clientSocket.getLocalAddress().toString()
					+ "! Lovely to meet you.\n\r");
		} catch (IOException e) {
			System.out
					.println("IO error in stream " + messageNum + " on HELO.");
		}
	}

	/**
	 * Function to handle MAIL FROM: commands
	 * 
	 * @param data
	 */
	private void receiveMailFrom(String data) {
		try {

			/*
			 * If the sender is already set, reject the new one and send back an
			 * error message
			 */
			if (sender != null) {
				outgoing.writeChars("503 You've already given me a sender, silly.\n\r");

			} else {
				/*
				 * Stores sender address after stripping away spaces and brackets
				 */
				sender = data.substring(10).replaceAll("[ <>]", "");
				if (!sender.matches("[a-zA-Z0-9._%+-]+@([a-zA-Z0-9.-])+usyd.edu.au")) {
					sender = null;
					outgoing.writeChars("500 That sender isn't from the University of Sydney! Try again...\n\r");
				} else {
				outgoing.writeChars("250 Happily received " + sender
						+ " as sender.\n\r");
				}
			}

		} catch (IOException e) {
			System.out.println("IO error in stream " + messageNum
					+ " on MailFrom.");
		}
	}

	/**
	 * Function to handle RCPT TO: commands. Strips spaces and brackets from recipient name, saves it to
	 * recipients list and responds with the appropriate message.
	 * 
	 * @param data
	 */
	private void receiveRcptTo(String data) {
		String recipient = data.substring(8).replaceAll("[ <>]", "");
		recipients.add(recipient);

		try {
			outgoing.writeChars("250 Added " + recipient
					+ " to list of recipients. What a lucky bugger!\n\r");
		} catch (IOException e) {
			System.out.println("IO error in stream " + messageNum
					+ " on RcptTo.");
		}
	}

	/**
	 * Funtion to quit communication. Send quit message, closes data streams and closes socket.
	 */
	private void receiveQuit() {
		try {
			outgoing.writeChars("221 Received your QUIT loud and clear! Terminating connection at "
					+ new java.util.Date().toString()
					+ ".\n\rTalk to you later.");
			incoming.close();
			outgoing.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("Unable to close connection for " + messageNum
					+ ".");
		}
	}
}
