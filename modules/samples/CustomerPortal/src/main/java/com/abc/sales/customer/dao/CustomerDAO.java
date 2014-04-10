package com.abc.sales.customer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.abc.sales.customer.Customer;

public class CustomerDAO {
	
	public Customer[] getCustomers() {
		List<Customer> list = new ArrayList<Customer>();
		try {
			DataSource dataSource = getDataSource();
			Connection connection = dataSource.getConnection();

			PreparedStatement prepStmt = connection.prepareStatement("select * from Customer");
			ResultSet results = prepStmt.executeQuery();
			while (results.next()) {
				String name = results.getString("Name");
				String region = results.getString("Region");
				String category = results.getString("Category");
				Customer customer = new Customer(name, category, region);
				list.add(customer);
			}
			results.close();
			prepStmt.close();
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list.toArray(new Customer[list.size()]);
	}
	
	private DataSource getDataSource() {
		DataSource dataSource = null;
		Hashtable<String, String> env = new Hashtable<String, String>();
		try {
			InitialContext context = new InitialContext();
			if(env.size()>0){
				context = new InitialContext(env);
			}
			dataSource = (DataSource) context.lookup("jdbc/customer_ds");			
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return dataSource;
	}
}
