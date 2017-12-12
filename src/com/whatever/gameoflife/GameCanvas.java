package com.whatever.gameoflife;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class GameCanvas extends JPanel {
    //constants
    private Color ALIVE_COLOR   = Color.BLACK;
    private Color DEAD_COLOR    = Color.WHITE;

    //instance variables
    private int cellSize;
    private int[][] cells;
    private int[][] buffer;
    private int dimensionX;
    private int dimensionY;

    //vars
    private int timeStep;    //board update step in milliseconds
    private long prevTime;
    private boolean paused;

    public GameCanvas(int canvasWidth, int canvasHeight, int cellSize)
    {
        setBackground(Color.WHITE);
        setSize(canvasWidth, canvasHeight);
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        this.cellSize = cellSize;
        this.dimensionX = canvasWidth / cellSize;
        this.dimensionY = canvasHeight / cellSize;
        this.cells = new int[dimensionX][dimensionY];
        this.buffer = new int[dimensionX][dimensionY];

        this.timeStep = 100;
        this.prevTime = System.currentTimeMillis();
        this.paused = true;

        this.requestFocusInWindow();

        //gotta <3 anonymous classes
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] coords = mouseToBoard(e.getX(), e.getY(), cellSize, new int[] {dimensionX, dimensionY});
                toggleCell(coords[0], coords[1], cells);
                repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                int[] coords = mouseToBoard(arg0.getX(), arg0.getY(), cellSize, new int[] {dimensionX, dimensionY});
                setCell(coords[0], coords[1], cells);
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent arg0) { }
        });
    }
    public GameCanvas(int canvasWidth, int canvasHeight, int cellSize, int timeStep) {
        this(canvasWidth, canvasHeight, cellSize);
        this.timeStep = timeStep;
    }

    public void startGame() {
        //while loop was hanging thread, using timer instead
        new Timer(timeStep, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (paused) {
                    prevTime = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - prevTime >= timeStep) {
                        prevTime = System.currentTimeMillis();
                        update();
                    }
                }
            }
        }).start();
    }

    public void togglePause() {
        paused = !paused;
    }

    @Override
    public void paint(Graphics g)
    {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                if (cells[i][j] == 1) {
                    g.setColor(ALIVE_COLOR);
                } else {
                    g.setColor(DEAD_COLOR);
                }
                g.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);

                g.setColor(Color.BLACK);
                g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
    }

    public void printArray(int[][] array) {
        System.out.print("[ ");
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                System.out.print(getCell(i, j, array) + " ");
            }
            System.out.print("|");
        }
        System.out.println("]");
    }

    public void update() {
        clear(buffer);
        pushCells(cells, buffer);
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                int neighbors = getLiveNeighbors(i, j, cells);
                if (neighbors < 2) {
                    unsetCell(i, j, buffer);    //underpopulation
                } else if (neighbors == 3) {         //if 2 just leave it alone, if 3 reanimate dead cell
                    setCell(i, j, buffer);
                } if (neighbors > 3) {
                    unsetCell(i, j, buffer);    //overcrowding
                }
            }
        }
        clear(cells);
        pushCells(buffer, cells);
        repaint();
    }

    public static int[] mouseToBoard(int mX, int mY, int cellSize, int[] dimension) {
        int adjX = (int) Math.floor((double) mX / (double) cellSize);
        int adjY = (int) Math.floor((double) mY / (double) cellSize);
        boolean okayX = adjX <= dimension[0] - 1;    //less than zero doesn't happen
        boolean okayY = adjY <= dimension[1] - 1;
        int[] toReturn = {adjX, adjY};
        if (!okayX) toReturn[0] = dimension[0] - 1;
        if (!okayY) toReturn[1] = dimension[1] - 1;
        return toReturn;
    }

    /**
     *
     * @param x x-val
     * @param y y-val
     * @return 1 if cell @ (x,y) is alive, else 0
     */
    public int getCell(int x, int y, int[][] cells) {
        //if outside board, it is dead
        if (x > dimensionX - 1 || y > dimensionY - 1) {
            return 0;
        }
        if (x < 0 || y < 0) {
            return 0;
        }

        return cells[x][y];
    }

    public void setCell(int x, int y, int[][] cells) {
        boolean validX = x >= 0 && x <= dimensionX - 1;
        boolean validY = y >= 0 && y <= dimensionY - 1;
        if (!(validX && validY)) {
            return;
        }
        cells[x][y] = 1;
        repaint();
    }

    public void unsetCell(int x, int y, int[][] cells) {
        boolean validX = x >= 0 && x <= dimensionX - 1;
        boolean validY = y >= 0 && y <= dimensionY - 1;
        if (!(validX && validY)) {
            return;
        }
        cells[x][y] = 0;
        repaint();
    }

    public void toggleCell(int x, int y, int[][] cells) {
        if (getCell(x, y, cells) == 1) {
            unsetCell(x, y, cells);
        } else {
            setCell(x, y, cells);
        }
    }

    private int getLiveNeighbors(int x, int y, int[][] cells) {
        int toReturn = 0;
        for (int i = x - 1; i <= x + 1; i++) {        //get entire 3x3 grid around curr cell, including curr cell
            for (int j = y - 1; j <= y + 1; j++) {
                toReturn += getCell(i, j, cells);
            }
        }
        toReturn -= getCell(x, y, cells);    //subtract curr cell value
        return toReturn;
    }

    /**
     * Pushes cells from board a to b
     * @param a first board
     * @param b second board
     */
    private void pushCells(int[][] copyFrom, int[][] copyTo) {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                copyTo[i][j] = copyFrom[i][j];
            }
        }
    }

    public void clear(int[][] cells) {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                unsetCell(i, j, cells);
            }
        }
        repaint();
    }

    public void clearAll() {
        clear(cells);
        clear(buffer);
    }

    public void randomize(float probability) {
        clearAll();
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                double val = Math.random();
                if (val <= probability) {
                    setCell(i, j, cells);
                }
            }
        }
        repaint();
    }
}
