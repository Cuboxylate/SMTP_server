# SMTP Server

An implementation of a Simple Mail Tranfer Protocol
(SMTP) server for a univeristy assignment, with emails
formatted in Multipurpose Internet Mail Extensions 
(MIME).

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

The assignment outline is included for a full
description of functionality.

#### To test:

Compile all of the .java files and run:
	javac MySMTPServer
to set up the server. To connect to it from the
local (unix) machine type in the command line:
	telent localhost 6013
