/*
 * ProjectResource.java
 *
 * Created on 27. Mai 2003, 08:11
 */

package net.sourceforge.ganttproject.resource;

/**
 *
 * @author  barmeier
 */
public abstract class ProjectResource {
    
    protected int id;
    protected String name;
    private double costsPerUnit;
    private int maximumUnitsPerDay;
    private String unitMeasure; // means hours, days, meter, ...
    private String description;
    
    public void setName(String name) {
        this.name=name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setDescription(String description) {
        this.description=description;
    }
    
    public String getDescription() {
        return description;
    }

    public void setUnitMeasure(String unitMeasure) {
        this.unitMeasure=unitMeasure;
    }
    
    public String getUnitMeasure() {
        return unitMeasure;
    }
    
    public void setCostsPerUnit(double costsPerUnit) {
        this.costsPerUnit=costsPerUnit;
    }
    
    public double getCostsPerUnit() {
        return costsPerUnit;
    }
    
    public void setMaximumUnitsPerDay(int maximumUnitsPerDay) {
        this.maximumUnitsPerDay=maximumUnitsPerDay;
    }
    
    public int getMaximumUnitsPerDay() {
        return maximumUnitsPerDay;
    }
    
    public void setId(int id) {
        if (this.id==-1)        // setting the id is only allowed when id is not assigned
            this.id=id;
    }
    
    public int getId() {
        return id;
    }
    
    
    public String toString() {
        return name;
    }
    
}
