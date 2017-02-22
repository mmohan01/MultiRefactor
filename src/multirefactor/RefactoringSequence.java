package multirefactor;

import java.util.ArrayList;
import java.util.HashMap;

public class RefactoringSequence
{
	private float fitness;
	private float[] MOFitness;
	private int rank;
	private float crowdingDistance;
	
	private ArrayList<Integer> refactorings;
	private ArrayList<Integer> positions;
	private ArrayList<String> names;
	private ArrayList<String> refactoringInfo;
	private ArrayList<String> affectedClasses;
	private HashMap<String, Integer> elementDiversity;

	public RefactoringSequence(ArrayList<Integer> refactorings, ArrayList<Integer> positions, ArrayList<String> names, 
			                   ArrayList<String> refactoringInfo) 
	{
		this.fitness = 1.0f;
		this.rank = 0;
		this.crowdingDistance = -1.0f;

		this.refactorings = refactorings;
		this.positions = positions;
		this.names = names;
		this.refactoringInfo = refactoringInfo;
	}

	// Constructor used to store information for priority objective.
	public RefactoringSequence(ArrayList<Integer> refactorings, ArrayList<Integer> positions, ArrayList<String> names, 
							   ArrayList<String> refactoringInfo, ArrayList<String> affectedClasses) 
	{
		this.fitness = 1.0f;
		this.rank = 0;
		this.crowdingDistance = -1.0f;
		
		this.refactorings = refactorings;
		this.positions = positions;
		this.names = names;
		this.refactoringInfo = refactoringInfo;
		this.affectedClasses = affectedClasses;
	}

	// Constructor used to store information for diversity objective.
	public RefactoringSequence(ArrayList<Integer> refactorings, ArrayList<Integer> positions, ArrayList<String> names, 
			                   ArrayList<String> refactoringInfo, HashMap<String, Integer> elementDiversity) 
	{
		this.fitness = 1.0f;
		this.rank = 0;
		this.crowdingDistance = -1.0f;

		this.refactorings = refactorings;
		this.positions = positions;
		this.names = names;
		this.refactoringInfo = refactoringInfo;
		this.elementDiversity = elementDiversity;
	}
	
	// Constructor used to store information for both the priority and diversity objectives.
	public RefactoringSequence(ArrayList<Integer> refactorings, ArrayList<Integer> positions, ArrayList<String> names, 
							   ArrayList<String> refactoringInfo, ArrayList<String> affectedClasses, HashMap<String, Integer> elementDiversity) 
	{
		this.fitness = 1.0f;
		this.rank = 0;
		this.crowdingDistance = -1.0f;

		this.refactorings = refactorings;
		this.positions = positions;
		this.names = names;
		this.refactoringInfo = refactoringInfo;
		this.affectedClasses = affectedClasses;
		this.elementDiversity = elementDiversity;
	}
	
	public float getFitness()
	{
		return this.fitness;
	}
	
	public void setFitness(float fitness)
	{
		this.fitness = fitness;
	}
	
	public float[] getMOFitness()
	{
		return this.MOFitness;
	}
	
	public void setMOFitness(float[] MOFitness)
	{
		this.MOFitness = MOFitness;
	}
	
	public int getRank()
	{
		return this.rank;
	}
	
	public void setRank(int rank)
	{
		this.rank = rank;
	}
	
	public float getCrowdingDistance()
	{
		return this.crowdingDistance;
	}
	
	public void setCrowdingDistance(float crowdingDistance)
	{
		this.crowdingDistance = crowdingDistance;
	}
	
	public ArrayList<Integer> getRefactorings()
	{
		return this.refactorings;
	}	
	
	public void setRefactorings(ArrayList<Integer> refactorings)
	{
		this.refactorings = refactorings;
	}
	
	public ArrayList<Integer> getPositions()
	{
		return this.positions;
	}	
	
	public void setPositions(ArrayList<Integer> positions)
	{
		this.positions = positions;
	}
	
	public ArrayList<String> getNames()
	{
		return this.names;
	}
	
	public void setNames(ArrayList<String> names)
	{
		this.names = names;
	}
	
	public ArrayList<String> getRefactoringInfo()
	{
		return this.refactoringInfo;
	}
	
	public void setRefactoringInfo(ArrayList<String> refactoringInfo)
	{
		this.refactoringInfo = refactoringInfo;
	}

	public ArrayList<String> getAffectedClasses()
	{
		return this.affectedClasses;
	}
	
	public void setAffectedClasses(ArrayList<String> affectedClasses)
	{
		this.affectedClasses = affectedClasses;
	}
	
	public HashMap<String, Integer> getElementDiversity()
	{
		return this.elementDiversity;
	}
	
	public void setElementDiversity(HashMap<String, Integer> elementDiversity)
	{
		this.elementDiversity = elementDiversity;
	}
}
