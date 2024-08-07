package com.checkmarxts.cicd.expanders;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import com.checkmarxts.cicd.utils.PathUtil;
import org.apache.commons.io.IOUtils;

public class ZipExpandWriter extends BaseExpandWriter {

    public ZipExpandWriter()
    {
    }

    public IExpandedWriter getInstance(Path p) throws ExpandException
    {
        var inst = new ZipExpandWriter();
        inst.init(p);

        return inst;
    }

    
    public void expand(InputStream src) throws ExpandException
    {
        try
        {
            try(var zip = new ZipInputStream(src))
            {

                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                {
                    var curpath = Path.of(getDest().toString(), entry.getName());

                    if (entry.isDirectory())
                    {
                        try 
                        {
                            PathUtil.createAndValidateDir(curpath);
                        }
                        catch(Exception ex)
                        {
                            throw new ExpandException(ex, entry.getName());
                        }
                    }
                    else
                    {
                        try(var destfile = new FileOutputStream(curpath.toString()) )
                        {
                            IOUtils.copy(zip, destfile);
                        }
                    }
                    zip.closeEntry();
                }

            }
        }
        catch (Exception ex)
        {
            throw new ExpandException(ex);
        }
    }

    public void close()
    {

    }
   
}
