package org.kidscircle.coach.db;

import org.kidscircle.coach.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface UserService {


    public void saveUser(User user);
}
