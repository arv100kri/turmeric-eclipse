package org.ebayopensource.turmeric.eclipse.maven.core.utils.tests;

import org.apache.maven.repository.metadata.ArtifactMetadata;
import org.ebayopensource.turmeric.eclipse.maven.core.utils.MavenCoreUtils;
import org.ebayopensource.turmeric.eclipse.mavenapi.impl.MavenEclipseApi;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.index.nexus.IndexedArtifactGroup;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndexManager;
import org.eclipse.m2e.core.internal.repository.RepositoryRegistry;
import org.eclipse.m2e.core.repository.IRepository;
import org.eclipse.m2e.core.repository.IRepositoryRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestMavenCoreUtils {

	private static MavenEclipseApi api;
	private RepositoryRegistry repositoryRegistry;
	private NexusIndexManager indexManager = (NexusIndexManager) MavenPlugin.getIndexManager();

	@Before
	public void setUp() throws Exception {
		repositoryRegistry = (RepositoryRegistry) MavenPlugin.getRepositoryRegistry();
	}

	@After
	public void tearDown() throws Exception {
		repositoryRegistry = null;
	}

	@Test
	public void testGetMavenOrgProviderInstance() {
		assertNotNull(MavenCoreUtils.getMavenOrgProviderInstance());
	}

	@Test
	public void testLibraryNameArtifactMetaData() throws Exception {
		ArtifactMetadata metaData = new ArtifactMetadata(
				"org.ebayopensource.turmeric",
				"org.ebayopensource.turmeric.eclipse.maven.core.tests",
				"1.0.0", "eclipse-test");
		String libName = MavenCoreUtils.libraryName(metaData);
		assertEquals(
				"org.ebayopensource.turmeric:org.ebayopensource.turmeric.eclipse.maven.core.tests:eclipse-test:1.0.0",
				libName);
	}

	@Test
	public void testRepository() {
		assertNotNull(repositoryRegistry);
	}

	@Test
	public void testIndexManager() {
		IRepository repository = getRepository("ebayopensource-releases", IRepositoryRegistry.SCOPE_SETTINGS);

		IndexedArtifactGroup iag = new IndexedArtifactGroup(repository,	"org.junit");
		IndexedArtifactGroup resolveGroup = indexManager.resolveGroup(iag);
		assertTrue(resolveGroup.getFiles().size() > 0);
	}

	private IRepository getRepository(String repositoryId, int scope) {
		for (IRepository repository : repositoryRegistry.getRepositories(scope)) {
			if (repositoryId.equals(repository.getId())) {
				return repository;
			}
		}
		return null;
	}
	
	

}
