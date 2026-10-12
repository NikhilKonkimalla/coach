package org.kidscircle.coach.db;

import org.kidscircle.coach.model.User;

import java.util.List;

public interface UserService {

    void saveUser(User user);

    User findUserByUsername(String userName);

    User findByEmail(String email);

    User findByResetToken(String resetToken);

    List<User> findAllUsers();
}
