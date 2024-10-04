# Checkmarx PS CI/CD Tools

These are tools used for CI/CD integrations authored by Checkmarx Professional Services.  These
were made to perform some common functions in a way that is portable
across build environments.  The tools are implemented in Java and intended to run with a JRE version >= 17.

These are not intended for public use.

## archive-downloader

```
usage: java -jar archive-downloader.jar [-?] --cxonecli | --resolver | -u
       <arg> [-f <arg>] -o <arg> | -t <arg>    [--untgz | --unzip]
Downloads an archive (or file) at the provided URL, optionally expanding
the archive
 -?,--help             Request this help output.
    --cxonecli         Automatically download and expand the Checkmarx One
                       CLI archive appropriate for the current OS
 -f,--filename <arg>   Output filename (ignored if expanding the
                       downloaded archive)
 -o,--outdir <arg>     Path to a directory where output will be written
    --resolver         Automatically download and expand the SCA Resolver
                       archive appropriate for the current OS
 -t,--temp <arg>       Write output to a temporary directory using the
                       provided path as a prefix
 -u,--url <arg>        Url for the file to download
    --untgz            Expand the downloaded gzipped tar file in the
                       destination directory
    --unzip            Unzip the downloaded file in the destination
                       directory
At exit, the path to the downloaded artifacts is emitted on stdout
```

## cxflow-downloader

```
usage: java -jar cxflow-downloader.jar [-?] [-l | -v <arg>] [-o <arg> |
       -t] [-p <arg>] [-s]
Downloads the specified or latest version of CxFlow
 -?,--help            Request this help output.
 -l,--latest          Download the latest version (default).
 -o,--outdir <arg>    Path to a directory where the downloaded file will
                      be written.
 -p,--prefix <arg>    Path prefix for temporary download path.
 -s,--skip            Skip download if target JAR exists.
 -t,--temp            Create a temporary directory, write downloaded file
                      to the temporary directory.
 -v,--version <arg>   Download the version with this tag value.
At exit, the path to the downloaded jar is emitted on stdout
```
