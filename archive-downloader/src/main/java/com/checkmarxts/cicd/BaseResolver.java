package com.checkmarxts.cicd;
import java.net.URL;
import com.checkmarxts.cicd.expanders.IExpandedWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;


public abstract class BaseResolver {

    private String _selected_url;
    private IExpandedWriter _expander;
    private Path _outpath;

    public BaseResolver(Path outroot)
    {
        _outpath = outroot;
        resolve();
    }

    protected abstract void resolve();

    protected void setExpander(IExpandedWriter expander)
    {
        _expander = expander;
    }

    public IExpandedWriter getExpander()
    {
        return _expander;
    }

    public Path getOutputPath()
    {
        return _outpath;
    }

    protected void setDownloadURL(String url)
    {
        _selected_url = url;
    }

    public URL getDownloadURL() throws MalformedURLException
    {
        return new URL(_selected_url);
    }

    protected static boolean searchLddStream(InputStream ldd_output) throws Exception
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

    protected static boolean isMUSL()
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


}
