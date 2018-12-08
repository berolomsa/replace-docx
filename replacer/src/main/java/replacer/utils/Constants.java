package replacer.utils;

public final class Constants {
    public static final String REPLACE_ENDPOINT = "/replace";
    public static final String FILE_NAME_PATH_VARIABLE = "fileName:.+";
    public static final String FILES_PATH = "/files/";
    public static final String FILES_ENDPOINT = FILES_PATH + "**";
    public static final String UPLOAD_ENDPOINT = "/uploadFile";
    public static final String DELETE_PATH = "/deleteFile/";
    public static final String DELETE_ENDPOINT = DELETE_PATH + "**";
    public static final String GET_ALL_FILES_ENDPOINT = "/all";

    public static final String DIR = "/var/files/";
    public static final String DIR_TEMPLATES = "templates/";
    public static final String DIR_TEMPS = "temps/";
    public static final String EXTENTION = ".docx";
    public static final String SEPERATOR = "$";
}