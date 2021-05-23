package second;

public class Lorry extends Car implements Moveable, Stopable {

    public Lorry() {
        this.setEngine(new Engine("LorryEngine"));
    }

    @Override
    public void move() {
        System.out.println("Lorry is moving");
    }

    @Override
    public void stop() {
        System.out.println("Lorry is stop");
    }

}
