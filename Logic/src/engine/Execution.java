package engine;

import engine.dto.Dto;
import engine.dto.DtoTargetCircle;

import java.io.IOException;

public interface Execution {
    String peath = null;
    public Dto getTargetsData();

    public Dto getTargetData(String target);

    public Dto getAllPathsBetweenTargets(String targetFrom, String targetTo,boolean isRequired);

    public Dto runSimulationTask(int processingTime, double probabilityForSuccess, double probabilityForSuccessWithWarnings, boolean isRandom,boolean isRunTaskFromScratch) throws InterruptedException, IOException;

    public Dto getCircleOfTarget(String targetName);

    public boolean getWasRunBefore();

    public void readObjectFromFile(String filePath);

    public void writeObjectToFile(String path);

}
