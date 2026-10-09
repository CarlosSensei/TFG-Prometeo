package com.optiflow.crm.user;

import com.optiflow.crm.shared.exception.DuplicateResourceException;
import com.optiflow.crm.user.dto.CreateUserDto;
import com.optiflow.crm.user.model.User;

import com.optiflow.crm.user.model.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User findById(Long id) {

        return userRepository.findById(id).orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado con id: " + id));
    }

    @Override
    public User findByEmail(String email) {

        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }

    @Override
    @Transactional
    public User create(CreateUserDto dto) {

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {

            throw new DuplicateResourceException("Ya existe un usuario con el email " + dto.getEmail());
        }

        User user = new User();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        user.setEmail(dto.getEmail().trim().toLowerCase());

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setRole(dto.getRole());
        user.setActive(true);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        User user = findById(id);

        user.setActive(false);

        userRepository.save(user);
    }
}