/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.universAAL.context.sesame.sail.crypt;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.openrdf.sail.nativerdf.datastore.DataFile;
import org.openrdf.sail.nativerdf.datastore.DataStore;
import org.openrdf.sail.nativerdf.datastore.HashFile;
import org.openrdf.sail.nativerdf.datastore.IDFile;

import info.aduna.io.ByteArrayUtil;

/**
 * Class that provides indexed storage and retrieval of arbitrary length data.
 * 
 * @author Arjohn Kampman
 */
// Because fields are private we have to copy the whole content of DataStore
public class CryptDataStore extends DataStore{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final DataFile dataFile;

	private final IDFile idFile;

	private final HashFile hashFile;

	/**
	 * The checksum to use for calculating data hashes.
	 */
	private final CRC32 crc32 = new CRC32();

	private org.universAAL.context.sesame.sail.crypt.Codec codec =new Codec() {
	    public byte[] encode(byte[] data) {
		return org.bouncycastle.util.encoders.Base64
			.encode(data);
	    }

	    public byte[] decode(String data) {
		return org.bouncycastle.util.encoders.Base64
			.decode(data);
	    }
	};

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CryptDataStore(File dataDir, String filePrefix)
		throws IOException
	{
		this(dataDir, filePrefix, false);
	}

	public CryptDataStore(File dataDir, String filePrefix, boolean forceSync)
		throws IOException
	{
	    super(dataDir, filePrefix, forceSync); //This opens super files
	    super.clear(); // and this closes them. We want our own:
		dataFile = new DataFile(new File(dataDir, filePrefix + ".dat"), forceSync);
		idFile = new IDFile(new File(dataDir, filePrefix + ".id"), forceSync);
		hashFile = new HashFile(new File(dataDir, filePrefix + ".hash"), forceSync);
		File confHome = new File(new File(
			System.getProperty("bundles.configuration.location",
			System.getProperty("user.dir"))), 
			System.getProperty("sesame.uaal.store.keyfolder","mw.bus.model.osgi"));
		String cryptUtilInitMessage;
		try {
		    cryptUtilInitMessage = CryptUtil.init(confHome.getAbsolutePath(), codec);
		    System.out.println("Sesame uAAL Store: " + cryptUtilInitMessage);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the value for the specified ID.
	 * 
	 * @param id
	 *        A value ID, should be larger than 0.
	 * @return The value for the ID, or <tt>null</tt> if no such value could be
	 *         found.
	 * @throws Exception 
	 */
	public byte[] getData(int id)
		throws IOException
	{
		assert id > 0 : "id must be larger than 0, is: " + id;

		// Data not in cache or cache not used, fetch from file
		long offset = idFile.getOffset(id);

		if (offset != 0L) {
			byte[] crypt = dataFile.getData(offset);
			try {
			    return CryptUtil.decrypt(new String(crypt)).getBytes();
			} catch (Exception e) {
			    throw new IOException(e);
			}
		}

		return null;
	}

	/**
	 * Gets the ID for the specified value.
	 * 
	 * @param queryData
	 *        The value to get the ID for, must not be <tt>null</tt>.
	 * @return The ID for the specified value, or <tt>-1</tt> if no such ID could
	 *         be found.
	 * @throws Exception 
	 */
	public int getID(byte[] queryData)
		throws IOException
	{
		assert queryData != null : "queryData must not be null";

		int id = -1;

		// Value not in cache or cache not used, fetch from file
		int hash = getDataHash(queryData);
		HashFile.IDIterator iter = hashFile.getIDIterator(hash);
		try {
			while ((id = iter.next()) >= 0) {
				long offset = idFile.getOffset(id);
				byte[] crypt = dataFile.getData(offset);
				byte[] data;
				try {
				    data = CryptUtil.decrypt(new String(crypt)).getBytes();
				} catch (Exception e) {
				    throw new IOException(e);
				}
				if (Arrays.equals(queryData, data)) {
					// Matching data found
					break;
				}
			}
		}
		finally {
			iter.close();
		}

		return id;
	}

	/**
	 * Returns the maximum value-ID that is in use.
	 * 
	 * @return The largest ID, or <tt>0</tt> if the store does not contain any
	 *         values.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	public int getMaxID()
		throws IOException
	{
		return idFile.getMaxID();
	}

	/**
	 * Stores the supplied value and returns the ID that has been assigned to it.
	 * In case the data to store is already present, the ID of this existing data
	 * is returned.
	 * 
	 * @param data
	 *        The data to store, must not be <tt>null</tt>.
	 * @return The ID that has been assigned to the value.
	 * @throws Exception 
	 */
	public int storeData(byte[] data)
		throws IOException
	{
		assert data != null : "data must not be null";

		int id = getID(data);

		if (id == -1) {
			// Data not stored yet, store it under a new ID.
		    	byte[] crypt;
			try {
			    crypt = CryptUtil.encrypt(new String(data)).getBytes();
			} catch (Exception e) {
			    throw new IOException(e);
			}
			long offset = dataFile.storeData(crypt);
			id = idFile.storeOffset(offset);
			hashFile.storeID(getDataHash(data), id);
		}

		return id;
	}

	/**
	 * Synchronizes any recent changes to the data to disk.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void sync()
		throws IOException
	{
		hashFile.sync();
		idFile.sync();
		dataFile.sync();
	}

	/**
	 * Removes all values from the DataStore.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void clear()
		throws IOException
	{
		hashFile.clear();
		idFile.clear();
		dataFile.clear();
	}

	/**
	 * Closes the DataStore, releasing any file references, etc. In case a
	 * transaction is currently open, it will be rolled back. Once closed, the
	 * DataStore can no longer be used.
	 * 
	 * @exception IOException
	 *            If an I/O error occurred.
	 */
	public void close()
		throws IOException
	{
		hashFile.close();
		idFile.close();
		dataFile.close();
	}

	/**
	 * Gets a hash code for the supplied data.
	 * 
	 * @param data
	 *        The data to calculate the hash code for.
	 * @return A hash code for the supplied data.
	 */
	private int getDataHash(byte[] data) {
		synchronized (crc32) {
			crc32.update(data);
			int crc = (int)crc32.getValue();
			crc32.reset();
			return crc;
		}
	}

	/*--------------------*
	 * Test/debug methods *
	 *--------------------*/

//	public static void main(String[] args)
//		throws Exception
//	{
//		if (args.length < 2) {
//			System.err.println("Usage: java org.openrdf.sesame.sailimpl.nativerdf.datastore.DataStore <data-dir> <file-prefix>");
//			return;
//		}
//
//		System.out.println("Dumping DataStore contents...");
//		File dataDir = new File(args[0]);
//		CryptDataStore dataStore = new CryptDataStore(dataDir, args[1]);
//
//		DataFile.DataIterator iter = dataStore.dataFile.iterator();
//		while (iter.hasNext()) {
//			byte[] data = iter.next();
//
//			System.out.println(ByteArrayUtil.toHexString(data));
//		}
//	}
}
