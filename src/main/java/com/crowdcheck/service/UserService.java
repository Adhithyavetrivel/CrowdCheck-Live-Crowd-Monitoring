package com.crowdcheck.service;

import com.crowdcheck.entity.User;
import com.crowdcheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing user entities and role assignments.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Integer userId, User details) {
        User existing = getUserById(userId);
        existing.setUserName(details.getUserName());
        existing.setContactInfo(details.getContactInfo());
        existing.setUserRole(details.getUserRole());
        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User existing = getUserById(userId);
        userRepository.delete(existing);
    }
}
