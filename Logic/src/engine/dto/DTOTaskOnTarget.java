package engine.dto;

import engine.Target;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DTOTaskOnTarget implements Dto{
   // שם טרגט
   // זמן ריצה
   // DATA
   // FINISH STATUS
   // List string
   // List string skipped


    private String targetName="";
    private String data="";
    private int processingTime=0;
    private Target.Finish finish=Target.Finish.NOTFINISHED;
    private Target.TargetStatus status= Target.TargetStatus.FROZEN;
    private List<String> newOpenedTargets=null;
    private String newSkippedTargets="";
    private LocalDateTime startingTime=LocalDateTime.now();
    private LocalDateTime endingTime=LocalDateTime.now();


    public DTOTaskOnTarget(String name, String data,Target.TargetStatus status) {
        targetName=name;
        this.data=data;
        newOpenedTargets=new ArrayList<String>();
        newSkippedTargets="";
        //this.status= status;
    }
    public String getName(){return this.targetName;}

    public String getData(){return this.data;}

    public void setProcessingTime(int processingTime) {this.processingTime=processingTime; }

    public int getProcessingTime(){return this.processingTime;}

    public void setStatus(Target.TargetStatus status){this.status = status; }

    public Target.TargetStatus getStatus(){return status; }

    public void setFinishStatus(Target.Finish finishStatus) {this.finish=finishStatus; }

    public Target.Finish getFinish(){return finish;}

    public void setTargetsSkipped(List<Target> skippedList) {
        newSkippedTargets=skippedList.toString();
    }

    public void addOpenedTarget(Target neighbor) {
        newOpenedTargets.add(neighbor.toString());
    }

    public void setStartingTime(LocalDateTime startingTime) { this.startingTime=startingTime;}

    public void setEndingTime(LocalDateTime endingTime) {this.endingTime=endingTime;}


    @Override
    public String toString(){
        String s = "***********************************************************\n" +
                didFinishOrNot() +
                "Target's data : " + data + "\n" +
                "Task outcome on target : " + printFinish() + "\n" +
                getNewOpenedTargets() +
                getNewSkippedTargets();
        return s;

    }

    public String getNewSkippedTargets() {
        if(!finish.equals(Target.Finish.FAILURE)){
            return "";
        }
        if(newSkippedTargets.isEmpty()){
            return "There are no skipped targets duo to "+targetName+"\n";
        }else{
            return "The targets that were skipped are: "+newSkippedTargets+"\n";
        }
    }

    private String printFinish() {
        if(finish.equals(Target.Finish.NOTFINISHED)){
            return status.toString() + isFrozen();
        }else{
            return finish.toString();
        }
    }

    public String getNewOpenedTargets() {
        if(this.newOpenedTargets==null){
            return "";
        }
        if(!(finish.equals(Target.Finish.SUCCESS)||finish.equals(Target.Finish.SUCCESS_WITH_WARNINGS))){
            return "";
        }
        if(newOpenedTargets.size()==0) {
            return targetName + " didn't open any other target."+"\n";
        }else{
            return "The targets that are open now: "+ newOpenedTargets.toString()+"\n";
        }
    }

    private String isFrozen(){
        if(status.equals(Target.TargetStatus.FROZEN)){
            return " - Target detected on circle!";
        }
        return "";
    }

    private String didFinishOrNot(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        if(!finish.equals(Target.Finish.NOTFINISHED)){
            return "Processing "+targetName+" :"+"\n" +"Start processing time : "+dtf.format(startingTime)+"\n"+
                    "- target "+targetName+" in sleeping mode -\n"+
                    "- target "+targetName+" woke up -\n"+
                    "End of processing time :"+dtf.format(endingTime)+"\n"+
                    "Total processed for : "+LocalTime.ofSecondOfDay(processingTime/1000).toString()+"\n";
        }
        return "Failed to process "+targetName+"\n";
    }
}
