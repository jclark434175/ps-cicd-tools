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

testDownloadLatest()
{
    D=$(java -jar cxflow-downloader/build/libs/cxflow-downloader.jar -l -o $OUTROOT/tmp)
    test "$(find $OUTROOT -type f -name 'cx-flow*.jar' | wc -l)" == "1"
    assertTrue 0 $?
}


. $OUTROOT/shunit/shunit2