REM Grrr - to get around the rotten problem of http://ant.apache.org/faq.html#delegating-classloader-1.6
REM How about writing the taskdef so that it doesn't refer to junit?
set CLASSPATH=./test/lib/junit.jar
%ANT_HOME%\bin\ant %1 %2 %3 %4 %5 %6 %7