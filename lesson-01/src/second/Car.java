package second;

public abstract class Car implements Openable {

    private Engine engine;
    private String color;
    private String name;

    public Car() {
        engine = new Engine(getClass().getName());
    }

    public void start() {
        System.out.println("Car is starting");
        engine.vroomVroom();
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}