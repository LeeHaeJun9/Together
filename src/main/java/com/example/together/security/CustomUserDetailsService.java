// com.example.together.security.CustomUserDetailsService
package com.example.together.security;

import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = userRepository.findByUserId(username)
        .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자"));
    return new CustomUserDetails(u);
  }
}
