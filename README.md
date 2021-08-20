# COMPSYS 725 Assignment 1 - SFTP (simple FTP)

## Overview

The following commands have been implemented according to IETF:
https://datatracker.ietf.org/doc/html/rfc913

| Command | Description   | Returns  |
| --------|-------------  | ------   |
| `USER`  | Specifies a user on the remote system| `!<user-id> logged in`</br> `+User-id valid, send account and password` </br> `-Invalid user-id, try again`|
| `ACCT`  | Specifies an account on the remote system      |   $12    |
| `PASS`  | are neat      |    $1    |
| `TYPE`  | are neat      |    $1    |
| `LIST`  | are neat      |    $1    |
| `CDIR`  | are neat      |    $1    |
| `KILL`  | are neat      |    $1    |
| `NAME`  | are neat      |    $1    |
| `DONE`  | are neat      |    $1    |
| `STOR`  | are neat      |    $1    |
| `RETR`  | are neat      |    $1    |

