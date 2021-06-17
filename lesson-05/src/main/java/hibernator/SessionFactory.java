package hibernator;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.EntityManagerFactory;

public class SessionFactory {

    private static EntityManagerFactory factory = buildFactory();

    private static EntityManagerFactory buildFactory() {
        StandardServiceRegistry registry = null;
        try {
            registry = new StandardServiceRegistryBuilder().configure().build();
            factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
            throw new ExceptionInInitializerError("Factory initialization failed: " + e);
        }

        return factory;
    }

    public static EntityManagerFactory getFactory() {
        return factory;
    }

    public static void shutdown() {
        getFactory().close();
    }

}