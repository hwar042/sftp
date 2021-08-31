# COMPSYS 725 Assignment 1 - SFTP (simple FTP)

## Overview

These commands have been implemented according to IETF RFC 913:
https://datatracker.ietf.org/doc/html/rfc913
* The System has been built with JDK 8 (Java 1.8)
* The System has been configured using `localhost:6789` as default IP address and port

## Installation and Startup

* Clone the repository to a local disk
* Open a terminal window in the repo directory
* Build the client from the command line: `javac src/main/TCPClient.java -cp src`
* Build the server from the command line: `javac src/main/TCPServer.java -cp src`
* Start the server from the command line: `java -cp src main/TCPServer`
* From a **separate** terminal instance, start the client from the command line: `java -cp src main/TCPClient`
* Enter Client commands from the client terminal
* By default the client program is modified not to exit after receiving a response
* To restore its original behaviour, start the client with `java TCPClient -test`

## Server Behaviour
* Expected Welcome Message: `+hwar042 SFTP Service`
* Server requires a message from client (can be empty) to establish connection
* Full access to the server is only possible after a successful login (`!`).
* The Server will disconnect with an error message (`-`).
* Disconnection will reset chosen directory, login authentication and specified files.

## User and Account Details

There are 3 Users provided, and 2 Accounts. The details are:

| User   | Account     | Password     |
| -----  |---------    | --------     |
| admin  |*not needed* | *not needed* |
| albert | acct        | pass         |
| bob    | kobe        | bryant       |

* A user is required before an account or password.
* A password can be sent before an account.
* The admin user does not require an account or password.
* An account can be used with any user.
* Passwords are specific to an account.
* Passwords are case-sensitive, users and accounts are not.

Users and Accounts are stored in plaintext in `/database/users.txt` `/database/accts.txt` (respectively) in the form:

| User   | Admin  |
| -----  |------- |
| admin  | * |
| albert | |
| bob | |

where an `*` denotes no requirement for an account or password.

| Account   | Password  |
| -----  |------- |
| acct | pass |
| kobe | bryant |

## Testing

25 JUnit tests have been included, as well as a JUnit binary to launch tests from the commandline. Tested on Ubuntu 1804LTS

### Instructions for testing server functions:

* It is **strongly recommended** to test with a clean repo
* Build Client and Server and Start Server as per [installation instructions](#Installation and Startup)
From a terminal window (from the repo dir)
* Compile The JUnit Test: `javac -cp junit-platform-console-standalone-1.7.2.jar:src src/test/SimpleProgramTest.java`
* Run The JUnit Test: `java -jar junit-platform-console-standalone-1.7.2.jar -cp src --scan-classpath`

#### Tests Passing:

![Proof of Tests Passing](https://github.com/hwar042/sftp/raw/main/tests/testProof.png)

## Commands
* Commands are entered in the client console followed by the <kbd>↵ ENTER</kbd> key.
* Commands are **not** case sensitive.
* An unrecognized command will get the response: `-Unknown command, try again`

The Following commands can be executed at any time:
| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `USER user-id`  | Specifies a user on the remote system| `!<user-id> logged in`</br> `+User-id valid, send account and password` </br> `-Invalid user-id, try again`|
| `ACCT account`  | Specifies an account on the remote system |   `! Account valid, logged-in` </br> `+Account valid, send password` </br>  `-Invalid account, try again` |
| `PASS password`  | Specifies an account on the remote system |   `! Logged in` </br> `+Send account` </br>  `-Wrong password, try again` |

The Following Commands require a user to be logged on:
| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `TYPE { A \| B \| C }`  | Specifies the file mapping    |   `+Using { Ascii \| Binary \| Continuous } mode`  </br>  `-Type not valid` |
| `LIST { F \| V } directory-path`  | Returns listing of specified directory     |    `+<directory-path> <directory-contents>` </br> `-non-existent directory` </br>   `-invalid file listing format` |
| `CDIR new-directory`  | Changes current directory  |    `!Changed working dir to <new-directory>` </br> `-Can't connect to directory because: directory does not exist` |
| `KILL file-spec`  | Deletes specified file   |    `+<file-spec> deleted` </br>  `-Not deleted because file does not exist`  |
| `NAME old-file-spec`  | Selects File to be renamed      |    `+File exists` </br>    `-Can't find <old-file-spec>` |
| `DONE`  | Closes Connection |    `+closing connection`  |
| `RETR file-spec`  | Requests a file from the remote system |    `<number-of-bytes-that-will-be-sent> (as ascii digits)` </br> ` -File doesn't exist`   |
| `STOR { NEW \| OLD \| APP } file-spec`  | Requests to store a file on the remote system |  `+File exists, will create new generation of file` </br> `+File does not exist, will create new file` </br> `-File exists, but system doesn't support generations` </br> `+Will write over old file` </br> ` +Will create new file` </br> ` +Will append to file` </br> `+Will create file` |

The Following Commands are only available immediately after `NAME`:
| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `TOBE new-file-spec` | Renames file | `+<old-file-spec> was renamed to <new-file-spec>` </br> `-File wasn't renamed because file with dest path already exists`|

The Following Commands are only available immediately after `RETR`:
| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `SEND` | Sends requested file | `<requested file>` (to specified directory)* </br> **and** `+File received` |

<sub>\* The location to store the file is specified as a field in TCPClient.java<sub>
 
The Following Commands are only available immediately after `STORE` (each command must follow the previous):
| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `SIZE <file-size>` | Specifies the size of the file to be stored (must match) | `+ok, waiting for file` </br> **and** `Enter Absolute Filepath of File to Send` </br> `-Not enough room, don't send it` </br> `-Incorrect number format`|
 | `<path-of-file-to-be-sent>` | Sends the file at the specified path | `+Saved <file-spec>` </br> `-Couldn't save because of an I/O error`|

## File Structure
```
    sftp
    ├── database
    │   ├── accts.txt
    │   └── users.txt
    └── src
        ├── database
        │   ├── accts.txt
        │   └── users.txt
        ├── Auth.java
        ├── Connection.java
        ├── questions.txt
        ├── Reader.java
        ├── TCPClient.java
        └── TCPServer.java

```
