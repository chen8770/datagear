/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.JdbcUtil;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.persistence.support.AbstractModelDataAccessObject;
import org.datagear.persistence.support.UUID;

/**
 * 基于查询SQL的模型数据查询服务类。
 * 
 * @author datagear@163.com
 *
 */
public class ModelSqlSelectService extends AbstractModelDataAccessObject
{
	public ModelSqlSelectService()
	{
		super();
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param startRow
	 * @param fetchSize
	 * @param databaseModelResolver
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, int startRow, int fetchSize,
			DatabaseModelResolver databaseModelResolver) throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			StatementResultSetPair sr = getStatementResultSetPair(cn, sql, fetchSize);
			st = sr.getStatement();
			rs = sr.getResultSet();

			Model model = databaseModelResolver.resolve(cn, rs, UUID.gen());

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			StatementResultSetPair sr = getStatementResultSetPair(cn, sql, fetchSize);
			st = sr.getStatement();
			rs = sr.getResultSet();

			return select(cn, sql, rs, model, startRow, fetchSize);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 读取SQL的查询结果集。
	 * 
	 * @param cn
	 * @param sql
	 * @param rs
	 * @param model
	 * @param startRow
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public ModelSqlResult select(Connection cn, String sql, ResultSet rs, Model model, int startRow, int fetchSize)
			throws SQLException
	{
		ModelSqlResult modelSqlResult = new ModelSqlResult();
		modelSqlResult.setSql(sql);
		modelSqlResult.setModel(model);
		modelSqlResult.setStartRow(startRow);
		modelSqlResult.setFetchSize(fetchSize);

		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		Property[] properties = model.getProperties();

		if (properties == null || properties.length != columnCount)
			throw new IllegalArgumentException("ResultSet not match Model");

		List<Object> datas = new ArrayList<Object>();

		moveToPrevious(rs, startRow);

		for (int row = startRow; row < startRow + fetchSize; row++)
		{
			if (!rs.next())
				break;

			Object data = model.newInstance();

			for (int j = 1; j <= columnCount; j++)
			{
				Property property = properties[j - 1];

				if (!MU.isConcretePrimitiveProperty(property))
					throw new IllegalArgumentException(
							"The " + (j - 1) + "-th property must be concrete and primitive");

				Object columnValue = toPropertyValue(cn, rs, row, j, model, property,
						PropertyModel.valueOf(property, 0));

				property.set(data, columnValue);
			}

			datas.add(data);
		}

		modelSqlResult.setDatas(datas);

		return modelSqlResult;
	}

	/**
	 * 将一个未移动过游标的{@linkplain ResultSet}游标移动至指定行之前。
	 * 
	 * @param rs
	 * @param row
	 * @throws SQLException
	 */
	protected void moveToPrevious(ResultSet rs, int row) throws SQLException
	{
		// 第一行不做任何操作，避免不必要的调用可能导致底层不支持而报错
		if (row == 1)
			return;

		if (ResultSet.TYPE_FORWARD_ONLY == rs.getType())
			moveToPreviousByNext(rs, row);
		else
		{
			try
			{
				rs.absolute(row - 1);
			}
			catch (SQLException e)
			{
				moveToPreviousByNext(rs, row);
			}
		}
	}

	/**
	 * 将一个未移动过游标的{@linkplain ResultSet}游标移动至指定行之前，通过{@linkplain ResultSet#next()}方式。
	 * 
	 * @param rs
	 * @param row
	 * @throws SQLException
	 */
	protected void moveToPreviousByNext(ResultSet rs, int row) throws SQLException
	{
		for (int i = 1; i < row; i++)
		{
			if (!rs.next())
				break;
		}
	}

	/**
	 * 获取指定查询语句的{@linkplain StatementResultSetPair}。
	 * 
	 * @param cn
	 * @param selectSql
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	protected StatementResultSetPair getStatementResultSetPair(Connection cn, String selectSql, int fetchSize)
			throws SQLException
	{
		// 某些查询SQL语句并不支持ResultSet.TYPE_SCROLL_*（比如SQLServer的聚集列存储索引），
		// 为了兼容此种情况，这里先使用ResultSet.TYPE_SCROLL_INSENSITIVE，如果报错，再降级为ResultSet.TYPE_FORWARD_ONLY

		Statement st = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;

		try
		{
			rs = st.executeQuery(selectSql);

			return new StatementResultSetPair(st, rs);
		}
		catch (SQLException e)
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);
		}

		st = cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);
		rs = st.executeQuery(selectSql);

		return new StatementResultSetPair(st, rs);
	}

	/**
	 * {@linkplain Statement}、{@linkplain ResultSet}封装类。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class StatementResultSetPair
	{
		private Statement statement;

		private ResultSet resultSet;

		public StatementResultSetPair()
		{
			super();
		}

		public StatementResultSetPair(Statement statement, ResultSet resultSet)
		{
			super();
			this.statement = statement;
			this.resultSet = resultSet;
		}

		public Statement getStatement()
		{
			return statement;
		}

		public void setStatement(Statement statement)
		{
			this.statement = statement;
		}

		public ResultSet getResultSet()
		{
			return resultSet;
		}

		public void setResultSet(ResultSet resultSet)
		{
			this.resultSet = resultSet;
		}
	}

	/**
	 * 模型查询结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ModelSqlResult
	{
		private String sql;

		private Model model;

		private int startRow;

		private int fetchSize;

		private List<Object> datas;

		public ModelSqlResult()
		{
			super();
		}

		public ModelSqlResult(String sql, Model model, int startRow, int fetchSize, List<Object> datas)
		{
			super();
			this.sql = sql;
			this.model = model;
			this.startRow = startRow;
			this.fetchSize = fetchSize;
			this.datas = datas;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		public int getStartRow()
		{
			return startRow;
		}

		public void setStartRow(int startRow)
		{
			this.startRow = startRow;
		}

		public int getFetchSize()
		{
			return fetchSize;
		}

		public void setFetchSize(int fetchSize)
		{
			this.fetchSize = fetchSize;
		}

		public boolean hasData()
		{
			return (this.datas != null && !this.datas.isEmpty());
		}

		public List<Object> getDatas()
		{
			return datas;
		}

		public void setDatas(List<Object> datas)
		{
			this.datas = datas;
		}

		public boolean hasMoreData()
		{
			return this.datas != null && this.datas.size() >= this.fetchSize;
		}

		public int getNextStartRow()
		{
			return this.startRow + this.fetchSize;
		}
	}
}
