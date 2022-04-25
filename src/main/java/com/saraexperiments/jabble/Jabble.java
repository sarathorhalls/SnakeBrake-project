package com.saraexperiments.jabble;

public class Jabble implements Runnable {

  private Window window;
  private Renderer renderer;
  private Input input;
  private AbstractGame game;

  // 720p = 320, 180(var að nota 240)
  // 480p = 160, 120
  private int width = 320;
  private int height = 180;
  private float scale = 5f;
  private String label = "Wowee!";

  private boolean running = false;
  private static final double UPDATECAP = 1.0 / 60.0;

  public Jabble(AbstractGame game) {
    this.game = game;
  }

  public void start() {
    window = new Window(this);
    renderer = new Renderer(this);
    input = new Input(this);

    Thread thread = new Thread(this);
    // Mögulega breyta aftur yfir í .run()
    thread.start();
  }

  public void stop() {
    // Not needed at the moment
  }

  public void run() {
    running = true;

    boolean render = false;

    double firstTime = 0;
    double lastTime = System.nanoTime() / 1000000000.0;
    double passedTime = 0;
    double unprocessedTime = 0;
    double frameTime = 0;
    // int frames = 0;
    // int fps = 0;

    while (running) {
      firstTime = System.nanoTime() / 1000000000.0;
      passedTime = firstTime - lastTime;
      lastTime = firstTime;
      unprocessedTime += passedTime;
      frameTime += passedTime;
      render = false;

      while (unprocessedTime >= UPDATECAP) {
        unprocessedTime -= UPDATECAP;
        render = true;

        game.update(this, (float) UPDATECAP);

        input.update();

        if (frameTime >= 1.0) {
          frameTime = 0;
          // fps = frames;
          // frames = 0;
          // System.out.println("fps: " + fps);
        }
      }
      if (render) {
        renderer.clear();
        // renderer.drawText("FPS: " + fps , 50, 0, 0xffff43ff);
        game.render(this, renderer);
        renderer.process();
        window.update();
        // frames++;
      } else {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    dispose();
  }

  public void dispose() {
    // Not needed at the moment
  }

  // Getters and setters
  public int getWidth() {
    return width;
  }

  public void setWidth(int newWidth) {
    this.width = newWidth;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int newHeight) {
    this.height = newHeight;
  }

  public float getScale() {
    return scale;
  }

  public void setScale(float newScale) {
    this.scale = newScale;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String newLabel) {
    this.label = newLabel;
  }

  public Window getWindow() {
    return window;
  }

  public Input getInput() {
    return input;
  }
}