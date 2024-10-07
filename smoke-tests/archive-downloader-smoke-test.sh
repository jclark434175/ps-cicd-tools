#!/bin/bash
set -e
. $(dirname $0)/common


tearDown()
{
    rm -rf $OUTROOT/tmp
}

oneTimeTearDown()
{
  rm -rf $OUTROOT
}

testCanary()
{
    assertTrue 0 "test 1 -eq 1"
}

testCxOneCLIDownload()
{
    D=$(java -jar archive-downloader/build/libs/archive-downloader.jar --cxonecli -o $OUTROOT/tmp)
    assertTrue 0 "test -f $D/cx"
}

testResolverDownload()
{
    D=$(java -jar archive-downloader/build/libs/archive-downloader.jar --resolver -o $OUTROOT/tmp)
    assertTrue 0 "test -f $D/ScaResolver"
}

testJarAsZipDownload()
{
    D=$(java -jar archive-downloader/build/libs/archive-downloader.jar -u https://github.com/checkmarx-ltd/cx-flow/releases/download/1.7.05/cx-flow-1.7.05.jar -unzip -t $OUTROOT/tmp)
    test "$(cat $D/META-INF/MANIFEST.MF | grep "Implementation-Title: cx-flow" | wc -l)" == "1"
    assertTrue 0 $?
}

testDownloadWithUntgz()
{
    D=$(java -jar archive-downloader/build/libs/archive-downloader.jar -u https://github.com/checkmarx-ltd/cx-flow/archive/refs/tags/1.7.05.tar.gz -untgz -t $OUTROOT/tmp)
    assertTrue 0 "[ -f \"$D/cx-flow-1.7.05/version.txt\" ]"
}

testDownloadTGZWithWrongDecompressFails()
{
    java -jar archive-downloader/build/libs/archive-downloader.jar -u https://github.com/checkmarx-ltd/cx-flow/archive/refs/tags/1.7.05.tar.gz -unzip -t $OUTROOT/tmp > /dev/null 2>&1
    assertFalse 0 $?
}

testDownloadZipWithWrongDecompressFails()
{
    java -jar archive-downloader/build/libs/archive-downloader.jar -u https://github.com/checkmarx-ltd/cx-flow/archive/refs/tags/1.7.05.zip -untgz -t $OUTROOT/tmp > /dev/null 2>&1
    assertFalse 0 $?
}

testDownloadWithUnzip()
{
    java -jar archive-downloader/build/libs/archive-downloader.jar -u https://github.com/checkmarx-ltd/cx-flow/archive/refs/tags/1.7.05.zip -unzip -o $OUTROOT/tmp > /dev/null 2>&1
    assertTrue 0 $?
}


. $OUTROOT/shunit/shunit2