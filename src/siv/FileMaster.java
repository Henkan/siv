package siv;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * This class handles operations related to files and folders.
 */
public class FileMaster {

    /**
     * Check if the specified file exist and ask for overwrite
     * @param fileToCheck
     * @param reader
     */
    public static void checkIfFileExistAndAskOverwrite(File fileToCheck, Scanner reader) {
        if (fileToCheck.exists()) {
            System.out.println("The file " + fileToCheck.getName() + " already exist, do you want to overwrite it ? (yes/no)");
            String answer = reader.nextLine().toLowerCase();
            while(!answer.equals("yes") && !answer.equals("no")) {
                System.out.println("Please enter a correct input: yes/no");
                answer = reader.nextLine().toLowerCase();
            }
            if (!answer.equals("yes")) {
                System.out.println("The program will then terminate.");
                System.exit(0);
            }
        }
    }

    /**
     * Verify that the file is outside the given directory (and outside one of its subfolder)
     * @param fileToCheck
     * @param directory
     */
    public static void checkIfFileOutOfDirectory(File fileToCheck, File directory) {
        if (directory.getAbsolutePath().equals(fileToCheck.getParent()) || fileToCheck.getAbsolutePath().replace(fileToCheck.getName(), "").contains(directory.getAbsolutePath())) {
            System.out.println("The file "+ fileToCheck.getName() +" is inside the monitored directory.");
            System.exit(1);
        }
    }

    /**
     * Write in the verification file
     * @param infos
     * @param verifFile
     * @param hashFunction
     */
    public static void writeVerifFile(List<FileInfo> infos, File verifFile, String hashFunction) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(verifFile));
            writer.write(hashFunction + "\n");
            for(FileInfo fi : infos) {
                writer.write(fi.toString() + "\n");
            }
            writer.close();
        } catch(IOException e) {
            e.getMessage();
            System.out.println("Can't write verification file.");
        }
    }

    /**
     * Write the report file after initialization
     * @param reportFile
     * @param infos
     */
    public static void writeInitializationReportFile(File reportFile, ReportInfo infos) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(reportFile));
            writer.write(infos.toString());
            writer.close();
        } catch(IOException e) {
            e.getMessage();
            System.out.println("Can't write report file.");
        }
    }

    /**
     * Write the report file after verification
     * @param reportFile
     * @param infos
     * @param nbWarnings
     * @param warnings
     */
    public static void writeVerificationReportFile(File reportFile, ReportInfo infos, Integer nbWarnings, String warnings) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(reportFile));
            //Since ReportInfo object write "initialization" in the toString, it's easier to just replace it with "verification"
            writer.write(infos.toString().replace("initialization", "verification"));
            writer.write("\nReport file: " + reportFile.getAbsolutePath());
            writer.write("\nNumber of warnings: " + nbWarnings + "\n");
            if (!warnings.equals("")) writer.write("\nWARNINGS:\n" + warnings);
            writer.close();
        } catch(IOException e) {
            e.getMessage();
            System.out.println("Can't write report file.");
        }
    }

    /**
     * Get the first line of the verification file to get the hash function
     * @param verifFile
     * @return the hash function
     */
    public static String getHashFunction(File verifFile) {
        BufferedReader br;
        String hashFunction = "";
        try {
            br = new BufferedReader(new FileReader(verifFile));
            hashFunction = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("The verification file no longer exist.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Can't read verification file.");
            System.exit(1);
        }
        return hashFunction;
    }

    /**
     * Read the verification file and create FileInfo objects
     * @param verifFile
     * @return the list of FileInfo
     */
    public static List<FileInfo> readVerifFile(File verifFile) {
        BufferedReader br;
        List<FileInfo> infos = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(verifFile));
            //Skip line with hash
            br.readLine();
            String line;
            //Parse line and add to list for each line
            while((line = br.readLine()) != null) {
                infos.add(FileInfo.convertStringToFileInfo(line));
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("The verification file no longer exist.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Can't write in verification file.");
            System.exit(1);
        }
        return infos;
    }

    /**
     * Explore the directory to a certain depth and create a list of FileInfo with data gathered on files
     * @param directory
     * @param mDigest
     * @param depth
     * @return
     */
    public static ReportInfo exploreDirectoryContent(File directory, MessageDigest mDigest, Integer depth) {
        BufferedReader br;
        //Files contained in the folder
        File[] children = directory.listFiles();
        List<FileInfo> infos = new ArrayList<>();
        Integer nbDirectory = 0;
        Integer nbFile = 0;
        for(File f : children) {
            String owner = null;
            String group = null;
            String rights = null;
            try {
                //Get file attributes
                PosixFileAttributes attr = Files.readAttributes(f.toPath(), PosixFileAttributes.class);
                owner = attr.owner().getName();
                group = attr.group().getName();
                rights = PosixFilePermissions.toString(attr.permissions());
            } catch (IOException e) {
                //do something
            } catch (UnsupportedOperationException e) {
                System.out.println("This program does not support Windows system at the moment, try it on Linux.");
                System.exit(1);
            }
            if (f.isFile()) {
                //Handle file
                nbFile++;
                try {
                    br = new BufferedReader(new FileReader(f));
                    String fileContent = "";
                    String line;
                    while((line=br.readLine()) != null) {
                        fileContent += line;
                    }
                    br.close();
                    mDigest.update(fileContent.getBytes());
                    byte[] encryptedContent = mDigest.digest();
                    StringBuffer sb = new StringBuffer();
                    //Convert array of byte to string
                    for (int i = 0; i < encryptedContent.length; i++) {
                        sb.append(Integer.toString((encryptedContent[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    //Here we can use the absolute path, since the monitored directory is created with canonical path
                    FileInfo fi = new FileInfo(f.getAbsolutePath(), f.length(), owner, group, rights, f.lastModified(), sb.toString(), "f");
                    infos.add(fi);
                } catch (FileNotFoundException e) {
                    continue;
                } catch(IOException e) {
                    continue;
                }
            } else {
                //Handle directory
                nbDirectory++;
                if (depth > 0) {
                    //Explore content of subfolder
                    ReportInfo subReport = exploreDirectoryContent(f, mDigest, depth--);
                    long directorySize = 0;
                    //Compute folder size
                    for (FileInfo e : subReport.getFilesInfo()) {
                        directorySize += e.getSize();
                    }
                    FileInfo fi = new FileInfo(f.getAbsolutePath(), directorySize, owner, group, rights, f.lastModified(), "", "d");
                    infos.add(fi);
                    nbDirectory += subReport.getNbParsedDirectories();
                    nbFile += subReport.getNbParsedFiles();
                    infos.addAll(subReport.getFilesInfo());
                } else {
                    FileInfo fi = new FileInfo(f.getAbsolutePath(), f.length(), owner, group, rights, f.lastModified(), "", "d");
                    infos.add(fi);
                }
            }
        }
        ReportInfo report = new ReportInfo(infos, directory.getAbsolutePath(), "", nbDirectory, nbFile, Integer.toUnsignedLong(0));
        return report;
    }

}
