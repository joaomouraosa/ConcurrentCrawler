> Serving files locally
# # Compile the web server
# # javac WebServer.java && java WebServer . 8080 1
# # Run the sequential crawler
# # jav

# javac WebServer.java && java WebServer . 8080 1


> Generic sequential crawler
## javac SequentialCrawler.java && java SequentialCrawler [URL] 
## E.g, java SequentialCrawler 2 https://docs.oracle.com/javase/8/docs/api/java/util/concurrent.html

# Concurrent crawler
## javac SequentialCrawler.java && javac ConcurrentCrawler && java ConcurrentCrawler [threads] [URL] 
## E.g., java ConcurrentCrawler 2 https://docs.oracle.com/javase/8/docs/api/java/util/concurrent.html