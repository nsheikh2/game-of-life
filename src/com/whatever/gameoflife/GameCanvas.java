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

/**
 * Game canvas class - holds the board, etc.
 */
public class GameCanvas extends JPanel {

    /** Color of a living cell */
    private Color ALIVE_COLOR   = Color.BLACK;

    /** Color of a dead cell */
    private Color DEAD_COLOR    = Color.WHITE;

    /** Size of each cell, in pixels */
    private int cellSize;

    /** Current generation of the game. */
    private int[][] currGen;

    /** Next generation of the game. */
    private int[][] nextGen;

    /** Number of currGen horizontally. */
    private int dimensionX;

    /** Number of currGen vertically. */
    private int dimensionY;

    /** Board update step in milliseconds */
    private int timeStep;

    /** Game paused? */
    private boolean paused;

    /**
     * Constructor for GameCanvas
     * @param canvasWidth canvas width
     * @param canvasHeight canvas height
     * @param cellSize size of cell
     */
    public GameCanvas(int canvasWidth, int canvasHeight, int cellSize) {
        setBackground(Color.WHITE);
        setSize(canvasWidth, canvasHeight);
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        this.cellSize = cellSize;
        this.dimensionX = canvasWidth / cellSize;
        this.dimensionY = canvasHeight / cellSize;
        this.currGen = new int[dimensionX][dimensionY];
        this.nextGen = new int[dimensionX][dimensionY];
        this.timeStep = 100;
        this.paused = true;

        this.requestFocusInWindow();

        //gotta <3 anonymous classes
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] coords = mouseToBoard(e.getX(), e.getY(), cellSize, new int[] { dimensionX, dimensionY });
                toggleCell(coords[0], coords[1], currGen);
                repaint();
            }
        });
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                int[] coords = mouseToBoard(arg0.getX(), arg0.getY(), cellSize, new int[] { dimensionX, dimensionY });
                setCell(coords[0], coords[1], currGen);
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent arg0) { }
        });
    }

    /**
     * Gets time step.
     * @return time step
     */
    public int getTimeStep() {
        return timeStep;
    }
    /**
     * Sets time step.
     * @param timeStep time step to set
     */
    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * Starts the game.
     */
    public void startGame() {
        //while loop was hanging thread, using timer instead
        new Timer(timeStep, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!paused) {
                    update();
                }
            }
        }).start();
    }

    /**
     * Toggles the status of the game from paused to unpaused.
     */
    public void togglePause() {
        paused = !paused;
    }

    @Override
    public void paint(Graphics g)
    {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                if (currGen[i][j] == 1) {
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

    /**
     * Updates the board.
     */
    public void update() {
        clear(nextGen);
        pushCells(currGen, nextGen);
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                int neighbors = getLiveNeighbors(i, j, currGen);
                if (neighbors == 2) {
                    //if two just leave alone
                    continue;
                } else if (neighbors < 2) {
                    //underpopulation
                    unsetCell(i, j, nextGen);
                } else if (neighbors == 3) {
                    //reanimate dead cell
                    setCell(i, j, nextGen);
                } if (neighbors > 3) {
                    //overcrowding
                    unsetCell(i, j, nextGen);
                }
            }
        }
        clear(currGen);
        pushCells(nextGen, currGen);
        repaint();
    }

    /**
     * Converts a mouse coordinate on the board into a board coordinate.
     * @param mX mouse x
     * @param mY mouse y
     * @param cellSize size of cell
     * @param dimension array of board dimensions { x, y }
     * @return
     */
    public static int[] mouseToBoard(int mX, int mY, int cellSize, int[] dimensions) {
        int adjX = (int) Math.floor((double) mX / (double) cellSize);
        int adjY = (int) Math.floor((double) mY / (double) cellSize);
        boolean okayX = adjX <= dimensions[0] - 1;    //less than zero doesn't happen
        boolean okayY = adjY <= dimensions[1] - 1;
        int[] toReturn = {adjX, adjY};
        if (!okayX) toReturn[0] = dimensions[0] - 1;
        if (!okayY) toReturn[1] = dimensions[1] - 1;
        return toReturn;
    }

    /**
     * Gets a cell.
     * @param x x-val
     * @param y y-val
     * @param cells cell grid
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

    /**
     * Sets a cell to be alive.
     * @param x cell x-val
     * @param y cell y-val
     * @param cells cell grid
     */
    public void setCell(int x, int y, int[][] cells) {
        boolean validX = x >= 0 && x <= dimensionX - 1;
        boolean validY = y >= 0 && y <= dimensionY - 1;
        if (!(validX && validY)) {
            return;
        }
        cells[x][y] = 1;
        repaint();
    }

    /**
     * Sets a cell to be dead.
     * @param x cell x-val
     * @param y cell y-val
     * @param cells cell grid
     */
    public void unsetCell(int x, int y, int[][] cells) {
        boolean validX = x >= 0 && x <= dimensionX - 1;
        boolean validY = y >= 0 && y <= dimensionY - 1;
        if (!(validX && validY)) {
            return;
        }
        cells[x][y] = 0;
        repaint();
    }

    /**
     * Toggles a cell dead or alive.
     * @param x cell x-val
     * @param y cell y-val
     * @param cells cell grid
     */
    public void toggleCell(int x, int y, int[][] cells) {
        if (getCell(x, y, cells) == 1) {
            unsetCell(x, y, cells);
        } else {
            setCell(x, y, cells);
        }
    }

    /**
     * Finds all living neighbors of a given cell.
     * @param x cell x-val
     * @param y cell y-val
     * @param cells cell grid
     * @return the number of living neighbors
     */
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

    /**
     * Clears a grid of cells
     * @param currGen the grid of cells to clear
     */
    public void clear(int[][] cells) {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                unsetCell(i, j, cells);
            }
        }
        repaint();
    }

    /**
     * Clears both the current and next generation of currGen.
     */
    public void clearAll() {
        clear(currGen);
        clear(nextGen);
    }

    /**
     * Clears the board and randomizes the current generation
     * @param probability chance that any given cell will be alive
     */
    public void randomize(float probability) {
        clearAll();
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                double val = Math.random();
                if (val <= probability) {
                    setCell(i, j, currGen);
                }
            }
        }
        repaint();
    }
}
