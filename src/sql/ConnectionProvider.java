package sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {

	private String url = "jdbc:mysql://192.168.0.112/hiscore?autoReconnect=true&useSSL=false";

	private String user = "bgr";

	private String pass = "alterbo159753";

	public Connection getConn() {

		Connection conn = null;

		try {

			Class.forName( "com.mysql.jdbc.Driver" ).getDeclaredConstructor().newInstance();

			conn = DriverManager.getConnection( url, user, pass);

			conn.setAutoCommit(false);

		}catch(NoSuchMethodException e) {
			e.printStackTrace();
		}catch(InvocationTargetException e) {
			e.printStackTrace();
		}catch( IllegalAccessException e) {
			e.printStackTrace();
		}catch( InstantiationException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}


}
