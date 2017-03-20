#!/bin/bash
# compile, lance et stop une topologie

STORM_BIN="/home/xorhead/storm/apache-storm-1.0.2/bin/storm"
PROJECT_DIR="/home/xorhead/autoscale-benchmark"
JAR_NAME="/home/xorhead/autoscale-benchmark/target/stormBench-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
CLASS_NAME="stormBench.stormBench.LinearTopology"

compile(){
	cd $PROJECT_DIR;
	mvn assembly:assembly;
	cd -;
}

launch_topology(){
    echo "Lancement de la topology";
    $STORM_BIN jar $JAR_NAME $CLASS_NAME;
}

stop_topology(){
    echo "kill de heatwave";
    $STORM_BIN kill heatwave;
}

PS3='Choisissez une action: '
options=("Compiler les topologies" "Lancer une topologie" "Stopper une topologie" "Quitter")
select opt in "${options[@]}"
do
    case $opt in
        "Compiler les topologies")
            echo "you chose choice 1"
	    compile
            ;;
        "Lancer une topologie")
            echo "you chose choice 2"
	    launch_topology
            ;;
        "Stopper une topologie")
            echo "you chose choice 3"
	    stop_topology
            ;;
        "Quitter")
            break
            ;;
        *) echo invalid option;;
    esac
done

