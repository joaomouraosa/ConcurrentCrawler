// package pc.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

// import pc.util.UnexpectedException;

public class ConcurrentCrawler extends SequentialCrawler {
	HashSet<String> visited = new HashSet<>();
	private final Collection<String> visitedLinks = Collections.synchronizedList(new ArrayList<String>());

	long count = 0L;

	public static void main(String[] args) throws IOException {

		int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
		String url = args.length > 1 ? args[1] : "http://localhost:8123";
		ConcurrentCrawler cc = new ConcurrentCrawler(threads);

		cc.setVerboseOutput(false);
		cc.crawl(url);
		cc.stop();
	}

	/**
	 * The fork-join pool.
	 */
	private final ForkJoinPool pool;

	/**
	 * Constructor.
	 * 
	 * @param threads number of threads.
	 * @throws IOException if an I/O error occurs
	 */
	public ConcurrentCrawler(int threads) throws IOException {
		pool = new ForkJoinPool(threads);
	}

	@Override
	public void crawl(String root) {
		long t = System.currentTimeMillis();
		log("Starting at %s", root);
		int count = pool.invoke(new TransferTask(0, root));
		t = System.currentTimeMillis() - t;
		log("%d Done in %d ms", count, t);
	}

	/**
	 * Stop the crawler.
	 */
	public void stop() {
		pool.shutdown();
	}

	@SuppressWarnings("serial")
	private class TransferTask extends RecursiveTask<Integer> {
		final int rid;
		final String path;

		TransferTask(int rid, String path) {
			this.rid = rid;
			this.path = path;
		}

		@Override
		protected Integer compute() {
			try {
				List<String> links = performTransfer(rid, new URL(path));
				List<RecursiveTask> forks = new ArrayList<RecursiveTask>();
				int value = 0;
				int rid = this.rid;
				URL url = new URL(path);

				for (String link : links) {
					String newURL = new URL(url, new URL(url, link).getPath()).toString();
					if (!visitedLinks.contains(newURL.toString())) {
						visitedLinks.add(newURL.toString());
						rid++;
						TransferTask task = new TransferTask(rid, newURL.toString());
						forks.add(task);
						task.fork();
					}
				}

				for (RecursiveTask<Integer> task : forks) {
					value += 1 + task.join();
				}
				return value;
			} catch (Exception e) {
				return null;
			}

		}

	}
}
