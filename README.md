# Directory Synchronization

Directory Synchronization is a desktop application to compare and backup files.  Directory Synchronization is developed in Java and can be executed on multiple operating systems.   Enter two directories to recursively compare files between the two directories.  Easily copy files between directories.  Enter a directory and recursively find duplicate files in the directory.  Directory Synchronization was originally developed to backup photos and videos to a backup disk drive.

## User Interface

![alt tag](https://github.com/dthorntonz/DirectorySynchronization/blob/master/DirectorySynchronization.png)

## Execution Dependencies

- JDK 1.7 or greater

## Executing Directory Synchronization

- [Download  the self executing dirsync-1.0.0.jar](releases/download/1.0.0/dirsync-1.0.0.jar)
- Double click the jar file
- or
- Enter the following command on a command line:
$ java jar dirsync.jar

## Compilation Dependencies

- Apache Maven 3.3 or greater  

## Compiling

-  Compile a self executing jar file:
$ mvn package
- Look for the file dirsync-x.x.x-jar-with-dependencies.jar in the target directory.

## License 
Copyright © 2015 Danny Thornton

Distributed under the Apache License, version 2.0. (http://www.apache.org/licenses/LICENSE-2.0.html)
