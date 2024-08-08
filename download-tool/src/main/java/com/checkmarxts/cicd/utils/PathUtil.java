package com.checkmarxts.cicd.utils;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class PathUtil {
    public static Path createAndValidateDir(Path dirpath) throws InvalidPathException, IOException
    {
        return createAndValidateDir(dirpath, null);
    }

    public static Path createAndValidateDir(Path dirpath, Set<PosixFilePermission> perm) throws InvalidPathException, IOException
    {
        Path retval = null;

        if (Files.exists(dirpath) && !Files.isWritable(dirpath))
            throw new InvalidPathException(dirpath.toString(), "Directory " + dirpath + " is not writable.");
        else if (!Files.exists(dirpath))
        {
            if (perm != null)
                retval = Files.createDirectories(dirpath, PosixFilePermissions.asFileAttribute(perm) ).toAbsolutePath();
            else
                // Follow OS umask or default permissions.
                retval = Files.createDirectories(dirpath).toAbsolutePath();

            if (!Files.exists(retval))
                throw new InvalidPathException(dirpath.toString(), "Unable to create directory " + dirpath);
        }
        else
            retval = dirpath;

        return retval;
    }
    
}
