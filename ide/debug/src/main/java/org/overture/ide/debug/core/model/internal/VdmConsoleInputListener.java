package org.overture.ide.debug.core.model.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.overture.ide.debug.core.dbgp.IDbgpSession;
import org.overture.ide.debug.core.dbgp.exceptions.DbgpException;

public class VdmConsoleInputListener
{
	final IVdmStreamProxy proxy;
	final IDbgpSession session;
	Thread thread;

	public VdmConsoleInputListener(IDbgpSession session, IVdmStreamProxy proxy)
	{
		this.proxy = proxy;
		this.session = session;
	}

	public void start()
	{
		thread = new Thread(new Runnable()
		{

			public void run()
			{
				InputStream is = proxy.getStdin();
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					String line = null;
					while ((line = reader.readLine()) != null)
					{
						try
						{
							final String result = session.getExtendedCommands().execute(line).getValue();
							proxy.writeStdout(result);
						} catch (DbgpException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (UnsupportedEncodingException e)
				{
					//
				} catch (IOException e)
				{
					// dont't care
				}

			}
		});
		thread.start();
	}

	public void stop()
	{
		if (thread != null)
		{
			thread.stop();
		}
	}
}
