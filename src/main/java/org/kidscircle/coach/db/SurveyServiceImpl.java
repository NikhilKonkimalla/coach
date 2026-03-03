package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Survey;
import org.springframework.beans.factory.annotation.Autowired;

public class SurveyServiceImpl implements SurveyService{
	
    @Autowired
    private SurveyRepository surveyRepository;

    @Override
    public Survey findSurveyByUserId(long userId) 
    { 
    	Survey s = surveyRepository.findSurveyByUserId(userId);
    	return s;
    }

    @Override
    public void saveSurvey(Survey s) {
        this.surveyRepository.save(s);
    }

}
