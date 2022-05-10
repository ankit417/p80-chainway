// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.info;

import com.codniv.cookbook1.info.subtype.CaptureType;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateType;

public class CaptureInfo extends MorphoInfo
{
	private static CaptureInfo	mInstance	= null;

	public static CaptureInfo getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new CaptureInfo();
			mInstance.reset();
		}
		return mInstance;
	}

	private CaptureInfo()
	{
	}

	public String toString()
	{
		return "IDNumber:\t" + IDNumber + "\r\n" + "FirstName:\t" + FirstName + "\r\n" + "LastName:\t" + LastName + "\r\n" + "fingerNumber:\t" + fingerNumber + "\r\n" + "enrollType:\t" + captureType
				+ "\r\n" + "latentDetect:\t" + latentDetect + "\r\n" + "TemplateType:\t" + templateType;
	}

	public void reset()
	{
		IDNumber = "";
		FirstName = "";
		LastName = "";
		fingerNumber = 1;
		captureType = CaptureType.Enroll;
		latentDetect = false;
		templateType = TemplateType.MORPHO_NO_PK_FP;
		templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
	}

	private String			IDNumber		= "";
	private String			FirstName		= "";
	private String			LastName		= "";
	private int				fingerNumber	= 0;
	private CaptureType		captureType		= CaptureType.Enroll;
	private boolean			latentDetect	= false;
	private TemplateType	templateType	= TemplateType.MORPHO_NO_PK_FP;
	private TemplateFVPType	templateFVPType	= TemplateFVPType.MORPHO_NO_PK_FVP;

	public String getIDNumber()
	{
		return IDNumber;
	}

	public MorphoInfo setIDNumber(String iDNumber)
	{
		IDNumber = iDNumber;
        return null;
    }

	public String getFirstName()
	{
		return FirstName;
	}

	public void setFirstName(String firstName)
	{
		FirstName = firstName;
	}

	public String getLastName()
	{
		return LastName;
	}

	public void setLastName(String lastName)
	{
		LastName = lastName;
	}

	public int getFingerNumber()
	{
		return fingerNumber;
	}

	public void setFingerNumber(int fingerNumber)
	{
		this.fingerNumber = fingerNumber;
	}

	public CaptureType getCaptureType()
	{
		return captureType;
	}

	public void setCaptureType(CaptureType captureType)
	{
		this.captureType = captureType;
	}

	public boolean isLatentDetect()
	{
		return latentDetect;
	}

	public void setLatentDetect(boolean latentDetect)
	{
		this.latentDetect = latentDetect;
	}

	/**
	 * @return the templateType
	 */
	public TemplateType getTemplateType()
	{
		return templateType;
	}

	public TemplateFVPType getTemplateFVPType()
	{
		return templateFVPType;
	}

	/**
	 * @param templateType the templateType to set
	 */
	public void setTemplateType(TemplateType templateType)
	{
		this.templateType = templateType;
	}

	public void setTemplateFVPType(TemplateFVPType templateFVPType)
	{
		this.templateFVPType = templateFVPType;
	}
}
