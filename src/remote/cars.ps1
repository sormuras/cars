
$down=$PSScriptRoot + "\getdown.jar"

echo "Launching cars remote..."

If (-not(Test-Path -path $down)) {
  wget http://repo2.maven.org/maven2/com/threerings/getdown/1.4/getdown-1.4.jar -OutFile getdown.jar
  wget http://stonebone.de/download/cars/remote/getdown.txt -OutFile getdown.txt
}

java -jar getdown.jar $PSScriptRoot
