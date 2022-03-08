import engine.Execution;
import engine.dto.Dto;
import engine.engineGpup.GpupExecution;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Ui {

    private Execution execution;
    private Boolean isFileUploaded;

    public Ui() {
        this.isFileUploaded = false;
        menu();
        //Dto currMsg

        // System.out.println("1. Please enter file name:");
        // Scanner scanner=new Scanner(System.in);
        // String fileName=scanner.nextLine();
        // try {
        //     FileChecker newFile=new FileChecker(fileName);
        // } catch (FileNotFoundException e) {
        //     e.printStackTrace();
        // }

    }

    private void menu(){

        boolean exit = false;

        while (!exit) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("------------------------------------------------------------");
            System.out.println("|   Choose your preference:                                |");
            System.out.println("|   1. Read file of system information                     |");
            System.out.println("|   2. Display general information about the target graph  |");
            System.out.println("|   3. Display target information                          |");
            System.out.println("|   4. Find all paths between two targets                  |");
            System.out.println("|   5. Run simulation task                                 |");
            System.out.println("|   6. Show circle that target in                          |");
            System.out.println("|   7. Load state to file                                  |");
            System.out.println("|   8. Exit                                                |");
            System.out.println("------------------------------------------------------------");


            String choice = scanner.nextLine();
            switch (choice) {///try catch
                case "1": {
                    readFileMenu();
                    break;
                }
                case "2": {
                    if (isFileAlreadyLoaded()) {
                        displayTargetGraphInfoMenu();
                    }
                    break;
                }
                case "3": {
                    if (isFileAlreadyLoaded()) {
                        displayTargetInfoMenu();
                    }
                    break;
                }
                case "4": {
                    if (isFileAlreadyLoaded()) {
                        findAllPathsMenu();
                    }
                    break;
                }
                case "5": {
                    if (isFileAlreadyLoaded()) {
                        try {
                            runTaskMenu();
                        }catch (InterruptedException e){
                        }
                    }
                    break;
                }
                case "6": {
                    if (isFileAlreadyLoaded()) {
                        runGetTargetCircleMenu();
                    }
                    break;
                }
                case "7": {
                    if (isFileAlreadyLoaded()) {
                        writeToFile();
                    }
                    break;
                }
                case "8": {
                    exit = true;
                    break;
                }
                default: {
                    ///throw
                    System.out.println("Please enter a valid choice (a number from 1-6).");
                }

            }


        }
    }

    private boolean readProcessedFile() {
        System.out.println("Please enter full path to file");
        System.out.println("(To exit - press 00)");
        Scanner scanner=new Scanner(System.in);
        String fileName=scanner.nextLine();

        if (!fileName.equals("00")) {
            try {
                Execution temp=new GpupExecution();
                temp.readObjectFromFile(fileName);
                if(execution==null){
                    execution=new GpupExecution();
                }
                execution=temp;

                System.out.println("File loaded successfully");
                return true;
            }catch(RuntimeException e){
                System.out.println("File does not exist" + "\n");
                readProcessedFile();
            }
        }
        return false;
    }

    private void runGetTargetCircleMenu() {

        System.out.println("Please enter a target name");
        System.out.println("(To exit - press 00)");
        Scanner scanner=new Scanner(System.in);
        String targetName=scanner.nextLine();

        if (!targetName.equals("00")) {
            try {
                System.out.println(execution.getCircleOfTarget(targetName));
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n");
                runGetTargetCircleMenu();
            }
        }
    }

    private boolean isFileAlreadyLoaded() {
        if (!isFileUploaded) {
            System.out.println("Please load file before pressing any other option. (options 1 or 6 are valid)");
            return false;
        }
        return true;
    }

    private void readFileMenu() {
        System.out.println("Please enter 1 to load XML file");
        System.out.println("Please enter 2 to load processed file");
        System.out.println("(To exit - press 00)");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while(true){
            switch (input){
                case "00": {
                    return;
                }
                case "1": {
                    if(readXMLFile()){
                        return;
                    }else{
                        System.out.println("Please enter 1 to load XML file");
                        System.out.println("Please enter 2 to load processed file");
                        System.out.println("(To exit - press 00)");
                        input = scanner.nextLine();
                    }
                    break;
                }
                case "2": {
                    if(readProcessedFile()){
                        return;
                    }else{
                        System.out.println("Please enter 1 to load XML file");
                        System.out.println("Please enter 2 to load processed file");
                        System.out.println("(To exit - press 00)");
                        input = scanner.nextLine();
                    }
                    break;
                }
                default:{
                    System.out.println("Please enter valid input");
                    System.out.println("Please enter 1 to load XML file");
                    System.out.println("Please enter 2 to load processed file");
                    System.out.println("(To exit - press 00)");
                    input = scanner.nextLine();
                }
            }

        }

    }

    private boolean readXMLFile() {
        System.out.println("Please enter the full path of the file (must have .xml ending)");
        System.out.println("(To exit - press 00)");
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.nextLine();

        if (!fileName.equals("00")) {
            try {

                Execution tempExecution = null;
                tempExecution = new GpupExecution(fileName);
                this.execution = tempExecution;
                this.isFileUploaded = true;
                System.out.println("The file loaded successfully!");
                return true;

            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n");
                readXMLFile();
            }
        }
        return false;
    }

    private void displayTargetGraphInfoMenu() {
        System.out.println(execution.getTargetsData());
    }

    private void displayTargetInfoMenu() {
        System.out.println("Please enter the name of target you would like to receive data on: ");
        System.out.println("(To exit - press 00)");

        Scanner scanner = new Scanner(System.in);
        String targetName = scanner.nextLine();
        if (!targetName.equals("00")) {
            try {
                System.out.println(execution.getTargetData(targetName));
            } catch (RuntimeException e) {
                System.out.println(e.getMessage() + "\n");
                displayTargetInfoMenu();
            }
        }
    }

    private void findAllPathsMenu() {
        System.out.println("Please enter the starting target of the path: ");
        System.out.println("(To exit - press 00)");

        Scanner scanner = new Scanner(System.in);
        String targetFromName = scanner.nextLine();
        if (!targetFromName.equals("00")) {

            System.out.println("Please enter the ending target of the path: ");
            System.out.println("(To exit - press 00)");

            String targetToName = scanner.nextLine();
            if (!targetToName.equals("00")) {
                System.out.println("For 'Required for' ratio, please enter 1.");
                System.out.println("For 'Depend on' ratio, please enter 2.");
                System.out.println("(To exit - press 00)");

                String ratio = scanner.nextLine();
                if (!ratio.equals("00")) {
                    try {
                        boolean isRatioGood = false;
                        while (!isRatioGood)
                            switch (ratio) {
                                case "1": {
                                    System.out.println(execution.getAllPathsBetweenTargets(targetFromName, targetToName, true));
                                    isRatioGood = true;
                                    break;
                                }
                                case "2": {
                                    System.out.println(execution.getAllPathsBetweenTargets(targetFromName, targetToName, false));
                                    isRatioGood = true;
                                    break;
                                }
                                case "00": {
                                    isRatioGood = true;
                                    break;
                                }
                                default: {
                                    System.out.println("Ratio is not valid.");
                                    System.out.println("For 'Required for' ratio, please enter 1.");
                                    System.out.println("For 'Depend on' ratio, please enter 2.");
                                    System.out.println("(To exit - press 00)");
                                    ratio = scanner.nextLine();
                                }
                            }
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage() + "\n");///throw
                        findAllPathsMenu();
                    }
                }
            }
        }

    }

    private void runTaskMenu() throws InterruptedException {
        int processingTime = 0;
        double probabilityForSuccess = 0;
        double probabilityForSuccessWithWarnings = 0;
        boolean isRandom = false;
        boolean isRunTaskFromScratch = true;

        boolean validInput = false;
        boolean isValid;

        Scanner scanner = new Scanner(System.in);
        System.out.println("(To exit - press 00)");


        System.out.println("Please choose how to run the task on the graph:");
        System.out.println("To start task 'From Scratch' , press 1.");
        System.out.println("To start task 'Incremental' (on remaining targets), press 2.");
        String input = scanner.nextLine();
        isValid = false;
        while (!isValid) {

            switch (input) {
                case "00": {
                    return;
                }
                case "1": {
                    isRunTaskFromScratch = true;
                    break;
                }
                case "2": {
                    if (!execution.getWasRunBefore()) {
                        System.out.println("Graph never processed before. Starting task 'From Scratch'.");
                    }
                    isRunTaskFromScratch = false;
                    break;
                }
                default: {
                    System.out.println("Please enter valid input.");
                    System.out.println("To start task 'From Scratch' , press 1.");
                    System.out.println("To start task 'Incremental' (on remaining targets), press 2.");
                    input = scanner.nextLine();
                }

            }


            System.out.println("Please enter processing time for each target (in ms)");
            input = scanner.nextLine();
            isValid = false;
            while (!isValid) {
                try {
                    processingTime = Integer.parseInt(input);
                    if (processingTime >= 0) {
                        isValid = true;
                    } else {
                        System.out.println("Please enter a an non-negative integer number");
                        input = scanner.nextLine();
                    }
                    if (input.equals("00"))
                        return;
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a an non-negative integer number");
                    input = scanner.nextLine();
                }
            }


            if (input.equals("00")) {
                return;
            }

            System.out.println("For random time (between 0 to " + processingTime + ") press 1");
            System.out.println("For fixed time (" + processingTime + ") press 2");
            input = scanner.nextLine();

            validInput = false;
            while (!validInput) {
                switch (input) {
                    case "1": {
                        isRandom = true;
                        validInput = true;
                        break;
                    }
                    case "2": {
                        isRandom = false;
                        validInput = true;
                        break;
                    }
                    case "00": {
                        return;
                    }
                    default: {
                        System.out.println("Please enter valid input.");
                        System.out.println("For random time (between 0 to " + processingTime + " ms) press 1");
                        System.out.println("For fixed time (" + processingTime + " ms) press 2");
                        input = scanner.nextLine();
                    }
                }
            }

            if (input.equals("00")) {
                return;
            }
            System.out.println("Please enter the probability for success. (Decimal number between 0 and 1)");
            input = scanner.nextLine();
            if (input.equals("00")) {
                return;
            }

            isValid = false;
            while (!isValid) {
                try {
                    probabilityForSuccess = Double.parseDouble(input);
                    if (probabilityForSuccess >= 0 && probabilityForSuccess <= 1) {
                        isValid = true;
                    } else {
                        System.out.println("Please enter a number between 0 to 1");
                        input = scanner.nextLine();
                    }
                    if (input.equals("00"))
                        return;
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number between 0 to 1");
                    input = scanner.nextLine();
                }
            }

            if (input.equals("00")) {
                return;
            }
            System.out.println("Please enter the probability for success with warnings. (Decimal number between 0 and 1)");
            input = scanner.nextLine();
            isValid = false;
            if (input.equals("00")) {
                return;
            }
            while (!isValid) {
                try {
                    probabilityForSuccessWithWarnings = Double.parseDouble(input);
                    if (probabilityForSuccessWithWarnings >= 0 && probabilityForSuccessWithWarnings <= 1) {
                        isValid = true;
                    } else {
                        System.out.println("Please enter a number between 0 to 1");
                        input = scanner.nextLine();
                    }
                    if (input.equals("00"))
                        return;
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number between 0 to 1");
                    input = scanner.nextLine();
                }
            }

            try {
                System.out.println(execution.runSimulationTask(processingTime, probabilityForSuccess, probabilityForSuccessWithWarnings, isRandom, isRunTaskFromScratch));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void writeToFile(){
        System.out.println("Please enter full path to folder");
        Scanner scanner=new Scanner(System.in);
        String folder=scanner.nextLine();

        if (!folder.equals("00")) {
            try {
                execution.writeObjectToFile(folder);
                System.out.println("File export successfully");
            }catch(RuntimeException e){
                System.out.println("Error with export file" + "\n");
            }
        }
    }
}
