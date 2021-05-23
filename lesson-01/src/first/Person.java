package first;

public class Person {

    //Вот тут очень пригодился бы lombok,
    //но мы тут делаем vanilla java!
    private String firstName;
    private String lastName;
    private String middleName;
    private String country;
    private String address;
    private String phone;
    private int age;
    private String gender;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getCountry() {
        return country;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public static Builder newBuilder() {
        return new Person().new Builder();
    }

    //1. Создать builder для класса first.Person со следующими полями:
    //String firstName,
    //String lastName,
    //String middleName,
    //String country,
    //String address,
    //String phone,
    //int age,
    //String gender.
    public class Builder {

        private Builder() {
        }

        public Builder setFirstName(String firstName) {
            Person.this.firstName = firstName;
            return this;
        }

        public Builder setMiddleName(String middleName) {
            Person.this.middleName = middleName;
            return this;
        }

        public Builder setLastName(String lastName) {
            Person.this.lastName = lastName;
            return this;
        }

        public Builder setCountry(String country) {
            Person.this.country = country;
            return this;
        }

        public Builder setAddress(String address) {
            Person.this.address = address;
            return this;
        }

        public Builder setPhone(String phone) {
            Person.this.phone = phone;
            return this;
        }

        public Builder setAge(int age) {
            Person.this.age = age;
            return this;
        }

        public Builder setGender(String gender) {
            Person.this.gender = gender;
            return this;
        }

        public Person build() {
            return Person.this;
        }

    }

}