package com.checkmarxts.cicd.expanders;

import java.nio.file.Path;
import java.io.InputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.checkmarxts.cicd.utils.PermissionUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;


public class TgzExpandWriter extends BaseExpandWriter {

    public TgzExpandWriter()
    {
    }

    public IExpandedWriter getInstance(Path p) throws ExpandException
    {
        var inst = new TgzExpandWriter();
        inst.init(p);

        return inst;
    }

    public void expand(InputStream src) throws ExpandException
    {
        try
        {
            try(var gz = new GzipCompressorInputStream(src, true))
            {
                try(var tar = new TarArchiveInputStream(gz) )
                {
                    
                    // https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-linux64.tar.gz
                    for (var entry = tar.getNextEntry() ; entry != null; entry = tar.getNextEntry() )
                    {
                        var curpath = Path.of(getDest().toString(), entry.getName());
                        var mode = PermissionUtils.translateUnixMode(entry.getMode());

                        if (entry.isDirectory())
                            expandDir(curpath, mode);
                        else
                            expandFile(tar, curpath, mode);
                    }
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
