package basicUtil;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import connectionUtil.ConnectionPool;
import connectionUtil.ConnectionPoolImpl;

public class BasicImpl implements Basic {
    private String objectName; // object to work with Basic
    private ConnectionPool cp = ConnectionPoolImpl.getInstance(); // bộ quản lý kết nối dc chia sẻ
    protected Connection con; // kết nối của riêng Basic sử dụng

    public BasicImpl(String objectName) {
        this.objectName = objectName; // xác định đối tượng làm việc

        // xin kết nối
        try {
            this.con = this.cp.getConnection(this.objectName);
            if(this.con.getAutoCommit()) // kiểm tra chế độ thực thi của kết nối
                this.con.setAutoCommit(false); // huỷ chế độ
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean execute(PreparedStatement preStmt) {
        if(preStmt == null) return false;
        try {
            int recordAffectedCount = preStmt.executeUpdate();
            if (recordAffectedCount == 0) {
                this.con.rollback();
                return false;
            }
            this.con.commit(); // xác định thực thi sau cùng
            return true;
        }
        catch(SQLException e) { // trở lại trạng thái an toàn của kết nối
            e.printStackTrace();
            try { this.con.rollback(); } 
            catch (SQLException e1) { e1.printStackTrace(); }
        }
		return false;
    }

    @Override
    public boolean add(PreparedStatement preStmt) {
    	return this.execute(preStmt);
    }

    @Override
    public boolean edit(PreparedStatement preStmt) {
        return this.execute(preStmt);
    }

    @Override
    public boolean del(PreparedStatement preStmt) {
        return this.execute(preStmt);
    }

    @Override
    public ResultSet get(String sql, int value) {
    	PreparedStatement preStmt;
    	ResultSet resultSet;
        try {
        	preStmt = this.con.prepareStatement(sql);
            if(value > 0) preStmt.setInt(1, value);
            resultSet = preStmt.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResultSet get(String sql, String name, String pass) {
    	PreparedStatement preStmt;
    	ResultSet resultSet;
        try {
        	preStmt = this.con.prepareStatement(sql);
            preStmt.setString(1, name);
            preStmt.setString(2, pass);
            resultSet = preStmt.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<ResultSet> gets(String multiSelect) {
        ArrayList<ResultSet> resultSetArray = new ArrayList<>();
        try {
            PreparedStatement stmt = this.con.prepareStatement(multiSelect);
            boolean results = stmt.execute();
            do {
                if(results) {
                    resultSetArray.add(stmt.getResultSet());
                }
                results = stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            } while(results);
            return resultSetArray;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void releaseConnection() {
        try {
            this.cp.releaseConnection(this.con, this.objectName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void finalize() throws Throwable {
        this.releaseConnection();
    }
    
}