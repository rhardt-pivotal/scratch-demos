package com.example.secdemo;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
public class SecdemoApplication extends WebSecurityConfigurerAdapter{

	public static void main(String[] args) {
		SpringApplication.run(SecdemoApplication.class, args);
	}

	@Autowired
	private SecurityProperties secProps;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (secProps.isRequireSsl()) {
			http.requiresChannel().anyRequest().requiresSecure();
		}
		http.csrf().disable();//just to make it easier to test posts.
	}

	@Controller
	static class SecController {

		@RequestMapping("/secure1")
		public ResponseEntity<String> sec1() {
			return ResponseEntity.ok("sec 1 reporting for duty.");
		}


		@RequestMapping("/secure2")
		public ResponseEntity<String> sec2() {
			return ResponseEntity.ok("sec 2 reporting for duty.");
		}

		@RequestMapping(value = "/secpost", method = RequestMethod.POST)
		public ResponseEntity<String> secpost() {
			return ResponseEntity.ok("I feel ya");
		}


	}

	static class ProcessList {
		private String host, id, user, command, state, info, db;

		public ProcessList(){
			super();
		}

		public ProcessList(Map<String, ?> rs) {
			super();
			try {
				this.host = rs.get("Host").toString();
				this.id = rs.get("id").toString();
				this.user = rs.get("User").toString();
				this.command = rs.get("Command").toString();
				this.state = rs.get("State").toString();
				if(rs.containsKey("Info") && rs.get("Info") != null){
					this.info = rs.get("Info").toString();
				}
				if(rs.containsKey("db") && rs.get("db") != null){
					this.db = rs.get("db").toString();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}

		public String getDb() {
			return db;
		}

		public void setDb(String db) {
			this.db = db;
		}


		@Override
		public String toString() {
			return "\t\t************* ProcessList{" +
					"host='" + host + '\'' +
					", id='" + id + '\'' +
					", user='" + user + '\'' +
					", command='" + command + '\'' +
					", state='" + state + '\'' +
					", info='" + info + '\'' +
					", db='" + db+ '\'' +
					'}';
		}
	}

	@Component
	static class DBMonitorComponent {

		private static final Logger log = LoggerFactory.getLogger(DBMonitorComponent.class);

		@Autowired
		private JdbcTemplate jdbcTemplate;

		@Scheduled(fixedRate = 5000)
		public void run() throws SQLException {
			List<ProcessList> plList = jdbcTemplate.queryForList("SHOW PROCESSLIST").stream()
					.map((objMap) -> new ProcessList(objMap))
					.collect(Collectors.toList());
			log.info(plList.toString());
		}



	}




}
