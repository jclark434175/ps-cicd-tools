package com.checkmarxts.cicd;
import com.checkmarxts.cicd.github.CxFlowUrlResolverFactory;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import com.checkmarxts.cicd.github.CxFlowUrlResolver;
import com.checkmarxts.cicd.utils.PathUtil;

public class CxFlowDownload {

    private static Options getOptions()
    {

        Options opts = new Options();
        opts.addOption("?", "help", false, "Request this help output.");

        OptionGroup output_path_opts = new OptionGroup();

        output_path_opts.addOption(Option.builder("t").hasArg(false).desc("Create a temporary directory, write downloaded file to the temporary directory.").
            longOpt("temp").required().build());
        
        output_path_opts.addOption(Option.builder("o").hasArg().desc("Path to a directory where the downloaded file will be written.").
            longOpt("outdir").required().build());

        opts.addOptionGroup(output_path_opts);

        opts.addOption(Option.builder("p").hasArg().desc("Path prefix for temporary download path.").
            longOpt("prefix").required(false).build());

        opts.addOption(Option.builder("s").hasArg(false).desc("Skip download if target JAR exists.").
            longOpt("skip").required(false).build());

        OptionGroup version_opts = new OptionGroup();

        version_opts.addOption(Option.builder("l").hasArg(false).desc("Download the latest version (default).").
            longOpt("latest").required(false).build());
        
        version_opts.addOption(Option.builder("v").hasArg(true).desc("Download the version with this tag value.").
            longOpt("version").required(false).build());

        opts.addOptionGroup(version_opts);



        return opts;
    }

    private static String getDownloadedFilePath (Path outdir, CxFlowUrlResolver resolver, boolean skipIfExists) throws Exception
    {
        var destpath = Path.of(outdir.toString(), resolver.getFilename()).toAbsolutePath();

        if (!Files.exists(destpath) || (Files.exists(destpath) && !skipIfExists))
            try(var tool = new DownloadTool(resolver.getDownloadUrl()))
            {
                try (var dest = new FileOutputStream(destpath.toString()))
                {
                    tool.doDownload(dest);
                }
            }

        return destpath.toString();
    }


    public static void main(String[] args) throws Exception {

        try
        {
            var options = getOptions();
            var cmd_line = new DefaultParser().parse(options, args);

            if (cmd_line.getOptions().length == 0)
            {
                System.out.println("No options provided.  Run with '-?' to print option help.");
                System.exit(1);
            }

            if (cmd_line.hasOption("?"))
            {
                var help = new HelpFormatter();
                help.printHelp("java -jar cxflow-downloader.jar", "Downloads the specified or latest version of CxFlow", options, 
                "At exit, the path to the downloaded jar is emitted on stdout", true);
                System.exit(0);
            }

            Path outdir = null;

            if (cmd_line.hasOption("outdir"))
            {
                outdir = PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue("outdir")));
            }
            else if (cmd_line.hasOption("temp"))
            {
                if (!cmd_line.hasOption("prefix"))
                    outdir = Files.createTempDirectory("cx-flow").toAbsolutePath();
                else
                    outdir = Files.createTempDirectory(PathUtil.createAndValidateDir(Path.of(cmd_line.getOptionValue("prefix"))), "cx-flow").toAbsolutePath();
            }
            else
            {
                System.err.println("ERROR: Output directory unknown, please provide the correct options.");
                System.exit(-1);
            }

            CxFlowUrlResolver resolver = null;

            if (cmd_line.hasOption("version"))
                resolver = CxFlowUrlResolverFactory.forTag(cmd_line.getOptionValue("version"));
            else
                resolver = CxFlowUrlResolverFactory.forLatest();

            System.out.println(getDownloadedFilePath (outdir, resolver, cmd_line.hasOption("skip")) );
        }
        catch(ParseException pex)
        {
            System.err.println("ERROR: " + pex.getMessage());
            System.exit(-1);
        }
    }
}
