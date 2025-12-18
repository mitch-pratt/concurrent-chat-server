Discord - GROUP 18
==================

## Task
### Stage 1 – Simple Client (5)
* Design and implement a piece of software in Java which allows two instances to send Unicode messages back and forth to one another via TCP sockets and displays received messages onscreen. (4)

* Bonus goal – Can you implement a similar client in another language like Python or C and get it talking to your Java one? (1)
To achieve this bonus, you only need to provide a simple text client in a non-Java language.
No further credit will be awarded or deducted for implementing a server or any of your
advanced features from stages 3 or 4 in this second client, though it might not be as hard to
do as you think.
### Stage 2 – Simple Server (5)
* Design and implement a piece of software which acts as a server for your chat application, utilising threads to maintain socket connections with many clients simultaneously. The client should connect only to the server from this point forward, not to other clients, and should be able to remain relatively unchanged from part 1.
### Stage 3 – Fun with Concurrency (20)
* Repeat the following process four times:
    * Add an interesting feature to your server which utilises threads. (1)
    * Explain (1) and demonstrate a way this feature could fail without thread safety. (1)
    * Explain (1) and demonstrate how you have made this feature thread safe. (1)

Examples of interesting features and testing strategies could include:
* Multi-user group conversations which cannot get out of sync

* Server-side user settings such as status messages or filtering which multiple users can modify or see results from simultaneously

* An asynchronous mailing system, where the server can log messages for a user who is not currently online
Features may iterate on one another, for example group chats could have settings that must be up to date for all users. This is intended to be fun and creative, so if you think of something interesting and aren’t sure if it qualifies, please speak to your mentor or one of the lecturers.
Demonstrating how these features might fail, and how your thread safety fixes them is non-trivial and may require creative unit testing or spinning up tens or hundreds of clients at once. You must demonstrate a test which consistently passes on your thread safe code and consistently fails on an unsafe instance of the same code (you will likely want to program to interfaces to make this switching easier).
### Stage 4 – Fun with Networking (20)
During this part, you’ll need to implement real time video calls and file transfer between
clients. You should utilise your existing server as the signalling server to facilitate the
exchange of metadata and IP addresses, then utilise peer to peer communication between
clients.
* Implement a feature which allows a client to send a file to another (3)
* Implement a feature which allows for display of a transferred image or playback of a
transferred video. You may need to implement a very simple GUI or web interface to display
these, but no additional credit will be awarded for quality of the interface (2)
* Implement a feature which allows for real-time streaming of a video file from one client to
another with live playback. (7)
* Modify your real-time video streaming to utilise your device’s cameras, allowing for p2p
video calls (3)
* Satisfactorily answer questions about the implementation of these advanced networking
features during your final demo (5)
## Progress
### Stage 1 – Simple Client (5)
p2p TCP Done
#### TODO
* client in other language
### Stage 2 – Simple Server (5)
* Done
#### TODO
* error handling
### Stage 3 – Fun with Concurrency (20)
#### TODO
* ALL
* plan - pinned chat
* settings
* log
* group chat
### Stage 4 – Fun with Networking (20)
#### TODO
* ALL
