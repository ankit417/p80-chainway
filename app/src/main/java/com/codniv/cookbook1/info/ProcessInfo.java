// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.codniv.cookbook1.MorphoFingerprint;
import com.codniv.cookbook1.database.DatabaseItem;
import com.codniv.cookbook1.info.subtype.SecurityOption;
import com.codniv.cookbook1.info.subtype.SensorWindowPosition;
import com.codniv.cookbook1.tools.DeviceDetectionMode;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.IMsoPipe;
import com.morpho.morphosmart.sdk.MatchingStrategy;
import com.morpho.morphosmart.sdk.MorphoDevice.MorphoDevicePrivacyModeStatus;
import com.morpho.morphosmart.sdk.StrategyAcquisitionMode;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.SecurityLevel;
import com.morpho.morphosmart.sdk.MorphoLogLevel;
import com.morpho.morphosmart.sdk.MorphoLogMode;

public class ProcessInfo
{
	private static ProcessInfo	mInstance	= null;

	public static ProcessInfo getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new ProcessInfo();
			mInstance.reset();
		}
		return mInstance;
	}
	
	private IMsoPipe msoPipeClient = null;
	private String ip = null;
	private int port = 0;
	
	public void setMsoPipeClient(IMsoPipe msoPipeClient) { this.msoPipeClient = msoPipeClient; }
	public IMsoPipe getMsoPipeClient() { return msoPipeClient; }
	public void setPort(int port) { this.port = port; }
	public void setIp(String ip) { this.ip = ip; }

	public String getIp() { return ip; }
	public int getPort() { return port; }

	private ProcessInfo()
	{
	}

	public void reset()
	{
		
		// MorphoDevice
		morphoDevice = null;

		//MorphoDataBase
		morphoDatabase = null;

		setStarted(false);

		// Current Tab First Part Info
		morphoInfo = null;

		// Second Part Bottom info
		baseStatusOk = true;
		noCheck = false;

		// Database record list
		databaseItems = null;
		databaseSelectedIndex = -1;

		// MSO Configuration
		MSOSerialNumber = "";
		MSOBus = -1;
		MSOAddress = -1;
		MSOFD = -1;
		SecurityOptions = new ArrayList<SecurityOption>();
		isEncryptedData = false;
		PrivacyModeStatus = MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED;
		PrivacyKey = null;

		// Database information
		maximumNumberofDatabaseValue = 1;
		maximumNumberOfRecordValue = 500;
		numberOfFingerPerRecord = 2;
		currentNumberofRecordValue = 0;
		currentNumberOfFreeRecordValue = 0;
		currentNumberOfUsedRecordValue = 0;		
		pkFormat = "SAGEM PkComp";
		fieldsNumber = 0;

		// General Biometric Info
		matchingThreshold = 5;
		Timeout = 0;
		setCoder(Coder.MORPHO_DEFAULT_CODER);
		securityLevel = SecurityLevel.FFD_SECURITY_LEVEL_LOW_HOST;
		matchingStrategy = MatchingStrategy.MORPHO_STANDARD_MATCHING_STRATEGY;
		forceFingerPlacementOnTop = true;
		advancedSecLevCompReq = false;
		fingerprintQualityThreshold = false;
		fingerprintQualityThresholdvalue = 0;

		// Options        
		imageViewer = true;
		asyncPositioningCommand = true;
		asyncEnrollmentCommand = true;
		asyncDetectQuality = true;
		asyncCodeQuality = true;
		exportMatchingPkNumber = false;
		wakeUpWithLedOff = false;
		sensorWindowPosition = SensorWindowPosition.Normal_0;
		encryptDatabaseValue = "N/A";
		
		logLevel = MorphoLogLevel.MORPHO_LOG_NOLOG;
		logMode	= MorphoLogMode.MORPHO_LOG_DISABLE;
	}

	// MorphoDevice
	private MorphoDevice				morphoDevice						= null;

	private MorphoDatabase				morphoDatabase						= null;

	private boolean						isStarted							= false;

	// Current Tab First Part Info
	private MorphoInfo					morphoInfo							= null;

	// Second Part Bottom info
	private boolean						baseStatusOk						= true;
	private boolean						noCheck								= false;

	// Database record list
	private int							databaseSelectedIndex				= -1;
	private List<DatabaseItem>			databaseItems						= null;

	// Database information
	private String						encryptDatabaseValue				= "N/A";
	private int							maximumNumberofDatabaseValue		= 0;
	private long						maximumNumberOfRecordValue			= 0;
	private int							numberOfFingerPerRecord				= 0;
	private int							currentNumberofRecordValue			= 0;
	private long						currentNumberOfFreeRecordValue		= 0;
	private long						currentNumberOfUsedRecordValue		= 0;	
	private String						pkFormat							= "SAGEM PkComp";
	private long						fieldsNumber						= 0;

	// General Biometric Info
	private int							matchingThreshold					= 5;
	private int							Timeout								= 0;
	private Coder						coder								= Coder.MORPHO_DEFAULT_CODER;
	private SecurityLevel				securityLevel						= SecurityLevel.FFD_SECURITY_LEVEL_LOW_HOST;
	private MatchingStrategy			matchingStrategy					= MatchingStrategy.MORPHO_STANDARD_MATCHING_STRATEGY;
	private StrategyAcquisitionMode		strategyAcquisitionMode				= StrategyAcquisitionMode.MORPHO_ACQ_EXPERT_MODE;
	private boolean						forceFingerPlacementOnTop			= true;
	private boolean						advancedSecLevCompReq				= false;
	private boolean						fingerprintQualityThreshold			= true;
	private int							fingerprintQualityThresholdvalue	= 0;

	// Logging Parameters
	private MorphoLogLevel 				logLevel							= MorphoLogLevel.MORPHO_LOG_NOLOG;
	private MorphoLogMode 				logMode								= MorphoLogMode.MORPHO_LOG_DISABLE;
	
	
	// Options    
	private boolean						imageViewer							= true;
	private boolean						asyncPositioningCommand				= true;
	private boolean						asyncEnrollmentCommand				= true;
	private boolean						asyncDetectQuality					= true;
	private boolean						asyncCodeQuality					= true;
	private boolean						exportMatchingPkNumber				= false;
	private boolean						wakeUpWithLedOff					= false;
	private SensorWindowPosition		sensorWindowPosition				= SensorWindowPosition.Normal_0;
	private int							callbackCmd							= CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue() 
																			| CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue()
																			| CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue()
																			| CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue()
																			| CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();

	// MSO configuration
	DeviceDetectionMode 					MsoDetectionMode 					= DeviceDetectionMode.SdkDetection;
	private String							MSOSerialNumber						= "";
	private int								MSOBus								= -1;
	private int								MSOAddress							= -1;
	private int								MSOFD								= -1;
	private String							MaxFAR								= "";
	private ArrayList<SecurityOption>		SecurityOptions						= new ArrayList<SecurityOption>();
	private boolean							isEncryptedData						= false;
	private MorphoDevicePrivacyModeStatus 	PrivacyModeStatus					= MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED;
	private byte[]							PrivacyKey							= null;				

	private volatile boolean			commandBioStart						= false;

	private MorphoFingerprint				morphoSample						= null;

	public MorphoInfo getMorphoInfo()
	{
		return morphoInfo;
	}

	public void setMorphoInfo(MorphoInfo morphoInfo)
	{
		this.morphoInfo = morphoInfo;
	}

	public boolean isBaseStatusOk()
	{
		return baseStatusOk;
	}

	public void setBaseStatusOk(boolean baseStatusOk)
	{
		this.baseStatusOk = baseStatusOk;
	}

	public String getPKFormat()
	{
		return this.pkFormat;
	}

	public void setPKFormat(String pkformat)
	{
		this.pkFormat = pkformat;
	}

	public long getFieldsNumber()
	{
		return this.fieldsNumber;
	}

	public void setFieldsNumber(long fieldsnumber)
	{
		this.fieldsNumber = fieldsnumber;
	}

	public boolean isNoCheck()
	{
		return noCheck;
	}

	public void setNoCheck(boolean noCheck)
	{
		this.noCheck = noCheck;
	}

	public List<DatabaseItem> getDatabaseItems()
	{
		return databaseItems;
	}

	public void setDatabaseItems(List<DatabaseItem> databaseItems)
	{
		this.databaseItems = databaseItems;
	}

	public int getDatabaseSelectedIndex()
	{
		return databaseSelectedIndex;
	}

	public void setDatabaseSelectedIndex(int databaseSelectedIndex)
	{
		this.databaseSelectedIndex = databaseSelectedIndex;
	}

	public long getMaximumNumberOfRecordValue()
	{
		return maximumNumberOfRecordValue;
	}

	public void setMaximumNumberOfRecordValue(long maximumNumberOfRecordValue)
	{
		this.maximumNumberOfRecordValue = maximumNumberOfRecordValue;
	}

	public int getMaximumNumberOfDBsValue()
	{
		return maximumNumberofDatabaseValue;
	}

	public void setMaximumNumberOfDBsValue(int maximumNumberofDatabaseValue)
	{
		this.maximumNumberofDatabaseValue = maximumNumberofDatabaseValue;
	}

	public int getNumberOfFingerPerRecord()
	{
		return numberOfFingerPerRecord;
	}

	public void setNumberOfFingerPerRecord(int numberOfFingerPerRecord)
	{
		this.numberOfFingerPerRecord = numberOfFingerPerRecord;
	}

	public int getCurrentNumberOfRecordValue()
	{
		return currentNumberofRecordValue;
	}

	public void setCurrentNumberOfRecordValue(int currentNumberOfRecordValue)
	{
		this.currentNumberofRecordValue = currentNumberOfRecordValue;
	}

	public long getCurrentNumberOfFreeRecordValue()
	{
		return currentNumberOfFreeRecordValue;
	}

	public void setCurrentNumberOfFreeRecordValue(long currentNumberOfFreeRecordValue)
	{
		this.currentNumberOfFreeRecordValue = currentNumberOfFreeRecordValue;
	}

	public long getCurrentNumberOfUsedRecordValue()
	{
		return currentNumberOfUsedRecordValue;
	}

	public void setCurrentNumberOfUsedRecordValue(long currentNumberOfUsedRecordValue)
	{
		this.currentNumberOfUsedRecordValue = currentNumberOfUsedRecordValue;
	}


	public DeviceDetectionMode getMsoDetectionMode()
	{
		return MsoDetectionMode;
	}

	public void setMsoDetectionMode(DeviceDetectionMode msoDetectionMode)
	{
		MsoDetectionMode = msoDetectionMode;
	}

	public String getMSOSerialNumber()
	{
		return MSOSerialNumber;
	}

	public void setMSOSerialNumber(String mSOSerialNumber)
	{
		MSOSerialNumber = mSOSerialNumber;
	}

	public int getMSOBus()
	{
		return MSOBus;
	}

	public void setMSOBus(int mSOBus)
	{
		MSOBus = mSOBus;
	}

	public int getMSOAddress()
	{
		return MSOAddress;
	}

	public void setMSOAddress(int mSOAddress)
	{
		MSOAddress = mSOAddress;
	}

	public int getMSOFD()
	{
		return MSOFD;
	}

	public void setMSOFD(int mSOFD)
	{
		MSOFD = mSOFD;
	}

	public String getMaxFAR()
	{
		return MaxFAR;
	}

	public void setMaxFAR(String maxFAR)
	{
		MaxFAR = maxFAR;
	}

	public ArrayList<SecurityOption> getSecurityOptions()
	{
		return SecurityOptions;
	}

	public void setSecurityOptions(ArrayList<SecurityOption> securityOptions)
	{
		SecurityOptions = securityOptions;
	}
	
	public void setBioDataEncryptionState(boolean state) {
		isEncryptedData = state;
	}
	
	public boolean getBioDataEncryptionState() {
		return isEncryptedData;
	}
	
	public void setPrivacyModeStatus(MorphoDevicePrivacyModeStatus status) {
		PrivacyModeStatus = status;
	}
	
	public MorphoDevicePrivacyModeStatus getPrivacyModeStatus() {
		return PrivacyModeStatus;
	}
	
	public void setPrivacyKey(byte[] privacyKey) {
		if(null == privacyKey) {
			PrivacyKey = null;
		} else {
			PrivacyKey = Arrays.copyOf(privacyKey, privacyKey.length);
		}
	}
	
	public byte[] getPrivacyKey() {
		return PrivacyKey;
	}

	/**
	 * @return the morphoDevice
	 */
	public MorphoDevice getMorphoDevice()
	{
		return morphoDevice;
	}

	/**
	 * @param morphoDevice the morphoDevice to set
	 */
	public void setMorphoDevice(MorphoDevice morphoDevice)
	{
		this.morphoDevice = morphoDevice;
	}

	/**
	 * @return the isStarted
	 */
	public boolean isStarted()
	{
		return isStarted;
	}

	/**
	 * @param isStarted the isStarted to set
	 */
	public void setStarted(boolean isStarted)
	{
		this.isStarted = isStarted;
	}

	public boolean isImageViewer()
	{
		return imageViewer;
	}

	public void setImageViewer(boolean imageViewer)
	{
		this.imageViewer = imageViewer;
		if (imageViewer)
		{
			callbackCmd |= CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue();
		}
		else
		{
			callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue();
		}
	}

	public boolean isAsyncPositioningCommand()
	{
		return asyncPositioningCommand;
	}

	public void setAsyncPositioningCommand(boolean asyncPositioningCommand)
	{
		this.asyncPositioningCommand = asyncPositioningCommand;
		if (asyncPositioningCommand)
		{
			callbackCmd |= CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue();
		}
		else
		{
			callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue();
		}
	}

	public boolean isAsyncEnrollmentCommand()
	{
		return asyncEnrollmentCommand;
	}

	public void setAsyncEnrollmentCommand(boolean asyncEnrollmentCommand)
	{
		this.asyncEnrollmentCommand = asyncEnrollmentCommand;
		if (asyncEnrollmentCommand)
		{
			callbackCmd |= CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();
		}
		else
		{
			callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();
		}
	}

	public boolean isAsyncDetectQuality()
	{
		return asyncDetectQuality;
	}

	public void setAsyncDetectQuality(boolean asyncDetectQuality)
	{
		this.asyncDetectQuality = asyncDetectQuality;
		if (asyncDetectQuality)
		{
			callbackCmd |= CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();
		}
		else
		{
			callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();
		}
	}

	public boolean isAsyncCodeQuality()
	{
		return asyncCodeQuality;
	}

	public void setAsyncCodeQuality(boolean asyncCodeQuality)
	{
		this.asyncCodeQuality = asyncCodeQuality;
		if (asyncDetectQuality)
		{
			callbackCmd |= CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue();
		}
		else
		{
			callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue();
		}
	}

	public boolean isExportMatchingPkNumber()
	{
		return exportMatchingPkNumber;
	}

	public void setExportMatchingPkNumber(boolean exportMatchingPkNumber)
	{
		this.exportMatchingPkNumber = exportMatchingPkNumber;
	}

	public boolean isWakeUpWithLedOff()
	{
		return wakeUpWithLedOff;
	}

	public void setWakeUpWithLedOff(boolean wakeUpWithLedOff)
	{
		this.wakeUpWithLedOff = wakeUpWithLedOff;
	}

	public SensorWindowPosition getSensorWindowPosition()
	{
		return sensorWindowPosition;
	}

	public void setSensorWindowPosition(SensorWindowPosition sensorWindowPosition)
	{
		this.sensorWindowPosition = sensorWindowPosition;
	}

	public int getMatchingThreshold()
	{
		return matchingThreshold;
	}

	public int setMatchingThreshold(int matchingThreshold)
	{
		if ((matchingThreshold >= 0) && (matchingThreshold <= 10))
		{
			this.matchingThreshold = matchingThreshold;
		}
		else
		{
			this.matchingThreshold = matchingThreshold % 10;
		}
		return this.matchingThreshold;
	}

	public int getTimeout()
	{
		return Timeout;
	}

	public void setTimeout(int timeout)
	{
		Timeout = timeout;
	}

	public SecurityLevel getSecurityLevel()
	{
		return securityLevel;
	}
 
	public void setSecurityLevel(SecurityLevel securityLevel)
	{
		this.securityLevel = securityLevel;
	}

	public MatchingStrategy getMatchingStrategy()
	{
		return matchingStrategy;
	}

	public void setMatchingStrategy(MatchingStrategy matchingStrategy)
	{
		this.matchingStrategy = matchingStrategy;
	}

	public boolean isForceFingerPlacementOnTop()
	{
		return forceFingerPlacementOnTop;
	}

	public void setForceFingerPlacementOnTop(boolean forceFingerPacementOnTop)
	{
		this.forceFingerPlacementOnTop = forceFingerPacementOnTop;
	}

	public boolean isFingerprintQualityThreshold()
	{
		return fingerprintQualityThreshold;
	}

	public void setFingerprintQualityThreshold(boolean fingerprintQualityThreshold)
	{
		this.fingerprintQualityThreshold = fingerprintQualityThreshold;
	}

	public int getFingerprintQualityThresholdvalue()
	{
		return fingerprintQualityThresholdvalue;
	}

	public void setFingerprintQualityThresholdvalue(int fingerprintQualityThresholdvalue)
	{
		this.fingerprintQualityThresholdvalue = fingerprintQualityThresholdvalue;
	}

	/**
	 * @return the coder
	 */
	public Coder getCoder()
	{
		return coder;
	}

	/**
	 * @param coder the coder to set
	 */
	public void setCoder(Coder coder)
	{
		this.coder = coder;
	}

	/**
	 * @return the morphoDatabase
	 */
	public MorphoDatabase getMorphoDatabase()
	{
		return morphoDatabase;
	}

	/**
	 * @param morphoDatabase the morphoDatabase to set
	 */
	public void setMorphoDatabase(MorphoDatabase morphoDatabase)
	{
		this.morphoDatabase = morphoDatabase;
	}

	public void setAdvancedSecLevCompReq(boolean advancedSecLevCompReq)
	{
		this.advancedSecLevCompReq = advancedSecLevCompReq;
	}

	/**
	 * @return the boolean is Advanced Security Levels Compatibility Required
	 */
	public boolean isAdvancedSecLevCompReq()
	{
		return advancedSecLevCompReq;
	}

	/**
	 * @return the commandBioStart
	 */
	public boolean isCommandBioStart()
	{
		return commandBioStart;
	}

	/**
	 * @param commandBioStart the commandBioStart to set
	 */
	public void setCommandBioStart(boolean commandBioStart)
	{
		this.commandBioStart = commandBioStart;
	}

	public int getCallbackCmd()
	{
		return callbackCmd;
	}

	public void setCallbackCmd(int callbackCmd)
	{
		this.callbackCmd = callbackCmd;
	}

	public MorphoFingerprint getMorphoSample()
	{
		return morphoSample;
	}

	public void setMorphoSample(MorphoFingerprint morphoSample)
	{
		this.morphoSample = morphoSample;
	}

	public String getEncryptDatabaseValue() {
		return encryptDatabaseValue;
	}

	public void setEncryptDatabaseValue(String encryptDatabaseValue) {
		this.encryptDatabaseValue = encryptDatabaseValue;
	}

	public StrategyAcquisitionMode getStrategyAcquisitionMode() {
		return strategyAcquisitionMode;
	}

	public void setStrategyAcquisitionMode(StrategyAcquisitionMode strategyAcquisitionMode) {
		this.strategyAcquisitionMode = strategyAcquisitionMode;
	}

	public MorphoLogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(MorphoLogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public MorphoLogMode getLogMode() {
		return logMode;
	}

	public void setLogMode(MorphoLogMode logMode) {
		this.logMode = logMode;
	}
}
