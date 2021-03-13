package siv;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final List<String> VALID_MODES = new ArrayList<>(Arrays.asList("-i", "-v", "-h"));
    private static final List<String> VALID_OPTIONS = new ArrayList<>(Arrays.asList( "-D","-V","-R","-H"));

    public static final Integer MAX_DEPTH = 5;

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);

        //Read arguments
        Integer nbSpecifiedMode = 0;
        String chosenMode = "";
        String pathDirectoryToMonitor = null;
        String pathVerificationFile = null;
        String pathReportFile = null;
        String hashFunction = null;
        String option = null;
        for(String s : args) {
            if (VALID_MODES.contains(s)) {
                //Argument is a valid mode
                chosenMode = s;
                nbSpecifiedMode++;
            } else if (VALID_OPTIONS.contains(s)) {
                //Argument is a valid option
                option = s;
            } else if (option != null) {
                //Get value of the given parameter
                switch(option) {
                    case "-D":
                        pathDirectoryToMonitor = s;
                        break;
                    case "-V":
                        pathVerificationFile = s;
                        break;
                    case "-R":
                        pathReportFile = s;
                        break;
                    case "-H":
                        hashFunction = s;
                        break;
                }
                option = null;
            }
        }

        //Check number of mode
        if (nbSpecifiedMode==0) {
            outputError("An incorrect mode or no mode was specified. Please see help by using mode -h.");
        } else if (nbSpecifiedMode > 1) {
            outputError("Too many modes were specified. Please limit to one.");
        }
        //Check options and arguments
        if (chosenMode.equals("-v") && hashFunction != null) {
            outputError("You can't give a hash function in verification mode.");
        }
        if (chosenMode.equals("-v") || chosenMode.equals("-i")) {
            if (pathDirectoryToMonitor == null) {
                outputError("No monitored directory was specified.");
            } else if (pathVerificationFile == null) {
                outputError("No verification file was specified.");
            } else if (pathReportFile == null) {
                outputError("No report file was specified.");
            }
        }

        File directoryToMonitor = null;
        File verificationFile = null;
        File reportFile = null;

        if (chosenMode.equals("-i")) {
            //Initialization mode
            //Verify that hash digest is valid
            MessageDigest mDigest = null;
            try {
                mDigest = MessageDigest.getInstance(hashFunction);
            } catch (NoSuchAlgorithmException e) {
                outputError("The hashing algorithm '"+ hashFunction +"' is not supported. Please see help using -h to see the supported hash functions.");
            } catch (NullPointerException e) {
                outputError("No hashing function was specified.");
            }

            //Construct file objects
            try {
                directoryToMonitor = (new File(pathDirectoryToMonitor)).getCanonicalFile();
                verificationFile = (new File(pathVerificationFile)).getCanonicalFile();
                reportFile = (new File(pathReportFile)).getCanonicalFile();
            } catch (IOException e) {
                System.out.println("Canonical couldn't be resolved, absolute paths will be used instead. So you might end up with filenames containing .. or . in the reports.");
                directoryToMonitor = new File(pathDirectoryToMonitor);
                verificationFile = new File(pathVerificationFile);
                reportFile = new File(pathReportFile);
            }

            //Verify that monitored directory exists
            if (!directoryToMonitor.exists() || !directoryToMonitor.isDirectory()) {
                outputError("The given monitored directory doesn't exist or is not a directory");
            }

            //Check that location of verification file and report file are out of monitored directory
            FileMaster.checkIfFileOutOfDirectory(verificationFile, directoryToMonitor);
            FileMaster.checkIfFileOutOfDirectory(reportFile, directoryToMonitor);

            //Check if report or verification file already exist and ask for overwrite
            FileMaster.checkIfFileExistAndAskOverwrite(verificationFile, reader);
            FileMaster.checkIfFileExistAndAskOverwrite(reportFile, reader);

            //Iterate through directory content
            long start = System.nanoTime();
            ReportInfo report = FileMaster.exploreDirectoryContent(directoryToMonitor, mDigest, MAX_DEPTH);
            report.setPathVerificationFile(verificationFile.getAbsolutePath());
            long end = System.nanoTime();
            report.setTimelength(end - start);

            //Write verification and report file
            FileMaster.writeVerifFile(report.getFilesInfo(), verificationFile, hashFunction);
            FileMaster.writeInitializationReportFile(reportFile, report);

            System.out.println("Initialization complete, you can check the report file now");

        } else if (chosenMode.equals("-v")) {
            //Verification mode
            //Check that verification file exists
            try {
                directoryToMonitor = (new File(pathDirectoryToMonitor)).getCanonicalFile();
                verificationFile = (new File(pathVerificationFile)).getCanonicalFile();
                reportFile = (new File(pathReportFile)).getCanonicalFile();
            } catch (IOException e) {
                System.out.println("Canonical couldn't be resolved, absolute paths will be used instead. So you might end up with filenames containing .. or . in the reports.");
                directoryToMonitor = new File(pathDirectoryToMonitor);
                verificationFile = new File(pathVerificationFile);
                reportFile = new File(pathReportFile);
            }

            //Verify that monitored directory exists
            if (!directoryToMonitor.exists() || !directoryToMonitor.isDirectory()) {
                outputError("The given monitored directory doesn't exist or is not a directory");
            }

            //Check that location of verification file and report file are out of monitored directory
            if (!verificationFile.exists()) {
                outputError("The verification file doesn't exist.");
            }
            FileMaster.checkIfFileOutOfDirectory(verificationFile, directoryToMonitor);
            FileMaster.checkIfFileOutOfDirectory(reportFile, directoryToMonitor);

            //Check if report file already exist and ask for overwrite
            FileMaster.checkIfFileExistAndAskOverwrite(reportFile, reader);

            //Get information from verif file
            hashFunction = FileMaster.getHashFunction(verificationFile);
            List<FileInfo> infos = FileMaster.readVerifFile(verificationFile);

            //Create hash digest
            MessageDigest mDigest = null;
            try {
                mDigest = MessageDigest.getInstance(hashFunction);
            } catch (NoSuchAlgorithmException e) {
                outputError("The hashing function '"+ hashFunction +"' from verification file is not supported. Someone changed the file manually.");
            } catch (NullPointerException e) {
                outputError("No hashing function was written in verification file, someone made changes.");
            }

            //Explore directory
            long start = System.nanoTime();
            ReportInfo report = FileMaster.exploreDirectoryContent(directoryToMonitor, mDigest, MAX_DEPTH);
            report.setPathVerificationFile(verificationFile.getAbsolutePath());

            //Compare verification data and directory
            String reportInfo = "";
            Integer nbWarnings = 0;
            boolean fileChecked = false;
            String warning = "";
            for(FileInfo verifFi : new ArrayList<>(infos)) {
                for(FileInfo actualFi : report.getFilesInfo()) {
                    //Here we can use the path as comparison, since the path of the monitored directory is given
                    //using canonical path, and then the absolute path of each file/subfolder is retrieved
                    if (verifFi.getFullPath().equals(actualFi.getFullPath())) {
                        //Same file or directory
                        warning = "";
                        //Check each attribute
                        if (verifFi.getSize() != actualFi.getSize()) {
                            warning += "size, ";
                        }
                        if (!verifFi.getDigestedContent().equals(actualFi.getDigestedContent())) {
                            warning += "content, ";
                        }
                        if (!verifFi.getOwnerUser().equals(actualFi.getOwnerUser())) {
                            warning += "owner (user), ";
                        }
                        if (!verifFi.getOwnerGroup().equals(actualFi.getOwnerGroup())) {
                            warning += "owner (group), ";
                        }
                        if (!verifFi.getAccessRights().equals(actualFi.getAccessRights())) {
                            warning += "access rights, ";
                        }
                        if (verifFi.getLastModifDate() != actualFi.getLastModifDate()) {
                            warning += "last modification date, ";
                        }

                        //Create line for report file
                        if (!warning.equals("")) {
                            warning = (verifFi.getType().equals("f") ? "File" : "Directory") + " " + verifFi.getFullPath() + " --- Change of " + warning;
                            reportInfo += warning.substring(0, warning.length() -2) + ".\n";
                            nbWarnings++;
                        }
                        //Since the file has been checked, we can remove it from the list and break the for loop
                        fileChecked = true;
                        report.getFilesInfo().remove(actualFi);
                        break;
                    }
                }
                if (fileChecked) {
                    //The file has been checked so we can remove it from the list and go to the next one
                    fileChecked = false;
                    infos.remove(verifFi);
                    continue;
                }
            }

            //Handle removed files/directory
            for(FileInfo deletedFile : infos) {
                reportInfo += "The " + (deletedFile.getType().equals("f") ? "file " : "directory ") + deletedFile.getFullPath() +" has been deleted.\n";
                nbWarnings++;
            }
            //Handle new files/directory
            for(FileInfo newFile : report.getFilesInfo()) {
                reportInfo += "The " + (newFile.getType().equals("f") ? "file " : "directory ") + newFile.getFullPath() +" is new.\n";
                nbWarnings++;
            }

            long end = System.nanoTime();
            report.setTimelength(end - start);

            //Write report file
            FileMaster.writeVerificationReportFile(reportFile, report, nbWarnings, reportInfo);
            System.out.println("Verification complete, you can check the report file now (" + nbWarnings + " warnings).");

        } else {
            //Help page
            outputHelp();
        }
    }

    private static void outputHelp() {
        System.out.println("System Integrity Verifier (SIV) - Help Page\n" +
                "ARGUMENTS\n" +
                "   -i\n" +
                "      Initialization mode with max folder depth of " + MAX_DEPTH + "\n" +
                "   -v\n" +
                "      Verification mode with max folder depth of " + MAX_DEPTH + "\n" +
                "   -h\n" +
                "      Show this page\n" +
                "   -D <directory>\n" +
                "      Specify the directory to monitor\n" +
                "      The directory must exist or the program will fail\n" +
                "      Mandatory argument with -i or -v\n" +
                "   -V <file>\n" +
                "      Specify the verification file\n" +
                "      If the file already exist you will be asked if you want to overwrite it\n" +
                "      It should be outside the monitored directory\n" +
                "      Mandatory argument with -i or -v\n" +
                "   -R <file>\n" +
                "      Specify the report file\n" +
                "      If the file already exist you will be asked if you want to overwrite it\n" +
                "      It should be outside the monitored directory\n" +
                "      Mandatory argument with -i or -v\n" +
                "   -H <function>\n" +
                "      Specify the hashing function\n" +
                "      Mandatory argument with -i but can't be used with -v\n" +
                "      Supported functions are:\n" +
                "         MD5 with md5|MD5\n" +
                "         MD2 with md2|MD2\n" +
                "         SHA-1 with sha1|sha-1|SHA1|SHA-1\n" +
                "         SHA-256 with sha-256|SHA-256\n" +
                "         SHA-384 with sha-384|SHA-384\n" +
                "         SHA-512 with sha-512|SHA-512\n" +
                "USE\n" +
                "   To start monitoring a directory you can use this command:\n" +
                "      java -cp src/ siv.Main -i -D important_directory -V verificationDB -R my_report.txt -H sha1\n" +
                "   This will start the program in initialization mode and important_directory will be monitored.\n" +
                "   Once the initialization is complete, statistics will be reported into my_report.txt and \n" +
                "   the verification file will be created from content of important_directory.\n" +
                "\n" +
                "   To verify if a directory was modified, you can use this command:\n" +
                "      java -cp src/ siv.Main -v -D important_directory -V verificationDB -R my_report2.txt\n" +
                "   This will start the program in verification mode and important_directory will be verified\n" +
                "   by comparing the content with verificationDB.\n" +
                "   Once this task is complete, statistics will be reported into my_report2.txt.\n" +
                "AUTHOR\n" +
                "   Written by Sylvain Roncoroni");
    }

    /**
     * Output a message and end the program
     * @param msg
     */
    public static void outputError(String msg) {
        System.out.println(msg);
        System.exit(1);
    }
}
