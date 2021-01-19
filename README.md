# CE303 Assignment

For this assignment we were tasked with creating a market application in both Java and C#. There was to be a server side element utilizing sockets and threads to enable multiple client connections of 'Traders'. Upon first Trader connection, they would acquire stock, and be able to transfer stock to other traders connected or themselves. When a trader with the stock leaves, the stock is assigned to a random trader or held if no traders available until the next trader connects. 

A full server-client communication protocol was designed using synchronous design, the clients can receive messages from the server at any time: not fuly a-synchronous. All resources on the server implementation requiring thread safety utilized resource locks, atomic integers, and/or synchronization methods to prevent race conditions.

Additoinal points were given and implemented in both languages for a functional GUI, Unit testing, and stateful restarting of the server. This assignment was graded 100%!

To run this software bat files have been created for all applications. Run any of the bat files from the following.

Client application
ClientCSharp.bat - CSharp implementation
ClientJava.bat - Java implementation

Server application
ServerCSharp.bat - CSharp implementation
ServerJava - Java implementation

These have been tested and are executable on lab machines.

If you require running these directly from the command line, open the associated '.bat' file for the application
you desire to run in a notepad, and run the commands listed in between '@ECHO OFF' - '@ECHO ON'.

It is imperitive that the folder hierarchy structure remains as was provided in the zip file.

Server restarting has been implemented in both languages, however, the CSharp varient may take a brief moment for the GUI to appear. Given this
You can run both client and server through launching the client alone.
