package com.optiflow.crm.user;

import com.optiflow.crm.shared.exception.UserAlreadyCreatedException;
import com.optiflow.crm.shared.exception.UserNotFoundException;
import com.optiflow.crm.user.dto.CreateUserDto;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private User createUser(Long id) {

        User user = new User();

        user.setId(id);
        user.setFirstName("Carlos");
        user.setLastName("García");
        user.setEmail("carlos@optiflow.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.SELLER);
        user.setActive(true);

        return user;
    }

    private CreateUserDto createDto() {

        CreateUserDto dto = new CreateUserDto();

        dto.setFirstName("Carlos");
        dto.setLastName("García");
        dto.setEmail("carlos@optiflow.com");
        dto.setPassword("Password123!");
        dto.setRole(UserRole.SELLER);

        return dto;
    }

    // =====================================
    // 1. FIND ALL
    // =====================================


    @Test
    void givenActiveUsers_whenFindAll_thenReturnPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        User first = createUser(1L);
        User second = createUser(2L);

        Page<User> expected = new PageImpl<>(
                List.of(first, second),
                pageable, 2);

        when(userRepository.findByActiveTrue(pageable))
                .thenReturn(expected);

        // WHEN
        Page<User> result = service.findAll(pageable);

        // THEN
        assertSame(expected, result);
        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream()
                .allMatch(User::getActive));

        verify(userRepository).findByActiveTrue(pageable);
        verify(userRepository, never()).findAll(pageable);
    }


    // =====================================
    // 2. FIND ALL EMPTY
    // =====================================


    @Test
    void givenNoActiveUsers_whenFindAll_thenReturnEmptyPage() {

        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByActiveTrue(pageable))
                .thenReturn(Page.empty(pageable));

        // WHEN
        Page<User> result = service.findAll(pageable);

        // THEN
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(userRepository).findByActiveTrue(pageable);
    }


    // =====================================
    // 3. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturnUser() {

        // GIVEN
        User user = createUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN
        User result = service.findById(1L);

        // THEN
        assertSame(user, result);
        assertEquals(1L, result.getId());

        verify(userRepository).findById(1L);
    }

    // =====================================
    // 4. FIND BY ID NOT FOUND
    // =====================================

    @Test
    void givenUnknownId_whenFindById_thenThrowNotFound() {

        // GIVEN
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(UserNotFoundException.class, () -> service.findById(99L));

        verify(userRepository).findById(99L);
    }

    // =====================================
    // 5. FIND BY EMAIL
    // =====================================

    @Test
    void givenExistingEmail_whenFindByEmail_thenReturnUser() {

        // GIVEN
        User user = createUser(1L);

        when(userRepository.findByEmailIgnoreCase("CARLOS@OPTIFLOW.COM"))
                .thenReturn(Optional.of(user));

        // WHEN
        User result = service.findByEmail("CARLOS@OPTIFLOW.COM");

        // THEN
        assertSame(user, result);

        verify(userRepository).findByEmailIgnoreCase(
                "CARLOS@OPTIFLOW.COM"
        );
    }

    // =====================================
    // 6. FIND BY EMAIL NOT FOUND
    // =====================================

    @Test
    void givenUnknownEmail_whenFindByEmail_thenThrowNotFound() {

        // GIVEN
        String email = "unknown@optiflow.com";

        when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(UserNotFoundException.class,
                () -> service.findByEmail(email));

        verify(userRepository).findByEmailIgnoreCase(email);
    }

    // =====================================
    // 7. CREATE USER
    // =====================================

    @Test
    void givenValidDto_whenCreate_thenSaveUser() {

        // GIVEN
        CreateUserDto dto = createDto();

        when(userRepository.existsByEmailIgnoreCase(dto.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = service.create(dto);

        // THEN
        assertNotNull(result);

        assertAll(
                () -> assertEquals("Carlos", result.getFirstName()),
                () -> assertEquals("García", result.getLastName()),
                () -> assertEquals("carlos@optiflow.com", result.getEmail()),
                () -> assertEquals("encoded-password", result.getPassword()),
                () -> assertEquals(UserRole.SELLER, result.getRole()),
                () -> assertTrue(result.getActive()));

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(result);
    }

    // =====================================
    // 8. NORMALIZAR EMAIL
    // =====================================


    @Test
    void givenEmailWithSpacesAndUppercase_whenCreate_thenNormalizeBeforeChecking() {

        // GIVEN
        CreateUserDto dto = createDto();
        dto.setEmail("  CARLOS@OPTIFLOW.COM  ");

        String normalizedEmail = "carlos@optiflow.com";

        when(userRepository.existsByEmailIgnoreCase(normalizedEmail))
                .thenReturn(false);

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = service.create(dto);

        // THEN
        assertEquals(normalizedEmail, result.getEmail());

        verify(userRepository).existsByEmailIgnoreCase(normalizedEmail);
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(argThat(user ->
                        normalizedEmail.equals(user.getEmail())));
    }


    // =====================================
    // 9. PASSWORD ENCODING
    // =====================================

    @Test
    void givenRawPassword_whenCreate_thenEncodeBeforeSave() {

        // GIVEN
        CreateUserDto dto = createDto();

        when(userRepository.existsByEmailIgnoreCase(dto.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("$2a$10$hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = service.create(dto);

        // THEN
        assertEquals("$2a$10$hashed-password", result.getPassword());
        assertNotEquals(dto.getPassword(), result.getPassword());

        verify(passwordEncoder).encode("Password123!");

        verify(userRepository).save(argThat(user -> "$2a$10$hashed-password"
                .equals(user.getPassword())));
    }

    // =====================================
    // 10. USER ROLE
    // =====================================

    @Test
    void givenOpticianRole_whenCreate_thenPreserveRole() {

        // GIVEN
        CreateUserDto dto = createDto();
        dto.setRole(UserRole.OPTICIAN);

        when(userRepository.existsByEmailIgnoreCase(dto.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = service.create(dto);

        // THEN
        assertEquals(UserRole.OPTICIAN, result.getRole());
        assertTrue(result.getActive());

        verify(userRepository).save(result);
    }

    // =====================================
    // 11. DUPLICATE EMAIL
    // =====================================

    @Test
    void givenDuplicateEmail_whenCreate_thenThrowWithoutSaving() {

        // GIVEN
        CreateUserDto dto = createDto();

        when(userRepository.existsByEmailIgnoreCase(dto.getEmail()))
                .thenReturn(true);

        // WHEN + THEN
        assertThrows(UserAlreadyCreatedException.class,
                () -> service.create(dto));

        verify(userRepository).existsByEmailIgnoreCase(dto.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // =====================================
    // 12. SOFT DELETE
    // =====================================

    @Test
    void givenExistingUser_whenDelete_thenDeactivate() {

        // GIVEN
        User user = createUser(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        // WHEN
        service.delete(5L);

        // THEN
        assertFalse(user.getActive());

        verify(userRepository).save(user);
        verify(userRepository, never()).delete(any(User.class));
        verify(userRepository, never()).deleteById(anyLong());
    }

    // =====================================
    // 13. DELETE USER NOT FOUND
    // =====================================

    @Test
    void givenUnknownUser_whenDelete_thenThrow() {

        // GIVEN
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(UserNotFoundException.class,
                () -> service.delete(99L));

        verify(userRepository, never()).save(any(User.class));
    }

    // =====================================
    // 14. DELETE ALREADY INACTIVE
    // =====================================

    @Test
    void givenInactiveUser_whenDelete_thenRemainInactive() {

        // GIVEN
        User user = createUser(5L);
        user.setActive(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        // WHEN
        service.delete(5L);

        // THEN
        assertFalse(user.getActive());

        verify(userRepository).save(user);
    }

    // =====================================
    // 15. DUPLICATE MAIL AFTER NORMALIZE
    // =====================================


    @Test
    void givenDuplicateEmailWithSpaces_whenCreate_thenRejectNormalizedEmail() {

        // GIVEN
        CreateUserDto dto = createDto();
        dto.setEmail("  CARLOS@OPTIFLOW.COM  ");

        String normalizedEmail = "carlos@optiflow.com";

        when(userRepository.existsByEmailIgnoreCase(normalizedEmail))
                .thenReturn(true);

        // WHEN + THEN
        assertThrows(UserAlreadyCreatedException.class,
                () -> service.create(dto));

        verify(userRepository).existsByEmailIgnoreCase(normalizedEmail);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

}
