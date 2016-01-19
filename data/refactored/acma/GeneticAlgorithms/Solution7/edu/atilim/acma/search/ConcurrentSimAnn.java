package edu.atilim.acma.search;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;

public class ConcurrentSimAnn extends ConcurrentMultiRunAlgorithm {
    private double startTemp;
    private int iterations;

    public ConcurrentSimAnn() {
    }

    public ConcurrentSimAnn(String name, RunConfig config, Design initialDesign, double startTemp, int iterations, int runCount) {
        super(name, config, initialDesign, runCount);

        this.startTemp = startTemp;
        this.iterations = iterations;
    }

    @Override
     public AbstractAlgorithm spawnAlgorithm() {
        return new SimAnnAlgorithm(new SolutionDesign(getInitialDesign(), getConfig()), null, startTemp, iterations);
    }

    @Override
     public String getRunInfo() {
        return String.format("Simulated Annealing. Initial Temperature: %f. Iterations: %d.", startTemp, iterations);
    }

    @Override
     public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(0); //version
        out.
        writeDouble(startTemp);
        out.writeInt(iterations);
    }

    @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        in.readInt();
        startTemp = in.readDouble();
        iterations = in.readInt();
    }
}
