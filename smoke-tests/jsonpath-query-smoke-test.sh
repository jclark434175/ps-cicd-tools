#!/bin/bash
set -e
. $(dirname $0)/common

RESULT_FILE=$(dirname $0)/cx_result.json

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

testStringResult()
{
    RES=$(java -jar jsonpath-query/build/libs/jsonpath-query.jar -f $RESULT_FILE -q "$.ScanID")
    assertTrue 0 "[ "$RES" == "6e508aef-f707-497f-8fd3-2bd9dde6c7d8" ]"
}

testStringResultQuoted()
{
    RES=$(java -jar jsonpath-query/build/libs/jsonpath-query.jar -f $RESULT_FILE -q "$.ScanID" -d)
    assertTrue 0 "[ "$RES" == "6e508aef-f707-497f-8fd3-2bd9dde6c7d8" ]"
}

testArrayResult()
{
    test $(java -jar jsonpath-query/build/libs/jsonpath-query.jar -f $RESULT_FILE -q "$.EnginesEnabled" | wc -l)  == "4"
    assertTrue 0 $?
}

testArrayResultQuoted()
{
    test $(java -jar jsonpath-query/build/libs/jsonpath-query.jar -f $RESULT_FILE -q "$.EnginesEnabled" -d | grep "^\"" | wc -l)  == "4"
    assertTrue 0 $?
}

testFailOnUnsupportedType()
{
    java -jar jsonpath-query/build/libs/jsonpath-query.jar -f $RESULT_FILE -q "$.APISecurity" > /dev/null 2>&1
    assertFalse 0 $?
}


. $OUTROOT/shunit/shunit2