package org.purl.storage.util;

import junit.framework.TestCase;

public class StorageHelper_TEST extends TestCase {
	public void testUnrelatedURIs() {
		String input = "ffcpl:/net/people/johnson";
		assertEquals(input, StorageHelper.convertForStorage(input));
	}
	
	public void testApostrophes() {
		String input = "ffcpl:/net/people/murphy's";
		assertEquals("ffcpl:/net/people/murphy''s", StorageHelper.convertForStorage(input));		
	}

	public void testPlus() {
		String input = "ffcpl:/net/people/johnson%2Bjohnson";
		assertEquals("ffcpl:/net/people/johnson+johnson", StorageHelper.convertForStorage(input));				
	}
	
	public void testCompound() {
		String input = "ffcpl:/net/people/murphy's%2Bjohnson";
		assertEquals("ffcpl:/net/people/murphy''s+johnson", StorageHelper.convertForStorage(input));						
	}
}
