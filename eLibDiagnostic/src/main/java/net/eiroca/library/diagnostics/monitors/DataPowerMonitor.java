/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;

public class DataPowerMonitor extends GenericHTTPMonitor {

  protected final static String CONFIG_DATAPOWERURL = "datapowerURL";
  protected final static String CONFIG_DATAPOWERTEMPLATE = "datapowerTemplate";
  protected final static String CONFIG_DATAPOWERVERSION = "datapowerVersion";

  protected final static double ONE_KIB = 1024;

  protected final MetricGroup mgDatapower = new MetricGroup(mgMonitor, "DataPower Statistics", "DataPower - {0}");

  protected final MetricGroup mgDatapowerAccepted = new MetricGroup(mgDatapower, "DataPower Accepted", "Accepted - {0}");
  protected final Measure mConnectionsAccepted_oneDay = mgDatapowerAccepted.createMeasure("oneDay", MetricAggregation.zero, "ConnectionsAccepted_oneDay", "counter");
  protected final Measure mConnectionsAccepted_oneHour = mgDatapowerAccepted.createMeasure("oneHour", MetricAggregation.zero, "ConnectionsAccepted_oneHour", "counter");
  protected final Measure mConnectionsAccepted_oneMinute = mgDatapowerAccepted.createMeasure("oneMinute", MetricAggregation.zero, "ConnectionsAccepted_oneMinute", "counter");
  protected final Measure mConnectionsAccepted_tenMinutes = mgDatapowerAccepted.createMeasure("tenMinutes", MetricAggregation.zero, "ConnectionsAccepted_tenMinutes", "counter");
  protected final Measure mConnectionsAccepted_tenSeconds = mgDatapowerAccepted.createMeasure("tenSeconds", MetricAggregation.zero, "ConnectionsAccepted_tenSeconds", "counter");
  //
  protected final MetricGroup mgCPUUsage = new MetricGroup(mgDatapower, "DataPower CPU Usage", "CPU Usage - {0}");
  protected final Measure mCPUUsage_oneDay = mgCPUUsage.createMeasure("oneDay", MetricAggregation.zero, "CPUUsage_oneDay", "percent");
  protected final Measure mCPUUsage_oneHour = mgCPUUsage.createMeasure("oneHour", MetricAggregation.zero, "CPUUsage_oneHour", "percent");
  protected final Measure mCPUUsage_oneMinute = mgCPUUsage.createMeasure("oneMinute", MetricAggregation.zero, "CPUUsage_oneMinute", "percent");
  protected final Measure mCPUUsage_tenMinutes = mgCPUUsage.createMeasure("tenMinutes", MetricAggregation.zero, "CPUUsage_tenMinutes", "percent");
  protected final Measure mCPUUsage_tenSeconds = mgCPUUsage.createMeasure("tenSeconds", MetricAggregation.zero, "CPUUsage_tenSeconds", "percent");
  //
  protected final MetricGroup mgDocumentCachingSummary = new MetricGroup(mgDatapower, "DataPower Document Caching", "Document Caching - {0}");
  protected final Measure mDocumentCachingSummary_CacheCount = mgDocumentCachingSummary.createMeasure("CacheCount", MetricAggregation.zero, "DocumentCachingSummary_CacheCount", "counter");
  protected final Measure mDocumentCachingSummary_DocCount = mgDocumentCachingSummary.createMeasure("DocCount", MetricAggregation.zero, "DocumentCachingSummary_DocCount", "counter");
  protected final Measure mDocumentCachingSummary_CacheSize = mgDocumentCachingSummary.createMeasure("CacheSize", MetricAggregation.zero, "DocumentCachingSummary_CacheSize", "bytes");
  protected final Measure mDocumentCachingSummary_ByteCount = mgDocumentCachingSummary.createMeasure("ByteCount", MetricAggregation.zero, "DocumentCachingSummary_ByteCount", "bytes");
  //
  protected final MetricGroup mgStylesheetCachingSummary = new MetricGroup(mgDatapower, "DataPower Stylesheet Caching", "Stylesheet Caching - {0}");
  protected final Measure mStylesheetCachingSummary_CacheCount = mgStylesheetCachingSummary.createMeasure("CacheCount", MetricAggregation.zero, "StylesheetCachingSummary_CacheCount", "counter");
  protected final Measure mStylesheetCachingSummary_ReadyCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_ReadyCount", MetricAggregation.zero, "StylesheetCachingSummary_ReadyCount", "counter");
  protected final Measure mStylesheetCachingSummary_CacheSize = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_CacheSize", MetricAggregation.zero, "StylesheetCachingSummary_CacheSize", "bytes");
  protected final Measure mStylesheetCachingSummary_PendingCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_PendingCount", MetricAggregation.zero, "StylesheetCachingSummary_PendingCount", "counter");
  protected final Measure mStylesheetCachingSummary_BadCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_BadCount", MetricAggregation.zero, "StylesheetCachingSummary_BadCount", "counter");
  protected final Measure mStylesheetCachingSummary_DupCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_DupCount", MetricAggregation.zero, "StylesheetCachingSummary_DupCount", "counter");
  protected final Measure mStylesheetCachingSummary_CacheKBCount = mgStylesheetCachingSummary.createMeasure("StylesheetCachingSummary_CacheKBCount", MetricAggregation.zero, "StylesheetCachingSummary_CacheKBCount", "counter");
  //
  protected final MetricGroup mgEnvironmentalSensors = new MetricGroup(mgDatapower, "DataPower Environmental Sensors", "Environmental Sensors - {0}");
  protected final Measure mEnvironmentalSensors_SystemTemp = mgEnvironmentalSensors.createMeasure("SystemTemp", MetricAggregation.zero, "EnvironmentalSensors_systemTemp", "unit");
  protected final Measure mEnvironmentalSensors_CPU1Temp = mgEnvironmentalSensors.createMeasure("CPU1Temp", MetricAggregation.zero, "EnvironmentalSensors_CPU1Temp", "unit");
  protected final Measure mEnvironmentalSensors_CPU2Temp = mgEnvironmentalSensors.createMeasure("CPU2Temp", MetricAggregation.zero, "EnvironmentalSensors_CPU2Temp", "unit");
  protected final Measure mEnvironmentalSensors_CPU1RPM = mgEnvironmentalSensors.createMeasure("CPU1RPM", MetricAggregation.zero, "EnvironmentalSensors_CPU1RPM", "unit");
  protected final Measure mEnvironmentalSensors_CPU2RPM = mgEnvironmentalSensors.createMeasure("CPU2RPM", MetricAggregation.zero, "EnvironmentalSensors_CPU2RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis1RPM = mgEnvironmentalSensors.createMeasure("Chassis1RPM", MetricAggregation.zero, "EnvironmentalSensors_Chassis1RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis2RPM = mgEnvironmentalSensors.createMeasure("Chassis2RPM", MetricAggregation.zero, "EnvironmentalSensors_Chassis2RPM", "unit");
  protected final Measure mEnvironmentalSensors_Chassis3RPM = mgEnvironmentalSensors.createMeasure("Chassis3RPM", MetricAggregation.zero, "EnvironmentalSensors_Chassis3RPM", "unit");
  protected final Measure mEnvironmentalSensors_CaseOpen = mgEnvironmentalSensors.createMeasure("CaseOpen", MetricAggregation.zero, "EnvironmentalSensors_CaseOpen", "unit");
  protected final Measure mEnvironmentalSensors_Volt33 = mgEnvironmentalSensors.createMeasure("Volt33", MetricAggregation.zero, "EnvironmentalSensors_Volt33", "unit");
  protected final Measure mEnvironmentalSensors_Volt5 = mgEnvironmentalSensors.createMeasure("Volt5", MetricAggregation.zero, "EnvironmentalSensors_Volt5", "unit");
  protected final Measure mEnvironmentalSensors_Volt12 = mgEnvironmentalSensors.createMeasure("Volt12", MetricAggregation.zero, "EnvironmentalSensors_Volt12", "unit");
  protected final Measure mEnvironmentalSensors_PowerSupplyOK = mgEnvironmentalSensors.createMeasure("PowerSupplyOk", MetricAggregation.zero, "EnvironmentalSensors_PowerSupplyOk", "unit");
  //
  protected final MetricGroup mgHTTPConnections = new MetricGroup(mgDatapower, "DataPower HTTP Connections", "HTTP Connections - {0}");
  protected final Measure mHTTPConnections_ReqTenSec = mgHTTPConnections.createMeasure("ReqTenSec", MetricAggregation.zero, "HTTPConnections_ReqTenSec", "unit");
  protected final Measure mHTTPConnections_ReqSec = mgHTTPConnections.createMeasure("ReqSec", MetricAggregation.zero, "HTTPConnections_ReqSec", "unit");
  protected final Measure mHTTPConnections_ReqOneMin = mgHTTPConnections.createMeasure("ReqOneMin", MetricAggregation.zero, "HTTPConnections_ReqOneMin", "unit");
  protected final Measure mHTTPConnections_ReqTenMin = mgHTTPConnections.createMeasure("ReqTenMin", MetricAggregation.zero, "HTTPConnections_ReqTenMin", "unit");
  protected final Measure mHTTPConnections_ReqOneHour = mgHTTPConnections.createMeasure("ReqOneHour", MetricAggregation.zero, "HTTPConnections_ReqOneHour", "unit");
  protected final Measure mHTTPConnections_ReqOneDay = mgHTTPConnections.createMeasure("ReqOneDay", MetricAggregation.zero, "HTTPConnections_ReqOneDay", "unit");
  protected final Measure mHTTPConnections_ReuseTenSec = mgHTTPConnections.createMeasure("ReuseTenSec", MetricAggregation.zero, "HTTPConnections_ReuseTenSec", "unit");
  protected final Measure mHTTPConnections_ReuseSec = mgHTTPConnections.createMeasure("ReuseSec", MetricAggregation.zero, "HTTPConnections_ReuseSec", "unit");
  protected final Measure mHTTPConnections_ReuseOneMin = mgHTTPConnections.createMeasure("ReuseOneMin", MetricAggregation.zero, "HTTPConnections_ReuseOneMin", "unit");
  protected final Measure mHTTPConnections_ReuseTenMin = mgHTTPConnections.createMeasure("ReuseTenMin", MetricAggregation.zero, "HTTPConnections_ReuseTenMin", "unit");
  protected final Measure mHTTPConnections_ReuseOneHour = mgHTTPConnections.createMeasure("ReuseOneHour", MetricAggregation.zero, "HTTPConnections_ReuseOneHour", "unit");
  protected final Measure mHTTPConnections_ReuseOneDay = mgHTTPConnections.createMeasure("ReuseOneDay", MetricAggregation.zero, "HTTPConnections_ReuseOneDay", "unit");
  protected final Measure mHTTPConnections_CreateTenSec = mgHTTPConnections.createMeasure("CreateTenSec", MetricAggregation.zero, "HTTPConnections_CreateTenSec", "unit");
  protected final Measure mHTTPConnections_CreateSec = mgHTTPConnections.createMeasure("CreateSec", MetricAggregation.zero, "HTTPConnections_CreateSec", "unit");
  protected final Measure mHTTPConnections_CreateOneMin = mgHTTPConnections.createMeasure("CreateOneMin", MetricAggregation.zero, "HTTPConnections_CreateOneMin", "unit");
  protected final Measure mHTTPConnections_CreateTenMin = mgHTTPConnections.createMeasure("CreateTenMin", MetricAggregation.zero, "HTTPConnections_CreateTenMin", "unit");
  protected final Measure mHTTPConnections_CreateOneHour = mgHTTPConnections.createMeasure("CreateOneHour", MetricAggregation.zero, "HTTPConnections_CreateOneHour", "unit");
  protected final Measure mHTTPConnections_CreateOneDay = mgHTTPConnections.createMeasure("CreateOneDay", MetricAggregation.zero, "HTTPConnections_CreateOneDay", "unit");
  protected final Measure mHTTPConnections_ReturnTenSec = mgHTTPConnections.createMeasure("ReturnTenSec", MetricAggregation.zero, "HTTPConnections_ReturnTenSec", "unit");
  protected final Measure mHTTPConnections_ReturnSec = mgHTTPConnections.createMeasure("ReturnSec", MetricAggregation.zero, "HTTPConnections_ReturnSec", "unit");
  protected final Measure mHTTPConnections_ReturnOneMin = mgHTTPConnections.createMeasure("ReturnOneMin", MetricAggregation.zero, "HTTPConnections_ReturnOneMin", "unit");
  protected final Measure mHTTPConnections_ReturnTenMin = mgHTTPConnections.createMeasure("ReturnTenMin", MetricAggregation.zero, "HTTPConnections_ReturnTenMin", "unit");
  protected final Measure mHTTPConnections_ReturnOneHour = mgHTTPConnections.createMeasure("ReturnOneHour", MetricAggregation.zero, "HTTPConnections_ReturnOneHour", "unit");
  protected final Measure mHTTPConnections_ReturnOneDay = mgHTTPConnections.createMeasure("ReturnOneDay", MetricAggregation.zero, "HTTPConnections_ReturnOneDay", "unit");
  protected final Measure mHTTPConnections_OfferTenSec = mgHTTPConnections.createMeasure("OfferTenSec", MetricAggregation.zero, "HTTPConnections_OfferTenSec", "unit");
  protected final Measure mHTTPConnections_OfferSec = mgHTTPConnections.createMeasure("OfferSec", MetricAggregation.zero, "HTTPConnections_OfferSec", "unit");
  protected final Measure mHTTPConnections_OfferOneMin = mgHTTPConnections.createMeasure("OfferOneMin", MetricAggregation.zero, "HTTPConnections_OfferOneMin", "unit");
  protected final Measure mHTTPConnections_OfferTenMin = mgHTTPConnections.createMeasure("OfferTenMin", MetricAggregation.zero, "HTTPConnections_OfferTenMin", "unit");
  protected final Measure mHTTPConnections_OfferOneHour = mgHTTPConnections.createMeasure("OfferOneHour", MetricAggregation.zero, "HTTPConnections_OfferOneHour", "unit");
  protected final Measure mHTTPConnections_OfferOneDay = mgHTTPConnections.createMeasure("OfferOneDay", MetricAggregation.zero, "HTTPConnections_OfferOneDay", "unit");
  protected final Measure mHTTPConnections_DestroyTenSec = mgHTTPConnections.createMeasure("DestroyTenSec", MetricAggregation.zero, "HTTPConnections_DestroyTenSec", "unit");
  protected final Measure mHTTPConnections_DestroySec = mgHTTPConnections.createMeasure("DestroySec", MetricAggregation.zero, "HTTPConnections_DestroySec", "unit");
  protected final Measure mHTTPConnections_DestroyOneMin = mgHTTPConnections.createMeasure("DestroyOneMin", MetricAggregation.zero, "HTTPConnections_DestroyOneMin", "unit");
  protected final Measure mHTTPConnections_DestroyTenMin = mgHTTPConnections.createMeasure("DestroyTenMin", MetricAggregation.zero, "HTTPConnections_DestroyTenMin", "unit");
  protected final Measure mHTTPConnections_DestroyOneHour = mgHTTPConnections.createMeasure("DestroyOneHour", MetricAggregation.zero, "HTTPConnections_DestroyOneHour", "unit");
  protected final Measure mHTTPConnections_DestroyOneDay = mgHTTPConnections.createMeasure("DestroyOneDay", MetricAggregation.zero, "HTTPConnections_DestroyOneDay", "unit");
  //
  protected final MetricGroup mgMemoryStatus = new MetricGroup(mgDatapower, "DataPower Memory Status", "Memory Status - {0}");
  protected final Measure mMemoryStatus_FreeMemory = mgMemoryStatus.createMeasure("FreeMemory", MetricAggregation.zero, "MemoryStatus_FreeMemory", "kilobytes");
  protected final Measure mMemoryStatus_HoldMemory = mgMemoryStatus.createMeasure("HoldMemory", MetricAggregation.zero, "MemoryStatus_HoldMemory", "kilobytes");
  protected final Measure mMemoryStatus_ReqMemory = mgMemoryStatus.createMeasure("ReqMemory", MetricAggregation.zero, "MemoryStatus_ReqMemory", "kilobytes");
  protected final Measure mMemoryStatus_TotalMemory = mgMemoryStatus.createMeasure("TotalMemory", MetricAggregation.zero, "MemoryStatus_TotalMemory", "kilobytes");
  protected final Measure mMemoryStatus_UsedMemory = mgMemoryStatus.createMeasure("UsedMemory", MetricAggregation.zero, "MemoryStatus_UsedMemory", "kilobytes");
  protected final Measure mMemoryStatus_Usage = mgMemoryStatus.createMeasure("Usage", MetricAggregation.zero, "MemoryStatus_Usage", "unit");
  //
  protected final MetricGroup mgFileSystemStatus = new MetricGroup(mgDatapower, "DataPower FileSystem Status", "FileSystem Status - {0}");
  protected final Measure mFileSystemStatus_FreeEncrypted = mgFileSystemStatus.createMeasure("FreeEncrypted", MetricAggregation.zero, "FileSystemStatus_FreeEncrypted", "unit");
  protected final Measure mFileSystemStatus_TotalEncrypted = mgFileSystemStatus.createMeasure("TotalEncrypted", MetricAggregation.zero, "FileSystemStatus_TotalEncrypted", "unit");
  protected final Measure mFileSystemStatus_FreeTemporary = mgFileSystemStatus.createMeasure("FreeTemporary", MetricAggregation.zero, "FileSystemStatus_FreeTemporary", "unit");
  protected final Measure mFileSystemStatus_TotalTemporary = mgFileSystemStatus.createMeasure("TotalTemporary", MetricAggregation.zero, "FileSystemStatus_TotalTemporary", "unit");
  protected final Measure mFileSystemStatus_FreeInternal = mgFileSystemStatus.createMeasure("FreeInternal", MetricAggregation.zero, "FileSystemStatus_FreeInternal", "unit");
  protected final Measure mFileSystemStatus_TotalInternal = mgFileSystemStatus.createMeasure("TotalInternal", MetricAggregation.zero, "FileSystemStatus_TotalInternal", "unit");
  //
  protected final MetricGroup mgSystemUsage = new MetricGroup(mgDatapower, "DataPower System Usage", "System Usage - {0}");
  protected final Measure mSystemUsage_Interval = mgSystemUsage.createMeasure("Interval", MetricAggregation.zero, "SystemUsage_Interval", "unit");
  protected final Measure mSystemUsage_Load = mgSystemUsage.createMeasure("Load", MetricAggregation.zero, "SystemUsage_Load", "unit");
  protected final Measure mSystemUsage_WorkList = mgSystemUsage.createMeasure("WorkList", MetricAggregation.zero, "SystemUsage_WorkList", "unit");
  //
  protected final MetricGroup mgTCPSummary = new MetricGroup(mgDatapower, "DataPower TCP Summary", "TCP Summary - {0}");
  protected final Measure mTCPSummary_Established = mgTCPSummary.createMeasure("Established", MetricAggregation.zero, "TCPSummary_Established", "unit");
  protected final Measure mTCPSummary_Syn_Sent = mgTCPSummary.createMeasure("Syn_Sent", MetricAggregation.zero, "TCPSummary_Syn_Sent", "unit");
  protected final Measure mTCPSummary_Syn_Received = mgTCPSummary.createMeasure("Syn_Received", MetricAggregation.zero, "TCPSummary_Syn_Received", "unit");
  protected final Measure mTCPSummary_Fin_Wait_1 = mgTCPSummary.createMeasure("Fin_Wait_1", MetricAggregation.zero, "TCPSummary_Fin_Wait_1", "unit");
  protected final Measure mTCPSummary_Fin_Wait_2 = mgTCPSummary.createMeasure("Fin_Wait_2", MetricAggregation.zero, "TCPSummary_Fin_Wait_2", "unit");
  protected final Measure mTCPSummary_Time_Wait = mgTCPSummary.createMeasure("Time_Wait", MetricAggregation.zero, "TCPSummary_Time_Wait", "unit");
  protected final Measure mTCPSummary_Closed = mgTCPSummary.createMeasure("Closed", MetricAggregation.zero, "TCPSummary_Closed", "unit");
  protected final Measure mTCPSummary_Close_Wait = mgTCPSummary.createMeasure("Close_Wait", MetricAggregation.zero, "TCPSummary_Close_Wait", "unit");
  protected final Measure mTCPSummary_Last_Ack = mgTCPSummary.createMeasure("Last_Ack", MetricAggregation.zero, "TCPSummary_Last_Ack", "unit");
  protected final Measure mTCPSummary_Listen = mgTCPSummary.createMeasure("Listen", MetricAggregation.zero, "TCPSummary_Listen", "unit");
  protected final Measure mTCPSummary_Closing = mgTCPSummary.createMeasure("Closing", MetricAggregation.zero, "TCPSummary_Closing", "unit");
  //
  protected final MetricGroup mgEthernetInterfaceStatus = new MetricGroup(mgDatapower, "DataPower Ethernet Interface Status", "Interface Status - {0}");
  protected final Measure mEthernetInterfaceStatus_Status = mgEthernetInterfaceStatus.createMeasure("Status", MetricAggregation.zero, "EthernetInterfaceStatus_Status", "unit");
  protected final Measure mEthernetInterfaceStatus_Collisions = mgEthernetInterfaceStatus.createMeasure("Collisions", MetricAggregation.zero, "EthernetInterfaceStatus_Collisions", "unit");
  protected final Measure mEthernetInterfaceStatus_Collisions2 = mgEthernetInterfaceStatus.createMeasure("Collisions2", MetricAggregation.zero, "EthernetInterfaceStatus_Collisions2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCPackets = mgEthernetInterfaceStatus.createMeasure("RxHCPackets", MetricAggregation.zero, "EthernetInterfaceStatus_RxHCPackets", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCPackets2 = mgEthernetInterfaceStatus.createMeasure("RxHCPackets2", MetricAggregation.zero, "EthernetInterfaceStatus_RxHCPackets2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxHCBytes = mgEthernetInterfaceStatus.createMeasure("RxHCBytes", MetricAggregation.zero, "EthernetInterfaceStatus_RxHCBytes", "bytes");
  protected final Measure mEthernetInterfaceStatus_RxHCBytes2 = mgEthernetInterfaceStatus.createMeasure("RxHCBytes2", MetricAggregation.zero, "EthernetInterfaceStatus_RxHCBytes2", "bytes");
  protected final Measure mEthernetInterfaceStatus_RxErrors = mgEthernetInterfaceStatus.createMeasure("RxErrors", MetricAggregation.zero, "EthernetInterfaceStatus_RxErrors", "unit");
  protected final Measure mEthernetInterfaceStatus_RxErrors2 = mgEthernetInterfaceStatus.createMeasure("RxErrors2", MetricAggregation.zero, "EthernetInterfaceStatus_RxErrors2", "unit");
  protected final Measure mEthernetInterfaceStatus_RxDrops = mgEthernetInterfaceStatus.createMeasure("RxDrops", MetricAggregation.zero, "EthernetInterfaceStatus_RxDrops", "unit");
  protected final Measure mEthernetInterfaceStatus_RxDrops2 = mgEthernetInterfaceStatus.createMeasure("RxDrops2", MetricAggregation.zero, "EthernetInterfaceStatus_RxDrops2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCPackets = mgEthernetInterfaceStatus.createMeasure("TxHCPackets", MetricAggregation.zero, "EthernetInterfaceStatus_TxHCPackets", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCPackets2 = mgEthernetInterfaceStatus.createMeasure("TxHCPackets2", MetricAggregation.zero, "EthernetInterfaceStatus_TxHCPackets2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxHCBytes = mgEthernetInterfaceStatus.createMeasure("TxHCBytes", MetricAggregation.zero, "EthernetInterfaceStatus_TxHCBytes", "bytes");
  protected final Measure mEthernetInterfaceStatus_TxHCBytes2 = mgEthernetInterfaceStatus.createMeasure("TxHCBytes2", MetricAggregation.zero, "EthernetInterfaceStatus_TxHCBytes2", "bytes");
  protected final Measure mEthernetInterfaceStatus_TxErrors = mgEthernetInterfaceStatus.createMeasure("TxErrors", MetricAggregation.zero, "EthernetInterfaceStatus_TxErrors", "unit");
  protected final Measure mEthernetInterfaceStatus_TxErrors2 = mgEthernetInterfaceStatus.createMeasure("TxErrors2", MetricAggregation.zero, "EthernetInterfaceStatus_TxErrors2", "unit");
  protected final Measure mEthernetInterfaceStatus_TxDrops = mgEthernetInterfaceStatus.createMeasure("TxDrops", MetricAggregation.zero, "EthernetInterfaceStatus_TxDrops", "unit");
  protected final Measure mEthernetInterfaceStatus_TxDrops2 = mgEthernetInterfaceStatus.createMeasure("TxDrops2", MetricAggregation.zero, "EthernetInterfaceStatus_TxDrops2", "unit");
  //
  protected final MetricGroup mgObjectStatus = new MetricGroup(mgDatapower, "DataPower Object Status", "Object Status - {0}");
  protected final Measure mObjectStatus_OpState = mgObjectStatus.createMeasure("OpState", MetricAggregation.zero, "ObjectStatus_OpState", "unit");
  protected final Measure mObjectStatus_AdminState = mgObjectStatus.createMeasure("AdminState", MetricAggregation.zero, "ObjectStatus_AdminState", "unit");
  //
  protected final MetricGroup mgStylesheetExecutions = new MetricGroup(mgDatapower, "DataPower Stylesheet Executions", "Stylesheet Executions - {0}");
  protected final Measure mStylesheetExecutions_TenSeconds = mgStylesheetExecutions.createMeasure("TenSeconds", MetricAggregation.zero, "StylesheetExecutions_TenSeconds", "unit");
  protected final Measure mStylesheetExecutions_OneMinute = mgStylesheetExecutions.createMeasure("OneMinute", MetricAggregation.zero, "StylesheetExecutions_OneMinute", "unit");
  protected final Measure mStylesheetExecutions_TenMinutes = mgStylesheetExecutions.createMeasure("TenMinutes", MetricAggregation.zero, "StylesheetExecutions_TenMinutes", "unit");
  protected final Measure mStylesheetExecutions_OneHour = mgStylesheetExecutions.createMeasure("OneHour", MetricAggregation.zero, "StylesheetExecutions_OneHour", "unit");
  protected final Measure mStylesheetExecutions_OneDay = mgStylesheetExecutions.createMeasure("OneDay", MetricAggregation.zero, "StylesheetExecutions_OneDay", "unit");
  //
  protected final MetricGroup mgDomainStatus = new MetricGroup(mgDatapower, "DataPower Domain Status", "Domain Status - {0}");
  protected final Measure mDomainStatus_SaveNeeded = mgDomainStatus.createMeasure("SaveNeeded", MetricAggregation.zero, "DomainStatus_SaveNeeded", "unit");
  protected final Measure mDomainStatus_TraceEnabled = mgDomainStatus.createMeasure("TraceEnabled", MetricAggregation.zero, "DomainStatus_TraceEnabled", "unit");
  protected final Measure mDomainStatus_DebugEnabled = mgDomainStatus.createMeasure("DebugEnabled", MetricAggregation.zero, "DomainStatus_DebugEnabled", "unit");
  protected final Measure mDomainStatus_ProbeEnabled = mgDomainStatus.createMeasure("ProbeEnabled", MetricAggregation.zero, "DomainStatus_ProbeEnabled", "unit");
  protected final Measure mDomainStatus_DiagEnabled = mgDomainStatus.createMeasure("DiagEnabled", MetricAggregation.zero, "DomainStatus_DiagEnabled", "unit");
  //
  protected final MetricGroup mgHTTPTransactions = new MetricGroup(mgDatapower, "DataPower HTTP Transactions", "HTTP Transactions - {0}");
  protected final Measure mHTTPTransactions_TenSeconds = mgHTTPTransactions.createMeasure("TenSeconds", MetricAggregation.zero, "HTTPTransactions_TenSeconds", "unit");
  protected final Measure mHTTPTransactions_OneMinute = mgHTTPTransactions.createMeasure("OneMinute", MetricAggregation.zero, "HTTPTransactions_OneMinute", "unit");
  protected final Measure mHTTPTransactions_TenMinutes = mgHTTPTransactions.createMeasure("TenMinutes", MetricAggregation.zero, "HTTPTransactions_TenMinutes", "unit");
  protected final Measure mHTTPTransactions_OneHour = mgHTTPTransactions.createMeasure("OneHour", MetricAggregation.zero, "HTTPTransactions_OneHour", "unit");
  protected final Measure mHTTPTransactions_OneDay = mgHTTPTransactions.createMeasure("OneDay", MetricAggregation.zero, "HTTPTransactions_OneDay", "unit");
  //
  protected final MetricGroup mgHTTPMeanTransactionTime = new MetricGroup(mgDatapower, "DataPower HTTP Mean Transaction Time", "HTTP Mean Transaction Time - {0}");
  protected final Measure mHTTPMeanTransactionTime_TenSeconds = mgHTTPMeanTransactionTime.createMeasure("TenSeconds", MetricAggregation.zero, "HTTPMeanTransactionTime_TenSeconds", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneMinute = mgHTTPMeanTransactionTime.createMeasure("OneMinute", MetricAggregation.zero, "HTTPMeanTransactionTime_OneMinute", "ms");
  protected final Measure mHTTPMeanTransactionTime_TenMinutes = mgHTTPMeanTransactionTime.createMeasure("TenMinutes", MetricAggregation.zero, "HTTPMeanTransactionTime_TenMinutes", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneHour = mgHTTPMeanTransactionTime.createMeasure("OneHour", MetricAggregation.zero, "HTTPMeanTransactionTime_OneHour", "ms");
  protected final Measure mHTTPMeanTransactionTime_OneDay = mgHTTPMeanTransactionTime.createMeasure("OneDay", MetricAggregation.zero, "HTTPMeanTransactionTime_OneDay", "ms");
  //
  protected final MetricGroup mgWSOperationMetricsSimpleIndex = new MetricGroup(mgDatapower, "DataPower WS Operation Metrics", "WS Operation Metrics - {0}");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfRequests", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_NumberOfRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfFailedRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfFailedRequests", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_NumberOfFailedRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests = mgWSOperationMetricsSimpleIndex.createMeasure("NumberOfSuccessfulRequests", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests", "unit");
  protected final Measure mWSOperationMetricsSimpleIndex_ServiceTime = mgWSOperationMetricsSimpleIndex.createMeasure("ServiceTime", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_ServiceTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxResponseTime = mgWSOperationMetricsSimpleIndex.createMeasure("MaxResponseTime", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_MaxResponseTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_LastResponseTime = mgWSOperationMetricsSimpleIndex.createMeasure("LastResponseTime", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_LastResponseTime", "ms");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxRequestSize = mgWSOperationMetricsSimpleIndex.createMeasure("MaxRequestSize", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_MaxRequestSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_LastRequestSize = mgWSOperationMetricsSimpleIndex.createMeasure("LastRequestSize", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_LastRequestSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_MaxResponseSize = mgWSOperationMetricsSimpleIndex.createMeasure("MaxResponseSize", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_MaxResponseSize", "bytes");
  protected final Measure mWSOperationMetricsSimpleIndex_LastResponseSize = mgWSOperationMetricsSimpleIndex.createMeasure("LastResponseSize", MetricAggregation.zero, "WSOperationMetricsSimpleIndex_LastResponseSize", "bytes");

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

    public MetricDef(final Measure measure, final String attribute) {
      super();
      this.measure = measure;
      this.attribute = attribute;
    }

    public MetricDef minAPIversion(final int minAPIversion) {
      this.minAPIversion = minAPIversion;
      maxAPIversion = Integer.MAX_VALUE;
      return this;
    }

    public MetricDef maxAPIversion(final int maxAPIversion) {
      minAPIversion = 0;
      this.maxAPIversion = maxAPIversion;
      return this;
    }

    public MetricDef scale(final double scale) {
      this.scale = scale;
      return this;
    }

    public MetricDef split(final String splitName, final String key1, final String key2) {
      this.splitName = splitName;
      splitKey1 = key1;
      splitKey2 = key2;
      return this;
    }

    public MetricDef split(final String splitName, final String key) {
      this.splitName = splitName;
      splitKey1 = key;
      splitKey2 = null;
      return this;
    }

    public MetricDef type(final int type) {
      this.type = type;
      return this;
    }

  }

  private static Map<String, Double> ALIAS = new HashMap<>();
  static {
    DataPowerMonitor.ALIAS.put("yes", 1.0);
    DataPowerMonitor.ALIAS.put("ok", 1.0);
    DataPowerMonitor.ALIAS.put("on", 1.0);
    DataPowerMonitor.ALIAS.put("up", 1.0);
    DataPowerMonitor.ALIAS.put("enabled", 1.0);
  }

  private final List<MetricDef> connectionsAcceptedMetrics = new ArrayList<>();
  private final List<MetricDef> CPUUsageMetrics = new ArrayList<>();
  private final List<MetricDef> documentCachingSummary = new ArrayList<>();
  private final List<MetricDef> stylesheetCachingSummary = new ArrayList<>();
  private final List<MetricDef> environmentalSensors = new ArrayList<>();
  private final List<MetricDef> httpConnections = new ArrayList<>();
  private final List<MetricDef> memoryStatus = new ArrayList<>();
  private final List<MetricDef> filesystemStatus = new ArrayList<>();
  private final List<MetricDef> systemUsage = new ArrayList<>();
  private final List<MetricDef> TCPSummary = new ArrayList<>();
  private final List<MetricDef> objectStatus = new ArrayList<>();
  private final List<MetricDef> ethernetInterfaceStatus = new ArrayList<>();
  private final List<MetricDef> stylesheetExecutions = new ArrayList<>();
  private final List<MetricDef> domainStatus = new ArrayList<>();
  private final List<MetricDef> httpTransactions = new ArrayList<>();
  private final List<MetricDef> httpMeanTransactionTime = new ArrayList<>();
  private final List<MetricDef> wsOperationMetricsSimpleIndex = new ArrayList<>();

  public DataPowerMonitor() {
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
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_CacheSize, "CacheSizeKiB").minAPIversion(7).scale(DataPowerMonitor.ONE_KIB).split("XMLManager", "XMLManager"));
    documentCachingSummary.add(new MetricDef(mDocumentCachingSummary_ByteCount, "KiByteCount").minAPIversion(7).scale(DataPowerMonitor.ONE_KIB).split("XMLManager", "XMLManager"));
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
  public void readConf() throws CommandException {
    super.readConf();
    datapowerTemplate = context.getConfigString(DataPowerMonitor.CONFIG_DATAPOWERTEMPLATE, null);
    datapowerVersion = context.getConfigInt(DataPowerMonitor.CONFIG_DATAPOWERVERSION, 7);
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      final URL baseURL = getURL(DataPowerMonitor.CONFIG_DATAPOWERURL, host.getHostName());
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
    final boolean result = true;

    somaCall("ConnectionsAccepted", connectionsAcceptedMetrics);
    somaCall("CPUUsage", CPUUsageMetrics);
    somaCall("DocumentCachingSummary", documentCachingSummary);
    somaCall("StylesheetCachingSummary", stylesheetCachingSummary);
    somaCall("EnvironmentalSensors", environmentalSensors);
    somaCall("HTTPConnections", httpConnections);
    somaCall("MemoryStatus", memoryStatus);
    somaCall("FilesystemStatus", filesystemStatus);
    somaCall("SystemUsage", systemUsage);
    somaCall("TCPSummary", TCPSummary);
    somaCall("ObjectStatus", objectStatus);
    somaCall("EthernetInterfaceStatus", ethernetInterfaceStatus);
    somaCall("StylesheetExecutions", stylesheetExecutions);
    somaCall("DomainStatus", domainStatus);
    somaCall("HTTPTransactions", httpTransactions);
    somaCall("HTTPMeanTransactionTime", httpMeanTransactionTime);
    somaCall("WSOperationMetricsSimpleIndex", wsOperationMetricsSimpleIndex);

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

  public String callDPSOMAMethod(final String SOMAMethod) throws CommandException {
    context.debug("SOMA call to " + SOMAMethod);
    String response = null;
    final String mergedSOAPEnvelope = datapowerTemplate.replaceAll("@SOMAMONITORCLASS@", SOMAMethod);
    // connect
    try {
      context.debug("SOMA URL: " + fetcher.getURL());
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

  private boolean somaCall(final String method, final List<MetricDef> metrics) throws CommandException {
    String response = callDPSOMAMethod(method);
    if (response == null) {
      context.warn("Invalid SOMA response " + method + " RC=" + fetcher.httpStatusCode);
      return false;
    }
    if (metrics == null) {
      response = response.replace('\n', ' ');
      response = response.replace('\r', ' ');
      context.info("SOMA " + method + " -> " + response);
    }
    else {
      final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      Document doc = null;
      try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
        final InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(response));
        doc = docBuilder.parse(is);
        doc.getDocumentElement().normalize();
      }
      catch (ParserConfigurationException | IOException | SAXException ex) {
        context.error("Parsing error " + ex);
      }
      final NodeList measureList = doc.getElementsByTagName(method);
      context.debug("NodeList length: " + measureList.getLength());
      for (int i = 0; i < measureList.getLength(); i++) {
        final Element element = (Element)measureList.item(i);
        for (final MetricDef md : metrics) {
          try {
            if ((md.minAPIversion <= datapowerVersion) && (datapowerVersion <= md.maxAPIversion)) {
              double value;
              switch (md.type) {
                case 1:
                  final String x = getValueStr(element, md.attribute);
                  value = LibStr.isEmptyOrNull(x) ? 0 : 1;
                  break;
                default:
                  value = getValue(element, md.attribute) * md.scale;
                  break;
              }
              if (value > 0) {
                if (md.splitName != null) {
                  final String splitVal1 = getValueStr(element, md.splitKey1);
                  final String splitVal2 = (md.splitKey2 != null) ? getValueStr(element, md.splitKey2) : null;
                  final String splitVal = splitVal1 + (splitVal2 != null ? "_" + splitVal2 : "");
                  if (splitVal != null) {
                    md.measure.getSplitting(md.splitName, splitVal).addValue(value);
                  }
                }
                else {
                  md.measure.addValue(value);
                }
              }
            }
          }
          catch (final Exception e) {
            context.error("Measure population failed, exception is: " + e + " element is: " + element + " and tagName is: " + md.attribute + " ");
          }
        }
      }
    }
    return true;
  }

  private String getValueStr(final Element element, final String attribute) {
    final NodeList taglist = element.getElementsByTagName(attribute);
    if (taglist.getLength() > 0) {
      final Element tag = (Element)taglist.item(0);
      return tag.getTextContent();
    }
    return null;
  }

  private double getValue(final Element element, final String attribute) {
    final String value = getValueStr(element, attribute);
    if (value == null) { return 0.0; }
    double d = 0.0;
    try {
      d = Double.parseDouble(value);
    }
    catch (final NumberFormatException e) {
      final Double v = DataPowerMonitor.ALIAS.get(value);
      if (v != null) {
        d = v;
      }
    }
    return d;
  }

}
