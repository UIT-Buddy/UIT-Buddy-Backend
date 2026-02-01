package com.uit.buddy.security;

import com.uit.buddy.entity.user.User;
import com.uit.buddy.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByMssv(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        //
        return new JwtUserDetails(user);
    }
}
