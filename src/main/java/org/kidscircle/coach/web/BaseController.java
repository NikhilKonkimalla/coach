package org.kidscircle.coach.web;

import org.kidscircle.coach.db.UserRepository;
import org.kidscircle.coach.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;

public abstract class BaseController {

    @Autowired
    protected UserRepository userRepository;

    protected User getCurrentUser(Principal principal) {
        if (principal == null) throw new IllegalStateException("No authenticated user");
        return userRepository.findByEmail(principal.getName());
    }

    protected void assertOwnership(Long resourceUserId, Principal principal) {
        User current = getCurrentUser(principal);
        if (!current.getUserId().equals(resourceUserId)) {
            throw new SecurityException("Access denied");
        }
    }
}
