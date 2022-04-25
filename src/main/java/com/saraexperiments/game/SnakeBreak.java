package com.saraexperiments.game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.saraexperiments.jabble.*;
import com.saraexperiments.jabble.gfx.*;

public class SnakeBreak implements AbstractGame {

  private static final int BACKGROUNDCOLOR = 0xff16381f;
  private static final int SNAKECOLOR = 0xffff1399;
  private static final int FOODCOLOR = 0xff52e2f7;
  private static final int SCALE = 10;
  private static final int GRIDEXEMPTIONX = 3;
  private static final int GRIDEXEMPTIONY = 3;
  private static final int DISTANCEFROMWALLX = SCALE / 2 * (GRIDEXEMPTIONX);
  private static final int DISTANCEFROMWALLY = SCALE / 2 * (GRIDEXEMPTIONY);
  private final Image heart = new Image("heartSmall.png");
  private final Image healthBar = new Image("healthBarOutline1.png");
  private final Image startImage = new Image("WelcomeArt1.png");
  private static final Image[] startAnimationArray = new Image[18];
  private final Image borderArt = new Image("GameBorderArt.png");
  private final Image backgroundImage = new Image("Motherboard.png");
  private final ImageTile joystick = new ImageTile("JoystickTileset12x12.png", 12, 12);
  private final ImageTile startButton = new ImageTile("ButtonTileset5x5.png", 5, 5);
  private final ImageTile explosion = new ImageTile("explosionTileset.png", 16, 16);

  private int[][] grid;
  private int[][] snakeGrid;

  private float[][] bombTimer;

  private int foodX;
  private int foodY;
  private int snakeX;
  private int snakeY;
  private int superX;
  private int superY;
  private int temp;
  private int temp2;
  private int snakeLength = 3;
  private int counter = 0;
  private int direction = 0;
  private int lastDirection = 0;
  private int score = 0;
  private int lives = 3;

  private double health = 0.25;

  private float joystickMove = 0;
  private float startSnakeMove = 0;
  private float animationTimer = 0;
  private float speed = 1;
  private float time = 0;

  private boolean eaten = true;
  private boolean superEaten = true;
  private boolean running = false;
  private boolean animation = false;
  private boolean start = false;
  private boolean gameover = false;
  private boolean winCondition = false;
  private boolean finalExplosion = false;
  private boolean collision = false;
  private boolean liveCalc = true;

  private ArrayList<int[]> snakeBody;

  @Override
  public void update(Jabble jb, float deltaTime) {
    if (!running && !gameover && !winCondition) {
      if (jb.getInput().isKeyDown(KeyEvent.VK_SPACE)) {
        animation = true;
      }
      if (start) {
        // starting variables
        running = true;
        score = 0;
        health = 0.25;
        lives = 3;
        eaten = true;
        finalExplosion = false;
        // random snake grid pos
        startSnake(jb);
        // creating the main grid
        grid = new int[jb.getHeight() / SCALE - GRIDEXEMPTIONY][jb.getWidth() / SCALE - GRIDEXEMPTIONX];
        bombTimer = new float[jb.getHeight() / SCALE - GRIDEXEMPTIONY][jb.getWidth() / SCALE - GRIDEXEMPTIONX];
        start = false;
      }
      if (!animation) {
        joystickMove += deltaTime;
        startSnakeMove += deltaTime;
        animationTimer = 0;
      } else {
        animationTimer += deltaTime;
      }

    } else if (gameover) {
      int clearX = (int) (Math.random() * (jb.getWidth() / SCALE - GRIDEXEMPTIONX));
      int clearY = (int) (Math.random() * (jb.getHeight() / SCALE - GRIDEXEMPTIONY));
      if (grid[clearY][clearX] == 4) {
        grid[clearY][clearX] = 0;
      }
      if (jb.getInput().isKeyDown(KeyEvent.VK_SPACE)) {
        gameover = false;
        startSnakeMove = 0;
      }
    } else if (winCondition) {
      startSnakeMove += deltaTime;
      int clearX = (int) (Math.random() * (jb.getWidth() / SCALE - GRIDEXEMPTIONX));
      int clearY = (int) (Math.random() * (jb.getHeight() / SCALE - GRIDEXEMPTIONY));
      if (grid[clearY][clearX] < 4) {
        grid[clearY][clearX] = 5;
        bombTimer[clearY][clearX] = 1;
      }
      int spacesLeft = 0;
      for (int y = 0; y < bombTimer.length; y++) {
        for (int x = 0; x < bombTimer[0].length; x++) {
          if (finalExplosion && grid[y][x] < 4) {
            grid[y][x] = 5;
            bombTimer[y][x] = 1;
          }
          if (bombTimer[y][x] > 0) {
            bombTimer[y][x] += deltaTime;
          }
          if (grid[y][x] < 4) {
            spacesLeft++;
          }
        }
      }
      if (spacesLeft < 15) {
        finalExplosion = true;
      }
      if (jb.getInput().isKeyDown(KeyEvent.VK_SPACE) && finalExplosion) {
        winCondition = false;
        startSnakeMove = 0;
      }
    }
    if (running) {
      // Grid creation

      snakeGrid = new int[grid.length][grid[0].length];
      for (int y = 0; y < grid.length; y++) {
        for (int x = 0; x < grid[0].length; x++) {
          snakeGrid[y][x] = 0;
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
        snakeGrid[snakeBody.get(i)[0]][snakeBody.get(i)[1]] = 1;
      }
      time += deltaTime * speed;
      if (time > 1) {
        if (snakeX == foodX && snakeY == foodY) {
          score++;
          eaten = true;
          liveCalc = true;
          grid[foodY][foodX] = 4;
          snakeLength++;
          speed += 0.7;
          health += 0.75;
          if (health >= 100) {
            running = false;
            winCondition = true;
          }
        }
        if ((snakeX == superX && snakeY == superY) && grid[superY][superX] != 4) {
          if (!(score < 5)) {
            score -= 5;
            speed -= 3;
            superEaten = true;
            grid[superY][superX] = 4;
            health += 0.75;
          } else {
            superEaten = true;
            grid[superY][superX] = 4;
            health += 0.75;
          }
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
          // when snake size is inconsitant
          counter++;
          // Collision checks
          if (!collision) {
            if (!collision) {
              // movement if snake is on-screen
              if ((snakeX >= 0 && snakeX < jb.getWidth() / SCALE - GRIDEXEMPTIONX)
                  && (snakeY >= 0 && snakeY < jb.getHeight() / SCALE - GRIDEXEMPTIONY)) {
                snakeBody.add(new int[] { snakeY, snakeX });
              }
              // Teleporting
              else if (snakeX < 0) {
                // Left-side
                snakeX += jb.getWidth() / SCALE - GRIDEXEMPTIONX;
                snakeBody.add(new int[] { snakeY, snakeX });
              } else if (snakeX >= jb.getWidth() / SCALE - GRIDEXEMPTIONX) {
                // Right-side
                snakeX = 0;
                snakeBody.add(new int[] { snakeY, snakeX });
              } else if (snakeY < 0) {
                // Top
                snakeY += (jb.getHeight() / SCALE - GRIDEXEMPTIONY);
                snakeBody.add(new int[] { snakeY, snakeX });
              } else {
                // Bottom
                snakeY = 0;
                snakeBody.add(new int[] { snakeY, snakeX });
              }
            }
            // check if food has been eaten yet and only runs when collision = true
            else if (liveCalc) {
              lives--;
              liveCalc = false;
              // gameover condition
              if (lives == 0) {
                running = false;
                gameover = true;
              } else {
                // restart snake
                startSnake(jb);
              }
            }
          }
        } else {
          if (!collision) {
            // if snake is on-screen
            if ((snakeX >= 0 && snakeX < jb.getWidth() / SCALE - GRIDEXEMPTIONX)
                && (snakeY >= 0 && snakeY < jb.getHeight() / SCALE - GRIDEXEMPTIONY)) {
              snakeBody.add(new int[] { snakeY, snakeX });
              snakeBody.remove(0);
            }
            // Teleporting
            else if (snakeX < 0) {
              // Left-side
              snakeX += jb.getWidth() / SCALE - GRIDEXEMPTIONX;
              snakeBody.add(new int[] { snakeY, snakeX });
              snakeBody.remove(0);
            } else if (snakeX >= jb.getWidth() / SCALE - GRIDEXEMPTIONX) {
              // Right-side
              snakeX = 0;
              snakeBody.add(new int[] { snakeY, snakeX });
              snakeBody.remove(0);
            } else if (snakeY < 0) {
              // Top
              snakeY += (jb.getHeight() / SCALE - GRIDEXEMPTIONY);
              snakeBody.add(new int[] { snakeY, snakeX });
              snakeBody.remove(0);
            } else {
              // Bottom
              snakeY = 0;
              snakeBody.add(new int[] { snakeY, snakeX });
              snakeBody.remove(0);
            }
          }
          // check if food has been eaten yet and only runs when collision = true
          else if (liveCalc) {
            lives--;
            liveCalc = false;
            // gameover condition
            if (lives == 0) {
              running = false;
              gameover = true;
            } else {
              // restart snake
              startSnake(jb);
            }
          }
        }
        time = 0;
      }
    }
  }

  @Override
  public void render(Jabble jb, Renderer renderer) {
    /****************************************
     * Rendering Main game
     *************************************************/
    // Engine has lighting, this turns it off
    renderer.setAmbientColor(0xffffffff);
    if (running) {
      /*
       * Map Border
       * //Top
       * for(int i = 0; i < jb.getWidth() - SCALE; i++) {
       * renderer.setPixel(i + 5, 5, BACKGROUNDCOLOR);
       * }
       * //Bottom
       * for(int i = 0; i < jb.getWidth() - SCALE; i++) {
       * renderer.setPixel(i + 5, jb.getHeight() - 5, BACKGROUNDCOLOR);
       * }
       * //Left-side
       * for(int i = 0; i < jb.getHeight() - SCALE; i++) {
       * renderer.setPixel(5, i + 5, BACKGROUNDCOLOR);
       * }
       * //Right-side
       * for(int i = 0; i < jb.getHeight() - SCALE; i++) {
       * renderer.setPixel(jb.getWidth() - 5, i + 5, BACKGROUNDCOLOR);
       * }
       */

      drawBackground(renderer);
      for (int y = 0; y < grid.length; y++) {
        for (int x = 0; x < grid[0].length; x++) {
          switch (grid[y][x]) {
            // grid
            case 4: {
              renderer.setPixel(x * SCALE + (DISTANCEFROMWALLX + (SCALE / 2)),
                  y * SCALE + (DISTANCEFROMWALLY + (SCALE / 2)), 0x00000000);
              break;
            }
            case 2: {
              renderer.setPixel(x * SCALE + (DISTANCEFROMWALLX + (SCALE / 2)),
                  y * SCALE + (DISTANCEFROMWALLY + (SCALE / 2)), 0xff000000);
              break;
            }
            // snack
            case 1: {
              renderer.setPixel(x * SCALE + (DISTANCEFROMWALLX + (SCALE / 2)),
                  y * SCALE + (DISTANCEFROMWALLY + (SCALE / 2)), FOODCOLOR);
              break;
            }
            // super snack
            case 3: {
              for (int y1 = 0; y1 < 3; y1++) {
                for (int x1 = 0; x1 < 3; x1++) {
                  renderer.setPixel(x1 + x * SCALE + (DISTANCEFROMWALLX + (SCALE / 2)) - 1,
                      y1 + y * SCALE + (DISTANCEFROMWALLY + (SCALE / 2)) - 1, 0xffd4af37);
                }
              }
            }
          }
          // Drawing the snake
          if (snakeGrid[y][x] == 1) {
            renderer.setPixel(x * SCALE + (DISTANCEFROMWALLX + (SCALE / 2)),
                y * SCALE + (DISTANCEFROMWALLY + (SCALE / 2)), SNAKECOLOR);
          }
        }
      }
      renderer.drawImage(borderArt, 0, 0);
      // Drawing Lives
      for (int i = 0; i < lives; i++) {
        renderer.drawImage(heart, 20 + i * (heart.getWidth() - 4), 10);
      }
      // Setting color of healthbar
      int healthColor;
      if (health <= 10) {
        healthColor = 0xff00ff00;
      } else if (health <= 20) {
        healthColor = 0xff33ff00;
      } else if (health <= 30) {
        healthColor = 0xff66ff00;
      } else if (health <= 40) {
        healthColor = 0xff99ff00;
      } else if (health <= 50) {
        healthColor = 0xffCCff00;
      } else if (health <= 60) {
        healthColor = 0xffffff00;
      } else if (health <= 70) {
        healthColor = 0xffffcc00;
      } else if (health <= 80) {
        healthColor = 0xffff9900;
      } else if (health <= 90) {
        healthColor = 0xffff6600;
      } else if (health <= 95) {
        healthColor = 0xffff3300;
      } else {
        healthColor = 0xffff0000;
      }
      // drawing healthbar
      renderer.drawFilledRect(112, 10, (int) (0.96 * health), 8, healthColor);
      // drawing healthbar-border
      renderer.drawImage(healthBar, 110, 8);
      // drwaing healthbar-percentage
      renderer.drawText((int) health + "%", 212, 8, 0xffffffff);
    } else if (gameover) {
      /*******************************
       * gameover screen
       *********************************/
      drawBackground(renderer);
      renderer.drawImage(borderArt, 0, 0);
      // Setting color of healthbar
      int healthColor;
      if (health <= 10) {
        healthColor = 0xff00ff00;
      } else if (health <= 20) {
        healthColor = 0xff33ff00;
      } else if (health <= 30) {
        healthColor = 0xff66ff00;
      } else if (health <= 40) {
        healthColor = 0xff99ff00;
      } else if (health <= 50) {
        healthColor = 0xffCCff00;
      } else if (health <= 60) {
        healthColor = 0xffffff00;
      } else if (health <= 70) {
        healthColor = 0xffffcc00;
      } else if (health <= 80) {
        healthColor = 0xffff9900;
      } else if (health <= 90) {
        healthColor = 0xffff6600;
      } else if (health <= 95) {
        healthColor = 0xffff3300;
      } else {
        healthColor = 0xffff0000;
      }
      // drawing healthbar
      renderer.drawFilledRect(112, 10, (int) (0.96 * health), 8, healthColor);
      // drawing healthbar-border
      renderer.drawImage(healthBar, 110, 8);
      // drwaing healthbar-percentage
      renderer.drawText((int) health + "%", 212, 8, 0xffffffff);
      temp = renderer.drawText("GAMEOVER", (int) ((jb.getWidth() - temp) / 2), (int) (jb.getHeight() / 4), SNAKECOLOR);
      temp2 = renderer.drawText("Press SPACE to retry", (int) ((jb.getWidth() - temp2) / 2), 150, FOODCOLOR);
    } else if (winCondition) {
      /********************************* Win Screen ********************************/
      drawBackground(renderer);
      renderer.drawImage(borderArt, 0, 0);
      // Setting color of healthbar
      int healthColor;
      if (health <= 10) {
        healthColor = 0xff00ff00;
      } else if (health <= 20) {
        healthColor = 0xff33ff00;
      } else if (health <= 30) {
        healthColor = 0xff66ff00;
      } else if (health <= 40) {
        healthColor = 0xff99ff00;
      } else if (health <= 50) {
        healthColor = 0xffCCff00;
      } else if (health <= 60) {
        healthColor = 0xffffff00;
      } else if (health <= 70) {
        healthColor = 0xffffcc00;
      } else if (health <= 80) {
        healthColor = 0xffff9900;
      } else if (health <= 90) {
        healthColor = 0xffff6600;
      } else if (health <= 95) {
        healthColor = 0xffff3300;
      } else {
        healthColor = 0xffff0000;
      }
      // drawing healthbar
      renderer.drawFilledRect(112, 10, (int) (0.96 * health), 8, healthColor);
      // drawing healthbar-border
      renderer.drawImage(healthBar, 110, 8);
      // drwaing healthbar-percentage
      renderer.drawText((int) health + "%", 212, 8, 0xffffffff);
      if (startSnakeMove < 1.3) {
        temp = renderer.drawText("COMPUTER MALFUNCTION", (int) ((jb.getWidth() - temp) / 2),
            (int) (jb.getHeight() / 4) - 6, SNAKECOLOR);
      } else if (startSnakeMove > 2.6) {
        startSnakeMove = 0;
      }
      if (finalExplosion) {
        temp2 = renderer.drawText("Press SPACE to begin again", (int) ((jb.getWidth() - temp2) / 2), 146, FOODCOLOR);
      }
    } else {
      /*************************
       * Welcome Screen
       ***************************************/
      if (!animation) {
        renderer.drawImage(startImage, 0, 0);
        int joystickDirection = joystickDirectionFunc();
        renderer.drawImageTile(joystick, 126, 86, joystickDirection, 0);
        renderer.drawImageTile(startButton, 150, 86, 0, 0);
        drawTinySnake(renderer);
        /**************************
         * Snake animation on the start screen
         ************************************/
      } else {
        if (animationTimer < 0.8) {
          renderer.drawImage(startImage, 0, 0);
          renderer.drawImageTile(joystick, 126, 86, 0, 0);
          renderer.drawImageTile(startButton, 150, 86, 1, 0);
        } else if (animationTimer < 1.0) {
          renderer.drawImage(startImage, 0, 0);
          renderer.drawImageTile(joystick, 126, 86, 0, 0);
          renderer.drawImageTile(startButton, 150, 86, 0, 0);
        } else if (animationTimer > 1.0) {
          for (int i = 0; i < startAnimationArray.length; i++) {
            if (animationTimer > 1 + (0.05 * i)) {
              renderer.drawImage(startAnimationArray[i], ((320 - startAnimationArray[i].getWidth()) / 2),
                  -(9 * (i + 1)));
            }
            if (animationTimer > 1 + (0.05 * startAnimationArray.length)) {
              animation = false;
              start = true;
            }
          }
        }
      }
    }
  }

  /************************ Main Function *****************************/
  public static void main(String[] args) {
    Jabble jb = new Jabble(new SnakeBreak());
    jb.setLabel("Snake Breaker");
    for (int i = 0; i < startAnimationArray.length; i++) {
      startAnimationArray[i] = new Image("WelcomeArt" + (i + 2) + ".png");
    }

    jb.start();
  }

  /**
   * calculates and places food and super food on grid
   * 
   * @param width
   * @param height
   */
  private void computeFood(int width, int height) {
    // set food randomly
    foodX = (int) (Math.random() * (width / SCALE - GRIDEXEMPTIONX));
    foodY = (int) (Math.random() * (height / SCALE - GRIDEXEMPTIONY));
    // check if food is in snake
    for (int i = 0; i < snakeBody.size() - 1; i++) {
      if ((foodY == snakeBody.get(i)[0] && foodX == snakeBody.get(i)[1]) || grid[foodY][foodX] == 4) {
        computeFood(width, height);
      }
    }
    // Super food
    int superOdds = (int) (Math.random() * 10);
    if (superOdds == 5 && score > 5) {
      if (!superEaten) {
        grid[superY][superX] = 0;
      }
      superEaten = false;
      superX = (int) (Math.random() * (width / SCALE - GRIDEXEMPTIONX));
      superY = (int) (Math.random() * (height / SCALE - GRIDEXEMPTIONY));
    }
  }

  /**
   * Sets the start parameters for the snake
   * 
   * @param jb game engine
   */
  public void startSnake(Jabble jb) {
    speed = 1;
    direction = 0;
    lastDirection = 0;
    counter = 0;
    snakeLength = 3;
    collision = false;
    score = 0;
    snakeX = (int) (Math.random() * (jb.getWidth() / SCALE - GRIDEXEMPTIONX));
    snakeY = (int) (Math.random() * (jb.getHeight() / SCALE - GRIDEXEMPTIONY - 3) + 3);
    snakeBody = new ArrayList<int[]>(0);
  }

  /**
   * @return returns what direction the joystick should point in
   */
  public int joystickDirectionFunc() {
    if (joystickMove > 9) {
      joystickMove = 0;
      return 0;
    } else if (joystickMove > 8.8) {
      return 2;
    } else if (joystickMove > 8.3) {
      return 0;
    } else if (joystickMove > 8.2) {
      return 1;
    } else if (joystickMove > 8) {
      return 0;
    } else if (joystickMove > 7.8) {
      return 2;
    } else if (joystickMove > 7.6) {
      return 0;
    } else if (joystickMove > 7.4) {
      return 2;
    } else if (joystickMove > 7) {
      return 0;
    } else if (joystickMove > 6.8) {
      return 1;
    } else if (joystickMove > 4.6) {
      return 0;
    } else if (joystickMove > 4.4) {
      return 1;
    } else if (joystickMove > 4.2) {
      return 0;
    } else if (joystickMove > 4) {
      return 1;
    } else if (joystickMove > 3.4) {
      return 0;
    } else if (joystickMove > 3.2) {
      return 2;
    } else if (joystickMove > 3) {
      return 0;
    } else if (joystickMove > 2.8) {
      return 1;
    } else if (joystickMove > 2) {
      return 0;
    } else if (joystickMove > 1.8) {
      return 2;
    } else if (joystickMove > 1.2) {
      return 0;
    } else if (joystickMove > 1) {
      return 2;
    } else if (joystickMove > 0.8) {
      return 0;
    } else {
      return 0;
    }
  }

  private void drawBackground(Renderer renderer) {
    // Background Image Rendering
    renderer.drawImage(backgroundImage, 0, 0);

    // Grid rendering
    for (int y = 0; y < grid.length; y++) {
      for (int x = 0; x < grid[0].length; x++) {
        // checks if grid is supposed to be empty
        if (grid[y][x] == 4) {
          boolean[] broken = new boolean[4];
          // makes calculations based off the outer walls
          if (y == 0) {
            broken[1] = true;
          }
          if (x == 0) {
            broken[3] = true;
          }
          if (y == grid.length - 1) {
            broken[0] = true;
          }
          if (x == grid[0].length - 1) {
            broken[2] = true;
          }
          // checks every side to see wheter it's broken
          // bottom check
          if (broken[0] || grid[y + 1][x] == 4) {
            broken[0] = true;
          }
          // top check
          if (broken[1] || grid[y - 1][x] == 4) {
            broken[1] = true;
          }
          // right check
          if (broken[2] || grid[y][x + 1] == 4) {
            broken[2] = true;
          }
          // left check
          if (broken[3] || grid[y][x - 1] == 4) {
            broken[3] = true;
          }
          // side painting
          // bottom
          if (!broken[0]) {
            for (int x1 = 2; x1 < 6; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + 8, BACKGROUNDCOLOR);
            }
            for (int x1 = 1; x1 < 5; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + 9, BACKGROUNDCOLOR);
            }
          }
          // top
          if (!broken[1]) {
            for (int x1 = 2; x1 < 5; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + 0, BACKGROUNDCOLOR);
            }
            for (int x1 = 7; x1 < 10; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + 0, BACKGROUNDCOLOR);
            }
            renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 3, y * SCALE + DISTANCEFROMWALLY + 1, BACKGROUNDCOLOR);
            for (int x1 = 6; x1 < 10; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + 1, BACKGROUNDCOLOR);
            }
            renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 9, y * SCALE + DISTANCEFROMWALLY + 2, BACKGROUNDCOLOR);
          }
          // right
          if (!broken[2]) {
            for (int y1 = 4; y1 < 6; y1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 9, y * SCALE + DISTANCEFROMWALLY + y1, BACKGROUNDCOLOR);
            }
            renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 7, y * SCALE + DISTANCEFROMWALLY + 9, BACKGROUNDCOLOR);
            for (int y1 = 7; y1 < 10; y1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 8, y * SCALE + DISTANCEFROMWALLY + y1, BACKGROUNDCOLOR);
            }
            for (int y1 = 8; y1 < 10; y1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 9, y * SCALE + DISTANCEFROMWALLY + y1, BACKGROUNDCOLOR);
            }
          }
          // left
          if (!broken[3]) {
            for (int y1 = 1; y1 < 8; y1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 0, y * SCALE + DISTANCEFROMWALLY + y1, BACKGROUNDCOLOR);
            }
            renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 1, y * SCALE + DISTANCEFROMWALLY + 2, BACKGROUNDCOLOR);
            for (int y1 = 4; y1 < 7; y1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + 1, y * SCALE + DISTANCEFROMWALLY + y1, BACKGROUNDCOLOR);
            }
          }
        } else if (grid[y][x] == 5) {
          if (bombTimer[y][x] < 1.2) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 0,
                0);
          } else if (bombTimer[y][x] < 1.3) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 1,
                0);
          } else if (bombTimer[y][x] < 1.4) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 2,
                0);
          } else if (bombTimer[y][x] < 1.5) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 3,
                0);
          } else if (bombTimer[y][x] < 1.6) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 0,
                1);
          } else if (bombTimer[y][x] < 1.7) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 1,
                1);
          } else if (bombTimer[y][x] < 1.8) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 2,
                1);
          } else if (bombTimer[y][x] < 1.9) {
            renderer.drawImageTile(explosion, x * SCALE + DISTANCEFROMWALLX - 3, y * SCALE + DISTANCEFROMWALLY - 3, 3,
                1);
          } else if (bombTimer[y][x] > 1.9) {
            bombTimer[y][x] = 0;
            grid[y][x] = 4;
          }
        } else {
          for (int y1 = 0; y1 < SCALE; y1++) {
            for (int x1 = 0; x1 < SCALE; x1++) {
              renderer.setPixel(x * SCALE + DISTANCEFROMWALLX + x1, y * SCALE + DISTANCEFROMWALLY + y1,
                  BACKGROUNDCOLOR);
            }
          }
        }
      }
    }
  }

  private void drawTinySnake(Renderer renderer) {
    if (startSnakeMove < 1) {
      // snake
      renderer.setPixel(136, 58, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 2) {
      // snake
      renderer.setPixel(136, 58, SNAKECOLOR);
      renderer.setPixel(138, 58, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 3) {
      // snake
      renderer.setPixel(136, 58, SNAKECOLOR);
      renderer.setPixel(138, 58, SNAKECOLOR);
      renderer.setPixel(140, 58, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 4) {
      // snake
      renderer.setPixel(138, 58, SNAKECOLOR);
      renderer.setPixel(140, 58, SNAKECOLOR);
      renderer.setPixel(142, 58, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 5) {
      // snake
      renderer.setPixel(140, 58, SNAKECOLOR);
      renderer.setPixel(142, 58, SNAKECOLOR);
      renderer.setPixel(144, 58, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 6) {
      // snake
      renderer.setPixel(142, 58, SNAKECOLOR);
      renderer.setPixel(144, 58, SNAKECOLOR);
      renderer.setPixel(144, 56, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 7) {
      // snake
      renderer.setPixel(144, 58, SNAKECOLOR);
      renderer.setPixel(144, 56, SNAKECOLOR);
      renderer.setPixel(144, 54, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 8) {
      // snake
      renderer.setPixel(144, 56, SNAKECOLOR);
      renderer.setPixel(144, 54, SNAKECOLOR);
      renderer.setPixel(144, 52, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 9) {
      // snake
      renderer.setPixel(144, 54, SNAKECOLOR);
      renderer.setPixel(144, 52, SNAKECOLOR);
      renderer.setPixel(144, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 10) {
      // snake
      renderer.setPixel(144, 52, SNAKECOLOR);
      renderer.setPixel(144, 50, SNAKECOLOR);
      renderer.setPixel(146, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 11) {
      // snake
      renderer.setPixel(144, 50, SNAKECOLOR);
      renderer.setPixel(146, 50, SNAKECOLOR);
      renderer.setPixel(148, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 12) {
      // snake
      renderer.setPixel(146, 50, SNAKECOLOR);
      renderer.setPixel(148, 50, SNAKECOLOR);
      renderer.setPixel(150, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 13) {
      // snake
      renderer.setPixel(148, 50, SNAKECOLOR);
      renderer.setPixel(150, 50, SNAKECOLOR);
      renderer.setPixel(152, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 14) {
      // snake
      renderer.setPixel(150, 50, SNAKECOLOR);
      renderer.setPixel(152, 50, SNAKECOLOR);
      renderer.setPixel(154, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 15) {
      // snake
      renderer.setPixel(152, 50, SNAKECOLOR);
      renderer.setPixel(154, 50, SNAKECOLOR);
      renderer.setPixel(156, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 16) {
      // snake
      renderer.setPixel(154, 50, SNAKECOLOR);
      renderer.setPixel(156, 50, SNAKECOLOR);
      renderer.setPixel(158, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 17) {
      // snake
      renderer.setPixel(156, 50, SNAKECOLOR);
      renderer.setPixel(158, 50, SNAKECOLOR);
      renderer.setPixel(160, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 18) {
      // snake
      renderer.setPixel(158, 50, SNAKECOLOR);
      renderer.setPixel(160, 50, SNAKECOLOR);
      renderer.setPixel(162, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 19) {
      // snake
      renderer.setPixel(160, 50, SNAKECOLOR);
      renderer.setPixel(162, 50, SNAKECOLOR);
      renderer.setPixel(164, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 20) {
      // snake
      renderer.setPixel(162, 50, SNAKECOLOR);
      renderer.setPixel(164, 50, SNAKECOLOR);
      renderer.setPixel(166, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 21) {
      // snake
      renderer.setPixel(164, 50, SNAKECOLOR);
      renderer.setPixel(166, 50, SNAKECOLOR);
      renderer.setPixel(168, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 22) {
      // snake
      renderer.setPixel(166, 50, SNAKECOLOR);
      renderer.setPixel(168, 50, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 23) {
      // snake
      renderer.setPixel(168, 50, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(172, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 24) {
      // snake
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(172, 50, SNAKECOLOR);
      renderer.setPixel(174, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 25) {
      // snake
      renderer.setPixel(172, 50, SNAKECOLOR);
      renderer.setPixel(174, 50, SNAKECOLOR);
      renderer.setPixel(176, 50, SNAKECOLOR);
      // food
      renderer.setPixel(176, 52, FOODCOLOR);
    } else if (startSnakeMove < 26) {
      // snake
      renderer.setPixel(174, 50, SNAKECOLOR);
      renderer.setPixel(176, 50, SNAKECOLOR);
      renderer.setPixel(176, 52, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 27) {
      // snake
      renderer.setPixel(174, 50, SNAKECOLOR);
      renderer.setPixel(176, 50, SNAKECOLOR);
      renderer.setPixel(176, 52, SNAKECOLOR);
      renderer.setPixel(174, 52, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 27.8) {
      // snake
      renderer.setPixel(176, 50, SNAKECOLOR);
      renderer.setPixel(176, 52, SNAKECOLOR);
      renderer.setPixel(174, 52, SNAKECOLOR);
      renderer.setPixel(172, 52, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 28.6) {
      // snake
      renderer.setPixel(176, 52, SNAKECOLOR);
      renderer.setPixel(174, 52, SNAKECOLOR);
      renderer.setPixel(172, 52, SNAKECOLOR);
      renderer.setPixel(170, 52, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 29.2) {
      // snake
      renderer.setPixel(174, 52, SNAKECOLOR);
      renderer.setPixel(172, 52, SNAKECOLOR);
      renderer.setPixel(170, 52, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 30) {
      // snake
      renderer.setPixel(172, 52, SNAKECOLOR);
      renderer.setPixel(170, 52, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(170, 48, SNAKECOLOR);
      // food
      renderer.setPixel(170, 46, FOODCOLOR);
    } else if (startSnakeMove < 30.8) {
      // snake
      renderer.setPixel(170, 52, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(170, 48, SNAKECOLOR);
      renderer.setPixel(170, 46, SNAKECOLOR);
      // food
      renderer.setPixel(174, 52, FOODCOLOR);
    } else if (startSnakeMove < 31.5) {
      // snake
      renderer.setPixel(170, 52, SNAKECOLOR);
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(170, 48, SNAKECOLOR);
      renderer.setPixel(170, 46, SNAKECOLOR);
      renderer.setPixel(168, 46, SNAKECOLOR);
      // food
      renderer.setPixel(174, 52, FOODCOLOR);
    } else if (startSnakeMove < 32.2) {
      // snake
      renderer.setPixel(170, 50, SNAKECOLOR);
      renderer.setPixel(170, 48, SNAKECOLOR);
      renderer.setPixel(170, 46, SNAKECOLOR);
      renderer.setPixel(168, 46, SNAKECOLOR);
      renderer.setPixel(168, 48, SNAKECOLOR);
      // food
      renderer.setPixel(174, 52, FOODCOLOR);
    } else if (startSnakeMove < 32.9) {
      // snake
      renderer.setPixel(170, 48, SNAKECOLOR);
      renderer.setPixel(170, 46, SNAKECOLOR);
      renderer.setPixel(168, 46, SNAKECOLOR);
      renderer.setPixel(168, 48, SNAKECOLOR);
      // food
      renderer.setPixel(174, 52, FOODCOLOR);
    } else if (startSnakeMove < 34) {
      // food
      renderer.setPixel(174, 52, FOODCOLOR);
    } else if (startSnakeMove > 34) {
      startSnakeMove = 0;
    }
  }
}
