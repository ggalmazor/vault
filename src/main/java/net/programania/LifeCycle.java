package net.programania;

public interface LifeCycle {
  <T extends LifeCycle> T start();

  <T extends LifeCycle> T stop();
}
