package com.saraexperiments.jabble.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {
  private int width;
  private int height;
  private int[] pixelArray;
  private boolean alpha = false;
  private int lightBlock = Light.NONE;

  public Image(String path) {
    BufferedImage image = null;

    try {
      image = ImageIO.read(getClass().getResourceAsStream(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (image != null) {
      width = image.getWidth();
      height = image.getHeight();
      pixelArray = image.getRGB(0, 0, width, height, null, 0, width);
      image.flush();
    }
  }

  public Image(int[] pixelArray, int width, int height) {
    this.pixelArray = pixelArray;
    this.width = width;
    this.height = height;
  }

  // getters and setters
  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int[] getPixelArray() {
    return pixelArray;
  }

  public void setPixelArray(int[] pixelArray) {
    this.pixelArray = pixelArray;
  }

  public boolean isAlpha() {
    return alpha;
  }

  public void setAlpha(boolean alpha) {
    this.alpha = alpha;
  }

  public int getLightBlock() {
    return lightBlock;
  }

  public void setLightBlock(int lightBlock) {
    this.lightBlock = lightBlock;
  }
}