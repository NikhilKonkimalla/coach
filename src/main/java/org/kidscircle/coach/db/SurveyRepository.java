package org.kidscircle.coach.db;


import org.kidscircle.coach.model.Survey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SurveyRepository extends CrudRepository<Survey, Long>{
	
	@Query( "select s from Survey s where s.userId = :userId" )
	public Survey findSurveyByUserId(@Param("userId") long userId);

}
