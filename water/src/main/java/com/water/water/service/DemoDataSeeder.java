package com.water.water.service;

import com.water.water.model.*;
import com.water.water.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataSeeder {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public String seedDemoData() {
        if (userRepository.existsByEmail("superadmin@gmail.com")) {
            return "Super Admin exists.";
        }

        User u = new User();
        u.setName("Super Admin");
        u.setEmail("superadmin@gmail.com");
        u.setPassword(passwordEncoder.encode("Password@123"));
        u.setRole(Role.ROLE_ADMIN);
        u.setApproved(true);
        userRepository.save(u);

        return "Super Admin seeded successfully.";
    }
}
