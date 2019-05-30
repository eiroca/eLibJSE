/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.library.diagnostics.monitors;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.ext.library.http.utils.URLFetcherException;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;

public class DatapowerMonitor extends GenericHTTPMonitor {

  protected final static String CONFIG_DATAPOWERURL = "datapowerURL";
  protected final static String CONFIG_DATAPOWERTEMPLATE = "datapowerTemplate";
  protected final static String CONFIG_DATAPOWERVERSION = "datapowerVersion";

  protected final static double ONE_KIB = 1024;

  protected final MetricGroup mgDatapowerAccepted = new MetricGroup("DataPower Accepted", "DataPower Accepted - {0}");
  protected final Measure mConnectionsAccepted_oneDay = mgDatapowerAccepted.createMeasure("oneDay", "ConnectionsAccepted_oneDay", "counter");
  protected final Measure mConnectionsAccepted_oneHour = mgDatapowerAccepted.createMeasure("oneHour", "ConnectionsAccepted_oneHour", "counter");
  protected final Measure mConnectionsAccepted_oneMinute = mgDatapowerAccepted.createMeasure("oneMinute", "ConnectionsAccepted_oneMinute", "counter");
  protected final Measure mConnectionsAccepted_tenMinutes = mgDatapowerAccepted.createMeasure("tenMinutes", "ConnectionsAccepted_tenMinutes", "counter");
  protected final Measure mConnectionsAccepted_tenSeconds = mgDatapowerAccepted.createMeasure("tenSeconds", "ConnectionsAccepted_tenSeconds", "counter");
  //
  protected final MetricGroup mgCPUUsage = new MetricGroup("DataPower CPU Usage", "DataPower CPU Usage - {0}");
  protected final Measure mCPUUsage_oneDay = mgCPUUsage.createMeasure("oneDay", "CPUUsage_oneDay", "percent");
  protected final Measure mCPUUsage_oneHour = mgCPUUsage.createMeasure("oneHour", "CPUUsage_oneHour", "percent");
  protected final Measure mCPUUsage_oneMinute = mgCPUUsage.createMeasure("oneMinute", "CPUUsage_oneMinute", "percent");
  protected final Measure mCPUUsage_tenMinutes = mgCPUUsage.createMeasure("tenMinutes", "CPUUsage_tenMinutes", "percent");
  protected final Measure mCPUUsage_tenSeconds = mgCPUUsage.createMeasure("tenSeconds", "CPUUsage_tenSeconds", "percent");
  //
  protected final MetricGroup mgDocumentCachingSummary = new MetricGroup("DataPower Document Caching", "DataPower Document Caching - {0}");
  protected final Measure mDocumentCachingSummary_CacheCount = mgDocumentCachingSummary.createMeasure("CacheCount", "DocumentCachingSummary_CacheCount", "counter");
  protected final Measure mDocumentCachingSummary_DocCount = mgDocumentCachingSummary.createMeasure("DocCount", "DocumentCachingSummary_DocCount", "counter");
  protected final Measure mDocumentCachingSummary_CacheSize = mgDocumentCachingSummary.createMeasure("CacheSize", "DocumentCachingSummary_CacheSize", "bytes");
  protected final Measure mDocumentCachingSummary_ByteCount = mgDocumentCachingSummary.createMeasure("ByteCount", "DocumentCachingSummary_ByteCount", "bytes");
  //
  protected final MetricGroup mgStylesheetCachingSummary = new MetricGroup("DataPower Stylesheet Caching", "DataPower Stylesheet Caching - {0}");
  protected final Measure mStylesheetCachingSummary_CacheCount = mgStylesheetCachingSummary.createMeasure("CacheCount", "StylesheetCachingSummary_CacheCount", "counter");
  protected final Measure mStylesheetCachingSummary_ReadyCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_ReadyCount", "StylesheetCachingSummary_ReadyCount", "counter");
  protected final Measure mStylesheetCachingSummary_CacheSize = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_CacheSize", "StylesheetCachingSummary_CacheSize", "bytes");
  protected final Measure mStylesheetCachingSummary_PendingCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_PendingCount", "StylesheetCachingSummary_PendingCount", "counter");
  protected final Measure mStylesheetCachingSummary_BadCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_BadCount", "StylesheetCachingSummary_BadCount", "counter");
  protected final Measure mStylesheetCachingSummary_DupCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_DupCount", "StylesheetCachingSummary_DupCount", "counter");
  protected final Measure mStylesheetCachingSummary_CacheKBCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_CacheKBCount", "StylesheetCachingSummary_CacheKBCount", "counter");
  //
  protected final MetricGroup mgEnvironmentalSensors = new MetricGroup("DataPower Environmental Sensors", "DataPower Environmental Sensors - {0}");
  protected final Measure mEnvironmentalSensors_SystemTemp = mgEnvironmentalSensors.createMeasure("SystemTemp", "EnvironmentalSensors_systemTemp", "unit");
  protected final Measure mEnvironmentalSensors_CPU1Temp = mgEnvironmentalSensors.createMeasure("CPU1Temp", "EnvironmentalSensors_CPU1Temp", "unit");
  protected final Measure mEnvironmentalSensors_CPU2Temp = mgEnvironmentalSensors.createMeasure("CPU2Temp", "EnvironmentalSensors_CPU2Temp", "unit");
  protected final Measure mEnvironmentalSensors_CPU1RPM = mgEnvironmentalSensors.createMeasure("CPU1RPM", "EnvironmentalSensors_CPU1RPM", "unit");
  protected final Measure mEnvironmentalSensors_CPU2RPM = mgEnvironmentalSensors.createMeasure("CPU2RPM", "EnvironmentalSensors_CPU2RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis1RPM = mgEnvironmentalSensors.createMeasure("Chassis1RPM", "EnvironmentalSensors_Chassis1RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis2RPM = mgEnvironmentalSensors.createMeasure("Chassis2RPM", "EnvironmentalSensors_Chassis2RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis3RPM = mgEnvironmentalSensors.createMeasure("Chassis3RPM", "EnvironmentalSensors_Chassis3RPM", "unit");
  protected final Measure mEnvironmentalSensors_CaseOpen = mgEnvironmentalSensors.createMeasure("CaseOpen", "EnvironmentalSensors_CaseOpen", "unit");
  protected final Measure mEnvironmentalSensors_Volt33 = mgEnvironmentalSensors.createMeasure("Volt33", "EnvironmentalSensors_Volt33", "unit");
  protected final Measure mEnvironmentalSensors_Volt5 = mgEnvironmentalSensors.createMeasure("Volt5", "EnvironmentalSensors_Volt5", "unit");
  protected final Measure mEnvironmentalSensors_Volt12 = mgEnvironmentalSensors.createMeasure("Volt12", "EnvironmentalSensors_Volt12", "unit");
  protected final Measure mEnvironmentalSensors_PowerSupplyOK = mgEnvironmentalSensors.createMeasure("PowerSupplyOk", "EnvironmentalSensors_PowerSupplyOk", "unit");
  //
  protected final MetricGroup mgHTTPConnections = new MetricGroup("DataPower HTTP Connections", "DataPower HTTP Connections - {0}");
  protected final Measure mHTTPConnections_ReqTenSec = mgHTTPConnections.createMeasure("ReqTenSec", "HTTPConnections_ReqTenSec", "unit");
  protected final Measure mHTTPConnections_ReqSec = mgHTTPConnections.createMeasure("ReqSec", "HTTPConnections_ReqSec", "unit");
  protected final Measure mHTTPConnections_ReqOneMin = mgHTTPConnections.createMeasure("ReqOneMin", "HTTPConnections_ReqOneMin", "unit");
  protected final Measure mHTTPConnections_ReqTenMin = mgHTTPConnections.createMeasure("ReqTenMin", "HTTPConnections_ReqTenMin", "unit");
  protected final Measure mHTTPConnections_ReqOneHour = mgHTTPConnections.createMeasure("ReqOneHour", "HTTPConnections_ReqOneHour", "unit");
  protected final Measure mHTTPConnections_ReqOneDay = mgHTTPConnections.createMeasure("ReqOneDay", "HTTPConnections_ReqOneDay", "unit");
  protected final Measure mHTTPConnections_ReuseTenSec = mgHTTPConnections.createMeasure("ReuseTenSec", "HTTPConnections_ReuseTenSec", "unit");
  protected final Measure mHTTPConnections_ReuseSec = mgHTTPConnections.createMeasure("ReuseSec", "HTTPConnections_ReuseSec", "unit");
  protected final Measure mHTTPConnections_ReuseOneMin = mgHTTPConnections.createMeasure("ReuseOneMin", "HTTPConnections_ReuseOneMin", "unit");
  protected final Measure mHTTPConnections_ReuseTenMin = mgHTTPConnections.createMeasure("ReuseTenMin", "HTTPConnections_ReuseTenMin", "unit");
  protected final Measure mHTTPConnections_ReuseOneHour = mgHTTPConnections.createMeasure("ReuseOneHour", "HTTPConnections_ReuseOneHour", "unit");
  protected final Measure mHTTPConnections_ReuseOneDay = mgHTTPConnections.createMeasure("ReuseOneDay", "HTTPConnections_ReuseOneDay", "unit");
  protected final Measure mHTTPConnections_CreateTenSec = mgHTTPConnections.createMeasure("CreateTenSec", "HTTPConnections_CreateTenSec", "unit");
  protected final Measure mHTTPConnections_CreateSec = mgHTTPConnections.createMeasure("CreateSec", "HTTPConnections_CreateSec", "unit");
  protected final Measure mHTTPConnections_CreateOneMin = mgHTTPConnections.createMeasure("CreateOneMin", "HTTPConnections_CreateOneMin", "unit");
  protected final Measure mHTTPConnections_CreateTenMin = mgHTTPConnections.createMeasure("CreateTenMin", "HTTPConnections_CreateTenMin", "unit");
  protected final Measure mHTTPConnections_CreateOneHour = mgHTTPConnections.createMeasure("CreateOneHour", "HTTPConnections_CreateOneHour", "unit");
  protected final Measure mHTTPConnections_CreateOneDay = mgHTTPConnections.createMeasure("CreateOneDay", "HTTPConnections_CreateOneDay", "unit");
  protected final Measure mHTTPConnections_ReturnTenSec = mgHTTPConnections.createMeasure("ReturnTenSec", "HTTPConnections_ReturnTenSec", "unit");
  protected final Measure mHTTPConnections_ReturnSec = mgHTTPConnections.createMeasure("ReturnSec", "HTTPConnections_ReturnSec", "unit");
  protected final Measure mHTTPConnections_ReturnOneMin = mgHTTPConnections.createMeasure("ReturnOneMin", "HTTPConnections_ReturnOneMin", "unit");
  protected final Measure mHTTPConnections_ReturnTenMin = mgHTTPConnections.createMeasure("ReturnTenMin", "HTTPConnections_ReturnTenMin", "unit");
  protected final Measure mHTTPConnections_ReturnOneHour = mgHTTPConnections.createMeasure("ReturnOneHour", "HTTPConnections_ReturnOneHour", "unit");
  protected final Measure mHTTPConnections_ReturnOneDay = mgHTTPConnections.createMeasure("ReturnOneDay", "HTTPConnections_ReturnOneDay", "unit");
  protected final Measure mHTTPConnections_OfferTenSec = mgHTTPConnections.createMeasure("OfferTenSec", "HTTPConnections_OfferTenSec", "unit");
  protected final Measure mHTTPConnections_OfferSec = mgHTTPConnections.createMeasure("OfferSec", "HTTPConnections_OfferSec", "unit");
  protected final Measure mHTTPConnections_OfferOneMin = mgHTTPConnections.createMeasure("OfferOneMin", "HTTPConnections_OfferOneMin", "unit");
  protected final Measure mHTTPConnections_OfferTenMin = mgHTTPConnections.createMeasure("OfferTenMin", "HTTPConnections_OfferTenMin", "unit");
  protected final Measure mHTTPConnections_OfferOneHour = mgHTTPConnections.createMeasure("OfferOneHour", "HTTPConnections_OfferOneHour", "unit");
  protected final Measure mHTTPConnections_OfferOneDay = mgHTTPConnections.createMeasure("OfferOneDay", "HTTPConnections_OfferOneDay", "unit");
  protected final Measure mHTTPConnections_DestroyTenSec = mgHTTPConnections.createMeasure("DestroyTenSec", "HTTPConnections_DestroyTenSec", "unit");
  protected final Measure mHTTPConnections_DestroySec = mgHTTPConnections.createMeasure("DestroySec", "HTTPConnections_DestroySec", "unit");
  protected final Measure mHTTPConnections_DestroyOneMin = mgHTTPConnections.createMeasure("DestroyOneMin", "HTTPConnections_DestroyOneMin", "unit");
  protected final Measure mHTTPConnections_DestroyTenMin = mgHTTPConnections.createMeasure("DestroyTenMin", "HTTPConnections_DestroyTenMin", "unit");
  protected final Measure mHTTPConnections_DestroyOneHour = mgHTTPConnections.createMeasure("DestroyOneHour", "HTTPConnections_DestroyOneHour", "unit");
  protected final Measure mHTTPConnections_DestroyOneDay = mgHTTPConnections.createMeasure("DestroyOneDay", "HTTPConnections_DestroyOneDay", "unit");
  //
  protected final MetricGroup mgMemoryStatus = new MetricGroup("DataPower Memory Status", "DataPower Memory Status - {0}");
  protected final Measure mMemoryStatus_FreeMemory = mgMemoryStatus.createMeasure("FreeMemory", "MemoryStatus_FreeMemory", "kilobytes");
  protected final Measure mMemoryStatus_HoldMemory = mgMemoryStatus.createMeasure("HoldMemory", "MemoryStatus_HoldMemory", "kilobytes");
  protected final Measure mMemoryStatus_ReqMemory = mgMemoryStatus.createMeasure("ReqMemory", "MemoryStatus_ReqMemory", "kilobytes");
  protected final Measure mMemoryStatus_TotalMemory = mgMemoryStatus.createMeasure("TotalMemory", "MemoryStatus_TotalMemory", "kilobytes");
  protected final Measure mMemoryStatus_UsedMemory = mgMemoryStatus.createMeasure("UsedMemory", "MemoryStatus_UsedMemory", "kilobytes");
  protected final Measure mMemoryStatus_Usage = mgMemoryStatus.createMeasure("Usage", "MemoryStatus_Usage", "unit");
  //
  protected final MetricGroup mgFileSystemStatus = new MetricGroup("DataPower FileSystem Status", "DataPower FileSystem Status - {0}");
  protected final Measure mFileSystemStatus_FreeEncrypted = mgFileSystemStatus.createMeasure("FreeEncrypted", "FileSystemStatus_FreeEncrypted", "unit");
  protected final Measure mFileSystemStatus_TotalEncrypted = mgFileSystemStatus.createMeasure("TotalEncrypted", "FileSystemStatus_TotalEncrypted", "unit");
  protected final Measure mFileSystemStatus_FreeTemporary = mgFileSystemStatus.createMeasure("FreeTemporary", "FileSystemStatus_FreeTemporary", "unit");
  protected final Measure mFileSystemStatus_TotalTemporary = mgFileSystemStatus.createMeasure("TotalTemporary", "FileSystemStatus_TotalTemporary", "unit");
  protected final Measure mFileSystemStatus_FreeInternal = mgFileSystemStatus.createMeasure("FreeInternal", "FileSystemStatus_FreeInternal", "unit");
  protected final Measure mFileSystemStatus_TotalInternal = mgFileSystemStatus.createMeasure("TotalInternal", "FileSystemStatus_TotalInternal", "unit");
  //
  protected final MetricGroup mgSystemUsage = new MetricGroup("DataPower System Usage", "DataPower System Usage - {0}");
  protected final Measure mSystemUsage_Interval = mgSystemUsage.createMeasure("Interval", "SystemUsage_Interval", "unit");
  protected final Measure mSystemUsage_Load = mgSystemUsage.createMeasure("Load", "SystemUsage_Load", "unit");
  protected final Measure mSystemUsage_WorkList = mgSystemUsage.createMeasure("WorkList", "SystemUsage_WorkList", "unit");
  //
  protected final MetricGroup mgTCPSummary = new MetricGroup("DataPower TCP Summary", "DataPower TCP Summary - {0}");
  protected final Measure mTCPSummary_Established = mgTCPSummary.createMeasure("Established", "TCPSummary_Established", "unit");
  protected final Measure mTCPSummary_Syn_Sent = mgTCPSummary.createMeasure("Syn_Sent", "TCPSummary_Syn_Sent", "unit");
  protected final Measure mTCPSummary_Syn_Received = mgTCPSummary.createMeasure("Syn_Received", "TCPSummary_Syn_Received", "unit");
  protected final Measure mTCPSummary_Fin_Wait_1 = mgTCPSummary.createMeasure("Fin_Wait_1", "TCPSummary_Fin_Wait_1", "unit");
  protected final Measure mTCPSummary_Fin_Wait_2 = mgTCPSummary.createMeasure("Fin_Wait_2", "TCPSummary_Fin_Wait_2", "unit");
  protected final Measure mTCPSummary_Time_Wait = mgTCPSummary.createMeasure("Time_Wait", "TCPSummary_Time_Wait", "unit");
  protected final Measure mTCPSummary_Closed = mgTCPSummary.createMeasure("Closed", "TCPSummary_Closed", "unit");
  protected final Measure mTCPSummary_Close_Wait = mgTCPSummary.createMeasure("Close_Wait", "TCPSummary_Close_Wait", "unit");
  protected final Measure mTCPSummary_Last_Ack = mgTCPSummary.createMeasure("Last_Ack", "TCPSummary_Last_Ack", "unit");
  protected final Measure mTCPSummary_Listen = mgTCPSummary.createMeasure("Listen", "TCPSummary_Listen", "unit");
  protected final Measure mTCPSummary_Closing = mgTCPSummary.createMeasure("Closing", "TCPSummary_Closing", "unit");
  //
  protected final MetricGroup mgEthernetInterfaceStatus = new MetricGroup("DataPower Ethernet Interface Status", "DataPower Ethernet Interface Status - {0}");
  protected final Measure mEthernetInterfaceStatus_Status = mgEthernetInterfaceStatus.createMeasure("Status", "EthernetInterfaceStatus_Status", "unit");
  protected final Measure mEthernetInterfaceStatus_Collisions = mgEthernetInterfaceStatus.createMeasure("Collisions", "EthernetInterfaceStatus_Collisions", "unit");
  protected final Measure mEthernetInterfaceStatus_Collisions2 = mgEthernetInterfaceStatus.createMeasure("Collisions2", "EthernetInterfaceStatus_Collisions2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCPackets = mgEthernetInterfaceStatus.createMeasure("RxHCPackets", "EthernetInterfaceStatus_RxHCPackets", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCPackets2 = mgEthernetInterfaceStatus.createMeasure("RxHCPackets2", "EthernetInterfaceStatus_RxHCPackets2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCBytes = mgEthernetInterfaceStatus.createMeasure("RxHCBytes", "EthernetInterfaceStatus_RxHCBytes", "bytes");
  protected final Measure mEthernetInterfaceStatus_RxHCBytes2 = mgEthernetInterfaceStatus.createMeasure("RxHCBytes2", "EthernetInterfaceStatus_RxHCBytes2", "bytes");
  protected final Measure mEthernetInterfaceStatus_RxErrors = mgEthernetInterfaceStatus.createMeasure("RxErrors", "EthernetInterfaceStatus_RxErrors", "unit");
  protected final Measure mEthernetInterfaceStatus_RxErrors2 = mgEthernetInterfaceStatus.createMeasure("RxErrors2", "EthernetInterfaceStatus_RxErrors2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxDrops = mgEthernetInterfaceStatus.createMeasure("RxDrops", "EthernetInterfaceStatus_RxDrops", "unit");
  protected final Measure mEthernetInterfaceStatus_RxDrops2 = mgEthernetInterfaceStatus.createMeasure("RxDrops2", "EthernetInterfaceStatus_RxDrops2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCPackets = mgEthernetInterfaceStatus.createMeasure("TxHCPackets", "EthernetInterfaceStatus_TxHCPackets", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCPackets2 = mgEthernetInterfaceStatus.createMeasure("TxHCPackets2", "EthernetInterfaceStatus_TxHCPackets2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCBytes = mgEthernetInterfaceStatus.createMeasure("TxHCBytes", "EthernetInterfaceStatus_TxHCBytes", "bytes");
  protected final Measure mEthernetInterfaceStatus_TxHCBytes2 = mgEthernetInterfaceStatus.createMeasure("TxHCBytes2", "EthernetInterfaceStatus_TxHCBytes2", "bytes");
  protected final Measure mEthernetInterfaceStatus_TxErrors = mgEthernetInterfaceStatus.createMeasure("TxErrors", "EthernetInterfaceStatus_TxErrors", "unit");
  protected final Measure mEthernetInterfaceStatus_TxErrors2 = mgEthernetInterfaceStatus.createMeasure("TxErrors2", "EthernetInterfaceStatus_TxErrors2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxDrops = mgEthernetInterfaceStatus.createMeasure("TxDrops", "EthernetInterfaceStatus_TxDrops", "unit");
  protected final Measure mEthernetInterfaceStatus_TxDrops2 = mgEthernetInterfaceStatus.createMeasure("TxDrops2", "EthernetInterfaceStatus_TxDrops2", "unit");
  //
  protected final MetricGroup mgObjectStatus = new MetricGroup("DataPower Object Status", "DataPower Object Status - {0}");
  protected final Measure mObjectStatus_OpState = mgObjectStatus.createMeasure("OpState", "ObjectStatus_OpState", "unit");
  protected final Measure mObjectStatus_AdminState = mgObjectStatus.createMeasure("AdminState", "ObjectStatus_AdminState", "unit");
  //
  protected final MetricGroup mgStylesheetExecutions = new MetricGroup("DataPower Stylesheet Executions", "DataPower Stylesheet Executions - {0}");
  protected final Measure mStylesheetExecutions_TenSeconds = mgStylesheetExecutions.createMeasure("TenSeconds", "StylesheetExecutions_TenSeconds", "unit");
  protected final Measure mStylesheetExecutions_OneMinute = mgStylesheetExecutions.createMeasure("OneMinute", "StylesheetExecutions_OneMinute", "unit");
  protected final Measure mStylesheetExecutions_TenMinutes = mgStylesheetExecutions.createMeasure("TenMinutes", "StylesheetExecutions_TenMinutes", "unit");
  protected final Measure mStylesheetExecutions_OneHour = mgStylesheetExecutions.createMeasure("OneHour", "StylesheetExecutions_OneHour", "unit");
  protected final Measure mStylesheetExecutions_OneDay = mgStylesheetExecutions.createMeasure("OneDay", "StylesheetExecutions_OneDay", "unit");
  //
  protected final MetricGroup mgDomainStatus = new MetricGroup("DataPower Domain Status", "DataPower Domain Status - {0}");
  protected final Measure mDomainStatus_SaveNeeded = mgDomainStatus.createMeasure("SaveNeeded", "DomainStatus_SaveNeeded", "unit");
  protected final Measure mDomainStatus_TraceEnabled = mgDomainStatus.createMeasure("TraceEnabled", "DomainStatus_TraceEnabled", "unit");
  protected final Measure mDomainStatus_DebugEnabled = mgDomainStatus.createMeasure("DebugEnabled", "DomainStatus_DebugEnabled", "unit");
  protected final Measure mDomainStatus_ProbeEnabled = mgDomainStatus.createMeasure("ProbeEnabled", "DomainStatus_ProbeEnabled", "unit");
  protected final Measure mDomainStatus_DiagEnabled = mgDomainStatus.createMeasure("DiagEnabled", "DomainStatus_DiagEnabled", "unit");
  //
  protected final MetricGroup mgHTTPTransactions = new MetricGroup("DataPower HTTP Transactions", "DataPower HTTP Transactions - {0}");
  protected final Measure mHTTPTransactions_TenSeconds = mgHTTPTransactions.createMeasure("TenSeconds", "HTTPTransactions_TenSeconds", "unit");
  protected final Measure mHTTPTransactions_OneMinute = mgHTTPTransactions.createMeasure("OneMinute", "HTTPTransactions_OneMinute", "unit");
  protected final Measure mHTTPTransactions_TenMinutes = mgHTTPTransactions.createMeasure("TenMinutes", "HTTPTransactions_TenMinutes", "unit");
  protected final Measure mHTTPTransactions_OneHour = mgHTTPTransactions.createMeasure("OneHour", "HTTPTransactions_OneHour", "unit");
  protected final Measure mHTTPTransactions_OneDay = mgHTTPTransactions.createMeasure("OneDay", "HTTPTransactions_OneDay", "unit");
  //
  protected final MetricGroup mgHTTPMeanTransactionTime = new MetricGroup("DataPower HTTP Mean Transaction Time", "DataPower HTTP Mean Transaction Time - {0}");
  protected final Measure mHTTPMeanTransactionTime_TenSeconds = mgHTTPMeanTransactionTime.createMeasure("TenSeconds", "HTTPMeanTransactionTime_TenSeconds", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneMinute = mgHTTPMeanTransactionTime.createMeasure("OneMinute", "HTTPMeanTransactionTime_OneMinute", "ms");
  protected final Measure mHTTPMeanTransactionTime_TenMinutes = mgHTTPMeanTransactionTime.createMeasure("TenMinutes", "HTTPMeanTransactionTime_TenMinutes", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneHour = mgHTTPMeanTransactionTime.createMeasure("OneHour", "HTTPMeanTransactionTime_OneHour", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneDay = mgHTTPMeanTransactionTime.createMeasure("OneDay", "HTTPMeanTransactionTime_OneDay", "ms");
  //
  protected final MetricGroup mgWSOperationMetricsSimpleIndex = new MetricGroup("DataPower WS Operation Metrics Simple Index", "DataPower WS Operation Metrics Simple Index - {0}");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfRequests", "WSOperationMetricsSimpleIndex_NumberOfRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfFailedRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfFailedRequests", "WSOperationMetricsSimpleIndex_NumberOfFailedRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfSuccessfulRequests", "WSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_ServiceTime = mgWSOperationMetricsSimpleIndex.createMeasure("ServiceTime", "WSOperationMetricsSimpleIndex_ServiceTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxResponseTime = mgWSOperationMetricsSimpleIndex.createMeasure("MaxResponseTime", "WSOperationMetricsSimpleIndex_MaxResponseTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_LastResponseTime = mgWSOperationMetricsSimpleIndex.createMeasure("LastResponseTime", "WSOperationMetricsSimpleIndex_LastResponseTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxRequestSize = mgWSOperationMetricsSimpleIndex.createMeasure("MaxRequestSize", "WSOperationMetricsSimpleIndex_MaxRequestSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_LastRequestSize = mgWSOperationMetricsSimpleIndex.createMeasure("LastRequestSize", "WSOperationMetricsSimpleIndex_LastRequestSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxResponseSize = mgWSOperationMetricsSimpleIndex.createMeasure("MaxResponseSize", "WSOperationMetricsSimpleIndex_MaxResponseSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_LastResponseSize = mgWSOperationMetricsSimpleIndex.createMeasure("LastResponseSize", "WSOperationMetricsSimpleIndex_LastResponseSize", "bytes");

  private final class MetricDef {

    Measure measure;
    int type = 0;
    String attribute;
    double scale = 1.0;
    int minAPIversion = 0;
    int maxAPIversion = Integer.MAX_VALUE;
    String splitName;
    String splitKey1;
    String splitKey2;

    public MetricDef(Measure measure, String attribute) {
      super();
      this.measure = measure;
      this.attribute = attribute;
    }

    public MetricDef minAPIversion(int minAPIversion) {
      this.minAPIversion = minAPIversion;
      this.maxAPIversion = Integer.MAX_VALUE;
      return this;
    }

    public MetricDef maxAPIversion(int maxAPIversion) {
      this.minAPIversion = 0;
      this.maxAPIversion = maxAPIversion;
      return this;
    }

    public MetricDef scale(double scale) {
      this.scale = scale;
      return this;
    }

    public MetricDef split(String splitName, String key1, String key2) {
      this.splitName = splitName;
      splitKey1 = key1;
      splitKey2 = key2;
      return this;
    }

    public MetricDef split(String splitName, String key) {
      this.splitName = splitName;
      splitKey1 = key;
      splitKey2 = null;
      return this;
    }

    public MetricDef type(int type) {
      this.type = type;
      return this;
    }

  }

  private static Map<String, Double> ALIAS = new HashMap<>();
  static {
    ALIAS.put("yes", 1.0);
    ALIAS.put("ok", 1.0);
    ALIAS.put("on", 1.0);
    ALIAS.put("up", 1.0);
    ALIAS.put("enabled", 1.0);
  }

  private List<MetricDef> connectionsAcceptedMetrics = new ArrayList<>();
  private List<MetricDef> CPUUsageMetrics = new ArrayList<>();
  private List<MetricDef> documentCachingSummary = new ArrayList<>();
  private List<MetricDef> stylesheetCachingSummary = new ArrayList<>();
  private List<MetricDef> environmentalSensors = new ArrayList<>();
  private List<MetricDef> httpConnections = new ArrayList<>();
  private List<MetricDef> memoryStatus = new ArrayList<>();
  private List<MetricDef> filesystemStatus = new ArrayList<>();
  private List<MetricDef> systemUsage = new ArrayList<>();
  private List<MetricDef> TCPSummary = new ArrayList<>();
  private List<MetricDef> objectStatus = new ArrayList<>();
  private List<MetricDef> ethernetInterfaceStatus = new ArrayList<>();
  private List<MetricDef> stylesheetExecutions = new ArrayList<>();
  private List<MetricDef> domainStatus = new ArrayList<>();
  private List<MetricDef> httpTransactions = new ArrayList<>();
  private List<MetricDef> httpMeanTransactionTime = new ArrayList<>();
  private List<MetricDef> wsOperationMetricsSimpleIndex = new ArrayList<>();

  public DatapowerMonitor() {
    super();
    connectionsAcceptedMetrics.add(new MetricDef(mConnectionsAccepted_tenSeconds, "tenSeconds"));
    connectionsAcceptedMetrics.add(new MetricDef(mConnectionsAccepted_oneMinute, "oneMinute"));
    connectionsAcceptedMetrics.add(new MetricDef(mConnectionsAccepted_tenMinutes, "tenMinutes"));
    connectionsAcceptedMetrics.add(new MetricDef(mConnectionsAccepted_oneHour, "oneHour"));
    connectionsAcceptedMetrics.add(new MetricDef(mConnectionsAccepted_oneDay, "oneDay"));
    //
    CPUUsageMetrics.add(new MetricDef(mCPUUsage_tenSeconds, "tenSeconds"));
    CPUUsageMetrics.add(new MetricDef(mCPUUsage_oneMinute, "oneMinute"));
    CPUUsageMetrics.add(new MetricDef(mCPUUsage_tenMinutes, "tenMinutes"));
    CPUUsageMetrics.add(new MetricDef(mCPUUsage_oneHour, "oneHour"));
    CPUUsageMetrics.add(new MetricDef(mCPUUsage_oneDay, "oneDay"));
    //
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_CacheCount, "CacheCount").split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_DocCount, "DocCount").split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_CacheSize, "CacheSizeKiB").minAPIversion(7).scale(ONE_KIB).split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_ByteCount, "KiByteCount").minAPIversion(7).scale(ONE_KIB).split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_CacheSize, "CacheSize").maxAPIversion(6).split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_ByteCount, "ByteCount").maxAPIversion(6).split("XMLManager", "XMLManager"));
    //
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_CacheCount, "CacheCount"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_ReadyCount, "ReadyCount"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_CacheSize, "CacheSize"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_PendingCount, "PendingCount"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_BadCount, "BadCount"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_DupCount, "DupCount"));
    stylesheetCachingSummary.add(new MetricDef(mStylesheetCachingSummary_CacheKBCount, "CacheKBCount"));
    //
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_SystemTemp, "systemTemp"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_CPU1Temp, "cpu1Temp"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_CPU2Temp, "cpu2Temp"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_CPU1RPM, "cpu1rpm"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_CPU2RPM, "cpu2rpm"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Chassis1RPM, "chassis1rpm"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Chassis2RPM, "chassis2rpm"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Chassis3RPM, "chassis3rpm"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_CaseOpen, "caseopen"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Volt5, "volt5"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Volt12, "volt12"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_Volt33, "volt33"));
    environmentalSensors.add(new MetricDef(mEnvironmentalSensors_PowerSupplyOK, "powersupply"));
    //
    httpConnections.add(new MetricDef(mHTTPConnections_ReqTenSec, "reqTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReqSec, "reqTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_ReqOneMin, "reqOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReqTenMin, "reqTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReqOneHour, "reqOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReqOneDay, "reqOneDay"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseTenSec, "reuseTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseSec, "reuseTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseOneMin, "reuseOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseTenMin, "reuseTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseOneHour, "reuseOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReuseOneDay, "reuseOneDay"));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateTenSec, "createTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateSec, "createTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateOneMin, "createOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateTenMin, "createTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateOneHour, "createOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_CreateOneDay, "createOneDay"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnTenSec, "returnTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnSec, "returnTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnOneMin, "returnOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnTenMin, "returnTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnOneHour, "returnOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_ReturnOneDay, "returnOneDay"));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferTenSec, "offerTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferSec, "offerTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferOneMin, "offerOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferTenMin, "offerTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferOneHour, "offerOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_OfferOneDay, "offerOneDay"));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroyTenSec, "destroyTenSec"));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroySec, "destroyTenSec").scale(0.1));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroyOneMin, "destroyOneMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroyTenMin, "destroyTenMin"));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroyOneHour, "destroyOneHr"));
    httpConnections.add(new MetricDef(mHTTPConnections_DestroyOneDay, "destroyOneDay"));
    //
    memoryStatus.add(new MetricDef(mMemoryStatus_TotalMemory, "TotalMemory"));
    memoryStatus.add(new MetricDef(mMemoryStatus_UsedMemory, "UsedMemory"));
    memoryStatus.add(new MetricDef(mMemoryStatus_FreeMemory, "FreeMemory"));
    memoryStatus.add(new MetricDef(mMemoryStatus_ReqMemory, "ReqMemory"));
    memoryStatus.add(new MetricDef(mMemoryStatus_HoldMemory, "HoldMemory"));
    memoryStatus.add(new MetricDef(mMemoryStatus_Usage, "Usage"));
    //
    filesystemStatus.add(new MetricDef(mFileSystemStatus_FreeEncrypted, "FreeEncrypted"));
    filesystemStatus.add(new MetricDef(mFileSystemStatus_TotalEncrypted, "TotalEncrypted"));
    filesystemStatus.add(new MetricDef(mFileSystemStatus_FreeTemporary, "FreeTemporary"));
    filesystemStatus.add(new MetricDef(mFileSystemStatus_TotalTemporary, "TotalTemporary"));
    filesystemStatus.add(new MetricDef(mFileSystemStatus_FreeInternal, "FreeInternal"));
    filesystemStatus.add(new MetricDef(mFileSystemStatus_TotalInternal, "TotalInternal"));
    //
    systemUsage.add(new MetricDef(mSystemUsage_Interval, "Interval"));
    systemUsage.add(new MetricDef(mSystemUsage_Load, "Load"));
    systemUsage.add(new MetricDef(mSystemUsage_WorkList, "WorkList"));
    //
    TCPSummary.add(new MetricDef(mTCPSummary_Established, "established"));
    TCPSummary.add(new MetricDef(mTCPSummary_Syn_Sent, "syn_sent"));
    TCPSummary.add(new MetricDef(mTCPSummary_Syn_Received, "syn_received"));
    TCPSummary.add(new MetricDef(mTCPSummary_Fin_Wait_1, "fin_wait_1"));
    TCPSummary.add(new MetricDef(mTCPSummary_Fin_Wait_2, "fin_wait_2"));
    TCPSummary.add(new MetricDef(mTCPSummary_Time_Wait, "time_wait"));
    TCPSummary.add(new MetricDef(mTCPSummary_Closed, "closed"));
    TCPSummary.add(new MetricDef(mTCPSummary_Close_Wait, "close_wait"));
    TCPSummary.add(new MetricDef(mTCPSummary_Last_Ack, "last_ack"));
    TCPSummary.add(new MetricDef(mTCPSummary_Listen, "listen"));
    TCPSummary.add(new MetricDef(mTCPSummary_Closing, "closing"));
    //
    objectStatus.add(new MetricDef(mObjectStatus_OpState, "OpState").split("Object Name", "Class", "Name"));
    objectStatus.add(new MetricDef(mObjectStatus_OpState, "AdminState").split("Object Name", "Class", "Name"));
    //
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_Status, "Status").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_Collisions, "Collisions").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_Collisions2, "Collisions2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxHCPackets, "RxHCPackets").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxHCPackets2, "RxHCPackets2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxHCBytes, "RxHCBytes").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxHCBytes2, "RxHCBytes2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxErrors, "RxErrors").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxErrors2, "RxErrors2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxDrops, "RxDrops").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_RxDrops2, "RxDrops2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxHCPackets, "TxHCPackets").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxHCPackets2, "TxHCPackets2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxHCBytes, "TxHCBytes").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxHCBytes2, "TxHCBytes2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxErrors, "TxErrors").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxErrors2, "TxErrors2").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxDrops, "TxDrops").split("Interface Name", "Name", "IP"));
    ethernetInterfaceStatus.add(new MetricDef(mEthernetInterfaceStatus_TxDrops2, "TxDrops2").split("Interface Name", "Name", "IP"));
    //
    stylesheetExecutions.add(new MetricDef(mStylesheetExecutions_TenSeconds, "tenSeconds").split("Stylesheet URL", "URL"));
    stylesheetExecutions.add(new MetricDef(mStylesheetExecutions_OneMinute, "oneMinute").split("Stylesheet URL", "URL"));
    stylesheetExecutions.add(new MetricDef(mStylesheetExecutions_TenMinutes, "tenMinutes").split("Stylesheet URL", "URL"));
    stylesheetExecutions.add(new MetricDef(mStylesheetExecutions_OneHour, "oneHour").split("Stylesheet URL", "URL"));
    stylesheetExecutions.add(new MetricDef(mStylesheetExecutions_OneDay, "oneDay").split("Stylesheet URL", "URL"));
    //
    domainStatus.add(new MetricDef(mDomainStatus_SaveNeeded, "SaveNeeded").split("Domain Name", "Domain"));
    domainStatus.add(new MetricDef(mDomainStatus_TraceEnabled, "TraceEnabled").split("Domain Name", "Domain"));
    domainStatus.add(new MetricDef(mDomainStatus_DebugEnabled, "DebugEnabled").split("Domain Name", "Domain"));
    domainStatus.add(new MetricDef(mDomainStatus_ProbeEnabled, "ProbeEnabled").split("Domain Name", "Domain"));
    domainStatus.add(new MetricDef(mDomainStatus_DiagEnabled, "DiagEnabled").split("Domain Name", "Domain"));
    //
    httpTransactions.add(new MetricDef(mHTTPTransactions_TenSeconds, "tenSeconds").split("Proxy", "proxy"));
    httpTransactions.add(new MetricDef(mHTTPTransactions_OneMinute, "oneMinute").split("Proxy", "proxy"));
    httpTransactions.add(new MetricDef(mHTTPTransactions_TenMinutes, "tenMinutes").split("Proxy", "proxy"));
    httpTransactions.add(new MetricDef(mHTTPTransactions_OneHour, "oneHour").split("Proxy", "proxy"));
    httpTransactions.add(new MetricDef(mHTTPTransactions_OneDay, "oneDay").split("Proxy", "proxy"));
    //
    httpMeanTransactionTime.add(new MetricDef(mHTTPMeanTransactionTime_TenSeconds, "tenSeconds").split("Proxy", "proxy"));
    httpMeanTransactionTime.add(new MetricDef(mHTTPMeanTransactionTime_OneMinute, "oneMinute").split("Proxy", "proxy"));
    httpMeanTransactionTime.add(new MetricDef(mHTTPMeanTransactionTime_TenMinutes, "tenMinutes").split("Proxy", "proxy"));
    httpMeanTransactionTime.add(new MetricDef(mHTTPMeanTransactionTime_OneHour, "oneHour").split("Proxy", "proxy"));
    httpMeanTransactionTime.add(new MetricDef(mHTTPMeanTransactionTime_OneDay, "oneDay").split("Proxy", "proxy"));
    //
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_NumberOfRequests, "NumberOfRequests").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_NumberOfFailedRequests, "NumberOfFailedRequests").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests, "NumberOfSuccessfulRequests").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_ServiceTime, "ServiceTime").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_MaxResponseTime, "MaxResponseTime").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_LastResponseTime, "LastResponseTime").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_MaxRequestSize, "MaxRequestSize").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_LastRequestSize, "LastRequestSize").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_MaxResponseSize, "MaxResponseSize").split("Service Endpoint Name", "ServiceEndpoint"));
    wsOperationMetricsSimpleIndex.add(new MetricDef(mWSOperationMetricsSimpleIndex_LastResponseSize, "LastResponseSize").split("Service Endpoint Name", "ServiceEndpoint"));
  }

  private String datapowerTemplate;
  private int datapowerVersion;

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgCPUUsage);
    groups.add(mgDatapowerAccepted);
    groups.add(mgDocumentCachingSummary);
    groups.add(mgDomainStatus);
    groups.add(mgEnvironmentalSensors);
    groups.add(mgEthernetInterfaceStatus);
    groups.add(mgFileSystemStatus);
    groups.add(mgHTTPConnections);
    groups.add(mgHTTPMeanTransactionTime);
    groups.add(mgHTTPTransactions);
    groups.add(mgMemoryStatus);
    groups.add(mgObjectStatus);
    groups.add(mgStylesheetCachingSummary);
    groups.add(mgStylesheetExecutions);
    groups.add(mgSystemUsage);
    groups.add(mgTCPSummary);
  }

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    datapowerTemplate = context.getConfigString(CONFIG_DATAPOWERTEMPLATE, null);
    datapowerVersion = context.getConfigInt(CONFIG_DATAPOWERVERSION, 7);
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      final URL baseURL = getURL(CONFIG_DATAPOWERURL, host.getHostName());
      context.info("Datapower URL: " + baseURL);
      fetcher.setURL(baseURL);
      fetcher.setMethod(URLFetcherConfig.METHOD_POST, null);
      fetcher.setPostType(ContentType.create("application/soap+xml", "UTF-8"));
    }
    return ok;
  }

  @Override
  public boolean runCheck() throws CommandException {
    context.info("Querying datapower baseURL: ", fetcher.getURL());
    boolean result = true;

    result &= somaCall("ConnectionsAccepted", connectionsAcceptedMetrics);
    result &= somaCall("CPUUsage", CPUUsageMetrics);
    result &= somaCall("DocumentCachingSummary", documentCachingSummary);
    result &= somaCall("StylesheetCachingSummary", stylesheetCachingSummary);
    result &= somaCall("EnvironmentalSensors", environmentalSensors);
    result &= somaCall("HTTPConnections", httpConnections);
    result &= somaCall("MemoryStatus", memoryStatus);
    result &= somaCall("FilesystemStatus", filesystemStatus);
    result &= somaCall("SystemUsage", systemUsage);
    result &= somaCall("TCPSummary", TCPSummary);
    result &= somaCall("ObjectStatus", objectStatus);
    result &= somaCall("EthernetInterfaceStatus", ethernetInterfaceStatus);
    result &= somaCall("StylesheetExecutions", stylesheetExecutions);
    result &= somaCall("DomainStatus", domainStatus);
    result &= somaCall("HTTPTransactions", httpTransactions);
    result &= somaCall("HTTPMeanTransactionTime", httpMeanTransactionTime);
    result &= somaCall("WSOperationMetricsSimpleIndex", wsOperationMetricsSimpleIndex);

    /* @formatter:off

    somaCall("ActiveUsers", null);
    somaCall("ARPStatus", null);
    somaCall("ConnectionsAccepted", null);
    somaCall("CPUUsage", null);
    somaCall("CryptoEngineStatus", null);
    somaCall("DateTimeStatus", null);
    somaCall("DNSCacheHostStatus", null);
    somaCall("DNSNameServerStatus", null);
    somaCall("DNSSearchDomainStatus", null);
    somaCall("DNSStaticHostStatus", null);
    somaCall("DocumentCachingSummary", null);
    somaCall("DocumentStatus", null);
    somaCall("DocumentStatusSimpleIndex", null);
    somaCall("DomainStatus", null);
    somaCall("DynamicQueueManager", null);
    somaCall("EnvironmentalFanSensors", null);
    somaCall("EnvironmentalSensors", null);
    somaCall("EthernetInterfaceStatus", null);
    somaCall("FilePollerStatus", null);
    somaCall("FilesystemStatus", null);
    somaCall("FirmwareStatus", null);
    somaCall("FirmwareVersion", null);
    somaCall("HSMKeyStatus", null);
    somaCall("HTTPConnections", null);
    somaCall("HTTPConnectionsCreated", null);
    somaCall("HTTPConnectionsDestroyed", null);
    somaCall("HTTPConnectionsOffered", null);
    somaCall("HTTPConnectionsRequested", null);
    somaCall("HTTPConnectionsReturned", null);
    somaCall("HTTPConnectionsReused", null);
    somaCall("HTTPMeanTransactionTime", null);
    somaCall("HTTPTransactions", null);
    somaCall("LibraryVersion", null);
    somaCall("LicenseStatus", null);
    somaCall("LoadBalancerStatus", null);
    somaCall("LogTargetStatus", null);
    somaCall("MemoryStatus", null);
    somaCall("MessageCounts", null);
    somaCall("MessageCountFilters", null);
    somaCall("MessageDurations", null);
    somaCall("MessageDurationFilters", null);
    somaCall("MessageSources", null);
    somaCall("MQQMstatus", null);
    somaCall("MQStatus", null);
    somaCall("NFSMountStatus", null);
    somaCall("NTPRefreshStatus", null);
    somaCall("ObjectStatus", null);
    somaCall("PortStatus", null);
    somaCall("ReceiveKbpsThroughput", null);
    somaCall("ReceivePacketThroughput", null);
    somaCall("RoutingStatus", null);
    somaCall("ServicesStatus", null);
    somaCall("SLMPeeringStatus", null);
    somaCall("SLMSummaryStatus", null);
    somaCall("SNMPStatus", null);
    somaCall("SSHTrustedHostStatus", null);
    somaCall("StandbyStatus", null);
    somaCall("StylesheetCachingSummary", null);
    somaCall("StylesheetExecutions", null);
    somaCall("StylesheetExecutionsSimpleIndex", null);
    somaCall("StylesheetMeanExecutionTime", null);
    somaCall("StylesheetMeanExecutionTimeSimpleIndex", null);
    somaCall("StylesheetProfiles", null);
    somaCall("StylesheetProfilesSimpleIndex", null);
    somaCall("StylesheetStatus", null);
    somaCall("StylesheetStatusSimpleIndex", null);
    somaCall("SystemUsage", null);
    somaCall("TCPSummary", null);
    somaCall("TCPTable", null);
    somaCall("TibcoEMSStatus", null);
    somaCall("TransmitKbpsThroughput", null);
    somaCall("TransmitPacketThroughput", null);
    somaCall("UDDISubscriptionKeyStatusSimpleIndex", null);
    somaCall("UDDISubscriptionServiceStatusSimpleIndex", null);
    somaCall("UDDISubscriptionStatusSimpleIndex", null);
    somaCall("Version", null);
    somaCall("WebAppFwAccepted", null);
    somaCall("WebAppFwRejected", null);
    somaCall("WebSphereJMSStatus", null);
    somaCall("WSMAgentSpoolers", null);
    somaCall("WSMAgentStatus", null);
    somaCall("WSOperationMetrics", null);
    somaCall("WSOperationsStatus", null);
    somaCall("WSRRSubscriptionServiceStatus", null);
    somaCall("WSRRSubscriptionStatus", null);
    somaCall("WSWSDLStatus", null);
    somaCall("WSWSDLStatusSimpleIndex", null);
    @formatter:on */

    return result;
  }

  public String callDPSOMAMethod(String SOMAMethod) throws CommandException {
    context.debug("SOMA call to " + SOMAMethod);
    String response = null;
    String mergedSOAPEnvelope = datapowerTemplate.replaceAll("@SOMAMONITORCLASS@", SOMAMethod);
    // connect
    try {
      context.debug("SOMA data: " + mergedSOAPEnvelope);
      fetcher.setPostData(mergedSOAPEnvelope);
      response = fetcher.execute();
    }
    catch (final URLFetcherException err) {
      return null;
    }
    if (!mServerReachable.hasValue()) {
      final int httpStatus = fetcher.httpStatusCode;
      final boolean ok = (httpStatus > 0) && (httpStatus < 400);
      mServerReachable.setValue(httpStatus > 0);
      mServerConnectionTimeout.setValue(fetcher.connectionTimedOut);
      mServerSocketTimeout.setValue(fetcher.socketTimedOut);
      mServerLatency.setValue(net.eiroca.library.core.Helper.elapsed(fetcher.firstResponseStartTime, fetcher.firstResponseEndTime));
      mServerResponseTime.setValue(net.eiroca.library.core.Helper.elapsed(fetcher.responseStartTime, fetcher.responseEndTime));
      mServerVerified.setValue(1.0);
      mServerResult.setValue(httpStatus);
      mServerStatus.setValue(!ok);
    }
    return response;
  }

  private boolean somaCall(String method, List<MetricDef> metrics) throws CommandException {
    String response = callDPSOMAMethod(method);
    if (response == null) {
      context.warn("Invalid SOMA response " + method + "-->" + fetcher.httpStatusCode);
      return false;
    }
    if (metrics == null) {
      response = response.replace('\n', ' ');
      response = response.replace('\r', ' ');
      context.info("SOMA " + method + "-->" + response);
    }
    else {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      Document doc = null;
      try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(response));
        doc = docBuilder.parse(is);
        doc.getDocumentElement().normalize();
      }
      catch (ParserConfigurationException | IOException | SAXException ex) {
        context.error("Parsing error " + ex);
      }
      NodeList measureList = doc.getElementsByTagName(method);
      context.debug("NodeList length: " + measureList.getLength());
      for (int i = 0; i < measureList.getLength(); i++) {
        Element element = (Element)measureList.item(i);
        for (MetricDef md : metrics) {
          try {
            if ((md.minAPIversion <= datapowerVersion) && (datapowerVersion <= md.maxAPIversion)) {
              double value;
              switch (md.type) {
                case 1:
                  String x = getValueStr(element, md.attribute);
                  value = LibStr.isEmptyOrNull(x) ? 0 : 1;
                  break;
                default:
                  value = getValue(element, md.attribute) * md.scale;
                  break;
              }
              if (value > 0) {
                if (md.splitName != null) {
                  String splitVal1 = getValueStr(element, md.splitKey1);
                  String splitVal2 = (md.splitKey2 != null) ? getValueStr(element, md.splitKey2) : null;
                  String splitVal = splitVal1 + (splitVal2 != null ? "_" + splitVal2 : "");
                  if (splitVal != null) md.measure.getSplitting(md.splitName, splitVal).addValue(value);
                }
                else {
                  md.measure.addValue(value);
                }
              }
            }
          }
          catch (Exception e) {
            context.error("Measure population failed, exception is: " + e + " element is: " + element + " and tagName is: " + md.attribute + " ");
          }
        }
      }
    }
    return true;
  }

  private String getValueStr(Element element, String attribute) {
    NodeList taglist = element.getElementsByTagName(attribute);
    if (taglist.getLength() > 0) {
      Element tag = (Element)taglist.item(0);
      return tag.getTextContent();
    }
    return null;
  }

  private double getValue(Element element, String attribute) {
    String value = getValueStr(element, attribute);
    if (value == null) return 0.0;
    double d = 0.0;
    try {
      d = Double.parseDouble(value);
    }
    catch (NumberFormatException e) {
      Double v = ALIAS.get(value);
      if (v != null) d = v;
    }
    return d;
  }

}
