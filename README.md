Created by Oleksandr Ovdiienko a java application implementing a server that performs arithmetic operations for multiple clients with service discovery capabilities and statistical reporting.

First of all to run a program you need to write in a console  java -jar src/CCS.jar 1234 - this will start a server
1234 - it is a port number, just for example

Then for join to this server like a client, you need to write java src/Client.java 1234
1234 - same port number, on which we start our server

|   |                    ,---.
|---|,---..    ,,---.    |__. .   .,---.
|   |,---| \  / |---'    |    |   ||   |
`   '`---^  `'  `---'    `    `---'`   '
