package com.saraexperiments.jabble;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

public class Window {

  private JFrame frame;
  private BufferedImage image;
  private Canvas canvas;
  private Graphics g;
  private BufferStrategy bs;

  public Window(Jabble jb) {
    image = new BufferedImage(jb.getWidth(), jb.getHeight(), BufferedImage.TYPE_INT_RGB);

    canvas = new Canvas();
    Dimension s = new Dimension((int) (jb.getWidth() * jb.getScale()), (int) (jb.getHeight() * jb.getScale()));
    canvas.setPreferredSize(s);
    canvas.setMaximumSize(s);
    canvas.setMinimumSize(s);

    frame = new JFrame(jb.getLabel());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    frame.add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setVisible(true);

    canvas.createBufferStrategy(2);
    bs = canvas.getBufferStrategy();
    g = bs.getDrawGraphics();
    canvas.requestFocus();
  }

  public void update() {
    g.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
    bs.show();
  }

  // Getters
  public Canvas getCanvas() {
    return canvas;
  }

  public BufferedImage getImage() {
    return image;
  }

}