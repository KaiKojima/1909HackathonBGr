package sql;

import java.sql.Connection;

public class Dao {


	ConnectionProvider connectionProvider = new ConnectionProvider();

	public String printHiscore(){

		Connection conn = connectionProvider.getConn();


		return null;
	}

}
