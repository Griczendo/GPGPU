# GPGPU
GPGPU sorting
REMEMBER TO ADD THE FOLLOWING INTO JVM OPTIONS:
-Djava.library.path=target/natives

In case of: Native Libraries can't be found

Make sure the target/natives directory exists! If it doesn't, run mvn package which should create it.

Make sure you set your java.library.path as given above. If you're using an IDE, you need to add it to your launch settings as described in the setup guides on this wiki. If you're launching from the command line, make sure you set MAVEN_OPTS as given in Step 4 (note that -Djava.library.path does not work as a normal vm argument in maven's exec:java goal, and you must use MAVEN_OPTS instead). 


JUST ADD A MAVEN BUILD ("MAVEN GOAL") BEFORE CLASS RUN WITH COMMAND LINE:
package (as 'mvn' is added by default probably in Intellij so you don't need it)
