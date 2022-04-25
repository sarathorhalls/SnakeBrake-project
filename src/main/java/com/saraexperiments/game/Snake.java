package com.saraexperiments.game;

import java.awt.event.KeyEvent;

import java.util.ArrayList;

import com.saraexperiments.jabble.*;
import com.saraexperiments.jabble.Jabble;

public class Snake implements AbstractGame {

  private int[][] grid;
  private int foodX, foodY, snakeX, snakeY, superX, superY;
  private int temp, temp2, temp3;
  private int scale = 10;
  private int snakeLength = 3;
  private int counter = 0, direction = 0, lastDirection = 0, score = 0;
  private float speed = 1;
  private float time = 0;
  private boolean eaten = true;
  private boolean superEaten = true;
  private boolean running = false;
  private boolean gameover = false;
  private boolean collision = false;
  private ArrayList<int[]> snakeBody;

  @Override
  public void update(Jabble jb, float deltaTime) {
    if (!running) {
      if (jb.getInput().isKey(KeyEvent.VK_SPACE)) {
        // starting variables
        running = true;
        score = 0;
        speed = 1;
        direction = 0;
        lastDirection = 0;
        counter = 0;
        snakeLength = 3;
        collision = false;
        eaten = true;
        // random snake grid pos
        snakeX = (int) (Math.random() * (jb.getWidth() / scale - 1));
        snakeY = (int) (Math.random() * (jb.getHeight() / scale - 4) + 3);
        snakeBody = new ArrayList<int[]>(0);
      }
    }
    if (running) {
      // Grid creation
      grid = new int[jb.getHeight() / scale - 1][jb.getWidth() / scale - 1];
      for (int y = 0; y < grid.length; y++) {
        for (int x = 0; x < grid[0].length; x++) {
          grid[y][x] = 0;
        }
      }
      // snack position
      if (eaten) {
        computeFood(jb.getWidth(), jb.getHeight());
        eaten = false;
      }
      grid[foodY][foodX] = 1;
      if (!superEaten) {
        grid[superY][superX] = 3;
      }

      /**************** Snake Logic ********************/
      // key pressed
      // Up
      if (jb.getInput().isKey(KeyEvent.VK_UP) || jb.getInput().isKey(KeyEvent.VK_W)) {
        direction = 0;
      }
      // Down
      if (jb.getInput().isKey(KeyEvent.VK_DOWN) || jb.getInput().isKey(KeyEvent.VK_S)) {
        direction = 1;
      }
      // Left
      if (jb.getInput().isKey(KeyEvent.VK_LEFT) || jb.getInput().isKey(KeyEvent.VK_A)) {
        direction = 2;
      }
      // Right
      if (jb.getInput().isKey(KeyEvent.VK_RIGHT) || jb.getInput().isKey(KeyEvent.VK_D)) {
        direction = 3;
      }
      // check for collision
      for (int i = 0; i < snakeBody.size() - 1; i++) {
        if (snakeY == snakeBody.get(i)[0] && snakeX == snakeBody.get(i)[1]) {
          collision = true;
        }
      }
      // snake movement
      for (int i = 0; i < snakeBody.size(); i++) {
        grid[snakeBody.get(i)[0]][snakeBody.get(i)[1]] = 2;
      }
      time += deltaTime * speed;
      if (time > 1) {
        if (snakeX == foodX && snakeY == foodY) {
          score++;
          eaten = true;
          snakeLength++;
          speed += 1;
        }
        if (snakeX == superX && snakeY == superY) {
          score += 3;
          speed -= 1;
          superEaten = true;
        }
        switch (direction) {
          // Up
          case 0: {
            if (lastDirection != 1) {
              snakeY--;
              lastDirection = 0;
            } else {
              direction = 1;
              snakeY++;
            }
            break;
          }
          // Down
          case 1: {
            if (lastDirection != 0) {
              snakeY++;
              lastDirection = 1;
            } else {
              direction = 0;
              snakeY--;
            }
            break;
          }
          // Left
          case 2: {
            if (lastDirection != 3) {
              snakeX--;
              lastDirection = 2;
            } else {
              direction = 3;
              snakeX++;
            }
            break;
          }
          // Right
          case 3: {
            if (lastDirection != 2) {
              snakeX++;
              lastDirection = 3;
            } else {
              direction = 2;
              snakeX--;
            }
            break;
          }
        }
        if (counter != snakeLength) {
          counter++;
          if ((snakeX >= 0 && snakeX < jb.getWidth() / scale - 1)
              && (snakeY >= 0 && snakeY < jb.getHeight() / scale - 1) && !collision) {
            snakeBody.add(new int[] { snakeY, snakeX });
          } else {
            running = false;
            gameover = true;
          }
        } else {
          if ((snakeX >= 0 && snakeX < jb.getWidth() / scale - 1)
              && (snakeY >= 0 && snakeY < jb.getHeight() / scale - 1) && !collision) {
            snakeBody.add(new int[] { snakeY, snakeX });
            snakeBody.remove(0);
          } else {
            running = false;
            gameover = true;
          }
        }
        time = 0;
      }
    }
  }

  @Override
  public void render(Jabble jb, Renderer renderer) {
    renderer.setAmbientColor(0xffffffff);
    if (running) {
      // Border
      for (int i = 0; i < jb.getWidth() - scale; i++) {
        renderer.setPixel(i + 5, 5, 0xffffffff);
      }
      for (int i = 0; i < jb.getWidth() - scale; i++) {
        renderer.setPixel(i + 5, jb.getHeight() - 5, 0xffffffff);
      }
      for (int i = 0; i < jb.getHeight() - scale; i++) {
        renderer.setPixel(5, i + 5, 0xffffffff);
      }
      for (int i = 0; i < jb.getHeight() - scale; i++) {
        renderer.setPixel(jb.getWidth() - 5, i + 5, 0xffffffff);
      }

      // Grid rendering
      for (int y = 0; y < grid.length; y++) {
        for (int x = 0; x < grid[0].length; x++) {
          switch (grid[y][x]) {
            // grid
            case 0: {
              if (!(y == 0 && x == 2)) {
                renderer.setPixel(x * scale + ((int) scale), y * scale + ((int) scale), 0x000000);
              }
              break;
            }
            // snack
            case 1: {
              renderer.setPixel(x * scale + ((int) scale), y * scale + ((int) scale), 0xff228B22);
              break;
            }
            // snake
            case 2: {
              renderer.setPixel(x * scale + ((int) scale), y * scale + ((int) scale), 0xffff69b4);
              break;
            }
            case 3: {
              for (int y1 = 0; y1 < 3; y1++) {
                for (int x1 = 0; x1 < 3; x1++) {
                  renderer.setPixel(x1 + x * scale + (int) scale - 1, y1 + y * scale + (int) scale - 1, 0xffd4af37);
                }
              }
            }
          }

        }
      }
      renderer.drawText("Score: " + score, 6, 6, 0xffff69b4);
    } else if (gameover) {
      temp = renderer.drawText("GAMEOVER", (int) ((jb.getWidth() - temp) / 2), (int) (jb.getHeight() / 4), 0xffff69b4);
      temp3 = renderer.drawText("Score: " + score, (int) ((jb.getWidth() - temp3) / 2), (int) (jb.getHeight() / 4) + 8,
          0xffff69b4);
      temp2 = renderer.drawText("Press SPACE to begin", (int) ((jb.getWidth() - temp2) / 2), 150, 0xffDAA520);
    } else {
      // TODO: Create start image
      temp = renderer.drawText("SNAKE9000 3.0 ULTIMATE EDITION", (int) ((jb.getWidth() - temp) / 2),
          (int) (jb.getHeight() / 4), 0xffff69b4);
      temp2 = renderer.drawText("Press SPACE to begin", (int) ((jb.getWidth() - temp2) / 2),
          (int) (jb.getHeight() / 4 * 3), 0xffDAA520);
    }
  }

  public static void main(String[] args) {
    Jabble jb = new Jabble(new Snake());
    jb.start();
  }

  private void computeFood(int width, int height) {
    // set food
    foodX = (int) (Math.random() * (width / scale - 10));
    foodY = (int) (Math.random() * (height / scale - 10));
    // check if food is in snake or score display
    boolean inScoreDisplay = false;
    for (int i = 2; i < 5; i++) {
      if (!inScoreDisplay) {
        inScoreDisplay = (foodY == 0 && foodX == i);
      }
    }
    for (int i = 0; i < snakeBody.size() - 1; i++) {
      if ((foodY == snakeBody.get(i)[0] && foodX == snakeBody.get(i)[1]) || inScoreDisplay) {
        computeFood(width, height);
      }
    }
    // Super food
    int superOdds = (int) (Math.random() * 10);
    if (superOdds == 5 && score > 0) {
      superEaten = false;
      superX = (int) (Math.random() * (width / scale - 1));
      superY = (int) (Math.random() * (height / scale - 1));
    }
  }
}
