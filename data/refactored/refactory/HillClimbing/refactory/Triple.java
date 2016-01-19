package refactory;

public abstract class Triple<F, S, T>  {
    private F first;
    private S second;
    private T third;

    public abstract <F, S, T> Triple<F, S, T> create(F first, S second, T third)
     {
        return new Triple<F, S, T>(first, second, third);
    }

    public abstract F getFirst()
     {
        return first;
    }

    public abstract void setFirst(F first)
     {
        this.first = first;
    }

    public abstract S getSecond()
     {
        return second;
    }

    public abstract void setSecond(S second)
     {
        this.second = second;
    }

    public abstract T getThird()
     {
        return third;
    }

    public abstract void setThird(T third)
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
