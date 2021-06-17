package hibernator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Repository<T, V> {

    private final EntityManagerFactory factory;
    private final Class<T> type;

    public Repository(EntityManagerFactory factory, Class<T> type) {
        this.factory = factory;
        this.type = type;
    }

    @Transactional
    public List<T> findAll() throws Exception {
        return runWithManager(manager ->
            manager.createQuery(String.format("SELECT entity FROM %s entity", type.getSimpleName())).getResultList()
        );
    }

    @Transactional
    public T findById(V id) throws Exception {
        return runWithManager(manager ->
                manager.find(type, id));
    }

    public void insert(T object) throws Exception {
        runInTransaction(manager -> manager.persist(object));
    }

    public T update(T object) throws Exception {
        return returnFromTransaction(manager -> manager.merge(object));
    }

    public void delete(V id) throws Exception {
        runInTransaction(manager -> {
            T entity = manager.find(type, id);
            if (entity != null) {
                manager.remove(entity);
            }
        });
    }

    public V getAmount() throws Exception {
        return runWithManager(manager ->
                (V) manager.createQuery(String.format("SELECT COUNT(entity.id) FROM %s entity",
                        type.getSimpleName())).getSingleResult());
    }

    public V getFirstId() throws Exception {
        return runWithManager(manager ->
                (V) manager.createQuery(String.format("SELECT MIN(entity.id) FROM %s entity",
                        type.getSimpleName())).getSingleResult());
    }

    public V getLastId() throws Exception {
        return runWithManager(manager ->
                (V) manager.createQuery(String.format("SELECT MAX(entity.id) FROM %s entity",
                        type.getSimpleName())).getSingleResult());
    }

    private <R> R runWithManager(Function<EntityManager, R> function) throws Exception {
        EntityManager manager = factory.createEntityManager();
        try {
            return function.apply(manager);
        } catch (Exception e) {
            throw e;
        } finally {
            if (manager != null && manager.isOpen()) manager.close();
        }
    }

    private void runInTransaction(Consumer<EntityManager> method) throws Exception {
        EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            method.accept(manager);
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            throw e;
        } finally {
            if (manager != null && manager.isOpen()) manager.close();
        }
    }

    private <R> R returnFromTransaction(Function<EntityManager, R> function) throws Exception {
        EntityManager manager = factory.createEntityManager();
        R funcResult;
        try {
            manager.getTransaction().begin();
            funcResult = function.apply(manager);
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            throw e;
        } finally {
            if (manager != null && manager.isOpen()) manager.close();
        }
        return funcResult;
    }

}

