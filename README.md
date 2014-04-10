WebServer
=========

A simple web server Utility in Java.


The Protocol used in Sockets sending and receiving data is as follows (serially):

....(Number of files)...

For every file:
  ....(Data Length)....
  ....(File name)....
  ....(File Data)....
