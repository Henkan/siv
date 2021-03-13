package siv;

import java.util.List;
import java.util.ArrayList;

/**
 * This class is the representation of a report.
 */
public class ReportInfo {

    private List<FileInfo> filesInfo;
    private String pathMonitoredDirectory;
    private String pathVerificationFile;
    private Integer nbParsedDirectories;
    private Integer nbParsedFiles;
    private Long timelength;

    public ReportInfo(List<FileInfo> filesInfo, String pathMonitoredDirectory, String pathVerificationFile, Integer nbParsedDirectories, Integer nbParsedFiles, Long timelength) {
        this.filesInfo = new ArrayList<>(filesInfo);
        this.pathMonitoredDirectory = pathMonitoredDirectory;
        this.pathVerificationFile = pathVerificationFile;
        this.nbParsedDirectories = nbParsedDirectories;
        this.nbParsedFiles = nbParsedFiles;
        this.timelength = timelength;
    }

    /**
     * Write the report file directly
     * @return
     */
    @Override
    public String toString() {
        return "Monitored directory: " + pathMonitoredDirectory + "\n" +
                "Verification file: " + pathVerificationFile + "\n" +
                "Number of directories parsed: " + nbParsedDirectories + "\n" +
                "Number of files parsed: " + nbParsedFiles + "\n" +
                "Time to complete initialization (in ms): " + Double.valueOf(timelength/1000000.0);
    }

    public List<FileInfo> getFilesInfo() {
        return filesInfo;
    }

    public void setFilesInfo(List<FileInfo> filesInfo) {
        this.filesInfo = filesInfo;
    }

    public String getPathMonitoredDirectory() {
        return pathMonitoredDirectory;
    }

    public void setPathMonitoredDirectory(String pathMonitoredDirectory) {
        this.pathMonitoredDirectory = pathMonitoredDirectory;
    }

    public String getPathVerificationFile() {
        return pathVerificationFile;
    }

    public void setPathVerificationFile(String pathVerificationFile) {
        this.pathVerificationFile = pathVerificationFile;
    }

    public Integer getNbParsedDirectories() {
        return nbParsedDirectories;
    }

    public void setNbParsedDirectories(Integer nbParsedDirectories) {
        this.nbParsedDirectories = nbParsedDirectories;
    }

    public Integer getNbParsedFiles() {
        return nbParsedFiles;
    }

    public void setNbParsedFiles(Integer nbParsedFiles) {
        this.nbParsedFiles = nbParsedFiles;
    }

    public Long getTimelength() {
        return timelength;
    }

    public void setTimelength(Long timelength) {
        this.timelength = timelength;
    }

}
