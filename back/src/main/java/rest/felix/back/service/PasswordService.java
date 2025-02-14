package rest.felix.back.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PasswordService {

  private final PasswordEncoder passwordencoder;

  public PasswordService(PasswordEncoder passwordEncoder) {
    this.passwordencoder = passwordEncoder;
  }

  public String hashPassword(String rawPassword) {
    return this.passwordencoder.encode(rawPassword);

  }

  public boolean verifyPassword(String rawPassword, String hashedPassword) {
    return this.passwordencoder.matches(rawPassword, hashedPassword);
  }

}
