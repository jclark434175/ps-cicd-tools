package com.checkmarxts.cicd.expanders;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import java.util.Set;
import java.nio.file.Path;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.io.FileOutputStream;
import com.checkmarxts.cicd.utils.PathUtil;
import org.apache.commons.io.IOUtils;


public abstract class BaseExpandWriter implements IExpandedWriter {

    private Path _dest = null;
    
    protected void init(Path p) throws ExpandException
    {
        try
        {
            _dest = p;
            PathUtil.createAndValidateDir(_dest);
        }
        catch(Exception ex)
        {
            throw new ExpandException(ex);
        }
    }

    protected Path getDest ()
    {
        return _dest;
    }


    protected void expandDir(Path destpath, Set<PosixFilePermission> mode) throws Exception
    {
        PathUtil.createAndValidateDir(destpath, mode);
    }

    protected void expandFile(InputStream src, Path destpath, Set<PosixFilePermission> mode) throws Exception
    {
        Files.deleteIfExists(destpath);
        try(var destfile = new FileOutputStream(destpath.toString()) )
        {
            IOUtils.copy(src, destfile);
        }

        Files.setPosixFilePermissions(destpath, mode);
    }
    
}
