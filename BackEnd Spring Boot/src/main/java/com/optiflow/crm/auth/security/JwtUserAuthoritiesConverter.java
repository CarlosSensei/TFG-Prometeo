package com.optiflow.crm.auth.security;

import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class JwtUserAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserRepository userRepository;

    public JwtUserAuthoritiesConverter(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        String email = jwt.getSubject();

        if (email == null || email.isBlank()) {
            throw invalidToken();
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(this::invalidToken);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw invalidToken();
        }

        if (user.getRole() == null) {
            throw invalidToken();
        }

        String authority = "ROLE_" + user.getRole().name();

        return List.of(new SimpleGrantedAuthority(authority));
    }

    private OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token",
                        "El token no corresponde a un usuario activo", null));
    }
}
