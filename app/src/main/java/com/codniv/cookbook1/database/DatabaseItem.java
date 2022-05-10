// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.database;

public class DatabaseItem implements Comparable<DatabaseItem>
{

	private String	id;
	private String	firstname;
	private String	lastname;
	private boolean	isSelected;

	public DatabaseItem(String id, String name, String lastname)
	{
		this.id = id;
		this.firstname = name;
		this.lastname = lastname;
		this.setSelected(false);
	}

	public String getFirstName()
	{
		return firstname;
	}

	public String getId()
	{
		return id;
	}

	public String getLastName()
	{
		return lastname;
	}

	@Override
	public int compareTo(DatabaseItem o)
	{
		if (this.id != null)
		{
			return this.id.toLowerCase().compareTo(o.getId().toLowerCase());
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}
}