// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.info;

import com.codniv.cookbook1.info.subtype.FingerPrintMode;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;

public class FingerPrintInfo extends MorphoInfo
{
	private static FingerPrintInfo	mInstance				= null;
	private CompressionAlgorithm	compressionAlgorithm	= CompressionAlgorithm.MORPHO_NO_COMPRESS;
	private FingerPrintMode			fingerPrintMode			= FingerPrintMode.Enroll;
	private int						compressRatio			= 10;
	private boolean					latentDetect			= false;

	public static FingerPrintInfo getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new FingerPrintInfo();
			mInstance.reset();
		}
		return mInstance;
	}

	private FingerPrintInfo()
	{
	}

	public String toString()
	{
		return "compressionAlgorithm:\t" + compressionAlgorithm + "\r\n" + "fingerPrintMode:\t" + fingerPrintMode + "\r\n" + "latentDetect:\t" + latentDetect;
	}

	public void reset()
	{
		setFingerPrintMode(FingerPrintMode.Enroll);
		setCompressionAlgorithm(CompressionAlgorithm.MORPHO_NO_COMPRESS);
		setLatentDetect(false);
	}

	public FingerPrintMode getFingerPrintMode()
	{
		return fingerPrintMode;
	}

	public void setFingerPrintMode(FingerPrintMode fingerPrintMode)
	{
		this.fingerPrintMode = fingerPrintMode;
	}

	public CompressionAlgorithm getCompressionAlgorithm()
	{
		return compressionAlgorithm;
	}

	public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm)
	{
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public boolean isLatentDetect()
	{
		return latentDetect;
	}

	public void setLatentDetect(boolean isLatentDetect)
	{
		this.latentDetect = isLatentDetect;
	}

	public int getCompressRatio()
	{
		return compressRatio;
	}

	public void setCompressRatio(int compressRatio)
	{
		this.compressRatio = compressRatio;
	}
}
