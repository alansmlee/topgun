package com.rta.tirms.common.test.util;

import java.io.File;
import java.io.IOException;

public class ProjectFolderTestUtil {

    /**
     * Get working folder
     * 
     * @throws IOException
     *             on error
     */
    public static String getProjectFolder() throws IOException {
        return new File(".").getCanonicalPath();
    }

}
