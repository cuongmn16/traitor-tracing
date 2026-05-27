package com.example.traitortracing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.traitortracing.entity.Users;
import com.example.traitortracing.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.findByUsername("admin").isEmpty()) {

            Users admin = new Users();

            admin.setUsername("admin");

            // mã hóa mật khẩu
            admin.setPassword_hash(
                    passwordEncoder.encode("admin"));

            admin.setRole("ADMIN");

            userRepository.save(admin);

            System.out.println("Đã tạo tài khoản admin");
        }
    }
}