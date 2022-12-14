package pc.bqueue;

import java.util.Arrays;

import scala.Array;
import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

/**
 * STM implementation of queue (given in class). 
 * 
 *
 * @param <E> Type of elements.
 */
public class STMBQueueU<E> implements BQueue<E>{
  
  private final Ref.View<Integer> size; 
  private final Ref.View<Integer> head;
  private final Ref.View<TArray.View<E>> arrayRef;

  /**
   * Constructor.
   * @param initialCapacity Initial queue capacity.
   * @throws IllegalArgumentException if {@code capacity <= 0}
   */
  public STMBQueueU(int initialCapacity) {
    if (initialCapacity <= 0) 
      throw new IllegalArgumentException();
    size = STM.newRef(0);
    head = STM.newRef(0);
    arrayRef = STM.newRef(STM.newTArray(initialCapacity));
  }

  @Override
  public int capacity() {
    return UNBOUNDED;
  }
  
  @Override
  public int size() {
    return size.get();
  }

  @Override
  public void add(E elem) {
    STM.atomic(() -> {
    	TArray.View<E> array = arrayRef.get();
    	if (size.get() == array.length()) {
    		int capacity = array.length()*2;
    		TArray.View<E> newArray = STM.newTArray(capacity);
    		
    		for (int i=0;i<array.length();i++) {
    			newArray.update(i, array.apply(i));
    		}
    		array = newArray;
    		arrayRef.set(newArray);
    	}
    	arrayRef.get().update((head.get() + size.get()) % array.length(), elem);
		STM.increment(size, 1);
    });
  }

  @Override
	public E remove() {
		return STM.atomic(() -> {
			if (size.get() == 0) 
				STM.retry();
			E elem = arrayRef.get().apply(head.get());      
			head.set((head.get() + 1) % arrayRef.get().length());
			STM.increment(size, -1);				
			return elem;
		});
	}
  
  /**
   * Test instantiation (do not run in cooperative mode).
   */
  public static final class Test extends BQueueTest {
    @Override
    <T> BQueue<T> createBQueue(int initialCapacity) {
      return new STMBQueueU<>(initialCapacity);
    }
  }

}
