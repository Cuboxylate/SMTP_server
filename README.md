# SMTP Server

An implementation of an SMTP (Simple Mail Tranfer Protocol) server, with emails
formatted in MIME (Multipurpose Internet Mail Extensions). This was originally for a networking university course, to teach us about coding to compliance specifications. 

SMTP functionality is compliant with RFC 2821,
and MIME is compliant with RFC 2045.

The main function in MySMTPServer.java sets up an 
infinite loop listening for incoming connections, 
and forks a new process to deal each new connection 
detected. The SMTPParser class deals with the 
basic SMTP commands, whereas the MIMEParser class
deals specifically with the email construction
commands.

Emails are saved locally in the emails/ folder.

#### To test:

Compile all of the .java files and run:
	javac MySMTPServer
to set up the server. To connect to it from the
local (unix) machine type in the command line:
	telent localhost 6013
