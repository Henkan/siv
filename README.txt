After extraction, put the src folder where you want (for example /home/student)
Then, to run the SIV, follow the steps below.
You can refer to the Usage part in the report for more explanations and examples:

1 - Run the following command to compile the .java to .class (Java is already installed on Server A):
	javac src/siv/*.java

2 - You can now run the program with the following command:
	java -cp src/ siv.Main <arguments>

3 - Replace <arguments> in the command above with the correct arguments for what you want to do. 
    Start by using the -h arguments to get help, or refer to the report to get examples.

The following hash functions are supported:
Function   | Syntax
---------------------
MD2        | md2, MD2
MD5        | md5, MD5
SHA-1      | sha1, SHA1, sha-1, SHA-1
SHA-256    | sha-256, SHA-256
SHA-384    | sha-384, SHA-384
SHA-512    | sha-512, SHA-512

