package com.fbs.sys.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class SQLDao {
	private final String driver = "com.mysql.jdbc.Driver";
	// URLָ��Ҫ���ʵ����ݿ���
	private final String url = "jdbc:mysql://127.0.0.1:3306/db_coal";
	// MySQL����ʱ���û���
	private final String user = "root";
	// Java����MySQL����ʱ������
	private final String password = "hello";
	private Connection conn;
	
	public SQLDao() {	
		// ������������
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//��������
	public Connection getConnect(){
		try {
			conn = DriverManager.getConnection(url, user, password);
			if(!conn.isClosed())
				System.out.println("Succeeded connecting to the Database!");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return conn;
	}
	
    public void CutConnection() throws SQLException{
    	try{
           if(conn!=null);
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
	       conn.close();
        }
    }
	public boolean insertCoalTest(final String filename, final double meanvalue,
				final double variance, final long usingtime){
		String insql=null;
		try{
            insql="insert into "+
            		"tb_coal_test(filename,meanvalue,variance,usingtime,enddate) "+
            		"values(?,?,?,?,?)";
           PreparedStatement ps=conn.prepareStatement(insql);
           ps.setString(1, filename);
           ps.setDouble(2, meanvalue);
           ps.setDouble(3, variance);
           ps.setLong(4, usingtime);
           ps.setDate(5, new java.sql.Date((new Date()).getTime()));
           int result=ps.executeUpdate();
           //ps.executeUpdate();�޷��ж��Ƿ��Ѿ�����
           if(result>0)
               return true;
       }catch(Exception e){
           e.printStackTrace();
       }
       return false;
	}
}
