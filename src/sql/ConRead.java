package sql;

public class ConRead {

	public static void main(String[] args) {


		Dao dao = new Dao();

		dao.insert( "hogehoge", 9000);
		dao.select();

	}

}
