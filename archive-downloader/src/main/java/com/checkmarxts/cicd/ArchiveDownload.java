package com.checkmarxts.cicd;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.function.Supplier;
import java.net.URL;
import java.net.MalformedURLException;
import com.checkmarxts.cicd.expanders.ZipExpandWriter;
import com.checkmarxts.cicd.expanders.TgzExpandWriter;
import com.checkmarxts.cicd.expanders.NoExpandWriter;
import com.checkmarxts.cicd.expanders.IExpandedWriter;
import com.checkmarxts.cicd.utils.PathUtil;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;


public class ArchiveDownload {


    private static Options getOptions()
    {
        Options opts = new Options();

        opts.addOption(Option.builder("?").longOpt("help").required(false).hasArg(false)
            .desc("Request this help output.").build());

        opts.addOption(Option.builder("p").hasArg().desc("Path prefix to use when creating a temporary directory.").
            longOpt("prefix").required(false).build());
        
        opts.addOption(Option.builder("u").hasArg().desc("Url for the file to download.").longOpt("url").required().build());
        
        opts.addOption(Option.builder("f").hasArg().desc("Output filename (ignored if expanding the downloaded archive).").
            longOpt("filename").required(false).build());

            
        OptionGroup output_path_opts = new OptionGroup();

        output_path_opts.addOption(Option.builder("t").hasArg(false).desc("Write output to a temporary directory.").
            longOpt("temp").required().build());
        
        output_path_opts.addOption(Option.builder("o").hasArg().desc("Path to a directory where output will be written.").
            longOpt("outdir").required().build());

        opts.addOptionGroup(output_path_opts);


        OptionGroup expand_opts = new OptionGroup();

        expand_opts.addOption(Option.builder().hasArg(false).desc("Unzip the downloaded file in the destination directory.").
            longOpt("unzip").required(false).build());

        expand_opts.addOption(Option.builder().hasArg(false).desc("Expand the downloaded gzipped tar file in the destination directory.").
            longOpt("untgz").required(false).build());

        opts.addOptionGroup(expand_opts);

        return opts;
    }

    private static void showHelp(Options opts)
    {
        var help = new HelpFormatter();
        help.printHelp("java -jar archive-downloader.jar", "Downloads an archive (or file) at the provided URL, optionally expanding the archive.", 
            opts, "At exit, the path to the downloaded artifacts is emitted on stdout.", true);
    }


    public static void main(String[] args) throws Exception {
        var options = getOptions();

        try
        {
            var cmd_line = new DefaultParser().parse(options, args);

            if (cmd_line.hasOption("?"))
            {
                showHelp(options);
                System.exit(0);
            }

            URL url = null;

            try
            {
                url = new URL(cmd_line.getOptionValue("url"));
            }
            catch (MalformedURLException ex)
            {
                System.err.println("ERROR: URL for file to download is invalid.");
                System.err.println(ex.getMessage());
                System.exit(-1);
            }

            String dest_filename = null;

            if (cmd_line.hasOption("filename"))
                dest_filename = cmd_line.getOptionValue("filename");
            else
                dest_filename = Path.of(url.getFile()).getFileName().toString();

            Path outdir = null;

            if (cmd_line.hasOption("outdir"))
                outdir = PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue("outdir")));
            else if (cmd_line.hasOption("temp"))
                outdir = Files.createTempDirectory(PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue("prefix"))), null).toAbsolutePath();
            else
            {
                System.err.println("ERROR: Output directory unknown, please provide the correct options.");
                System.exit(-1);
            }

            Supplier<IExpandedWriter> outwriter_factory = null;

            Path stdout_path = null;

            if (cmd_line.hasOption("unzip"))
            {
                stdout_path = outdir;
                outwriter_factory = () -> new ZipExpandWriter();
            }
            else if (cmd_line.hasOption("untgz"))
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
            showHelp(options);
            System.exit(-1);

        }
    }
}
