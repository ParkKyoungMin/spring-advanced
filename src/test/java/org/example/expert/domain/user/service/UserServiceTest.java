package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        // User 객체 생성
        mockUser = new User("test@example.com", "encodedOldPassword", UserRole.USER);
        ReflectionTestUtils.setField(mockUser, "id", 1L); // ID 값을 설정
    }

    @Test
    void 비밀번호_변경_성공() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPass123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPassword", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", mockUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("encodedNewPassword");

        // when
        userService.changePassword(userId, request);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode("NewPass123");
    }

    @Test
    void 존재하지_않는_유저_비밀번호_변경_실패() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPass123");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void 현재_비밀번호_불일치_비밀번호_변경_실패() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongPassword", "NewPass123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));

        // then
        assertEquals("현재 비밀번호가 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    void 새_비밀번호가_기존_비밀번호와_같을_경우_비밀번호_변경_실패() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "SamePassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPassword", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("SamePassword", mockUser.getPassword())).thenReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));

        // then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }
}
