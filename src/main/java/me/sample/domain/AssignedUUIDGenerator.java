package me.sample.domain;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;

import java.io.Serializable;

public class AssignedUUIDGenerator extends UUIDGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        if (object instanceof AbstractIdentifiable) {
            AbstractIdentifiable identifiable = (AbstractIdentifiable) object;
            Serializable id = identifiable.getId();
            if (id != null) {
                return id;
            }
        }

        return super.generate(session, object);
    }
}
