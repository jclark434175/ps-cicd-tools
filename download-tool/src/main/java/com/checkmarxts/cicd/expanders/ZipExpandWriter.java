package com.checkmarxts.cicd.expanders;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import com.checkmarxts.cicd.utils.PathUtil;
import com.checkmarxts.cicd.utils.PermissionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;


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
            try(var zip = ZipFile.builder().setByteArray(src.readAllBytes()).get())
            {

                for (var entries = zip.getEntries(); entries.hasMoreElements(); )
                {
                    var entry = entries.nextElement();
                    
                    var mode = PermissionUtils.translateUnixMode(entry.getUnixMode());

                    var curpath = Path.of(getDest().toString(), entry.getName());

                    if (entry.isDirectory())
                        expandDir(curpath, mode);
                    else
                        expandFile(zip.getInputStream(entry), curpath, mode);
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
