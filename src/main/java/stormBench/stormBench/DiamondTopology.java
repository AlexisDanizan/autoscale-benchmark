/**
 * 
 */
package stormBench.stormBench;

import java.util.ArrayList;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

import stormBench.stormBench.operator.bolt.elementary.DiamondHeatwaveBolt;
import stormBench.stormBench.operator.bolt.elementary.SleepBolt;
import stormBench.stormBench.operator.spout.elementary.SyntheticStreamSpout;
import stormBench.stormBench.utils.FieldNames;
import stormBench.stormBench.utils.XmlTopologyConfigParser;

/**
 * @author Roland
 *
 */
public class DiamondTopology {

	protected static final String DIAMOND_TABLE = "results_diamond";
	protected static final String JDBC_CONF = "jdbc.conf";
	
	public static void main(String[] args) throws Exception {
		

		/**
		 * Setting of execution parameters
		 */
		XmlTopologyConfigParser parameters = new XmlTopologyConfigParser("topParameters.xml");
		parameters.initParameters();
		
		String stateHost = parameters.getStateHost();
		String topId = parameters.getTopId();
		
		int nbTasks = Integer.parseInt(parameters.getNbTasks());
		int interNbExecutors = Integer.parseInt(parameters.getInterNbExecutors());
		int sinkNbExecutors = Integer.parseInt(parameters.getSinkNbExecutors());
		
    	/**
    	 * Declaration of source and sink components
    	 */
    	ArrayList<Integer> codes = new ArrayList<>();
    	codes.add(0);
    	codes.add(1);
    	codes.add(2);
    	
    	//StreamSimSpout spout = new StreamSimSpout(parameters.getSgHost(), Integer.parseInt(parameters.getSgPort()));
    	SyntheticStreamSpout spout = new SyntheticStreamSpout(stateHost, codes);
        /**
         * Declaration of the diamond topology
         */
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout(FieldNames.SOURCE.toString(), spout).setCPULoad(10.0).setMemoryLoad(64.0);
        
        builder.setBolt(FieldNames.INTER.toString() + FieldNames.LYON.toString(), new DiamondHeatwaveBolt(FieldNames.LYON.toString(), 28), interNbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping(FieldNames.SOURCE.toString(), FieldNames.LYON.toString())
        .setCPULoad(20.0)
        .setMemoryLoad(256.0);
        
        builder.setBolt(FieldNames.INTER.toString() + FieldNames.VILLEUR.toString(), new DiamondHeatwaveBolt(FieldNames.VILLEUR.toString(), 30), interNbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping(FieldNames.SOURCE.toString(), FieldNames.VILLEUR.toString())
        .setCPULoad(20.0)
        .setMemoryLoad(256.0);
        
        builder.setBolt(FieldNames.INTER.toString() + FieldNames.VAULX.toString(), new DiamondHeatwaveBolt(FieldNames.VAULX.toString(), 26), interNbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping(FieldNames.SOURCE.toString(), FieldNames.VAULX.toString())
        .setCPULoad(20.0)
        .setMemoryLoad(256.0);
        
        builder.setBolt(FieldNames.SINK.toString(), new SleepBolt(80), sinkNbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping(FieldNames.INTER.toString() + FieldNames.LYON.toString(), FieldNames.LYON.toString())
        .shuffleGrouping(FieldNames.INTER.toString() + FieldNames.VILLEUR.toString(), FieldNames.VILLEUR.toString())
        .shuffleGrouping(FieldNames.INTER.toString() + FieldNames.VAULX.toString(), FieldNames.VAULX.toString())
        .setCPULoad(80.0)
        .setMemoryLoad(512.0);
        
        /**
         * Configuration of metadata of the topology
         */
        Config config = new Config();
        config.setNumAckers(8);
        config.setNumWorkers(24);
        
		/**
		 * Call to the topology submitter for storm
		 */
		StormSubmitter.submitTopology(topId, config, builder.createTopology());
	}
}