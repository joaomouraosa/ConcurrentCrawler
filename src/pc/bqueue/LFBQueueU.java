package pc.bqueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Lock-free implementation of queue - unbounded variant.
 * 
 *
 * @param <E> Type of elements.
 */
public class LFBQueueU<E>  implements BQueue<E> {

	private E[] array;
	private final AtomicInteger head;
	private final AtomicInteger tail;
	private final AtomicBoolean addElementFlag;
	private final Rooms rooms;
	private final boolean useBackoff;

	/**
	 * Constructor.
	 * @param initialCapacity Initial queue capacity.
	 * @param backoff Flag to enable/disable the use of back-off.
	 * @throws IllegalArgumentException if {@code capacity <= 0}
	 */
	@SuppressWarnings("unchecked")
	public LFBQueueU(int initialCapacity, boolean backoff) {
		head = new AtomicInteger(0);
		tail = new AtomicInteger(0);new AtomicMarkableReference<>(0, false);
		addElementFlag = new AtomicBoolean(false);
		array = (E[]) new Object[initialCapacity];
		useBackoff = backoff;
		rooms = new Rooms(3, backoff);
	}

	@Override
	public int capacity() {
		return UNBOUNDED;
	}

	@Override
	public int size() {
		rooms.enter(2);
		int value =  tail.get() - head.get();
		rooms.leave(2);
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void add(E elem) {

		try {
			rooms.enter(0);
			while (!addElementFlag.compareAndSet(false, true)) {}

			int p = tail.getAndIncrement();

			if (p < array.length)
				array[p] = elem;
			else {
				int size = array.length*2;

				E[] arrayNew = (E[]) new Object[size];
				for (int i=0;i<array.length;i++)
					arrayNew[i]=array[i];

				array=null;
				array = arrayNew;
				array[p] = elem;
			}
		} finally {
			rooms.leave(0);
			addElementFlag.set(false);
		}
	}

	@Override
	public E remove() {   
		E elem = null;

		while(true) {
			rooms.enter(1);
			int p = head.getAndIncrement();
			if (p < tail.get()) {
				elem = array[p];
				array[p] = null;
				rooms.leave(1);
				break;
			}
			else {
				head.getAndDecrement();
				rooms.leave(1);
			}
			if (useBackoff) 
				Backoff.delay();
		}
		if (useBackoff)
			Backoff.reset();
		return elem;
	}

	synchronized void info(String text) {
		System.out.printf("%s | ", Thread.currentThread().getName());
		System.out.println(text);
	}

	/**
	 * Test instantiation.
	 */
	public static final class Test extends BQueueTest {
		@Override
		<T> BQueue<T> createBQueue(int capacity) {
			return new LFBQueueU<>(capacity, false);
		}
	}
}
