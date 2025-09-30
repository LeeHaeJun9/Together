package com.example.together.security;

import com.example.together.domain.User;
import com.example.together.domain.SystemRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

  private final User user;
  private final List<GrantedAuthority> authorities;

  public CustomUserDetails(User user) {
    this.user = user;
    String role = (user.getSystemRole() == null ? SystemRole.USER : user.getSystemRole()).name();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
  @Override public String getPassword() { return user.getPassword(); }
  @Override public String getUsername() { return user.getUserId(); }

  /* 잠금 여부는 성공핸들러에서 처리 → 여기서는 true 유지 */
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return true; }
}
