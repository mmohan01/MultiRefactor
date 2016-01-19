package refactory;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;

public class RefactoringSequence {
    private float fitness;
    private int rank;
    private float crowdingDistance;

    private CrossReferenceServiceConfiguration sc;
    private ArrayList<Integer> refactorings;
    private ArrayList<int[]> positions;
    private ArrayList<Integer> IDs;
    private ArrayList<String> refactoringInfo;

    public RefactoringSequence(CrossReferenceServiceConfiguration sc, ArrayList<Integer> refactorings,
                               ArrayList<int[]> positions, ArrayList<Integer> IDs, ArrayList<String> refactoringInfo)
     {
        this.fitness = 1.0f;
        this.rank = 0;
        this.crowdingDistance = 0.0f;

        this.sc = sc;
        this.refactorings = refactorings;
        this.positions = positions;
        this.IDs = IDs;
        this.refactoringInfo = refactoringInfo;
    }

    public CrossReferenceServiceConfiguration getServiceConfiguration()
     {
        return this.sc;
    }

    public void setServiceConfiguration(CrossReferenceServiceConfiguration sc)
     {
        this.sc = sc;
    }

    public float getFitness()
     {
        return this.fitness;
    }

    public void setFitness(float fitness)
     {
        this.fitness = fitness;
    }

    public float getRank()
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

    public ArrayList<int[]> getPositions()
     {
        return this.positions;
    }

    public void setPositions(ArrayList<int[]> positions)
     {
        this.positions = positions;
    }

    public ArrayList<Integer> getIDs()
     {
        return this.IDs;
    }

    public void setIDs(ArrayList<Integer> IDs)
     {
        this.IDs = IDs;
    }

    public ArrayList<String> getRefactoringInfo()
     {
        return this.refactoringInfo;
    }

    public void setRefactoringInfo(ArrayList<String> refactoringInfo)
     {
        this.refactoringInfo = refactoringInfo;
    }
}
