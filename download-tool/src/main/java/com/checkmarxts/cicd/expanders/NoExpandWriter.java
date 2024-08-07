package com.checkmarxts.cicd.expanders;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;


public class NoExpandWriter implements IExpandedWriter {

    private FileOutputStream _fs = null;

    public NoExpandWriter()
    {
    }

    private NoExpandWriter(Path destfile) throws FileNotFoundException
    {
        _fs = new FileOutputStream(destfile.toString());
    }

    public IExpandedWriter getInstance(Path p) throws ExpandException
    {
        try
        {
            return new NoExpandWriter(p);
        }
        catch(Exception ex)
        {
            throw new ExpandException(ex);
        }
    }
    
    public void expand(InputStream src) throws ExpandException
    {
        try
        {
            IOUtils.copy(src, _fs);
        }
        catch (Exception ex)
        {
            throw new ExpandException(ex);
        }

    }

    public void close()
    {
        if (_fs != null)
        {
            try
            {
                _fs.flush();
                _fs.close();
            }
            catch (IOException iex)
            {
                System.err.println(iex.getMessage());
                System.exit(-1);
            }
        }

    }
    
}
