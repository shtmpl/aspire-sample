package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import me.sample.security.ExtendedUser;
import me.sample.service.SecurityService;

import java.util.Optional;

@Service("securityService")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityServiceImpl implements SecurityService {

    @Override
    public ExtendedUser getUser() {
        return getFromContext()
                .orElse(null);
    }

    @Override
    public Long getUserId() {
        return getFromContext()
                .map(ExtendedUser::getId)
                .orElse(null);
    }

    private Optional<ExtendedUser> getFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .map(p -> (ExtendedUser) p);
    }
}
