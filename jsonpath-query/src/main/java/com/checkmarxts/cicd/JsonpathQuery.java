package com.checkmarxts.cicd;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import com.jayway.jsonpath.JsonPath;
import java.io.FileInputStream;


public class JsonpathQuery {

    private static Option _help = Option.builder("?").longOpt("help").hasArg(false).desc("Request this help output.").build();
    private static Option _file = Option.builder("f").hasArg(true).desc("Path to the json file.").longOpt("file").required().build();
    private static Option _query = Option.builder("q").hasArg(true).desc("The jsonpath query to execute.").longOpt("query").required().build();
    private static Option _delim = Option.builder("d").hasArg(false).desc("Surround individual results with quotes.").longOpt("delim").build();

    private static Options _all_options;
    private static Options _help_options;

    static
    {
        _all_options = new Options();
        _help_options = new Options();

        _all_options.addOption(_help);
        _all_options.addOption(_query);
        _all_options.addOption(_delim);
        _all_options.addOption(_file);

        _help_options.addOption(_help);
    }



    private static void showHelp(Options opts)
    {
        var help = new HelpFormatter();
        help.printHelp("java -jar jsonpath-query.jar", "Executes a jsonpath query against a json file emitting string or string-array results to stdout.", opts, 
            "At exit, the query results are emitted on stdout", true);
        System.exit(0);
    }

    private static void output(java.util.List list, boolean delim)
    {
        for (var l : list)
            output((String)l, delim);
    }

    private static void output(java.lang.String str, boolean delim)
    {
        char delim_char = '"';

        if (delim)
            System.out.println(String.format("%c%s%c", delim_char, str, delim_char));
        else
            System.out.println(str);
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


            var query = cmd_line.getOptionValue(_query);
            var file = cmd_line.getOptionValue(_file);
            var delim = cmd_line.hasOption(_delim);

            var jp = JsonPath.parse(new FileInputStream(file));
            var res = jp.read(query);

            if (res instanceof java.lang.String)
                output((java.lang.String)res, delim);
            else if (res instanceof java.util.List)
                output((java.util.List)res, delim);
            else
            {
                System.err.println("Query result is not supported.");
                System.exit(-1);
            }
    
        }
        catch(ParseException pex)
        {
            System.err.println("ERROR: " + pex.getMessage());
            System.exit(-1);
        }
    }
}
