package com.whatever.gameoflife;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args)
	{

	    //Set up components
	    GameCanvas gc = new GameCanvas(400, 400, 10);
	    gc.startGame();

	    //frame setup
	    JFrame frame = new JFrame();
	    frame.setResizable(true);
	    frame.setTitle("Game of Life");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.requestFocus();

	    //has a hard time working inside GameCanvas for some reason, hmm
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                //System.out.println("\"" + e.getKeyChar() + "\" key pressed");
                switch(e.getKeyChar()) {
                case ' ':
                    gc.togglePause();
                    break;
                case 'r':
                    gc.randomize((float) Math.random());
                    break;
                case 'c':
                    gc.clearAll();
                default:
                    break;
                }
            }
            @Override
            public void keyReleased(KeyEvent arg0) { }
            @Override
            public void keyTyped(KeyEvent arg0) { }
        });

	    //Add components
	    frame.getContentPane().add(gc);

	    //show frame
	    frame.pack();
	    frame.setVisible(true);
	}
}
