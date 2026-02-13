package edu.eci.arsw.immortals;

import edu.eci.arsw.concurrency.PauseController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ImmortalManager implements AutoCloseable {
  private final List<Immortal> population = new CopyOnWriteArrayList<>();
  private final List<Future<?>> futures = new ArrayList<>();
  private final PauseController controller = new PauseController();
  private final ScoreBoard scoreBoard = new ScoreBoard();
  private ExecutorService exec;

  private final String fightMode;
  private final int initialHealth;
  private final int damage;
  private int initialPopulation;
  private int healthRemovedByCleanup = 0;

  public ImmortalManager(int n, String fightMode) {
    this(n, fightMode, Integer.getInteger("health", 100), Integer.getInteger("damage", 10));
  }

  public ImmortalManager(int n, String fightMode, int initialHealth, int damage) {
    this.fightMode = fightMode;
    this.initialHealth = initialHealth;
    this.damage = damage;
    initialPopulation = n;
    for (int i=0;i<n;i++) {
      population.add(new Immortal("Immortal-"+i, initialHealth, damage, population, scoreBoard, controller));
    }
  }

  public synchronized void start() {
    if (exec != null) stop();
    exec = Executors.newVirtualThreadPerTaskExecutor();
    for (Immortal im : population) {
      futures.add(exec.submit(im));
    }
  }

  public void pause() { controller.pause(); }
  public void resume() { controller.resume(); }
  
  public void stop() {
    if (exec == null) return; // Ya detenido
    
    // 1. Señalar a todos los inmortales que deben detenerse
    for (Immortal im : population) im.stop();
    
    // 2. Si está pausado, resumir para que los hilos puedan terminar
    if (controller.paused()) {
      controller.resume();
    }
    
    // 3. Apagar el executor (no acepta más tareas)
    exec.shutdown();
    
    // 4. Esperar hasta 5 segundos a que todos terminen
    try {
      if (!exec.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
        // Si no terminaron, forzar apagado
        exec.shutdownNow();
        // Esperar un poco más
        if (!exec.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
          System.err.println("Warning: Some threads did not terminate");
        }
      }
    } catch (InterruptedException e) {
      exec.shutdownNow();
      Thread.currentThread().interrupt();
    }
    
    // 5. Limpiar recursos
    futures.clear();
    exec = null;
  }

  public int aliveCount() {
    if (controller.paused()) {
      try {
        controller.waitForAllPaused(population.size());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    int c = 0;
    for (Immortal im : population) if (im.isAlive()) c++;
    return c;
  }

  public long totalHealth() {
    if (controller.paused()) {
      try {
        controller.waitForAllPaused(population.size());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    long sum = 0;
    for (Immortal im : population) sum += im.getHealth();
    return sum;
  }

  public List<Immortal> populationSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(population));
  }

  public ScoreBoard scoreBoard() { return scoreBoard; }
  public PauseController controller() { return controller; }

  @Override public void close() { stop(); }
  
  public long calculateExpectedTotal() {
      long netLossPerFight = damage - (damage / 2);
      return (long) initialHealth * initialPopulation - scoreBoard.totalFights() * netLossPerFight - healthRemovedByCleanup;
}

  public boolean checkInvariant(){
      return totalHealth() == calculateExpectedTotal();
  }
  
  public int getInitialHealth() { return initialHealth; }
  public int getDamage() { return damage; }
  public int getInitialPopulation() { return initialPopulation; }
  
  public int removeDead() {
    int removed = 0;
    for (Immortal im : population) {
      if (im.getHealth() <= 0) {
        int health = im.getHealth();
        population.remove(im);
        healthRemovedByCleanup += health;
        removed++;
      }
    }
    return removed;
  }
  
}
