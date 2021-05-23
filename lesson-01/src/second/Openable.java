package second;

public interface Openable {

    default void open() {
        System.out.println("Somebody opened the car!");
    }

}
