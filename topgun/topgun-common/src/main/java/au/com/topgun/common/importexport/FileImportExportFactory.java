package au.com.topgun.common.importexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class FileImportExportFactory {

    final static String DEFAULT_LINE_SEPERATOR = "\n";

    public static FileImport createFileImport(String directory, String filename, String lineSeperator) throws IOException {
        return new FileImport(directory, filename, lineSeperator);
    }

    public static FileExport createFileExport(String directory, String filename) throws IOException {
        return new FileExport(directory, filename);
    }

    public static String stripLastBackslash(String directoryStr) {
        if (directoryStr.endsWith("\\") || directoryStr.endsWith("/")) {
            return directoryStr.substring(0, directoryStr.length() - 1);
        }
        return directoryStr;
    }
    
    public static String padLeftZeros(String str, int n) {
        if (n <= 0) {
            n = 1;
        }
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }

    public static void renameFile(String folder, String oldFilename, String newFilename) throws IOException {
        final File oldFile = new File(folder, oldFilename);
        final File newFile = new File(folder, newFilename);
        if (!oldFile.renameTo(newFile)) {
            throw new IOException(String.format("Unable to rename file[%s] to [%s] in folder[%s]", oldFilename,
                    newFilename, folder));
        }
    }

    /**
     * ********************************************** Inner classes
     * **********************************************
     */

    /**
	 * 
	 */
    public static class FileImport {

        public final static String ENCODING = "UTF-8";
        public final static String PREPARING_MODIFIED_FILE_EXTENSION = ".parsing";
        public final static String FINAL_MODIFIED_FILE_EXTENSION = ".parsed";

        private final String directory;
        private final String filename;
        private final File inFile;
        private LineIterator it;
        private final String LINE_SEPERATOR;

        public FileImport(String directory, String filename, String lineSeperator) {
            this.directory = stripLastBackslash(directory);
            this.filename = filename;
            this.LINE_SEPERATOR = lineSeperator;
            /**
             * Prepare to read file
             */
            inFile = new File(getFullFilePath());
        }

        public String getDirectory() {
            return directory;
        }

        public String getFilename() {
            return filename;
        }
        
        public String getLineSeparator() {
            return this.LINE_SEPERATOR;
        }

        public String getFullFilePath() {
            return directory + "/" + filename;
        }

        public LineIterator getLineIterator() throws IOException {
            if (it == null) {
                /**
                 * Ensure efficiently reads large file line by line. Low memory foot
                 * prints
                 */
                it = FileUtils.lineIterator(inFile, ENCODING);
            }
            return it;
        }

        public String getLastLine() throws IOException {
            return getLastLineFast(inFile);
        }
        
        public void close() {
            if (it != null) {
                LineIterator.closeQuietly(it);
            }
        }
        
        /**
         * TODO: Best to use ReversedLinesFileReader.class from Apache 2.5
         *       (pity we are using commons-io 1.2 which does not have class)
         *       Good to replace the following implementation when we upgrade to commons-io 2.5
         *       It will be much simpler!!!
         * 
         * Contribution:
         * http://nunobrito1981.blogspot.com/2014/11/java-reading-last-line-on-large-text.html
         * 
         * Returns the last line from a given text file. This method is particularly
         * well suited for very large text files that contain millions of text lines
         * since it will just seek the end of the text file and seek the last line
         * indicator. Please use only for large sized text files.
         * 
         * @param file A file on disk
         * @return The last line or an empty string if nothing was found
         * 
         * @author Nuno Brito
         * @author Michael Schierl
         * @throws IOException 
         * @license MIT
         * @date 2014-11-01
         */
        private static String getLastLineFast(final File file) throws IOException {
            // file needs to exist
            if (file.exists() == false || file.isDirectory()) {
                    return "";
            }

            // avoid empty files
            if (file.length() <= 2) {
                    return "";
            }

            // open the file for read-only mode
            RandomAccessFile fileAccess = null;
            try {
                fileAccess = new RandomAccessFile(file, "r");
                final char breakLine = '\n';
                // offset of the current filesystem block - start with the last one
                long blockStart = (file.length() - 1) / 4096 * 4096;
                // hold the current block
                byte[] currentBlock = new byte[(int) (file.length() - blockStart)];
                // later (previously read) blocks
                final List<byte[]> laterBlocks = new ArrayList<byte[]>();
                while (blockStart >= 0) {
                    fileAccess.seek(blockStart);
                    fileAccess.readFully(currentBlock);
                    // ignore the last 2 bytes of the block if it is the first one
                    int lengthToScan = currentBlock.length - (laterBlocks.isEmpty() ? 2 : 0);
                    for (int i = lengthToScan - 1; i >= 0; i--) {
                        if (currentBlock[i] == breakLine) {
                            // we found our end of line!
                            StringBuilder result = new StringBuilder();
                            // RandomAccessFile#readLine uses ISO-8859-1, therefore
                            // we do here too
                            result.append(new String(currentBlock, i + 1, currentBlock.length - (i + 1), ENCODING));
                            for (byte[] laterBlock : laterBlocks) {
                                    result.append(new String(laterBlock, ENCODING));
                            }
                            // maybe we had a newline at end of file? Strip it.
                            if (result.charAt(result.length() - 1) == breakLine) {
                                    // newline can be \r\n or \n, so check which one to strip
                                    int newlineLength = result.charAt(result.length() - 2) == '\r' ? 2 : 1;
                                    result.setLength(result.length() - newlineLength);
                            }
                            return result.toString();
                        }
                    }
                    // no end of line found - we need to read more
                    laterBlocks.add(0, currentBlock);
                    blockStart -= 4096;
                    currentBlock = new byte[4096];
                }
            } catch (IOException ex) {
                    throw ex;
            } finally {
                if (fileAccess != null) {
                    fileAccess.close();
                }
            }
            // oops, no line break found or some exception happened
            return "";
        }
        
    } // end class {@link #FileImport}

    /**
	 * 
	 */
    public static class FileExport {

        final static String ENCODING = "UTF-8";
        public final static String PREPARING_FILE_EXTENSION = ".preparing";
        public final static String READY_TO_USE_FILE_EXTENSION = ".ready";

        private final String directory;
        private final String filename;
        private final File outFile;
        private FileWriter fwriter;
        private Writer bfwriter;

        public FileExport(String directory, String filename) throws IOException {
            this.directory = stripLastBackslash(directory);
            this.filename = filename;
            this.outFile = new File(getFullFilePath());
        }

        public String getDirectory() {
            return directory;
        }

        public String getFilename() {
            return filename;
        }

        public String getFullFilePath() {
            return directory + "/" + filename;
        }

        public Writer getWriter() throws IOException {
            if (bfwriter == null) {
                /**
                 * Prepare to write file
                 */
                fwriter = new FileWriter(outFile);
                bfwriter = new BufferedWriter(fwriter); // More efficient write
            }
            return bfwriter;
        }

        public void close() {
            if (bfwriter != null) {
                try {
                    bfwriter.flush();
                    bfwriter.close();
                } catch (Exception e) {
                    // die quietly
                }
            }
            if (fwriter != null) {
                try {
                    fwriter.flush();
                    fwriter.close(); // to be sure
                } catch (Exception e) {
                    // die quietly
                }
            }
        }
    } // end class {@link #FileExport}
}
