# Maekawa-Distributed-Mutual-Exclusion
Implement a mutual exclusion service using Maekawa’s distributed mutual exclusion algorithm with pre-emption strategy to avoid deadlocks


Project Description:
---------------------
This project provides two function calls to the application: 
a) csEnter() and 
b) csLeave(). 

- The first function call csEnter() allows an application to request permission to start executing its critical section. The function call is blocking and returns only when the invoking application can execute its critical section. 
- The second function call csLeave() allows an application to inform the service that it has finished executing its critical section.

Project Environment:
---------------------
TCP Socket Programming and Multithreading using JAVA, Ubuntu 14.04 TLS, and Shell scripting
