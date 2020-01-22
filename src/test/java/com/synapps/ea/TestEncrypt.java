package com.synapps.ea;

import static org.junit.Assert.*;

import org.junit.Test;

import com.documentum.fc.common.DfException;
import com.documentum.fc.impl.util.RegistryPasswordUtils;

public class TestEncrypt {

	@Test
	public void testEncrypt() throws DfException {
		String password = "Ax3oJxy57E4j1F9dZIag";
		System.out.println(RegistryPasswordUtils.encrypt(password));
	}

	@Test
	public void testDecrypt() throws DfException {
		String password = "AAAAEN11kmQ7UmqZYxUT1iifhRvhfryLCua+rR6INGzzGnQdl+5v+4sFg5aby02R9WxFRg==";
		System.out.println(RegistryPasswordUtils.decrypt(password));
	}
	
}
