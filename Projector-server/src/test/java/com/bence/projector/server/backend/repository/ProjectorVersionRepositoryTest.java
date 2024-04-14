package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.ProjectorVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class ProjectorVersionRepositoryTest {

    @Autowired
    private ProjectorVersionRepository projectorVersionRepository;

    @Test
    public void testFindAllByVersionIdGreaterThan() {
        ProjectorVersion projectorVersion = getAProjectorVersion();
        projectorVersionRepository.save(projectorVersion);
        List<ProjectorVersion> projectorVersions = getProjectorVersionsAsList(projectorVersionRepository.findAll());
        Assert.assertEquals(1, projectorVersions.size());
        ProjectorVersion version = projectorVersions.get(0);
        Assert.assertEquals(projectorVersion.getVersion(), version.getVersion());
    }

    private List<ProjectorVersion> getProjectorVersionsAsList(Iterable<ProjectorVersion> projectorVersionIterable) {
        ArrayList<ProjectorVersion> projectorVersions = new ArrayList<>();
        for (ProjectorVersion projectorVersion : projectorVersionIterable) {
            projectorVersions.add(projectorVersion);
        }
        return projectorVersions;
    }

    private ProjectorVersion getAProjectorVersion() {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("test");
        return projectorVersion;
    }
}