package com.checkmarxts.cicd.expanders;
import java.nio.file.Path;
import java.io.InputStream;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import com.checkmarxts.cicd.utils.PathUtil;


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
    
}
