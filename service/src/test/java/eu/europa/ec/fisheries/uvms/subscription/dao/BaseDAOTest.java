package eu.europa.ec.fisheries.uvms.subscription.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import com.ninja_squad.dbsetup.DbSetupTracker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2gis.functions.factory.H2GISFunctions;

@Slf4j
public abstract class BaseDAOTest {
	protected static DbSetupTracker dbSetupTracker = new DbSetupTracker();

	private final String TEST_DB_USER = "sa";
	private final String TEST_DB_PASSWORD = "";
	private final String TEST_DB_URL = "jdbc:h2:mem:testdb;LOCK_MODE=0;INIT=CREATE SCHEMA IF NOT EXISTS "+ getSchema() +";DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0";

	protected DataSource ds = JdbcConnectionPool.create(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);

	protected EntityManager em;

	@SneakyThrows
	public BaseDAOTest() {

		H2GISFunctions.load(ds.getConnection());

		log.info("BuildingEntityManager for unit tests");
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory(getPersistenceUnitName());
		em = emFactory.createEntityManager();
	}

	protected abstract String getSchema();

	protected abstract String getPersistenceUnitName();
}
