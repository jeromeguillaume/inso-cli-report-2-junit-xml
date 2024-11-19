#!/bin/bash

rm -f results/InsoCliJunit.log

#----------------------------------
# TEST 01
#----------------------------------
TEST="Successfull_conversion_w/o_failed"
./_tool/toolTest.sh 01 $TEST false
RC01=$?

#----------------------------------
# TEST 02
#----------------------------------
TEST="Successfull_conversion_w_failed"
./_tool/toolTest.sh 02 $TEST false
RC02=$?

#----------------------------------
# TEST 03
#----------------------------------
TEST="inso_cli_empty_file"
./_tool/toolTest.sh 03 $TEST false
RC03=$?

#----------------------------------
# TEST 04
#----------------------------------
TEST="inso_invalid_no_Running_request_for_[network]_Response_failed"
./_tool/toolTest.sh 04 $TEST true
RC04=$?

#----------------------------------
# TEST 05
#----------------------------------
TEST="inso_invalid_[network]_Response_failed_without_err"
./_tool/toolTest.sh 05 $TEST true
RC05=$?

#----------------------------------
# TEST 06
#----------------------------------
TEST="inso_invalid_Test_results_without_Running_request"
./_tool/toolTest.sh 06 $TEST true
RC06=$?

#----------------------------------
# TEST 07
#----------------------------------
TEST="inso_invalid_Test_results_type"
./_tool/toolTest.sh 07 $TEST true
RC07=$?
