
SCS Project Deployment Instruction
----------------------------------------------------------------------
for JAR creation, we can use the command on the window command prompt
For Example,

C:\workspace\SCS>
>sbt
>assembly

Project JAR file will be created the following directory

C:\workspace\SCS\target\scala-2.11\scs-bot.jar

Then, we have to create a directory tree as below, 

C:\SCS-JAR 
		|-->config  	|-->appconfig	|-->application.conf
		|				|-->dbconf		|->postgres.conf
		|				|				|->mssql.conf
		|				|				|->mysql.conf
		|				|				|->oracle.conf
		|-->scs-bot.jar



Execute the JAR file using Command Prompt
----------------------------------------------------------------------
Default:

C:\SCS-JAR> java -jar scs-bot.jar
SCS Project connect PostgreSQL Database as default

for PostgreSQL:
C:\SCS-JAR> set runMode=postgresql
C:\SCS-JAR> java -jar scs-bot.jar

for MS SQLServer:
C:\SCS-JAR> set runMode=mssql
C:\SCS-JAR> java -jar scs-bot.jar

for Oracle:
C:\SCS-JAR> set runMode=oracle
C:\SCS-JAR> java -jar scs-bot.jar

for MySql:
C:\SCS-JAR> set runMode=mysql
C:\SCS-JAR> java -jar scs-bot.jar


If you want to exit the project, type Ctrl+c on the window command prompt



