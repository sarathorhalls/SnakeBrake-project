package com.saraexperiments.jabble;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

  private Jabble jb;

  private static final int NUMKEYS = 256;
  private static final int NUMBUTTONS = 5;

  private boolean[] keys = new boolean[NUMKEYS];
  private boolean[] keysLast = new boolean[NUMKEYS];
  private boolean[] buttons = new boolean[NUMBUTTONS];
  private boolean[] buttonsLast = new boolean[NUMBUTTONS];

  private int mouseX;
  private int mouseY;
  private int scroll;

  public Input(Jabble jb) {
    this.jb = jb;
    mouseX = 0;
    mouseY = 0;
    scroll = 0;

    jb.getWindow().getCanvas().addKeyListener(this);
    jb.getWindow().getCanvas().addMouseListener(this);
    jb.getWindow().getCanvas().addMouseMotionListener(this);
    jb.getWindow().getCanvas().addMouseWheelListener(this);
  }

  public void update() {
    // System.arraycopy(keys, 0, keysLast, NUMKEYS, NUMKEYS);
    for (int i = 0; i < NUMKEYS; i++) {
      keysLast[i] = keys[i];
    }

    // System.arraycopy(buttons, 0, buttonsLast, NUMBUTTONS, NUMBUTTONS);

    for (int i = 0; i < NUMBUTTONS; i++) {
      buttonsLast[i] = buttons[i];
    }

    scroll = 0;
  }

  public boolean isKey(int keyCode) {
    return keys[keyCode];
  }

  public boolean isKeyDown(int keyCode) {
    return keys[keyCode] && !keysLast[keyCode];
  }

  public boolean isKeyUp(int keyCode) {
    return !keys[keyCode] && keysLast[keyCode];
  }

  public boolean isButton(int buttonCode) {
    return buttons[buttonCode];
  }

  public boolean isButtonDown(int buttoncode) {
    return buttons[buttoncode] && !buttonsLast[buttoncode];
  }

  public boolean isButtonUp(int buttoncode) {
    return !buttons[buttoncode] && buttonsLast[buttoncode];
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    scroll = e.getWheelRotation();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseX = (int) (e.getX() / jb.getScale());
    mouseY = (int) (e.getY() / jb.getScale());
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // TODO: not implemented
  }

  @Override
  public void mousePressed(MouseEvent e) {
    buttons[e.getButton()] = true;
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    buttons[e.getButton()] = false;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // TODO: not implemented
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // TODO: not implemented
  }

  @Override
  public void keyTyped(KeyEvent e) {
    // TODO: not implemented
  }

  @Override
  public void keyPressed(KeyEvent e) {
    keys[e.getKeyCode()] = true;
  }

  @Override
  public void keyReleased(KeyEvent e) {
    keys[e.getKeyCode()] = false;
  }

  public int getScroll() {
    return scroll;
  }

  public int getMouseX() {
    return mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }

}