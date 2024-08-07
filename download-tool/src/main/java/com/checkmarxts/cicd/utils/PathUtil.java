 package com.checkmarxts.cicd.utils;

 import java.io.IOException;
 import java.nio.file.InvalidPathException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
public class PathUtil {

    public static Path createAndValidateDir(Path dirpath) throws InvalidPathException, IOException
    {
        Path retval = null;

        if (Files.exists(dirpath) && !Files.isWritable(dirpath))
            throw new InvalidPathException(dirpath.toString(), "Directory " + dirpath + " is not writable.");
        else if (!Files.exists(dirpath))
        {
            retval = Files.createDirectories(dirpath).toAbsolutePath();

            if (!Files.exists(retval))
                throw new InvalidPathException(dirpath.toString(), "Unable to create directory " + dirpath);
        }
        else
            retval = dirpath;

        return retval;
    }
    
}
