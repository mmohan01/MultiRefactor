package multirefactor;

public class Triple<F, S, T> 
{
	private F first;
	private S second;
	private T third;
	
	public static <F, S, T> Triple<F, S, T> create(F first, S second, T third) 
	{
		return new Triple<F, S, T>(first, second, third);
	}
	
	public F getFirst() 
	{
		return first;
	}
	
	public void setFirst(F first) 
	{
		this.first = first;
	}
	
	public S getSecond() 
	{
		return second;
	}
	
	public void setSecond(S second) 
	{
		this.second = second;
	}
	
	public T getThird() 
	{
		return third;
	}
	
	public void setThird(T third) 
	{
		this.third = third;
	}

	public Triple(F first, S second, T third) 
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
}