package first;

public class Main {

    public static void main(String[] args) {
        Person meMyselfAndI =
                Person.newBuilder().
                        setFirstName("Oleg").
                        setLastName("Shokin").
                        setAge(34).setGender("M").
                        setCountry("Narnia!").
                        build();
        System.out.println(meMyselfAndI);
    }

}