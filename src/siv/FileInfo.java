package siv;

/**
 * This class is the representation of information gathered on a file or folder.
 */
public class FileInfo {

    private static final String SEPARATOR = "|";

    private String fullPath;
    private long size; //in bytes
    private String ownerUser;
    private String ownerGroup;
    private String accessRights; //in symbolic notation
    private long lastModifDate; //in ms since epoch
    private String digestedContent;
    private String type; //f for file, d for directory

    public FileInfo(String fullPath, long size, String ownerUser, String ownerGroup, String accessRights, long lastModifDate, String digestedContent, String type) {
        this.fullPath = fullPath;
        this.size = size;
        this.ownerUser = ownerUser;
        this.ownerGroup = ownerGroup;
        this.accessRights = accessRights;
        this.lastModifDate = lastModifDate;
        this.digestedContent = digestedContent;
        this.type = type;
    }

    public static FileInfo convertStringToFileInfo(String s) {
        //Pipe is set between [] because it's a special character
        //-1 is set to keep empty values (ie digested content for folders)
        String[] parts = s.split("[" + SEPARATOR + "]", -1);
        return new FileInfo(parts[0], Long.parseLong(parts[1]), parts[2], parts[3], parts[4], Long.parseLong(parts[5]), parts[6], parts[7]);
    }

    /**
     * Structure used in verification file
     * @return
     */
    @Override
    public String toString() {
        return fullPath + SEPARATOR + size + SEPARATOR + ownerUser + SEPARATOR + ownerGroup + SEPARATOR + accessRights + SEPARATOR + lastModifDate + SEPARATOR + digestedContent + SEPARATOR + type;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(String ownerUser) {
        this.ownerUser = ownerUser;
    }

    public String getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public long getLastModifDate() {
        return lastModifDate;
    }

    public void setLastModifDate(long lastModifDate) {
        this.lastModifDate = lastModifDate;
    }

    public String getDigestedContent() {
        return digestedContent;
    }

    public void setDigestedContent(String digestedContent) {
        this.digestedContent = digestedContent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
