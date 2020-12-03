@ECHO OFF
cd MarketJava
start java -cp "./libs/gson-2.8.2.jar;./out" Client.ClientProgram
TIMEOUT 1
@ECHO ON