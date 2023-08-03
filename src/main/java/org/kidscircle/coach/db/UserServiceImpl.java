package org.kidscircle.coach.db;

import org.kidscircle.coach.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
    private UserRepository userRepository;


    public void saveUser(User user) {
        userRepository.save(user);
    }
    
    public User findUserByUsername(String username) {
        return userRepository.findByUserName(username);
    }
}
