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
package net.eiroca.library.diagnostics.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.eiroca.library.config.parameter.ListParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IAction;
import net.eiroca.library.diagnostics.IConverter;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.diagnostics.ITask;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.converters.BaseConverter;
import net.eiroca.library.diagnostics.converters.ReplaceSpaceConverter;
import net.eiroca.library.diagnostics.converters.URLEncoderConverter;
import net.eiroca.library.diagnostics.converters.UnixScriptConverter;
import net.eiroca.library.diagnostics.converters.WindowsScriptConverter;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.diagnostics.validators.GenericValidator;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;

public abstract class BaseAction implements IAction<ActionData, ReturnObject>, IServerMonitor, ITask {

  private static Map<String, Class<? extends BaseConverter>> converters = new HashMap<>();
  static {
    BaseAction.converters.put("NULL removal", BaseConverter.class);
    BaseAction.converters.put("Unix Script", UnixScriptConverter.class);
    BaseAction.converters.put("Windows commands", WindowsScriptConverter.class);
    BaseAction.converters.put("URL encoder", URLEncoderConverter.class);
    BaseAction.converters.put("Replace space", ReplaceSpaceConverter.class);
  }

  // measurement variables
  public MetricGroup mgResult = new MetricGroup("Generic Execution Monitor");
  protected Measure mStatus = mgResult.createMeasure("Status", "Exection status (0 OK)", "boolean");
  protected Measure mResult = mgResult.createMeasure("Result", "Output result", "number");
  protected Measure mVerified = mgResult.createMeasure("ContentVerified", "1 if result is validated and valid", "boolean");

  public static final String SPLIT_GROUP = "Values";

  // Config
  protected ContextParameters params = new ContextParameters();
  final StringParameter pConverter = new StringParameter(params, "converter", "NULL removal", true, false);
  final StringParameter pParameter = new StringParameter(params, "param", null, false, true);
  final StringParameter pMetricPrefix = new StringParameter(params, "metricPrefix", "***ReturnedMeasures:", false, false);
  final ListParameter pMetricNames = new ListParameter(params, "metricNames", Helper.LISTSEPERATOR, null, false, true);

  protected IContext context;
  protected GenericValidator validator;
  protected IConverter converter;

  @Override
  public void setup(final IContext context) throws CommandException {
    this.context = context;
    params.loadConfig(context, null);
    context.info(params);
    validator = new GenericValidator();
    validator.setup(context);
    final String converterName = pConverter.get();
    if (BaseAction.converters.containsKey(converterName)) {
      try {
        converter = BaseAction.converters.get(converterName).newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        converter = new BaseConverter();
      }
    }
    else {
      converter = new BaseConverter();
    }
  }

  @Override
  public void resetMetrics() {
  }

  @Override
  public void close() throws Exception {
    Helper.close(validator);
    validator = null;
  }

  @Override
  public boolean check(final String target) throws CommandException {
    final ActionData data = new ActionData();
    data.set(ActionData.HOST, target);
    final ReturnObject result = execute(data);
    return result != null;
  }

  @Override
  public boolean execute() throws CommandException {
    final ReturnObject result = execute(null);
    return result != null;
  }

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    groups.add(mgResult);
  }

  protected void processMetrics(final ReturnObject result, final String rowSep, final String colSep) {
    if (result == null) { return; }
    final String output = result.getOutput();
    boolean isValid;
    try {
      isValid = (output != null) ? validator.isValid(output) : false;
    }
    catch (final CommandException e) {
      isValid = false;
      context.warn("Validation error: " + Helper.getExceptionAsString(e));
    }
    mVerified.setValue(isValid);
    final String prefix = pMetricPrefix.get();
    String record = null;
    final String[] records = output.trim().split(rowSep);
    for (final String r : records) {
      if (r.startsWith(prefix)) {
        record = r.substring(prefix.length()).trim();
        break;
      }
    }
    if (LibStr.isNotEmptyOrNull(record)) {
      final String[] values = record.split(colSep);
      setReturnedMeasures(values);
    }
    final Integer rc = result.getRetCode();
    if (rc != null) {
      mStatus.setValue(rc.intValue() == 0);
      mResult.setValue(rc.doubleValue());
    }
  }

  protected void setReturnedMeasures(final String[] returnedValues) {
    final String[] metricNames = pMetricNames.get();
    if (metricNames == null) { return; }
    final int numVal = returnedValues.length;
    final IMetric<?> m = mResult.getSplitting(BaseAction.SPLIT_GROUP);
    for (int i = 0; i < metricNames.length; i++) {
      final String name = metricNames[i];
      Double d;
      if (i < numVal) {
        d = Helper.getDouble(returnedValues[i], Double.NaN);
      }
      else {
        d = Double.NaN;
      }
      if (d != Double.NaN) {
        m.getSplitting(name).setValue(d);
      }
    }
  }

  public String getParameter() {
    return pParameter.get();
  }

  public void setParameter(final String param) {
    pParameter.set(param);
  }

}
