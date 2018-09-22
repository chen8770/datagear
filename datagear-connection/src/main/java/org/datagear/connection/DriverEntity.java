/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.Serializable;

/**
 * JDBC驱动程序实体。
 * 
 * @author datagear@163.com
 *
 */
public class DriverEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** ID */
	private String id;

	/** 驱动类名 */
	private String driverClassName;

	/** 展示名称 */
	private String displayName;

	/** 展示描述 */
	private String displayDesc;

	public DriverEntity()
	{
		super();
	}

	public DriverEntity(String id, String driverClassName)
	{
		super();
		this.id = id;
		this.driverClassName = driverClassName;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getDriverClassName()
	{
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName)
	{
		this.driverClassName = driverClassName;
	}

	public boolean hasDisplayName()
	{
		return (this.displayName != null && !this.displayName.isEmpty());
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public boolean hasDisplayDesc()
	{
		return (this.displayDesc != null && !this.displayDesc.isEmpty());
	}

	public String getDisplayDesc()
	{
		return displayDesc;
	}

	public void setDisplayDesc(String displayDesc)
	{
		this.displayDesc = displayDesc;
	}

	public String getDisplayText()
	{
		if (hasDisplayName())
			return getDisplayName();
		else
			return getDriverClassName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DriverEntity other = (DriverEntity) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + ", driverClassName=" + driverClassName + ", displayName="
				+ displayName + ", displayDesc=" + displayDesc + "]";
	}

	/**
	 * 构建{@linkplain DriverEntity}。
	 * 
	 * @param id
	 * @param driverClassName
	 * @return
	 */
	public static DriverEntity valueOf(String id, String driverClassName)
	{
		return new DriverEntity(id, driverClassName);
	}
}