package engine.graph;

import generated.GPUPDescriptor;
import generated.GPUPTarget;
import generated.GPUPTargets;

import javax.accessibility.AccessibleValue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class GpupGraphChecker {
    final static String JAXB_XML_PACKAGE_NAME = "generated";

    private Graph graph;
    private  List<GPUPTarget> arrTargetName;

    private GPUPDescriptor descriptor;/////

    public GpupGraphChecker(String fileName) throws JAXBException, FileNotFoundException {
        descriptor = JAXBTransform(fileName);
        int targetsSize=descriptor.getGPUPTargets().getGPUPTarget().size();
        arrTargetName =descriptor.getGPUPTargets().getGPUPTarget();
        graph = new Graph(targetsSize);

        insertEdgesIfExist(targetsSize, descriptor);
        IsThereAnyDoubleDependency();
    }

    private void IsThereAnyDoubleDependency(){
        for(int i=0;i<graph.getSize();i++) {
            for (int j = 0; j < graph.getSize(); j++) {
                if (graph.isEdgeExist(i, j) && graph.isEdgeExist(j, i)) {
                   throw new RuntimeException("There is a 'Double Dependency' between two targets in graph,"+arrTargetName.get(i).getName()+" and "+arrTargetName.get(j)+" please enter a valid XML file to continue");

                }
            }
        }

    }

    private void insertEdgesIfExist(int targetsSize, GPUPDescriptor descriptor){
        List<String> namesTarget=new ArrayList<String>(targetsSize);

        //{A,B,C,A}
        for(int i=0;i<targetsSize;i++){//get all the target names to namesTarget array
            String newTargetName=descriptor.getGPUPTargets().getGPUPTarget().get(i).getName();
            for(String currName:namesTarget){
                if(newTargetName.equalsIgnoreCase(currName)){
                    throw new RuntimeException(newTargetName+" already exist in graph, please insert a valid XML file to continue.");
                }
            }
            namesTarget.add(descriptor.getGPUPTargets().getGPUPTarget().get(i).getName());// target name is valid.

        }

        //The following loop puts into the graph all the edges according to the requiredFor or the dependsOn of the file
        for(int i=0;i<targetsSize;i++){

            if(descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPTargetDependencies()!=null){
                int dependencySize = descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPTargetDependencies().getGPUGDependency().size();

                for(int j=0;j<dependencySize;j++){

                    String currentNeighbor=descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPTargetDependencies().getGPUGDependency().get(j).getValue();
                    if(namesTarget.contains(currentNeighbor)){

                        int indexOfNeighbor = namesTarget.indexOf(currentNeighbor);
                        if(descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPTargetDependencies().getGPUGDependency().get(j).getType().equals("requiredFor")){
                            graph.addEdge(i,indexOfNeighbor);

                        }else if(descriptor.getGPUPTargets().getGPUPTarget().get(i).getGPUPTargetDependencies().getGPUGDependency().get(j).getType().equals("dependsOn")){
                            graph.addEdge(indexOfNeighbor,i);
                        }
                    }else{
                        throw new RuntimeException(currentNeighbor + " does not exist in graph, please enter a valid XML file to continue.");

                    }
                }
            }
        }

    }

    private GPUPDescriptor JAXBTransform(String fileName) throws JAXBException, FileNotFoundException {
        GPUPDescriptor descriptor = null;
        try {
            InputStream inputStream = new FileInputStream(new File(fileName));
            descriptor = deserializeFrom(inputStream);

        } catch (JAXBException | FileNotFoundException e) {///מה עושים אם נפל הגאקסוי
            throw  e;
        }
        return descriptor;
    }

    private static GPUPDescriptor deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        //GPUPDescriptor test= (GPUPDescriptor) u.unmarshal(in);
        return (GPUPDescriptor) u.unmarshal(in);
    }

    public Graph getGraph() {
        return graph;
    }

    /////
    public String getPathForTaskFolder() {
        return descriptor.getGPUPConfiguration().getGPUPWorkingDirectory();
    }

}
