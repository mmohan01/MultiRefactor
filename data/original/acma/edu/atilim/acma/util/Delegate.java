package edu.atilim.acma.util;

public interface Delegate {
	public static interface Void
	{
		void run();
	}
	
	public static interface Void1P<Tin>
	{
		void run(Tin in);
	}
	
	public static interface Void2P<Tin1, Tin2>
	{
		void run(Tin1 in1, Tin2 in2);
	}
	
	public static interface Func<Tret>
	{
		Tret run();
	}
	
	public static interface Func1P<Tret, Tin>
	{
		Tret run(Tin in);
	}
	
	public static interface Func2P<Tret, Tin1, Tin2>
	{
		Tret run(Tin1 in1, Tin2 in2);
	}
}
