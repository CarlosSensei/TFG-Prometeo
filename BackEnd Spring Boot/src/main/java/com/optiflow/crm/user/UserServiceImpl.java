package com.optiflow.crm.user;

import com.optiflow.crm.shared.exception.UserAlreadyCreatedException;
import com.optiflow.crm.shared.exception.UserNotFoundException;
import com.optiflow.crm.user.dto.CreateUserDto;
import com.optiflow.crm.user.model.User;

import com.optiflow.crm.user.model.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Implantacion del service de user
@Service
public class UserServiceImpl implements UserService {

    // Inyeccion de repository y crear passwords
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Buscar todos los users
    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // BUscar por id
    @Override
    public User findById(Long id) {

        return userRepository.findById(id).orElseThrow(() ->
                        new UserNotFoundException(id));
    }

    // Buscar por email
    @Override
    public User findByEmail(String email) {

        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                        new UserNotFoundException(email));
    }

    // Crear un nuevo usuario
    @Override
    @Transactional
    public User create(CreateUserDto dto) {

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {

            throw new UserAlreadyCreatedException(dto.getEmail());
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

    // Borrar un usuario desactivandolo
    @Override
    @Transactional
    public void delete(Long id) {

        User user = findById(id);

        user.setActive(false);

        userRepository.save(user);
    }
}