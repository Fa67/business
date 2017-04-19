package com.eayun.common.dao.jpa;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;

import com.eayun.common.dao.impl.RepositoryDao;

@SuppressWarnings("unchecked")
public class BaseRepositoryFactory extends JpaRepositoryFactory {

    public BaseRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryMetadata metadata,
                                                            EntityManager em) {

        JpaEntityInformation<Object, Serializable> entityMetadata2 = (JpaEntityInformation<Object, Serializable>) JpaEntityInformationSupport
            .getEntityInformation((Class<Object>) metadata.getDomainType(), em);
        return new RepositoryDao<Object, Serializable>(entityMetadata2, em);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return RepositoryDao.class;
    }
}
