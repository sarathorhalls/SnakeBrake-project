package com.saraexperiments.jabble;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.saraexperiments.jabble.gfx.*;

public class Renderer {
  private ArrayList<ImageRequest> imageRequest = new ArrayList<ImageRequest>();
  private ArrayList<LightRequest> lightRequest = new ArrayList<LightRequest>();

  private int pixelWidth;
  private int pixelHeight;
  private int[] pixelArray;
  private int[] zBuffer;
  private int[] lightMap;
  private int[] lightBlock;

  private int ambientColor = 0xff232323;
  private int zDepth = 0;
  private boolean processing = false;

  private Font font = Font.STANDARD;

  public Renderer(Jabble jb) {
    pixelWidth = jb.getWidth();
    pixelHeight = jb.getHeight();
    pixelArray = ((DataBufferInt) jb.getWindow().getImage().getRaster().getDataBuffer()).getData();
    zBuffer = new int[pixelArray.length];
    lightMap = new int[pixelArray.length];
    lightBlock = new int[pixelArray.length];
  }

  public void clear() {
    for (int i = 0; i < pixelArray.length; i++) {
      pixelArray[i] = 0;
      zBuffer[i] = 0;
      lightMap[i] = ambientColor;
      lightBlock[i] = 0;
    }
  }

  public void process() {
    processing = true;

    Collections.sort(imageRequest, new Comparator<ImageRequest>() {
      @Override
      public int compare(ImageRequest i0, ImageRequest i1) {
        if (i0.zDepth < i1.zDepth) {
          return -1;
        }
        if (i0.zDepth < i1.zDepth) {
          return 1;
        }
        return 0;
      }

    });

    for (int i = 0; i < imageRequest.size(); i++) {
      ImageRequest ir = imageRequest.get(i);
      setzDepth(ir.zDepth);
      drawImage(ir.image, ir.offsetX, ir.offsetY);
    }

    for (int i = 0; i < lightRequest.size(); i++) {
      LightRequest l = lightRequest.get(i);
      this.drawLightRequest(l.light, l.locX, l.locY);
    }

    for (int i = 0; i < pixelArray.length; i++) {
      float r = ((lightMap[i] >> 16) & 0xff) / 255f;
      float g = ((lightMap[i] >> 8) & 0xff) / 255f;
      float b = ((lightMap[i]) & 0xff) / 255f;

      pixelArray[i] = ((int) (((pixelArray[i] >> 16) & 0xff) * r) << 16 | (int) (((pixelArray[i] >> 8) & 0xff) * g) << 8
          | (int) (((pixelArray[i]) & 0xff) * b));
    }

    imageRequest.clear();
    lightRequest.clear();
    processing = false;
  }

  public void setPixel(int x, int y, int value) {

    int alpha = (value >> 24 & 0xff);

    if ((x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight) || alpha == 0) {
      return;
    }

    int index = x + y * pixelWidth;

    if (zBuffer[index] > zDepth) {
      return;
    }

    zBuffer[index] = zDepth;

    if (alpha == 255) {
      pixelArray[index] = value;
    } else {
      int pixelColor = pixelArray[index];

      int newRed = ((pixelColor >> 16) & 0xff)
          - (int) ((((pixelColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * (alpha / 255f));
      int newGreen = ((pixelColor >> 8) & 0xff)
          - (int) ((((pixelColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * (alpha / 255f));
      int newBlue = (pixelColor & 0xff) - (int) (((pixelColor & 0xff) - (value & 0xff)) * (alpha / 255f));

      pixelArray[index] = (255 << 24 | newRed << 16 | newGreen << 8 | newBlue);
    }
  }

  public void setLightMap(int x, int y, int value) {

    if ((x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight)) {
      return;
    }

    int baseColor = lightMap[x + y + pixelWidth];

    int maxRed = Math.max(((baseColor >> 16) & 0xff), ((value >> 16) & 0xff));
    int maxGreen = Math.max(((baseColor >> 8) & 0xff), ((value >> 8) & 0xff));
    int maxBlue = Math.max(((baseColor) & 0xff), ((value) & 0xff));

    lightMap[x + y * pixelWidth] = (maxRed << 16 | maxGreen << 8 | maxBlue);
  }

  public void setLightBlock(int x, int y, int value) {

    if ((x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight)) {
      return;
    }

    if (zBuffer[x + y * pixelWidth] > zDepth) {
      return;
    }

    lightBlock[x + y * pixelWidth] = value;
  }

  public void drawImage(Image image, int offsetX, int offsetY) {

    if (image.isAlpha() && !processing) {
      imageRequest.add(new ImageRequest(image, zDepth, offsetX, offsetY));
      return;
    }

    // Offscreen
    if (offsetX + image.getWidth() < 0)
      return;
    if (offsetY + image.getHeight() < 0)
      return;
    if (offsetX >= pixelWidth)
      return;
    if (offsetY >= pixelHeight)
      return;

    // temp variables
    int newX = 0;
    int newY = 0;
    int newWidth = image.getWidth();
    int newHeight = image.getHeight();

    // clipping
    if (offsetX < 0) {
      newX -= offsetX;
    }
    if (offsetY < 0) {
      newY -= offsetY;
    }
    if (newWidth + offsetX >= pixelWidth) {
      newWidth -= newWidth + offsetX - pixelWidth;
    }
    if (newHeight + offsetY >= pixelHeight) {
      newHeight -= newHeight + offsetY - pixelHeight;
    }

    for (int y = newY; y < newHeight; y++) {
      for (int x = newX; x < newWidth; x++) {
        setPixel(x + offsetX, y + offsetY, image.getPixelArray()[x + y * image.getWidth()]);
        setLightBlock(x + offsetX, y + offsetY, image.getLightBlock());
      }
    }
  }

  public void drawImageTile(ImageTile image, int offsetX, int offsetY, int tileX, int tileY) {

    if (image.isAlpha() && !processing) {
      imageRequest.add(new ImageRequest(image.getTileImage(tileX, tileY), zDepth, offsetX, offsetY));
      return;
    }

    // Offscreen
    if (offsetX + image.getTileWidth() < 0)
      return;
    if (offsetY + image.getTileHeight() < 0)
      return;
    if (offsetX >= pixelWidth)
      return;
    if (offsetY >= pixelHeight)
      return;

    // temp variables
    int newX = 0;
    int newY = 0;
    int newWidth = image.getTileWidth();
    int newHeight = image.getTileHeight();

    // clipping
    if (offsetX < 0) {
      newX -= offsetX;
    }
    if (offsetY < 0) {
      newY -= offsetY;
    }
    if (newWidth + offsetX >= pixelWidth) {
      newWidth -= newWidth + offsetX - pixelWidth;
    }
    if (newHeight + offsetY >= pixelHeight) {
      newHeight -= newHeight + offsetY - pixelHeight;
    }

    for (int y = newY; y < newHeight; y++) {
      for (int x = newX; x < newWidth; x++) {
        setPixel(x + offsetX, y + offsetY, image.getPixelArray()[(x + tileX * image.getTileWidth())
            + (y + tileY * image.getTileHeight()) * image.getWidth()]);
        setLightBlock(x + offsetX, y + offsetY, image.getLightBlock());
      }
    }
  }

  public int drawText(String text, int offsetX, int offsetY, int color) {
    int offset = 0;

    for (int i = 0; i < text.length(); i++) {
      int unicode = text.codePointAt(i);
      if (unicode >= 0 && unicode < 256) {
        for (int y = 0; y < font.getFontImage().getHeight(); y++) {
          for (int x = 0; x < font.getWidths()[unicode]; x++) {
            if (font.getFontImage().getPixelArray()[(x + font.getOffsets()[unicode])
                + (y * font.getFontImage().getWidth())] == 0xffffffff) {
              setPixel(x + offsetX + offset, y + offsetY, color);
              // System.out.println("X: " + x + offsetX + offset + ", Y: " + y + offsetY);
            }
          }
        }
        offset += font.getWidths()[unicode];
      }
    }
    return offset;
  }

  public void drawRect(int offsetX, int offsetY, int width, int height, int color) {
    for (int y = 0; y <= height; y++) {
      setPixel(offsetX, y + offsetY, color);
      setPixel(offsetX + width, y + offsetY, color);
    }
    for (int x = 0; x <= width; x++) {
      setPixel(x + offsetX, offsetY, color);
      setPixel(x + offsetX, offsetY + height, color);
    }
  }

  public void drawFilledRect(int offsetX, int offsetY, int width, int height, int color) {

    // Offscreen
    if (offsetX < -width)
      return;
    if (offsetY < -height)
      return;
    if (offsetX >= pixelWidth)
      return;
    if (offsetY >= pixelHeight)
      return;

    // temp variables
    int newX = 0;
    int newY = 0;
    int newWidth = width;
    int newHeight = height;

    // clipping
    if (offsetX < 0) {
      newX -= offsetX;
    }
    if (offsetY < 0) {
      newY -= offsetY;
    }
    if (newWidth + offsetX >= pixelWidth) {
      newWidth -= newWidth + offsetX - pixelWidth;
    }
    if (newHeight + offsetY >= pixelHeight) {
      newHeight -= newHeight + offsetY - pixelHeight;
    }

    for (int y = newY; y < newHeight; y++) {
      for (int x = newX; x < newWidth; x++) {
        setPixel(offsetX + x, offsetY + y, color);
      }
    }
    for (int x = 0; x <= width; x++) {

    }
  }

  public void drawLight(Light l, int offsetX, int offsetY) {
    lightRequest.add(new LightRequest(l, offsetX, offsetY));
  }

  private void drawLightRequest(Light l, int offsetX, int offsetY) {
    for (int i = 0; i <= l.getDiameter(); i++) {
      drawLightLine(l, l.getRadius(), l.getRadius(), i, 0, offsetX, offsetY);
      drawLightLine(l, l.getRadius(), l.getRadius(), i, l.getDiameter(), offsetX, offsetY);
      drawLightLine(l, l.getRadius(), l.getRadius(), 0, i, offsetX, offsetY);
      drawLightLine(l, l.getRadius(), l.getRadius(), l.getDiameter(), i, offsetX, offsetY);
    }
  }

  private void drawLightLine(Light l, int x0, int y0, int x1, int y1, int offsetX, int offsetY) {
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);

    int sx = x0 < x1 ? 1 : -1;
    int sy = y0 < y1 ? 1 : -1;

    int err = dx - dy;
    int e2;

    while (true) {
      int screenX = x0 - l.getRadius() + offsetX;
      int screenY = y0 - l.getRadius() + offsetY;

      if (screenX < 0 || screenX >= pixelWidth || screenY < 0 || screenY >= pixelHeight)
        return;

      int lightColor = l.getLightValue(x0, y0);
      if (lightColor == 0)
        return;

      if (lightBlock[screenX + screenY * pixelWidth] == Light.FULL)
        return;

      setLightMap(screenX, screenY, lightColor);

      if (x0 == x1 && y0 == y1)
        break;

      e2 = 2 * err;

      if (e2 > -1 * dy) {
        err -= dy;
        x0 += sx;
      }
      if (e2 < dx) {
        err += dx;
        y0 += sy;
      }
    }
  }

  public int getzDepth() {
    return zDepth;
  }

  public void setzDepth(int zDepth) {
    this.zDepth = zDepth;
  }

  public int getAmbientColor() {
    return ambientColor;
  }

  public void setAmbientColor(int ambientColor) {
    this.ambientColor = ambientColor;
  }
}