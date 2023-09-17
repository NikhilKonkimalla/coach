package org.kidscircle.coach.db;


import org.kidscircle.coach.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	
	@Query( "select g from User g where g.userName = :userName" )
	public User findUserByUserName(@Param("userName") String userName);
	
}