package com.saraexperiments.jabble.gfx;

public class ImageTile extends Image {

  private int tileWidth;
  private int tileHeight;

  public ImageTile(String path, int tileWidth, int tileHeight) {
    super(path);
    this.tileHeight = tileHeight;
    this.tileWidth = tileWidth;
  }

  public Image getTileImage(int tileX, int tileY) {
    int[] pixelArray = new int[tileWidth * tileHeight];

    for (int y = 0; y < tileHeight; y++) {
      for (int x = 0; x < tileWidth; x++) {
        pixelArray[x + y * tileWidth] = this.getPixelArray()[(x + tileX * tileWidth)
            + (y + tileY + tileHeight) * this.getWidth()];
      }
    }

    return new Image(pixelArray, tileWidth, tileHeight);
  }

  public int getTileWidth() {
    return tileWidth;
  }

  public void setTileWidth(int tileWidth) {
    this.tileWidth = tileWidth;
  }

  public int getTileHeight() {
    return tileHeight;
  }

  public void setTileHeight(int tileHeight) {
    this.tileHeight = tileHeight;
  }

}