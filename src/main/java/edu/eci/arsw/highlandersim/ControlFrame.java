package edu.eci.arsw.highlandersim;

import edu.eci.arsw.immortals.Immortal;
import edu.eci.arsw.immortals.ImmortalManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public final class ControlFrame extends JFrame {

  private ImmortalManager manager;
  private final JTextArea output = new JTextArea(14, 40);
  private final JButton startBtn = new JButton("Start");
  private final JButton pauseAndCheckBtn = new JButton("Pause & Check");
  private final JButton resumeBtn = new JButton("Resume");
  private final JButton removeDeadBtn = new JButton("Remove Dead");
  private final JButton stopBtn = new JButton("Stop");

  private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 5000, 1));
  private final JSpinner healthSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
  private final JSpinner damageSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
  private final JComboBox<String> fightMode = new JComboBox<>(new String[]{"ordered", "naive"});

  public ControlFrame(int count, String fight) {
    setTitle("Highlander Simulator — ARSW");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(8,8));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel("Count:"));
    countSpinner.setValue(count);
    top.add(countSpinner);
    top.add(new JLabel("Health:"));
    top.add(healthSpinner);
    top.add(new JLabel("Damage:"));
    top.add(damageSpinner);
    top.add(new JLabel("Fight:"));
    fightMode.setSelectedItem(fight);
    top.add(fightMode);
    add(top, BorderLayout.NORTH);

    output.setEditable(false);
    output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    add(new JScrollPane(output), BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottom.add(startBtn);
    bottom.add(pauseAndCheckBtn);
    bottom.add(resumeBtn);
    bottom.add(removeDeadBtn);
    bottom.add(stopBtn);
    add(bottom, BorderLayout.SOUTH);

    startBtn.addActionListener(this::onStart);
    pauseAndCheckBtn.addActionListener(this::onPauseAndCheck);
    resumeBtn.addActionListener(this::onResume);
    removeDeadBtn.addActionListener(this::onRemoveDead);
    stopBtn.addActionListener(this::onStop);

    pack();
    setLocationByPlatform(true);
    setVisible(true);
  }

  private void onStart(ActionEvent e) {
    safeStop();
    int n = (Integer) countSpinner.getValue();
    int health = (Integer) healthSpinner.getValue();
    int damage = (Integer) damageSpinner.getValue();
    String fight = (String) fightMode.getSelectedItem();
    manager = new ImmortalManager(n, fight, health, damage);
    manager.start();
    output.setText("Simulation started with %d immortals (health=%d, damage=%d, fight=%s)%n"
      .formatted(n, health, damage, fight));
  }

  private void onPauseAndCheck(ActionEvent e) {
    if (manager == null) return;
    manager.pause();
    
    try { Thread.sleep(100); } catch (InterruptedException ie) {}
    
    List<Immortal> pop = manager.populationSnapshot();
    long actualTotal = 0;
    StringBuilder sb = new StringBuilder();
    
    for (Immortal im : pop) {
      int h = im.getHealth();
      actualTotal += h;
      sb.append(String.format("%-14s : %5d%n", im.name(), h));
    }
    
    sb.append("================================\n");
    long fights = manager.scoreBoard().totalFights();
    long expectedTotal = manager.calculateExpectedTotal();
    long difference = Math.abs(expectedTotal - actualTotal);
    boolean valid = manager.checkInvariant();
    
    sb.append(String.format("Fights:         %d%n", fights));
    sb.append(String.format("Actual Total:   %d%n", actualTotal));
    sb.append("--------------------------------\n");
    sb.append("INVARIANT CHECK:\n");
    sb.append(String.format("  Expected:     %d%n", expectedTotal));
    sb.append(String.format("  Actual:       %d%n", actualTotal));
    sb.append(String.format("  Difference:   %d%n", difference));
    sb.append(String.format("  Status:       %s%n", valid ? "✓ VALID" : "✗ INVALID"));
    
    if (!valid) {
      sb.append("\n⚠️  WARNING: Invariant violation!\n");
      sb.append("Possible causes:\n");
      sb.append("  • Incomplete pause (threads still fighting)\n");
      sb.append("  • Race condition in synchronization\n");
    }
    
    output.setText(sb.toString());
  }

  private void onResume(ActionEvent e) {
    if (manager == null) return;
    manager.resume();
    output.append("\n\n▶ Simulation RESUMED\n");
  }
  
  private void onRemoveDead(ActionEvent e) {
    if (manager == null) return;
    int populationBefore = manager.populationSnapshot().size();
    int removed = manager.removeDead();
    int populationAfter = manager.populationSnapshot().size();
    
    if (removed > 0) {
      output.append(String.format("\n\n☠️  Removed %d dead immortal%s\n", removed, removed == 1 ? "" : "s"));
      output.append(String.format("   Population: %d → %d\n", populationBefore, populationAfter));
    } else {
      output.append("\n\n✓ No dead immortals to remove\n");
    }
  }

  private void onStop(ActionEvent e) { 
    if (manager == null) {
      output.append("\n\n⚠️  No simulation running\n");
      return;
    }
    
    long startTime = System.currentTimeMillis();
    int populationCount = manager.populationSnapshot().size();
    long fights = manager.scoreBoard().totalFights();
    
    safeStop(); 
    
    long duration = System.currentTimeMillis() - startTime;
    output.append(String.format("\n\n■ Simulation STOPPED\n"));
    output.append(String.format("   Total fights: %d\n", fights));
    output.append(String.format("   Final population: %d immortals\n", populationCount));
    output.append(String.format("   Shutdown time: %dms\n", duration));
  }

  private void safeStop() {
    if (manager != null) {
      manager.stop();
      manager = null;
    }
  }

  public static void main(String[] args) {
    int count = Integer.getInteger("count", 8);
    String fight = System.getProperty("fight", "ordered");
    SwingUtilities.invokeLater(() -> new ControlFrame(count, fight));
  }
}
