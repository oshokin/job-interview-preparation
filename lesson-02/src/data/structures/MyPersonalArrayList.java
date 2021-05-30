package data.structures;

@SuppressWarnings("unchecked")
public class MyPersonalArrayList<T> implements Iterable<T> {

    private T[] array;
    private int arraySize; //пусть пользователь думает, что массив такого размера, как Golang.len
    private int arrayCapacity; //истинный размер списка, как Golang.cap

    public MyPersonalArrayList() {
        //чего бы то и нет? мой ArrayList = java ArrayList * 2 :)
        this(32);
    }

    public MyPersonalArrayList(int arrayCapacity) {
        if (arrayCapacity < 0) throw new IllegalArgumentException("Illegal сapacity: " + arrayCapacity);
        this.arrayCapacity = arrayCapacity;
        array = (T[]) new Object[arrayCapacity];
    }

    public int size() {
        return arraySize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public T get(int index) {
        return array[index];
    }

    public void set(int index, T element) {
        array[index] = element;
    }

    public void clear() {
        for (int i = 0; i < arraySize; i++) {
            array[i] = null;
        }
        arraySize = 0;
    }

    public void add(T elem) {
        //перебор, пора увеличиться
        if (arraySize + 1 >= arrayCapacity) {
            if (arrayCapacity == 0) arrayCapacity = 1;
            else arrayCapacity *= 2; //удвоим размер
            T[] newArray = (T[]) new Object[arrayCapacity];
            if (arraySize >= 0) System.arraycopy(array, 0, newArray, 0, arraySize);
            array = newArray;
        }
        array[arraySize++] = elem;
    }

    public T removeAt(int removableIndex) {
        if (removableIndex >= arraySize || removableIndex < 0) throw new IndexOutOfBoundsException();
        T data = array[removableIndex];
        T[] newArray = (T[]) new Object[arraySize - 1];
        for (int i = 0, j = 0; i < arraySize; i++, j++) {
            if (i == removableIndex) j--;
            else newArray[j] = array[i];
        }
        array = newArray;
        arrayCapacity = --arraySize;

        return data;
    }

    public boolean remove(Object object) {
        int index = indexOf(object);
        if (index == -1) return false;
        removeAt(index);
        return true;
    }

    public int indexOf(Object object) {
        for (int i = 0; i < arraySize; i++) {
            if (object == null) {
                if (array[i] == null) return i;
            } else {
                if (object.equals(array[i])) return i;
            }
        }
        return -1;
    }

    public boolean contains(Object object) {
        return indexOf(object) != -1;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new java.util.Iterator<T>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < arraySize;
            }

            @Override
            public T next() {
                return array[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        if (arraySize == 0) return "[]";
        else {
            StringBuilder sb = new StringBuilder(arraySize);
            sb.append("[");
            for (int i = 0; i < arraySize - 1; i++) {
                sb.append(array[i]);
                sb.append(", ");
            }
            sb.append(array[arraySize - 1]);
            sb.append("]");

            return sb.toString();
        }
    }

}