package me.sample.security;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@EqualsAndHashCode(callSuper = false)
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ExtendedUser extends User {

    @Getter
    Long id;

    @Builder(builderMethodName = "extendedBuilder")
    public ExtendedUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
