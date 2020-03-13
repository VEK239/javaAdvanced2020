SET home=C:\Users\lizzka239\IdeaProjects\javaAdvanced2020
SET package=ru.ifmo.rain.vlasova.implementor
SET packageDir=ru\ifmo\rain\vlasova\implementor
SET outDir=out\production\javaAdvanced2020
SET kgeorgiy=info\kgeorgiy\java\advanced\implementor
SET src=%home%\src
SET out=%home%\%outDir%
SET run=%home%\run\implementor

cd %home%
javadoc -d javadoc -link https://docs.oracle.com/en/java/javase/13/docs/api^
 -cp %src% -private -author --source-path %src% %src%\%packageDir%\ComparedMethod.java ^
 %src%\%packageDir%\Implementor.java %src%\%packageDir%\ImplementorCodeGenerator.java %src%\%kgeorgiy%\*.java

