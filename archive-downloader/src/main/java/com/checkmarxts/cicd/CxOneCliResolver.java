package com.checkmarxts.cicd;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

import com.checkmarxts.cicd.expanders.TgzExpandWriter;
import com.checkmarxts.cicd.expanders.ZipExpandWriter;


public class CxOneCliResolver extends BaseResolver {

    final private static String _windows_url = "https://github.com/Checkmarx/ast-cli/releases/latest/download/ast-cli_windows_x64.zip";
    final private static String _linux_url = "https://github.com/Checkmarx/ast-cli/releases/latest/download/ast-cli_linux_x64.tar.gz";
    final private static String _arm64_url = "https://github.com/Checkmarx/ast-cli/releases/latest/download/ast-cli_linux_arm64.tar.gz";
    final private static String _arm32_url = "https://github.com/Checkmarx/ast-cli/releases/latest/download/ast-cli_linux_armv6.tar.gz";
    final private static String _mac_url = "https://github.com/Checkmarx/ast-cli/releases/latest/download/ast-cli_darwin_x64.tar.gz";

    public CxOneCliResolver(Path outdir)
    {
        super(outdir);
    }


    @Override
    protected void resolve() {
        if (SystemUtils.IS_OS_MAC)
        {
            setExpander(new TgzExpandWriter());
            setDownloadURL(_mac_url);
        }
        else if (SystemUtils.IS_OS_WINDOWS)
        {
            setExpander(new ZipExpandWriter());
            setDownloadURL(_windows_url);
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            setExpander(new TgzExpandWriter());

            var arch = System.getProperty("os.arch");

            if (arch.startsWith("arm"))
                setDownloadURL(_arm32_url);
            else if (arch.startsWith("aarch64"))
                setDownloadURL(_arm64_url);
            else
                setDownloadURL(_linux_url);
        }
        else
            throw new RuntimeException("The OS type could not be determined, CxOne CLI download can't be automatically sourced.");
        
    }
}
