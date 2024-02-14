package com.lambdalogic.util.rcp.chunk;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;


/**
 * Class that executes an operation for a potential large number of items in chunks.
 * 
 * This class is intended for operations that have a Collection parameter that can contain a large amount of items.
 * E.g. an operation where a user can select items to delete them. Instead of doing the operation in one step 
 * (deleting all items at once) this class splits the items into chunks and executes the operation multiple times, once 
 * for each chunk.
 * E.g.: A user selects 333 items to delete them. With chunk size of 50 (the default chunk size), the ChunkExecutor 
 * executes the delete operation 7 times, the first 6 times with 50 items and the final one with 33 items.
 * While doing so a progress dialog is shown that allows the user to cancel the operation.
 * 
 * @param <Item>
 */
public abstract class ChunkExecutor<Item> {

	/**
	 * Default chunk size.
	 */
	public static final int DEFAULT_CHUNK_SIZE = 50;
	
	/**
	 * Chunk size.
	 */
	private int chunkSize = DEFAULT_CHUNK_SIZE;
	
	/**
	 * Optional message that is shown while the operation is executed.
	 */
	private String operationMessage;
	
	/**
	 * Optional error message that is shown if an exception occurs while executing the operation for a single chunk.
	 */
	private String errorMessage;

	
	private boolean cancelled = false;
	
	
	/**
	 * The operation that is executed several times, once for each chunk of items.
	 * @param chunkList
	 *  contains chunkSize items
	 * @throws Exception
	 */
	protected abstract void executeChunk(List<Item> chunkList) throws Exception;
	
	
	/**
	 * The items the operation is working on.
	 * @return
	 */
	protected abstract Collection<Item> getItems();
	
	
	public ChunkExecutor() {
		super();
	}


	/**
	 * Call @{link {@link #executeChunk(Collection)} for all items, but in chunks of {@link #chunkSize}.
	 */
	public void executeInChunks() {
		
		BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					Collection<Item> itemCol = getItems();
					
					String name = operationMessage;
					if (name == null) {
						name = UtilI18N.ExecutingAction;
					}
					monitor.beginTask(name, itemCol.size());
					
					List<Item> chunkList = createArrayList(chunkSize);
					
					Iterator<Item> iterator = itemCol.iterator();
					int count = 0;
					
					while (iterator.hasNext()) {
						// put up to chunkSize Profiles into profileChunkList
						chunkList.clear();
						while (
							chunkList.size() < chunkSize 
							&& 
							iterator.hasNext()
						) {
							Item item = iterator.next();
							chunkList.add(item);
						}
						
						count += chunkList.size();
						monitor.subTask(count + " " + UtilI18N._of_ + " " + itemCol.size());
						
						executeChunk(chunkList);
						
						
						monitor.worked(chunkList.size());
						if (cancelled || monitor.isCanceled()) {
							break;
						}
					}

					monitor.done();
				}
				catch (Throwable t) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, errorMessage);
				}
			}
		});

	}


	/**
	 * Get current chunk size.
	 * @return
	 */
	public int getChunkSize() {
		return chunkSize;
	}


	/**
	 * Set current chunk size.
	 * @param chunkSize
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}


	/**
	 * Get operation message.
	 * @return
	 */
	public String getOperationMessage() {
		return operationMessage;
	}


	/**
	 * Set operation message.
	 * @param operationMessage
	 */
	public void setOperationMessage(String operationMessage) {
		this.operationMessage = operationMessage;
	}


	/**
	 * Get error message.
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}


	/**
	 * Set error message.
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	
	protected void cancel() {
		cancelled = true;
	}
	
}
