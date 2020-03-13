SET home=C:\Users\lizzka239\IdeaProjects\javaAdvanced2020
SET package=ru.ifmo.rain.vlasova.implementor
SET packageDir=ru\ifmo\rain\vlasova\implementor
SET outDir=out\production\javaAdvanced2020
SET kgeorgiy=info\kgeorgiy\java\advanced\implementor
SET src=%home%\src
SET out=%home%\%outDir%
SET run=%home%\run\implementor

cd %home%
javac -cp %src% -d %out% %src%\%packageDir%\*.java %src%\%kgeorgiy%\*.java

cd %out%
java -cp . -p . -m info.kgeorgiy.java.advanced.implementor jar-interface ru.ifmo.rain.vlasova.implementor.Implementor abracabadra

cd %home%