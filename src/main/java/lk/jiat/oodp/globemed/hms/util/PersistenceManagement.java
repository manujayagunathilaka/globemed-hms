package lk.jiat.oodp.globemed.hms.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Manujaya
 */
public class PersistenceManagement {

    private static EntityManagerFactory entityManagerFactory;

    private PersistenceManagement() {

    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            Map<String, Object> properties = new HashMap<>();
            
            // Load and override properties programmatically from environment/env file
            properties.put("jakarta.persistence.jdbc.url", EnvLoader.get("DB_URL"));
            properties.put("jakarta.persistence.jdbc.user", EnvLoader.get("DB_USER"));
            properties.put("jakarta.persistence.jdbc.password", EnvLoader.get("DB_PASSWORD"));
            
            entityManagerFactory = Persistence.createEntityManagerFactory("GlobeMedPU", properties);
        }

        return entityManagerFactory;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

}
