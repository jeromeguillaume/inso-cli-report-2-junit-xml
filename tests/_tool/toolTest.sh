#!/bin/bash
DEBUG="BEGIN "$1-$2
echo "$DEBUG"
echo "$DEBUG" >> results/InsoCliJunit.log
JAVA_OUTPUT=results/javaOutput.log
rm -f $JAVA_OUTPUT

java -jar ../bin/InsoCliJunit.jar --input $1-inso-cli.log --output results/$1-inso.junit.xml >> $JAVA_OUTPUT 2>&1
RC=$?
# If there is a JAVA Exception
if [ -s $JAVA_OUTPUT ]; then
  cat $JAVA_OUTPUT >> results/InsoCliJunit.log
  # Compare first line only (and avoid the Java Stack trace with number of lines that can changed)
  diff <(head -n 1 $1-inso.junit-expected.xml) <(head -n 1 $JAVA_OUTPUT)
else
  # Compare all the content
  diff results/$1-inso.junit.xml $1-inso.junit-expected.xml
fi

RC=$?

if [[ $RC == 0 ]];
then
DEBUG="END   "$1-$2" => Result: "✅
else
DEBUG="END   "$1-$2" => Result: "❌
fi
echo "$DEBUG"
echo "$DEBUG" >> results/InsoCliJunit.log

rm -f $JAVA_OUTPUT

exit $RC