package com.checkmarxts.cicd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;

import com.checkmarxts.cicd.expanders.IExpandedWriter;
import com.checkmarxts.cicd.expanders.TgzExpandWriter;
import com.checkmarxts.cicd.expanders.ZipExpandWriter;

class ResolverResolver {

    final private static String _windows_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-win64.zip";
    final private static String _libc_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-linux64.tar.gz";
    final private static String _libc_musl_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-musl64.tar.gz";
    final private static String _mac_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-macos64.tar.gz";

    private String _selected_url;
    private IExpandedWriter _expander;
    private Path _outpath;


    public ResolverResolver(Path outroot)
    {
        _outpath = outroot;

        if (SystemUtils.IS_OS_MAC)
        {
            _expander = new TgzExpandWriter();
            _selected_url = _mac_url;
        }
        else if (SystemUtils.IS_OS_WINDOWS)
        {
            _expander = new ZipExpandWriter();
            _selected_url = _windows_url;
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            _expander = new TgzExpandWriter();

            // MUSL or GLIBC?
            _selected_url = isMUSL() ? _libc_musl_url : _libc_url;
        }
        else
            throw new RuntimeException("The OS type could not be determined, SCA Resolver download can't be automatically sourced.");
    }

    private boolean searchLddStream(InputStream ldd_output) throws Exception
    {
        final var musl_pattern = Pattern.compile("musl", Pattern.CASE_INSENSITIVE);

        try(var reader = new BufferedReader (new InputStreamReader(ldd_output)))
        {
            while (reader.ready())
                if (musl_pattern.matcher(reader.readLine()).find())
                    return true;
        }

        return false;
    }

    private boolean isMUSL()
    {
        try
        {
            var exec = Runtime.getRuntime().exec("ldd --version");
            exec.waitFor();
            return searchLddStream(exec.getInputStream()) || searchLddStream(exec.getErrorStream());
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error probing for the libc type.", ex);
        }
    }

    public URL getDownloadURL() throws MalformedURLException
    {
        return new URL(_selected_url);
    }

    public IExpandedWriter getExpander()
    {
        return _expander;
    }

    public Path getOutputPath()
    {
        return _outpath;
    }
    
}
