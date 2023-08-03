package org.kidscircle.coach.model;


import java.util.Date;

import javax.persistence.*;

@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public String goalName;
    public String goalImportance;
    public int monthNumber;
    public int prepStartMonths;

    // Constructors, getters, and setters

    public Goal() {
    }
    
    public Goal(String name) {
    	this.goalName = name;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGoalName() {
		return goalName;
	}

	public void setGoalName(String goalName) {
		this.goalName = goalName;
	}

	public String getGoalImportance() {
		return goalImportance;
	}

	public void setGoalImportance(String goalImportance) {
		this.goalImportance = goalImportance;
	}



	public int getMonthNumber() {
		return monthNumber;
	}

	public void setMonthNumber(int monthNumber) {
		this.monthNumber = monthNumber;
	}

	public int getPrepStartMonths() {
		return prepStartMonths;
	}

	public void setPrepStartMonths(int prepStartMonths) {
		this.prepStartMonths = prepStartMonths;
	}


    // Other methods (if any)
    // ...
}

