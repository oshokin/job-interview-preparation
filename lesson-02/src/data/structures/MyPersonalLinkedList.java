package data.structures;

public class MyPersonalLinkedList<T> implements Iterable<T> {
    private int size = 0;
    private LinkedListNode<T> head = null;
    private LinkedListNode<T> tail = null;

    //очистка связанного списка
    public void clear() {
        LinkedListNode<T> currentNode = head;
        while (currentNode != null) {
            LinkedListNode<T> next = currentNode.next;

            currentNode.prev = null;
            currentNode.next = null;
            currentNode.data = null;

            currentNode = next;
        }
        currentNode = null;
        head = null;
        tail = null;
        size = 0;
    }

    //размер списка
    public int size() {
        return size;
    }

    //список пуст?
    public boolean isEmpty() {
        return size() == 0;
    }

    //добавить элемент в конец списка
    public void add(T elem) {
        addLast(elem);
    }

    //добавить узел в конец списка
    public void addLast(T elem) {
        if (isEmpty()) {
            head = new LinkedListNode<>(elem, null, null);
            tail = new LinkedListNode<>(elem, null, null);
        } else {
            tail.next = new LinkedListNode<>(elem, tail, null);
            tail = tail.next;
        }
        size++;
    }

    //добавить элемент в начало списка
    public void addFirst(T elem) {
        if (isEmpty()) {
            head = new LinkedListNode<>(elem, null, null);
            tail = new LinkedListNode<>(elem, null, null);
        } else {
            head.prev = new LinkedListNode<>(elem, null, head);
            head = head.prev;
        }
        size++;
    }

    //достать значение из начала списка, если оно имеется
    public T peekFirst() {
        if (isEmpty()) throw new RuntimeException("List is empty, sir!");
        return head.data;
    }

    //достать значение из конца списка, если он имеется
    public T peekLast() {
        if (isEmpty()) throw new RuntimeException("La lista esta vacia! No puede ser!");
        return tail.data;
    }

    //удалить значение из начала списка
    public T removeFirst() {
        if (isEmpty()) throw new RuntimeException("List is devastated and totally empty!");
        //извлечь данные из начала списка и передвинуть указатель начала вперед на один узел
        T data = head.data;
        head = head.next;
        --size;

        //если список пуст, обнуляем указатель конца списка
        if (isEmpty()) tail = null;
            //иначе обнуляем предыдущий узел
        else head.prev = null;

        //вернем данные, которые были в первом удаленном узле
        return data;
    }

    //удалить значение из конца списка
    public T removeLast() {
        if (isEmpty()) throw new RuntimeException("List is devastated and totally empty!");
        //извлечь данные из конца списка и передвинуть указатель конца назад на один узел
        T data = tail.data;
        tail = tail.prev;
        --size;

        //если список пуст, обнуляем указатель начала списка
        if (isEmpty()) head = null;
            //иначе обнуляем узел, который был только что удален
        else tail.next = null;

        //вернем данные, которые были в только что удаленном узле
        return data;
    }

    //удалить конкретный узел из списка
    private T remove(LinkedListNode<T> linkedListNode) {
        //если узел находится в начале или конце списка, обработаем такие случаи предварительно
        if (linkedListNode.prev == null) return removeFirst();
        if (linkedListNode.next == null) return removeLast();

        // Make the pointers of adjacent nodes skip over 'linkedListNode'
        linkedListNode.next.prev = linkedListNode.prev;
        linkedListNode.prev.next = linkedListNode.next;

        //сохраним данные для результата метода
        T data = linkedListNode.data;

        //зачистим связи
        linkedListNode.prev = null;
        linkedListNode.next = null;

        //зачистим данные
        linkedListNode.data = null;

        //зачистим узел
        linkedListNode = null;

        --size;

        //вернем данные из узла, который мы только что убрали
        return data;
    }

    //удалить узел по индексу
    public T removeAt(int index) {
        //проверим правильность индекса
        if (index < 0 || index >= size) throw new IllegalArgumentException();

        int i;
        LinkedListNode<T> currentNode;

        //накрутим немножечко дихотомии
        //значение до середины списка
        if (index < size / 2) {
            for (i = 0, currentNode = head; i != index; i++) {
                currentNode = currentNode.next;
            }
            //значение после середины списка
        } else {
            for (i = size - 1, currentNode = tail; i != index; i--) {
                currentNode = currentNode.prev;
            }
        }
        return remove(currentNode);
    }

    //удалить значение из списка
    public boolean remove(Object object) {
        LinkedListNode<T> currentNode;

        //если объект = null
        if (object == null) {
            for (currentNode = head; currentNode != null; currentNode = currentNode.next) {
                if (currentNode.data == null) {
                    remove(currentNode);
                    return true;
                }
            }
            //если объект != null
        } else {
            for (currentNode = head; currentNode != null; currentNode = currentNode.next) {
                if (object.equals(currentNode.data)) {
                    remove(currentNode);
                    return true;
                }
            }
        }
        return false;
    }

    //найти индекс значения в списке
    public int indexOf(Object object) {
        int index = 0;
        LinkedListNode<T> currentNode = head;

        //если объект = null
        if (object == null) {
            for (; currentNode != null; currentNode = currentNode.next, index++) {
                if (currentNode.data == null) {
                    return index;
                }
            }
            //если объект != null
        } else
            for (; currentNode != null; currentNode = currentNode.next, index++) {
                if (object.equals(currentNode.data)) {
                    return index;
                }
            }

        return -1;
    }

    //есть ли значение в списке
    public boolean contains(Object object) {
        return indexOf(object) != -1;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new java.util.Iterator<T>() {
            private LinkedListNode<T> currentNode = head;

            @Override
            public boolean hasNext() {
                return currentNode != null;
            }

            @Override
            public T next() {
                T data = currentNode.data;
                currentNode = currentNode.next;
                return data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        LinkedListNode<T> currentNode = head;

        while (currentNode != null) {
            sb.append(currentNode.data);
            sb.append(", ");

            currentNode = currentNode.next;
        }
        sb.append(" ]");

        return sb.toString();
    }

    //узел списка
    private static class LinkedListNode<T> {

        private T data;
        private LinkedListNode<T> prev, next;

        public LinkedListNode(T data, LinkedListNode<T> prev, LinkedListNode<T> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }

        @Override
        public String toString() {
            return data.toString();
        }

    }

}