package sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Dao {


	private ConnectionProvider connectionProvider = new ConnectionProvider();
	private int hiScore = 0;


	//hiScoreのゲッター
	public int getHiscore(){
		return hiScore;
	}

	//SELECT文を実行してハイスコアを
	public void select(){
		Connection conn = connectionProvider.getConn();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT  ");
		sql.append("score, ");
		sql.append("date  ");
		sql.append("FROM  ");
		sql.append("record  ");
		sql.append("ORDER BY  ");
		sql.append("score DESC,  ");
		sql.append("date DESC ");
		sql.append("LIMIT 1  ");
		sql.append(";");

		Statement st = null;
		ResultSet rs = null;

		try{
			st = conn.createStatement();
			rs = st.executeQuery(sql.toString());

			while(rs.next()){
				hiScore = rs.getInt("Score");
//				System.out.println(hiScore);
//
//				String date = rs.getString("date");
//				System.out.println(date);
			}


		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			this.close(rs,st,conn);
		}



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

	private void close( ResultSet rs, Statement st, Connection conn) {
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
