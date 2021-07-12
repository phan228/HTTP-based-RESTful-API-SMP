package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner

// Name: Ngoc Phan
// SID: 12246097
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;
import java.util.Calendar;

public class DBEngine {
    private DataSource ds;
    public boolean isInit = false;
    public DBEngine(String host, String database, String login, 
		String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String dbConnectionString = null;
            if(database == null) {
                dbConnectionString ="jdbc:mysql://" + host + "?" 
					+"user=" + login  +"&password=" + password 
					+"&useUnicode=true&useJDBCCompliantTimezoneShift=true"
					+"&useLegacyDatetimeCode=false&serverTimezone=UTC"; 
			} else {
                dbConnectionString ="jdbc:mysql://" + host + "/" + database
				+ "?" + "user=" + login  +"&password=" + password 
				+ "&useUnicode=true&useJDBCCompliantTimezoneShift=true"
				+ "&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }
            ds = setupDataSource(dbConnectionString);
            isInit = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    } // DBEngine()

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
            connectionFactory = 
				new DriverManagerConnectionFactory(connectURI, null);
        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    } // setupDataSource()

    public Map<String,String> getUsers() {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT * FROM Identity";
            stmt = conn.prepareStatement(queryString);
			// No parameters, so no binding needed.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String userId = Integer.toString(rs.getInt("idnum"));
                String userName = rs.getString("handle");
                userIdMap.put(userId, userName);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getUsers()

    public int insertuser( String handle, String pass, String fname, String location, String xmail, Date bdate) {
        int returnid = -1;
        PreparedStatement stmt = null;
        try {
            Connection conn = ds.getConnection();
            Calendar calendar = Calendar.getInstance();
            java.sql.Date join = new java.sql.Date(calendar.getTime().getTime());
            String queryString = null;
            queryString = "INSERT INTO Identity(handle, password, fullname, location, email, bdate, joined) VALUES (?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,handle);
            stmt.setString(2, pass);
            stmt.setString(3, fname);
            stmt.setString(4, location);
            stmt.setString(5, xmail);
            stmt.setDate(6, bdate);
            stmt.setDate(7, join);
            stmt.executeUpdate();

            ResultSet r = stmt.getGeneratedKeys();
            if (r.next()) {
                returnid = r.getInt(1);
            }
            r.close();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnid;
    }//insertuser()

    public String getUserData(String idnum) {
        String userData = "{}";
        String userName = null;
        String fname = null;
        String location = null;
        String email = null;
        String birthDay = null;
        String join = null;
        PreparedStatement stmt = null;
        Integer id = Integer.parseInt(idnum);
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT handle, fullname, location, email, bdate, joined FROM Identity WHERE idnum = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userName = rs.getString("handle");
                fname = rs.getString("fullname");
                location = rs.getString("location");
                email = rs.getString("email");
                birthDay = rs.getString("bdate");
                join = rs.getString("joined");
            }

            userData = "{\"status\": \"1\", \"handle\": \"" + userName + "\",\"fullname\":\""
                    + fname + "\",\"location\":\"" + location + "\",\"email\":\"" + email + "\",\"bdate\":\""
                    + birthDay + "\",\"joined\":\"" + join + "\"}";

            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userData;
    } // getUserData()

    public String blockuser(int idnum, int blocked) {
        String result = "{\"status\":\"0\", \"error\":\"DNE\"}";
        PreparedStatement stmt = null;
        try {
            Connection conn = ds.getConnection();
            Calendar calendar = Calendar.getInstance();
            String queryString = null;
            queryString = "INSERT INTO Block(idnum, blocked) VALUES (?,?)";
            stmt = conn.prepareStatement(queryString);
            stmt.setInt(1,idnum);
            stmt.setInt(2, blocked);
            stmt.execute();

            result = "{\"status\":\"1\"}";
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    public Map<String,String> getBDATE(String idnum) {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        Integer id = Integer.parseInt(idnum);
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
// Here is a statement, but we want a prepared statement.
//            queryString = "SELECT bdate FROM Identity WHERE idnum = "+id;
//            
            queryString = "SELECT bdate FROM Identity WHERE idnum = ?";
// ? is a parameter placeholder
            stmt = conn.prepareStatement(queryString);
			stmt.setInt(1,id);
// 1 here is to denote the first parameter.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bdate = rs.getString("bdate");
                userIdMap.put("bdate", bdate);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getBDATE()

    public int reprint(Integer rpnum, Integer idnum, Integer sidnum, Boolean action){
        Integer id = 0;
        PreparedStatement stmt = null;
        try{
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "INSERT INTO Reprint(rpnum, idnum, sidnum, likeit, tstamp) VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP);";
            //queryString = "INSERT INTO Reprint(rpnum, idnum, sidnum, likeit, tstamp) VALUES("+1+"," +1234+","+ 22+","+ true+", CURRENT_TIMESTAMP)";
            stmt = conn.prepareStatement(queryString);

            stmt.setInt(1, rpnum);
            stmt.setInt(2, idnum);
            stmt.setInt(3, sidnum);
            stmt.setBoolean(4, action);
            stmt.execute();

            stmt.close();
            conn.close();
            id = 1;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            id = -1;
        }
        return id;
    } //reprint()

    public int getId(String hand, String pass){

        String password = null;
        Integer id = null;
        try{
            Connection conn = ds.getConnection();
            PreparedStatement stmt = null;
            String queryString = null;

            queryString = "SELECT password, idnum FROM Identity WHERE handle = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,hand);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                id = rs.getInt("idnum");
                password = rs.getString("password");
            }
            rs.close();
            stmt.close();
            conn.close();
            if(password.equals(pass)){				//If the password does not match
                return id;
            }else{
                return -2;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            id = -1;
        }
        return id;
    } //getId()

    public int follow(Integer idnum, Integer userid){
        Integer id = 0;
        PreparedStatement stmt = null;

        try{
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "INSERT INTO Follows(follower, followed, tstamp) VALUES(?, ?, CURRENT_TIMESTAMP);";
            stmt = conn.prepareStatement(queryString);

            stmt.setInt(1, userid);
            stmt.setInt(2, idnum);

            stmt.execute();

            stmt.close();
            conn.close();
            id = 1;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            id = -1;
        }
        return id;
    } //follow()

    public int unfollow(Integer idnum, Integer userid){
        // just put these and date into follow table
        Integer id = 0;
        PreparedStatement stmt = null;

        try{
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "DELETE FROM Follows WHERE follower=? AND followed=?;";
            stmt = conn.prepareStatement(queryString);

            stmt.setInt(1, userid);
            stmt.setInt(2, idnum);
            stmt.execute();

            stmt.close();
            conn.close();
            id = 1;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            id = -1;
        }
        return id;
    } //unfollow()

    public String suggestions(int id)
    {
        String data = "{}";
        Integer idnum = null;
        String username = null;
        int i = id;
        PreparedStatement statement = null;
        try {
            Connection conn = ds.getConnection();
            String query = "";
            statement = conn.prepareStatement(query);
            statement.setInt(1,i);
            ResultSet R = statement.executeQuery();
            while(R.next()) {
                idnum = R.getInt("idnum");
                username = R.getString("handle");
            }
            data = "{\"status\":\"1\", \"idnum\": \"" +idnum + "\", \"handle\":\""
                    + username + "\"}";
            R.close();
            statement.close();
            conn.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return data;
    }// suggestions()

    public Map<String,String> getPoststory() {
        Map<String,String> postStoryMap = new HashMap<>();

        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT Story.sidnum, Identity.handle FROM Story Inner Join Identity On Story.idnum = Identity.idnum";
            stmt = conn.prepareStatement(queryString);
            // No parameters, so no binding needed.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String storyId = Integer.toString(rs.getInt("sidnum"));
                String userName = rs.getString("handle");
                postStoryMap.put(storyId, userName);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return postStoryMap;
    } // getPoststory()

} // class DBEngine
