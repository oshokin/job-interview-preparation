package concurrency.pong;

import java.util.concurrent.Phaser;

public class PingPongDemo {
    private static final Phaser phaser = new Phaser(1);

    public static void main(String[] args) {
        makeMove("ping");
        makeMove("pong");
    }

    private static void makeMove(final String message) {
        Thread pongThread = new Thread(new InfinitePingPongThread(phaser, message));
        pongThread.start();
    }

}
