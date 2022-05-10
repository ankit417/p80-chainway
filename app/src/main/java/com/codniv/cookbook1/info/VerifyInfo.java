// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.info;

import com.codniv.cookbook1.info.subtype.AuthenticationMode;

public class VerifyInfo extends MorphoInfo
{
	private static VerifyInfo	mInstance	= null;

	public static VerifyInfo getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new VerifyInfo();
			mInstance.reset();
		}
		return mInstance;
	}

	private VerifyInfo()
	{
	}

	private AuthenticationMode	authenticationMode	= AuthenticationMode.File;
	private String				fileName			= "";

	public String toString()
	{
		return "authenticationMode:\t" + authenticationMode + "\r\n" + "fileName:\t" + fileName;
	}

	public void reset()
	{
		authenticationMode = AuthenticationMode.File;
		setFileName("");
	}

	public AuthenticationMode getAuthenticationMode()
	{
		return authenticationMode;
	}

	public void setAuthenticationMode(AuthenticationMode authenticationMode)
	{
		this.authenticationMode = authenticationMode;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
}
