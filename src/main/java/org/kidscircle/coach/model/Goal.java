package org.kidscircle.coach.model;


import javax.persistence.*;

/*
 * CREATE TABLE goal (
    id INTEGER PRIMARY KEY,
    user_name TEXT,
    goal_name TEXT NOT NULL,
    goal_importance TEXT,
    month_number INTEGER,
    prep_start_months INTEGER)
    
 */
@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;
    
    private Long userId;
    
    
    public String goalName;
    public String goalImportance;
    public int monthNumber;
    public int year;
    public int prepStartMonths;

    // Constructors, getters, and setters

    public Goal() {
    }
    
    public Goal(String name) {
    	this.goalName = name;
    }

	public Long getGoalId() {
		return goalId;
	}

	public void setGoalId(Long id) {
		this.goalId = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
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

