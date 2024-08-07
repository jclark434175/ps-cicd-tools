package com.checkmarxts.cicd;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.AutoCloseable;
import java.nio.file.Paths;
import java.nio.file.Path;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ProtocolException;
import com.checkmarxts.cicd.expanders.IExpandedWriter;
import com.checkmarxts.cicd.expanders.ExpandException;

public class DownloadTool implements AutoCloseable
{

    private final URL _download_url;
    private final HttpHost _download_host;
    private final HttpGet _download_get;
    private final CloseableHttpClient _client;
    private final ClassicHttpResponse _response;


    public DownloadTool(String url) throws MalformedURLException, URISyntaxException, IOException, HttpException
    {
        _download_url = new URL(url);
        _download_host = new HttpHost(_download_url.getProtocol(), _download_url.getHost());
        _download_get = new HttpGet(_download_url.toURI());
        _client = HttpClients.createDefault();
        _response = _client.executeOpen(_download_host, _download_get, null);

        if (_response.getCode() != 200)
            throw new HttpException(String.format("Request for [%s] failed: %d (%s)", url, _response.getCode(), _response.getReasonPhrase()));
    }

    public void doDownload(OutputStream write_dest) throws IOException
    {
        _response.getEntity().writeTo(write_dest);
    }

    public void expandDownload(IExpandedWriter expander) throws IOException, ExpandException
    {
        expander.expand(_response.getEntity().getContent());
    }

    public void close() throws IOException
    {
        if (_response != null)
        {
            EntityUtils.consume(_response.getEntity());
            _response.close();
        }

        if (_client != null)
            _client.close();
    }

    
}
