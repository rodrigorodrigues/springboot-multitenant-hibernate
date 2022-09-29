package com.example.springbootmultitenanthibernate;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;

@Slf4j
@NoRepositoryBean
public class CustomRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    public static MultiTenantEntityManagerFactoryBean multiTenantEntityManagerFactoryBean;

    public CustomRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        log.info("Using custom repository");
    }

    //TODO Very ugly workaround need to find a better way to set entityManager.
    private void setDynamicEntityManager() {
        Field field = ReflectionUtils.findField(this.getClass(), "em");
        field.setAccessible(true);
        EntityManager entityManager = multiTenantEntityManagerFactoryBean.getEntityManagerInterface();
        ReflectionUtils.setField(field, this, entityManager);
        log.info("setDynamicEntityManager:entityManagerAfter: {}", entityManager);
    }

    //TODO Need to override all methods from SimpleJpaRepository.
    @Override
    public Page<T> findAll(Pageable pageable) {
        log.info("findlAll: {}", pageable);
        setDynamicEntityManager();
        return super.findAll(pageable);
    }
}
