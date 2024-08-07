package com.checkmarxts.cicd.github;

import java.lang.Exception;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

public class CxFlowUrlResolver {

    private final HttpHost _target_host = new HttpHost("https", "api.github.com");
    private final Pattern _jar_regex = Pattern.compile("(?<name>cx-flow)-.+?(?<j11ext>-java11)?.jar");
    
    private HttpGet _request_path;
    private URL _download_url;

    public CxFlowUrlResolver(String request_path)
    {
        _request_path = new HttpGet(request_path);
    }

    private String responseHandler(ClassicHttpResponse resp) throws HttpException, IOException
    {
        if (resp.getCode() != 200)
            throw new HttpException("Error accessing GitHub release API: %d (%s)", resp.getCode(), resp.getReasonPhrase());

        var resp_json = new JSONObject(new JSONTokener(resp.getEntity().getContent()));

        if (resp_json.isEmpty())
            throw new HttpException("GitHub release API returned no data.");

        try
        {
            var release_assets = resp_json.getJSONArray("assets");

            ArrayList<String> candidates = new ArrayList<String>();

            for(Object asset_obj : release_assets)
            {
                JSONObject asset = (JSONObject)asset_obj;

                if (asset.getString("content_type").compareTo("application/java-archive") != 0)
                    continue;

                var matcher = _jar_regex.matcher(asset.getString("browser_download_url"));

                if (matcher.find() && matcher.group("name") != null)
                {
                    if (matcher.group("j11ext") != null)
                        return asset.getString("browser_download_url");
                    
                    candidates.add(asset.getString("browser_download_url"));

                }

            }

            if (candidates.size() == 1)
                return candidates.get(0);
            else
                throw new GitHubException("Multiple JARs published, can't resolve the correct JAR.");

        }
        catch (JSONException ex)
        {
            throw new HttpException("Malformed JSON response.");

        }
    }

    private void populate() throws Exception
    {
        if (_download_url == null)
        {
            try (final CloseableHttpClient httpclient = HttpClients.createDefault())
            {
                _download_url = new URL(httpclient.execute(_target_host, _request_path, null, this::responseHandler));
            }
        }
    }

    public String getFilename() throws Exception
    {
        populate();
        return Paths.get(_download_url.getFile()).getFileName().toString();
    }
    

    public String getDownloadUrl() throws Exception
    {
        populate();
        return _download_url.toString();
    }

}
