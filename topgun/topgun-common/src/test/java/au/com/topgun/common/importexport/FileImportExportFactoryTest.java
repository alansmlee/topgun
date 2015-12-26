package au.com.topgun.common.importexport;

import java.io.File;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import au.com.topgun.common.importexport.FileImportExportFactory.FileExport;
import au.com.topgun.common.importexport.FileImportExportFactory.FileImport;

import com.rta.tirms.common.test.util.ProjectFolderTestUtil;
import com.rta.tirms.common.test.util.WorkFolderTestUtil;

public class FileImportExportFactoryTest {

    static final String TEST_DATA_FOLDER = "src//test//resources//testdata//F32";

    @Before
    public void setup() throws Exception {
    }

    @After
    public void tearDown() {
        WorkFolderTestUtil.cleanWorkFolder();
    }

    @Test
    public void test() throws Exception {

        System.out.println(ProjectFolderTestUtil.getProjectFolder());
        System.out.println(WorkFolderTestUtil.getWorkFolder());
        final File f = WorkFolderTestUtil.createWorkSubFolder("abc");
        System.out.println(f.getCanonicalPath());
        WorkFolderTestUtil.removeExistingFolder(f);
    }

    /**
     * ****************************************************************** Test -
     * FileImport
     * ******************************************************************
     */

    @Test
    public void testCreateFileImport() throws Exception {
        /**
         * Test set up
         */
        // Create the test work folder
        final File testWorkfolder = WorkFolderTestUtil.createWorkSubFolder("CreateFileImport");
        FileUtils.cleanDirectory(testWorkfolder);

        // The test file and path in eclipse project
        final String testFilename = "999000322618.TXT";

        // Copy from eclipse project to test work folder (to work there)
        final File srcDataFile = WorkFolderTestUtil.createFile(TEST_DATA_FOLDER, testFilename);
        FileUtils.copyFileToDirectory(srcDataFile, testWorkfolder);

        /**
         * The real test begins here
         */
        // Test various APIs
        final FileImport fi = FileImportExportFactory.createFileImport(testWorkfolder.getCanonicalPath(), testFilename,
                FileImportExportFactory.DEFAULT_LINE_SEPERATOR);
        final String dir = fi.getDirectory();
        Assert.assertEquals(testWorkfolder.getCanonicalPath(), dir);
        final String fname = fi.getFilename();
        Assert.assertEquals(testFilename, fname);
        final String fullpath = WorkFolderTestUtil.replaceBackSlashes(fi.getFullFilePath());
        final String expectedFullpath = WorkFolderTestUtil.replaceBackSlashes(testWorkfolder.getCanonicalPath() + "/"
                + testFilename);
        Assert.assertEquals(expectedFullpath, fullpath);

        // Test reading last line
        final String lastline = fi.getLastLine();
        Assert.assertEquals("T999000322618201510021120440000000004", lastline);

        // Test reading file content
        final LineIterator li = fi.getLineIterator();
        Assert.assertNotNull(li);
        final StringBuilder sb = new StringBuilder();
        while (li.hasNext()) {
            sb.append(li.nextLine());
            sb.append("\n"); // this is mimic the original file since
                             // LineIterator strips this end-of-line character.
        }

        fi.close();

        final String expectedFileContent = FileUtils.readFileToString(srcDataFile, "UTF-8");
        Assert.assertEquals(expectedFileContent, sb.toString());

    }

    /**
     * ****************************************************************** Test -
     * FileExport
     * ******************************************************************
     */

    @Test
    public void testCreateFileExport() throws Exception {
        /**
         * Test set up
         */
        // Create the test work folder
        final File testWorkfolder = WorkFolderTestUtil.createWorkSubFolder("CreateFileExport");
        FileUtils.cleanDirectory(testWorkfolder);

        // The test file and path in eclipse project
        final String testInputFilename = "999000322618.TXT";
        final String testExportFilename = testInputFilename + ".exported";
        final File srcDataFile = WorkFolderTestUtil.createFile(TEST_DATA_FOLDER, testInputFilename);

        /**
         * The real test begins here
         */
        // Test various APIs
        final FileExport fe = FileImportExportFactory.createFileExport(testWorkfolder.getCanonicalPath(),
                testExportFilename);
        final String dir = fe.getDirectory();
        Assert.assertEquals(testWorkfolder.getCanonicalPath(), dir);
        final String fname = fe.getFilename();
        Assert.assertEquals(testExportFilename, fname);
        final String fullpath = WorkFolderTestUtil.replaceBackSlashes(fe.getFullFilePath());
        final String expectedFullpath = WorkFolderTestUtil.replaceBackSlashes(testWorkfolder.getCanonicalPath() + "/"
                + testExportFilename);
        Assert.assertEquals(expectedFullpath, fullpath);

        // Test writing file content
        final Writer writer = fe.getWriter();
        final String expectedExportedFileContent = FileUtils.readFileToString(srcDataFile, "UTF-8");
        writer.append(expectedExportedFileContent);
        fe.close();
        final File exportedDataFile = WorkFolderTestUtil.createFile(dir, testExportFilename);
        final String exportedDataFileContent = FileUtils.readFileToString(exportedDataFile, "UTF-8");
        Assert.assertEquals(expectedExportedFileContent, exportedDataFileContent);

    }

    /**
     * ****************************************************************** Test -
     * FileImportExportFactory
     * ******************************************************************
     */

    @Test
    public void testStripLastBackslash() {

        final String noLastBackSlash = "test/noBackSlash/nope";
        String resultStr = FileImportExportFactory.stripLastBackslash(noLastBackSlash);
        Assert.assertEquals(noLastBackSlash, resultStr);

        final String oneLastBackSlash = "test/oneBackSlash/";
        resultStr = FileImportExportFactory.stripLastBackslash(oneLastBackSlash);
        Assert.assertEquals("test/oneBackSlash", resultStr);

        final String oneLastBackSlash2 = "test/oneBackSlash\\";
        resultStr = FileImportExportFactory.stripLastBackslash(oneLastBackSlash2);
        Assert.assertEquals("test/oneBackSlash", resultStr);

    }

    @Test
    public void testRenameFile() throws Exception {

        /**
         * Test set up
         */
        // Create the test work folder
        final File testWorkfolder = WorkFolderTestUtil.createWorkSubFolder("renameFile");
        FileUtils.cleanDirectory(testWorkfolder);

        // The test file and path in eclipse project
        final String testFilename = "999000322618.TXT";

        // Copy from eclipse project to test work folder (to work there)
        final File srcDataFile = WorkFolderTestUtil.createFile(TEST_DATA_FOLDER, testFilename);
        FileUtils.copyFileToDirectory(srcDataFile, testWorkfolder);

        /**
         * The real test begins here
         */
        Collection col = FileUtils.listFiles(testWorkfolder, new String[] { "TXT" }, false);
        Assert.assertEquals(1, col.size());
        File fileInCol = (File) col.iterator().next();
        Assert.assertEquals(testFilename, fileInCol.getName());

        final String newFilename = testFilename + ".new";
        FileImportExportFactory.renameFile(testWorkfolder.getCanonicalPath(), testFilename, newFilename);

        col = FileUtils.listFiles(testWorkfolder, new String[] { "new" }, false);
        Assert.assertEquals(1, col.size());
        fileInCol = (File) col.iterator().next();
        Assert.assertEquals(newFilename, fileInCol.getName());
    }
    
    @Test
    public void testLeftZeroPadding() throws Exception {
        final String str01 = "123";
        String result = FileImportExportFactory.padLeftZeros(str01, 0);
        Assert.assertEquals(str01, result);
        result = FileImportExportFactory.padLeftZeros(str01, 1);
        Assert.assertEquals(str01, result);
        result = FileImportExportFactory.padLeftZeros(str01, 3);
        Assert.assertEquals(str01, result);
        result = FileImportExportFactory.padLeftZeros(str01, 5);
        Assert.assertEquals("00"+str01, result);
    }

}
