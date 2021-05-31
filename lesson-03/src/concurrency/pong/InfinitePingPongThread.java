package concurrency.pong;

import java.util.concurrent.Phaser;

public class InfinitePingPongThread implements Runnable {

    private String message;
    private Phaser phaser;

    public InfinitePingPongThread(Phaser phaser, String message) {
        this.message = message;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println(message);
            phaser.awaitAdvance(phaser.arrive() + 1);
        }
    }

}