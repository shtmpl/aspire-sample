package me.sample.domain;

import org.springframework.data.util.ProxyUtils;

import java.io.Serializable;

public abstract class AbstractIdentifiable<Id extends Serializable> {

    public abstract Id getId();

    @Override
    public boolean equals(Object object) {
        if (null == object) {
            return false;
        }

        if (this == object) {
            return true;
        }

        if (!getClass().equals(ProxyUtils.getUserClass(object))) {
            return false;
        }

        AbstractIdentifiable<?> that = (AbstractIdentifiable<?>) object;

        return getId() == null ? false : getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;

        hashCode += getId() == null ? 0 : getId().hashCode() * 31;

        return hashCode;
    }
}
