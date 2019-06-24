/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeListener;
import org.datagear.dataexchange.TextDataImportException;
import org.datagear.dataexchange.UnsupportedExchangeException;
import org.datagear.dataexchange.support.ColumnNotFoundException;
import org.datagear.dataexchange.support.ExecuteDataImportSqlException;
import org.datagear.dataexchange.support.IllegalSourceValueException;
import org.datagear.dataexchange.support.SetImportColumnValueException;
import org.datagear.dataexchange.support.TableMismatchException;
import org.datagear.dataexchange.support.TableNotFoundException;
import org.datagear.dataexchange.support.UnsupportedSqlTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class CometdDataExchangeListener implements DataExchangeListener
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(CometdDataExchangeListener.class);

	public static final String EXCEPTION_DISPLAY_MESSAGE_KEY = "dataexchange.error.";

	private DataExchangeCometdService dataExchangeCometdService;

	private ServerChannel dataExchangeServerChannel;

	private MessageSource messageSource;

	private Locale locale;

	private volatile long _startTime = System.currentTimeMillis();

	public CometdDataExchangeListener()
	{
		super();
	}

	public CometdDataExchangeListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, MessageSource messageSource, Locale locale)
	{
		super();
		this.dataExchangeCometdService = dataExchangeCometdService;
		this.dataExchangeServerChannel = dataExchangeServerChannel;
		this.messageSource = messageSource;
		this.locale = locale;
	}

	public void setDataExchangeCometdService(DataExchangeCometdService dataExchangeCometdService)
	{
		this.dataExchangeCometdService = dataExchangeCometdService;
	}

	public ServerChannel getDataExchangeServerChannel()
	{
		return dataExchangeServerChannel;
	}

	public void setDataExchangeServerChannel(ServerChannel dataExchangeServerChannel)
	{
		this.dataExchangeServerChannel = dataExchangeServerChannel;
	}

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	@Override
	public void onStart()
	{
		this._startTime = System.currentTimeMillis();
		sendMessage(buildStartMessage());
	}

	@Override
	public void onException(DataExchangeException e)
	{
		sendMessage(buildExceptionMessage(e));
	}

	@Override
	public void onSuccess()
	{
		sendMessage(buildSuccessMessage());
	}

	@Override
	public void onFinish()
	{
		long duration = System.currentTimeMillis() - this._startTime;
		sendMessage(buildFinishMessage(duration));
	}

	/**
	 * 发送消息。
	 * 
	 * @param dataExchangeMessage
	 */
	protected void sendMessage(DataExchangeMessage dataExchangeMessage)
	{
		try
		{
			this.dataExchangeCometdService.sendMessage(this.dataExchangeServerChannel, dataExchangeMessage);
		}
		catch (Throwable t)
		{
			LOGGER.error("send message error", dataExchangeMessage);
		}
	}

	/**
	 * 解析数据交换异常I18N消息。
	 * 
	 * @param e
	 * @return
	 */
	protected String resolveDataExchangeExceptionI18n(DataExchangeException e)
	{
		String message = "";

		String code = buildDataExchangeExceptionI18nCode(e);

		if (e instanceof ColumnNotFoundException)
		{
			ColumnNotFoundException e1 = (ColumnNotFoundException) e;
			message = getI18nMessage(code, e1.getTable(), e1.getColumnName());
		}
		else if (e instanceof TableMismatchException)
		{
			TableMismatchException e1 = (TableMismatchException) e;
			message = getI18nMessage(code, e1.getTable());
		}
		else if (e instanceof TableNotFoundException)
		{
			TableNotFoundException e1 = (TableNotFoundException) e;
			message = getI18nMessage(code, e1.getTable());
		}
		else if (e instanceof ExecuteDataImportSqlException)
		{
			ExecuteDataImportSqlException e1 = (ExecuteDataImportSqlException) e;
			message = getI18nMessage(code, e1.getDataIndex() + 1, e1.getCause().getMessage());
		}
		else if (e instanceof IllegalSourceValueException)
		{
			IllegalSourceValueException e1 = (IllegalSourceValueException) e;
			message = getI18nMessage(code, e1.getDataIndex() + 1, e1.getColumnName(), e1.getSourceValue());
		}
		else if (e instanceof SetImportColumnValueException)
		{
			SetImportColumnValueException e1 = (SetImportColumnValueException) e;
			message = getI18nMessage(code, e1.getDataIndex() + 1, e1.getColumnName(), e1.getSourceValue());
		}
		else if (e instanceof TextDataImportException)
		{
			TextDataImportException e1 = (TextDataImportException) e;
			message = getI18nMessage(code, e1.getDataIndex() + 1);
		}
		else if (e instanceof UnsupportedExchangeException)
		{
			message = getI18nMessage(code);
		}
		else if (e instanceof UnsupportedSqlTypeException)
		{
			UnsupportedSqlTypeException e1 = (UnsupportedSqlTypeException) e;
			message = getI18nMessage(code, e1.getSqlType());
		}
		else
			message = getI18nMessage(code);

		return message;
	}

	/**
	 * 构建数据交换异常I18N消息码。
	 * 
	 * @param e
	 * @return
	 */
	protected String buildDataExchangeExceptionI18nCode(DataExchangeException e)
	{
		return EXCEPTION_DISPLAY_MESSAGE_KEY + e.getClass().getSimpleName();
	}

	/**
	 * 获取I18N消息。
	 * 
	 * @param code
	 * @param args
	 * @return
	 */
	protected String getI18nMessage(String code, Object... args)
	{
		try
		{
			return this.messageSource.getMessage(code, args, this.locale);
		}
		catch (Throwable t)
		{
			return "???" + code + "???";
		}
	}

	/**
	 * 构建开始消息。
	 * 
	 * @return
	 */
	protected abstract DataExchangeMessage buildStartMessage();

	/**
	 * 构建异常消息。
	 * 
	 * @param e
	 * @return
	 */
	protected abstract DataExchangeMessage buildExceptionMessage(DataExchangeException e);

	/**
	 * 构建成功消息。
	 * 
	 * @return
	 */
	protected abstract DataExchangeMessage buildSuccessMessage();

	/**
	 * 构建完成消息。
	 * 
	 * @param duration
	 * @return
	 */
	protected abstract DataExchangeMessage buildFinishMessage(long duration);
}