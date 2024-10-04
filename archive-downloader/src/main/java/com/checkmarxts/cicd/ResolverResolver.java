package com.checkmarxts.cicd;
import java.nio.file.Path;
import org.apache.commons.lang3.SystemUtils;
import com.checkmarxts.cicd.expanders.TgzExpandWriter;
import com.checkmarxts.cicd.expanders.ZipExpandWriter;


class ResolverResolver extends BaseResolver {

    final private static String _windows_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-win64.zip";
    final private static String _libc_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-linux64.tar.gz";
    final private static String _libc_musl_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-musl64.tar.gz";
    final private static String _mac_url = "https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-macos64.tar.gz";

    public ResolverResolver(Path outdir)
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

            // MUSL or GLIBC?
            setDownloadURL(isMUSL() ? _libc_musl_url : _libc_url);
        }
        else
            throw new RuntimeException("The OS type could not be determined, SCA Resolver download can't be automatically sourced.");
    }
    
}
