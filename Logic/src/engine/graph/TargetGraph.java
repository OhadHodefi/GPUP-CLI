package engine.graph;

import engine.Target;
import generated.GPUPDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TargetGraph implements Serializable {
    final static String JAXB_XML_PACKAGE_NAME = "generated";

    private Graph graph;
    private List<Target> targetList;
    //private Target.Location independent;

    public TargetGraph(Graph graph, String fileName) throws JAXBException, FileNotFoundException {
        this.graph=graph;
        GPUPDescriptor descriptor=extracted(fileName);
        createTargetList(descriptor);
        insertDependOnAndRequire();
    }

    private void insertDependOnAndRequire() {
        for(int i=0;i<targetList.size();i++){
            for (Integer vertex: graph.myDependentOn(i)) {
                targetList.get(i).addDependingOn(targetList.get(vertex));
            }

            for (Integer vertex: graph.myRequiredFor(i)) {
                targetList.get(i).addRequiredFor(targetList.get(vertex));
            }
        }
    }

    private void createTargetList(GPUPDescriptor descriptor) {
        targetList=new ArrayList<Target>();
        for(int i=0;i<descriptor.getGPUPTargets().getGPUPTarget().size();i++){
            Target newTarget=new Target(descriptor.getGPUPTargets().getGPUPTarget().get(i).getName(),
                                        descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPUserData(),
                                        graph.getLocation(i));
            targetList.add(newTarget);
        }
    }

    private GPUPDescriptor extracted(String fileName) throws FileNotFoundException, JAXBException {
        GPUPDescriptor descriptor=null;
        try {
            InputStream inputStream = new FileInputStream(new File(fileName));
            descriptor = deserializeFrom(inputStream);
        } catch (JAXBException | FileNotFoundException e) {
            throw e;
        }
        return descriptor;
    }

    private static GPUPDescriptor deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (GPUPDescriptor) u.unmarshal(in);
        //GPUPDescriptor test= (GPUPDescriptor) u.unmarshal(in);
    }

    public int getNumOfTargets(){ return targetList.size();}

    public int getNumOfIndependent(){
        int res=0;
        for (Target target:targetList) {
            if(target.getLocation()==Target.Location.INDEPENDENT){
                res++;
            }
        }
        return res;
    }

    public int getNumOfLeaf(){
        int res=0;
        for (Target target:targetList) {
            if(target.getLocation()== Target.Location.LEAF){
                res++;
            }
        }
        return res;
    }

    public int getNumOfMiddle(){
        int res=0;
        for (Target target:targetList) {
            if(target.getLocation()==Target.Location.MIDDLE){
                res++;
            }
        }
        return res;
    }

    public int getNumOfRoot(){
        int res=0;
        for (Target target:targetList) {
            if(target.getLocation()==Target.Location.ROOT){
                res++;
            }
        }
        return res;
    }

    public Target getTarget(String targetName) {
        Target res=null;
        for (Target target:targetList) {
            if(targetName.equalsIgnoreCase(target.getName())){
                return target;
            }
        }
        throw new RuntimeException(targetName+" does not exist");
    }

    public List<ArrayList<String>> getAllPaths(String targetNameFrom, String targetNameTo){
        int targetFromIndex = 0,targetToIndex = 0;

        Target targetFrom = getTarget(targetNameFrom);
        Target targetTo = getTarget(targetNameTo);

        for(int i=0;i<targetList.size();i++){
            if(targetList.get(i).equals(targetFrom)){
                targetFromIndex=i;
            }
            if(targetList.get(i).equals(targetTo)){
                targetToIndex=i;
            }
        }



        List<ArrayList<Integer>> indexPaths=graph.getAllPaths(targetFromIndex,targetToIndex);
        return convertIndexPathsToNamePathsString(indexPaths);

    }

    private List<ArrayList<String>> convertIndexPathsToNamePathsString(List<ArrayList<Integer>> indexPaths) {
        List<ArrayList<String>>res= new ArrayList<ArrayList<String>>();
        for(ArrayList<Integer> arr:indexPaths){
            ArrayList<String> newPath=new ArrayList<String>();
            for(Integer i:arr){
                newPath.add(getTargetNameByIndex(i));
            }
            res.add(newPath);
        }
        return res;
    }

    private String getTargetNameByIndex(Integer i) {
        return targetList.get(i).getName();
    }



    public Target getTarget(int i) {
        return targetList.get(i);
    }

    public int getIndex(Target neighbor) {
        return targetList.indexOf(neighbor);
    }

    public void setListTarget(List<Target> targetList) {
        this.targetList=targetList;
    }


}

