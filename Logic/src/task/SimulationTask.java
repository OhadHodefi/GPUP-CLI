package task;

import engine.Target;
import engine.dto.DTOTaskOnTarget;
import engine.dto.Dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

public class SimulationTask implements Task, Serializable {
    private int processingTime;
    private double probabilityForSuccess;
    private double probabilityForSuccessWithWarnings;

    public SimulationTask(int processingTime, double probabilityForSuccess, double probabilityForSuccessWithWarnings, boolean isRandom) {
       this.probabilityForSuccess=probabilityForSuccess;
       this.probabilityForSuccessWithWarnings=probabilityForSuccessWithWarnings;
       if(isRandom){
           if(processingTime!=0){
               Random r = new Random();
               int low = 0;
               int high = processingTime;
               this.processingTime = r.nextInt(high-low) + low;
           }else{
               processingTime=0;
           }
       }else{
           this.processingTime=processingTime;
       }

    }

    public Dto run(Target currentTarget) throws InterruptedException {
        DTOTaskOnTarget dtoTaskOnTarget = new DTOTaskOnTarget(currentTarget.getName(), currentTarget.getData(),currentTarget.getTargetStatus());

        if(currentTarget.getTargetStatus().equals(Target.TargetStatus.WAITING)){
            currentTarget.setTargetStatus(Target.TargetStatus.INPROCESS);

            LocalDateTime startingTime = LocalDateTime.now();
            dtoTaskOnTarget.setStartingTime(startingTime);
            Thread.sleep(processingTime);
            dtoTaskOnTarget.setProcessingTime(processingTime);

            Random r = new Random();
            double outcome = r.nextDouble();

            if(outcome<=probabilityForSuccess){
                double isSuccessWithWarnings = r.nextDouble();
                if(isSuccessWithWarnings<=probabilityForSuccessWithWarnings){
                    currentTarget.setFinishStatus(Target.Finish.SUCCESS_WITH_WARNINGS);
                }else{
                    currentTarget.setFinishStatus(Target.Finish.SUCCESS);
                }
            }else{
                currentTarget.setFinishStatus(Target.Finish.FAILURE);
            }

            currentTarget.setTargetStatus(Target.TargetStatus.FINISHED);
        }
        dtoTaskOnTarget.setFinishStatus(currentTarget.getFinishStatus());
        LocalDateTime endingTime = LocalDateTime.now();
        dtoTaskOnTarget.setEndingTime(endingTime);
        dtoTaskOnTarget.setStatus(currentTarget.getTargetStatus());
        dtoTaskOnTarget.setFinishStatus(currentTarget.getFinishStatus());
        return dtoTaskOnTarget;
    }


    public void setProcessingTime(int processingTime){this.processingTime=processingTime;}
    public void setProbabilityForSuccess(double probabilityForSuccess){this.probabilityForSuccess=probabilityForSuccess;}
    public void setProbabilityForSuccessWithWarnings(double probabilityForSuccessWithWarnings){this.probabilityForSuccessWithWarnings=probabilityForSuccessWithWarnings;}

}
