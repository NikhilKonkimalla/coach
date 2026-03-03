package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Survey;

public interface SurveyService {
	
	Survey findSurveyByUserId(long userId);

	void saveSurvey(Survey s);

}
