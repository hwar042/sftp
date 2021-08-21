# COMPSYS 725 Assignment 1 - SFTP (simple FTP)

## Overview

These commands have been implemented according to IETF:
https://datatracker.ietf.org/doc/html/rfc913

## Installation and Startup

The System has been built with JDK 8 (Java 1.8).
* Clone the repository to a local disk
* Navigate to `sftp/src/` (where sftp is the cloned directory)
* Build the client from the command line: `javac TCPClient.java`
* Build the server from the command line: `javac TCPServer.java`
* Start the server from the command line: `java TCPServer`
* From a **separate** terminal instance, start the client from the command line: `java TCPClient`
* Enter Client commands from the client terminal

## Server Behaviour
* Expected Welcome Message: `+hwar042 SFTP Service`
* Server requires a message from client (can be empty) to establish connection
* Full access to the server is only possible after a successful login (`!`).
* The Server will disconnect with an error message (`-`).
* Disconnection will reset user directory, login authentication and specified files.

## Commands

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

