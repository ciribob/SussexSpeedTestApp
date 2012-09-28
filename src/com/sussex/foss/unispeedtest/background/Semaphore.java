package com.sussex.foss.unispeedtest.background;

/**
 * This is a simple semaphore used to stop multiple threads
 * 
 * @author Ciaran
 * 
 */
public class Semaphore
{
	private boolean flag = false;

	public Semaphore()
	{

	}

	public synchronized void take() throws InterruptedException
	{
		while (flag)
			wait();
		flag = true;
		this.notify();
	}

	public synchronized void release() throws InterruptedException
	{
		flag = false;
		this.notify();
	}

}
