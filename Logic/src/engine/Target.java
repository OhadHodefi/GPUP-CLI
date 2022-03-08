package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Target implements Serializable {



    public enum Location{LEAF,MIDDLE,ROOT,INDEPENDENT}
    public enum Finish{SUCCESS,FAILURE, SUCCESS_WITH_WARNINGS,NOTFINISHED}
    public enum TargetStatus{FROZEN ,SKIPPED ,WAITING ,INPROCESS ,FINISHED}

    private String name;
    private String data;
    private TargetStatus targetStatus;
    private Location location;
    private List<Target>  dependsOn=new ArrayList<Target>();
    private List<Target> requiredFor=new ArrayList<Target>();
    private Finish finishStatus;



    public Target(String name, String data, Location type){
        this.name=name;
        this.data=data;
        targetStatus=TargetStatus.FROZEN;
        location = type;
        finishStatus=Finish.NOTFINISHED;
    }

    public String getName(){ return name;  }

    public List<Target>getDependingOn(){
        return dependsOn;
    }

    public List<Target>getRequiredTo(){
        return requiredFor;
    }

    public TargetStatus getTargetStatus(){
        return targetStatus;
    }

    public Finish getFinishStatus(){
        return finishStatus;
    }


    public boolean addDependingOn(Target newTarget){
        if(newTarget!=null){
            dependsOn.add(newTarget);
            return true;
        }else {
            ///trow try add null target
            return false;
        }
    }

    public boolean addRequiredFor(Target newTarget){
        if(newTarget!=null){
            requiredFor.add(newTarget);
            return true;
        }else {
            ///trow try add null target
            return false;
        }
    }

    public Location getLocation(){return location;}

    public String getData() {return data;}

    public List<String> getRequiredToTargetsName() {
        List<String>res=new ArrayList<String>();
        for(Target target : requiredFor){
            res.add(target.getName());
        }
        return res;
    }

    public List<String> getDependingOnTargetsName() {
        List<String>res=new ArrayList<String>();
        for(Target target : dependsOn){
            res.add(target.getName());
        }
        return res;
    }



    @Override
    public String toString(){
        return name;
    }



    public void setFinishStatus(Finish outcome) {
        finishStatus=outcome;
    }
    public void setTargetStatus(TargetStatus status) {
        targetStatus=status;
    }


}
