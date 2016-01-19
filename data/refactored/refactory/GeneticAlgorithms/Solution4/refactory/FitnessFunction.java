package refactory;

import java.util.ArrayList;

public abstract class FitnessFunction {
    // No attributes - empty constructor.
    public FitnessFunction() {}

    public float calculateScore(Metrics m, ArrayList<Triple<String, Boolean, Float>> configuration)
     {
        float amount = 0;

        for (Triple<String, Boolean, Float> metric: configuration)
         {
            switch (metric.getFirst()) {
                case "classDeclarationAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.classDeclarationAmount()) : -(metric.getThird() * m.classDeclarationAmount());
                    break;
                case "methodDeclarationAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.methodDeclarationAmount()) : -(metric.getThird() * m.methodDeclarationAmount());
                    break;
                case "methodsPerType":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.methodsPerType()) : -(metric.getThird() * m.methodsPerType());
                    break;
                case "abstractness":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.abstractness()) : -(metric.getThird() * m.abstractness());
                    break;
                case "abstractAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.abstractAmount()) : -(metric.getThird() * m.abstractAmount());
                    break;
                case "staticAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.staticAmount()) : -(metric.getThird() * m.staticAmount());
                    break;
                case "finalAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.finalAmount()) : -(metric.getThird() * m.finalAmount());
                    break;
                case "innerClassAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.innerClassAmount()) : -(metric.getThird() * m.innerClassAmount());
                    break;
                case "childAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.childAmount()) : -(metric.getThird() * m.childAmount());
                    break;
                case "linesOfCode":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.linesOfCode()) : -(metric.getThird() * m.linesOfCode());
                    break;
                case "fileAmount":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.fileAmount()) : -(metric.getThird() * m.fileAmount());
                    break;
                case "visibility":
                    amount += (metric.getSecond() == true) ? (metric.getThird() * m.visibility()) : -(metric.getThird() * m.visibility());
                    break;
            }
        }

        return amount;
    }
}
