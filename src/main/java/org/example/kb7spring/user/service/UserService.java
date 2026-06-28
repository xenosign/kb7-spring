package org.example.kb7spring.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.user.domain.User;
import org.example.kb7spring.user.dto.UserJoinDto;
import org.example.kb7spring.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(UserJoinDto userJoinDto) {
        User user = User.createMember(userJoinDto.getUsername(), passwordEncoder.encode(userJoinDto.getPassword()));
        userRepository.save(user);
    }
}
