package de.tucottbus.kt.lcars.util;

import java.lang.reflect.Array;
import java.util.concurrent.*;


/**
 * BoundedBuffer
 * <p/>
 * Bounded buffer using \Semaphore
 *
 * @author Brian Goetz and Tim Peierls
 */
public class BlockingBoundedBuffer <E> {
    private final Semaphore availableItems, availableSpaces;
    private final E[] items;
    private int putPosition = 0, takePosition = 0;
    private final Class<E> genericType;

    @SuppressWarnings("unchecked")
    public BlockingBoundedBuffer(int capacity, Class<E> clazz) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        availableItems = new Semaphore(0);
        availableSpaces = new Semaphore(capacity);
        items = (E[]) new Object[capacity];
        genericType = clazz;
    }

    public boolean isEmpty() {
        return availableItems.availablePermits() == 0;
    }

    public boolean isFull() {
        return availableSpaces.availablePermits() == 0;
    }

    public void put(E x) throws InterruptedException {
        availableSpaces.acquire();
        doInsert(x);
        availableItems.release();
    }

    public E take() throws InterruptedException {
        availableItems.acquire();
        E item = doExtract();
        availableSpaces.release();
        return item;
    }
    
    public E[] takeAll() throws InterruptedException {
        int permits = Math.max(1, availableItems.availablePermits());
        // TODO: possible interrupt followed by a take() call can block the next acquire
        availableItems.acquire(permits);
        
        E[] result = doExtract(permits);
        availableSpaces.release(permits);
        return result;
    }

    private synchronized void doInsert(E x) {
        int i = putPosition;
        items[i] = x;
        putPosition = (++i == items.length) ? 0 : i;
    }

    private synchronized E doExtract() {
        int i = takePosition;
        E x = items[i];
        items[i] = null;
        takePosition = (++i == items.length) ? 0 : i;
        return x;
    }
    
    @SuppressWarnings("unchecked")
    private synchronized E[] doExtract(int count) {
      int i = takePosition;
      int length = items.length;
      E[] result = (E[]) Array.newInstance(genericType, count);
      for (int iresult = 0; iresult < count; iresult++)
      {
        result[iresult] = items[i];
        items[i] = null;
        i = (++i == length) ? 0 : i;
      }
      
      takePosition = i;
      return result;
    }
}
