package engine.engineGpup;
import engine.Execution;
import engine.Target;
import engine.dto.*;
import engine.graph.GpupGraphChecker;
import engine.graph.TargetGraph;
import task.SimulationTask;
import task.Task;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

@XmlRootElement
public class GpupExecution implements Execution {
    final int NO_NEED_TO_RUN_ON = -1;

    private Task task;
    private TargetGraph targetGraph;
    private boolean wasRunBefore = false;
    private String pathForTaskFolder;
    private String folder;

    public GpupExecution(){

    }

    public GpupExecution(String fileName) throws JAXBException, FileNotFoundException {
        GpupGraphChecker graphChecker = new GpupGraphChecker(fileName);
        pathForTaskFolder = graphChecker.getPathForTaskFolder();
        task=null;
        targetGraph=new TargetGraph(graphChecker.getGraph(),fileName);
    }



    public Dto getTargetsData(){
        return new DTOTargetsData(targetGraph.getNumOfTargets(),
                targetGraph.getNumOfLeaf(),
                targetGraph.getNumOfMiddle(),
                targetGraph.getNumOfRoot(),
                targetGraph.getNumOfIndependent());
    }

    @Override
    public Dto getTargetData(String targetName) {
        Target target = targetGraph.getTarget(targetName);
        return new DTOTargetData(target.getName(),
                target.getLocation(),
                target.getRequiredToTargetsName(),
                target.getDependingOnTargetsName(),
                target.getData());

    }

    @Override
    public Dto getAllPathsBetweenTargets(String targetFrom, String targetTo,boolean isRequired) {
        if(isRequired){
            return new DTOPathsBetweenTargets(targetGraph.getTarget(targetFrom).getName(), targetGraph.getTarget(targetTo).getName(),targetGraph.getAllPaths(targetFrom, targetTo), isRequired);
        }else{
            return new DTOPathsBetweenTargets(targetGraph.getTarget(targetFrom).getName(), targetGraph.getTarget(targetTo).getName(),targetGraph.getAllPaths(targetTo, targetFrom), isRequired);
        }

    }

    public boolean getWasRunBefore(){ return wasRunBefore;}

    @Override
    public Dto runSimulationTask(int processingTime, double probabilityForSuccess, double probabilityForSuccessWithWarnings, boolean isRandom,boolean isRunTaskFromScratch) throws InterruptedException, IOException {
        task=new SimulationTask(processingTime, probabilityForSuccess, probabilityForSuccessWithWarnings, isRandom);
        createFileForTask("Simulation");
        Dto res =null;
        if(isRunTaskFromScratch){
            res = runTaskFromScratch();
        }else{
            res = runTaskIncremental();
        }
        wasRunBefore = true;
        return res;
    }


    private Dto runTaskIncremental() throws InterruptedException, IOException {
        //the for update the indegree of the vertex
        List<Integer> indegree = new ArrayList<Integer>(targetGraph.getNumOfTargets());
        for (int i = 0; i < targetGraph.getNumOfTargets(); i++) {
            indegree.add(0);
            for(Target currTarget:targetGraph.getTarget(i).getDependingOn()){
                if(currTarget.getFinishStatus().equals(Target.Finish.FAILURE)||currTarget.getFinishStatus().equals(Target.Finish.NOTFINISHED)){
                    indegree.set(i, indegree.get(i)+1);
                }
            }
        }

        for (int i = 0; i < targetGraph.getNumOfTargets(); i++){
            //if target success/with warnings we don't want to run on him
            if(targetGraph.getTarget(i).getFinishStatus().equals(Target.Finish.SUCCESS)||targetGraph.getTarget(i).getFinishStatus().equals(Target.Finish.SUCCESS_WITH_WARNINGS)){
                indegree.set(i, NO_NEED_TO_RUN_ON);
            }else{//target was failure or no_finished so we want initialize him to frozen and NOTFINISHED
                targetGraph.getTarget(i).setTargetStatus(Target.TargetStatus.FROZEN);
                targetGraph.getTarget(i).setFinishStatus(Target.Finish.NOTFINISHED);
            }
        }



        return runTask(indegree);
    }

    private Dto runTaskFromScratch() throws InterruptedException, IOException {
             //the for update the indegree of the vertex
             List<Integer> indegree = new ArrayList<Integer>(targetGraph.getNumOfTargets());
             for (int i = 0; i < targetGraph.getNumOfTargets(); i++) {
                     indegree.add(targetGraph.getTarget(i).getDependingOn().size());
                 }
             return runTask(indegree);
         }

    private Dto runTask(List<Integer> indegree) throws InterruptedException, IOException {
        Date startDate = new Date();

        List<DTOTaskOnTarget> dtoArr=new ArrayList<DTOTaskOnTarget>();
        Queue<Target> queue = new ArrayDeque<Target>();

        //this for insert to the queue all the vertex that there inDegree is 0
        for (int i = 0; i < targetGraph.getNumOfTargets(); i++) {
            if (indegree.get(i) == 0) {
                targetGraph.getTarget(i).setTargetStatus(Target.TargetStatus.WAITING);
                queue.add(targetGraph.getTarget(i));
            }
        }

        while (!queue.isEmpty()) {
            Target currentTarget = queue.poll();
            DTOTaskOnTarget newDto = (DTOTaskOnTarget) task.run(currentTarget);

            //change *all* the dependent on currentTarget to skipped
            if (currentTarget.getFinishStatus().equals(Target.Finish.FAILURE)) {
                List<Target> skippedList=new ArrayList<Target>();
                makeSkipped(currentTarget,skippedList);
                newDto.setTargetsSkipped(skippedList);
            }

            //foreach neighbor of currentTarget
            for (Target neighbor : currentTarget.getRequiredTo()) {
                //indegree[i]=indegree[i]-1
                int indexOfNeighbor = targetGraph.getIndex(neighbor);
                indegree.set(indexOfNeighbor, indegree.get(indexOfNeighbor) - 1);

                //if indegree's neighbor is 0 push to queue
                //neighbor can't finish, because only a target that has been run on them can have a finish, and it is not possible to run on a target unless its indegree is 0, and only now the target 'neighbor' get to 0
                if (indegree.get(indexOfNeighbor) == 0) {
                    //if neighbor status is frozen its mean it is possible to run him so change his status to frozen
                    if (neighbor.getTargetStatus().equals(Target.TargetStatus.FROZEN)) {
                        neighbor.setTargetStatus(Target.TargetStatus.WAITING);
                    }
                    queue.add(neighbor);
                    newDto.addOpenedTarget(neighbor);
                }
            }
            dtoArr.add(newDto);
            writeToFile(newDto);
        }

        for (int i =0 ; i <indegree.size() ;i++) {
            if(indegree.get(i)>0){
                //there is a circle
                dtoArr.add(new DTOTaskOnTarget(targetGraph.getTarget(i).getName(),targetGraph.getTarget(i).getData(),targetGraph.getTarget(i).getTargetStatus() ));
            }
        }
        Date endDate = new Date();
        //
        return new DtoTaskOnTargets(LocalTime.ofSecondOfDay((endDate.getTime() - startDate.getTime())/ 1000),dtoArr);
    }

    private void writeToFile(DTOTaskOnTarget newDto) throws IOException {
        try (Writer out1 = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(folder+"//"+newDto.getName()+".log"), "UTF-8"))) {
            out1.write(newDto.toString());
        }
        catch (Exception e){
             throw e;
        }
    }

    private void makeSkipped(Target currentTarget,List<Target> skippedList) {
        if(currentTarget.getLocation().equals(Target.Location.LEAF)){
            if(currentTarget.getFinishStatus().equals(Target.Finish.FAILURE)){
                return;
            }else{
                currentTarget.setTargetStatus(Target.TargetStatus.SKIPPED);
                return;
            }
        }else{
            for(Target neighbor:currentTarget.getRequiredTo()){
                if(!skippedList.contains(neighbor)){
                    neighbor.setTargetStatus(Target.TargetStatus.SKIPPED);
                    skippedList.add(neighbor);
                    makeSkipped(neighbor, skippedList);
                }
            }
        }
    }
    public DtoTargetCircle getCircleOfTarget(String targetName){
        Target target= targetGraph.getTarget(targetName);

        List<String> res=new ArrayList<String>();
        boolean cont=true;
        for(Target neighbor:target.getRequiredTo()){
            List<ArrayList<String>> allPathsFromNeighbor=targetGraph.getAllPaths(neighbor.getName(),target.getName());

            //for all paths
            for(ArrayList<String> arr : allPathsFromNeighbor){
                //if target name contain in arr there circle that target in
                if(arr.contains(target.getName())){

                    //move all target name to res until see target's name
                    for(int i=0;i<arr.size() && cont;i++){
                        if(arr.get(i).equals(target.getName())){
                            cont=false;

                        }
                        res.add(arr.get(i));

                    }
                }
            }
        }

        return new DtoTargetCircle(res,target.getName());
    }



    private void createFileForTask(String typeOfTask) {
        Date date=new Date();
        //C:\Users\Ohad Hodefi\Desktop\JAVA\GPUP\GPUP\ex1-big.xmlSimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        //C:\Users\Ohad Hodefi\Desktop\JAVA\GPUP\GPUP\ex1-big.xmlSimpleDateFormat d = new SimpleDateFormat("";
        //SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/YYYY hh:mm:ss");
        File temp=new File(pathForTaskFolder);
        if(!temp.exists()){
            throw new RuntimeException("There no path lik: "+ pathForTaskFolder);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        folder=pathForTaskFolder+"\\"+ typeOfTask+" - "+sdf.format(date);
        File file=new File(folder);
        if(!file.mkdir()){
            throw new RuntimeException("Can not open folder");
        }
    }

    @Override
    public void readObjectFromFile(String filePath){
        File file=new File(filePath+".dat");
        if(!file.exists()){
            throw new RuntimeException("file does not exists");
        }
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(filePath+".dat"))){
            this.targetGraph=(TargetGraph) in.readObject();
            this.wasRunBefore=(boolean) in.readObject();
            this.pathForTaskFolder=(String)in.readObject();
            this.task=(Task) in.readObject();
            this.folder=(String) in.readObject();
        }catch (IOException | ClassNotFoundException e){
            System.out.println(e.getMessage());
            throw new IllegalArgumentException(" couldn't load instance from file");
        }
    }

    @Override
    public void writeObjectToFile(String path){
        checkIfGraphIsLoaded();

        path +=".dat";
        validatePath(path);
        try(ObjectOutputStream out=
                    new ObjectOutputStream(
                            new FileOutputStream(path))){
            out.writeObject(targetGraph);
            out.writeObject(wasRunBefore);
            out.writeObject(pathForTaskFolder);
            out.writeObject(task);
            out.writeObject(folder);
            out.flush();
        }catch (IOException e){
            throw new IllegalArgumentException("couldn't write instance to file " + e.getMessage());
        }
    }

    private void validatePath(String path) {
        File tempFile = new File(path);
        if(!tempFile.getParentFile().exists()){
            throw new RuntimeException("file does not exist");
        }
    }

    private void checkIfGraphIsLoaded() {
        if(targetGraph==null){
            throw new RuntimeException("There no any data to save");
        }
    }


}
