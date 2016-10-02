package net.hycube.simulator.transport;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import net.hycube.core.UnrecoverableRuntimeException;
import net.hycube.transport.NetworkNodePointer;


public class SimNodePointer implements NetworkNodePointer, Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6480940695554099146L;
	
	
	public static final int ADDRESS_SIM_ID_LENGTH = 4;
	public static final int ADDRESS_SIM_NODE_ID_LENGTH = 16;
	public static final int ADDRESS_BYTE_LENGTH = ADDRESS_SIM_ID_LENGTH + ADDRESS_SIM_NODE_ID_LENGTH;
	
	public static final String NETWORK_ADDR_CHARSET = "ISO-8859-1";		//exactly one byte per character 
	
	
	public SimNodePointer(String address) {
		
		String[] addrSplit = validateNetworkAddress(address); 
		if (addrSplit == null) {
			throw new IllegalArgumentException("The input address string is invalid.");
		}
		this.addressString = address;
		this.simId = addrSplit[0];
		this.simNodeId = addrSplit[1];
		try {
			ByteBuffer buf = ByteBuffer.allocate(ADDRESS_BYTE_LENGTH);
			buf.put(this.simId.getBytes(NETWORK_ADDR_CHARSET));
			buf.put(this.simNodeId.getBytes(NETWORK_ADDR_CHARSET));
			this.addressBytes = buf.array();
		} catch (UnsupportedEncodingException e) {
			throw new UnrecoverableRuntimeException("An exception has been thrown while converting the network address to a byte array.", e);
		}
	}
	
	public SimNodePointer(byte[] addressBytes) {
		String[] addrSplit = validateNetworkAddress(addressBytes);
		if (addrSplit == null) {
			throw new IllegalArgumentException("The input address byte array represents an invalid address.");
		}
		this.addressString = net.hycube.utils.StringUtils.join(addrSplit, ":");
		this.simId = addrSplit[0];
		this.simNodeId = addrSplit[1];
		this.addressBytes = addressBytes;
		
	}
	
	public SimNodePointer(String simId, String simNodeId) {
		String[] addrSplit = validateNetworkAddress(simId, simNodeId);
		if (addrSplit == null) {
			throw new IllegalArgumentException("The input address string is invalid.");
		}
		this.addressString = net.hycube.utils.StringUtils.join(addrSplit, ":");
		this.simId = simId;
		this.simNodeId = simNodeId;
		try {
			ByteBuffer buf = ByteBuffer.allocate(ADDRESS_BYTE_LENGTH);
			buf.put(this.simId.getBytes(NETWORK_ADDR_CHARSET));
			buf.put(this.simNodeId.getBytes(NETWORK_ADDR_CHARSET));
			this.addressBytes = buf.array();
		} catch (UnsupportedEncodingException e) {
			throw new UnrecoverableRuntimeException("An exception has been thrown while converting the network address to a byte array.", e);
		}
		
	}

	
	
	protected String addressString;
	protected String simId;
	protected String simNodeId;
	protected byte[] addressBytes;
	
	
	
	
	@Override
	public String getAddressString() {
		return addressString;
	}
	
	@Override
	public byte[] getAddressBytes() {
		return addressBytes;
	}
	
	@Override
	public int getByteLength() {
		return SimNodePointer.getAddressByteLength();
	}
	
	public static int getAddressByteLength() {
		return ADDRESS_BYTE_LENGTH;
	}
	
	public String getSimId() {
		return simId;
	}
	
	public String getSimNodeId() {
		return simNodeId;
	}
	
	public static String[] validateNetworkAddress(String networkAddress) {
		try {
			String[] addrSplit = networkAddress.split(":");
			if (addrSplit.length != 2) return null;
			return validateNetworkAddress(addrSplit[0], addrSplit[1]);
		}
		catch (Exception e) {
			return null;
		}

	}

	
	public static String[] validateNetworkAddress(byte[] networkAddressBytes) {
		if (networkAddressBytes == null || networkAddressBytes.length != ADDRESS_BYTE_LENGTH) return null;
		String simId = null;
		String simNodeId = null;
		try {
			ByteBuffer buf = ByteBuffer.wrap(networkAddressBytes);
			byte[] simIdB = new byte[ADDRESS_SIM_ID_LENGTH];
			byte[] simNodeIdB = new byte[ADDRESS_SIM_NODE_ID_LENGTH];
			buf.get(simIdB);
			buf.get(simNodeIdB);
			simId = new String(simIdB, NETWORK_ADDR_CHARSET);
			simNodeId = new String(simNodeIdB, NETWORK_ADDR_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new UnrecoverableRuntimeException("An exception has been thrown while converting a byte array to the network address.", e);
		}
		return validateNetworkAddress(simId, simNodeId);
		
	}

	
	public static String[] validateNetworkAddress(String simId, String simNodeId) {
		String[] addrSplit = new String[] {simId, simNodeId};
		try {
			
			//!!the checks below were disabled, because they were a bottleneck in terms of memory-consuming
			//it is therefore assumed that the address has the expected format and simId and simNodeId are alphanumeric
//			if (!addrSplit[0].matches("[a-zA-z0-9]+")) return null;
//			if (!addrSplit[1].matches("[a-zA-z0-9]+")) return null;
			
			if (addrSplit[0].length() != ADDRESS_SIM_ID_LENGTH) return null;
			if (addrSplit[1].length() != ADDRESS_SIM_NODE_ID_LENGTH) return null;
		}
		catch (Exception e) {
			return null;
		}
		return addrSplit;
	}

	
	
}
