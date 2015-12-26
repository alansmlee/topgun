package com.rta.tirms.common.test.util;

import java.io.File;
import java.io.IOException;

public class WorkFolderTestUtil {

    private static File workFolder = null;

    static {
        workFolder = new File("./generated/testing");
        if (!workFolder.exists()) {
            workFolder.mkdir();
        }
    }
    
    public static void cleanWorkFolder() {
        if (workFolder != null) {
            recursiveDelete(workFolder);
        }
    }
    
    /**
     * Get working folder
     * 
     * @throws IOException
     *             on error
     */
    public static String getWorkFolder() throws IOException {
        ensureValidWorkFolder();
        return workFolder.getCanonicalPath();
    }

    public static File createFile(String folder, String filename) {
        return new File(folder, filename);
    }

    /**
     * Create folder relative to working directory - DO NOT put a / in
     * front. e.g. createFolder("test/FileUtil");
     * 
     * @param relativePathToWorkingFolder
     * @throws IOException
     *             on error
     */
    public static File createWorkSubFolder(String relativePathToWorkingFolder) throws IOException {
        ensureValidWorkFolder();
        ensureSubFolderOfWorkingFolder(relativePathToWorkingFolder);
        final String fullpath = getWorkFolder() + "/" + relativePathToWorkingFolder;
        final File file = new File(fullpath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("Failed to created directory: " + file.getPath());
            }
        }
        return file;
    }

    /**
     * Remove the folder expecting this folder exists
     * 
     * @param file
     *            the folder
     * @throws IOException
     *             if folder does not exist or unable to be removed
     */
    public static void removeExistingFolder(File folder) throws IOException {
        ensureValidWorkFolder();
        ensureSubFolderOfWorkingFolder(folder);
        if (folder.list().length != 0) {
            // This check is necessary because File.delete() expects empty
            // directory
            throw new IOException(folder.getCanonicalPath() + ": unable to delete because it still has content");
        }
        if (!folder.delete()) {
            throw new IOException(folder.getCanonicalPath() + ": Failed to delete");
        }
    }

    public static void removeExistingSubFolderByforce(File folder) throws IOException {
        ensureValidWorkFolder();
        ensureSubFolderOfWorkingFolder(folder);
        recursiveDelete(folder);
    }

    public static void ensureValidWorkFolder() throws IOException {
        if (null == workFolder) {
            throw new IOException(
                    String.format("*** WORK FOLDER not exist - set it up first by calling TestFileUtil.setWorkFolder(folderFullPath)"));
        }
    }

    public static void ensureSubFolderOfWorkingFolder(String relativePathToWorkingFolder) throws IOException {
        ensureValidWorkFolder();
        final String fullpath = replaceBackSlashes(getWorkFolder() + "/" + relativePathToWorkingFolder);
        final String workingFolderPath = replaceBackSlashes(getWorkFolder());
        final int idx = fullpath.indexOf(workingFolderPath);
        if (idx != 0 || workingFolderPath.equals(fullpath)) {
            throw new IOException(String.format("NOT ALLOWED - %s: is not sub-folder of working folder[%s]",
                    fullpath, getWorkFolder()));
        }
    }

    /**
     * Ensure folder is sub-folder of working folder
     * 
     * @param folder
     * @throws IOException
     */
    public static void ensureSubFolderOfWorkingFolder(File folder) throws IOException {
        ensureValidWorkFolder();
        if (!folder.exists()) {
            throw new IOException(folder.getCanonicalPath() + ": does not exist");
        }
        if (!folder.isDirectory()) {
            throw new IOException(folder.getCanonicalPath() + ": is not a diretory");
        }
        final String workingFolderPath = replaceBackSlashes(getWorkFolder());
        final String folderPath = replaceBackSlashes(folder.getCanonicalPath());
        final int idx = folderPath.indexOf(workingFolderPath);
        if (idx != 0 || workingFolderPath.equals(folderPath)) {
            throw new IOException(String.format("NOT ALLOWED - %s: is not sub-folder of working folder[%s]",
                    folder.getCanonicalPath(), getWorkFolder()));
        }
    }

    private static void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

    public static String replaceBackSlashes(String str) {
        return str.replace('\\', '/');
    }

}
