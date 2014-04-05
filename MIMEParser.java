/**
 * Interprets commands for creating an email, and
 * saves the result in the included /emails
 * folder.
 *
 * Sebastian Dunn 2013
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MIMEParser {
    private final BufferedReader incoming;
    private final DataOutputStream outgoing;

    private final int messageNum;
	private String version = null;
	private String date = null;
	private String mimeSender = null;
	private String mimeRecipients = null;
	private String subject = null;
	private String content = null;
	private ArrayList<String> body = new ArrayList<String>();
	
	public MIMEParser(BufferedReader incoming, DataOutputStream outgoing, int messageNum) {
	    this.incoming = incoming;
	    this.outgoing = outgoing;
	    this.messageNum = messageNum;
	}
	
	public void parse() {
			String nextline;
            		try {
				// will loop until it reads a "."
				while (true) {
					nextline = incoming.readLine();

					//save version, stripping away preceding or succeeding spaces
					if (nextline.toLowerCase().startsWith("MIME-Version:")) {
						version = nextline.substring(13).replaceAll("[ ]", "");
						//System.out.println(version);

						//saves date, stripping only preceding space. Date syntax may include spaces
					} else if (nextline.toLowerCase().startsWith("date:")) {
						date = nextline.substring(5);
						if (date.startsWith(" ")){
							date = date.substring(1);
						}
						//System.out.println(date);

						//removes " ", <, > from From address
					} else if (nextline.toLowerCase().startsWith("from:")) {
						int index = nextline.indexOf("<");
						if (index == -1) { index = 0; } 
						mimeSender = nextline.substring(index).replaceAll("[ <>]", "");
						//System.out.println(mimeSender);

						//assumes only one recipient
					} else if (nextline.toLowerCase().startsWith("to:")) {
						int index = nextline.indexOf("<");
						if (index == -1) { index = 0; } 
						mimeRecipients = nextline.substring(index).replaceAll("[ <>]", "");
						//System.out.println(mimeRecipients);

						//Only strips a preceding space in Subject
					} else if (nextline.toLowerCase().startsWith("subject:")) {
						subject = nextline.substring(8);
						if (subject.startsWith(" ")){
							subject = subject.substring(1);
						}
						//System.out.println(subject);

						//Only strips a preceding space in content type
					} else if (nextline.toLowerCase().startsWith("content-type:")) {
						content = nextline.substring(13);
						if (content.startsWith(" ")){
							content = content.substring(1);
						}
						//System.out.println(content);

						//If line was blank, restart loop
					} else if (nextline.compareTo("") == 0) {
						continue;

					//If the end of data flag is encountered, send receipt message and break loop.
					} else if (nextline.compareTo(".") == 0) {
						outgoing.writeChars("250 Received loud and clear! I'll put it in my postbox.\n\r");
						break;

						//If line had no header, assume it was part of the body
					} else {
						body.add(nextline);
						//System.out.println(nextline + " added to body");
					}

				}


			} catch (Exception e) {
				System.out.println("IO error in stream " + messageNum
					+ " on Data (error handling message).");
				e.printStackTrace();
			}
            
			writeToFile();
		}

	private void writeToFile() {
		PrintWriter output = null;
		try {
			output = new PrintWriter("emails/email" + messageNum +".txt", "UTF-8");
		} catch (Exception e) {
			System.out.println("Error creating email file/printwriter for " + messageNum);
		}
		
		output.println("Message " + messageNum);
		output.println("From: " + mimeSender);
		output.println("To: " + mimeRecipients);
		output.println("Date: " + date);
		output.println("Subject: " + subject);
		output.print("Body: ");
		
		for (String line: body) {
			output.println(line);
		}
		
		output.close();
	}
}
