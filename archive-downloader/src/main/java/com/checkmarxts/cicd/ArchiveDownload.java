package com.checkmarxts.cicd;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.checkmarxts.cicd.expanders.IExpandedWriter;
import com.checkmarxts.cicd.expanders.NoExpandWriter;
import com.checkmarxts.cicd.expanders.TgzExpandWriter;
import com.checkmarxts.cicd.expanders.ZipExpandWriter;
import com.checkmarxts.cicd.utils.PathUtil;


public class ArchiveDownload {


    private static Option _help = Option.builder("?").longOpt("help").hasArg(false).desc("Request this help output.").build();

    private static Option _filename = Option.builder("f").hasArg().desc("Output filename (ignored if expanding the downloaded archive)").
        longOpt("filename").build();
    
    private static Option _url = Option.builder("u").hasArg().desc("Url for the file to download").longOpt("url").build();

    private static Option _resolver = Option.builder().hasArg(false)
        .desc("Automatically download and expand the SCA Resolver archive appropriate for the current OS")
        .longOpt("resolver").build();

    private static Option _temp = Option.builder("t").hasArg().desc("Write output to a temporary directory using the provided path as a prefix").
        longOpt("temp").build();

    private static Option _outdir = Option.builder("o").hasArg().desc("Path to a directory where output will be written").
        longOpt("outdir").build();

    private static Option _unzip = Option.builder().hasArg(false).desc("Unzip the downloaded file in the destination directory").
        longOpt("unzip").build();

    private static Option _untgz = Option.builder().hasArg(false).desc("Expand the downloaded gzipped tar file in the destination directory").
        longOpt("untgz").build();

    private static Options _all_options;
    private static Options _help_options;
    


    static
    {
        _all_options = new Options();
        _help_options = new Options();

        _all_options.addOption(_help);
        _all_options.addOption(_filename);
        _help_options.addOption(_help);

        var dl_src_group = new OptionGroup();
        dl_src_group.setRequired(true);
        dl_src_group.addOption(_url);
        dl_src_group.addOption(_resolver);
        _all_options.addOptionGroup(dl_src_group);

        var output_path_group = new OptionGroup();
        output_path_group.setRequired(true);
        output_path_group.addOption(_temp);
        output_path_group.addOption(_outdir);
        _all_options.addOptionGroup(output_path_group);

        

        var expand_group = new OptionGroup();
        expand_group.addOption(_unzip);
        expand_group.addOption(_untgz);
        _all_options.addOptionGroup(expand_group);


    }

    private static void showHelp(Options opts)
    {
        var help = new HelpFormatter();
        help.printHelp("java -jar archive-downloader.jar", "Downloads an archive (or file) at the provided URL, optionally expanding the archive", 
            opts, "At exit, the path to the downloaded artifacts is emitted on stdout", true);
    }


    public static void main(String[] args) throws Exception {

        try
        {
            var help_cmd_line = new DefaultParser().parse(_help_options, args, true);

            if (help_cmd_line.hasOption(_help))
            {
                showHelp(_all_options);
                System.exit(0);
            }

            var cmd_line = new DefaultParser().parse(_all_options, args);


            Path outdir = null;

            if (cmd_line.hasOption(_outdir))
                outdir = PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue(_outdir)));
            else if (cmd_line.hasOption(_temp))
                outdir = Files.createTempDirectory(PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue(_temp))), null).toAbsolutePath();
            else
            {
                System.err.println("ERROR: Output directory unknown, please provide the correct options.");
                System.exit(-1);
            }


            URL url = null;

            final ResolverResolver rr = cmd_line.hasOption(_resolver) ? new ResolverResolver(outdir) : null;

            if (rr != null && cmd_line.hasOption(_filename))
                System.err.println("WARNING: Output filename is ignored when downloading SCA Resolver.");

            try
            {
                if (cmd_line.hasOption(_url))
                    url = new URL(cmd_line.getOptionValue(_url));
                else
                    url = rr.getDownloadURL();
            }
            catch (MalformedURLException ex)
            {
                System.err.println("ERROR: URL for file to download is invalid.");
                System.err.println(ex.getMessage());
                System.exit(-1);
            }

            String dest_filename = null;

            if (cmd_line.hasOption(_filename))
                dest_filename = cmd_line.getOptionValue(_filename);
            else
                dest_filename = Path.of(url.getFile()).getFileName().toString();

            Supplier<IExpandedWriter> outwriter_factory = null;

            Path stdout_path = null;

            if (rr != null)
            {
                stdout_path = rr.getOutputPath();
                outwriter_factory = () -> rr.getExpander();
            }
            else if (cmd_line.hasOption(_unzip))
            {
                stdout_path = outdir;
                outwriter_factory = () -> new ZipExpandWriter();
            }
            else if (cmd_line.hasOption(_untgz))
            {
                stdout_path = outdir;
                outwriter_factory = () -> new TgzExpandWriter();
            }
            else
            {
                stdout_path = Path.of(outdir.toString(), dest_filename);
                outwriter_factory = () -> new NoExpandWriter();
            }


            try(var dt = new DownloadTool(url.toString()))
            {
                try(var expander = outwriter_factory.get().getInstance(stdout_path) )
                {
                    dt.expandDownload(expander);
                }
            }

            System.out.println(stdout_path.toAbsolutePath().toString());
        }
        catch (ParseException pex)
        {

            System.err.println("ERROR: " + pex.getMessage());
            showHelp(_all_options);
            System.exit(-1);

        }
    }
}
