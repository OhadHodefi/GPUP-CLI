package engine.dto;

import java.util.List;

public class DtoTargetCircle implements Dto{
    private List<String> list;
    private String targetName;

    public DtoTargetCircle(List<String> list, String targetName){
        this.list=list;
        this.targetName=targetName;
    }

    public String toString(){
        String res;
        if(list.size()==0){
            res=targetName+" isn't in any circle";
        }else{
            res=targetName+" in the circle : "+list;
        }
        return res;
    }
}
