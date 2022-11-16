# Generic sequential crawler
> javac SequentialCrawler.java && java SequentialCrawler [URL]
> java SequentialCrawler https://docs.oracle.com/javase/8/docs/api/java/util/concurrent.html  # eg

# Concurrent crawler
>javac SequentialCrawler.java && javac ConcurrentCrawler && java ConcurrentCrawler [THREADS] [URL]
>java ConcurrentCrawler 4 https://docs.oracle.com/javase/8/docs/api/java/util/concurrent.html  # (4 threads concurrently)
