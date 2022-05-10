// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.info;

import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateType;

public class EnrollInfo extends MorphoInfo
{
	private static EnrollInfo	mInstance	= null;

	public static EnrollInfo getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new EnrollInfo();
			mInstance.reset();
		}
		return mInstance;
	}

	private EnrollInfo()
	{
	}

	public String toString()
	{
		return "idNumber" + IDNumber + "\r\n" + "firstname:\t" + firstName + "\r\n" + "lastname:\t" + lastName + "\r\n" + "fingernumber:\t" + fingerNumber + "\r\n" + "savePKinDatabase:\t"
				+ savePKinDatabase + "\r\n" + "exportImage:\t" + compressionAlgorithm.getLabel() + "\r\n" + "fpTemplateType:\t" + templateType;

	}

	public void reset()
	{
		IDNumber = "";
		firstName = "";
		lastName = "";
		fingerNumber = 1;
		savePKinDatabase = true;
		compressionAlgorithm = CompressionAlgorithm.NO_IMAGE;
		templateType = TemplateType.MORPHO_NO_PK_FP;
		updateTemplate = false;
		setFingerIndex(0);
	}

	private String					IDNumber				= "";
	private String					firstName				= "";
	private String					lastName				= "";
	private int						fingerNumber			= 0;
	private boolean					savePKinDatabase		= true;
	private CompressionAlgorithm	compressionAlgorithm	= CompressionAlgorithm.NO_IMAGE;
	private TemplateType			templateType			= TemplateType.MORPHO_NO_PK_FP;
	private TemplateFVPType			fvptemplateType			= TemplateFVPType.MORPHO_NO_PK_FVP;
	private boolean                 updateTemplate          = false;
	private int						fingerIndex				= 0;

	public String getIDNumber()
	{
		return IDNumber;
	}

	public void setIDNumber(String IDNumber)
	{
		this.IDNumber = IDNumber;
	}

	public String getFirstname()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public int getFingerNumber()
	{
		return fingerNumber;
	}

	public void setFingerNumber(int fingerNumber)
	{
		this.fingerNumber = fingerNumber;
	}

	public boolean isSavePKinDatabase()
	{
		return savePKinDatabase;
	}
	
	public boolean isUpdateTemplate()
	{
		return this.updateTemplate;
	}

	public void setSavePKinDatabase(boolean savePKinDatabase)
	{
		this.savePKinDatabase = savePKinDatabase;
	}

	/**
	 * @return the templateType
	 */
	public TemplateType getTemplateType()
	{
		return templateType;
	}

	/**
	 * @return the fvpTemplateType
	 */
	public TemplateFVPType getFVPTemplateType()
	{
		return fvptemplateType;
	}

	/**
	 * @param templateType the templateType to set
	 */
	public void setTemplateType(TemplateType templateType)
	{
		this.templateType = templateType;
	}

	/**
	 * @param templateType the templateType to set
	 */
	public void setFVPTemplateType(TemplateFVPType fvptemplateType)
	{
		this.fvptemplateType = fvptemplateType;
	}

	/**
	 * @return the compressionAlgorithm
	 */
	public CompressionAlgorithm getCompressionAlgorithm()
	{
		return compressionAlgorithm;
	}

	/**
	 * @param compressionAlgorithm the compressionAlgorithm to set
	 */
	public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm)
	{
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public void setUpdateTemplate(boolean updateTemplate) 
	{		
		this.updateTemplate = updateTemplate;
	}

	public int getFingerIndex() {
		return fingerIndex;
	}

	public void setFingerIndex(int fingerIndex) {
		this.fingerIndex = fingerIndex;
	}
}
