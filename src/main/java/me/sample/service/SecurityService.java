package me.sample.service;

import me.sample.security.ExtendedUser;

public interface SecurityService {
    ExtendedUser getUser();

    Long getUserId();
}
