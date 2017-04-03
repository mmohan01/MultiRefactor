package multirefactor;

public class MetricSpecification
{
	private String name;
	private boolean maximise;
	private float weight;

	public MetricSpecification(String name, boolean maximise, float weight) 
	{
		this.name = name;
		this.maximise = maximise;
		this.weight = weight;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public boolean getMaximise() 
	{
		return maximise;
	}
	
	public void setMaximise(boolean maximise) 
	{
		this.maximise = maximise;
	}
	
	public float getWeight() 
	{
		return weight;
	}
	
	public void setWeight(float weight) 
	{
		this.weight = weight;
	}
}