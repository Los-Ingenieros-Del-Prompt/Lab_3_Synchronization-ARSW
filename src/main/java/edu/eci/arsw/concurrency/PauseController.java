package edu.eci.arsw.concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class PauseController {
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition unpaused = lock.newCondition();
  private final Condition allPaused = lock.newCondition();
  private volatile boolean paused = false;
  private AtomicInteger waitingThreads = new AtomicInteger(0);

  public void pause() { lock.lock(); try { paused = true; } finally { lock.unlock(); } }
  
  public void resume() { 
    lock.lock(); 
    try { 
      paused = false; 
      waitingThreads.set(0);
      unpaused.signalAll(); 
    } finally { 
      lock.unlock(); 
    } 
  }
  
  public boolean paused() { return paused; }

  public void awaitIfPaused() throws InterruptedException {
    lock.lockInterruptibly();
    try { 
      if (paused) {
        waitingThreads.incrementAndGet();
        allPaused.signalAll();
        while (paused) {
          unpaused.await();
        }
        waitingThreads.decrementAndGet();
      }
    } finally { 
      lock.unlock(); 
    }
  }

  public void waitForAllPaused(int expectedThreads) throws InterruptedException {
    lock.lockInterruptibly();
    try {
      while (paused && waitingThreads.get() < expectedThreads) {
        allPaused.await();
      }
    } finally {
      lock.unlock();
    }
  }
}
