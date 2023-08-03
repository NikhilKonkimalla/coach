package org.kidscircle.coach.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "survey")
public class Survey {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    /*
	What drives you?
			My desire to achieve my goals
			To support my family
			To be a role model
			Pressure to excel (family, friends, yourself etc.)
	*/
    private String drive;
    
    /*
	When you feel unmotivated, what do you do?
		Take a break and come back
		Think about why what I am doing is important to me
		Push through whatever block I have
		Quit working
	*/
    private String unmotivated;
    
    /*
	How strongly do you believe in yourself?
		I strongly believe in my abilities 
		I moderately believe in my abilities
		I don’t believe in my abilities
	*/
    
    private String belief;
    
    /*
 	How important is having a clear sense of purpose in motivating you to study/learn?
		It is very important
		It doesn't play a significant role in my motivation.
		It depends on the subject I am learning
		I'm unsure about the importance of having a sense of purpose.
	*/
    private String purpose;
    
    /*
	What impact do past successes have on your motivation?
		 Past successes boost my confidence 
		 I feel pressure to consistently replicate past achievements.
		 Past successes have little impact on my motivation.
	*/
    private String success;
    
    /*
	How do you respond to setbacks?
		I view them as learning opportunities
		I feel discouraged
		Not sure
	*/
    private String setback;
		
    /*
     In what environment do you best perform?
		By myself, focused
		With a group of people who share my goal
		With any group of people
		In a public setting (library, coffee shop etc.
	*/
    private String environment;
    
    /*
	What distracts you from your goals the most?
		Myself/overthinking
		My phone/other electronics
		Other people/bad influences
		School/Extracurriculars not related to my goal
		other responsibilities
	*/
    private String distract;
    
    /*
	What helps you most when face a setback
		Take a break from work
		Talk to a friend
		Speak to a coach or a teacher
		Focus on the next goal
	*/
    private String help;

	public String getDrive() {
		return drive;
	}

	public void setDrive(String drive) {
		this.drive = drive;
	}

	public String getUnmotivated() {
		return unmotivated;
	}

	public void setUnmotivated(String unmotivated) {
		this.unmotivated = unmotivated;
	}

	public String getBelief() {
		return belief;
	}

	public void setBelief(String belief) {
		this.belief = belief;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getSetback() {
		return setback;
	}

	public void setSetback(String setback) {
		this.setback = setback;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getDistract() {
		return distract;
	}

	public void setDistract(String distract) {
		this.distract = distract;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

}
