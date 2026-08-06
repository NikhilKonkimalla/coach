package org.kidscircle.coach.db;

import org.kidscircle.coach.model.User;

public interface UserService {

    void saveUser(User user);

    User findUserByUsername(String userName);

    User findByEmail(String email);
}
