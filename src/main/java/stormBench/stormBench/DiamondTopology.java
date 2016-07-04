/**
 * 
 */
package stormBench.stormBench;

import java.util.Map;

import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.mapper.JdbcMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcMapper;
import org.apache.storm.shade.com.google.common.collect.Maps;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import stormBench.stormBench.operator.bolt.DiamondHeatwaveBolt;
import stormBench.stormBench.operator.bolt.HookableJdbcInsertBolt;
import stormBench.stormBench.operator.spout.ElementSpout;
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
		
		String topId = parameters.getTopId();
		String sgHost = parameters.getSgHost();
		int sgPort = Integer.parseInt(parameters.getSgPort());
		int nbTasks = Integer.parseInt(parameters.getNbTasks());
		int nbExecutors = Integer.parseInt(parameters.getNbExecutors());
		String dbHost = parameters.getDbHost();
		
		Map<String, Object> map = Maps.newHashMap();
    	map.put("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
    	map.put("dataSource.url", "jdbc:mysql://"+ dbHost +"/benchmarks");
    	map.put("dataSource.user","root");

    	/**
    	 * Declaration of source and sink components
    	 */
        ConnectionProvider connectionProvider = new HikariCPConnectionProvider(map);
        connectionProvider.prepare();

        JdbcMapper jdbcMapperBench = new SimpleJdbcMapper(DIAMOND_TABLE, connectionProvider);
        HookableJdbcInsertBolt PersistanceBolt = new HookableJdbcInsertBolt(connectionProvider, jdbcMapperBench)
        		.withTableName(DIAMOND_TABLE)
        		.withQueryTimeoutSecs(30);
        
        ElementSpout spout = new ElementSpout(sgHost, sgPort);
        
        /**
         * Declaration of the diamond topology
         */
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout("source", spout, nbExecutors).setNumTasks(nbTasks);
        
        builder.setBolt(FieldNames.LYON.toString(), new DiamondHeatwaveBolt(FieldNames.LYON.toString(), 28), nbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping("source", FieldNames.LYON.toString());
        
        builder.setBolt(FieldNames.VILLEUR.toString(), new DiamondHeatwaveBolt(FieldNames.VILLEUR.toString(), 30), nbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping("source", FieldNames.VILLEUR.toString());
        
        builder.setBolt(FieldNames.VAULX.toString(), new DiamondHeatwaveBolt(FieldNames.VAULX.toString(), 26), nbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping("source", FieldNames.VAULX.toString());
        
        builder.setBolt("sink", PersistanceBolt, nbExecutors).setNumTasks(nbTasks)
        .shuffleGrouping(FieldNames.LYON.toString(), FieldNames.LYON.toString())
        .shuffleGrouping(FieldNames.VILLEUR.toString(), FieldNames.VILLEUR.toString())
        .shuffleGrouping(FieldNames.VAULX.toString(), FieldNames.VAULX.toString());
        
        /**
         * Configuration of metadata of the topology
         */
        Config config = new Config();
        config.setNumAckers(8);
        config.put(JDBC_CONF, map);
        
		/**
		 * Call to the topology submitter for storm
		 */
		StormSubmitter.submitTopology(topId, config, builder.createTopology());
	}
}