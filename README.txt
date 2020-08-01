To set up the database, use the MYSQL commands in 'mysqlcommands.txt'.

To run the application, use the following command:
javac  -classpath "./lib/*:." *.java && java -classpath "./lib/*:." NewsFeedPoll

To run the server, use the following command:
javac -classpath "./lib/*:." *.java && java -classpath "./lib/*:." Server

You can then access the data via the following commands:

List all news articles by id and title:
http://localhost:8000/newsitems/list

Fetch all news articles:
http://localhost:8000/newsitems/all

Fetch one single news article by id:
http://localhost:8000/newsitems/item?id=1


When fetching from a different source, change the constants in NewsFeedPoll.java accordingly.
