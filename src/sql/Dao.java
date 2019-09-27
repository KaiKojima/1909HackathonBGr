package sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dao {


	ConnectionProvider connectionProvider = new ConnectionProvider();

	public String printHiscore(){

		Connection conn = connectionProvider.getConn();


		return null;
	}



	public void insert(String name, int score) {

		Connection conn = connectionProvider.getConn();

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		sql.append("  RECORD ");
		sql.append("VALUES( ");
		sql.append("  ? ");
		sql.append("  ,? ");
		sql.append("  ,SYSDATE() ");
		sql.append("); ");

		PreparedStatement st = null;

		try {

			st = conn.prepareStatement(sql.toString());

			int index = 1;
			st.setString( index++, name);
			st.setInt( index++, score);

			st.executeUpdate();

			conn.commit();

		}catch(SQLException e) {
			e.printStackTrace();
		}finally {
			this.close( null, st, conn);
		}
	}

	private void close( ResultSet rs, PreparedStatement st, Connection conn) {
		if( rs != null ) {
			try {
				rs.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}

		if( st != null ) {
			try {
				st.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}

		if( conn != null ) {
			try {
				conn.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
