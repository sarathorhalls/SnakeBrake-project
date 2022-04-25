package com.saraexperiments.jabble;

public abstract interface AbstractGame {
  public abstract void update(Jabble jb, float deltaTime);

  public abstract void render(Jabble jb, Renderer renderer);
}